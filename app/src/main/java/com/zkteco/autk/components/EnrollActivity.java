package com.zkteco.autk.components;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.zkteco.autk.R;
import com.zkteco.autk.presenters.EnrollPresenter;
import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;
import com.zkteco.autk.views.RoundProgressBar;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:27
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollActivity extends BaseActivity<EnrollPresenter> {
    private static final String TAG = Utils.TAG + "#" + EnrollActivity.class.getSimpleName();

    private ImageView mFaceRect;
    private TextureView mPreVRect;
    private RoundProgressBar mRoundProgress;
    private Button mEnroll;
    private boolean isEnrolling = false;

    private int mProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.setSurfaceTextureListener(mPreVRect);
        mPresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_enroll;
    }

    @Override
    public void initViews() {
        mFaceRect = (ImageView) findViewById(R.id.face_info);
        mPreVRect = (TextureView) findViewById(R.id.preview);
        //mPreVRect = (CameraPreview) mContext.findViewById(R.id.preview);

        mRoundProgress = (RoundProgressBar) findViewById(R.id.detector_progress);
        mRoundProgress.setProgressTextVisibility(RoundProgressBar.ProgressTextVisibility.Invisible);
        mRoundProgress.setProgress(mProgress);

        WindowManager wm =getWindowManager();
        ViewGroup.LayoutParams params = mPreVRect.getLayoutParams();
        params.width = wm.getDefaultDisplay().getWidth();
        params.height = (int) (params.width * (640.0f / 480.0f));
        Logger.d(TAG, "surface view width = " + params.width + ", height = " + params.height);

        mPreVRect.setLayoutParams(params);
        mFaceRect.setLayoutParams(params);

        mEnroll = (Button) findViewById(R.id.enroll);
        mEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnrolling) {
                    mPresenter.identifyFace();
                    mEnroll.setText(R.string.button_enroll);
                    isEnrolling = false;
                } else {
                    mPresenter.enrollFace();
                    mEnroll.setText(R.string.button_identify);
                    isEnrolling = true;
                }
            }
        });
    }

    @Override
    public void initPresenter() {
        mPresenter = new EnrollPresenter();
    }

    public void setFaceViewImage(final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFaceRect.setImageBitmap(bmp);
            }
        });
    }

    public void updateProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress += 20;
                mRoundProgress.setProgress(mProgress);
            }
        });
    }

    public void resetProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress = 0;
                mRoundProgress.setProgress(mProgress);
            }
        });
    }

    public void fillProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress = 100;
                mRoundProgress.setProgress(mProgress);
            }
        });
    }

    public void updateUI() {

    }
}
