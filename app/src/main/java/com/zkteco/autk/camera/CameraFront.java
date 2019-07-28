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
public class CameraFront extends CameraBase implements Camera.PreviewCallback, TextureView.SurfaceTextureListener{
    private static final String TAG = Utils.TAG + "#" + CameraFront.class.getSimpleName();

    private static final boolean DEBUG = Utils.DEBUG;

    private final int CAMERA_WIDTH = 640;
    private final int CAMERA_HEIGHT = 480;
    private final int PREVIEW_FORMAT = ImageFormat.NV21;

    private Activity mContext;

    public CameraFront(Activity context) {
        super();
        mContext = context;
    }

    public void open() {
        open(getFrontId());
    }

    @Override
    public void startPreview() throws IOException {
        if (isOpened()) {
            Logger.e(TAG, "Camera is not opened yet");
            return;
        }

        if (isPreview()) {
            return;
        }

        Camera.Size size = getOptimalPreviewSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        if (DEBUG)
            Logger.d(TAG, "PreviewWidth = " + size.width + ", PreviewHeight = " + size.height);
        getParameters().setPreviewSize(size.width, size.height);
        getParameters().setPreviewFormat(PREVIEW_FORMAT);

        List<String> focusModes = getParameters().getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        int surfaceRotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        setDisplayOrientation(surfaceRotation);

        setPreviewCallback(this);

        super.startPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}