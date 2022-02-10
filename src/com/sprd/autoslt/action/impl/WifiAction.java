package com.sprd.autoslt.action.impl;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.WifiTestUtil;

public class WifiAction extends AbstractAction {

    private static final String TAG = "SLTWifiAction";

    private WifiManager mWifiManager;
    private String mSSID = null;
    private WifiTestUtil wifiTestUtil;

    public WifiAction(StatusChangedListener listener) {
        super(listener);
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        wifiTestUtil = new WifiTestUtil(mWifiManager, mContext) {

            public void wifiStateChange(int newState) {
                switch (newState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d("huasong", "Wifi ON,Discovering...");
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.d("huasong", "Wifi OFF");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.d("huasong", "Wifi Closing");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.d("huasong", "Wifi Opening");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                    default:
                        Log.d("huasong", "Wifi state Unknown");
                        // do nothing
                        break;

                }
            }

            public void wifiDeviceListChange(List<ScanResult> wifiDeviceList) {
                if (wifiDeviceList == null) {
                    return;
                }

                for (ScanResult result : wifiDeviceList) {
                    Log.d("huasong", "result.SSID:" + result.SSID);
                    Log.d("huasong", "mSSID:" + mSSID);
                    Log.d("huasong", "???"+result.SSID.equals(mSSID));
                    if(result.SSID.equals(mSSID)) {
                        ok();
                        stop();
                        mHandler.removeMessages(ACTION_TIME_OUT);
                        return;
                    }
                }
                //error(TAG + " find failed");
                //stop();
                //mHandler.removeMessages(ACTION_TIME_OUT);
            }
        };
    }

    @Override
    public void start(final String ssid) {
        Log.d("huasong", "start wifi test:" + ssid);
        if (TextUtils.isEmpty(ssid)) {
            Log.e(TAG, "start() failed -> ssid is empty!");
            error(TAG + " start() failed -> ssid is empty!");
            return;
        }
        wifiTestUtil.startTest(mContext);
        mHandler.sendEmptyMessageDelayed(ACTION_TIME_OUT, 12000);
        mSSID = ssid;
    }

    @Override
    public void stop() {
        wifiTestUtil.stopTest();
        mHandler.removeMessages(ACTION_TIME_OUT);
    }
}
