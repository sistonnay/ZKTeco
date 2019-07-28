package com.zkteco.autk.components;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.zkteco.autk.presenters.BasePresenter;

/**
 * author: Created by Ho Dao on 2019/7/28 0028 23:04
 * email: 372022839@qq.com (github: sistonnay)
 */
public abstract class BaseActivity<P extends BasePresenter> extends Activity implements BaseView {

    protected P mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(getLayoutId());

        initPresenter();
        mPresenter.attachView(this);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
