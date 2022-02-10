package com.sprd.autoslt;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sprd.autoslt.common.SLTConstant;

public class SLTReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("MMLog", "------------------");
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Intent sltIntent = new Intent();
            sltIntent.setClass(context, SLTService.class);
            sltIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(sltIntent);

            //on keybaoad project, run wakeup scirpt to wake up the phone
            runMonkeyScriptThread(context, "wakeup");

            Log.d("MMLog", "!!");

            return;
        }
        Uri uri = intent.getData();
        Log.d("huasong", "onReceive uri:" + uri);
        if(uri == null) return ;
        String host = uri.getHost();

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d("huasong", "onReceive host:" + host);

        if("007".equals(host)){
            i.setClass(context, SLTActivity.class);
            context.startActivity(i);
        }
    }

    private void runMonkeyScriptThread(Context context, final String scriptName) {
        File monkeyFile = new File(SLTConstant.SLT_SDCARD_PATH + scriptName);
        Log.d("MMLog", "monkeyFile = " + monkeyFile.exists());
        if (!monkeyFile.exists()) {
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                runMonkeyScript(scriptName);
            }
        }).start();
    }

    private boolean runMonkeyScript(String scripFile) {
        Process mProcess = null;
        try {
            Log.d("MMLog", "execMonkey enter");
            mProcess = new ProcessBuilder()
                    .command("monkey", "-f",
                            SLTConstant.SLT_SDCARD_PATH + scripFile, "1")
                    .redirectErrorStream(true).start();

            int exitValue = mProcess.waitFor();
            Log.d("MMLog", "Process.waitFor() return " + exitValue);
            Log.e("MMLog", "execMonkey exit");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                mProcess.destroy();
                mProcess = null;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return true;
    }


}
