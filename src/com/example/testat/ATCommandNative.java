package com.example.testat;

import java.io.File;

import com.sprd.autoslt.SLTApplication;

import android.app.Activity;
import android.util.Log;

public class ATCommandNative {

    static {
        try {
            System.loadLibrary("atci");
        } catch (UnsatisfiedLinkError e) {
            Log.d("ATCommandNative", " #loadLibrary atci failed", e);
            e.printStackTrace();
        }
    }
    
    public static void reloadLib() {
        try {
            /*File dir = SLTApplication.getApplication().getDir("jniLibs", Activity.MODE_PRIVATE);
            System.load(dir.getAbsolutePath() + File.separator + "libc++.so");
            System.load(dir.getAbsolutePath() + File.separator + "librilutils.so");
            System.load(dir.getAbsolutePath() + File.separator + "libatci.so");
            System.load(dir.getAbsolutePath() + File.separator + "libjni_at.so");*/
        	System.loadLibrary("c++");
        	System.loadLibrary("rilutils");
        	System.loadLibrary("atci");      	
        	
        } catch (UnsatisfiedLinkError e) {
            Log.d("huasong", "UnsatisfiedLinkError:", e);
        }
    }

    /**
     * send AT cmd to modem
     *
     * @return (String: the return value of send cmd, "OK":sucess, "ERROR":fail)
     */
    public static native String native_sendATCmd(int phoneId, String cmd);

}
