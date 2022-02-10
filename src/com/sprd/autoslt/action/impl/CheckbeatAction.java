package com.sprd.autoslt.action.impl;

import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;

public class CheckbeatAction extends AbstractAction {
    private static final String TAG = "CheckbeatAction";

    public CheckbeatAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "CheckbeatAction:" + param);
        ok();
    }

    @Override
    public void stop() {
    }
}