package com.zkteco.autk.models;

import android.text.TextUtils;

import com.zkteco.autk.IContract;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 00:29
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EnrollModel implements IContract.IModel {

    private IdentifyInfo mIdentifyInfo;

    public EnrollModel() {
        mIdentifyInfo = new IdentifyInfo();
    }

    public IdentifyInfo getIdentifyInfo() {
        return mIdentifyInfo;
    }

    public static class IdentifyInfo {
        public String name;
        public String id;
        public String phone;
        public String faceId;

        public boolean isLegalEnrollInfo() {
            return !(TextUtils.isEmpty(name) || TextUtils.isEmpty(id) || TextUtils.isEmpty(phone));
        }
    }
}
