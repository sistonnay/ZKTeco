package com.zkteco.autk.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.zkteco.autk.utils.Utils;

public abstract class SimpleDialog {

    private static final String TAG = Utils.TAG + "#" + SimpleDialog.class.getSimpleName();

    private Context mContext;
    private String mTitle;
    private String mMessage;
    private boolean cancelDisabled;
    private AlertDialog.Builder mDialogBuilder;

    public SimpleDialog(Context context, String title, String message) {
        mContext = context;
        mTitle = title;
        mMessage = message;
        mDialogBuilder = new AlertDialog.Builder(mContext);
    }

    public SimpleDialog(Context context, int title, int message) {
        mContext = context;
        mTitle = mContext.getString(title);
        mMessage = mContext.getString(message);
        mDialogBuilder = new AlertDialog.Builder(mContext);
    }

    public void disableCancel(boolean disable) {
        this.cancelDisabled = disable;
    }

    public void show() {
        mDialogBuilder.setTitle(mTitle);
        mDialogBuilder.setMessage(mMessage);
        mDialogBuilder.setCancelable(cancelDisabled);
        mDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogOK();
                dialog.dismiss();
            }
        });
        if (!cancelDisabled) {
            mDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        mDialogBuilder.create().show();
    }

    public abstract void onDialogOK();
}
