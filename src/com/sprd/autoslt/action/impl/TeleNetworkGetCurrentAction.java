package com.sprd.autoslt.action.impl;

import android.util.Log;

public class TeleNetworkGetCurrentAction extends TeleNetworkAction {

    private static final String TAG = "TeleNetworkGetCurrentAction";

    public TeleNetworkGetCurrentAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String param) {
        Log.d(TAG,"1111222222");
    }

    @Override
    public void stop() {
        // nothing to do
    }

}
