package com.sprd.autoslt.action.impl;

import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.ConditionVariable;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.WifiAutoConnectManager;
import com.sprd.autoslt.util.WifiTestUtil;
import com.sprd.autoslt.util.WifiTestUtil.WifiSecurityType;

public class WifiModeAction extends AbstractAction {

    private static final String TAG = "WifiModeAction";

    private WifiManager mWifiManager;
    private String mSSID = null;
    private WifiTestUtil wifiTestUtil;
    private String mType;
    private ConditionVariable mConditionVariable = new ConditionVariable(false);

    public WifiModeAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        wifiTestUtil = new WifiTestUtil(mWifiManager, mContext) {

            public void wifiStateChange(int newState) {
                switch (newState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d(TAG, "Wifi ON,Discovering...");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.d(TAG, "Wifi OFF");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Log.d(TAG, "Wifi Closing");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.d(TAG, "Wifi Opening");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                default:
                    Log.d(TAG, "Wifi state Unknown");
                    // do nothing
                    break;
                }
            }
            public void wifiDeviceListChange(List<ScanResult> wifiDeviceList) {
                if (wifiDeviceList == null) {
                    return;
                }
                for (ScanResult result : wifiDeviceList) {
                    Log.d(TAG, "result.SSID:" + result.SSID);
                    Log.d(TAG, "mSSID:" + mSSID);
                    Log.d(TAG, "???" + result.SSID.equals(mSSID));
                    mHandler.removeMessages(ACTION_TIME_OUT);
                    if (mSSID == null) {
                        stop();
                        return;
                    }
                    if (result.SSID.equals(mSSID)) {
                        return;
                    }
                }
            }
            public void resultOfConditionVariable(){
                mConditionVariable.open();
            }
        };
    }

    @Override
    public void start(String param) {
        mHandler.sendEmptyMessageDelayed(ACTION_TIME_OUT, 12000);
        wifiTestUtil.registerAllReceiver();
        Log.d(TAG, "WifiModeAction:" + param + "mType:" + mType);
        if (mType.equals(SLTConstant.ACTION_TYPE_SET_WIFI_ON)) {
            wifiTestUtil.startWifi(mContext);
            ok();
            stop();
        } else if (mType.equals(SLTConstant.ACTION_TYPE_SET_WIFI_OFF)) {
            wifiTestUtil.stopWifi(mContext);
            ok();
            stop();
        } else if (mType.equals(SLTConstant.ACTION_TYPE_START_WIFI_CONNECT)) {
            if(!wifiTestUtil.isWifiEnabled()) {
                error("status error");
                Log.d(TAG, "isWifiEnabled:" + wifiTestUtil.isWifiEnabled());
            } else {
                if(TextUtils.isEmpty(param)) {
                    error("status error");
                    return;
                }
                String[] keyAndValue = SLTUtil.parseParam(param);
                mSSID = keyAndValue[0];
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                Log.d(TAG, "wifiInfo = " + wifiInfo );
                Log.d(TAG, "wifiInfo.getSupplicantState() = " + wifiInfo.getSupplicantState());
                Log.d(TAG, "getWifiConnect():" + wifiTestUtil.getWifiConnect());
                //String SSID_conneted = "\""+mSSID +"\"";
                if (/*wifiInfo.getSSID().equals(SSID_conneted)*/wifiTestUtil.getWifiConnect()) {
                Log.d(TAG, "************disconnect ****");
                    mWifiManager.disconnect();
                    mWifiManager.disableNetwork(wifiInfo.getNetworkId());
                    mWifiManager.removeNetwork(wifiInfo.getNetworkId());
                }
                wifiTestUtil.setWifiConnect(false);
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                wifiConfiguration.SSID = "\"" + mSSID + "\"";
                if(keyAndValue.length == 2 && !TextUtils.isEmpty(keyAndValue[1])) {
                    Log.d(TAG, "connect mSSID begin:" + mSSID + " pwd:" +  keyAndValue[1]);
                    wifiConfiguration.preSharedKey =  '"' + keyAndValue[1] + '"';
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);//WPA_PSK
                } else {
                    wifiConfiguration.allowedKeyManagement.set(KeyMgmt.NONE);
                }
                Log.d("huasong", "start connect with SSID : " + mSSID);
                mWifiManager.connect(wifiConfiguration, null);
                for (int i = 0; i < 50; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Log.d(TAG, "i = "+i+"; wifiTestUtil.getWifiConnect() --connect : " + wifiTestUtil.getWifiConnect());
                    if (wifiTestUtil.getWifiConnect()) {
                        end("pass");
                        return;
                    }
                }
                mWifiManager.disconnect();
                end("fail");
                stop();
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_GET_WIFI_INFO)) {
            if(!wifiTestUtil.isWifiEnabled()) {
                error("status error");
                Log.d(TAG, "isWifiEnabled:" + wifiTestUtil.isWifiEnabled());
            } else {
                String[] keyAndValue = SLTUtil.parseParam(param);
                mSSID = keyAndValue[0];
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                Log.d(TAG, "wifiInfo.getSSID:" + wifiInfo.getSSID());
                String ssid = wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() -1);
                if(!ssid.equals(mSSID)) {
                    end("fail");
                } else {
                    end("" + wifiInfo.getRssi());
                    Log.d(TAG, "Rssi:" + wifiInfo.getRssi());
                }
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_SET_WIFI_AP_ON)) {
            if(TextUtils.isEmpty(param)) {
                error("status error");
                return;
            }
            String[] SSIDAndPsw = SLTUtil.parseParam(param);
            Log.d(TAG, "SSIDAndPsw = " + SSIDAndPsw.length);
            Log.d(TAG, " turnOnWifiAp" + wifiTestUtil.getWifiAPState());
/*          if (wifiTestUtil.getWifiAPState() ==wifiTestUtil.WIFI_AP_STATE_ENABLED) {
                error("status error");
                Log.d(TAG, "wifi Ap is enable");
                return;
            }*/
            if (SSIDAndPsw.length == 1) {
                boolean result1 =wifiTestUtil.turnOnWifiAp(SSIDAndPsw[0], null,WifiSecurityType.WIFICIPHER_NOPASS);
                if (!mConditionVariable.block(5000)) {
                    Log.d(TAG, "block fail");
                    end("fail");
                }
                mConditionVariable.close();
                if (result1 && wifiTestUtil.getWifiAPState()== wifiTestUtil.WIFI_AP_STATE_ENABLED) {
                    ok();
                    stop();
                }else {
                    end("fail");
                }
            } else if (SSIDAndPsw.length == 2) {
                boolean result2 = wifiTestUtil.turnOnWifiAp(SSIDAndPsw[0], SSIDAndPsw[1],WifiSecurityType.WIFICIPHER_WPA2);
                if (!mConditionVariable.block(8000)) {
                    Log.d(TAG, "block fail");
                    end("fail");
                }
                mConditionVariable.close();
                if (result2 && wifiTestUtil.getWifiAPState()== wifiTestUtil.WIFI_AP_STATE_ENABLED) {
                    ok();
                    stop();
                }else {
                    end("fail");
                }
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_SET_WIFI_AP_OFF)) {
            if (wifiTestUtil.getWifiAPState() ==wifiTestUtil.WIFI_AP_STATE_DISABLED) {
                Log.d(TAG, "wifi Ap is disable");
                ok();
            }else {
                wifiTestUtil.closeWifiAp();
                if (!mConditionVariable.block(8000)) {
                    Log.d(TAG, "block fail");
                    end("fail");
                }
                mConditionVariable.close();
                if (wifiTestUtil.getWifiAPState() == wifiTestUtil.WIFI_AP_STATE_DISABLED) {
                    ok();
                    stop();
                }else {
                    end("fail");
                }
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_FORGET_WIFI)) {
            forgetWifi(mContext);
            ok();
        }
    }

    @Override
    public void stop() {
        mHandler.removeMessages(ACTION_TIME_OUT);
        wifiTestUtil.unregisterAllReceiver();
    }

    private void forgetWifi(Context context){
        Log.d(TAG, "forgetWifi start");
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiAutoConnectManager manager = WifiAutoConnectManager.newInstance(wifiManager);
            int networkId = WifiAutoConnectManager.getNetworkId();
            Log.d(TAG, "forgetWifi networkId="+networkId);
            if(networkId < 0){
                String ssid = WifiAutoConnectManager.getBSSID();
                Log.d(TAG, "forgetWifi networkId="+networkId);
                wifiManager.disableEphemeralNetwork(ssid);
            }else{
                manager.forgetWifi(networkId);
            }
            manager.closeWifi();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
