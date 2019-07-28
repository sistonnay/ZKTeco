package com.zkteco.android.zklivefacedemo;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

/**
 * @author gy.lin
 * @create 2018/8/13
 * @Describe
 */

public class IdentifyFromCamera extends Activity implements SurfaceHolder.Callback{
    private SurfaceView mSurfaceView;
    private Button mTemplateButton;
    private Button mIdentifyButton;
    private TextView mStatusTextView;

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private final int CAMERA_WIDTH = 640;
    private final int CAMERA_HEIGH = 480;
    private final int cameraId = 0;

    private boolean ADD_OPTION = false;
    private boolean IDENTIFY_OPTION = false;

    private byte[] mTemplate1 = null;
    private byte[] mTemplate2 = null;

    private int FACEID = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_from_camera);
        initView();
    }

    private void initView() {
        mSurfaceView = (SurfaceView)findViewById(R.id.identifySurfaceView);
        mTemplateButton = (Button)findViewById(R.id.AddtemplateButtonCamera);
        mIdentifyButton = (Button)findViewById(R.id.identifyButtonCamera);
        mStatusTextView = (TextView)findViewById(R.id.statusTextViewCamera);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        OpenCameraAndSetSurfaceviewSize(cameraId);

        mTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ADD_OPTION = true;
            }
        });

        mIdentifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IDENTIFY_OPTION = true;
            }
        });
    }

    private Void OpenCameraAndSetSurfaceviewSize(int cameraId) {

        if(mCamera == null){
            mCamera = Camera.open(cameraId);
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(CAMERA_WIDTH, CAMERA_HEIGH);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        mCamera.setParameters(parameters);

        return null;
    }
    private Void SetAndStartPreview(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);
            mCamera.setPreviewCallback(new IdentifyPreview());
            mCamera.startPreview();
            //mCamera.cancelAutoFocus();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    private Void kill_camera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return null;
    }

    class IdentifyPreview implements Camera.PreviewCallback{
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if(ADD_OPTION){
                ADD_OPTION = false;
                mTemplate1 = ZKLiveFaceManager.getInstance().getTemplateFromNV21(data,CAMERA_WIDTH,CAMERA_HEIGH);
                if(mTemplate1 == null){
                    mStatusTextView.setText(getString(R.string.extract_template_fail));
                    return;
                }
                if(ZKLiveFaceManager.getInstance().dbAdd("faceID_"+FACEID,mTemplate1)){
                    mStatusTextView.setText(""+getString(R.string.dbadd_template_success)+",id="+"faceID_"+FACEID);
                    FACEID ++;
                }else{
                    mStatusTextView.setText(getString(R.string.dbadd_template_fail));
                }
            }
            if(IDENTIFY_OPTION){
                IDENTIFY_OPTION = false;
                mTemplate2 = ZKLiveFaceManager.getInstance().getTemplateFromNV21(data,CAMERA_WIDTH,CAMERA_HEIGH);
                if(mTemplate2 == null){
                    mStatusTextView.setText(getString(R.string.extract_template_fail));
                    return;
                }
                String id = ZKLiveFaceManager.getInstance().identify(mTemplate2);
                if(TextUtils.isEmpty(id)){
                    mStatusTextView.setText(getString(R.string.identify_fail));
                }else{
                    mStatusTextView.setText(""+getString(R.string.identify_success)+",id="+id);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mCamera!=null){
            SetAndStartPreview(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void onDestroy(){
        super.onDestroy();
        mTemplate1 = null;
        mTemplate2 = null;
    }
}
