package com.sprd.autoslt.action.impl;

import android.util.Log;

import com.sprd.autoslt.SLTApplication;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.MemorySizeUtil;

public class GetMemoryInfoAction extends AbstractAction {
    private static final String TAG = "MemoryInfoAction";

    public GetMemoryInfoAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "GetMemoryInfo:" + param);
		Log.d(TAG, "GetMemoryInfo: RAM = " 
               + MemorySizeUtil.getTotalMemorySize(SLTApplication.getApplication().getApplicationContext())
               + " ROM = "+ MemorySizeUtil.getAvailableInternalMemorySize());
        end("RAM:"
                + MemorySizeUtil.formatFileSize(MemorySizeUtil
                        .getTotalMemorySize(SLTApplication.getApplication()
                                .getApplicationContext()), false)
                + "^ROM:"
                + MemorySizeUtil.formatFileSize(
                        MemorySizeUtil.getAvailableInternalMemorySize(), false));
    }

    @Override
    public void stop() {
    }
}