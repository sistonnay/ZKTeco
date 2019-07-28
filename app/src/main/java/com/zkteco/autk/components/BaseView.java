package com.zkteco.autk.components;

import com.zkteco.autk.IContract;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:03
 * email: 372022839@qq.com (github: sistonnay)
 */
public interface BaseView extends IContract.IView {
    int getLayoutId();

    void initViews();
}
