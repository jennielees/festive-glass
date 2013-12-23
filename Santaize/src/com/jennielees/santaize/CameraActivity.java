package com.jennielees.santaize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
//import com.google.android.glass.touchpad.Gesture;
//import com.google.android.glass.touchpad.GestureDetector;

public class CameraActivity extends Activity implements CvCameraViewListener2 {

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
	
	private Rect[] 				   facesArray;
	private Mat 				   mSantaHat;
	private FrameLayout			   rootLayout;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  //      gestureDetector = createGestureDetector(this);
        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        
        setContentView(R.layout.activity_camera);

        rootLayout = (FrameLayout) findViewById(R.id.root_layout);
        

        mOpenCvCameraView = (FaceSurfaceView) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

    }
    
    @Override
    protected void onDestroy() {
    	if (cameraOpen) {
    		camera.stopPreview();
        	camera.release();
        	cameraOpen = false;
    	}
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	if (cameraOpen) {
    		camera.stopPreview();
    		camera.release();
    		cameraOpen = false;
    	}
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();

        // Initialise surface for preview
//        surface = new FaceSurfaceView(this);
        
//        setContentView(surface);
        
//        holder = surface.getHolder();
        
//        holder.setFixedSize(640,360);
        
//        holder.addCallback(this);
        
       
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

    	
    }
    
    // Shutter button override
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
        	if (cameraOpen) {
        		camera.stopPreview();
        		camera.release();
        		cameraOpen = false;
        	}
            // Stop the preview and release the camera.
            // Execute your logic as quickly as possible
            // so the capture happens quickly.
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    
    public boolean takeCameraPhoto() {
    //	camera.takePicture(this, null, null, this);
    	return true;
    }
    /*
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    // do something on tap
                	
                	// take a photo
                	takeCameraPhoto();
                	//
                    return true;
                } else if (gesture == Gesture.TWO_TAP) {
                    // do something on two finger tap
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    // do something on right (forward) swipe
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    // do something on left (backwards) swipe
                    return true;
                }
                return false;
            }
        });
        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
              // do something on finger count changes
            }
        });
        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                // do something on scrolling
            	return true;
            }
        });
        return gestureDetector;
    }*/

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (gestureDetector != null) {
       //     return gestureDetector.onMotionEvent(event);
        }
        return false;
    }
    
    // Camera callbacks
    public void onShutter() {
    	
    }
    /*
    public void onPictureTaken(byte[] img, Camera cam) {
    	// can now re-resume the camera or do something else.
    	// should show the image and have a new onTap listener
    	// to share it.
    	
    	Bitmap bmp;
    	bmp = BitmapFactory.decodeByteArray(img, 0, img.length);
    	
    	Bitmap mBmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
    	Canvas c = new Canvas(mBmp);
    	
    	FaceDetector f = new FaceDetector(mBmp.getWidth(), mBmp.getHeight(), 25);
    	Face[] faces = new Face[25];
    	int numFaces = f.findFaces(mBmp, faces);
    	
    	Log.d("LookingGlass", numFaces + " faces found");
    	
    }*/
    
    public void onFaceDetection(Face[] faces, Camera cam) {
    	// face.rect
    	// http://developer.android.com/reference/android/hardware/Camera.Face.html
    	Canvas canvas = holder.lockCanvas();
    	
    	Paint paint = new Paint();
    	paint.setColor(Color.rgb(255, 255, 255));
    	paint.setStrokeWidth(3);
    	
    	    	
    	for (Face f : faces) {
    		
    		 Matrix matrix = new Matrix();
    		 // This is the value for android.hardware.Camera.setDisplayOrientation.
    		 // matrix.postRotate(displayOrientation);
    		 // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
    		 // UI coordinates range from (0, 0) to (width, height).
    		 matrix.postScale(surface.getWidth() / 2000f, surface.getHeight() / 2000f);
    		 matrix.postTranslate(surface.getWidth() / 2f, surface.getHeight() / 2f);
    		
    	//	 RectF r = new RectF(f.rect);
    	//	 matrix.mapRect(r);
    		 
    	//	 canvas.drawRect(r, paint);
    	}
    }
    
    /*
     * Begin OpenCV experiments
     */
    
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

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.enableFpsMeter();
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        Log.d(TAG, "Camera View Started");
        mOpenCvCameraView.setPreviewFps();
     //   mOpenCvCameraView.setEffect("neon");
        
        try {
        	mSantaHat = Utils.loadResource(this, R.drawable.santa_hat, Highgui.CV_LOAD_IMAGE_COLOR);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }
    
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

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
//        for (int i = 0; i < facesArray.length; i++) {
//            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//        }
       // mRgba = drawSantaHats(mRgba, facesArray);
        drawSantaImageViews(mRgba.width(), mRgba.height());
        return mRgba;
    }
    
    private Mat drawSantaHats(Mat cameraImage, Rect[] faces) {
    	for (int i=0; i < faces.length; i++) {
    		Rect face = faces[i];
    		// ensure the santa hat is the right size
    		Mat localSanta = new Mat();
    		Imgproc.resize(mSantaHat, localSanta, new Size(face.width, face.height));
    		// move up height pixels
    		
    		Mat faceArea = cameraImage.submat(face);
    		localSanta.copyTo(faceArea); // deal with transparency?
    		//Log.d(TAG, "Drawing santa hat");
    	}
    	return cameraImage;
    }
    
	    
	public void drawSantaImageViews(double mWidth, double mHeight) {
		Log.d(TAG, "Drawing santas");
		
		final ImageView[] santas = new ImageView[facesArray.length];
		final FrameLayout.LayoutParams[] paramses = new FrameLayout.LayoutParams[facesArray.length];
		
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
			
			final FrameLayout.LayoutParams fParams = params;
			final ImageView fImage = tmpFace;
			
			santas[i] = fImage;
			paramses[i] = fParams;
			
		}
		
		runOnUiThread(new Runnable() {
			
		    public void run() {
				clearImageView(rootLayout);
				for (int i = 0; i < santas.length; i++) {
					rootLayout.addView(santas[i], paramses[i]);
				}
		    }
		});

	}
		 
	private void clearImageView(ViewGroup v) {
	    boolean doBreak = false;
	    while (!doBreak) {
	        int childCount = v.getChildCount();
	        int i;
	        for(i=0; i<childCount; i++) {
	            View currentChild = v.getChildAt(i);
	            // Change ImageView with your disired type view
	            if (currentChild instanceof ImageView) {
	                v.removeView(currentChild);
	                break;
	            }
	        }
	
	        if (i == childCount) {
	            doBreak = true;
	        }
	    }
	}

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
        
        Toast.makeText(this, String.format("Face size: %.0f%%", mRelativeFaceSize*100.0f), Toast.LENGTH_SHORT).show();
    }

}
