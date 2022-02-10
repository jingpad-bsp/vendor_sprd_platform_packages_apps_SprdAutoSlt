package com.sprd.validationtools.utils;

import android.util.Log;

public class Native {

    static {
        try {
            System.loadLibrary("jni_sprdautoslt");
        } catch (UnsatisfiedLinkError e) {
            Log.d("ValidationToolsNative", " #loadLibrary jni_sprdautoslt failed  ");
            e.printStackTrace();
        }
    }

    /**
     * send AT cmd to modem 
     *
     * @return (String: the return value of send cmd, "OK":sucess, "ERROR":fail)
     */
    public static native String native_sendATCmd(int phoneId, String cmd);
	//public static native int native_dualCameraVerfication(String leftImage, String rightImage, String otpPath);

    /*SPRD bug 759782 : Display RMS value*/
    //public static native double native_getCameraVerficationRMS();
    /*@}*/
}