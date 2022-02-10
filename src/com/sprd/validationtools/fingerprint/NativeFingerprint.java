package com.sprd.validationtools.fingerprint;

import android.util.Log;

public class NativeFingerprint {

    static {
        //Maybe cause remote UnsatisfiedLinkError.
        try {
            System.loadLibrary("jni_sprdautofingerprint");
        } catch (UnsatisfiedLinkError e) {
            Log.d("NativeFingerprint", " #loadLibrary jni_sprdautofingerprint failed  ");
            e.printStackTrace();
        }
    }

    static native public int factory_init();

    static native public int factory_exit();

    static native public int spi_test();

    static native public int deadpixel_test();

    static native public int interrupt_test();

    static native public int finger_detect();

}

