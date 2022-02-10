package com.sprd.autoslt.action.impl;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;

public class StartKeyModeAction extends AbstractAction {
    private static final String TAG = "StartKeyModeAction";

    public StartKeyModeAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "StartKeyModeAction:" + param);
        try {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.sprd.validationtools", "com.sprd.validationtools.itemstest.TestResultActivity");
            intent.setComponent(comp);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(mContext, "current not support", Toast.LENGTH_SHORT).show();
        }
        ok();
    }

    @Override
    public void stop() {
    }
}