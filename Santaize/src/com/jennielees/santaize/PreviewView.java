package com.jennielees.santaize;

import java.util.List;

import java.io.FileOutputStream;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.util.AttributeSet;


public class PreviewView extends JavaCameraView implements PictureCallback {

    private String mPictureFileName = "temp.jpg";

	public PreviewView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d("LookingGlass", "PreviewView init");
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	return super.onTouchEvent(event);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	}
	
	public void setPreviewFps() {
		Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange(30000, 30000);	
        mCamera.setParameters(params);
        Log.d("LookingGlass", "Preview FPS set.");
	}

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }
    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }
	
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i("LookingGlass", "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);

            fos.write(data);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("LookingGlass", "Exception in photoCallback", e);
        }

    }

}