package com.sprd.autoslt;

import com.sprd.autoslt.common.SLTConstant;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class SLTLogManager {
    private static final String TAG = "SLTLogManager";
    private static Handler mAcitvityHandler;

    public static void setHandler(Handler handler) {
        mAcitvityHandler = handler;
    }

    public static void sendLog(String obj) {
        if(mAcitvityHandler != null) {
            Message msg = mAcitvityHandler.obtainMessage(SLTConstant.UPDATE_LOG);
            msg.obj = obj;
            mAcitvityHandler.sendMessage(msg);
            Log.d(TAG, "sendLog:" + obj);
        }
    }

    public static void clearLog() {
        if(mAcitvityHandler != null) {
            mAcitvityHandler.sendEmptyMessage(SLTConstant.CLEAR_LOG);
            Log.d(TAG, "clearLog..");
        }
    }

    public static void sendUpdate(String obj) {
        Log.d("huasong", "sendUpdate" + obj);
        if(mAcitvityHandler != null) {
            Message msg = mAcitvityHandler.obtainMessage(SLTConstant.UPDATE_RESULT);
            msg.obj = obj;
            mAcitvityHandler.sendMessage(msg);
            Log.d(TAG,"sendUpdate:" + obj);
        }
    }

    public static void updateRecordResult() {
        Log.d("huasong", "updateResult");
        if(mAcitvityHandler != null) {
            mAcitvityHandler.sendEmptyMessage(SLTConstant.UPDATE_RECORD_RESULT);
        }
    }
    
    public static void updateRecordHistoryInfo(String obj) {
        Log.d("huasong", "updateRecordHistoryInfo");
        if(mAcitvityHandler != null) {

            Message msg = mAcitvityHandler.obtainMessage(SLTConstant.UPDATA_RECORD_HISTOTY_INFO);
            msg.obj = obj;
            mAcitvityHandler.sendMessage(msg);
            Log.d(TAG,"updateRecordHistoryInfo:" + obj);
        }
    }
	
	 public static void updateFailNoteInfo(String obj){
    	Log.d("huasong", "updateFailNoteInfo");
        if(mAcitvityHandler != null) {

            Message msg = mAcitvityHandler.obtainMessage(SLTConstant.UPDATE_FAIL_NOTE_INFO);
            msg.obj = obj;
            mAcitvityHandler.sendMessage(msg);
            Log.d(TAG,"updateFailNoteInfo:" + obj);
        }
    }

    public static void clearResult() {
        if(mAcitvityHandler != null) {
            mAcitvityHandler.sendEmptyMessage(SLTConstant.CLEAR_RESULT);
            Log.d(TAG, "clearLog..");
        }
    }
}
