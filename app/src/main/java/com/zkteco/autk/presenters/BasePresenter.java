package com.zkteco.autk.presenters;

import com.zkteco.autk.IContract;
import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * author: Created by Ho Dao on 2019/7/28 0028 23:14
 * email: 372022839@qq.com (github: sistonnay)
 */
public abstract class BasePresenter<M extends IContract.IModel, V extends IContract.IView> implements IContract.IPresenter<V> {
    private static final String TAG = Utils.TAG + "#" + BasePresenter.class.getSimpleName();

    protected M mModel;
    protected WeakReference<V> mView;

    @Override
    public void attachView(V view) {
        mView = new WeakReference(view);
        Logger.v(TAG, "view attached");
    }

    @Override
    public boolean isViewAttached() {
        return mView != null && mView.get() != null;
    }

    @Override
    public void detachView() {
        mView.clear();
        Logger.v(TAG, "view detached");
    }
}
