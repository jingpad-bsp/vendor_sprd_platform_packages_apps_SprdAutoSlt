package com.sprd.autoslt.action;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.SLTApplication;

public abstract class AbstractAction implements IAction {

    public StatusChangedListener mStatusChangedListener;
    public static Context mContext;
    public static final int ACTION_TIME_OUT = 0;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case ACTION_TIME_OUT:
                error("time out");
                stop();
                Log.e("huasong", " time out");
                break;
            }
            super.handleMessage(msg);
        }
    };
    int i=0;

    public interface StatusChangedListener {
        void onStatusOk();

        void onStatusEnd();

        void onStatusResult(String result);
		
		void onStatusResult(String cmdString,String result);

        void onStatusError(String errorMessage);
    }

    public AbstractAction(StatusChangedListener listener) {
        mStatusChangedListener = listener;
        mContext = SLTApplication.getApplication().getApplicationContext();
    }

    @Override
    public void ok() {
        mStatusChangedListener.onStatusOk();
        ++i;
        Log.d("SLTManager","i: "+i);
    }

    @Override
    public void end() {
        mStatusChangedListener.onStatusEnd();
    }

    @Override
    public void error(String errorMessage) {
        Log.d("slt_action", "error:" + errorMessage);
        mStatusChangedListener.onStatusError(errorMessage);
    }

    @Override
    public void end(String result) {
        mStatusChangedListener.onStatusResult(result + "^");
    }
	
		@Override
	public void end(String cmdString, String result) {
		// TODO Auto-generated method stub
		mStatusChangedListener.onStatusResult(cmdString,result + "^");
	}
}
