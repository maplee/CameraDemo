package com.matt.camera.open;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.matt.camera.BuildConfig;
import com.matt.camera.controller.CameraController;
import com.matt.camera.open.filter.AFilter;
import com.matt.camera.open.model.Size;

/**
 * Author:Created by jiaguofeng on 2019/5/30.
 * Email:jiaguofeng@inno72.com
 */
public class CameraOpenApi {
    private static final String TAG = "CameraOpenApi";
    

    private Builder mBuilder;

    public Builder init(Context context){
        mBuilder = new Builder(context);
        return mBuilder;
    }

    public void startRecord(){
        if(mBuilder != null && mBuilder.mCameraController != null){
            if(!mBuilder.videoMode){
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "startRecord: videoMode is false");
                }
                return;
            }
            mBuilder.mCameraController.startRecord();
        }
    }

    public void stopRecord(){
        if(mBuilder != null && mBuilder.mCameraController != null){
            if(!mBuilder.videoMode){
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "stopRecord: videoMode is false");
                }
                return;
            }
            mBuilder.mCameraController.stopRecord();
        }
    }

    public void destory(){
        if(mBuilder != null && mBuilder.mCameraController != null){
            mBuilder.mCameraController.destroy();
        }
    }

    public void onPause(){
        if(mBuilder != null && mBuilder.mCameraController != null){
            mBuilder.mCameraController.onPause();
        }
    }

    public void onResume(){
        if(mBuilder != null && mBuilder.mCameraController != null){
            mBuilder.mCameraController.onResume();
        }
    }


    public void addAFilter(AFilter aFilter){
        if(mBuilder != null && mBuilder.mCameraController != null){
            mBuilder.mCameraController.addAFilter(aFilter);
        }
    }

    public void removeAFilter(AFilter aFilter){
        if(mBuilder != null && mBuilder.mCameraController != null){
            mBuilder.mCameraController.removeAFilter(aFilter);
        }
    }

    public void takePhoto(){
        if(mBuilder != null && mBuilder.mCameraController != null ){
            if(mBuilder.videoMode){
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "takePhoto: videoMode is true");
                }
                return;
            }
            mBuilder.mCameraController.takePhoto();
        }
    }

    public static class Builder{

        private CameraController mCameraController;

        private Context mContext;
        private int mRotation;
        private String videoPath;
        private SurfaceView mSurfaceView;
        private IFrame mIFrame;
        private IVideo mVideoCallback;
        private boolean videoMode;
        private int cameraId;
        private boolean mScaleMirror;
        private com.matt.camera.open.model.Size mCustomVideoSize;
        private com.matt.camera.open.model.Size mCustomPreviewSize;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder rotation(int rotation) {
            mRotation = rotation;
            return this;
        }

        public Builder videoPath(String videoPath) {
            this.videoPath = videoPath;
            return this;
        }

        public Builder surfaceView(SurfaceView surfaceView) {
            mSurfaceView = surfaceView;
            return this;
        }

        public Builder frameCallback(IFrame IFrame) {
            mIFrame = IFrame;
            return this;
        }

        public Builder videoCallback(IVideo videoCallback) {
            mVideoCallback = videoCallback;
            return this;
        }

        public Builder videoMode(boolean videoMode) {
            this.videoMode = videoMode;
            return this;
        }

        public Builder camera(int cameraId) {
            this.cameraId = cameraId;
            return this;
        }

        public Builder customVideoSize(Size customVideoSize) {
            mCustomVideoSize = customVideoSize;
            return this;
        }

        public Builder customPreviewSize(Size customPreviewSize) {
            mCustomPreviewSize = customPreviewSize;
            return this;
        }

        public Builder scaleMirror(boolean scaleMirror) {
            mScaleMirror = scaleMirror;
            return this;
        }

        public void build(){
            if(mContext == null){
                throw new RuntimeException("context is null");
            }
            if(mSurfaceView == null){
                throw new RuntimeException("SurfaceView is null");
            }
            mCameraController = new CameraController(mContext,cameraId);
            mCameraController.setRotation(mRotation);
            mCameraController.setSurfaceView(mSurfaceView);
            mCameraController.setVideoMode(videoMode);
            mCameraController.setIFrame(mIFrame);
            mCameraController.setVideoCallback(mVideoCallback);
            mCameraController.setScaleMirror(mScaleMirror);

            if(!TextUtils.isEmpty(videoPath)){
                mCameraController.setVideoPath(videoPath);
            }
            if(mCustomVideoSize != null){
                mCameraController.setCustomVideoSize(mCustomVideoSize);
            }
            if(mCustomPreviewSize != null){
                mCameraController.setCustomPreviewSize(mCustomPreviewSize);
            }

        }
    }
}
