package com.zkteco.autk.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Trace;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Created by Ho Dao on 2019/7/29 0029 20:03
 * email: 372022839@qq.com (github: sistonnay)
 */
public class PermissionUtil {
    private static final String TAG = Utils.TAG + "#" + PermissionUtil.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST = 0x0001;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};

    private List<String> mUncheckedPermissions = new ArrayList<>();

    private PermissionUtil() {
    }

    private static volatile PermissionUtil INSTANCE;

    public static PermissionUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (PermissionUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PermissionUtil();
                }
            }
        }
        return INSTANCE;
    }

    public boolean verifyPermissions(Activity activity) {
        Trace.beginSection("initPermissions");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mUncheckedPermissions.clear();
                for (String permission : REQUIRED_PERMISSIONS) {
                    if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                        mUncheckedPermissions.add(permission);
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                            Logger.v(TAG, "Required permissions are accessed for this application.");
                        }
                    }
                }
                if (mUncheckedPermissions.size() > 0) {
                    ActivityCompat.requestPermissions(activity, mUncheckedPermissions.toArray(new String[mUncheckedPermissions.size()]), PERMISSIONS_REQUEST);
                    return false;
                }
            }
        } finally {
            Trace.endSection();
        }
        return true;
    }

    public void onRequestPermissionsResult(
            Activity activity, final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean isAllGranted = true;
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                activity.overridePendingTransition(0, 0);
            } else {
                activity.finish();
            }
        }
    }

}
