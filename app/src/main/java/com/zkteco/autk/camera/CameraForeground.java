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
public abstract class CameraForeground<A extends Activity> extends CameraBase implements Camera.PreviewCallback, TextureView.SurfaceTextureListener {
    private static final String TAG = Utils.TAG + "#" + CameraForeground.class.getSimpleName();

    private int mPreviewWidth = 640;
    private int mPreviewHeight = 360;
    private int mPreviewFormat = ImageFormat.NV21;
    private CameraPreview mCameraPreview;

    protected A mContext;

    public CameraForeground(A context) {
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

    public void setCameraPreview(CameraPreview cameraPreview) {
        this.mCameraPreview = cameraPreview;
    }

    public abstract void open();

    @Override
    public void startPreview() throws IOException {
        if (!isOpened()) {
            Logger.e(TAG, "Camera is not opened yet");
            return;
        }

        if (isPreview()) {
            return;
        }

        Camera.Size size = getOptimalPreviewSize(mPreviewWidth, mPreviewHeight);
        Logger.d(TAG, "PreviewWidth = " + size.width + ", PreviewHeight = " + size.height);
        Camera.Parameters parameters = getParameters();
        parameters.setPreviewSize(size.width, size.height);

        List<String> focusModes = getParameters().getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        getParameters().setPreviewFormat(mPreviewFormat);
        setParameters(parameters);

        int surfaceRotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        setDisplayOrientation(surfaceRotation);
        setPreviewCallback(this);

        super.startPreview();
        Logger.d(TAG, "Camera started preview!");
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCameraPreview != null) {
            mCameraPreview.onPreview(data);
        }
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