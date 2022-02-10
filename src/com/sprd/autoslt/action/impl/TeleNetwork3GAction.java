package com.sprd.autoslt.action.impl;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class TeleNetwork3GAction extends TeleNetworkAction {

    private static final String TAG = "SLTTeleNetwork3GAction";

    public TeleNetwork3GAction(StatusChangedListener listener) {
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

        // switch network to 3G when it is not.
        switchTeleNetwork(NETWORK_3G);

    }

    @Override
    public void stop() {
        mIsStop = true;
        Log.d(TAG,"stop 3G ");
        endCall();
        end();
    }
}
