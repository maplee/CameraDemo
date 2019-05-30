/*
 *
 * Renderer.java
 * 
 * Created by Wuwang on 2017/3/3
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.matt.camera;

import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.util.Size;
import android.view.SurfaceView;

import com.matt.camera.controller.TextureController;

/**
 * Description:
 */
public interface Renderer extends GLSurfaceView.Renderer {

    void onDestroy();

    void setRecordMode(boolean isRecord);

    void setTextureController(TextureController textureController);

    void setSurfaceView(SurfaceView surfaceView);

    void setMediaRecorder(MediaRecorder mediaRecorder);

    Integer getSensorOrientation();

    Size getPreviewSize();

    Size getVideoSize();

    void setCustomPreviewSize(com.matt.camera.open.model.Size customPreviewSize);

}
