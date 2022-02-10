package com.sprd.autoslt.action.impl;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class TeleNetwork4GAction extends TeleNetworkAction {

    private static final String TAG = "SLTTeleNetwork4GAction";

    public TeleNetwork4GAction(StatusChangedListener listener) {
        super(listener);
        Log.d("huasong", "TeleNetwork4GAction!!!!!!!");
    }

    @Override
    public void start(String phoneNumber) {
        Log.d("huasong", "start4G!!!!!!!");
        if (TextUtils.isEmpty(phoneNumber)) {
            Log.e(TAG, "start() failed -> phone number is empty!");
            error(TAG + " start() failed -> phone number is empty!");
            return;
        }
        mIsStop = false;
        mPhoneNumber = phoneNumber;

        Log.d("huasong", "switch tele network");
        // switch network to 4G when it is not.
        switchTeleNetwork(NETWORK_4G);
    }

    @Override
    public void stop() {
        mIsStop = true;
        end();
        endCall();
    }
}
