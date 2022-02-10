package com.sprd.autoslt.action.impl;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.LCDModeTestActivity;
import com.sprd.autoslt.camera.CameraTestActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

public class LCDModeAction extends AbstractAction {
    private static final String TAG = "LCDModeAction";
    
    private String mType;

    public LCDModeAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "LCDModeAction:" + param);
        if(mType.equals(SLTConstant.ACTION_TYPE_START_LCD_MODE)) {
            String mFileName = null;
            if(!TextUtils.isEmpty(param)) {
                if(param.contains("^")) {
                    String[] keyValue = param.split("\\^");
                    mFileName = keyValue[0];
                } else {
                    mFileName = param;
                }
                Log.d(TAG, "mFileName:" + mFileName);
                if (mFileName.contains("/")) {
                    File image = new File(mFileName);
                    if(!image.exists()) {
                        error("file not exists!");
                        Toast.makeText(mContext, "Image File " + mFileName + " not exist!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    if (!mFileName.equalsIgnoreCase("b255.jpg") && !mFileName.equalsIgnoreCase("g255.jpg")
                             &&!mFileName.equalsIgnoreCase("r255.jpg")&&!mFileName.equalsIgnoreCase("l0.jpg")
                             &&!mFileName.equalsIgnoreCase("l127.jpg") &&!mFileName.equalsIgnoreCase("l255.jpg")) {
                            error("file not exists!");
                            Toast.makeText(mContext, "Image File " + mFileName + " not exist!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                }                             
            }else {
                  error("param error");
                  return;
            }
            try {
                Intent intent = new Intent(mContext, LCDModeTestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra("param", param);
                mContext.startActivity(intent);
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        int count = 0;
                        while((!SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.LCDModeTestActivity") && count < 10) || !LCDModeTestActivity.isActivityInFront) {
                            Log.e(TAG, "sleep " + count);
                            SLTUtil.sleep(500);
                            count++;
                            if(LCDModeTestActivity.isActivityInFront) {
                                ok();
                                return;
                            }
                        }
                        if(LCDModeTestActivity.isActivityInFront) {
                            ok();
                        } else {
                            error("timeout!!");
                        }
                    }
                    
                }).start();
            } catch (Exception e) {
                error("status error");
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_END_LCD_MODE)) {
                Log.d(TAG, "gettopacticity:"+ SLTUtil.getTopActivity(mContext).toString());
                if (SLTUtil.getTopActivity(mContext).toString()
                         .contains("com.sprd.autoslt.activity.LCDModeTestActivity")) {
                       LCDModeTestActivity.instance.finish();
                }
               try { 
                    Thread.sleep(100);
               } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
               }
             ok();
        }
    }

    @Override
    public void stop() {
    }
}