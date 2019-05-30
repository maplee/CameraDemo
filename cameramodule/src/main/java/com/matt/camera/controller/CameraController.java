package com.matt.camera.controller;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.matt.camera.BuildConfig;
import com.matt.camera.Renderer;
import com.matt.camera.open.filter.AFilter;
import com.matt.camera.open.IFrame;
import com.matt.camera.open.IVideo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Author:Created by matt on 2019/3/22.
 * Email:
 */
public class CameraController {

    private static final String TAG = "CameraController";

    private Context mContext;
    private int mRotation;
    private TextureController mTextureController;
    private Renderer mRenderer;

    private String videoPath = "/sdacrd/";
    private String outputPath;
    private SurfaceView mSurfaceView;
    private IFrame mIFrame;
    private IVideo mVideoCallback;

    private boolean isRecord = false;
    private MediaRecorder mMediaRecorder;

    private com.matt.camera.open.model.Size mCustomVideoSize;

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    public CameraController(Context context,int cameraId) {
        this.mContext = context;
        mTextureController = new TextureController(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRenderer = new Camera2Renderer(mContext,cameraId);
        } else {
            mRenderer = new Camera1Renderer(cameraId);
        }
        mRenderer.setTextureController(mTextureController);
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        File file = new File(videoPath);
        file.mkdirs();
    }


    /**
     * 设置相机预览大小
     * @param customVideoSize
     */
    public void setCustomVideoSize(com.matt.camera.open.model.Size customVideoSize) {
        mCustomVideoSize = customVideoSize;
    }

    /**
     * 设置相机预览大小
     * @param customPreviewSize
     */
    public void setCustomPreviewSize(com.matt.camera.open.model.Size customPreviewSize) {
        mRenderer.setCustomPreviewSize(customPreviewSize);

    }

    public void setScaleMirror(boolean scaleMirror) {
        if(mTextureController != null){
            mTextureController.setScaleMirror(scaleMirror);
        }
    }

    /**
     * 设置视频模式
     * @param record
     */
    public void setVideoMode(boolean record) {
        this.isRecord = record;
        mRenderer.setRecordMode(isRecord);
    }

    /**
     * 设置页面方向
     * @param rotation
     */
    public void setRotation(int rotation) {
        mRotation = rotation;
    }

