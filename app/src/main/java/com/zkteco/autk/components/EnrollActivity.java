package com.zkteco.autk.components;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.zkteco.autk.presenters.EnrollPresenter;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:27
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollActivity extends BaseActivity<EnrollPresenter> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void initViews() {

    }

    @Override
    public void initPresenter() {
        mPresenter = new EnrollPresenter();
    }
}
