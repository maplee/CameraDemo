package com.matt.camerademo;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.matt.camera.open.CameraOpenApi;
import com.matt.camera.open.IVideo;
import com.matt.camera.open.filter.AFilter;
import com.matt.camera.open.filter.Beauty;

/**
 * Author:Created by matt on 2019/5/28.
 */
public class VideoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "VideoFragment";

    private Context mContext;
    private ViewGroup rootView;
    private SurfaceView mSurfaceView;
    private ImageButton mShutter;
    private TextView timeTv;


    private boolean isRecording = false;

    private CameraOpenApi mCameraOpenApi;


    public static VideoFragment newInstance() {

        Bundle args = new Bundle();

        VideoFragment fragment = new VideoFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_video, null);
        mSurfaceView = (SurfaceView) rootView.findViewById(R.id.surface_video);
        mShutter = (ImageButton) rootView.findViewById(R.id.btn_shutter);
        timeTv = (TextView) rootView.findViewById(R.id.tv_time);
        mShutter.setOnClickListener(this);
        init();
        return rootView;
    }

    private void init() {
        mContext = getActivity().getApplicationContext();
        mCameraOpenApi = new CameraOpenApi();
        mCameraOpenApi.init(mContext)
                .camera(1)
                .rotation(getActivity().getWindowManager().getDefaultDisplay().getRotation())
                .videoMode(true)
                .surfaceView(mSurfaceView)
//                .scaleMirror(true)
                .videoPath("/sdcard/video/")
                .videoCallback(new IVideo() {
                    @Override
                    public void result(String outputPath) {
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, String.format("result: %s", outputPath));
                        }
                    }
                }).build();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_shutter) {
            if (isRecording) {
                isRecording = false;
                mCameraOpenApi.stopRecord();
            } else {
                isRecording = true;
                mCameraOpenApi.startRecord();
            }
        }
    }

    private AFilter mAFilter;
    private void addAFilter() {
        Beauty mBeautyFilter = new Beauty(getResources());
        mBeautyFilter.setFlag(6);
        mAFilter = mBeautyFilter;
        mCameraOpenApi.addAFilter(mAFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraOpenApi != null) {
            mCameraOpenApi.onResume();
        }
        addAFilter();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraOpenApi != null) {
            mCameraOpenApi.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraOpenApi != null) {
            mCameraOpenApi.destory();
        }
    }
}
