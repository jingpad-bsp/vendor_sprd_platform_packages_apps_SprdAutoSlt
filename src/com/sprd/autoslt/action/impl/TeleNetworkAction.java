package com.sprd.autoslt.action.impl;

import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import java.util.List;

import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;

import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.TelephonyIntents;

import android.os.ServiceManager;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.telephony.ServiceState;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.TelephonyManagerSprd;

import com.sprd.autoslt.R;

import android.os.SystemProperties;

public abstract class TeleNetworkAction extends AbstractAction {

    private static final String TAG = "SLTTeleNetworkAction";

    protected static final int NETWORK_UNKNOW = 0;
    protected static final int NETWORK_2G = 1;
    protected static final int NETWORK_3G = 2;
    protected static final int NETWORK_4G = 3;
    protected static final int NETWORK_5G = 4;

    /** @hide */
    public static final int MODEM_TYPE_GSM = 10; // GSM ONLY
    /** @hide */
    public static final int MODEM_TYPE_TDSCDMA = 12; // TD ONLY
    /** @hide */
    public static final int MODEM_TYPE_WCDMA = 11; // WCDMA ONLY
    /** @hide */
    public static final int MODEM_TYPE_LTE_TD = 1; // TD-LTE ONLY
    /** @hide */
    public static final int MODEM_TYPE_LTE_FDD = 2; // FDD-LTE ONLY
    /** @hide */
    public static final int MODEM_TYPE_OTHERS = -1;

    public int target_network;

    private static final String RE_SET_MODE_AT = "AT+RESET=1";
    private static final String ENG_AT_NETMODE = "AT^SYSCONFIG=";
    private static final String SERVER_NAME = "atchannel";

    private TelephonyManagerSprd mTelephonyManager;
    private TelephonyManager sTelephonyManager;

    private int mNetworkType;
    protected boolean mIsStop = false;
    protected String mPhoneNumber;

    private static final int START_CALL = 1;
    private static final int CHECK_NETWORK_STATUS = 2;
    private static final int CHECK_END = 3;

    private int mCurrentRadioFeatures;
    protected int mCheckCount = 0;

    private static final String CALL_PACKAGENAME = "com.android.dialer";
    ActivityManager mActivityManager;

    protected Handler mNetworkHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case START_CALL:
                Log.d(TAG, "call phone number : " + mPhoneNumber);
                //startCall(mPhoneNumber);

