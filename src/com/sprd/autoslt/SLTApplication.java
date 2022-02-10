package com.sprd.autoslt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class SLTApplication extends Application {

    private static SLTApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        //copySoAync();
    }

    /*private void copySoAync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = mApplication.getDir("jniLibs", Activity.MODE_PRIVATE);
                String[] list = null;
                try {
                    list = mApplication.getAssets().list("");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Log.d("huasong", "list:" + (list == null));
                Log.d("huasong", "list.length:" + list.length);
                int i = 0;
                for(String file : list) {
                    if(TextUtils.isEmpty(file) || !file.contains("lib")) continue;
                    File distFile = new File(dir.getAbsolutePath() + File.separator + new File(file).getName());
                    if (copyFileFromAssets(mApplication.getApplicationContext(), new File(file).getName(), distFile.getAbsolutePath())){
                        Log.d("huasong", "new File(file).getName():" + new File(file).getName() + " distFile.getAbsolutePath():" + distFile.getAbsolutePath());
                        Log.d("huasong", "distFile.exists():" + distFile.exists());
                        Log.d("huasong", "i:" + i);
                        i++;
                    }
                }
                try {
                    System.load(dir.getAbsolutePath() + File.separator + "libc++.so");
                    System.load(dir.getAbsolutePath() + File.separator + "librilutils.so");
                    System.load(dir.getAbsolutePath() + File.separator + "libatci.so");
                    System.load(dir.getAbsolutePath() + File.separator + "libjni_at.so");
                    System.load(dir.getAbsolutePath() + File.separator + "libfmjni.so");
                } catch (UnsatisfiedLinkError e) {
                    Log.d("huasong", "UnsatisfiedLinkError:", e);
                }
                Log.d("huasong", "fileName.exists():" + dir.exists());
            }
        }).start();
    }

    public static boolean copyFileFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("huasong", "[copyFileFromAssets] IOException ", e);
        }
        return copyIsFinish;
    }*/

    public static final Application getApplication() {
        return mApplication;
    }
}
