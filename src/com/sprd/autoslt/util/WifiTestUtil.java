package com.sprd.autoslt.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

public class WifiTestUtil {
    private static String TAG = "WifiTestUtil";

    private static final int DELAY_TIME = 5000;
    private WifiManager mWifiManager = null;
    private Context mContext = null;
    private int mLastCount = 0;
    private WifiStateChangeReceiver mWifiStateChangeReceiver = null;
    private WifiScanReceiver mWifiScanReceiver = null;
    private WifiNetworkStateChangeReceiver mWifiNetworkStateChangeReceiver = null;
    private StartScanThread mStartScanThread = null;
    private static final String DEFAULT_AP_PASSWORD = "12345678";

    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    public static int WIFI_AP_STATE_DISABLING = 10;
    public static int WIFI_AP_STATE_DISABLED = 11;
    public static int WIFI_AP_STATE_ENABLING = 12;
    public static int WIFI_AP_STATE_ENABLED = 13;
    public static int WIFI_AP_STATE_FAILED = 14;
    public boolean isWifiConnect = false;

    public boolean getWifiConnect() {
        return isWifiConnect;
    }

    public void setWifiConnect(boolean isWifiConnect) {
        this.isWifiConnect = isWifiConnect;
    }

    public WifiTestUtil(WifiManager wifiManager, Context context) {
        mWifiManager = wifiManager;
        mContext = context;
        Log.d(TAG, "WifiTestUtil");
    }

    public void registerAllReceiver() {
        mWifiStateChangeReceiver = new WifiStateChangeReceiver();
        String filterFlag1 = WifiManager.WIFI_STATE_CHANGED_ACTION;
        IntentFilter filter1 = new IntentFilter(filterFlag1);
        mContext.registerReceiver(mWifiStateChangeReceiver, filter1);

        mWifiScanReceiver = new WifiScanReceiver();
        String filterFlag2 = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
        IntentFilter filter2 = new IntentFilter(filterFlag2);
        mContext.registerReceiver(mWifiScanReceiver, filter2);

        mWifiNetworkStateChangeReceiver = new WifiNetworkStateChangeReceiver();
        String filterFlag3 = WifiManager.NETWORK_STATE_CHANGED_ACTION;
        IntentFilter filter3 = new IntentFilter(filterFlag3);
        mContext.registerReceiver(mWifiNetworkStateChangeReceiver, filter3);

        // IntentFilter apfilter = new IntentFilter();
        // apfilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        // mContext.registerReceiver(mWifiAPStateBroadcastReceiver, apfilter);

    }

