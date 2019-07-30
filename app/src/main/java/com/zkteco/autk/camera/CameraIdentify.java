package com.zkteco.autk.camera;

import android.graphics.ImageFormat;
import android.text.TextUtils;

import com.zkteco.android.graphics.ImageConverter;
import com.zkteco.autk.R;
import com.zkteco.autk.components.EnrollActivity;
import com.zkteco.autk.models.ZKLiveFaceManager;
import com.zkteco.autk.utils.BitmapUtil;
import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 14:26
 * email: 372022839@qq.com (github: sistonnay)
 */
public class CameraIdentify extends CameraForeground<EnrollActivity> {
    private static final String TAG = Utils.TAG + "#" + CameraIdentify.class.getSimpleName();

    private final int CAMERA_WIDTH = 640;
    private final int CAMERA_HEIGHT = 480;
    private final int PREVIEW_FORMAT = ImageFormat.NV21;
    private final boolean DECODE_AS_BITMAP = false;

    private boolean isEnrollOption = false;
    private boolean isIdentifyOption = false;
    private byte[] mTemplate = null;
    private int faceId = 0;

    public CameraIdentify(EnrollActivity context) {
        super(context);
        setPreviewWidth(CAMERA_WIDTH);
        setPreviewHeight(CAMERA_HEIGHT);
        setPreviewFormat(PREVIEW_FORMAT);
    }

    public void setEnrollOption(boolean enable) {
        isEnrollOption = enable;
    }

    public void setIdentifyOption(boolean enable) {
        isIdentifyOption = enable;
    }

    @Override
    public void open() {
        open(getBackId());
        Logger.d(TAG, "Camera opened!");
    }

    @Override
    public void onPreview(byte[] data) {
        if (isEnrollOption) {
            isIdentifyOption = false;
            mTemplate = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, false);
            if (mTemplate == null) {
                mContext.toast(mContext.getString(R.string.extract_template_fail));
                return;
            }
            if (ZKLiveFaceManager.getInstance().dbAdd("faceID_" + faceId, mTemplate)) {
                mContext.toast(mContext.getString(R.string.db_add_template_success) + ",id=faceID_" + faceId);
                mContext.fillProgress();
                isEnrollOption = false;
                faceId++;
            } else {
                mContext.toast(mContext.getString(R.string.db_add_template_fail));
            }
        }
        if (isIdentifyOption) {
            mTemplate = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, false);
            if (mTemplate == null) {
                //mContext.toast(mContext.getString(R.string.extract_template_fail));
                return;
            }
            String id = ZKLiveFaceManager.getInstance().identify(mTemplate);
            if (TextUtils.isEmpty(id)) {
                //mContext.toast(mContext.getString(R.string.identify_fail));
            } else {
                mContext.toast(mContext.getString(R.string.identify_success) + ",id=" + id);
            }
            mContext.resetProgress();
        }
    }

    private BitmapUtil.Yuv2Bitmap mYuv2Bitmap;

    private byte[] getTemplate(byte[] data, int width, int height, boolean bmpMode) {
        if (bmpMode) {
            if (mYuv2Bitmap == null) {
                mYuv2Bitmap = new BitmapUtil.Yuv2Bitmap(mContext);
            }
            return ZKLiveFaceManager.getInstance().getTemplateFromBitmap(mYuv2Bitmap.convert(data, width, height, 90));
        } else {
            byte[] dst = new byte[data.length];
            ImageConverter.rotateNV21Degree90(data, dst, width, height);//旋转90度，宽高对调
            return ZKLiveFaceManager.getInstance().getTemplateFromNV21(dst, height, width);
        }
    }

}
