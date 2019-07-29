package com.zkteco.autk.camera;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.text.TextUtils;

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
        Bitmap bmp = convertYuv2Bitmap(data);
        if (isEnrollOption) {
            isIdentifyOption = false;
            //mTemplate = ZKLiveFaceManager.getInstance().getTemplateFromNV21(data, CAMERA_WIDTH, CAMERA_HEIGHT);
            mTemplate = ZKLiveFaceManager.getInstance().getTemplateFromBitmap(bmp);
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
            //mTemplate = ZKLiveFaceManager.getInstance().getTemplateFromNV21(data, CAMERA_WIDTH, CAMERA_HEIGHT);
            mTemplate = ZKLiveFaceManager.getInstance().getTemplateFromBitmap(bmp);
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

    private RenderScript mRs;
    private ScriptIntrinsicYuvToRGB mYuvToRgbIntrinsic;
    private Type.Builder mYuvType, mRgbaType;
    private Allocation mIn, mOut;

    private Bitmap convertYuv2Bitmap(byte[] data) {
        // convert yuvData to bitmap
        mRs = (null == mRs) ? RenderScript.create(mContext) : mRs;
        if (null == mYuvToRgbIntrinsic) {
            mYuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(mRs, Element.U8_4(mRs));
        }
        if (null == mYuvType) {
            mYuvType = new Type.Builder(mRs, Element.U8(mRs)).setX(data.length);
        }
        if (null == mRgbaType) {
            mRgbaType = new Type.Builder(mRs, Element.RGBA_8888(mRs)).setX(CAMERA_WIDTH).setY(CAMERA_HEIGHT);
        }
        if (null == mIn) {
            mIn = Allocation.createTyped(mRs, mYuvType.create(), Allocation.USAGE_SCRIPT);
        }
        if (null == mOut) {
            mOut = Allocation.createTyped(mRs, mRgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        mIn.copyFrom(data);

        mYuvToRgbIntrinsic.setInput(mIn);
        mYuvToRgbIntrinsic.forEach(mOut);
        Bitmap outBmp = Bitmap.createBitmap(CAMERA_WIDTH, CAMERA_HEIGHT, Bitmap.Config.ARGB_8888);
        mOut.copyTo(outBmp);
        outBmp = BitmapUtil.rotateBitmap(outBmp, 270);
        return outBmp;
    }
}
