package com.zkteco.autk.models;

import android.graphics.Bitmap;

import com.zkteco.android.biometric.liveface56.ZKLiveFaceService;
import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

/**
 * @author gy.lin
 * @create 2018/8/13
 * @Describe
 */
public class ZKLiveFaceManager {
    private static final String TAG = Utils.TAG + "#" + ZKLiveFaceManager.class.getSimpleName();

    private static ZKLiveFaceManager zkLiveFaceManager = null;
    private long context;
    private boolean isInit = false;
    public final int DEFAULT_VERIFY_SCORE = 65;

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public static ZKLiveFaceManager getInstance() {
        if (zkLiveFaceManager == null) {
            zkLiveFaceManager = new ZKLiveFaceManager();
        }
        return zkLiveFaceManager;
    }

    public boolean isAuthorized() {
        return ZKLiveFaceService.isAuthorized();
    }

    public String getHardwareId() {
        byte[] hwid = new byte[256];
        int[] size = new int[1];
        size[0] = 256;
        if (0 == ZKLiveFaceService.getHardwareId(hwid, size)) {
            String hwidStr = new String(hwid, 0, size[0]);
            Logger.d(TAG, "machinecode:" + hwidStr);
            return hwidStr;
        } else {
            return null;
        }
    }

    public String getDeviceFingerprint() {
        byte[] hwid = new byte[32 * 1024];
        int[] size = new int[1];
        size[0] = 32 * 1024;
        if (0 == ZKLiveFaceService.getDeviceFingerprint(hwid, size)) {
            String hwidStr = new String(hwid, 0, size[0]);
            return hwidStr;
        } else {
            return null;
        }
    }

    public boolean setParameterAndInit(String path) {
        if (!isAuthorized()) {
            ZKLiveFaceService.setParameter(0, 1011, path.getBytes(), path.length());
        }
        long[] retContext = new long[1];
        int ret = ZKLiveFaceService.init(retContext);
        Logger.i(TAG, "init ret = " + ret);
        if (ret == 0) {
            context = retContext[0];
            ZKLiveFaceService.dbClear(context);
            setInit(true);
            return true;
        } else {
            setInit(false);
            return false;
        }
    }

    public byte[] getTemplateFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int[] detectedFaces = new int[1];
        int ret = ZKLiveFaceService.detectFacesFromBitmap(context, bitmap, detectedFaces);
        if (ret != 0 || detectedFaces[0] <= 0) {
            return null;
        }
        long[] faceContext = new long[1];
        ret = ZKLiveFaceService.getFaceContext(context, 0, faceContext);
        if (ret != 0) {
            return null;
        }
        byte[] template = new byte[8 * 1024];
        int[] size = new int[1];
        int[] resverd = new int[1];
        size[0] = 8 * 1024;
        ret = ZKLiveFaceService.extractTemplate(faceContext[0], template, size, resverd);
        if (ret == 0) {
            return template;
        }
        return null;
    }

    public byte[] getTemplateFromNV21(byte[] nv21, int width, int heigh) {
        int[] detectedFaces = new int[1];
        int ret = ZKLiveFaceService.detectFacesFromNV21(context, nv21, width, heigh, detectedFaces);
        if (ret != 0 || detectedFaces[0] <= 0) {
            return null;
        }
        long[] faceContext = new long[1];
        ret = ZKLiveFaceService.getFaceContext(context, 0, faceContext);
        if (ret != 0) {
            return null;
        }
        byte[] template = new byte[8 * 1024];
        int[] size = new int[1];
        int[] resverd = new int[1];
        size[0] = 8 * 1024;
        ret = ZKLiveFaceService.extractTemplate(faceContext[0], template, size, resverd);
        if (ret == 0) {
            return template;
        }
        return null;
    }

    public int verify(byte[] template1, byte[] template2) {
        int[] score = new int[1];
        int ret = ZKLiveFaceService.verify(context, template1, template2, score);
        Logger.i(TAG, "verify ret = " + ret);
        if (ret == 0) {
            return score[0];
        }
        return 0;
    }

    public String identify(byte[] template) {
        int[] score = new int[1];
        byte[] faceIDS = new byte[256];
        int[] maxRetCount = new int[1];
        maxRetCount[0] = 1;
        int ret = ZKLiveFaceService.dbIdentify(context, template, faceIDS, score, maxRetCount, 78, 100);
        if (ret != 0) {
            return null;
        }
        return new String(faceIDS).trim().toString();
    }

    public boolean dbAdd(String id, byte[] template) {
        int ret = ZKLiveFaceService.dbAdd(context, id, template);
        if (ret == 0) {
            return true;
        }
        return false;
    }
}
