package com.zkteco.autk;

/**
 * author: Created by Ho Dao on 2019/7/28 0028 22:47
 * email: 372022839@qq.com (github: sistonnay)
 */
public interface IContract {

    interface IView {
        void initPresenter();
    }

    interface IPresenter<V> {
        void attachView(V view);
        boolean isViewAttached();
        void detachView();
    }

    interface IModel {

    }
}
