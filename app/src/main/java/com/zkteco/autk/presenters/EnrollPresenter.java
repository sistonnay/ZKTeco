package com.zkteco.autk.presenters;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:30
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollPresenter extends BasePresenter<EnrollModel, EnrollActivity> implements CameraBase.CameraPreview {
    private static final String TAG = Utils.TAG + "#" + EnrollPresenter.class.getSimpleName();

    private static final boolean DECODE_AS_BITMAP = false;

    private static final int CAMERA_WIDTH = CameraIdentify.CAMERA_WIDTH;
    private static final int CAMERA_HEIGHT = CameraIdentify.CAMERA_HEIGHT;

    private static final int MSG_START_PREVIEW = 0x01;
    private static final int MSG_ENROLL_EXISTED = 0x02;
    private static final int MSG_ENROLL_SUCCESS = 0x03;
    private static final int MSG_ENROLL_FAIL = 0x04;
    private static final int MSG_IDENTIFY_SUCCESS = 0x06;
    private static final int MSG_IDENTIFY_FAIL = 0x07;
    private static final int MSG_UPDATE_UI = 0x08;
    private static final int MSG_STOP_RINGSTONE = 0x09;

    private static final int THREAD_MSG_IDENTIFY = 0x101;
    private static final int THREAD_MSG_ENROLL = 0x102;

    private Object mLock = new Object();

    private boolean hasTextureListener = false;

    private SharedPreferences mSP = null;
    private DatabaseHelper mDbHelper = null;

    private EnrollActivity mActivity = null;
    private CameraIdentify mCamera = null;

    private Handler mHandler;
    private Handler mThreadHandler;
    private FaceDetectTask mDetectTask;

    private String mAdminPass = null;

    private long mLastRingtoneTime = 0;
    private long mCurrRingtoneTime = 0;

    private SoundPool mSoundPool = null;

    public void init() {
        mActivity = mView.get();
        mModel = new EnrollModel();
        mCamera = new CameraIdentify(mActivity);
        mDbHelper = new DatabaseHelper(mActivity);
        mHandler = new H(mActivity.getMainLooper());
        mDetectTask = new FaceDetectTask(mHandler);
        mDetectTask.start();
        mThreadHandler = mDetectTask.getHandler();
        mHandler.sendEmptyMessageDelayed(MSG_START_PREVIEW, 2000); //延时2秒开始人脸识别，防止启动时卡死
        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        mSoundPool.load(mActivity, R.raw.identify,1);
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
        mHandler.sendEmptyMessage(MSG_STOP_RINGSTONE);
    }

    public void destroy() {
        if (mCamera != null) {
            mCamera.release();
            Logger.d(TAG, "activity onDestroy and camera released");
        }
        if (mSoundPool != null) {
            mSoundPool.release();
        }
    }

    public String getUploadUrl() {
        if (mSP == null) {
            mSP = mActivity.getSharedPreferences("HttpParams", MODE_PRIVATE);
        }
        return mSP.getString("url", Utils.URL);
    }

    public void setUploadUrl(String url) {
        if (mSP == null) {
            mSP = mActivity.getSharedPreferences("HttpParams", MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = mSP.edit();
        editor.putString("url", url);
        editor.commit();
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

    @Override
    public void onPreview(byte[] data) {
        switch (mActivity.getMode()) {
            case EnrollActivity.MODE_IDENTIFY:
                if (!mThreadHandler.hasMessages(THREAD_MSG_IDENTIFY)) {
                    mThreadHandler.sendMessageDelayed(mThreadHandler.obtainMessage(THREAD_MSG_IDENTIFY, data), 500);
                }
                break;
            case EnrollActivity.MODE_ENROLL: {
                mActivity.setMode(EnrollActivity.MODE_NULL);
                mThreadHandler.sendMessage(mThreadHandler.obtainMessage(THREAD_MSG_ENROLL, data));
            }
            break;
        }
    }

    class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_PREVIEW:
                    mCamera.setCameraPreview(EnrollPresenter.this);
                    break;
                case MSG_IDENTIFY_SUCCESS: {
                    mActivity.updateAlert(mActivity.getString(R.string.identify_success) + " 工号:" + getJobNumber());
                    mCurrRingtoneTime = System.currentTimeMillis();
                    if ((mCurrRingtoneTime - mLastRingtoneTime > 2500)) {
                        mLastRingtoneTime = mCurrRingtoneTime;
                        mSoundPool.play(1,1,1,0,0,1);
                        sendEmptyMessageDelayed(MSG_STOP_RINGSTONE, 2000);
                    }
                    resetInfo();
                    sendEmptyMessageDelayed(MSG_UPDATE_UI, 1000);
                }
                break;
                case MSG_IDENTIFY_FAIL:
                    //mActivity.toast(mActivity.getString(R.string.identify_fail));
                    break;
                case MSG_UPDATE_UI:
                    mActivity.refreshUI();
                    break;
                case MSG_ENROLL_EXISTED: {
                    SimpleDialog alertDialog = new SimpleDialog(mActivity, "提示", "人脸已经注册过!") {
                        @Override
                        public void onDialogOK() {
                            resetInfo();
                            mActivity.setMode(EnrollActivity.MODE_ENTERING);
                            obtainMessage(MSG_UPDATE_UI).sendToTarget();
                        }
                    };
                    alertDialog.disableCancel(true);
                    alertDialog.show();
                }
                break;
                case MSG_ENROLL_SUCCESS: {
                    SimpleDialog alertDialog = new SimpleDialog(mActivity, "提示", "工号:" + getJobNumber() + "\n人脸注册成功!") {
                        @Override
                        public void onDialogOK() {
                            resetInfo();
                            mActivity.setMode(EnrollActivity.MODE_ENTERING);
                            obtainMessage(MSG_UPDATE_UI).sendToTarget();
                        }
                    };
                    alertDialog.disableCancel(true);
                    alertDialog.show();
                }
                break;
                case MSG_ENROLL_FAIL: {
                    mActivity.setMode(EnrollActivity.MODE_ENROLL);
                    mActivity.toast(mActivity.getString(R.string.db_add_template_fail));
                }
                break;
                case MSG_STOP_RINGSTONE: {
                    mSoundPool.stop(1);
                }
                break;
            }
        }
    }

    class FaceDetectTask extends HandlerThread implements Handler.Callback {
        private Object mLock = new Object();

        private Handler mHandler;
        private Handler mMainHandler;

        private BitmapUtil.Yuv2Bitmap mYuv2Bitmap;

        public FaceDetectTask(Handler handler) {
            super("face-detect-thread");
            mMainHandler = handler;
        }

        @Override
        public void run() {
            synchronized (mLock) {
                super.run();
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case THREAD_MSG_IDENTIFY:
                    syncIdentify((byte[]) msg.obj);
                    break;
                case THREAD_MSG_ENROLL:
                    syncEnroll((byte[]) msg.obj);
                    break;
            }
            return true;
        }

        public Handler getHandler() {
            if (mHandler == null) {
                if (getLooper() != null) {
                    mHandler = new Handler(getLooper(), this);
                }
            }
            return mHandler;
        }

        private void syncIdentify(final byte[] data) {
            byte[] template = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, DECODE_AS_BITMAP);
            if (template == null) {
                Logger.e(TAG, "template is null!");
                return;
            }
            String id = ZKLiveFaceManager.getInstance().identify(template);
            if (TextUtils.isEmpty(id)) {
                mMainHandler.obtainMessage(MSG_IDENTIFY_FAIL).sendToTarget();
            } else {
                setFaceId(id);
                synchronized (mLock) {
                    setJobNumber(DatabaseUtils.getInstance().insertFaceCheckInInfo(mDbHelper, getFaceId(), System.currentTimeMillis(), getUploadUrl()));
                    mMainHandler.obtainMessage(MSG_IDENTIFY_SUCCESS).sendToTarget();
                }
            }
        }

        private void syncEnroll(final byte[] data) {
            byte[] template = getTemplate(data, CAMERA_WIDTH, CAMERA_HEIGHT, DECODE_AS_BITMAP);
            if (template == null) {
                Logger.e(TAG, "template is null!");
                mMainHandler.obtainMessage(MSG_ENROLL_FAIL).sendToTarget();
                return;
            }
            String id = ZKLiveFaceManager.getInstance().identify(template);
            if (id != null) {
                setFaceId(id);
                mMainHandler.obtainMessage(MSG_ENROLL_EXISTED).sendToTarget();
            } else {
                id = "FID_" + System.currentTimeMillis();
                if (ZKLiveFaceManager.getInstance().dbAdd(id, template)) {
                    setFaceId(id);
                    setTemplate(template);
                    synchronized (mLock) {
                        long rowId = DatabaseUtils.getInstance().insertFaceEnrollInfo(mDbHelper, mModel.getIdentifyInfo());
                        if (rowId != -1) {
                            mMainHandler.obtainMessage(MSG_ENROLL_SUCCESS).sendToTarget();
                        } else {
                            mMainHandler.obtainMessage(MSG_ENROLL_FAIL).sendToTarget();
                        }
                    }
                } else {
                    mMainHandler.obtainMessage(MSG_ENROLL_FAIL).sendToTarget();
                }
            }
        }

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
    }
}
