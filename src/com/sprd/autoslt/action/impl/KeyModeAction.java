package com.sprd.autoslt.action.impl;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.KeyTestActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

public class KeyModeAction extends AbstractAction {
    private static final String TAG = "KeyModeAction";
    
    private String mType;

    public KeyModeAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "KeyModeAction:" + param);
        if(mType.equals(SLTConstant.ACTION_TYPE_START_KEY_MODE)) {
            try {
                if (SLTUtil.getTopActivity(mContext).toString().contains(
                        "com.sprd.autoslt.activity.KeyTestActivity")) {
                    ok();
                    return;
                }
                Intent intent = new Intent(mContext, KeyTestActivity.class);
                //ComponentName comp = new ComponentName("com.sprd.validationtools", "com.sprd.validationtools.itemstest.KeyTestActivity");
                //intent.setComponent(comp);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        int count = 0;
                        while((!SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.KeyTestActivity") && count < 4) || !KeyTestActivity.isActivityInFront) {
                            Log.e(TAG, "sleep " + count);
                            SLTUtil.sleep(500);
                            count++;
                            if(KeyTestActivity.isActivityInFront) {
                                ok();
                                return;
                            }
                        }
                        if(KeyTestActivity.isActivityInFront) {
                            ok();
                        } else {
                            error("timeout!!");
                        }
                    }
                    
                }).start();
            } catch (Exception e) {
                Toast.makeText(mContext, "current not support" + e.toString(), Toast.LENGTH_SHORT).show();
                error("status error");
            }
        } else if(mType.equals(SLTConstant.ACTION_TYPE_END_KEY_MODE)) {
            Log.d(TAG, "gettopacticity:" + SLTUtil.getTopActivity(mContext).toString());
            if(SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.KeyTestActivity")) {
                KeyTestActivity.instance.finish();
               // ok();
            } 
            try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ok();
        } else if(mType.equals(SLTConstant.ACTION_TYPE_GET_KEY_RESULT)) {
            if(TextUtils.isEmpty(TestResultUtil.getInstance().getCurrentStepName())) {
                end("null");
            } else {
                end(TestResultUtil.getInstance().getCurrentStepName());
                TestResultUtil.getInstance().reset();
            }
        }
    }

    @Override
    public void stop() {
    }
}