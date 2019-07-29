package com.zkteco.autk.camera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

import java.io.IOException;
import java.util.List;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 02:26
 * email: 372022839@qq.com (github: sistonnay)
 */
public abstract class CameraForeground extends CameraBase implements Camera.PreviewCallback, TextureView.SurfaceTextureListener{
    private static final String TAG = Utils.TAG + "#" + CameraForeground.class.getSimpleName();

    private static final boolean DEBUG = Utils.DEBUG;

    private int mPreviewWidth = 640;
    private int mPreviewHeight = 480;
    private int mPreviewFormat = ImageFormat.NV21;

    protected Activity mContext;

    public CameraForeground( Activity context) {
        super();
        mContext = context;
    }

    public void setPreviewWidth(int previewWidth) {
        this.mPreviewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.mPreviewHeight = previewHeight;
    }

    public void setPreviewFormat(int previewFormat) {
        this.mPreviewFormat = previewFormat;
    }

    public abstract void open();
    public abstract void onPreview(byte[] data);

    @Override
    public void startPreview() throws IOException {
        if (isOpened()) {
            Logger.e(TAG, "Camera is not opened yet");
            return;
        }

        if (isPreview()) {
            return;
        }

        Camera.Size size = getOptimalPreviewSize(mPreviewWidth, mPreviewHeight);
        if (DEBUG)
            Logger.d(TAG, "PreviewWidth = " + size.width + ", PreviewHeight = " + size.height);
        getParameters().setPreviewSize(size.width, size.height);
        getParameters().setPreviewFormat(mPreviewFormat);

        List<String> focusModes = getParameters().getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        int surfaceRotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        setDisplayOrientation(surfaceRotation);

        setPreviewCallback(this);

        super.startPreview();
        if (DEBUG) Logger.d(TAG, "Camera started preview!");
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        onPreview(data);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (mLock) {
            open();
            try {
                setPreviewTexture(surface);
                startPreview();
            } catch (IOException e) {
                Logger.e(TAG, "Camera Preview Exception:", e);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}