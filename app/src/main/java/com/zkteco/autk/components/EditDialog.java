package com.zkteco.autk.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

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
    private int mInputType;
    private boolean textHide = false;
    private boolean cancelDisabled = true;
    private AlertDialog.Builder mDialogBuilder;
    private int displayWidth, displayHeight;

    public EditDialog(Context context, String title, int inputType) {
        mContext = context;
        mTitle = title;
        mInputType = inputType;
        mDialogBuilder = new AlertDialog.Builder(mContext, R.style.EditDialog);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        displayWidth = wm.getDefaultDisplay().getWidth();
        displayHeight = wm.getDefaultDisplay().getHeight();
    }

    public EditDialog(Context context, int title, int inputType) {
        this(context, context.getString(title), inputType);
    }

    public void passWordStyle(boolean show){
        this.textHide = show;
    }

    public void disableCancel(boolean disable) {
        this.cancelDisabled = disable;
    }

    public void show() {
        final View root = LayoutInflater.from(mContext).inflate(R.layout.et_ly, null);
        final EditText editText = (EditText) root.findViewById(R.id.edit_text);
        editText.setInputType(mInputType);
        if (textHide) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        mDialogBuilder.setView(root);
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
        AlertDialog dialog = mDialogBuilder.create();

        final TextView title = new TextView(mContext);
        title.setText(mTitle);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.parseColor("#E4E4E4"));
        title.setTextSize(23);
        dialog.setCustomTitle(title);//设置字体

        dialog.show();

        final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = 3 * displayWidth / 5;
        dialog.getWindow().setAttributes(params);
    }

    public abstract void onDialogOK(String text);
}