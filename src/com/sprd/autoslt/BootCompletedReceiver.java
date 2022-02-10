package com.sprd.autoslt;

import com.sprd.autoslt.sqlite.EngSqlite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.sprd.autoslt.camera.Util;
import android.os.SystemProperties;

public class BootCompletedReceiver extends BroadcastReceiver{
    private static String TAG = "BootCompletedReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    private static final String BOOT_MODE = "ro.bootmode";
    private static final String BOOT_MODE_UPT = "upt_mode";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "action = " + intent.getAction());
        if(intent.getAction() != null && intent.getAction().equals(ACTION_BOOT)){
            String bootmode = SystemProperties.get(BOOT_MODE, "unknow");
            Log.d(TAG, "onReceive bootmode = " + bootmode);
            if(bootmode != null && bootmode.equals(BOOT_MODE_UPT)){
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClass(context, SLTActivity.class);
                context.startActivity(i);
                return;
            }
        }
    }
}
