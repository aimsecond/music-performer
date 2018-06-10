package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.List;

/**
 * Created by Jiang on 5/31/2018.
 */

public class MyCameraView extends JavaCameraView{


    public MyCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public boolean isAutoWhiteBalanceLockSupported() {
        //return mParameters.isAutoWhiteBalanceLockSupported();
        return mCamera.getParameters().isAutoWhiteBalanceLockSupported();
    }

    public boolean getAutoWhiteBalanceLock () {
        //return mParameters.getAutoWhiteBalanceLock();
        return mCamera.getParameters().getAutoWhiteBalanceLock();
    }

    public void setAutoWhiteBalanceLock (boolean toggle) {
        Camera.Parameters params = mCamera.getParameters();
        params.setAutoWhiteBalanceLock(toggle);
        mCamera.setParameters(params);
    }

    public void startAutoFocus(Camera.AutoFocusCallback cb) {
        mCamera.autoFocus(cb);
    }

    public int getMaxNumFocusAreas () {
        return mCamera.getParameters().getMaxNumFocusAreas();
    }
    public boolean isAutoExposureLockSupported () {
        return mCamera.getParameters().isAutoExposureLockSupported();
    }

}
