package com.sprd.autoslt.action.impl;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;

public class VibratorModeAction extends AbstractAction {
    private static final String TAG = "VibratorModeAction";

    private Vibrator mVibrator = null;

    private String mType;

    public VibratorModeAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    @Override
    public void start(String param) {
        Log.d(TAG, "VibratorModeAction:" + param + "mType:" + mType);
        if(mType.equals(SLTConstant.ACTION_TYPE_START_VIBRATOR)) {
            mVibrator.vibrate(60000);
            ok();
        } else if (mType.equals(SLTConstant.ACTION_TYPE_END_VIBRATOR)) {
            if(mVibrator != null) {
                mVibrator.cancel();
                ok();
            } else {
                error("status error!");
            }
        }
    }

    @Override
    public void stop() {
    }
}