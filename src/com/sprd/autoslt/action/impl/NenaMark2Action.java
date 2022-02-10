package com.sprd.autoslt.action.impl;

import android.widget.Toast;
import java.io.File;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.action.AbstractAction.StatusChangedListener;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.sprd.autoslt.R;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;

public class NenaMark2Action extends AbstractAction {
    private Context mContext;
    private static final String TAG = "NenaMark2Action";
    private static final String packgename = "com.sprd.engineermode";
    private static final String ss = "has no monkey file";
    private static final String SCRIPT_BENCHMARK_start = "benchmark";
    private static final String SCRIPT_BENCHMARK_stop = "benchmark1";
    private Process mProcess = null;

    public NenaMark2Action(StatusChangedListener listener, Context context) {
        super(listener);
        mContext = context;
    }

    @Override
    public void start(String param) {

        runMonkeyScriptThread(SCRIPT_BENCHMARK_start);
        ok();
        /* slt */
        // try {
        // PackageManager pm = mContext.getPackageManager();
        // Log.d(TAG, " clickedon going get intent");
        // Intent intent = pm.getLaunchIntentForPackage(packgename);
        // if (intent != null) {
        // mContext.startActivity(intent);
        // Log.d(TAG, " startActivity");
        // }else{
        // Log.d(TAG, " intent: " +intent);
        // }
        // } catch (Exception e) {
        // Log.w(TAG, "startActivity error " + e.getMessage());
        // }

    }

    @Override
    public void stop() {
        runMonkeyScriptThread(SCRIPT_BENCHMARK_stop);
        end();
    }

    private void runMonkeyScriptThread(final String scriptName) {
        File monkeyFile = new File(SLTConstant.SLT_SDCARD_PATH + scriptName);
        Log.d(TAG, "monkeyFile = " + monkeyFile.exists());
        if (!monkeyFile.exists()) {
            Toast.makeText(
                    mContext,
                    mContext.getResources().getString(
                            R.string.has_no_monkey_file), Toast.LENGTH_LONG)
                    .show();
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                runMonkeyScript(scriptName);
            }
        }).start();
    }

    private boolean runMonkeyScript(String scripFile) {
        try {
            Log.d(TAG, "execMonkey enter");
            mProcess = new ProcessBuilder()
                    .command("monkey", "-f",
                            SLTConstant.SLT_SDCARD_PATH + scripFile, "1")
                    .redirectErrorStream(true).start();

            int exitValue = mProcess.waitFor();
            Log.d(TAG, "Process.waitFor() return " + exitValue);
            Log.e(TAG, "execMonkey exit");
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
