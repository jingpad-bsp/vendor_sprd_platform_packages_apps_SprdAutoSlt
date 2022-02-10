package com.sprd.autoslt.action.impl;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.HeadsetTestActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

public class HeadsetModeAction extends AbstractAction {
    private static final String TAG = "HeadsetModeAction";
    
    private String mType;

    public HeadsetModeAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "KeyModeAction:" + param);
        if(mType.equals(SLTConstant.ACTION_TYPE_START_HEADSET_MODE)) {
            if(!SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.HeadsetTestActivity")) {
              try {
                    Intent intent = new Intent(mContext, HeadsetTestActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    new Thread(new Runnable() {
                         @Override
                         public void run() {
                             // TODO Auto-generated method stub
                              int at = 0;
                              while (true) {
                                   Log.d(TAG, "HeadsetModeAction Thread.sleep 200ms");
                                   try {
                                        Thread.sleep(200);
                                     } catch (InterruptedException e) {
                                         e.printStackTrace();
                                     }
                                Log.d(TAG, "getTopActivity = "+ SLTUtil.getTopActivity(mContext).toString());
                                if (SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.HeadsetTestActivity")) {
                                     ok();
                                     break;
                                }
                                  at++;
                                if (at >= 10) {
                                    end("fail");
                                    break;
                                }
                              }
                         }
                    }).start();
                    //ok();
                } catch (Exception e) {
                    Toast.makeText(mContext, "current not support" + e.toString(), Toast.LENGTH_SHORT).show();
                    error("status error");
                }
            }else {
              ok();
            }
        } else if(mType.equals(SLTConstant.ACTION_TYPE_END_HEADSET_MODE)) {
            Log.d(TAG, "gettopacticity:" + SLTUtil.getTopActivity(mContext));
            if(SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.HeadsetTestActivity")) {
                HeadsetTestActivity.instance.finish();               
            }
            try {
                  Thread.sleep(100);
            } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
            }
            ok();
        } else if(mType.equals(SLTConstant.ACTION_TYPE_GET_HEADSET_RESULT)) {
            if(TextUtils.isEmpty(TestResultUtil.getInstance().getCurrentStepName())) {
                end("PlugOut");
            } else {                
                end(TestResultUtil.getInstance().getCurrentStepName());
            }
        }
    }

    @Override
    public void stop() {
    }
}