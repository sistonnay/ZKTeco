package com.zkteco.autk.models;

import android.text.TextUtils;

import com.zkteco.autk.IContract;
import com.zkteco.autk.utils.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.FormBody;
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
        public String type = "人脸识别";
        public String deviceId = SERIAL;

        public void upload(String url) {
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()//form表单
                    .add("JSONOBJECT", toString())
                    .build();

            Request request = new Request.Builder()
                    .url(url)//请求的url
                    .post(formBody)
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
                jsonObj.put("userName", name);   //字符串，如"张三"
                jsonObj.put("CardNo", job_number);//可转化为int
                jsonObj.put("verifyTime", stamp2DateTime(time));  //time为可转化为long的毫秒级时间戳
                jsonObj.put("getPastType", type); //字符串，如"人脸识别"
                jsonObj.put("storageNo", deviceId); //字符串，设备SN号
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

        public String stamp2DateTime(String stamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            return sdf.format(new Date(Long.parseLong(stamp))); // 时间戳转换日期
        }

        public String getUrlFromFile(String fileName) {
            String jsonString = FileUtil.getJSON(fileName);
            if (jsonString == null) {
                return "http://www.googleeeeeeeee.com";
            }
            try {
                JSONObject json = new JSONObject(jsonString);
                return json.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
                return "http://www.googleeeeeeeee.com";
            }
        }
    }
}
