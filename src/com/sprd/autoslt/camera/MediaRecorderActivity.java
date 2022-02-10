package com.sprd.autoslt.camera;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sprd.autoslt.R;
import com.sprd.autoslt.SltBaseActivity;
import com.sprd.autoslt.action.impl.VideoCameraAction;
import com.sprd.autoslt.camera.MovieRecorderView.CameraScreenNailProxy;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class MediaRecorderActivity extends SltBaseActivity implements
        SurfaceHolder.Callback, OnErrorListener {
    private static String TAG = "MediaRecorderActivity";
    private CameraScreenNailProxy mCameraScreenNailProxy;
    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder;
    private String mFileName = null;
    private String mTestcameraID = null;
    private static final int FRONT_CAMERA = 1;
    private static final int BACK_CAMERA = 0;
    private static final int BACK_SECOND_CAMERA = 2;
    private boolean isRecording = false;
    private MediaRecorder mRecorder;
    private Camera mCamera = null;
    private Camera.Size mSize = null;
    private int mCameraId = 0;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private SharedPreferences mPrefs;
    private Handler mHandler = null;
    private boolean isOpenCamera = false;
    private File mVecordFile = null;
    private VideoCameraAction videoCameraAction;
    public static MediaRecorderActivity instance;
    public static String FLASH_MODE = "off";
    private Runnable recordRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.d(TAG, "isOpenCamera = " +isOpenCamera);
            if (!isRecording && isOpenCamera) {
                startRecord();
            } else {
                stopRecord();
                recordError("start record fail ");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(TAG, "===== onCreate =====");
        setWindow();
        setContentView(R.layout.activity_media_recorder);
        initViews();
    }

    private void setWindow() {
        instance = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    private void initViews() {
        mPrefs = Util.getSharedPreferences(this);
        TestResultUtil.getInstance().reset();
        videoCameraAction = VideoCameraAction.getInstance(null, null);
        Intent intent = getIntent();
        mTestcameraID = intent.getStringExtra("cameraID");
        mFileName = Util.getSharedPreference(this, "cameraID",
                "slt_test_vedio.mp4");
        Log.d(TAG, "mTestcameraID =" + mTestcameraID + "; mFileName ="
                + mFileName);
        if (mTestcameraID.equals("front")) {
            mCameraId = FRONT_CAMERA;
            mFileName = VideoCameraAction.VIDEO_FRONT_PATH_NAME;
        } else if (mTestcameraID.equals("back")) {
            mCameraId = BACK_CAMERA;
            mFileName = VideoCameraAction.VIDEO_BACK_PATH_NAME;
        } else if (mTestcameraID.equals("backsecond")) {
            mCameraId = BACK_SECOND_CAMERA;
            mFileName = VideoCameraAction.VIDEO_BACK_SECOND_PATH_NAME;
        }
        SLTUtil.deleteFileIfExists(new File(SLTConstant.SLT_SDCARD_PATH+ mFileName));
        SLTUtil.deleteFileIfExists(new File(SLTConstant.SLT_INTERNAL_PATH + mFileName));
        SLTUtil.deleteFileIfExists(new File("/sdcard/slt/" + mFileName));
        mSurfaceView = (SurfaceView) findViewById(R.id.recorder_surfaceview);
        mHandler = new Handler();

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.setKeepScreenOn(true);
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHandler.postDelayed(recordRunnable, 1000);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "===== onResume =====");
//      initCamera();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "===== onPause =====");
        freeResource();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG, "===== onDestroy =====");
        freeResource();
        mHandler.removeCallbacks(recordRunnable);
        super.onDestroy();
    }

    int mPreviewSizeWidth = 480;
    int mPreviewSizeHeight = 320;
    int mVideoSizeWidth = 480;
    int mVideoSizeHeight = 320;
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
            try {
                Camera.Parameters parameters = mCamera.getParameters();

                //Parameters parameter = mCamera.getParameters();
                List<Camera.Size> prviewSizeList = parameters.getSupportedPreviewSizes();
                List<Camera.Size> videoSizeList = parameters.getSupportedVideoSizes();
                int index = Util.bestVideoSize(videoSizeList, prviewSizeList.get(0).width);
                Log.d(TAG, "index="+index);
                int mPreviewSizeWidth = prviewSizeList .get(0).width;
                int mPreviewSizeHeight = prviewSizeList .get(0).height;
                Log.d(TAG, "mPreviewSizeWidth="+mPreviewSizeWidth+",mPreviewSizeHeight="+mPreviewSizeHeight);
                mVideoSizeWidth = videoSizeList .get(index).width;
                mVideoSizeHeight = videoSizeList .get(index).height;
                Log.d(TAG, "mVideoSizeWidth="+mVideoSizeWidth+",mVideoSizeHeight="+mVideoSizeHeight);

                Size size = parameters.getPictureSize();
                List<Size> sizes = parameters.getSupportedPreviewSizes();
                Size optimalSize = getOptimalPreviewSize(this,sizes,
                        (double) size.width / size.height);
                Size original = parameters.getPreviewSize();
                if (!original.equals(optimalSize)) {
                    parameters.setPreviewSize(optimalSize.width,
                            optimalSize.height);
                }
                Log.v(TAG, "Preview size is " + optimalSize.width + "x"
                        + optimalSize.height);
                mPreviewWidth = optimalSize.width;
                mPreviewHeight = optimalSize.height;
                parameters.set("orientation", "portrait");
                parameters.setFlashMode(FLASH_MODE);
                mCamera.setParameters(parameters);

                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                isOpenCamera = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                freeResource();
                recordError("init Camera fail");
                e.printStackTrace();
            }
        }
    }

    public void freeResource() {
        if (mRecorder != null) {
            mRecorder.setOnErrorListener(null);
            mRecorder.release();
            mRecorder = null;
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
        isOpenCamera = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.d(TAG, "===== surfaceCreated =====");
        mSurfaceHolder = holder;
//      if (!isOpenCamera) {
            initCamera();
//      }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub
        Log.d(TAG, "===== surfaceChanged =====");
        mSurfaceHolder = holder;

        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            freeResource();
            recordError("surfaceChanged error");
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.d(TAG, "===== surfaceDestroyed =====");
        if (isRecording && mCamera != null) {
            mCamera.lock();
        }
        if (mSurfaceHolder != null) {
            mSurfaceHolder = null;
        }
        if (mSurfaceView != null) {
            mSurfaceView = null;
        }
//      if (isOpenCamera) {
            freeResource();
//      }
    }
 /*   private void initializeCameraOpenAfter() {
        // The preview picture of take photo has some defective.
        Tuple<Integer, Integer> size = mCameraScreenNailProxy.getOptimalSize(
                CameraScreenNailProxy.KEY_SIZE_PREVIEW, mPreferences);
        if (mPreviewFrameLayout != null) {
            mPreviewFrameLayout.setAspectRatio((double) size.first
                    / (double) size.second, true);
        }
    }
*/
    private void createRecordDir() {
        //String filePath = SLTConstant.SLT_SDCARD_PATH + mFileName;
        String filePath = "/sdcard/slt/" +mFileName;
        /*if (Environment.getExternalStorageState(
                new File(SLTConstant.SLT_SD_PATH)).equals("mounted")) {
            TestResultUtil.getInstance().setCurrentStepName(SLTConstant.SLT_SDCARD_PATH + mFileName);
            filePath = SLTConstant.SLT_SDCARD_PATH + mFileName;
        } else {
            TestResultUtil.getInstance().setCurrentStepName(SLTConstant.SLT_INTERNAL_PATH + mFileName);
            filePath = SLTConstant.SLT_INTERNAL_PATH + mFileName;
        }*/
        File vecordFile = new File(filePath);
        TestResultUtil.getInstance().setCurrentStepName(filePath);

        try {
            if (!vecordFile.getParentFile().exists()) {
                vecordFile.getParentFile().mkdirs();
            }
            if (!vecordFile.exists()) {
                vecordFile.createNewFile();
            }
            mVecordFile = vecordFile;
        } catch (IOException e) {
            recordError("create Record file error");
            e.printStackTrace();
        }

        Log.d(TAG, "start file path : " + filePath);

    }

    private void startRecord() {
        createRecordDir();

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.unlock();
            mRecorder.setCamera(mCamera);
        }
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setVideoEncodingBitRate(1024 * 1024);
            mRecorder.setVideoFrameRate(30);
            mSurfaceHolder.setFixedSize(320, 240);
            Log.d(TAG, "mVideoSizeWidth =" + mVideoSizeWidth + ",mVideoSizeHeight="+mVideoSizeHeight);
            mRecorder.setVideoSize(mVideoSizeWidth, mVideoSizeHeight);
            mRecorder.setMaxDuration(60 * 1000);
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mRecorder.setOnErrorListener(this);
            //String path = getExternalCacheDir().getPath();
            if (mVecordFile != null) {
                mRecorder.setOutputFile(mVecordFile.getAbsolutePath());
                mRecorder.prepare();
                mRecorder.start();
                isRecording = true;
            }
            Log.d(TAG, "Media Recorder start ok ,so send Message testok");
            videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTOK);
        } catch (Exception e) {
            recordError("start Record error");
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        Log.d(TAG, "stopRecord");
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRecording = false;
    }

    private void recordError(String err){
        stopRecord();
        freeResource();
        TestResultUtil.getInstance().setCurrentStepStatus(err);
        videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTFAIL);
    }

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

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        // TODO Auto-generated method stub
        if (mr != null) {
            mr.reset();
        }
    }

/*  protected class CameraScreenNailProxy {
        private static final String TAG = "CameraScreenNailProxy";

        public static final int KEY_SIZE_PICTURE = 0;
        public static final int KEY_SIZE_PREVIEW = 1;

        private Tuple<Integer, Integer> mScreenSize;

        protected CameraScreenNailProxy() {
            initializeScreenSize();
        }

        private void initializeScreenSize() {

            DisplayMetrics metrics = new DisplayMetrics();
            metrics.widthPixels = mPrefs.getInt("metrics_widthPixels", 720);
            metrics.heightPixels = mPrefs.getInt("metrics_heightPixels", 1280);
            Log.d("yang", "metrics.widthPixels :" + metrics.widthPixels
                    + ",metrics.heightPixels :" + metrics.heightPixels);
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
            return CameraTestActivity.getCameraDisplayOrientation(mCameraId,
                    mCamera);
        }
    }

    protected boolean getScreenState(ComboPreferences pref) {
        boolean result = false;
        if (pref != null) {
            String str_on = "On";
            String str_val = pref.getString(
                    "pref_camera_video_full_screen_key", null);
            result = (str_val != null && str_val.equals(str_on));
        }
        return result;
    }*/
}
