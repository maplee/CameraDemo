package com.matt.camerademo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Author:Created by matt on 2019/5/28.
 */
public class VideoDemoActivity extends AppCompatActivity {


    private FrameLayout contentFl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_demo);
        contentFl = (FrameLayout)findViewById(R.id.video_demo_content_fl);
        VideoFragment videoFragment = VideoFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.video_demo_content_fl,videoFragment).commit();
    }
}
