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
import com.zkteco.autk.dao.DatabaseHelper;
import com.zkteco.autk.dao.DatabaseUtils;
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
    private static final int MSG_ENROLL_ADDED = 3;
    private static final int MSG_ENROLL_SUCCESS = 4;
    private static final int MSG_ENROLL_EXISTED = 5;
    private static final int MSG_IDENTIFY_GET_TEMPLATE_FAIL = 6;
    private static final int MSG_IDENTIFY_GET_TEMPLATE_SUCCESS = 7;
    private static final int MSG_IDENTIFY_FAIL = 8;
    private static final int MSG_IDENTIFY_SUCCESS = 9;
    private static final int MSG_IDENTIFY_SUCCESS_ALERT = 10;
    private static final int MSG_CONTINUE_IDENTIFY = 11;
    private static final int MSG_CONTINUE_ENROLL = 12;
    private static final int MSG_IDENTIFY_BEGIN = 13;

    private final int CAMERA_WIDTH = CameraIdentify.CAMERA_WIDTH;
    private final int CAMERA_HEIGHT = CameraIdentify.CAMERA_HEIGHT;

    private DatabaseHelper mDbHelper = null;

    private EnrollActivity mActivity = null;
    private CameraIdentify mCamera = null;
    private Object mLock = new Object();
    private boolean hasTextureListener = false;

    private Handler mHandler;

    private long preTimeMillis = 0;
    private long currTimeMillis = 0;

    private String mAdminPass = null;

    public void init() {
        mActivity = mView.get();
        mModel = new EnrollModel();
        mCamera = new CameraIdentify(mActivity);
        mDbHelper = new DatabaseHelper(mActivity);
        mHandler = new H(mActivity.getMainLooper());
        mHandler.sendEmptyMessageDelayed(MSG_IDENTIFY_BEGIN, 5000); //延时3秒开始人脸识别，防止启动时卡死
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

    public void setName(String name) {
        mModel.getIdentifyInfo().name = name;
    }

    public String getName() {
        return mModel.getIdentifyInfo().name;
    }

    public void setJobNumber(String jobNumber) {
        mModel.getIdentifyInfo().job_number = jobNumber;
    }

    public String getJobNumber() {
        return mModel.getIdentifyInfo().job_number;
    }

    public void setPhone(String phone) {
        mModel.getIdentifyInfo().phone = phone;
    }

    public String getPhone() {
        return mModel.getIdentifyInfo().phone;
    }

    public void setFaceId(String faceId) {
        mModel.getIdentifyInfo().faceId = faceId;
    }

    public String getFaceId() {
        return mModel.getIdentifyInfo().faceId;
    }

    public byte[] getTemplate() {
        return mModel.getIdentifyInfo().face_template;
    }

    public void setTemplate(byte[] template) {
        mModel.getIdentifyInfo().face_template = template;
    }

    public boolean isLegalEnrollInfo() {
        return mModel.getIdentifyInfo().isLegalEnrollInfo();
    }

    public String getAdminPass() {
        return mAdminPass;
    }

    public void setAdminPass(String pass) {
        this.mAdminPass = pass;
    }

    public void resetInfo() {
        mAdminPass = null;
        mModel.getIdentifyInfo().reset();
    }

    private void dbInsertEnrollInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long rowId = DatabaseUtils.getInstance().insertFaceEnrollInfo(mDbHelper, mModel.getIdentifyInfo());
                if (rowId != -1) {
                    mHandler.obtainMessage(MSG_ENROLL_SUCCESS).sendToTarget();
                } else {
                    mHandler.obtainMessage(MSG_ENROLL_FAIL).sendToTarget();
                }
            }
        }).start();
    }

    private void dbInsertCheckInInfo() {
//        new Thread(new Runnable() {
//            @Override
//            public synchronized void run() {
        setJobNumber(DatabaseUtils.getInstance().insertFaceCheckInInfo(mDbHelper, getFaceId(), System.currentTimeMillis()));
        mHandler.obtainMessage(MSG_IDENTIFY_SUCCESS_ALERT).sendToTarget();
//            }
//        }).start();
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
        byte[] template = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, DECODE_AS_BITMAP);
        if (template == null) {
            mHandler.obtainMessage(MSG_IDENTIFY_GET_TEMPLATE_FAIL).sendToTarget();
            return;
        }
        String id = ZKLiveFaceManager.getInstance().identify(template);
        if (TextUtils.isEmpty(id)) {
            mHandler.obtainMessage(MSG_IDENTIFY_FAIL).sendToTarget();
        } else {
            setFaceId(id);
            mHandler.obtainMessage(MSG_IDENTIFY_SUCCESS).sendToTarget();
        }
    }

    private void syncEnroll(final byte[] data) {
        byte[] template = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, DECODE_AS_BITMAP);
        if (template == null) {
            mHandler.obtainMessage(MSG_ENROLL_GET_TEMPLATE_FAIL).sendToTarget();
            return;
        }
        String id = ZKLiveFaceManager.getInstance().identify(template);
        if (id != null) {
            setFaceId(id);
            mHandler.obtainMessage(MSG_ENROLL_EXISTED).sendToTarget();
        } else {
            id = "FID_" + System.currentTimeMillis();
            if (ZKLiveFaceManager.getInstance().dbAdd(id, template)) {
                setFaceId(id);
                setTemplate(template);
                mHandler.obtainMessage(MSG_ENROLL_ADDED).sendToTarget();
            } else {
                mHandler.obtainMessage(MSG_ENROLL_FAIL).sendToTarget();
            }
        }
    }

    public void removeMessages() {
        mHandler.removeMessages(MSG_CONTINUE_IDENTIFY);
    }

    public void continueIdentify() {
        mHandler.sendEmptyMessage(MSG_CONTINUE_IDENTIFY);
    }

    class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_IDENTIFY_BEGIN:
                    mCamera.setCameraPreview(EnrollPresenter.this);
                    break;
                case MSG_IDENTIFY_GET_TEMPLATE_FAIL:
                    //mActivity.toast(mActivity.getString(R.string.extract_template_fail));
                    break;
                case MSG_IDENTIFY_GET_TEMPLATE_SUCCESS:
                    break;
                case MSG_IDENTIFY_FAIL:
                    //mActivity.toast(mActivity.getString(R.string.identify_fail));
                    break;
                case MSG_IDENTIFY_SUCCESS: {
                    mActivity.setMode(EnrollActivity.MODE_IDENTIFY_HANDLE);
                    dbInsertCheckInInfo();
                }
                break;
                case MSG_IDENTIFY_SUCCESS_ALERT: {
                    mActivity.updateAlert(mActivity.getString(R.string.identify_success) + " 工号:" + getJobNumber());
                    sendEmptyMessageDelayed(MSG_CONTINUE_IDENTIFY, 1500);
                }
                break;
                case MSG_ENROLL_GET_TEMPLATE_FAIL:
                    mActivity.setMode(EnrollActivity.MODE_ENROLLING);
                    //mActivity.toast(mActivity.getString(R.string.extract_template_fail));
                    break;
                case MSG_ENROLL_GET_TEMPLATE_SUCCESS:
                    break;
                case MSG_ENROLL_FAIL: {
                    mActivity.setMode(EnrollActivity.MODE_ENROLLING);
                    //mActivity.toast(mActivity.getString(R.string.db_add_template_fail));
                }
                break;
                case MSG_ENROLL_EXISTED: {
                    SimpleDialog alertDialog = new SimpleDialog(mActivity, "提示", "人脸已经注册过!") {
                        @Override
                        public void onDialogOK() {
                            mHandler.obtainMessage(MSG_CONTINUE_ENROLL).sendToTarget();
                        }
                    };
                    alertDialog.disableCancel(true);
                    alertDialog.show();
                }
                break;
                case MSG_ENROLL_ADDED:
                    dbInsertEnrollInfo();
                    break;
                case MSG_ENROLL_SUCCESS: {
                    SimpleDialog alertDialog = new SimpleDialog(mActivity, "提示", "工号:" + getJobNumber() + "\n人脸注册成功!") {
                        @Override
                        public void onDialogOK() {
                            obtainMessage(MSG_CONTINUE_ENROLL).sendToTarget();
                        }
                    };
                    alertDialog.disableCancel(true);
                    alertDialog.show();
                }
                case MSG_CONTINUE_ENROLL: {
                    mActivity.setMode(EnrollActivity.MODE_PRE_ENROLL);
                    resetInfo();
                    mActivity.refreshUI();
                }
                break;
                case MSG_CONTINUE_IDENTIFY: {
                    mActivity.setMode(EnrollActivity.MODE_IDENTIFY);
                    resetInfo();
                    mActivity.refreshUI();
                }
                break;
            }
        }
    }
}
