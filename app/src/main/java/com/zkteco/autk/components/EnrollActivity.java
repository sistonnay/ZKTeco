package com.zkteco.autk.components;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zkteco.autk.R;
import com.zkteco.autk.presenters.EnrollPresenter;
import com.zkteco.autk.utils.Utils;
import com.zkteco.autk.views.OverlayView;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:27
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollActivity extends BaseActivity<EnrollPresenter> implements View.OnClickListener {
    private static final String TAG = Utils.TAG + "#" + EnrollActivity.class.getSimpleName();

    private static final String ADMIN_PASS = "123456";

    public static final int MODE_NULL = -1;
    public static final int MODE_IDENTIFY = 0;
    public static final int MODE_CHECK_IN = 1;
    public static final int MODE_PRE_ENROLL = 2;
    public static final int MODE_ENROLLING = 3;

    private TextureView mPreVRect;
    private OverlayView mOverlayRect;
    private OverlayView.OverlayTheme mEnrollTheme;
    private OverlayView.OverlayTheme mIdentifyTheme;

    private LinearLayout mAlert;
    private LinearLayout mInputInfo;
    private LinearLayout mRegisterInfo;
    private ImageView mBackButton;
    private ImageView mNextButton;
    private ImageView mEnrollButton;

    private TextView mPassText;
    private TextView mNameText;
    private TextView mJobNumberText;
    private TextView mPhoneText;

    private int mode = MODE_IDENTIFY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        mPresenter.init();
        refreshUI();
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
        /*
        WindowManager wm =getWindowManager();
        ViewGroup.LayoutParams params = mPreVRect.getLayoutParams();
        params.width = wm.getDefaultDisplay().getWidth();
        params.height = (int) (params.width * (640.0f / 360.0f));
        Logger.d(TAG, "surface view width = " + params.width + ", height = " + params.height);

        mPreVRect.setLayoutParams(params);
        */

        mAlert = (LinearLayout) findViewById(R.id.ly_alert);

        mInputInfo = (LinearLayout) findViewById(R.id.ly_input_info);
        mBackButton = (ImageView) mInputInfo.findViewById(R.id.back);
        mBackButton.setOnClickListener(this);
        mNextButton = (ImageView) mInputInfo.findViewById(R.id.next);
        mNextButton.setOnClickListener(this);
        mPassText = (TextView) mInputInfo.findViewById(R.id.tv_password);
        mPassText.setOnClickListener(this);

        mRegisterInfo = (LinearLayout) mInputInfo.findViewById(R.id.ly_register_info);
        mNameText = (TextView) mRegisterInfo.findViewById(R.id.tv_name);
        mNameText.setOnClickListener(this);
        mJobNumberText = (TextView) mRegisterInfo.findViewById(R.id.tv_job_number);
        mJobNumberText.setOnClickListener(this);
        mPhoneText = (TextView) mRegisterInfo.findViewById(R.id.tv_phone);
        mPhoneText.setOnClickListener(this);

        mEnrollButton = (ImageView) findViewById(R.id.enroll);
        mEnrollButton.setOnClickListener(this);

        mEnrollTheme = mOverlayRect.getThemeFromTypedArray(
                obtainStyledAttributes(R.style.OverlayView_Enroll, R.styleable.OverlayView));
        mIdentifyTheme = mOverlayRect.getThemeFromTypedArray(
                obtainStyledAttributes(R.style.OverlayView_Identify, R.styleable.OverlayView));
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void refreshUI() {
        switch (mode) {
            case MODE_ENROLLING: {
                mAlert.setVisibility(View.GONE);
                mInputInfo.setVisibility(View.VISIBLE);
                mEnrollButton.setVisibility(View.GONE);
                mOverlayRect.setTheme(mEnrollTheme);
            }
            break;
            case MODE_PRE_ENROLL: {
                mAlert.setVisibility(View.GONE);
                mInputInfo.setVisibility(View.VISIBLE);
                mRegisterInfo.setVisibility(View.VISIBLE);
                mPassText.setVisibility(View.GONE);
                mEnrollButton.setVisibility(View.GONE);
                mOverlayRect.setTheme(mEnrollTheme);
            }
            break;
            case MODE_CHECK_IN: {
                mAlert.setVisibility(View.GONE);
                mInputInfo.setVisibility(View.VISIBLE);
                mPassText.setVisibility(View.VISIBLE);
                mRegisterInfo.setVisibility(View.GONE);
                mEnrollButton.setVisibility(View.GONE);
                mOverlayRect.setTheme(mIdentifyTheme);
            }
            break;
            case MODE_IDENTIFY: {
                mAlert.setVisibility(View.VISIBLE);
                mInputInfo.setVisibility(View.GONE);
                mEnrollButton.setVisibility(View.VISIBLE);
                mOverlayRect.setTheme(mIdentifyTheme);
            }
            break;
        }
        mNameText.setText(mPresenter.getName());
        mJobNumberText.setText(mPresenter.getJobNumber());
        mPhoneText.setText(mPresenter.getPhone());
        mPassText.setText(mPresenter.getAdminPass());
    }

    @Override
    public void initPresenter() {
        mPresenter = new EnrollPresenter();
    }

    @Override
    public void onBackPressed() {
        switch (mode) {
            case MODE_ENROLLING:
            case MODE_PRE_ENROLL:
            case MODE_CHECK_IN: {
                mode = MODE_IDENTIFY;
                mPresenter.resetInfo();
            }
            break;
            case MODE_IDENTIFY: {
                super.onBackPressed();
            }
            break;
        }
        refreshUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: {
                onBackPressed();
            }
            break;
            case R.id.next: {
                if (mode == MODE_CHECK_IN) {
                    if (TextUtils.equals(mPresenter.getAdminPass(), ADMIN_PASS)) {
                        mode = MODE_PRE_ENROLL;
                    } else {
                        toast("Password Error!");
                        return;
                    }
                } else if (mode == MODE_PRE_ENROLL) {
                    if (mPresenter.isLegalEnrollInfo()) {
                        mode = MODE_ENROLLING;
                    } else {
                        toast("Name or ID or Phone Error!");
                        return;
                    }
                }
                refreshUI();
            }
            break;
            case R.id.enroll: {
                mode = MODE_CHECK_IN;
                refreshUI();
            }
            break;
            case R.id.tv_password: {
                EditDialog dialog = new EditDialog(this, R.string.dialog_title_pass, InputType.TYPE_CLASS_NUMBER) {
                    @Override
                    public void onDialogOK(String text) {
                        mPassText.setText(text);
                        mPresenter.setAdminPass(text);
                    }
                };
                dialog.passWordStyle(true);
                dialog.show();
            }
            break;
            case R.id.tv_name: {
                new EditDialog(this, R.string.dialog_title_name, InputType.TYPE_CLASS_TEXT) {
                    @Override
                    public void onDialogOK(String text) {
                        mNameText.setText(text);
                        mPresenter.setName(text);
                    }
                }.show();
            }
            break;
            case R.id.tv_job_number: {
                new EditDialog(this, R.string.dialog_title_job_number, InputType.TYPE_CLASS_TEXT) {
                    @Override
                    public void onDialogOK(String text) {
                        mJobNumberText.setText(text);
                        mPresenter.setJobNumber(text);
                    }
                }.show();
            }
            break;
            case R.id.tv_phone: {
                new EditDialog(this, R.string.dialog_title_phone, InputType.TYPE_CLASS_PHONE) {
                    @Override
                    public void onDialogOK(String text) {
                        mPhoneText.setText(text);
                        mPresenter.setPhone(text);
                    }
                }.show();
            }
            break;
        }
    }
}
