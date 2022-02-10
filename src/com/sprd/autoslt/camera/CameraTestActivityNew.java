package com.sprd.autoslt.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.sprd.autoslt.R;
import com.sprd.autoslt.action.impl.CameraAction;
import com.sprd.autoslt.action.impl.DualCameraCheckAction;
import com.sprd.autoslt.action.impl.VideoCameraAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

import android.media.MediaActionSound;
import android.os.HandlerThread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.text.TextUtils;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class CameraTestActivityNew extends Activity implements
        SurfaceHolder.Callback {
    private static String TAG = "CameraTestActivityNew";
    public static CameraTestActivityNew instance;
    private static final int MSG_DO_AUTO_FOCUS = 1313;
    private static final int FRONT_CAMERA = 1;
    private static final int BACK_CAMERA = 0;
    private static final int BACK_SECOND_CAMERA = 2;
    private Handler mHandler = null;
    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder;
    private String mPictureName = null;
    private String mTestcameraID = null;
    private String mFocusParam = null;
    private int mCameraId = 0;
    private SharedPreferences mPrefs;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private static final String SHARED_PREFS_APR_FILE = "autoslt";
    private static String FLASH_MODE = "off";
    private Camera mCamera = null;
    private Camera.Size mSize = null;
    private boolean isOpenCamera = false;
    private boolean isTakePicture = false;
    private CameraAction mCameraAction = null;
    public static boolean mIsActivityInFront = false;
    private volatile boolean mAutoFocusSuccess = false;
    private static int mDisplayRotation = 0;
    private int mCount = 0;
    private boolean mNoFocusFlag = false;

    private Handler mWorkHandler;
    private HandlerThread mBackgroundThread;
    private CameraScreenNailProxy mCameraScreenNailProxy;
    private PreviewFrameLayout mPreviewFrameLayout;
    private ComboPreferences mPreferences;

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackgroundNew");
        mBackgroundThread.start();
        mWorkHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mWorkHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Runnable shotRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (mCamera != null) {
                try {
                    Log.d(TAG, "take Picture");
                    if (mNoFocusFlag) {
                        Log.w(TAG, "takePicture and nofocus...");
                        mCamera.setOneShotPreviewCallback(null);
                        mCamera.takePicture(shutterCallback, null,
                                pictureCallback);
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run, autoFocus...");
                                mCount = 0;
                                while (!mAutoFocusSuccess && mCount < 200) {
                                    try {
                                        Thread.sleep(15L);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mCount++;
                                }
                                if (mCount == 200) {
                                    Log.w(TAG,
                                            "takePicture and cancelAutoFocus...");
                                    mCamera.setOneShotPreviewCallback(null);
                                    mCamera.takePicture(shutterCallback, null,
                                            pictureCallback);
                                    mCamera.cancelAutoFocus();
                                    mAutoFocusSuccess = true;
                                }
                            }
                        }).start();
                        mCamera.autoFocus(focusCallback);
                        // mCamera.takePicture(shutterCallback, null,
                        // pictureCallback);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    recordError("camera takepicture exception");
                    e.printStackTrace();
                    return;
                }
                // mHandler.postDelayed(saveRunnable, 2000);
                // mWorkHandler.postDelayed(saveRunnable, 3000);
            } else {
                recordError("camera is null");
            }
        }
    };

    private Runnable saveRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            /*
             * if (Environment.getExternalStorageState( new
             * File(SLTConstant.SLT_SD_PATH)).equals("mounted")) {
             * TestResultUtil.getInstance().setCurrentStepName(
             * SLTConstant.SLT_SDCARD_PATH + mPictureName); } else {
             * TestResultUtil.getInstance().setCurrentStepName(
             * SLTConstant.SLT_INTERNAL_PATH + mPictureName); }
             */
            TestResultUtil.getInstance().setCurrentStepName(
                    "/sdcard/slt/" + mPictureName);
            if (isTakePicture) {
                Log.d(TAG, "take Picture ok");
                TestResultUtil.getInstance().setCurrentResult("ok");
                finish();
            } else {
                Log.d(TAG, "take Picture fail");
                TestResultUtil.getInstance().setCurrentResult("fail");
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setWindow();
        setContentView(R.layout.activity_camera_shot);
        initViews();
        // startBackgroundThread();
    }

    private void setWindow() {
        instance = this;
        mCameraAction = new CameraAction(null, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    private void initViews() {
        mPrefs = getSharedPreferences(SHARED_PREFS_APR_FILE,
                Context.MODE_PRIVATE);
        TestResultUtil.getInstance().reset();
        Intent intent = getIntent();
        mTestcameraID = intent.getStringExtra("cameraID");
        mFocusParam = intent.getStringExtra("focusParam");
        mPictureName = "camerashot_" + mTestcameraID + ".jpg";
        Log.d(TAG, "mTestcameraID =" + mTestcameraID + "; mFileName ="
                + mPictureName);
        if (mTestcameraID.equals("front")) {
            mCameraId = FRONT_CAMERA;
        } else if (mTestcameraID.equals("back")) {
            mCameraId = BACK_CAMERA;
        } else if (mTestcameraID.equals("backsecond")) {
            mCameraId = BACK_SECOND_CAMERA;
        }

        if (!TextUtils.isEmpty(mFocusParam) && mFocusParam.equals("nofocus")) {
            mNoFocusFlag = true;
        } else {
            mNoFocusFlag = false;
        }
        /*
         * SLTUtil.deleteFileIfExists(new File(SLTConstant.SLT_SDCARD_PATH +
         * mPictureName)); SLTUtil.deleteFileIfExists(new
         * File(SLTConstant.SLT_INTERNAL_PATH + mPictureName));
         */
        SLTUtil.deleteFileIfExists(new File("/sdcard/slt/" + mPictureName));
        mCameraScreenNailProxy = new CameraScreenNailProxy();
        mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
        mHandler = new Handler();

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.setKeepScreenOn(true);
        holder.addCallback(this);

        mHandler.postDelayed(shotRunnable, 1000);
        mIsActivityInFront = true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "onResume");
        // initCamera();
    }

    public void initCamera() {
        if (mCamera != null) {
            freeResource();
        }
        try {
            mCamera = Camera.open(mCameraId);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            recordError("open Camera fail");
            e1.printStackTrace();
            return;
        }
        if (mCamera != null) {
            setCameraDisplayOrientation(mCameraId, mCamera);
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
                    parameters.setPreviewSize(optimalSize.width,
                            optimalSize.height);
                }
                // parameters.setAutoWhiteBalanceLock(true);
                // parameters.setAutoExposureLock(true);
                // parameters.setExposureCompensation(0);
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
                // parameters.set("orientation", "portrait");
                // parameters.setWhiteBalance("auto");
                if (mCameraId == FRONT_CAMERA) {
                    parameters.set("orientation", "portrait");
                    parameters.setRotation(270);
                } else {
                    parameters.setFlashMode(FLASH_MODE);
                    parameters.setRotation(90);
                }
                /*
                 * parameters.setRotation(90);
                 * parameters.setFlashMode(FLASH_MODE);
                 */
                // parameters.setFocusMode("continuous-picture");

                parameters.set("iso", "auto");
                parameters.set("metering-mode", "center-weighted");
                mCamera.setParameters(parameters);
                if (mCameraId == FRONT_CAMERA) {
                    mCamera.setDisplayOrientation(90);
                }
                isOpenCamera = true;
                mCamera.startPreview();
                initializeCameraOpenAfter();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                recordError("init back Camera fail");
                e.printStackTrace();
            }
            // }
        }
    }

    private void initializeCameraOpenAfter() {
        Tuple<Integer, Integer> size = mCameraScreenNailProxy.getOptimalSize(
                CameraScreenNailProxy.KEY_SIZE_PREVIEW, mPreferences);
        if (mPreviewFrameLayout != null) {
            mPreviewFrameLayout.setAspectRatio((double) size.first
                    / (double) size.second, true);
        }
    }

    private void setFrontParameters() {
        Camera.Parameters parameters = null;
        parameters = mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.set("orientation", "portrait");
        // parameters.setAutoWhiteBalanceLock(true);
        // parameters.setAutoExposureLock(true);
        // parameters.setExposureCompensation(0);
        parameters.set("iso", "auto");
        parameters.set("metering-mode", "center-weighted");
        mCamera.setParameters(parameters);

        isOpenCamera = true;
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

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // mIsActivityInFront = false;
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onDestroy");
        mIsActivityInFront = false;
        mHandler.removeCallbacks(saveRunnable);
        mHandler.removeCallbacks(shotRunnable);
        // mWorkHandler.removeCallbacks(saveRunnable);
        // stopBackgroundThread();
        super.onDestroy();
    }

    private PictureCallback pictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken callback");

            String imageName = mPictureName;
            String filePath = "/sdcard/slt/" + imageName;
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
                fos.flush();
                isTakePicture = true;
                Log.d(TAG, "onPictureTaken write data");
            } catch (FileNotFoundException f_e) {
                isTakePicture = false;
                Log.d(TAG, "File not found: " + f_e.getMessage());
                f_e.printStackTrace();
            } catch (IOException e) {
                isTakePicture = false;
                Log.d(TAG, "Error accessing file: " + e.getMessage());
                e.printStackTrace();
            }finally{
                try {
                    if(fos != null){
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mHandler.postDelayed(saveRunnable, 500);

        }
    };

    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
        }
    };

    private AutoFocusCallback focusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            Log.w(TAG, "onAutoFocus success : " + success);
            // if (success) {
            if (mAutoFocusSuccess) {
                return;
            }
            Log.w(TAG, "onAutoFocus...");
            mCamera.setOneShotPreviewCallback(null);
            mCamera.takePicture(shutterCallback, null, pictureCallback);
            mCamera.cancelAutoFocus();
            mAutoFocusSuccess = true;
            // }else {
            // mCameraAutoFocusHandler.sendEmptyMessageDelayed(MSG_DO_AUTO_FOCUS,
            // 500);
            // }
        }
    };

    private final Handler mCameraAutoFocusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_DO_AUTO_FOCUS:
                if (mCamera != null) {
                    mCamera.autoFocus(focusCallback);
                }
                break;
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mSurfaceHolder = holder;
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub
        mSurfaceHolder = holder;

        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            // if (mCameraId == FRONT_CAMERA) {
            // mCamera.autoFocus(focusCallback);
            // }
        } catch (Exception e) {
            e.printStackTrace();
            freeResource();
            recordError("surfaceChanged error");
            finish();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

        // TODO Auto-generated method stub
        if (mCamera != null) {
            mCamera.lock();
        }
        if (mSurfaceHolder != null) {
            mSurfaceHolder = null;
        }
        if (mSurfaceView != null) {
            mSurfaceView = null;
        }
        freeResource();

    }

    public void freeResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            // if (mCameraId == FRONT_CAMERA) {
            mCamera.cancelAutoFocus();
            // }
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    private void recordError(String err) {
        freeResource();
        TestResultUtil.getInstance().setCurrentStepStatus(err);
        TestResultUtil.getInstance().setCurrentResult("fail");
        finish();
    }

    public static Size getOptimalPreviewSize(Activity currentActivity,
            List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null)
            return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Display display = currentActivity.getWindowManager()
                .getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int targetHeight = Math.min(point.x, point.y);
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
                Log.d(TAG, "getOptimalPreviewSize minDiff=" + minDiff);
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
                    Log.d(TAG, "getOptimalPreviewSize minDiff2=" + minDiff2);
                }
            }
        }
        return optimalSize;
    }

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

}