    public void unregisterAllReceiver() {
        // release wifi enabled receiver
        if (mWifiStateChangeReceiver != null) {
            mContext.unregisterReceiver(mWifiStateChangeReceiver);
            mWifiStateChangeReceiver = null;
        }

        // release wifi scan receiver
        if (mWifiScanReceiver != null) {
            mContext.unregisterReceiver(mWifiScanReceiver);
            mWifiScanReceiver = null;
        }

        if (mWifiNetworkStateChangeReceiver != null) {
            mContext.unregisterReceiver(mWifiNetworkStateChangeReceiver);
            mWifiNetworkStateChangeReceiver = null;
        }
        // if (mWifiAPStateBroadcastReceiver != null) {
        // mContext.unregisterReceiver(mWifiAPStateBroadcastReceiver);
        // mWifiAPStateBroadcastReceiver = null;
        // }
        // mContext = null;
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    public void startTest(Context context) {
        mContext = context;
        // registerAllReceiver();
        if (mWifiManager.isWifiEnabled()) {
            wifiStateChange(WifiManager.WIFI_STATE_ENABLED);
            wifiStartDiscovery();
        } else {
            wifiStateChange(WifiManager.WIFI_STATE_DISABLED);
            mWifiManager.setWifiEnabled(true);
        }
    }

    public void startWifi(Context context) {
        mContext = context;
        // registerAllReceiver();
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public void stopWifi(Context context) {
        mContext = context;
        // unregisterAllReceiver();
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    public void stopTest() {
        // mWifiManager.cancelDiscovery();
        // unregisterAllReceiver();
        mWifiManager.setWifiEnabled(false);
    }

    private void wifiStartDiscovery() {
        if (mWifiManager != null) {
            mStartScanThread = new StartScanThread();
            mStartScanThread.start();
            Log.w(TAG, "============startDiscovery===============");
        }
    }

    public void wifiStateChange(int newState) {
        // for override
    }

    public void wifiDeviceListChange(List<ScanResult> wifiDeviceList) {
        // for override
    }

    public void wifiDiscoveryFinished() {
        // for override
    }

    public void resultOfConditionVariable() {

    }

    private class WifiStateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newState = mWifiManager.getWifiState();
            Log.d(TAG, "" + newState);
            switch (newState) {
            case WifiManager.WIFI_STATE_ENABLED:
                wifiStartDiscovery();
                break;
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_UNKNOWN:
            case WifiManager.WIFI_STATE_ENABLING:
            default:
                // do nothing
                break;
            }

            wifiStateChange(newState);
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "WifiScanReceiver ---action = " + action);
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> wifiScanResultList = mWifiManager
                        .getScanResults();

                if (wifiScanResultList != null
                        && wifiScanResultList.size() != mLastCount) {
                    wifiDeviceListChange(wifiScanResultList);

                    mLastCount = wifiScanResultList.size();
                }
            }
        }
    }

    private class WifiNetworkStateChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            Log.d(TAG, "WifiNetworkStateChangeReceiver ----- action = "
                    + action);
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.d(TAG, "network state changed");
                NetworkInfo info = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "info.getState() = " + info.getState());
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    Log.d(TAG, "wifi network disconnected ");
                    isWifiConnect = false;
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    isWifiConnect = true;
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    Log.d(TAG, "wifi network connectd : " + wifiInfo.getSSID());
                }
            }
        }

    }

    class StartScanThread extends Thread {
        @Override
        public void run() {
            try {
                // wait until other actions finish.
                SystemClock.sleep(DELAY_TIME);
                mWifiManager.startScan();
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    public enum WifiSecurityType {
        WIFICIPHER_NOPASS, WIFICIPHER_WPA, WIFICIPHER_WEP, WIFICIPHER_INVALID, WIFICIPHER_WPA2
    }

    public boolean turnOnWifiAp(String str, String password,
            WifiSecurityType Type) {
        String ssid = str;
        WifiConfiguration wcfg = new WifiConfiguration();
        wcfg.SSID = new String(ssid);
        wcfg.networkId = 1;
        wcfg.allowedAuthAlgorithms.clear();
        wcfg.allowedGroupCiphers.clear();
        wcfg.allowedKeyManagement.clear();
        wcfg.allowedPairwiseCiphers.clear();
        wcfg.allowedProtocols.clear();
        wcfg.softApMaxNumSta = 5;

        if (Type == WifiSecurityType.WIFICIPHER_NOPASS) {
            Log.d(TAG, "wifi ap----no password");
            wcfg.allowedAuthAlgorithms.set(
                    WifiConfiguration.AuthAlgorithm.OPEN, true);
            wcfg.wepKeys[0] = "";
            wcfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wcfg.wepTxKeyIndex = 0;
        } else if (Type == WifiSecurityType.WIFICIPHER_WPA) {
            Log.d(TAG, "wifi ap----wpa");
            if (null != password && password.length() >= 8) {
                wcfg.preSharedKey = password;
            } else {
                wcfg.preSharedKey = DEFAULT_AP_PASSWORD;
            }
            wcfg.hiddenSSID = false;
            wcfg.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wcfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            // wcfg.allowedKeyManagement.set(4);
            wcfg.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            wcfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wcfg.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
        } else if (Type == WifiSecurityType.WIFICIPHER_WPA2) {
            Log.d(TAG, "wifi ap---- wpa2");
            if (null != password && password.length() >= 8) {
                wcfg.preSharedKey = password;
            } else {
                wcfg.preSharedKey = DEFAULT_AP_PASSWORD;
            }
            wcfg.hiddenSSID = true;
            wcfg.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wcfg.allowedKeyManagement.set(4);
            // wcfg.allowedKeyManagement.set(4);
            wcfg.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            wcfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wcfg.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
        }
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApConfiguration", wcfg.getClass());
            Boolean rt = (Boolean) method.invoke(mWifiManager, wcfg);
            Log.d(TAG, " rt = " + rt);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        }
        return setWifiApEnabled();
    }

    private boolean setWifiApEnabled() {
        while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED) {
            mWifiManager.setWifiEnabled(false);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        while (getWifiAPState() != WIFI_AP_STATE_DISABLED) {
            try {
                Method method1 = mWifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                method1.invoke(mWifiManager, null, false);
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }

        try {
            Method method1 = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method1.invoke(mWifiManager, null, true);
            int i = 0;
            while (getWifiAPState() != WIFI_AP_STATE_ENABLED) {
                Thread.sleep(100);
                i++;
                if (i > 50) {
                    break;
                }
            }
            resultOfConditionVariable();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    public void closeWifiAp() {
        if (getWifiAPState() != WIFI_AP_STATE_DISABLED) {
            try {
                Method method = mWifiManager.getClass().getMethod(
                        "getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method
                        .invoke(mWifiManager);
                Method method2 = mWifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                method2.invoke(mWifiManager, config, false);
                while (getWifiAPState() != WIFI_AP_STATE_DISABLED) {
                    Thread.sleep(100);
                }
                resultOfConditionVariable();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public int getWifiAPState() {
        int state = -1;
        try {
            Method method2 = mWifiManager.getClass()
                    .getMethod("getWifiApState");
            state = (Integer) method2.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, "getWifiAPState.state " + state);
        return state;
    }

}
