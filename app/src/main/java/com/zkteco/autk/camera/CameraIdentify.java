package com.zkteco.autk.camera;

import android.graphics.ImageFormat;

import com.zkteco.autk.components.EnrollActivity;
import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 14:26
 * email: 372022839@qq.com (github: sistonnay)
 */
public class CameraIdentify extends CameraForeground<EnrollActivity> {
    private static final String TAG = Utils.TAG + "#" + CameraIdentify.class.getSimpleName();

    public static final int CAMERA_WIDTH = 640;
    public static final int CAMERA_HEIGHT = 360;
    public static final int PREVIEW_FORMAT = ImageFormat.NV21;

    public CameraIdentify(EnrollActivity context) {
        super(context);
        setPreviewWidth(CAMERA_WIDTH);
        setPreviewHeight(CAMERA_HEIGHT);
        setPreviewFormat(PREVIEW_FORMAT);
    }

    @Override
    public void open() {
        open(getBackId());
        Logger.d(TAG, "Camera opened!");
    }
}
