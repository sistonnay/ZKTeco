package com.zkteco.autk.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:45
 * email: 372022839@qq.com (github: sistonnay)
 */
public class CameraBase {
    private static final String TAG = Utils.TAG + "#" + CameraBase.class.getSimpleName();

    private static final boolean DEBUG = Utils.DEBUG;

    private int mBackId = 0;
    private int mFrontId = 0;
    private int mCurrentId = 0;
    private boolean isOpened;
    private boolean isPreview;

    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private Camera.Parameters mParameters;
    private Camera.PreviewCallback mPreviewCallback;
    private List<Camera.Size> mSupportedPreviewSizes;

    protected Object mLock = new Object();

    public CameraBase() {
        isOpened = false;
        init();
    }

    private void init() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.getCameraInfo(i, localCameraInfo);
            switch (localCameraInfo.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    mFrontId = i;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    mBackId = i;
                    break;
                default:
                    break;
            }
        }
    }

    public int getFrontId() {
        return mFrontId;
    }

    public int getBackId() {
        return mBackId;
    }

    public Camera.Parameters getParameters() {
        return mParameters;
    }

    public boolean isPreview() {
        return isPreview;
    }

    public boolean isOpened() {
        return isOpened;
    }

    protected void open(int id) {
        synchronized (mLock) {
            if (isOpened) {
                if (DEBUG) Logger.d(TAG, "camera has been opened really");
                return;
            }
            if (id != mFrontId && id != mBackId) {
                Logger.e(TAG, "the camera id is not exist");
                return;
            }
            mCurrentId = id;
            if (DEBUG) Logger.d(TAG, "the current camera id : " + mCurrentId);
            mCamera = Camera.open(mCurrentId);
            mParameters = mCamera.getParameters();
            mSupportedPreviewSizes = mParameters.getSupportedPreviewSizes();
            mCameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCurrentId, mCameraInfo);
            isOpened = true;
            if (DEBUG) Logger.d(TAG, "camera " + mCurrentId + " opened success");
        }
    }

    /**
     * @param width
     * @param height
     * @return 返回跟view尺寸最接近的size
     */
    public Camera.Size getOptimalPreviewSize(int width, int height) {
        Camera.Size result = null;
        if (mSupportedPreviewSizes == null) {
            result = mCamera.new Size(width, height);
            if (DEBUG) Logger.d(TAG, "SupportedPreviewSizes is null");
        } else {
            double m = 1.7976931348623157E+308D;
            Iterator localIterator = mSupportedPreviewSizes.iterator();
            while (localIterator.hasNext()) {
                Camera.Size localSize = (Camera.Size) localIterator.next();
                if (DEBUG)
                    Logger.d(TAG, "PreviewWidth = " + localSize.width + ", PreviewHeight = " + localSize.height);
                int n = Math.abs(localSize.width - width) + Math.abs(localSize.height - height);
                if (n >= m) {
                    continue;
                }
                m = n;
                result = localSize;
            }
        }
        return result;
    }

    public void setDisplayOrientation(int orientation) {
        // surfaceRotation may be Surface.ROTATION_0,Surface.ROTATION_90,Surface.ROTATION_180,Surface.ROTATION_270:
        if (DEBUG)
            Logger.d(TAG, "the camera orientation : " + mCameraInfo.orientation + orientation);
        int surfaceAngle = 90 * orientation;
        int rotationAngle = (surfaceAngle + mCameraInfo.orientation) % 360;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotationAngle = (360 - rotationAngle) % 360;
        }
        mCamera.setDisplayOrientation(rotationAngle);
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
        if (DEBUG) Logger.d(TAG, "setPreviewCallback " + mPreviewCallback);
        mCamera.setPreviewCallback(mPreviewCallback);
        return;
    }

    public void setPreviewTexture(SurfaceTexture surface) throws IOException {
        mCamera.setPreviewTexture(surface);
        return;
    }

    protected void startPreview() throws IOException {
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
        isPreview = true;
    }

    public void stopPreview() {
        synchronized (mLock) {
            if (isOpened && isPreview) {
                mCamera.stopPreview();
                isPreview = false;
                if (DEBUG) Logger.d(TAG, "stopped preview!");
            }
        }
    }

    public void release() {
        synchronized (mLock) {
            if (isOpened) {
                if (isPreview) {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    isPreview = false;
                }
                mCamera.release();
                mCamera = null;
                isOpened = false;
            }
        }
    }
}
