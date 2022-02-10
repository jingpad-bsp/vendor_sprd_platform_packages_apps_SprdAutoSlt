package com.sprd.autoslt.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
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
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

public class CameraTestActivity extends Activity implements
        TextureView.SurfaceTextureListener {

    private static final String TAG = "CameraBackTestActivity";
    private CameraScreenNailProxy mCameraScreenNailProxy;
    public static final int CAMERA_START = 0;
    public static boolean mIsActivityInFront = false;
    public static final String FLASH_MODE_OFF = "off";
    public static final String FLASH_MODE_AUTO = "auto";
    public static final String FLASH_MODE_ON = "on";
    private static final String SHARED_PREFS_APR_FILE = "autoslt";
    private static final String FLASH_MODE_NAME = "flash_mode";
    private static final int FRONT_CAMERA = 1;
    private static final int BACK_CAMERA = 0;
    private static final int BACK_SECOND_CAMERA =2;
    private static int mDisplayRotation = 0;
    private int isFullTest = 0;
    private int fullTestActivityId;
    private Camera mCamera = null;
    private int mCameraId = 0;
    private SurfaceView mSurfaceView = null;
    private TextureView mTextureView = null;
    private SurfaceTexture mSurfaceTexture = null;
    private PreviewFrameLayout mPreviewFrameLayout;
    private TextView mLightMsg = null;
    private Button mTakePhotoBtn, mFlashLightBtn;
    private SurfaceHolder holder = null;
    private static final int PREVIEW_WIDTH = 320;
    private static final int PREVIEW_HEIGHT = 240;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private ComboPreferences mPreferences;
    private boolean mFlag = false;
    private final String testCaseName = "Camera test";
    private int groupId;
    private boolean isSurportCameraFlash = false;
    private Handler mHandler;
    private int mFlow = -1;
    private String mFileName = null;
    private String currentTestcamera = null;
    private TextView mCountDownView;
    private CountDownTimer mCountDownTimer;
    private float textSize;
    private boolean mIsAuto = false;
    private boolean mIsFront = false;
    private static String FLASH_MODE = "auto";
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private Runnable mR = new Runnable() {
        public void run() {
            if (mCamera != null) {
                Log.d(TAG, "zhijie is null.");
                try {
                    mCamera.takePicture(shutterCallback, null, mPicture);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    if (mCamera != null) {
                        Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(parameters);
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                    }
                    TestResultUtil.getInstance().setCurrentStepStatus("takePicture error");
                    TestResultUtil.getInstance().setCurrentResult("fail");
                    finish();
                    return;
                }
            } else {
                Log.d(TAG, "mCamera is null.");
                Toast.makeText(getApplicationContext(),
                        getString(R.string.camera_takepicture_fail_tips),
                        Toast.LENGTH_SHORT).show();
                TestResultUtil.getInstance().setCurrentStepStatus("camera null");
                TestResultUtil.getInstance().setCurrentResult("fail");
                finish();
                return;
            }
            mHandler.postDelayed(mR2, 2000);
        }
    };
    private Runnable mR2 = new Runnable() {
        public void run() {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.camera_takepicture_tips),
                    Toast.LENGTH_SHORT).show();
            if(Environment.getExternalStorageState(new File(SLTConstant.SLT_SD_PATH)).equals("mounted")) {
                TestResultUtil.getInstance().setCurrentStepName(
                        SLTConstant.SLT_SDCARD_PATH + mFileName);
            } else {
                TestResultUtil.getInstance().setCurrentStepName(
                        SLTConstant.SLT_INTERNAL_PATH + mFileName);
            }
            TestResultUtil.getInstance().setCurrentResult("ok");
            finish();
        }
    };

    protected class CameraScreenNailProxy {
        private static final String TAG = "CameraScreenNailProxy";

        public static final int KEY_SIZE_PICTURE = 0;
        public static final int KEY_SIZE_PREVIEW = 1;

        private Tuple<Integer, Integer> mScreenSize;

        protected CameraScreenNailProxy() {
            initializeScreenSize();
        }

        private void initializeScreenSize() {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            mScreenSize = new Tuple<Integer, Integer>(metrics.widthPixels,
                    metrics.heightPixels);
            Log.d(TAG,
                    String.format("screen size = { %dx%d }", new Object[] {
                            mScreenSize.first, mScreenSize.second }));
        }

        protected Tuple<Integer, Integer> getOptimalSize(int key,
                ComboPreferences pref) {

            Tuple<Integer, Integer> result = null;
            Size size = null;
            boolean b_full_screen = getScreenState(pref);
            int orientation = getOrientation();
            int width = mScreenSize.first, height = mScreenSize.second;
            Camera.Parameters mParameters = null;
            mParameters = mCamera.getParameters();

            if (KEY_SIZE_PICTURE == key) {
                size = mParameters.getPictureSize();
                width = size.width;
                height = size.height;
                result = Util.getOptimalSize(mScreenSize.first,
                        mScreenSize.second, width, height, b_full_screen);
                width = result.first;
                height = result.second;
                if (orientation % 180 == 0) {
                    int tmp = width;
                    width = height;
                    height = tmp;
                }
            }

            if (KEY_SIZE_PREVIEW == key) {
                size = mParameters.getPreviewSize();
                width = size.width;
                height = size.height;
                result = Util.getOptimalSize(mScreenSize.first,
                        mScreenSize.second, width, height, b_full_screen);
                width = result.first;
                height = result.second;
                if (orientation % 180 == 0) {
                    int tmp = width;
                    width = height;
                    height = tmp;
                }
            }

            result = new Tuple<Integer, Integer>(width, height);
            Log.d(TAG,
                    String.format(
                            "get optimal size: key = %d, is_full_screen = %b, size = { %dx%d }",
                            new Object[] { key, b_full_screen, result.first,
                                    result.second }));
            return result;
        }

        private int getOrientation() {
            return getCameraDisplayOrientation(mCameraId, mCamera);
        }
    }

    protected boolean getScreenState(ComboPreferences pref) {
        boolean result = false;
        if (pref != null) {
            String str_on = getString(R.string.pref_entry_value_on);
            String str_val = pref.getString(
                    "pref_camera_video_full_screen_key", null);
            result = (str_val != null && str_val.equals(str_on));
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setTitle(getResources().getText(R.string.back_camera_title_text));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.back_camera_result);
        Intent intent = getIntent();
        mHandler = new Handler();
        currentTestcamera = intent.getStringExtra("fileName");
        mIsAuto = intent.getBooleanExtra("mIsAuto", false);
        TestResultUtil.getInstance().reset();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setTitle(R.string.camera_test_title);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mCameraScreenNailProxy = new CameraScreenNailProxy();
        mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);
        mTextureView = (TextureView) findViewById(R.id.surfaceView);
        mTextureView.setSurfaceTextureListener(this);
        mCountDownView = (TextView) findViewById(R.id.progressBar);
        mLightMsg = (TextView) findViewById(R.id.light_msg);
        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
        mFlashLightBtn = (Button) findViewById(R.id.camera_fashlight);
        mCountDownView.setVisibility(View.GONE);
        if (mIsFront)
            mFlashLightBtn.setVisibility(View.GONE);

        mPrefs = getSharedPreferences(SHARED_PREFS_APR_FILE,
                Context.MODE_PRIVATE);
        FLASH_MODE = mPrefs.getString(FLASH_MODE_NAME, FLASH_MODE_AUTO);

        if (FLASH_MODE.equals(FLASH_MODE_OFF)) {
            mFlashLightBtn
                    .setBackgroundResource(R.drawable.ic_flash_off_holo_light);
        } else if (FLASH_MODE.equals(FLASH_MODE_AUTO)) {
            mFlashLightBtn
                    .setBackgroundResource(R.drawable.ic_flash_auto_holo_light);
        } else if (FLASH_MODE.equals(FLASH_MODE_ON)) {
            mFlashLightBtn
                    .setBackgroundResource(R.drawable.ic_flash_on_holo_light);
        }
        mEditor = mPrefs.edit();

        mCountDownTimer = new CountDownTimer(6000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("yang", "millisUntil is" + millisUntilFinished);
                mCountDownView.setText(String
                        .valueOf((millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                mCountDownView.setText(String.valueOf(0));
                mCountDownView.setVisibility(View.GONE);
            }
        };
        Log.d(TAG, " second mIsAuto:" + mIsAuto);
        if (mIsAuto) {
            mHandler.postDelayed(mR, 2000);
            //mCountDownView.setVisibility(View.VISIBLE);
            mCountDownTimer.start();
        }
        mIsActivityInFront = true;
    }

    private void startCamera() {
        if (mFlag) {
            Log.e(TAG, "stop & close");
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mFlag = false;
                mCamera = null;
                Log.d(TAG, "mCamera stop and release");
            }
        }
        Log.d(TAG, "zhijie startCamera");
        try {
            Log.e(TAG, "open camera "+ mCameraId);
            mCamera = Camera.open(mCameraId);
        } catch (RuntimeException e) {
            Log.e(TAG, "fail to open camera");
            e.printStackTrace();
            mCamera = null;
            TestResultUtil.getInstance().setCurrentResult("fail");
            TestResultUtil.getInstance().setCurrentStepStatus("open camera fail");
            return;
        }
        if (mCamera == null) {
            Log.e(TAG, "mCamera == null");
            TestResultUtil.getInstance().setCurrentResult("fail");
            TestResultUtil.getInstance().setCurrentStepStatus("open camera fail");
            return;
        }
        if (mCamera != null) {
            setCameraDisplayOrientation(mCameraId, mCamera);
            if (!mIsFront) {
                if (setParameters() == false) {
                    mCamera.release();
                    mCamera = null;
                    TestResultUtil.getInstance().setCurrentResult("fail");
                    TestResultUtil.getInstance().setCurrentStepStatus("set camera Parameters fail");
                    return;
                }
                
            } else {
                setFrontParameters();
            }
            try {
                /* SPRD: fix bug349132 change the SurfaceView to TextureView @{ */
                // mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewTexture(mSurfaceTexture);
                /* @} */
                Log.e(TAG, "start preview");
                mCamera.startPreview();
                mFlag = true;
                initializeCameraOpenAfter();
            } catch (Exception e) {
                mCamera.release();
            }
        }
    }

    private void setFrontParameters() {
        Camera.Parameters parameters = null;
        parameters = mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.set("orientation", "portrait");
        parameters.setAutoWhiteBalanceLock(true);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    private boolean setParameters() {
        try {
            Camera.Parameters parameters = null;
            parameters = mCamera.getParameters();
            Size size = parameters.getPictureSize();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            List<Size> storesizes = parameters.getSupportedPictureSizes();
            for (int i = 0; i < storesizes.size(); i++) {
                Camera.Size camerasize = storesizes.get(i);
                Log.d(TAG, "Camera.Size: " + camerasize);
            }
            Size optimalSize = getOptimalPreviewSize(this, sizes,
                    (double) size.width / size.height);
            Size original = parameters.getPreviewSize();
            if (!original.equals(optimalSize)) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            }
            parameters.setAutoWhiteBalanceLock(true);
            Log.v(TAG, "Preview size is " + optimalSize.width + "x"
                    + optimalSize.height);
            mPreviewWidth = optimalSize.width;
            mPreviewHeight = optimalSize.height;
            parameters.setPictureFormat(PixelFormat.JPEG);
            Camera.Size camerasize = storesizes.get(0);
            Log.d(TAG, "camerasize.width: " + camerasize);
            parameters.setPictureSize(camerasize.width, camerasize.height);
            Log.d(TAG, "camerasize.width: " + camerasize.width
                    + "camerasize.height: " + camerasize.height);
            //parameters.set("orientation", "portrait");
            parameters.setWhiteBalance("auto");
            parameters.setRotation(90);
            parameters.setFlashMode(FLASH_MODE);
            parameters.setFocusMode("continuous-picture");
            mCamera.setParameters(parameters);
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "zhijie callball");

            String imageName = mFileName;
            // String imageName = mFileName.replaceAll(".jpeg", ".vga");
            String filePath = SLTConstant.SLT_SDCARD_PATH + imageName;
            if(Environment.getExternalStorageState(new File(SLTConstant.SLT_SD_PATH)).equals("mounted")) {
                filePath = SLTConstant.SLT_SDCARD_PATH + imageName;
            } else {
                filePath = SLTConstant.SLT_INTERNAL_PATH + imageName;
            }
            File pictureFile = new File(filePath);
            FileOutputStream fos = null;
            try {
                if (!pictureFile.getParentFile().exists()) {
                    pictureFile.getParentFile().mkdirs();
                }
                if (!pictureFile.exists()) {
                    pictureFile.createNewFile();
                }
                fos = new FileOutputStream(pictureFile);
                fos.write(data);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }finally{
                try {
                    if(fos != null){
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mCamera.startPreview();
        }
    };

    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
        }
    };

    // private PictureCallback rawCallback = new PictureCallback() {
    // public void onPictureTaken(byte[] _data, Camera _camera) {
    //
    // }
    // };

    public static Size getOptimalPreviewSize(Activity currentActivity,
            List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Display display = currentActivity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int targetHeight = Math.min(point.x, point.y);
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
                Log.d(TAG, "getOptimalPreviewSize minDiff="+minDiff);
                break;
            }
        }
        if (optimalSize == null) {
            Log.w(TAG, "No preview size match the aspect ratio");
            double minDiff2 = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff2) {
                    optimalSize = size;
                    minDiff2 = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void failureIntent() {
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void cameraNumber() {
        if (mCameraId == BACK_CAMERA) {
            mCameraId = FRONT_CAMERA;
            if (mLightMsg != null) {
                mLightMsg.setVisibility(View.GONE);
            }
        } else {
            mCameraId = BACK_CAMERA;
            if (mLightMsg != null && isSurportCameraFlash) {
                mLightMsg.setVisibility(View.VISIBLE);
            }
        }
        Log.d(TAG, "switched camera id = " + mCameraId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActivityInFront = false;
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

    public static int getDisplayRotation() {
        return mDisplayRotation;
    }

    public static void setCameraDisplayOrientation(int cameraId, Camera camera) {
        int result = getCameraDisplayOrientation(cameraId, camera);
        camera.setDisplayOrientation(result);
    }

    public static int getCameraDisplayOrientation(int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation();
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private void initializeCameraOpenAfter() {
        // SPRD:Fixbug454827,The preview picture of take photo has some
        // defective.
        Tuple<Integer, Integer> size = mCameraScreenNailProxy.getOptimalSize(
                CameraScreenNailProxy.KEY_SIZE_PREVIEW, mPreferences);
        if (mPreviewFrameLayout != null) {
            mPreviewFrameLayout.setAspectRatio((double) size.first
                    / (double) size.second, true);
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mR);
        mHandler.removeCallbacks(mR2);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        super.onDestroy();
    }
}
