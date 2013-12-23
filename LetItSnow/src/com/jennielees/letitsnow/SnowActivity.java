package com.jennielees.letitsnow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class SnowActivity extends Activity implements OnClickListener, Callback, ShutterCallback, PictureCallback {

	Camera camera;
	SurfaceView surface;
	SurfaceHolder holder;
	private GestureDetector gestureDetector;
	boolean cameraOpen = false;
	
	SnowflakeView flakeView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.activity_snow);
		FrameLayout root_layout = (FrameLayout) findViewById(R.id.root_layout);
		
		flakeView = new SnowflakeView(this);
	    root_layout.addView(flakeView);
	    // For phone version
	    flakeView.setOnClickListener(this);
	    createGestureDetector(this);
	}
	
	public void onClick(View v) {
		if (v == flakeView) {
			takeCameraPhoto();
		}
	}

	

    @Override
    protected void onDestroy() {
    	if (cameraOpen) {
    		camera.stopPreview();
        	camera.release();
        	cameraOpen = false;
    	}

    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	if (cameraOpen) {
    		camera.stopPreview();
    		camera.release();
    		cameraOpen = false;
    	}
        flakeView.pause();
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();

        // Initialise surface for preview
        surface = (SurfaceView) findViewById(R.id.fd_activity_surface_view);
        
        // setContentView(surface);
        
        holder = surface.getHolder();
        
      //  holder.setFixedSize(640,360);
        // this size restriction was causing problems with N4 testing
        
        holder.addCallback(this);

    	flakeView.resume();
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
    	camera.takePicture(this, null, null, this);
    	return true;
    }
    
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
                	Log.d("LetItSnow", "Taking a photo!");
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
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
     //   if (gestureDetector != null) {
       //     return gestureDetector.onMotionEvent(event);
       // }
        return false;
    }
    
    // Camera callbacks
    public void onShutter() {
    	
    }
    
	
	private static File getOutputMediaFile() {
	    String state = Environment.getExternalStorageState();
	    if (!state.equals(Environment.MEDIA_MOUNTED)) {
	        return null;
	    }
	    else {
	        File folder_gui = new File(Environment.getExternalStoragePublicDirectory(
	                Environment.DIRECTORY_PICTURES) + File.separator + "Snow");
	        if (!folder_gui.exists()) {
	            Log.v("LetItSnow", "Creating folder: " + folder_gui.getAbsolutePath());
	            folder_gui.mkdirs();
	        }
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	        Date date = new Date();
	        String timeStamp = dateFormat.format(date);
	        String filename = "snow" + timeStamp + ".jpg";
	        File outFile = new File(folder_gui, filename);
	        Log.v("LetItSnow", "Returning file: " + outFile.getAbsolutePath());
	        return outFile;
	    }
	}
    
    public void onPictureTaken(byte[] img, Camera cam) {
    	// can now re-resume the camera or do something else.
    
    	Log.d("LetItSnow", "Picture taken");
    	
    	// freeze the contents of the snowflake view
    	
    	Bitmap cameraImage = BitmapFactory.decodeByteArray(img, 0, img.length);
    
    	Bitmap snowSnapshot = flakeView.snapshot(); // Maybe pass canvas?
    	
    	Bitmap combinedImage = Bitmap.createBitmap(cameraImage.getWidth(), cameraImage.getHeight(), cameraImage.getConfig());
    	Canvas canvas = new Canvas(combinedImage);
    	canvas.drawBitmap(cameraImage, 0, 0, null);
    	canvas.drawBitmap(snowSnapshot, 0, 0, null);
    	
    	// save to file
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.v("LetItSnow", "Error creating output file");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            combinedImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.v("LetItSnow", e.getMessage());
        } catch (IOException e) {
            Log.v("LetItSnow", e.getMessage());
        }
    	
        // TODO: Figure out how the hell to make this a card, unless it happens automatically
        // Alternatively, switch into "Card mode" post-shutter. Gesture becomes share action.
        
    	camera.startPreview();
    	
    }
    


    
    // SurfaceHolder callbacks
    
    public void surfaceChanged(SurfaceHolder sHolder, int format, int w, int h) {


    	try {
        	camera = Camera.open();
        	cameraOpen = true;
    		camera.setPreviewDisplay(holder);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
    	try { // This will fail if not on Glass
	        Camera.Parameters parameters = camera.getParameters();
	        parameters.setPreviewFpsRange(30000, 30000);
	        parameters.setPreviewSize(640, 360);
	        camera.setParameters(parameters);
    	}
    	catch (RuntimeException e) {
    		e.printStackTrace();
    		Log.d("LetItSnow", "You should write better code than this");
    	}
        camera.startPreview();
        
    }
    
    public void surfaceCreated(SurfaceHolder sHolder) {
    	
    
    	
    }
    
    public void surfaceDestroyed(SurfaceHolder sHolder) {

		//camera.stopPreview();
    }
	
}
