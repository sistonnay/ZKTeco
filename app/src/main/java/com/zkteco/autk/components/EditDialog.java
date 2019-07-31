package com.zkteco.autk.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import com.zkteco.autk.R;
import com.zkteco.autk.utils.Utils;

/**
 * author: Created by Ho Dao on 2019/8/1 0001 00:47
 * email: 372022839@qq.com (github: sistonnay)
 */
public abstract class EditDialog {

    private static final String TAG = Utils.TAG + "#" + EditDialog.class.getSimpleName();

    private Context mContext;
    private String mTitle;
    private int mInputType = InputType.TYPE_CLASS_TEXT;
    private boolean cancelDisabled = true;
    private AlertDialog.Builder mDialogBuilder;

    public EditDialog(Context context, String title, int inputType) {
        mContext = context;
        mTitle = title;
        mInputType= inputType;
        mDialogBuilder = new AlertDialog.Builder(mContext);
    }

    public EditDialog(Context context, int title, int inputType) {
        mContext = context;
        mTitle = mContext.getString(title);
        mDialogBuilder = new AlertDialog.Builder(mContext);
    }

    public void disableCancel(boolean disable) {
        this.cancelDisabled = disable;
    }

    public void show() {
        final EditText editText = new EditText(mContext);
        editText.setInputType(mInputType);
        editText.setBackground(mContext.getResources().getDrawable(R.drawable.et_underline_blue));
        mDialogBuilder.setView(editText);
        mDialogBuilder.setTitle(mTitle);
        mDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogOK(editText.getText().toString());
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

    public abstract void onDialogOK(String text);
}