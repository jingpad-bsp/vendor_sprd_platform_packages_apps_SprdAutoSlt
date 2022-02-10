
package com.sprd.autoslt.action.impl;

import java.io.File;

import android.R.string;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.camera.CameraTestActivity;
import com.sprd.autoslt.camera.CameraTestActivityNew;
import com.sprd.autoslt.camera.MediaRecorderActivity;
import com.sprd.autoslt.util.TestResultUtil;
import com.sprd.autoslt.util.SLTUtil;

public class CameraAction extends AbstractAction {

    private static final String TAG = "SLTCameraAction";
    private Context sContext;
    public static final int MSG_SHOTSTART_TESTOK = 2001;
    public static final int MSG_SHOTSTART_TESTFAIL = 2002;
    public static final int MSG_SHOTSTOP_TEST = 2003;
    public static final int MSG_SHOT_FINISH = 2004;
    public Handler cameraShotHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_SHOTSTART_TESTOK:
                Log.d(TAG, "MSG_VIDESTART_TESTOK ");
                ok();
                break;
            case MSG_SHOTSTART_TESTFAIL:
                error(TestResultUtil.getInstance().getCurrentStepStatus());
                TestResultUtil.getInstance().reset();
                sendEmptyMessage(MSG_SHOTSTOP_TEST);
                break;
            case MSG_SHOTSTOP_TEST :
                CameraTestActivityNew.instance.freeResource();
                CameraTestActivityNew.instance.finish();
                break;
            case MSG_SHOT_FINISH:
                String picturePath = TestResultUtil.getInstance().getCurrentStepName();
                Log.d(TAG, "picturePath = "+picturePath );
                if (picturePath == null || picturePath.equals("")) {
                    TestResultUtil.getInstance().setCurrentStepStatus("picture Path is null");
                    sendEmptyMessage(MSG_SHOTSTART_TESTFAIL);
                    break;
                }
                File picFile = new File(picturePath);
                if (!picFile.getParentFile().exists()) {
                    Log.d(TAG, "picFile.getParentFile().exists() = "+picFile.getParentFile().exists());
                    TestResultUtil.getInstance().setCurrentStepStatus("picture getParentFile is exists");
                    sendEmptyMessage(MSG_SHOTSTART_TESTFAIL);
                    break;
                }
                if (!picFile.exists()) {
                    Log.d(TAG, "picFile.exists = "+picFile.exists() );
                    TestResultUtil.getInstance().setCurrentStepStatus("picture file is not exists");
                    sendEmptyMessage(MSG_SHOTSTART_TESTFAIL);
                    break;
                }else {
                    end(TestResultUtil.getInstance().getCurrentStepName());
                }
                break;
            default:
                break;
            }
        }
    };

    public CameraAction(StatusChangedListener listener, Context context) {
        super(listener);
        sContext = context;
        TestResultUtil.getInstance().reset();
    }

    @Override
    public void start(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "start() failed : file name is empty!");
            error("CameraAction start() failed : file name is empty!");
            return;
        }

        String[] params = SLTUtil.parseParam(fileName);
        if (!CameraTestActivityNew.mIsActivityInFront) {
        Intent intent = new Intent(sContext,CameraTestActivityNew.class);
        intent.putExtra("cameraID", params[0]);
        if (params.length > 1) {
            intent.putExtra("focusParam", params[1]);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sContext.startActivity(intent);
        new Thread(new Runnable(){
            @Override
            public void run() {
                int count = 0;
                Log.e(TAG, "begin sleep..");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                while(CameraTestActivityNew.mIsActivityInFront && count < 50) {
                    Log.e(TAG, "sleep " + count);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    count++;
                }
                Log.d(TAG, "TestResultUtil.getInstance().getCurrentResult() =" + TestResultUtil.getInstance().getCurrentResult());
                Log.d(TAG, "TestResultUtil.getInstance().getCurrentStepName() =" + TestResultUtil.getInstance().getCurrentStepName());
                if(count == 49 || CameraTestActivityNew.mIsActivityInFront) {
                    error("timeout!!");
                    cameraShotHandler.sendEmptyMessage(MSG_SHOTSTOP_TEST);
                } else if (TestResultUtil.getInstance().getCurrentResult()!= null&&!TestResultUtil.getInstance().getCurrentResult().equals("")&&TestResultUtil.getInstance().getCurrentResult().equals("fail")) {
                    cameraShotHandler.sendEmptyMessage(MSG_SHOTSTART_TESTFAIL);
                } else{
                    cameraShotHandler.sendEmptyMessage(MSG_SHOT_FINISH);
                }
            }
        }).start();
        }

     /*   if (!CameraTestActivity.mIsActivityInFront) {
            Intent intent = new Intent(sContext, CameraTestActivity.class);
            intent.putExtra("fileName", fileName);
            intent.putExtra("mIsAuto", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sContext.startActivity(intent);
            new Thread(new Runnable(){
                @Override
                public void run() {
                    int count = 0;
                    Log.e(TAG, "begin sleep..");
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                    }
                    while(CameraTestActivity.mIsActivityInFront && count < 50) {
                        Log.e(TAG, "sleep " + count);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                        count++;
                    }
                    Log.d(TAG, "TestResultUtil.getInstance().getCurrentResult() =" +TestResultUtil.getInstance().getCurrentResult());
                    Log.d(TAG, "TestResultUtil.getInstance().getCurrentStepName() =" + TestResultUtil.getInstance().getCurrentStepName());
                    if(count == 99 || CameraTestActivity.mIsActivityInFront) {
                        error("timeout!!");
                    } else if (TestResultUtil.getInstance().getCurrentResult().equals("ok")) {
                         end(TestResultUtil.getInstance().getCurrentStepName());
                         Log.e(TAG, "end:"+TestResultUtil.getInstance().getCurrentStepName());
                    }else {
                        error("test fail");
                    }
                }
            }).start();
        } else {
            Log.d(TAG, "CameraTestActivity already in the foreground");
        }
        */
    }

    @Override
    public void stop() {
    }
}
