package com.zkteco.autk.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;

import com.zkteco.autk.R;
import com.zkteco.autk.dao.DatabaseHelper;
import com.zkteco.autk.dao.DatabaseUtils;
import com.zkteco.autk.models.ZKLiveFaceManager;
import com.zkteco.autk.utils.PermissionUtil;

import java.io.File;
import java.util.List;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 22:58
 * email: 372022839@qq.com (github: sistonnay)
 */
public class WelcomeActivity extends BaseActivity {

    private final int INIT_CODE = 1;
    private final int INIT_MSG_FAIL = 1;
    private final int INIT_MSG_SUCCESS = 2;
    private final int INIT_MSG_CHOOSE_FAIL = -1;

    private String licFilePath = null;
    private TextView mAlertText = null;
    private DatabaseHelper mHelper = null;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_MSG_SUCCESS:
                    mAlertText.setText(R.string.init_algorithm_success);
                    toast(getString(R.string.init_algorithm_success));
                    if(ZKLiveFaceManager.getInstance().isInit()) {
                        Intent intent = new Intent("com.intent.action.ENROLL");
                        startActivity(intent);
                        finish();
                    }
                    break;
                case INIT_MSG_FAIL:
                    mAlertText.setText(R.string.init_algorithm_fail);
                    toast(getString(R.string.init_algorithm_fail));
                    break;
                case INIT_MSG_CHOOSE_FAIL:
                    mAlertText.setText(R.string.choose_lic_file_failed);
                    toast(getString(R.string.choose_lic_file_failed));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //PermissionUtil.getInstance().authorize(this);
        PermissionUtil.getInstance().verifyPermissions(this);

        if (!ZKLiveFaceManager.getInstance().isAuthorized()) {
            String hardwareId = ZKLiveFaceManager.getInstance().getHardwareId();
            String defaultDevFpFilePath = "/sdcard/zklivefacedevfp.txt";
            String title  = getString(R.string.dialog_title_auth);
            String message = String.format(getString(R.string.dialog_message_auth), defaultDevFpFilePath, hardwareId);

            SimpleDialog dialog = new SimpleDialog(this, title, message) {
                @Override
                public void onDialogOK() {
                    ExFilePicker exFilePicker = new ExFilePicker();
                    exFilePicker.setQuitButtonEnabled(true);
                    exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                    exFilePicker.setChoiceType(ExFilePicker.ChoiceType.FILES);
                    exFilePicker.start(WelcomeActivity.this, INIT_CODE);
                }
            };
            dialog.disableCancel(true);
            dialog.show();
        } else {
            mHelper = new DatabaseHelper(this);
            init("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ZKLiveFaceManager.getInstance().isAuthorized()) {
            finish();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.welcome_ly;
    }

    @Override
    public void initViews() {
        mAlertText = (TextView) findViewById(R.id.alert_text);
    }

    @Override
    public void initPresenter() {

    }

    private void init(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ZKLiveFaceManager.getInstance().setParameterAndInit(path)) {
                    DatabaseUtils.getInstance().initFaceLibrary(mHelper);
                    mHandler.obtainMessage(INIT_MSG_SUCCESS).sendToTarget();
                } else {
                    mHandler.obtainMessage(INIT_MSG_FAIL).sendToTarget();
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (INIT_CODE == requestCode) {
                ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
                if (result == null || result.getCount() <= 0) {
                    mHandler.obtainMessage(INIT_MSG_CHOOSE_FAIL).sendToTarget();
                    return;
                }

                String path = result.getPath();
                List<String> names = result.getNames();

                for (int i = 0; i < names.size(); i++) {
                    File f = new File(path, names.get(i));
                    licFilePath = f.getAbsolutePath();
                    break;
                }

                if (!TextUtils.isEmpty(licFilePath)) {
                    init(licFilePath);
                } else {
                    mHandler.obtainMessage(INIT_MSG_CHOOSE_FAIL).sendToTarget();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
