package com.zkteco.autk.models;

import android.text.TextUtils;

import com.zkteco.autk.IContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.os.Build.SERIAL;

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
        public String phone;
        public String faceId;
        public String job_number;
        public byte[] face_template;

        public boolean isLegalEnrollInfo() {
            return !(TextUtils.isEmpty(name) || TextUtils.isEmpty(job_number) || TextUtils.isEmpty(phone));
        }

        public void reset() {
            name = null;
            phone = null;
            faceId = null;
            job_number = null;
            face_template = null;
        }
    }

    public static class uploadInfo {
        public String name;
        public String job_number;
        public String time;
        public String type = "face";
        public String deviceId = SERIAL;

        public void upload() {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = FormBody.create(
                    MediaType.parse("application/json; charset=utf-8"), toString());
            Request request = new Request.Builder()
                    .url("https://www.google.com")//请求的url
                    .post(requestBody)
                    .build();
            okhttp3.Call call = client.newCall(request);
            //异步请求，若采用同步请求的方式最好在线程中执行
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // response.body();获取返回数据
                        // final String res = response.body().string();
                    }
                }
            });
        }

        public JSONObject toJSON() {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("Name", name);   //字符串，如"张三"
                jsonObj.put("Number", job_number);//可转化为int
                jsonObj.put("Time", time);   //可转化为long的毫秒级时间戳
                jsonObj.put("Method", type); //字符串，如"face"
                jsonObj.put("deviceId", deviceId); //字符串，设备SN号
                return jsonObj;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return toJSON().toString();
        }
    }
}
