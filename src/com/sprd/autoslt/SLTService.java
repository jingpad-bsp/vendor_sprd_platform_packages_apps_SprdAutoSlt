package com.sprd.autoslt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.sprd.autoslt.R;
import com.sprd.autoslt.action.AbstractBackGroundAction;
import com.sprd.autoslt.action.AbstractBackGroundAction.BackStatusChangedListener;
import com.sprd.autoslt.common.SLTConstant;

public class SLTService extends Service {

    private static final String TAG = "MMLogSLTService";
    private SLTManager mManager;
    private ServiceBinder serviceBinder = new ServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = new SLTManager(this);

        mManager.restart();
        //create shutdown thread
        new Thread(new Runnable() {
            public void run() {
                int time = getTime();
                if (time == 0) return;
                    try {
                        Thread.sleep(time * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent shutdownIntent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    shutdownIntent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    shutdownIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    SLTService.this.startActivity(shutdownIntent);
            }
        }).start();
    }

    public void setBackStatusChangedListener(BackStatusChangedListener listener){
        if(mManager != null){
            mManager.setBackStatusChangedListener(listener);
        }
    }

    public ArrayList<AbstractBackGroundAction> getAllBackGroundAction(){
        if(mManager != null){
            return mManager.getAllBackGroundAction();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mManager.isStoped()) {
            mManager.restart();
            Toast.makeText(this, "onStartCommand mManager restart...",
                    Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "SLTService onDestroy...", Toast.LENGTH_SHORT)
                .show();
    }

    private int getTime() {
        int freq = 0;
            String readString = null;
            File storefile = new File(SLTConstant.SLT_SDCARD_PATH + "shutdown_time");
            FileInputStream fis = null;
            Log.e("MMLog", "freq:"+freq);
            try {
                if (!storefile.exists()) {
                    Log.e("MMLog", "file not exsits");
                } else {
                    fis = new FileInputStream(storefile);
                    byte[] buf = new byte[1024];
                    StringBuffer sb = new StringBuffer();
                    while((fis.read(buf)) != -1) {
                        sb.append(new String(buf));
                    }
                    readString = sb.toString();
                    if (readString.equals("nofm") || readString.length() < 4) {
                        readString = null;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                    Log.e("MMLog", "1", e);
            } catch (IOException e) {
                    Log.e("MMLog", "2", e);
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (readString != null) {
                try {
                    Log.e("MMLog", "readString.trim()"+readString.trim());
                    freq = Integer.parseInt(readString.trim());
                } catch (Exception e) {
                    storefile.delete();
                    freq = 0;
                }
            }
        Log.e("MMLog", "freq:"+freq);
        return freq;
    }
    class ServiceBinder extends Binder {
        public SLTService getService() {
            return SLTService.this;
        }
    }

    public void setMainHandler(Handler handler) {
        mManager.setMainHandler(handler);
    }
}
