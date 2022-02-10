package com.sprd.autoslt.action.impl;

import java.io.File;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.camera.MediaRecorderActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

public class VideoCameraAction extends AbstractAction {

    private static final String TAG = "SLTVideoCameraAction";
    public static final String VIDEO_FRONT_PATH_NAME = "cameravideo_front.mp4";
    public static final String VIDEO_BACK_PATH_NAME = "cameravideo_back.mp4";
    public static final String VIDEO_BACK_SECOND_PATH_NAME ="cameravideo_backse.mp4";
    public static final int MSG_VIDESTART_TESTOK = 1001;
    public static final int MSG_VIDESTART_TESTFAIL = 1002;
    public static final int MSG_STOP_TEST = 1003;

    private static String mType;
    private static VideoCameraAction instance;
    public Handler videoHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_VIDESTART_TESTOK:
				Log.d(TAG, "MSG_VIDESTART_TESTOK ");
				ok();
				break;
			case MSG_VIDESTART_TESTFAIL:
				error(TestResultUtil.getInstance().getCurrentStepStatus());
				TestResultUtil.getInstance().reset();
				break;
			case MSG_STOP_TEST :
				MediaRecorderActivity.instance.finish();
				break;
			default:
				break;
			}
		}
    	
    };

    public VideoCameraAction(StatusChangedListener listener) {
        super(listener);
    }

    public VideoCameraAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        TestResultUtil.getInstance().reset();
    }

    public static VideoCameraAction getInstance(StatusChangedListener listener, String type) {
        mType = type;
        if(instance == null) {
            instance = new VideoCameraAction(listener, type);
        }
        return instance;
    }

    @Override
    public void start(String fileName) {
        if (TextUtils.isEmpty(fileName) && !SLTConstant.ACTION_TYPE_END_CAMERA_VIDEO.equals(mType)) {
            error("CameraAction start() failed : file name is empty!");
            return;
        }
        if (SLTConstant.ACTION_TYPE_START_CAMERA_VIDEO.equals(mType)) {
        	Log.d(TAG, "start camera video");
        	if (SLTUtil.getTopActivity(mContext).toString().contains(
                        "com.sprd.autoslt.camera.MediaRecorderActivity")) {
				ok();
				return;
			}
			Intent intent = new Intent(mContext,MediaRecorderActivity.class);
			intent.putExtra("cameraID", fileName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}else if (SLTConstant.ACTION_TYPE_END_CAMERA_VIDEO.equals(mType)) {
			if (!SLTUtil.getTopActivity(mContext).toString().contains(
                        "com.sprd.autoslt.camera.MediaRecorderActivity")) {
				ok();
				return;
			}
			Log.d(TAG, "end camera video");
			MediaRecorderActivity.instance.stopRecord();
			MediaRecorderActivity.instance.freeResource();
			MediaRecorderActivity.instance.finish();
			if (TestResultUtil.getInstance().getCurrentStepName() == null 
					|| TestResultUtil.getInstance().getCurrentStepName().equals("")) {
				TestResultUtil.getInstance().setCurrentStepStatus("record file error");
				videoHandler.sendEmptyMessage(MSG_VIDESTART_TESTFAIL);
			}else {
				String recordPath = TestResultUtil.getInstance().getCurrentStepName();
				if (recordPath == null || recordPath.equals("")) {
					TestResultUtil.getInstance().setCurrentStepStatus("record file error");
					videoHandler.sendEmptyMessage(MSG_VIDESTART_TESTFAIL);
					return;
				}
				File recordFile = new File(recordPath);
				if (!recordFile.getParentFile().exists()) {
					TestResultUtil.getInstance().setCurrentStepStatus("record file error");
					videoHandler.sendEmptyMessage(MSG_VIDESTART_TESTFAIL);
					return;
				}
				if (!recordFile.exists()) {
					TestResultUtil.getInstance().setCurrentStepStatus("record file error");
					videoHandler.sendEmptyMessage(MSG_VIDESTART_TESTFAIL);
					return;
				}else {
					end(recordPath);
				}
			}
		}
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop...");
    }

}
