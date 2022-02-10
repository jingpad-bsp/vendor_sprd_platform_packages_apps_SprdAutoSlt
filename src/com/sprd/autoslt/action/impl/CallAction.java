package com.sprd.autoslt.action.impl;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telecom.TelecomManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import android.telephony.PreciseCallState;

public class CallAction extends AbstractAction {
    private static final String TAG = "CallAction";
    private static CallAction instance;
    private static String mType;
    private static TelephonyManager mTelephonyManager;
    private static ActivityManager mActivityManager;
    private static PackageManager mPackageManager;
    private static final String INCALL_UI = "com.android.incallui";
    private static final String INCALL_UI_ACTIVITY = "com.android.incallui.InCallActivity";
    private boolean mIsActive = false;
    private boolean isEmergencyNum = false;
    private static  TelecomManager tm;

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onDataConnectionStateChanged(int state) {
        }

        @Override
        public void onDataActivity(int direction) {
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d(TAG, "onPreciseCallStateChanged state: " + state + " incomingNumber:" + incomingNumber);
        }

        @Override
        public void onPreciseCallStateChanged(PreciseCallState preciseState) {
            Log.d(TAG, "onPreciseCallStateChanged: " + preciseState.toString());
            if(preciseState.getForegroundCallState() == PreciseCallState.PRECISE_CALL_STATE_ACTIVE) {
                mIsActive = true;
            } else {
                mIsActive = false;
            }
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
        }

        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
        }

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> arrayCi) {
            Log.d(TAG, "onCellInfoChanged: arrayCi=" + arrayCi);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Log.d(TAG, "onSignalStrengthChanged: SignalStrength=" +signalStrength);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.d(TAG, "onServiceStateChanged: ServiceState=" + serviceState);
        }
    };

    public CallAction(StatusChangedListener listener) {
        super(listener);
    }

    private CallAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = mContext.getPackageManager();
        mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE
              //b/27803938 - RadioInfo currently cannot read PRECISE_CALL_STATE
              | PhoneStateListener.LISTEN_PRECISE_CALL_STATE
              | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
              | PhoneStateListener.LISTEN_DATA_ACTIVITY
              | PhoneStateListener.LISTEN_CELL_LOCATION
              | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
              | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
              | PhoneStateListener.LISTEN_CELL_INFO
              | PhoneStateListener.LISTEN_SERVICE_STATE
              | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        tm = (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
    }

    public static CallAction getInstance(StatusChangedListener listener,
            String type) {
        if (instance == null) {
            instance = new CallAction(listener, type);
        }
        mType = type;
        return instance;
    }

    private boolean isEmergencyNumber(String phoneNumber) {
        boolean isEmergency = phoneNumber.equals("112")
                || phoneNumber.equals("110") || phoneNumber.equals("119")
                || phoneNumber.equals("120") || phoneNumber.equals("122");
        return isEmergency;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "CallAction type:" + mType + " param:" + param);
        if(mType.equals(SLTConstant.ACTION_TYPE_START_MAKE_CALL)) {
            if(TextUtils.isEmpty(param) || SLTUtil.getTopActivity(mContext).toString().contains(INCALL_UI_ACTIVITY)) {
                error("invalid param!");
                return;
            }

            String[] parameters = SLTUtil.parseParam(param);
            String simId = "0";
            String phoneNumber = parameters[0];
            if (parameters.length > 1) {
                simId = parameters[1];
            }

            if (isEmergencyNumber(phoneNumber)) {
                isEmergencyNum = true;
            } else {
                isEmergencyNum = false;
                if (!isNetworkInService()) {
                    error("out of service!");
                    return;
                }

                if (!isSpecifiedSimNetworkInService(Integer.parseInt(simId))) {
                    error("out of service!");
                    return;
                }
            }
            /*if(!isNetworkInService()) {
                error("out of service!");
                return;
            }*/
            mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_CALL_STATE
                    //b/27803938 - RadioInfo currently cannot read PRECISE_CALL_STATE
                    | PhoneStateListener.LISTEN_PRECISE_CALL_STATE
                    | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                    | PhoneStateListener.LISTEN_DATA_ACTIVITY
                    | PhoneStateListener.LISTEN_CELL_LOCATION
                    | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                    | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                    | PhoneStateListener.LISTEN_CELL_INFO
                    | PhoneStateListener.LISTEN_SERVICE_STATE
                    | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", Uri.parse("tel:" + phoneNumber));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("factory_mode", true);
            if (isEmergencyNum) {
                intent.putExtra("SimCard", 0);
            } else if(!TextUtils.isEmpty(simId) && TextUtils.isDigitsOnly(simId)){
                intent.putExtra("SimCard", Integer.valueOf(simId));
            } else {
                if (isSpecifiedSimNetworkInService(0)) {
                    intent.putExtra("SimCard", 0);
                } else {
                    if (isSpecifiedSimNetworkInService(1)) {
                        intent.putExtra("SimCard", 1);
                    }
                }
            }
            mContext.startActivity(intent);
            mIsActive = false;
            ok();
        } else if(mType.equals(SLTConstant.ACTION_TYPE_CHECK_CALL_STATUS)) {
            if(mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                Log.d(TAG, "call state idle...");
                end("idle");
            } else if(mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                Log.d(TAG, "call state offhook...");
                if(mIsActive) {
                    end("active");
                } else {
                    end("offhook");
                }
            } else if(mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                Log.d(TAG, "call state ringing...");
                end("ringing");
            }
        } else if(mType.equals(SLTConstant.ACTION_TYPE_END_CALL)) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            Log.d(TAG, "call mType=" + mType);
            tm.endCall();
            ok();
        } else {
            error("invalid cmd!");
        }
    }

    protected boolean isSpecifiedSimNetworkInService(int simId) {
        ServiceState state = mTelephonyManager.getServiceStateForSubscriber(
                SubscriptionManager.getSubId(simId)[0]);
        Log.d(TAG, "isSpecifiedSimNetworkInService, simId=:" + simId);
        if (state != null && state.getState() == ServiceState.STATE_IN_SERVICE) {
            return true;
        }
        return false;
    }

    protected boolean isNetworkInService() {
        int phoneCount = TelephonyManager.from(mContext).getPhoneCount();
        Log.d(TAG, "isNetworkInService phoneCount=" + phoneCount);
        for(int phoneId = 0; phoneId < phoneCount; phoneId++){
            ServiceState ss = mTelephonyManager
                    .getServiceStateForSubscriber(SubscriptionManager.getSubId(phoneId)[0]);
            if(ss != null && ss.getState() == ServiceState.STATE_IN_SERVICE){
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    @Override
    public void stop() {
    }
}