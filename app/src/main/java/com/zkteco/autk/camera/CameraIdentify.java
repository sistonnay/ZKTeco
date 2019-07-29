package com.zkteco.autk.camera;

import android.app.Activity;
import android.graphics.ImageFormat;

import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 14:26
 * email: 372022839@qq.com (github: sistonnay)
 */
public class CameraIdentify extends CameraForeground {
    private static final String TAG = Utils.TAG + "#" + CameraIdentify.class.getSimpleName();

    private static final boolean DEBUG = Utils.DEBUG;

    private final int CAMERA_WIDTH = 640;
    private final int CAMERA_HEIGHT = 480;
    private final int PREVIEW_FORMAT = ImageFormat.NV21;

    public CameraIdentify(Activity context) {
        super(context);
        setPreviewWidth(CAMERA_WIDTH);
        setPreviewHeight(CAMERA_HEIGHT);
        setPreviewFormat(PREVIEW_FORMAT);
    }

    @Override
    public void open() {
        open(getFrontId());
        if (DEBUG) Logger.d(TAG, "Camera opened!");
    }

    @Override
    public void onPreview(byte[] data) {

    }

}
