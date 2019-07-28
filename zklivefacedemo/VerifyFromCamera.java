package com.zkteco.android.zklivefacedemo;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
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

public class VerifyFromCamera extends Activity implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private Button mTemplate1Button;
    private Button mTemplate2Button;
    private Button mVerifyButton;
    private TextView mStatusTextView;

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private final int CAMERA_WIDTH = 640;
    private final int CAMERA_HEIGH = 480;
    private final int cameraId = 0;

    private boolean FIRST_OPTION = false;
    private boolean SECOND_OPTION = false;

    private byte[] mTemplate1 = null;
    private byte[] mTemplate2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_from_camera);
        initView();
    }

    private void initView() {
        mSurfaceView = (SurfaceView)findViewById(R.id.verifySurfaceView);
        mTemplate1Button = (Button)findViewById(R.id.template1ButtonCamera);
        mTemplate2Button = (Button)findViewById(R.id.template2ButtonCamera);
        mVerifyButton = (Button)findViewById(R.id.verifyButtonCamera);
        mStatusTextView = (TextView)findViewById(R.id.statusTextCamera);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        OpenCameraAndSetSurfaceviewSize(cameraId);

        mTemplate1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FIRST_OPTION = true;
            }
        });
        mTemplate2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SECOND_OPTION = true;
            }
        });
        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTemplate1 == null){
                    mStatusTextView.setText(getString(R.string.pls_extract_template1));
                    return;
                }
                if(mTemplate2 == null){
                    mStatusTextView.setText(getString(R.string.pls_extract_template2));
                    return;
                }
                int score = ZKLiveFaceManager.getInstance().verify(mTemplate1,mTemplate2);
                if(score >= ZKLiveFaceManager.getInstance().DEFAULT_VERIFY_SCORE){
                    mStatusTextView.setText(getString(R.string.verify_success));
                }else{
                    mStatusTextView.setText(getString(R.string.verify_fail));
                }
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
            mCamera.setPreviewCallback(new VerifyPreview());
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

    class VerifyPreview implements Camera.PreviewCallback{
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if(FIRST_OPTION){
                FIRST_OPTION = false;
                mTemplate1 = ZKLiveFaceManager.getInstance().getTemplateFromNV21(data,CAMERA_WIDTH,CAMERA_HEIGH);
                if(mTemplate1 == null){
                    mStatusTextView.setText(getString(R.string.extract_template1_fail));
                }else{
                    mStatusTextView.setText(getString(R.string.extract_template1_success));
                }
            }
            if(SECOND_OPTION){
                SECOND_OPTION = false;
                mTemplate2 = ZKLiveFaceManager.getInstance().getTemplateFromNV21(data,CAMERA_WIDTH,CAMERA_HEIGH);
                if(mTemplate2 == null){
                    mStatusTextView.setText(getString(R.string.extract_template2_fail));
                }else{
                    mStatusTextView.setText(getString(R.string.extract_template2_success));
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
