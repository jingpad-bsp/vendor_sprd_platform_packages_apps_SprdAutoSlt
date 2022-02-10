package com.sprd.autoslt.action.impl;

import java.io.File;

import android.os.Environment;
import android.util.Log;

import com.sprd.autoslt.SLTApplication;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.MemorySizeUtil;

public class GetFlashInfoAction extends AbstractAction {
    private static final String TAG = "GetFlashInfoAction";

    public GetFlashInfoAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "GetFlashInfoAction:" + param);
        if(isStorageMounted(new File("/storage/sdcard0/"))) {
        	Log.d(TAG, "getTotalExternalMemorySize = " +MemorySizeUtil.formatFileSize(MemorySizeUtil.getTotalExternalMemorySize(),false));      
        	Log.d(TAG, "getTotalInternalMemorySize = " +MemorySizeUtil.formatFileSize(MemorySizeUtil.getTotalInternalMemorySize(),false));
        	//end(MemorySizeUtil.formatFileSize(MemorySizeUtil.getTotalInternalMemorySize(),false));
        	end(MemorySizeUtil.formatFileSize(MemorySizeUtil.getTotalExternalMemorySize(),false));
        } else {
            error(TAG + "sdcard not exists!");
            stop();
        }
    }

    private boolean isStorageMounted(File path) {
        String state = Environment.getExternalStorageState(path);
        Log.d(TAG, "isStorageMounted:" + path + ":" + state);
        return "mounted".equals(state);
    }
    @Override
    public void stop() {
    }
}