package com.sprd.autoslt.action.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.telephony.SubscriptionManager;

import com.sprd.autoslt.common.SLTConstant;

public class NetWorkCelluarAction extends TeleNetworkAction {
    private static final String TAG = "SetNetWorkCelluarAction";
    private static NetWorkCelluarAction instance;
    private static String mType;

    public NetWorkCelluarAction(StatusChangedListener listener) {
        super(listener);
    }
    public static NetWorkCelluarAction getInstance(StatusChangedListener listener, String type) {
        mType = type;
        if (instance == null) {
            instance = new NetWorkCelluarAction(listener);
        }
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "NetWorkCelluarAction param:" + param);
        if(SLTConstant.ACTION_TYPE_SET_NETWORK_CELLULAR.equals(mType)) {
            if(TextUtils.isEmpty(param)) {
                error("param error");
                return;
            }
            if(param.equals("2G")) {
                if(isAirplaneModeOn()) {
                    setAirplaneMode(false);
                }
                // switch network to 2G when it is not.
                switchTeleNetwork(NETWORK_2G);
                ok();
            } else if(param.equals("3G")) {
                if(isAirplaneModeOn()) {
                    setAirplaneMode(false);
                }
                // switch network to 3G when it is not.
                switchTeleNetwork(NETWORK_3G);
                ok();
            } else if(param.equals("4G")) {
                if(isAirplaneModeOn()) {
                    setAirplaneMode(false);
                }
                // switch network to 4G when it is not.
                switchTeleNetwork(NETWORK_4G);
                ok();
            } else if(param.equals("flight")) {
                if(isAirplaneModeOn()) {
                    ok();
                } else {
                    setAirplaneMode(true);
                    ok();
                }
            } else {
                //error("invalid param..");
                if(isAirplaneModeOn()) {
                    setAirplaneMode(false);
                }
                Log.d(TAG, "param:" + param);
                // switch network
                int type = -1;
                try {
                    type = Integer.valueOf(param);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if(type > -1){
                    int mSubId = SubscriptionManager.getSubId(0)[0];
                    switchPreferredNetworkType(mSubId,type);
                    ok();
                }else{
                    error("invalid param..");
                }
            }
        } else if(SLTConstant.ACTION_TYPE_GET_NETWORK_CELLULAR.equals(mType)) {
            if(isAirplaneModeOn()) {
                Log.d(TAG, "airplane mode on");
                end("flight");
            } else {
                switch(getTeleNetwork()) {
                case NETWORK_2G:
                    end("2G");
                    break;
                case NETWORK_3G:
                    end("3G");
                    break;
                case NETWORK_4G:
                    end("4G");
                    break;
                case NETWORK_5G:
                    end("5G");
                    break;
                default:
                    end("unknown");
                    break;
                }
            }
        } else {
            error("invalid param..");
        }
    }

    private void setAirplaneMode(boolean enabled) {
        final ConnectivityManager mgr =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mgr.setAirplaneMode(enabled);
    }

    private boolean isAirplaneModeOn() {
        return Settings.System.getInt(mContext.getContentResolver(),
              Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    @Override
    public void stop() {
    }
}