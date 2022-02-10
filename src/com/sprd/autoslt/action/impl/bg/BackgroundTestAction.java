package com.sprd.autoslt.action.impl.bg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.InputDevice;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.action.AbstractBackGroundAction;
import com.sprd.autoslt.action.AbstractBackGroundAction.BackStatusChangedListener;
import com.sprd.autoslt.action.impl.otg.OTGTestAction;
import com.sprd.autoslt.action.impl.rtc.RTCTestAction;
import com.sprd.autoslt.fingerprint.FingerprintTestAction;
import com.sprd.autoslt.fingerprint.microarray.ISprdFingerDetectListener;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.StorageUtil;
import com.sprd.autoslt.util.TestColumns;
import com.sprd.autoslt.util.TestItem;
import com.sprd.autoslt.util.TestResultUtil;
import com.sprd.autoslt.R;

public class BackgroundTestAction extends AbstractAction {

    private static final String TAG = "BackgroundTestAction";
    private static BackgroundTestAction instance = null;
    private ArrayList<AbstractBackGroundAction> mBackGroundActions = new ArrayList<AbstractBackGroundAction>();
    private HashMap<String, BackgroundTestThread> mBackgroundTestThreads = new HashMap<String, BackgroundTestThread>();
    private BackStatusChangedListener mBackStatusChangedListener;

    private BackgroundTestAction(StatusChangedListener listener,BackStatusChangedListener listener2) {
        super(listener);
        mBackStatusChangedListener = listener2;
        TestResultUtil.getInstance().reset();
    }

    public static BackgroundTestAction getInstance(StatusChangedListener listener,BackStatusChangedListener listener2) {
        if(instance == null) {
            instance = new BackgroundTestAction(listener, listener2);
        }
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, TAG + " start param="+param);
        String[] params = SLTUtil.parseParam(param);
        if(params != null){
            resetAllAtions();
            mBackGroundActions.clear();
            for(int i=0;i<params.length;i++){
                String action = params[i];
                AbstractBackGroundAction bgAction = null;
                if(FingerprintTestAction.PARM_NAME.equals(action)){
                    bgAction = FingerprintTestAction.getInstance(mStatusChangedListener,mBackStatusChangedListener);
                    mBackGroundActions.add(bgAction);
                }else if(OTGTestAction.PARM_NAME.equals(action)){
                    bgAction = OTGTestAction.getInstance(mStatusChangedListener,mBackStatusChangedListener);
                    mBackGroundActions.add(bgAction);
                }else if(RTCTestAction.PARM_NAME.equals(action)){
                    bgAction = RTCTestAction.getInstance(mStatusChangedListener,mBackStatusChangedListener);
                    mBackGroundActions.add(bgAction);
                }else{
                    Log.d(TAG, TAG + " Ignore action:"+action);
                    continue;
                }
                BackgroundTestThread backThread = mBackgroundTestThreads.get(action);
                Log.d(TAG, TAG + " start action:"+action+",backThread:"+backThread);
                if(backThread != null){
                    Log.d(TAG, TAG + " Alread exits action:"+action);
                    backThread.stopTest();
                    backThread.startTest();
                }else{
                    BackgroundTestThread thread = new BackgroundTestThread(action, null, bgAction);
                    mBackgroundTestThreads.put(action, thread);
                    thread.start();
                    thread.stopTest();
                    thread.startTest();
                }
            }
            mBackStatusChangedListener.onBackgroundAtionsChange(mBackGroundActions);
        }
        ok();
    }

    @Override
    public void stop() {

    }

    private void resetAllAtions(){
        for(AbstractBackGroundAction action: mBackGroundActions){
            action.resetResult();
        }
    }

    public ArrayList<AbstractBackGroundAction> getAllBackGroundAction(){
    	return mBackGroundActions;
    }

    public void startBackgroundAction(){
        Log.d(TAG, "startBackgroundAction !!");
    }

    class BackgroundAsyncTask extends AsyncTask<AbstractBackGroundAction, Void, Void>{
        private AbstractBackGroundAction mAction = null;
        @Override
        protected Void doInBackground(AbstractBackGroundAction... params) {
            if(params == null){
                return null;
            }
            mAction = params[0];
            mAction.stopBackground();
            mAction.startBackground(null);
            return null;
        }
    }
}

