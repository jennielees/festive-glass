/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jennielees.letitsnow;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * This class is the custom view where all of the DroidSnowFlakes are drawn. This class has
 * all of the logic for adding, subtracting, and rendering DroidSnowFlakes.
 */
public class SnowflakeView extends View {

    Bitmap droid;       // The bitmap that all SnowFlakes use
    int numSnowFlakes = 0;  // Current number of SnowFlakes
    ArrayList<SnowFlake> SnowFlakes = new ArrayList<SnowFlake>(); // List of current SnowFlakes

    // Animator used to drive all separate SnowFlake animations. Rather than have potentially
    // hundreds of separate animators, we just use one and then update all SnowFlakes for each
    // frame of that single animation.
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    long startTime, prevTime; // Used to track elapsed time for animations and fps
    int frames = 0;     // Used to track frames per second
    Paint textPaint;    // Used for rendering fps text
    float fps = 0;      // frames per second
    Matrix m = new Matrix(); // Matrix used to translate/rotate each SnowFlake during rendering
    String fpsString = "";
    String numSnowFlakesString = "";

    /**
     * Constructor. Create objects used throughout the life of the View: the Paint and
     * the animator
     */
    public SnowflakeView(Context context) {
        super(context);
        droid = BitmapFactory.decodeResource(getResources(), R.drawable.blob);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);

        // This listener is where the action is for the flak animations. Every frame of the
        // animation, we calculate the elapsed time and update every SnowFlake's position and rotation
        // according to its speed.
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                long nowTime = System.currentTimeMillis();
                float secs = (float)(nowTime - prevTime) / 1000f;
                prevTime = nowTime;
                for (int i = 0; i < numSnowFlakes; ++i) {
                    SnowFlake SnowFlake = SnowFlakes.get(i);
                    SnowFlake.y += (SnowFlake.speed * secs);
                    if (SnowFlake.y > getHeight()) {
                        // If a SnowFlake falls off the bottom, send it back to the top
                        SnowFlake.y = 0 - SnowFlake.height;
                    }
                    // Add a tiny bit of x perturbation too.
                    double r = Math.random();
                    if (r > 0.8) {
                        SnowFlake.x += -1 + Math.random() *2;                    	
                    }
                    
                    SnowFlake.rotation = SnowFlake.rotation + (SnowFlake.rotationSpeed * secs);
                }
                // Force a redraw to see the SnowFlakes in their new positions and orientations
                invalidate();
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(3000);
    }

    public Bitmap snapshot() {
    	// Thanks to http://stackoverflow.com/questions/5536066/convert-view-to-bitmap-on-android
    	Bitmap returnedBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
    	Canvas canvas = new Canvas(returnedBitmap);
    	Drawable bgDrawable = this.getBackground();
    	if (bgDrawable != null) {
    		bgDrawable.draw(canvas);
    	}
    	this.draw(canvas);
    	return returnedBitmap;
    }
    
    int getNumSnowFlakes() {
        return numSnowFlakes;
    }

    private void setNumSnowFlakes(int quantity) {
        numSnowFlakes = quantity;
        numSnowFlakesString = "numSnowFlakes: " + numSnowFlakes;
    }

    /**
     * Add the specified number of droidSnowFlakes.
     */
    void addSnowFlakes(int quantity) {
        for (int i = 0; i < quantity; ++i) {
            SnowFlakes.add(SnowFlake.createFlake(getWidth(), droid));
        }
        setNumSnowFlakes(numSnowFlakes + quantity);
    }

    /**
     * Subtract the specified number of droidSnowFlakes. We just take them off the end of the
     * list, leaving the others unchanged.
     */
    void subtractSnowFlakes(int quantity) {
        for (int i = 0; i < quantity; ++i) {
            int index = numSnowFlakes - i - 1;
            SnowFlakes.remove(index);
        }
        setNumSnowFlakes(numSnowFlakes - quantity);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Reset list of droidSnowFlakes, then restart it with 8 SnowFlakes
        SnowFlakes.clear();
        numSnowFlakes = 0;
        addSnowFlakes(68);
        // Cancel animator in case it was already running
        animator.cancel();
        // Set up fps tracking and start the animation
        startTime = System.currentTimeMillis();
        prevTime = startTime;
        frames = 0;
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // For each SnowFlake: back-translate by half its size (this allows it to rotate around its center),
        // rotate by its current rotation, translate by its location, then draw its bitmap
        for (int i = 0; i < numSnowFlakes; ++i) {
            SnowFlake SnowFlake = SnowFlakes.get(i);
            m.setTranslate(-SnowFlake.width/2, -SnowFlake.height/2);
            m.postRotate(SnowFlake.rotation);
            m.postTranslate(SnowFlake.width/2 + SnowFlake.x, SnowFlake.height/2 + SnowFlake.y);
            canvas.drawBitmap(SnowFlake.bitmap, m, null);
        }
        // fps counter: count how many frames we draw and once a second calculate the
        // frames per second
        ++frames;
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - startTime;
        if (deltaTime > 1000) {
            float secs = (float) deltaTime / 1000f;
            fps = (float) frames / secs;
            fpsString = "fps: " + fps;
            startTime = nowTime;
            frames = 0;
        }
        canvas.drawText(numSnowFlakesString, getWidth() - 300, getHeight() - 50, textPaint);
        canvas.drawText(fpsString, getWidth() - 300, getHeight() - 80, textPaint);
    }

    public void pause() {
        // Make sure the animator's not spinning in the background when the activity is paused.
        animator.cancel();
    }

    public void resume() {
        animator.start();
    }

}