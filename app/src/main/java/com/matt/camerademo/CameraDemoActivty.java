package com.matt.camerademo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.matt.camera.open.CameraOpenApi;
import com.matt.camera.open.IFrame;
import com.matt.camera.open.filter.AFilter;
import com.matt.camera.open.filter.Beauty;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Author:Created by matt on 2019/3/25.
 */
public class CameraDemoActivty extends Activity implements IFrame {


    private static final String TAG = "CameraDemoActivty";

    private Context mContext;

    private CameraOpenApi mCameraOpenApi;

    private SurfaceView mSurfaceView;
    private ImageButton mShutterBtn;
    private Button mFilterButton;
    private ImageView mImageShow;
    private AFilter mAFilter;
    private boolean isAdd = false;

    private String mImgName;

    private Handler mHandler = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what){
                case 1:
                    Toast.makeText(getApplicationContext(),(String)msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void showToast(String info){
        Message message = Message.obtain();
        message.what = 1;
        message.obj = info;
        mHandler.sendMessage(message);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mCameraOpenApi = new CameraOpenApi();

        setContentView(R.layout.activity_camera_demo);
        mImageShow = (ImageView) findViewById(R.id.iv_show_camera);
        mImageShow.setVisibility(View.GONE);
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurface);
        mShutterBtn = (ImageButton) findViewById(R.id.btn_shutter);
        mFilterButton = (Button) findViewById(R.id.btn_filter);
        mShutterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCameraOpenApi.takePhoto();
                    }
                },100);

            }
        });
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdd) {
                    mCameraOpenApi.removeAFilter(mAFilter);
                } else {
                    addAFilter();
                }
                isAdd = !isAdd;

            }
        });

        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            Log.i(TAG, "onCreate: "+info.facing);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }

        mCameraOpenApi.init(getApplicationContext())
                .camera(cameraId)
                .frameCallback(this)
                .surfaceView(mSurfaceView)
//                .scaleMirror(true)
                .build();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraOpenApi != null) {
            mCameraOpenApi.onResume();
        }
        mFilterButton.performClick();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraOpenApi != null) {
            mCameraOpenApi.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraOpenApi != null) {
            mCameraOpenApi.destory();
        }
    }


    private void addAFilter() {
        Beauty mBeautyFilter = new Beauty(getResources());
        mBeautyFilter.setFlag(6);
        mAFilter = mBeautyFilter;
        mCameraOpenApi.addAFilter(mAFilter);
    }


    @Override
    public void onFrame(final byte[] bytes, long time) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onFrame: " + bytes.length + ",time:" + time);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888);
                ByteBuffer b = ByteBuffer.wrap(bytes);
                bitmap.copyPixelsFromBuffer(b);
                Matrix matrix = new Matrix();
                matrix.postScale(-1, 1);
                Bitmap cacheBitmap = Bitmap.createBitmap(bitmap, 0, 0, 720, 1280, matrix, true);
                final String picName = System.currentTimeMillis()+"";
                Utils.saveBmp2Gallery(getApplicationContext(),cacheBitmap,picName);
                bitmap.recycle();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImgName = Utils.getBitmapDir()+picName+".jpg";
                        Bitmap bitmap = Utils.compressBitmap(mContext, Uri.fromFile(new File(mImgName)), 720, 1280);
                        mImageShow.setImageBitmap(bitmap);
                        mFilterButton.setVisibility(View.GONE);
                        mImageShow.setVisibility(View.VISIBLE);
                        if (mCameraOpenApi != null) {
                            mCameraOpenApi.destory();
                        }
                    }
                });
            }
        }).start();
    }




}
