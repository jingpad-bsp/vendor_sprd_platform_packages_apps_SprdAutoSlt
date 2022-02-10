package com.sprd.autoslt.action.impl;

import android.text.TextUtils;
import android.util.Log;

import com.example.testat.ATCommandNative;
import com.sprd.autoslt.IATUtils;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;

public class ATAction extends AbstractAction {
    private static final String TAG = "ATAction";
    private static ATAction instance;
    private static String mType;

    public ATAction(StatusChangedListener listener) {
        super(listener);
    }

    private ATAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
    }

    public static ATAction getInstance(StatusChangedListener listener,
            String type) {
        if (instance == null) {
            instance = new ATAction(listener, type);
        }
        mType = type;
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "ATAction tpye:" + mType + " param:" + param);
        if(mType.equals(SLTConstant.ACTION_TYPE_GET_CFT_INFO)) {
            String result = "error";
            if(!TextUtils.isEmpty(param)) {
                if(param.equals("gsm")) {
                    try {
                        ATCommandNative.reloadLib();
                        //result = ATCommandNative.native_sendATCmd(0, "AT+SGMR=0,0,3,0");
                        result = IATUtils.sendAtCmd("AT+SGMR=0,0,3,0");
                    } catch (UnsatisfiedLinkError e) {
                        e.printStackTrace();
                        result = "read error";
                    }
                } else if(param.equals("wcdma")) {
                    try {
                        ATCommandNative.reloadLib();
                        //result = ATCommandNative.native_sendATCmd(0, "AT+SGMR=0,0,3,1");
                        result = IATUtils.sendAtCmd("AT+SGMR=0,0,3,1");
                    } catch (UnsatisfiedLinkError e) {
                        e.printStackTrace();
                       result = "read error";
                    }
                } else if(param.equals("lte")) {
                    try {
                        ATCommandNative.reloadLib();
                        //result = ATCommandNative.native_sendATCmd(0, "AT+SGMR=1,0,3,3");
                        result = IATUtils.sendAtCmd("AT+SGMR=1,0,3,3");
                    } catch (UnsatisfiedLinkError e) {
                        e.printStackTrace();
                       result = "read error";
                    }
                } else {
                    error("param error!");
                }
            } else {
                try {
                    ATCommandNative.reloadLib();
                    //result = ATCommandNative.native_sendATCmd(0, "AT+SGMR=0,0,3,0");
                    //result += ATCommandNative.native_sendATCmd(0, "AT+SGMR=0,0,3,1");
                    //result += ATCommandNative.native_sendATCmd(0, "AT+SGMR=0,0,3,3");
                    result = IATUtils.sendAtCmd("AT+SGMR=0,0,3,0");
                    result += IATUtils.sendAtCmd("AT+SGMR=0,0,3,1");
                    result += IATUtils.sendAtCmd("AT+SGMR=0,0,3,3");
                } catch (UnsatisfiedLinkError e) {
                    e.printStackTrace();
                    result = "read error";
                }
            }
            end(result);
        } else if(mType.equals(SLTConstant.ACTION_TYPE_CHECK_CALL_STATUS)) {
            
        } else {
            error("invalid cmd!");
        }
    }

    @Override
    public void stop() {
    }
}