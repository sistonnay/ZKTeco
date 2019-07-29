package com.zkteco.autk.presenters;

import android.app.Activity;
import android.view.TextureView;

import com.zkteco.autk.camera.CameraIdentify;
import com.zkteco.autk.components.EnrollActivity;
import com.zkteco.autk.models.EnrollModel;
import com.zkteco.autk.models.TimerTool;
import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

import java.io.IOException;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:30
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollPresenter extends BasePresenter<EnrollModel, EnrollActivity> {
    private static final String TAG = Utils.TAG + "#" + EnrollPresenter.class.getSimpleName();
    private static final boolean DEBUG = Utils.DEBUG;

    private Activity mActivity = null;
    private CameraIdentify mCamera = null;
    private Object mLock = new Object();

    public void init() {
        mActivity = mView.get();
        mModel = new EnrollModel();
        mCamera = new CameraIdentify(mActivity);
    }

    private void tryStartCamera() {
        if (DEBUG) TimerTool.getInstance().start("-tryStartCamera:");
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
                            if (DEBUG) TimerTool.getInstance().stop( "-tryStartCamera:");
                        }
                    }
                }
            }).start();
        }
    }

    public void setSurfaceTextureListener(TextureView textureView) {
        textureView.setSurfaceTextureListener(mCamera);
    }

    public void resume() {
        tryStartCamera();
    }

    public void pause() {
        if (mCamera != null) {
            mCamera.stopPreview();
            if (DEBUG) Logger.d(TAG, "activity onPause and camera preview stopped");
        }
    }

    public void destroy() {
        if (mCamera != null) {
            mCamera.release();
            if (DEBUG) Logger.d(TAG, "activity onDestroy and camera released");
        }
    }
}
