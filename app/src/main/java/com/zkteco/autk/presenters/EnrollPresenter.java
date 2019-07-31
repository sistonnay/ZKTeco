package com.zkteco.autk.presenters;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.TextureView;

import com.zkteco.android.graphics.ImageConverter;
import com.zkteco.autk.R;
import com.zkteco.autk.camera.CameraBase;
import com.zkteco.autk.camera.CameraIdentify;
import com.zkteco.autk.components.EnrollActivity;
import com.zkteco.autk.components.SimpleDialog;
import com.zkteco.autk.models.EnrollModel;
import com.zkteco.autk.models.TimerTool;
import com.zkteco.autk.models.ZKLiveFaceManager;
import com.zkteco.autk.utils.BitmapUtil;
import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

import java.io.IOException;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:30
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollPresenter extends BasePresenter<EnrollModel, EnrollActivity> implements CameraBase.CameraPreview {
    private static final String TAG = Utils.TAG + "#" + EnrollPresenter.class.getSimpleName();

    private static final boolean DECODE_AS_BITMAP = false;

    private static final int MSG_ENROLL_GET_TEMPLATE_FAIL = 0;
    private static final int MSG_ENROLL_GET_TEMPLATE_SUCCESS = 1;
    private static final int MSG_ENROLL_FAIL = 2;
    private static final int MSG_ENROLL_SUCCESS = 3;
    private static final int MSG_ENROLL_EXISTED = 4;
    private static final int MSG_IDENTIFY_GET_TEMPLATE_FAIL = 5;
    private static final int MSG_IDENTIFY_GET_TEMPLATE_SUCCESS = 6;
    private static final int MSG_IDENTIFY_FAIL = 7;
    private static final int MSG_IDENTIFY_SUCCESS = 8;

    private final int CAMERA_WIDTH = CameraIdentify.CAMERA_WIDTH;
    private final int CAMERA_HEIGHT = CameraIdentify.CAMERA_HEIGHT;

    private EnrollActivity mActivity = null;
    private CameraIdentify mCamera = null;
    private Object mLock = new Object();
    private boolean hasTextureListener = false;

    private Handler mHandler;
    private byte[] mTemplate = null;
    private long preTimeMillis = 0;
    private long currTimeMillis = 0;
    private String faceId = null;

    public void init() {
        mActivity = mView.get();
        mHandler = new H(mActivity.getMainLooper());
        mModel = new EnrollModel();
        mCamera = new CameraIdentify(mActivity);
        mCamera.setCameraPreview(this);
    }

    private void tryStartCamera() {
        TimerTool.getInstance().start("-tryStartCamera:");
        if (mCamera != null) {
            if (!mCamera.isOpened()) {
                synchronized (mLock) {
                    mCamera.open();
                }
            }
            //camera open 之后不能立即预览
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLock) {
                        if (!mCamera.isPreview()) {
                            try {
                                mCamera.startPreview();
                            } catch (IOException e) {
                                Logger.e(TAG, "Camera Preview Exception:", e);
                            }
                            TimerTool.getInstance().stop("-tryStartCamera:");
                        }
                    }
                }
            }).start();
        }
    }

    public void setSurfaceTextureListener(TextureView textureView) {
        if (!hasTextureListener) {
            textureView.setSurfaceTextureListener(mCamera);
            hasTextureListener = true;
        }
    }

    public void resume() {
        tryStartCamera();
    }

    public void pause() {
        if (mCamera != null) {
            mCamera.stopPreview();
            Logger.d(TAG, "activity onPause and camera preview stopped");
        }
    }

    public void destroy() {
        if (mCamera != null) {
            mCamera.release();
            Logger.d(TAG, "activity onDestroy and camera released");
        }
    }

    public void recordName(String name) {
        mModel.getIdentifyInfo().name = name;
    }

    public void recordId(String id) {
        mModel.getIdentifyInfo().id = id;
    }

    public void recordPhone(String phone) {
        mModel.getIdentifyInfo().phone = phone;
    }

    public void recordFaceId(String faceId) {
        mModel.getIdentifyInfo().faceId = faceId;
    }

    public boolean isLegalEnrollInfo() {
        return mModel.getIdentifyInfo().isLegalEnrollInfo();
    }

    @Override
    public void onPreview(byte[] data) {
        currTimeMillis = System.currentTimeMillis();
        long deltaTime = currTimeMillis - preTimeMillis;
        if (deltaTime < 1000) {
            return;
        }
        switch (mActivity.getMode()) {
            case EnrollActivity.MODE_IDENTIFY: {
                syncIdentify(data);
            }
            break;
            case EnrollActivity.MODE_CHECK_IN:
            case EnrollActivity.MODE_PRE_ENROLL:
                return;
            case EnrollActivity.MODE_ENROLLING: {
                mActivity.setMode(EnrollActivity.MODE_NULL);
                syncEnroll(data);
            }
            break;
        }
    }

    private BitmapUtil.Yuv2Bitmap mYuv2Bitmap;

    private byte[] getTemplate(byte[] data, int width, int height, boolean bmpMode) {
        if (bmpMode) {
            if (mYuv2Bitmap == null) {
                mYuv2Bitmap = new BitmapUtil.Yuv2Bitmap(mActivity);
            }
            return ZKLiveFaceManager.getInstance().getTemplateFromBitmap(mYuv2Bitmap.convert(data, width, height, 90));
        } else {
            byte[] dst = new byte[data.length];
            ImageConverter.rotateNV21Degree90(data, dst, width, height);//旋转90度，宽高对调
            return ZKLiveFaceManager.getInstance().getTemplateFromNV21(dst, height, width);
        }
    }

    private void syncIdentify(final byte[] data) {
        mTemplate = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, DECODE_AS_BITMAP);
        if (mTemplate == null) {
            mHandler.obtainMessage(MSG_IDENTIFY_GET_TEMPLATE_FAIL).sendToTarget();
            return;
        }
        String id = ZKLiveFaceManager.getInstance().identify(mTemplate);
        if (TextUtils.isEmpty(id)) {
            mHandler.obtainMessage(MSG_IDENTIFY_FAIL).sendToTarget();
        } else {
            mHandler.obtainMessage(MSG_IDENTIFY_SUCCESS, id).sendToTarget();
        }
    }

    private void syncEnroll(final byte[] data) {
        mTemplate = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, DECODE_AS_BITMAP);
        if (mTemplate == null) {
            mHandler.obtainMessage(MSG_ENROLL_GET_TEMPLATE_FAIL).sendToTarget();
            return;
        }
        faceId = ZKLiveFaceManager.getInstance().identify(mTemplate);
        if (faceId != null) {
            mHandler.obtainMessage(MSG_ENROLL_EXISTED, faceId).sendToTarget();
        } else {
            faceId = "faceID_" + System.currentTimeMillis();
            if (ZKLiveFaceManager.getInstance().dbAdd(faceId, mTemplate)) {
                mHandler.obtainMessage(MSG_ENROLL_SUCCESS, faceId).sendToTarget();
            } else {
                mHandler.obtainMessage(MSG_ENROLL_FAIL).sendToTarget();
            }
        }
    }

    class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_IDENTIFY_GET_TEMPLATE_FAIL:
                    mActivity.toast(mActivity.getString(R.string.extract_template_fail));
                    break;
                case MSG_IDENTIFY_GET_TEMPLATE_SUCCESS:
                    break;
                case MSG_IDENTIFY_FAIL:
                    mActivity.toast(mActivity.getString(R.string.identify_fail));
                    break;
                case MSG_IDENTIFY_SUCCESS:
                    mActivity.toast(mActivity.getString(R.string.identify_success) + "faceID=" + msg.obj);
                    break;
                case MSG_ENROLL_GET_TEMPLATE_FAIL:
                    mActivity.setMode(EnrollActivity.MODE_ENROLLING);
                    mActivity.toast(mActivity.getString(R.string.extract_template_fail));
                    break;
                case MSG_ENROLL_GET_TEMPLATE_SUCCESS:
                    break;
                case MSG_ENROLL_FAIL:
                    mActivity.setMode(EnrollActivity.MODE_ENROLLING);
                    mActivity.toast(mActivity.getString(R.string.db_add_template_fail));
                    break;
                case MSG_ENROLL_EXISTED: {
                    recordFaceId(msg.obj.toString());
                    SimpleDialog alertDialog = new SimpleDialog(mActivity, "提示", "人脸已被注册\n" + "faceID=" + msg.obj) {
                        @Override
                        public void onDialogOK() {
                            mActivity.setMode(EnrollActivity.MODE_IDENTIFY);
                            mActivity.refreshUI();
                        }
                    };
                    alertDialog.disableCancel(true);
                    alertDialog.show();
                }
                break;
                case MSG_ENROLL_SUCCESS: {
                    recordFaceId(msg.obj.toString());
                    SimpleDialog alertDialog = new SimpleDialog(mActivity, "提示", "人脸注册成功\n" + "faceID=" + msg.obj) {
                        @Override
                        public void onDialogOK() {
                            mActivity.setMode(EnrollActivity.MODE_IDENTIFY);
                            mActivity.refreshUI();
                        }
                    };
                    alertDialog.disableCancel(true);
                    alertDialog.show();
                }
                break;
            }
        }
    }
}
