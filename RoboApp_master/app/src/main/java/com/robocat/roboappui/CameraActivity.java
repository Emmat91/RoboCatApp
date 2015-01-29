package com.robocat.roboappui;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.hardware.Camera.CameraInfo;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;

    private String TAG = "Camera Activity";

    /**
     * sets fragment viewed to the camera activity
     * Initializes mCamera and mPreview
     * catch any creation errors and tell user
     * create listener for exit button which also releases camera and mediarecorder access
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        try{
            mCamera = getCameraInstance();

            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }


        catch (Exception e){
            Log.e(TAG, "error in creation");
        }


        Button button1 = (Button) findViewById(R.id.button_capture);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseMediaRecorder();
                releaseCamera();
                finish();
            }
        });

    }

    /**
     * safely access the front camera
     * if not accessible, catch error to prevent app crash
     */
    public static Camera getCameraInstance(){


        Camera c = null;
        try {
            c = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
        }


        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Releases the Camera and MediaRecorder if app is not active to prevent failure of other apps
     */
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }

    /**
     * Releases the Camera and MediaRecorder if the app crashes or is terminated
     */
    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaRecorder();
        releaseCamera();
    }

    /**
     * Not implemented but applicable for
     * future implementation of a MediaRecorder
     */
    private void releaseMediaRecorder(){
//        if (mMediaRecorder != null) {
//            mMediaRecorder.reset();   // clear recorder configuration
//            mMediaRecorder.release(); // release the recorder object
//            mMediaRecorder = null;
//            mCamera.lock();           // lock camera for later use
//        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

}