package com.matt.camera;

/**
 * Description:
 */
public interface FrameCallback {

    void onFrame(byte[] bytes, long time);

}
