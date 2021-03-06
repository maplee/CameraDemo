package com.matt.camerademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private int mCameraID =-1;
    //相机权限
    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_camera).setOnClickListener(this);
        findViewById(R.id.main_video).setOnClickListener(this);
        findViewById(R.id.main_back).setOnClickListener(this);
        // 授权
        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
        }
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.i(TAG, "onCreate: numberOfCameras:"+numberOfCameras);
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            Log.i(TAG, "onCreate: "+info.facing);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraID = i;
                break;
            }
        }
        Log.i(TAG, "onCreate: cameraId:"+mCameraID);
    }


    @Override
    public void onClick(View view) {
        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }
        if (view.getId() == R.id.main_back) {
            finish();
        }
        if(mCameraID == -1){
            Toast.makeText(this,"没有识别到摄像头",Toast.LENGTH_LONG).show();
            return;
        }

        if (view.getId() == R.id.main_camera) {
            startActivity(new Intent(getApplicationContext(), CameraDemoActivty.class));
        }else if (view.getId() == R.id.main_video) {
            startActivity(new Intent(getApplicationContext(), VideoDemoActivity.class));
        }
    }


    /**
     * Requests permissions necessary to use camera and save pictures.
     */
    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
    }

    /**
     * Tells whether all the necessary permissions are granted to this app.
     *
     * @return True if all the required permissions are granted.
     */
    private boolean hasAllPermissionsGranted() {
        for (String permission : CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
