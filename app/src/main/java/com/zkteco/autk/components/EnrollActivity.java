package com.zkteco.autk.components;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.zkteco.autk.R;
import com.zkteco.autk.presenters.EnrollPresenter;
import com.zkteco.autk.utils.Utils;
import com.zkteco.autk.views.OverlayView;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:27
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollActivity extends BaseActivity<EnrollPresenter> {
    private static final String TAG = Utils.TAG + "#" + EnrollActivity.class.getSimpleName();

    private TextureView mPreVRect;
    private OverlayView mOverlayRect;
    private OverlayView.OverlayTheme mEnrollTheme;
    private OverlayView.OverlayTheme mIdentifyTheme;

    private ImageView mEnroll;
    private boolean isEnrolling = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        mPreVRect = (TextureView) findViewById(R.id.preview);
        mOverlayRect = (OverlayView) findViewById(R.id.overlay_view);

        final TypedArray taEnroll = obtainStyledAttributes(R.style.OverlayView_Enroll, R.styleable.OverlayView);
        mEnrollTheme = mOverlayRect.getThemeFromTypedArray(taEnroll);

        final TypedArray taIdentify = obtainStyledAttributes(R.style.OverlayView_Identify, R.styleable.OverlayView);
        mIdentifyTheme = mOverlayRect.getThemeFromTypedArray(taIdentify);

        /*
        WindowManager wm =getWindowManager();
        ViewGroup.LayoutParams params = mPreVRect.getLayoutParams();
        params.width = wm.getDefaultDisplay().getWidth();
        params.height = (int) (params.width * (640.0f / 360.0f));
        Logger.d(TAG, "surface view width = " + params.width + ", height = " + params.height);

        mPreVRect.setLayoutParams(params);
        */

        mEnroll = (ImageView) findViewById(R.id.enroll);
        mEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnrolling) {
                    mPresenter.identifyFace();
                    isEnrolling = false;
                } else {
                    mPresenter.enrollFace();
                    isEnrolling = true;
                }
            }
        });
    }

    @Override
    public void initPresenter() {
        mPresenter = new EnrollPresenter();
    }

    public void showEnrollTheme() {
        mOverlayRect.setTheme(mEnrollTheme);
    }

    public void showIdentifyTheme() {
        mOverlayRect.setTheme(mIdentifyTheme);
    }
}
