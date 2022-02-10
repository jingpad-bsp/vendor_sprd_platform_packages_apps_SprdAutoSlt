package com.sprd.autoslt.action.impl;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.camera.CameraVerificationActivity;
import com.sprd.autoslt.camera.FrontCameraVerificationActivity;
import com.sprd.autoslt.util.TestResultUtil;

public class DualCameraCheckAction extends AbstractAction {
	private static final String TAG = "DualCameraCheckAction";
    private static DualCameraCheckAction instance;
    public static final int MSG_TESTOK = 9101;
    public static final int MSG_TESTFAIL = 9102;
	public static final int MSG_TESTERROR = 9103;

	public DualCameraCheckAction(StatusChangedListener listener) {
		super(listener);
		// TODO Auto-generated constructor stub
		TestResultUtil.getInstance().reset();
	}

	public static DualCameraCheckAction getInstance(StatusChangedListener listener) {       
        if(instance == null) {
            instance = new DualCameraCheckAction(listener);
        }
        return instance;
    }

	
	public Handler dualCameraCheckhandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_TESTOK:
				Log.d(TAG, "MSG_TESTOK ");
				end(TestResultUtil.getInstance().getCurrentStepStatus());
				TestResultUtil.getInstance().reset();
				break;
			case MSG_TESTFAIL:
			    end(TestResultUtil.getInstance().getCurrentStepStatus());
			    //error(TestResultUtil.getInstance().getCurrentStepStatus());
				TestResultUtil.getInstance().reset();
				break;
			case MSG_TESTERROR:
				error("fail");
				TestResultUtil.getInstance().reset();
				break;
			default:
				break;
			}
		}
    	
    };
	@Override
	public void start(String param) {
		// TODO Auto-generated method stub
		if (TextUtils.isEmpty(param)) {
            Log.e(TAG, "start() failed :  name is empty!");
            error("DualCameraCheckAction start() failed : file name is empty!");
            return;
        }
		if (("front").equals(param)) {
			Intent intent = new Intent(mContext,FrontCameraVerificationActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}else if (("back").equals(param)) {
			Intent intent = new Intent(mContext,CameraVerificationActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}else {
			error("status error");
		}

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

}
