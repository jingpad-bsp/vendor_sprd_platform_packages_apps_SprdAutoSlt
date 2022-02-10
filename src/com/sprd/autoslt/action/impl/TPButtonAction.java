package com.sprd.autoslt.action.impl;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.TPButtonTestActivity;
import com.sprd.autoslt.activity.TPButtonTestActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

public class TPButtonAction extends AbstractAction {
    private static final String TAG = "TPButtonAction";
    private static TPButtonAction instance;
    
    private static String mType;

    private TPButtonAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
    }
    
    public static TPButtonAction getInstance(StatusChangedListener listener, String type) {
        if(instance == null) {
            instance = new TPButtonAction(listener, type);
        }
        mType = type;
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "KeyModeAction:" + param);
        if(mType.equals(SLTConstant.ACTION_TYPE_START_TPBUTTON)) {
            try {
                if (SLTUtil.getTopActivity(mContext).toString().contains(
                        "com.sprd.autoslt.activity.TPButtonTestActivity")) {
                    ok();
                    //error("status error");
                    return;
                }
                Intent intent = new Intent(mContext, TPButtonTestActivity.class);
                //ComponentName comp = new ComponentName("com.sprd.validationtools", "com.sprd.validationtools.itemstest.TPButtonTestActivity");
                //intent.setComponent(comp);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                       new Thread(new Runnable() {
                            @Override
                            public void run() {
                              // TODO Auto-generated method stub
                              int at = 0;
                              while (true) {
                                Log.d(TAG, "TPButtonAction Thread.sleep 200ms");
                                try {
                                     Thread.sleep(200);
                                } catch (InterruptedException e) {
                                     e.printStackTrace();
                                }
                                Log.d(TAG, "getTopActivity = "+ SLTUtil.getTopActivity(mContext).toString());
                                if (SLTUtil.getTopActivity(mContext).toString()
                                    .contains("com.sprd.autoslt.activity.TPButtonTestActivity")) {
                                    ok();
                                    break;
                                }
                                at++;
                                if (at >= 10 ) {
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
        } else if(mType.equals(SLTConstant.ACTION_TYPE_END_TPBUTTON)) {
            Log.d(TAG, "gettopacticity:" + SLTUtil.getTopActivity(mContext).toString());
            if(SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.TPButtonTestActivity")) {
                TPButtonTestActivity.instance.finish();
              //  ok();
            } try {
                   Thread.sleep(100);
                   } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                   e.printStackTrace();
                }
             ok();
        } else if(mType.equals(SLTConstant.ACTION_TYPE_GET_TPBUTTON_RESULT)) {
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