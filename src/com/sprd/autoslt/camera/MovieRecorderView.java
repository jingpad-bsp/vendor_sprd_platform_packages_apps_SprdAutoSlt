package com.sprd.autoslt.camera;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import com.sprd.autoslt.R;
import android.os.SystemClock;

import com.sprd.autoslt.action.impl.VideoCameraAction;
import com.sprd.autoslt.camera.ComboPreferences;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.camera.CameraTestActivity;
import com.sprd.autoslt.camera.Util;
import com.sprd.autoslt.camera.PreviewFrameLayout;
import com.sprd.autoslt.util.TestResultUtil;

public class MovieRecorderView extends LinearLayout implements OnErrorListener {

    private static final String TAG = "MovieRecorderView";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Chronometer mChronometer;
    private CameraScreenNailProxy mCameraScreenNailProxy;
    private ComboPreferences mPreferences;

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private Timer mTimer;
    private OnRecordFinishListener mOnRecordFinishListener;

    private int mWidth;
    private int mHeight;
    private boolean isOpenCamera;
    private boolean iscameraOk = true;
    private boolean isMediaRecorderOk = true;
    private boolean mOpenCameraAgain = false;
    private int mRecordMaxTime;
    private int mTimeCount;
    private File mVecordFile = null;
    private String mFileName = null;
    private Handler mHanlder = null;
    private Context mContext;
    private int mCameraId = 0;
    private PreviewFrameLayout mPreviewFrameLayout;
    private SharedPreferences mPrefs;
    private LinearLayout mContainer = null;
    private static final int FRONT_CAMERA = 1;
    private static final int BACK_CAMERA = 0;
    private static final int BACK_SECOND_CAMERA =2;
    private VideoCameraAction videoCameraAction;

    private Runnable mCallChronometer = new Runnable() {
        public void run() {
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
        }
    };

    public MovieRecorderView(Context context) {
        this(context, null);
    }

    public MovieRecorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MovieRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mPrefs = Util.getSharedPreferences(context);
        mWidth = 320;
        mHeight = 240;
        isOpenCamera = true;
        mRecordMaxTime = 600;

