package com.sprd.autoslt.action.impl;

import android.telephony.TelephonyManager;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;

public class GetSIMResultAction extends AbstractAction {
    private static final String TAG = "GetSIMResultAction";
    private int mPhoneCount;
    private static GetSIMResultAction instance;
    private TelephonyManager mTelephonyManager;

    private GetSIMResultAction(StatusChangedListener listener) {
        super(listener);
    }
    
    public static GetSIMResultAction getInstance(StatusChangedListener listener) {
        if(instance == null) {
            instance = new GetSIMResultAction(listener);
        }
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "GetSIMResultAction:" + param);
        mPhoneCount = TelephonyManager.from(mContext).getPhoneCount();
        try {
            int count = Integer.parseInt(param);
            if(count > mPhoneCount) count = mPhoneCount;
            String result = null;
            for (int i = 0; i < count; i++) {
                if (i == 0) {
                    result = getSimResult(i) ? "pass" : "fail";
                } else {
                    result += "^";
                    result += getSimResult(i) ? "pass" : "fail";
                }
            }
            end(result);
        } catch (NumberFormatException e) {
            error("NumberFormatException");
            return;
        }
    }
    
    private boolean getSimResult(int simId) {
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE + simId);
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
            if (mTelephonyManager == null) {
                return false;
            }
        }
        if (mTelephonyManager.getSimState(simId) == TelephonyManager.SIM_STATE_READY) {
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
    }
}