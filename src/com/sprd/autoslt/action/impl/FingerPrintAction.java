package com.sprd.autoslt.action.impl;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.action.AbstractAction.StatusChangedListener;
import com.sprd.autoslt.fingerprint.FingerprintTestActivity;
import com.sprd.autoslt.util.TestResultUtil;

public class FingerPrintAction extends AbstractAction{
	private static final String TAG = "FingerPrintAction";
    private static FingerPrintAction instance;
    public static final int FINGERPRINT_CALIBRATION_FAIL = 8001;
    public static final int FINGERPRINT_TEST_SUCCESS = 8002;
    public static final int FINGERPRINT_TEST_FAIL = 8003;
	public static final int FINGERPRINT_TEST_ERROR = 8004;
	public FingerPrintAction(StatusChangedListener listener) {
		super(listener);
		// TODO Auto-generated constructor stub
		TestResultUtil.getInstance().reset();
	}
	
	public static FingerPrintAction getInstance(StatusChangedListener listener){
		 if(instance == null) {
	            instance = new FingerPrintAction(listener);
	        }
	        return instance;
	}

	public Handler fingerPrintHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case FINGERPRINT_TEST_SUCCESS:
				Log.d(TAG, "MSG_TESTOK ");				
				end("pass");
				break;
			case FINGERPRINT_TEST_FAIL:
				Log.d(TAG, "MSG_TESTFAIL ");
				end("fail");
				break;
			case FINGERPRINT_TEST_ERROR:
				Log.d(TAG, "FINGERPRINT_TEST_ERROR ");
				error("unsupport fingerprint");
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	public void start(String param) {
		// TODO Auto-generated method stub				
		Intent fingerPreintIntent = new Intent(mContext,FingerprintTestActivity.class );
		fingerPreintIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(fingerPreintIntent);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