        mCameraScreenNailProxy = new CameraScreenNailProxy();
        LayoutInflater.from(context)
                .inflate(R.layout.moive_recorder_view, this);
        mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);
        mContainer = (LinearLayout) findViewById(R.id.chronometer_container);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new CustomCallBack());
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHanlder = new Handler();
        videoCameraAction = VideoCameraAction.getInstance(null, null);
        TestResultUtil.getInstance().reset();
    }

    private class CustomCallBack implements Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            freeCameraResource();
        }

    }

    public void initCamera() {
        if (mCamera != null) {
            freeCameraResource();
        }
        mFileName = Util.getSharedPreference(mContext, "mFileName","slt_test_vedio.mp4");
        if(mFileName.equals("front")) {
            mCameraId = FRONT_CAMERA;
        } else if(mFileName.equals("back")) {
            mCameraId = BACK_CAMERA;
        }else if (mFileName.equals("backsecond")) {
            mCameraId = BACK_SECOND_CAMERA;
        }
        try {
            Log.d(TAG, "open camera " + mCameraId);
            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            e.printStackTrace();
            freeCameraResource();
            TestResultUtil.getInstance().setCurrentStepStatus("open camera fail");
            iscameraOk = false;
            return;
        }
        if (mCamera == null) {
            iscameraOk = false;
            return ;
        }

        try {
            setCameraParams();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            initializeCameraOpenAfter();
            mCamera.unlock();
        } catch (Exception e) {
            mCamera.release();
            TestResultUtil.getInstance().setCurrentStepStatus("set camera params fail");
            iscameraOk = false;

        }
    }

    /**
     * Setting the camera to portrait
     */
    private void setCameraParams() {
        if (mCamera != null) {

            Camera.Parameters parameters = null;
            parameters = mCamera.getParameters();
            Size size = parameters.getPictureSize();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            Size optimalSize = getOptimalPreviewSize(sizes, (double) size.width
                    / size.height);
            Size original = parameters.getPreviewSize();
            if (optimalSize != null && !original.equals(optimalSize)) {
                Log.v(TAG, "Preview size is " + optimalSize.width + "x"
                        + optimalSize.height);
                parameters
                        .setPreviewSize(optimalSize.width, optimalSize.height);
            }
            parameters.set("orientation", "portrait");
            parameters.setFlashMode(VideoCameraActivity.FLASH_MODE);
            mCamera.setParameters(parameters);
        }
    }

    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    private void createRecordDir() {

        mFileName = Util.getSharedPreference(mContext, "mFileName",
                "slt_test_vedio.mp4");
        if(mFileName.equals("front")) {
            mCameraId = FRONT_CAMERA;
            mFileName = VideoCameraAction.VIDEO_FRONT_PATH_NAME;
        } else if(mFileName.equals("back")) {
            mCameraId = BACK_CAMERA;
            mFileName = VideoCameraAction.VIDEO_BACK_PATH_NAME;
        }else if(mFileName.equals("backsecond")) {
            mCameraId = BACK_SECOND_CAMERA;
            mFileName = VideoCameraAction.VIDEO_BACK_SECOND_PATH_NAME;
        }
        String filePath = SLTConstant.SLT_SDCARD_PATH + mFileName;
        if(Environment.getExternalStorageState(new File(SLTConstant.SLT_SD_PATH)).equals("mounted")) {
            TestResultUtil.getInstance().setCurrentStepName(SLTConstant.SLT_SDCARD_PATH + mFileName);
            filePath = SLTConstant.SLT_SDCARD_PATH + mFileName;
        } else {
            TestResultUtil.getInstance().setCurrentStepName(SLTConstant.SLT_INTERNAL_PATH + mFileName);
            filePath = SLTConstant.SLT_INTERNAL_PATH + mFileName;
        }
        File vecordFile = new File(filePath);

        try {
            if (!vecordFile.getParentFile().exists()) {
                vecordFile.getParentFile().mkdirs();
            }
            if (!vecordFile.exists()) {
                vecordFile.createNewFile();
            }
            mVecordFile = vecordFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "start file path : " + filePath);

    }

    private void initRecord() throws IOException {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();

        Parameters parameter = mCamera.getParameters();
        List<Camera.Size> prviewSizeList = parameter.getSupportedPreviewSizes();
        List<Camera.Size> videoSizeList = parameter.getSupportedVideoSizes();
        int index = Util.bestVideoSize(videoSizeList, prviewSizeList.get(0).width);
        Log.d(TAG, "index="+index);
        int mPreviewSizeWidth = prviewSizeList .get(0).width;
        int mPreviewSizeHeight = prviewSizeList .get(0).height;
        Log.d(TAG, "mPreviewSizeWidth="+mPreviewSizeWidth+",mPreviewSizeHeight="+mPreviewSizeHeight);
        int mVideoSizeWidth = videoSizeList .get(index).width;
        int mVideoSizeHeight = videoSizeList .get(index).height;
        Log.d(TAG, "mVideoSizeWidth="+mVideoSizeWidth+",mVideoSizeHeight="+mVideoSizeHeight);
        mWidth = mVideoSizeWidth;
        mHeight = mVideoSizeHeight;
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setVideoSource(VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(AudioSource.MIC);
        mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoSize(mWidth, mHeight);
        mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 512);
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setVideoEncoder(VideoEncoder.MPEG_4_SP);
        // mediaRecorder.setMaxDuration(Constant.MAXVEDIOTIME * 1000);
        if (mVecordFile != null) {
            mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
        }
        mMediaRecorder.prepare();
        try {
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            stopRecord();
            releaseRecord();
            freeCameraResource();
            TestResultUtil.getInstance().setCurrentStepStatus("fail");
            videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTFAIL);
            isMediaRecorderOk = false;
            return;
        } catch (RuntimeException e) {
            e.printStackTrace();
            stopRecord();
            releaseRecord();
            freeCameraResource();
            TestResultUtil.getInstance().setCurrentStepStatus("fail");
            videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTFAIL);
            isMediaRecorderOk = false;
            return;
        } catch (Exception e) {
            e.printStackTrace();
            stopRecord();
            releaseRecord();
            freeCameraResource();
            TestResultUtil.getInstance().setCurrentStepStatus("fail");
            videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTFAIL);
            isMediaRecorderOk = false;
            return;
        }
        videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTOK);
    }

    public void record(final OnRecordFinishListener onRecordFinishListener) {
        this.mOnRecordFinishListener = onRecordFinishListener;
        createRecordDir();
        try {
            if (!isOpenCamera || mOpenCameraAgain)
                initCamera();
            if (mCamera == null || iscameraOk == false) {
                Log.d(TAG, "mCamera =  null || iscameraOk == false");
               TestResultUtil.getInstance().setCurrentStepStatus("fail");
               videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTFAIL);
               videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_STOP_TEST);
               return;
            }
            mChronometer.setBase(SystemClock.elapsedRealtime());
            initRecord();
            if (isMediaRecorderOk == false) {
               TestResultUtil.getInstance().setCurrentStepStatus("fail");
               videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTFAIL);
               videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_STOP_TEST);
               return;
            }
            mTimeCount = 0;
            mContainer.setVisibility(View.VISIBLE);
            mChronometer.start();
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mTimeCount++;
                    if (mTimeCount == mRecordMaxTime) {// Set maximum video
                                                       // recording of one hour
                                                       // test
                        stop();
                        if (mOnRecordFinishListener != null)
                            mOnRecordFinishListener.onRecordFinish();
                    }
                }
            }, 0, 1000);
        } catch (IOException e) {
            e.printStackTrace();
            stop();
            TestResultUtil.getInstance().setCurrentStepStatus("fail");
            videoCameraAction.videoHandler.sendEmptyMessage(VideoCameraAction.MSG_VIDESTART_TESTFAIL);
        }
    }

    public void stop() {
        mContainer.setVisibility(View.GONE);
        stopRecord();
        releaseRecord();
        freeCameraResource();
    }

    public void stopRecording() {
        stopRecord();
        releaseRecord();
        mOpenCameraAgain = true;
    }

    public void stopRecord() {
        mHanlder.post(mCallChronometer);

        if (mTimer != null)
            mTimer.cancel();
        if (mMediaRecorder != null) {
            // catch Error.
//            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * release resource.
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    public int getTimeCount() {
        return mTimeCount;
    }

    /**
     * @return the mVecordFile
     */
    public File getmVecordFile() {
        return mVecordFile;
    }

    public interface OnRecordFinishListener {
        public void onRecordFinish();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Size getOptimalPreviewSize(List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null)
            return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Point point = new Point();

        point.x = mPrefs.getInt("point_x", 720);
        point.y = mPrefs.getInt("point_y", 1280);
        Log.d(TAG, "point.x :" + point.x + ",point.y :" + point.y);
        int targetHeight = Math.min(point.x, point.y);
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
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

    private void initializeCameraOpenAfter() {
        // The preview picture of take photo has some defective.
        mPreferences = new ComboPreferences(mContext);
        Tuple<Integer, Integer> size = mCameraScreenNailProxy.getOptimalSize(
                CameraScreenNailProxy.KEY_SIZE_PREVIEW, mPreferences);
        if (mPreviewFrameLayout != null) {
            mPreviewFrameLayout.setAspectRatio((double) size.first
                    / (double) size.second, true);
        }
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
    }

}
