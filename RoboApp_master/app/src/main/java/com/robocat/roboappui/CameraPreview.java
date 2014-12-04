package com.robocat.roboappui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    //private Rect[] mFaces = null;
    //private Canvas mCanvas;
    //private Paint mPaint;



    private String TAG = "Camera Preview";

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        try {
            mHolder.addCallback(this);
        } catch (Exception e) {
            Log.e(TAG, "addCallBack");
        }

    }

    /**
     * Tells the camera where to draw the camera preview
     *
     * @param holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            //mCamera.startFaceDetection();

        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        } catch (NullPointerException N) {
            Log.e(TAG, "Error setting camera preview Null: " + N.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {

            //mCamera.stopFaceDetection();
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

            //mCamera.startFaceDetection();


        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    /*public void onFaceDetection(Face[] faces, Camera mCamera) {
        // For all faces in faces, draw a new rectangle, using their faces.rect values.
        Rect tRect[] = new Rect[faces.length];  // declaring temporary rect array to build.

        for (int i = 0; i < faces.length; i++) {
            // for each element, copy that into tRect.
            tRect[i] = faces[i].rect;
        }

        // Moving the temporary array into our main context.
        mFaces = tRect;

        // set up paintbrush for this instance.
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);

        // Draw each rect to the screen.
        for (int i = 0; i < mFaces.length; i++) {
            mCanvas.drawRect(mFaces[i], mPaint);
        }


        return;
    }*/
}