    /**
     * 拍照
     */
    public void takePhoto() {
        if (mTextureController != null) {
            mTextureController.takePhoto();
        }
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        if(isRecord && mIFrame != null){
            if(mTextureController != null){
                mTextureController.startRecord();
            }
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.start();
        }

    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if(isRecord && mIFrame != null){
            if(mTextureController != null){
                mTextureController.stopRecord();
            }
        }

        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mVideoCallback.result(outputPath);
        }
    }


    /**
     * 设置视频回调
     * @param videoCallback
     */
    public void setVideoCallback(IVideo videoCallback) {
        this.mVideoCallback = videoCallback;
    }

    /**
     * 流回调
     *
     * @param IFrame
     */
    public void setIFrame(IFrame IFrame) {
        mIFrame = IFrame;
        mTextureController.setFrameCallback(720, 1280, mIFrame);
    }

    /**
     * 流回调
     *
     * @param IFrame
     */
    public void setFrameCallback(int width, int height, IFrame IFrame) {
        mIFrame = IFrame;
        mTextureController.setFrameCallback(width, height, mIFrame);
    }

    /**
     * 设置布局
     *
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mRenderer.setSurfaceView(mSurfaceView);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (isRecord) {
                    prepareRecord();
                }
                if (mTextureController != null) {
                    mTextureController.surfaceCreated(holder);
                    mTextureController.setRenderer(mRenderer);
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mTextureController != null) {
                    mTextureController.surfaceChanged(width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mTextureController != null) {
                    mTextureController.surfaceDestroyed();
                }
                releaseMediaRecorder();
                mMediaRecorder = null;
                mSurfaceView = null;
            }
        });
    }

    public void removeAFilter(AFilter aFilter) {
        if (mTextureController != null) {
            mTextureController.removeFilter(aFilter);
        }
    }

    public void addAFilter(AFilter aFilter) {
        if (mTextureController != null) {
            mTextureController.addFilter(aFilter);
        }
    }

    public void onResume() {
        if (mTextureController != null) {
            mTextureController.onResume();
        }
        prepareRecord();
    }

    public void onPause() {
        if (mTextureController != null) {
            mTextureController.onPause();
        }
        releaseMediaRecorder();
    }

    public void destroy() {
        if (mTextureController != null) {
            mTextureController.destroy();
        }
        if(mRenderer != null){
            mRenderer.onDestroy();
        }
    }


    public void prepareRecord() {
        outputPath = videoPath + "video_" + System.currentTimeMillis() + ".mp4";
        File file = new File(outputPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "prepareRecord: ", e);
                }
            }
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置录制的视频编码h263 h264
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        if(mCustomVideoSize != null){
                mMediaRecorder.setVideoSize(mCustomVideoSize.getWidth(),mCustomVideoSize.getHeight());
        }else{
            if(Build.VERSION.SDK_INT >= 21){
                mMediaRecorder.setVideoSize(mRenderer.getVideoSize().getWidth(), mRenderer.getVideoSize().getHeight());
            }
        }

        mMediaRecorder.setOutputFile(outputPath);

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "prepareRecord: mRotation:"+mRotation+",SensorOrientation:"+mRenderer.getSensorOrientation());
        }
        int degrees = 0;
        switch (mRenderer.getSensorOrientation()) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                degrees = DEFAULT_ORIENTATIONS.get(mRotation);
                mMediaRecorder.setOrientationHint(degrees);
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                degrees = INVERSE_ORIENTATIONS.get(mRotation);
                mMediaRecorder.setOrientationHint(degrees);
                break;
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "prepareRecord: degrees:"+degrees);
        }
        try {
            mMediaRecorder.prepare();
            mRenderer.setMediaRecorder(mMediaRecorder);
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        } catch (IOException e) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        }
    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
        }
    }


    public void closeCamera() {
        destroy();
    }

    private static class Camera1Renderer implements Renderer {

        private Camera mCamera;
        private boolean isRecord;
        private int cameraId;

        private StreamConfigurationMap mStreamConfigurationMap;
        private CameraCharacteristics mCameraCharacteristics;

        private TextureController mTextureController;
        private SurfaceView mSurfaceView;
        private MediaRecorder mMediaRecorder;
        private Integer mSensorOrientation;
        private Size mPreviewSize;
        private Size mVideoSize;
        private com.matt.camera.open.model.Size mCustomPreviewSize;





        public Camera1Renderer(int cameraId) {
            this.cameraId = cameraId;
        }

        @Override
        public void onDestroy() {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }

        @Override
        public void setRecordMode(boolean isRecord) {
            this.isRecord = isRecord;
        }

        @Override
        public void setTextureController(TextureController textureController) {
            mTextureController = textureController;
        }

        @Override
        public void setSurfaceView(SurfaceView surfaceView) {
            mSurfaceView = surfaceView;
        }

        @Override
        public void setMediaRecorder(MediaRecorder mediaRecorder) {
            mMediaRecorder = mediaRecorder;
        }

        @Override
        public Integer getSensorOrientation() {
            return 270;
        }

        @Override
        public Size getPreviewSize() {
            return mPreviewSize;
        }

        @Override
        public Size getVideoSize() {
            return mVideoSize;
        }

        @Override
        public void setCustomPreviewSize(com.matt.camera.open.model.Size customPreviewSize) {
            mCustomPreviewSize = customPreviewSize;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            mCamera = Camera.open(cameraId);
            if(isRecord){
                mMediaRecorder.setCamera(mCamera);
            }

            if (mTextureController != null) {
                mTextureController.setImageDirection(cameraId);
                Camera.Size size = mCamera.getParameters().getPreviewSize();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mPreviewSize = new Size(size.width,size.height);
                }
                if(mCustomPreviewSize == null){
                    mTextureController.setDataSize(size.height, size.width);
                }else{
                    mTextureController.setDataSize(mCustomPreviewSize.getWidth(),mCustomPreviewSize.getHeight());
                }
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
    private static class Camera2Renderer implements Renderer {

        CameraDevice mDevice;
        CameraManager mCameraManager;
        private HandlerThread mThread;
        private Handler mHandler;
        private boolean isRecord;
        private StreamConfigurationMap mStreamConfigurationMap;
        private CameraCharacteristics mCameraCharacteristics;

        private TextureController mTextureController;
        private int cameraId;
        private SurfaceView mSurfaceView;
        private MediaRecorder mMediaRecorder;
        private Integer mSensorOrientation;
        private Size mPreviewSize;
        private Size mVideoSize;
        private com.matt.camera.open.model.Size mCustomPreviewSize;


        Camera2Renderer(Context context,int cameraId) {
            this.cameraId = cameraId;
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId + "");
                mStreamConfigurationMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                mVideoSize = chooseVideoSize(mStreamConfigurationMap.getOutputSizes(MediaRecorder.class));
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            mThread = new HandlerThread("camera2 ");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }

        @Override
        public void onDestroy() {
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        }

        @Override
        public void setRecordMode(boolean isRecord) {
            this.isRecord = isRecord;
        }

        @Override
        public void setTextureController(TextureController textureController) {
            mTextureController = textureController;
        }

        @Override
        public void setSurfaceView(SurfaceView surfaceView) {
            mSurfaceView = surfaceView;
        }

        @Override
        public void setMediaRecorder(MediaRecorder mediaRecorder) {
            mMediaRecorder = mediaRecorder;
        }

        @Override
        public Integer getSensorOrientation() {
            return mSensorOrientation;
        }

        @Override
        public Size getPreviewSize() {
            return mPreviewSize;
        }

        @Override
        public Size getVideoSize() {
            return mVideoSize;
        }

        @Override
        public void setCustomPreviewSize(com.matt.camera.open.model.Size customPreviewSize) {
            mCustomPreviewSize = customPreviewSize;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            try {
                if (mDevice != null) {
                    mDevice.close();
                    mDevice = null;
                }
                if (mTextureController == null) {
                    return;
                }
                if(mCustomPreviewSize != null){
                    mTextureController.setDataSize(mCustomPreviewSize.getHeight(), mCustomPreviewSize.getWidth());
                }else{
                    mPreviewSize = chooseOptimalSize(mStreamConfigurationMap.getOutputSizes(SurfaceTexture.class),
                            mSurfaceView.getWidth(), mSurfaceView.getHeight(), mVideoSize);
                    mTextureController.setDataSize(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                mCameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(CameraDevice camera) {
                        mDevice = camera;
                        try {
                            int templateType = CameraDevice.TEMPLATE_PREVIEW;
                            if (isRecord) {
                                templateType = CameraDevice.TEMPLATE_RECORD;
                            }
                            final CaptureRequest.Builder builder = mDevice.createCaptureRequest(templateType);
                            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                            mTextureController.getTexture().setDefaultBufferSize(
                                    mPreviewSize.getWidth(), mPreviewSize.getHeight());

                            List<Surface> surfaces = new ArrayList<>();

                            // Set up Surface for the camera preview
                            Surface previewSurface = new Surface(mTextureController.getTexture());
                            surfaces.add(previewSurface);
                            builder.addTarget(previewSurface);
                            if (isRecord) {
                                // Set up Surface for the MediaRecorder
                                Surface recorderSurface = mMediaRecorder.getSurface();
                                surfaces.add(recorderSurface);
                                builder.addTarget(recorderSurface);
                            }

                            mDevice.createCaptureSession(surfaces, new
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
                                                }, mHandler);
                                            } catch (CameraAccessException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onConfigureFailed(CameraCaptureSession session) {

                                        }
                                    }, mHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDisconnected(CameraDevice camera) {
                        mDevice = null;
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


        /**
         * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
         * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
         *
         * @param choices The list of available sizes
         * @return The video size
         */
        private Size chooseVideoSize(Size[] choices) {
            for (Size size : choices) {
                if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                    return size;
                }
            }
            Log.e(TAG, "Couldn't find any suitable video size");
            return choices[choices.length - 1];
        }

        /**
         * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
         * width and height are at least as large as the respective requested values, and whose aspect
         * ratio matches with the specified value.
         *
         * @param choices     The list of sizes that the camera supports for the intended output class
         * @param width       The minimum desired width
         * @param height      The minimum desired height
         * @param aspectRatio The aspect ratio
         * @return The optimal {@code Size}, or an arbitrary one if none were big enough
         */
        private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
            // Collect the supported resolutions that are at least as big as the preview Surface
            List<Size> bigEnough = new ArrayList<>();
            int w = aspectRatio.getWidth();
            int h = aspectRatio.getHeight();
            for (Size option : choices) {
                if (option.getHeight() == option.getWidth() * h / w &&
                        option.getWidth() >= width && option.getHeight() >= height) {
                    bigEnough.add(option);
                }
            }

            // Pick the smallest of those, assuming we found any
            if (bigEnough.size() > 0) {
                return Collections.min(bigEnough, new CompareSizesByArea());
            } else {
                Log.e(TAG, "Couldn't find any suitable preview size");
                return choices[0];
            }
        }


        /**
         * Compares two {@code Size}s based on their areas.
         */
        class CompareSizesByArea implements Comparator<Size> {

            @Override
            public int compare(Size lhs, Size rhs) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                        (long) rhs.getWidth() * rhs.getHeight());
            }

        }


    }


}
