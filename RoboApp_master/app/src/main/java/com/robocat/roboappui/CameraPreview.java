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


//Camera Preview Class
//Interface for previewing a camera's picture before taking a picture.
//This class provides the framework for handling the
//creation, destruction, and modification of the SurfaceHolder for the camera.
//Uses Callback functions to handle each event.

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String TAG = "Camera Preview";



    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        mHolder = getHolder();

        try {
            mHolder.addCallback(this);
        } catch (Exception e) {         // Adding the Callback functions has failed
            Log.e(TAG, "addCallBack");
        }

    }


    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);  // Android Camera class
            mCamera.startPreview();         // Android Camera class

            //mCamera.startFaceDetection();

        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        } catch (NullPointerException N) {
            Log.e(TAG, "Error setting camera preview Null: " + N.getMessage());
        }
    }
        
    public void surfaceDestroyed(SurfaceHolder holder) {
/*      // surfaceDestroyed should safely stop a camera preview if your surface is deleted.
        // A surface may potentially be destroyed even if no camera has been started.

        // stop preview before destroying surface
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // If there was no preview attached to the surface, surface is safe to destroy.
        }
*/
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
            mCamera.stopPreview();
        } catch (Exception e) {
            // If there was no preview attached to the surface, surface is safe to destroy.
        }

        // parameters format, w, and h can be implemented here to perform changes to your surface.
        // we currently implement no changes (rotations, expansion, reduction, inversion, etc.)

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

}




