package com.jennielees.santaize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.jennielees.lookingglass.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class SantaActivity extends Activity {

    private static final String    TAG                 = "LookingGlass";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private FaceSurfaceView			   mOpenCvCameraView;
    
	Camera camera;
	SurfaceView surface;
	SurfaceHolder holder;
	private GestureDetector gestureDetector;
	boolean cameraOpen = false;
	private Rect[]					facesArray;
	
	private Mat 				   mSantaHat;
	private FrameLayout rootLayout;
	
	  @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	  //      gestureDetector = createGestureDetector(this);
	        
	        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

	        setContentView(R.layout.activity_santa);

	        rootLayout = (FrameLayout) findViewById(R.id.linearLayout);
	        
	        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

	    	
	    }

    

	    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
	        @Override
	        public void onManagerConnected(int status) {
	            switch (status) {
	                case LoaderCallbackInterface.SUCCESS:
	                {
	                    Log.i(TAG, "OpenCV loaded successfully");

	                    try {
	                        // load cascade file from application resources
	                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
	                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
	                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
	                        FileOutputStream os = new FileOutputStream(mCascadeFile);

	                        byte[] buffer = new byte[4096];
	                        int bytesRead;
	                        while ((bytesRead = is.read(buffer)) != -1) {
	                            os.write(buffer, 0, bytesRead);
	                        }
	                        is.close();
	                        os.close();

	                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
	                        if (mJavaDetector.empty()) {
	                            Log.e(TAG, "Failed to load cascade classifier");
	                            mJavaDetector = null;
	                        } else
	                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

	                        cascadeDir.delete();

	                    } catch (IOException e) {
	                        e.printStackTrace();
	                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
	                    }



	        	        /*
	        	        try {
	        	        	mSantaHat = Utils.loadResource(getApplicationContext(), R.drawable.santa2, Highgui.CV_LOAD_IMAGE_COLOR);
	        	        }
	        	        catch (Exception e) {
	        	        	e.printStackTrace();
	        	        }
	        	        */
	                    
	        	        
	        	        // load the face and draw the mat
	        	        Mat m = detectFace();
	        	        Bitmap bm =  Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
	        	        
	        	        Utils.matToBitmap(m, bm);

	        	        ImageView iv = (ImageView) findViewById(R.id.imageView);
	        	        iv.setImageBitmap(bm);
	        	        
	        	        Size s = m.size();
	                    
	        	        drawSantaImageViews(s.width, s.height);
	                    
	                } break;
	                default:
	                {
	                    super.onManagerConnected(status);
	                } break;
	            }
	        }
	    };
	    
	public void drawSantaImageViews(double mWidth, double mHeight) {
		Log.d(TAG, "Drawing santas");
		for (int i = 0; i < facesArray.length; i++ ) {
			ImageView tmpFace = new ImageView(this);
			
			// Scale the imageview appropriately.
			double width = facesArray[i].width;
			double height = facesArray[i].height;
			

			DisplayMetrics metrics = getResources().getDisplayMetrics();
			int sWidth = metrics.widthPixels;
			
			
			// Scale width and height
			width = width * sWidth/mWidth;
			height = height * 768/mHeight;
			
			// Make hat a bit smaller than face
			width = width * 0.9;
			height = height * 0.9;
			
			// could calculate face center and go from there instead
			
			tmpFace.setScaleType(ScaleType.FIT_END);
			
			tmpFace.setImageResource(R.drawable.santa_hat);
			tmpFace.setVisibility(ImageView.VISIBLE);
			
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)width, (int)height);
			
			// Calculate the right X and Y co-ords
			// X and Y are based off matrix size, not screen size
			// Nexus 4 is 1280 x 768 so cheating (shouldn't be an issue w. video)
			
			double x = facesArray[i].x;
			double y = facesArray[i].y;
			
			x = x * sWidth/mWidth;
			y = y * 768/mHeight;
			
			x = x + width * 0.05; // fake centering since we scaled
			y = y - height*0.65;
			
			params.setMargins((int)x, (int)y, 0, 0);
			
			Log.d(TAG, "Drawing santa hat at x " + x + ", y " + y);
			
			rootLayout.addView(tmpFace, params);
		}
	}
	  
    public Mat detectFace() {

//        mRgba = inputFrame.rgba();
//        mGray = inputFrame.gray();
    	Mat mRgba = new Mat();
    	Mat mGray = new Mat();
    	
    	try {
    		mRgba = Utils.loadResource(this, R.drawable.one_direction, Highgui.CV_LOAD_IMAGE_COLOR);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return null;
    		
    	}
    	Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGR2GRAY); // might be BGR
    	
        //Imgproc.equalizeHist(mGray, mGray);
        //Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 0.0);
        
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
     //   mRgba = drawSantaHats(mRgba, facesArray);
        return mRgba;
    }
    
  /*  private Mat drawSantaHats(Mat cameraImage, Rect[] faces) {
    	for (int i=0; i < faces.length; i++) {
    		Rect face = faces[i];
    		int y = face.y;
    		Log.d("LookingGlass", "y is " + y);
    		int newY = y - face.height;
    		Point p = new Point(face.x, newY);
    		face = face + p;
    		
    		
    		// ensure the santa hat is the right size
    		Mat localSanta = new Mat();
    		Imgproc.resize(mSantaHat, localSanta, new Size(face.width, face.height));
    		// move up height pixels
    		
    		
    		Mat faceArea = cameraImage.submat(face);
    		Mat tempCopy = new Mat();
    		faceArea.copyTo(tempCopy);
    		Mat tempOut = new Mat();
    		Core.addWeighted(tempCopy, 1, localSanta, 1, 1, tempOut );
    		
    		tempOut.copyTo(faceArea); // deal with transparency?
    		//Log.d(TAG, "Drawing santa hat");
    	}
    	return cameraImage;
    }
*/
    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
        
        Toast.makeText(this, String.format("Face size: %.0f%%", mRelativeFaceSize*100.0f), Toast.LENGTH_SHORT).show();
    }

}
