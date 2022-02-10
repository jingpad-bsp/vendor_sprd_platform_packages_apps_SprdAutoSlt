package com.sprd.autoslt.action.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class TeleNetwork2GAction extends TeleNetworkAction {

    private static final String TAG = "SLTTeleNetwork2GAction";

    public TeleNetwork2GAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            Log.e(TAG, "start() failed -> phone number is empty!");
            error(TAG + " start() failed -> phone number is empty!");
            return;
        }

        mIsStop = false;
        mPhoneNumber = phoneNumber;
        Log.d(TAG, "sdfa");
        // switch network to 2G when it is not.
        switchTeleNetwork(NETWORK_2G);
    }

    @Override
    public void stop() {
        mIsStop = true;
        Log.d(TAG,"stop 2G ");
        endCall();
        end();
    }
}
