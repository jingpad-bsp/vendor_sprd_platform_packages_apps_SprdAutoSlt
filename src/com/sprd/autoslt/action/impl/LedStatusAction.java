package com.sprd.autoslt.action.impl;

import android.os.IBinder;
import android.os.Parcel;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.PhaseCheckParse;

public class LedStatusAction extends AbstractAction {
    private static final String TAG = "LedStatusAction";

    private static String mType;

    public static LedStatusAction instance;
    private PhaseCheckParse mCheckParse;

    private LedStatusAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mCheckParse = new PhaseCheckParse();
    }

    public static LedStatusAction getInstance(StatusChangedListener listener,
            String type) {
        if (instance == null) {
            instance = new LedStatusAction(listener, type);
        }
        mType = type;
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "LedStatusAction:" + param);
        if (mType.equals(SLTConstant.ACTION_TYPE_START_STATUS_LED)) {
        	if (mCheckParse == null) {
    			mCheckParse = new PhaseCheckParse();
    		}
            if(!TextUtils.isEmpty(param)) {
                if(param.equals("red")) {
                    closeLedLight();
                    mCheckParse.writeLedlightSwitch(7, 1);
                    ok();
                } else if(param.equals("blue")) {
                    closeLedLight();
                    mCheckParse.writeLedlightSwitch(8, 1);

                    ok();
                } else if(param.equals("green")) {
                    closeLedLight();
                    mCheckParse.writeLedlightSwitch(9, 1);
                    ok();
                } else {
                    error("param error!");
                }
            } else {
                error("param error!");
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_END_STATUS_LED)) {
            closeLedLight();
            ok();
        } else {
            error("param error!");
        }
    }

    private void closeLedLight() {
    	if (mCheckParse == null) {
			mCheckParse = new PhaseCheckParse();
		}
    	mCheckParse.writeLedlightSwitch(7, 0);
    	mCheckParse.writeLedlightSwitch(8, 0);
    	mCheckParse.writeLedlightSwitch(9, 0);
    }

    @Override
    public void stop() {
    }
}