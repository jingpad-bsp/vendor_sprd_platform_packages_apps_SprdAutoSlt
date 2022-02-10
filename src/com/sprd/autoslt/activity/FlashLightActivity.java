package com.sprd.autoslt.activity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.autoslt.R;
import com.sprd.autoslt.camera.ComboPreferences;
import com.sprd.autoslt.camera.PreviewFrameLayout;
import com.sprd.autoslt.camera.Tuple;
import com.sprd.autoslt.camera.Util;

public class FlashLightActivity extends Activity implements TextureView.SurfaceTextureListener{
    private static final String TAG = "FlashLightActivity";
    public static final int CAMERA_START = 0;
    private Camera mCamera = null;
    private int mCameraId = 0;
    private String mFlashLightcameraID = null;
    private TextureView mTextureView = null;
    private SurfaceTexture mSurfaceTexture = null;
    private TextView mLightMsg = null;
    private static final int PREVIEW_WIDTH = 320;
    private static final int PREVIEW_HEIGHT = 240;
    private boolean mFlag = false;
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private static final String testCaseName = "Camera test";
    private boolean isSurportCameraFlash = false;
    private Handler mHandler;
    public static FlashLightActivity instance;
    private Runnable mR = new Runnable() {
        public void run() {
        }
    };
    private Button mTakePhotoBtn;
    protected boolean getScreenState(ComboPreferences pref) {
        boolean result = false;
        if (pref != null) {
            String str_on = getString(R.string.pref_entry_value_on);
            String str_val = pref.getString("pref_camera_video_full_screen_key", null);
            result = (str_val != null && str_val.equals(str_on));
        }
        return result;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("huasong", "onCreate");
        super.onCreate(savedInstanceState);
        mFlashLightcameraID = getIntent().getStringExtra("flashlight_cameraID");
        if (mFlashLightcameraID.equalsIgnoreCase("front")) {
			mCameraId = BACK_CAMERA;
		}else if (mFlashLightcameraID.equalsIgnoreCase("back")) {
			mCameraId = FRONT_CAMERA;
		}
        //setTitle(getResources().getText(R.string.back_camera_title_text));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.back_camera_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setTitle(R.string.camera_test_title);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
//      mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mTextureView = (TextureView) findViewById(R.id.surfaceView);
        mTextureView.setSurfaceTextureListener(this);
        /* @}*/
        mLightMsg = (TextView)findViewById(R.id.light_msg);
        mLightMsg.setVisibility(View.VISIBLE);
        mHandler = new Handler();
        /*BEGIN 2016/04/13 zhijie.yang BUG535005 mmi add take photes of camera test */
        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCamera != null) {
                    mTakePhotoBtn.setEnabled(false);
                    mCamera.takePicture(shutterCallback, null, mPicture);
                } else {
                    Log.d(TAG, "mCamera is null.");
                }
            }
        });;
        instance = this;
    }

    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
        }
    };

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mCamera.startPreview();
            mTakePhotoBtn.setEnabled(true);
        }
    };
    /*END 2016/04/13 zhijie.yang BUG535005 mmi add take photes of camera test */

    private boolean isCameraFlashEnable() {
        PackageManager pm = (PackageManager) this.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : features) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCameraFrontEnable() {
        PackageManager pm = (PackageManager) this.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : features) {
            if (PackageManager.FEATURE_CAMERA_FRONT.equals(f.name)) {
                return true;
            }
        }
        return false;
    }
    private void startCamera() {
        if (mFlag) {
            Log.e(TAG, "stop & close");
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mFlag = false;
            }
        }
        try {
            Log.e(TAG, "open");
            Log.e("huasong", "open22");
            mCamera = Camera.open(mCameraId);
        } catch (RuntimeException e) {
            Log.e(TAG, "fail to open camera");
            Log.e("huasong", "fail to open camera");
            e.printStackTrace();
            mCamera = null;
        }
        if (mCamera != null) {
            Log.e("huasong", "mCamera2");
            Camera.Parameters parameters = null;
            parameters = mCamera.getParameters();
            parameters.set("orientation", "portrait");
            try {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				mCamera.release();
				return;
			}
            mCamera.setParameters(parameters);
            try {
                /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
//              mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewTexture(mSurfaceTexture);
                /* @}*/
                Log.e(TAG, "start preview");
                mCamera.startPreview();
                mFlag = true;
            } catch (Exception e) {
                mCamera.release();
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
            int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        mSurfaceTexture = surface;
        startCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
    /*public void surfaceChanged(SurfaceHolder sholder, int format, int width, int height) {
    }
    public void surfaceCreated(SurfaceHolder sholder) {
        startCamera();
    }
    public void surfaceDestroyed(SurfaceHolder sholder) {
    }*/
    /* @}*/

    @Override
    protected void onResume() {
        super.onResume();
        /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
        /*holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);*/
        /* @}*/
    }

    private void cameraNumber(){
        if(mCameraId == BACK_CAMERA){
            mCameraId = FRONT_CAMERA;
        if(mLightMsg!=null){
            mLightMsg.setVisibility(View.GONE);
                }
        }else{
            mCameraId = BACK_CAMERA;
         if(mLightMsg!=null && isSurportCameraFlash){
             mLightMsg.setVisibility(View.VISIBLE);
            }
       }
       Log.d("donglin", "switched camera id = " + mCameraId);
    }
    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        mHandler.removeCallbacks(mR);
        super.onDestroy();
    }
}