                if(SystemProperties.get("persist.radio.engtest.enable").contains("true")){
                    SystemProperties.set("persist.radio.engtest.enable", "false");
                    Log.d(TAG, "get vSystemProperties is " +SystemProperties.get("persist.radio.engtest.enable"));
                }
                break;
            case CHECK_NETWORK_STATUS:
                Log.d("huasong", "check status count:" + mCheckCount);
                if (isNetworkInService()) {
                    sendEmptyMessageDelayed(START_CALL, 2000);
                    Log.d("huasong", "isNetworkInService:" + isNetworkInService());
                } else {
                    if (mCheckCount++ < 40 && !mIsStop) {
                        sendEmptyMessageDelayed(CHECK_NETWORK_STATUS, 500);
                    } else {
                        Log.e(TAG, "switch network failed");

                        if(SystemProperties.get("persist.radio.engtest.enable").contains("true")){
                            SystemProperties.set("persist.radio.engtest.enable", "false");
                            Log.d(TAG, "get vSystemProperties is " +SystemProperties.get("persist.radio.engtest.enable"));
                        }
                        return;
                    }
                }
                break;
            case CHECK_END:
                killCallApp();
                break;
            }
        };
    };

    public TeleNetworkAction(StatusChangedListener listener) {
        super(listener);
        Method method;
        mTelephonyManager = new TelephonyManagerSprd(mContext);
        mCurrentRadioFeatures = mTelephonyManager.getPreferredNetworkType();
        sTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mActivityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
    }

    protected void startCall(String phoneNumber) {
        Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", Uri.parse("tel:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("factory_mode", true);
        mContext.startActivity(intent);
    }

    protected void endCall() {
        TelecomManager mTelecomManager = (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        mTelecomManager.endCall();
//        try {
//            Method method = TelephonyManager.class.getDeclaredMethod(
//                    "getITelephony", (Class[]) null);
//            method.setAccessible(true);
//            ITelephony iTelephony = (ITelephony) method.invoke(
//                    mTelephonyManager, (Object[]) null);
//            iTelephony.endCall();
//        } catch (Exception e) {
//            Log.e(TAG, "end call find Exception : " + e.getMessage());
//            e.printStackTrace();
//        }
        mNetworkHandler.sendEmptyMessageDelayed(CHECK_END, 1000);
    }

    // switch telephony network to 2G/3G/4G
    protected void switchTeleNetwork(int networkType) {
        if(!hasSimCard()) {
            return;
        }
        if(!SystemProperties.get("persist.radio.engtest.enable").contains("true")){
            SystemProperties.set("persist.radio.engtest.enable", "true");
            Log.d(TAG, "get vSystemProperties is " +SystemProperties.get("persist.radio.engtest.enable"));
        }
        mNetworkType = networkType;
        mCheckCount = 0;
        Log.d("huasong", "switchTeleNetwork mNetworkType:" + mNetworkType);
        switch (mNetworkType) {
        case NETWORK_2G:
            if (getTeleNetwork() == NETWORK_2G && isNetworkInService()) {
                mNetworkHandler.sendEmptyMessage(START_CALL);
                Log.d("huasong", "switchTeleNetwork START_CALL:");
            } else {
                Log.d("huasong", "switching gsm mode...");
                mTelephonyManager.setPreferredNetworkType(TelephonyManagerSprd.NT_GSM);
                mNetworkHandler.sendEmptyMessageDelayed(CHECK_NETWORK_STATUS, 2000);
                Toast.makeText(mContext, "switching gsm mode...", Toast.LENGTH_SHORT).show();
            }
            break;
        case NETWORK_3G:
            if (getTeleNetwork() == NETWORK_3G && isNetworkInService()) {
                Log.d("huasong", "isNetworkInService:" + isNetworkInService());
                mNetworkHandler.sendEmptyMessage(START_CALL);
            } else {
                mTelephonyManager.setPreferredNetworkType(TelephonyManagerSprd.NT_WCDMA);
                mNetworkHandler.sendEmptyMessageDelayed(CHECK_NETWORK_STATUS, 1000);
                Toast.makeText(mContext, "switching wcdma mode...", Toast.LENGTH_SHORT).show();
                Log.d("huasong", "isNetworkInService:" + isNetworkInService());
            }
            break;
        case NETWORK_4G:
            if (getTeleNetwork() == NETWORK_4G && isNetworkInService()) {
                mNetworkHandler.sendEmptyMessage(START_CALL);
            } else {
                mTelephonyManager.setPreferredNetworkType(TelephonyManagerSprd.NT_LTE_FDD_TD_LTE);
                mNetworkHandler.sendEmptyMessageDelayed(CHECK_NETWORK_STATUS, 1000);
                Toast.makeText(mContext, "switching lte mode...", Toast.LENGTH_SHORT).show();
            }
            break;
        default:
            break;

        }
        Log.d("huasong", "target_network:" + target_network);
    }

    protected void switchPreferredNetworkType(int mSubId, int networkType) {
        if(!hasSimCard()) {
            return;
        }
        mNetworkType = networkType;
        mCheckCount = 0;
        Log.d(TAG, "switchPreferredNetworkType mNetworkType:" + mNetworkType + ",mSubId=" + mSubId);
        boolean isSucceed = sTelephonyManager.setPreferredNetworkType(mSubId, networkType);
        Log.d(TAG, "setPreferredNetworkType isSucceed:" + isSucceed);
        mNetworkHandler.sendEmptyMessageDelayed(CHECK_NETWORK_STATUS, 1000);
        Log.d(TAG, "switchPreferredNetworkType target_network:" + target_network);
    }

    protected boolean isNetworkInService() {
        ServiceState ss = sTelephonyManager
                .getServiceStateForSubscriber(SubscriptionManager.getSubId(0)[0]);
        if (ss == null) {
            ss = sTelephonyManager
                    .getServiceStateForSubscriber(SubscriptionManager
                            .getSubId(1)[0]);
        }
        if (ss == null)
            return false;
        return ss.getState() == ServiceState.STATE_IN_SERVICE;
    }
    
    protected int getTeleNetwork() {
        mCurrentRadioFeatures = sTelephonyManager.getNetworkType();
        Log.d("huasong", "getTeleNetwork::" + sTelephonyManager.getNetworkClass(mCurrentRadioFeatures));
        return sTelephonyManager.getNetworkClass(mCurrentRadioFeatures);
    }

    private boolean hasSimCard() {
        Log.d(TAG, "hasSimCard founction, phone count = "
                + TelephonyManager.from(mContext).getPhoneCount());
        for (int i = 0; i < TelephonyManager.from(mContext).getPhoneCount(); i++) {
            if (TelephonyManager.from(mContext).getSimState(i) == TelephonyManager.SIM_STATE_READY) {
                Log.d("huasong", "hasSimCard return true");
                return true;
            }
        }
        Toast.makeText(mContext,
                mContext.getResources().getString(R.string.has_no_simcard),
                Toast.LENGTH_LONG).show();
        Log.d("huasong", "hasSimCard return false");
        return false;
    }
    
    

    protected void killCallApp() {
        if (isCallProcessRunning()) {
            mActivityManager.forceStopPackage(CALL_PACKAGENAME);
            Log.d("huasong", "forceStopPackage:" + CALL_PACKAGENAME);
        }
    }

    private boolean isCallProcessRunning() {
        List<RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            return runningTaskInfos.get(0).topActivity.toString().contains(CALL_PACKAGENAME);
        }
        return false;
    }
}
