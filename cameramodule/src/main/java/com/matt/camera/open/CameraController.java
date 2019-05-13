package com.matt.camera.open;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.matt.camera.Renderer;
import com.matt.camera.controller.TextureController;
import com.matt.camera.filter.AFilter;

import java.io.IOException;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Author:Created by jiaguofeng on 2019/3/22.
 * Email:
 */
public class CameraController {

    private Context mContext;
    private TextureController mTextureController;
    private Renderer mRenderer;
    private int cameraId = 1;


    private SurfaceView mSurfaceView;
    private FrameCallback mFrameCallback;

    private volatile static CameraController mInstance;

    public CameraController(Context context) {
        this.mContext = context;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mRenderer = new Camera2Renderer(mContext);
        }else{
            mRenderer = new Camera1Renderer();
        }
        mTextureController = new TextureController(mContext);
    }

//    public static CameraController getInstance(Context context){
//        if(mInstance == null){
//            synchronized (CameraController.class){
//                if(mInstance == null){
//                    mInstance = new CameraController(context);
//                }
//            }
//        }
//        return mInstance;
//    }

    /**
     * 拍照
     */
    public void takePhoto(){
        if(mTextureController != null){
            mTextureController.takePhoto();
        }
    }


    /**
     * 回调
     * @param frameCallback
     */
    public void setFrameCallback(FrameCallback frameCallback) {
        mFrameCallback = frameCallback;
        mTextureController.setFrameCallback(720, 1280, mFrameCallback);
    }

    /**
     * 设置布局
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(mTextureController != null){
                    mTextureController.surfaceCreated(holder);
                    mTextureController.setRenderer(mRenderer);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if(mTextureController != null){
                    mTextureController.surfaceChanged(width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(mTextureController != null){
                    mTextureController.surfaceDestroyed();
                }
            }
        });
    }

    public void removeAFilter(AFilter aFilter) {
        if(mTextureController != null){
            mTextureController.removeFilter(aFilter);
        }
    }

    public void addAFilter(AFilter aFilter) {
        if(mTextureController != null){
            mTextureController.addFilter(aFilter);
        }
    }

    public void onResume() {
        if(mTextureController != null){
            mTextureController.onResume();
        }
    }

    public void onPause() {
        if(mTextureController != null){
            mTextureController.onPause();
        }
    }

    public void destroy() {
        if(mTextureController != null){
            mTextureController.destroy();
        }
    }


    public void closeCamera() {
        destroy();
    }

    private class Camera1Renderer implements Renderer {

        private Camera mCamera;

        @Override
        public void onDestroy() {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            mCamera = Camera.open(cameraId);
            if(mTextureController != null){
                mTextureController.setImageDirection(cameraId);
                Camera.Size size = mCamera.getParameters().getPreviewSize();
                mTextureController.setDataSize(size.height, size.width);
                try {
                    mCamera.setPreviewTexture(mTextureController.getTexture());
                    mTextureController.getTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            mTextureController.requestRender();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }

    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class Camera2Renderer implements Renderer {

        CameraDevice mDevice;
        CameraManager mCameraManager;
        private HandlerThread mThread;
        private Handler mHandler;
        private Size mPreviewSize;

        Camera2Renderer(Context context) {
            mCameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
            mThread = new HandlerThread("camera2 ");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }

        @Override
        public void onDestroy() {
            if(mDevice!=null){
                mDevice.close();
                mDevice=null;
            }
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            try {
                if(mDevice!=null){
                    mDevice.close();
                    mDevice=null;
                }
                if(mTextureController == null){
                    return;
                }
                CameraCharacteristics c=mCameraManager.getCameraCharacteristics(cameraId+"");
                StreamConfigurationMap map=c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] sizes=map.getOutputSizes(SurfaceHolder.class);
                //自定义规则，选个大小
                mPreviewSize=sizes[0];
                mTextureController.setDataSize(mPreviewSize.getHeight(),mPreviewSize.getWidth());
                mCameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(CameraDevice camera) {
                        mDevice=camera;
                        try {
                            Surface surface=new Surface(mTextureController
                                    .getTexture());
                            final CaptureRequest.Builder builder=mDevice.createCaptureRequest
                                    (CameraDevice.TEMPLATE_PREVIEW);
                            builder.addTarget(surface);
                            mTextureController.getTexture().setDefaultBufferSize(
                                    mPreviewSize.getWidth(),mPreviewSize.getHeight());
                            mDevice.createCaptureSession(Arrays.asList(surface), new
                                    CameraCaptureSession.StateCallback() {
                                        @Override
                                        public void onConfigured(CameraCaptureSession session) {
                                            try {
                                                session.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
                                                    @Override
                                                    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                                                        super.onCaptureProgressed(session, request, partialResult);
                                                    }

                                                    @Override
                                                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                                        super.onCaptureCompleted(session, request, result);
                                                        mTextureController.requestRender();
                                                    }
                                                },mHandler);
                                            } catch (CameraAccessException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onConfigureFailed(CameraCaptureSession session) {

                                        }
                                    },mHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDisconnected(CameraDevice camera) {
                        mDevice=null;
                    }

                    @Override
                    public void onError(CameraDevice camera, int error) {

                    }
                }, mHandler);
            } catch (SecurityException | CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }
    }
}
