package com.sprd.autoslt.action.impl;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.text.TextUtils;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.FlashLightActivity;
import com.sprd.autoslt.activity.KeyTestActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.ShellUtils;

public class FlashLightModeAction extends AbstractAction {
    private static final String TAG = "FlashLightModeAction";
    public static final String CAMERA_FLASH = "/sys/devices/virtual/misc/sprd_flash/test";
    private CameraManager mCameraManager;
    private static String mCameraId;
    private static boolean mFlashlightEnabled;

    private String mType;

    public FlashLightModeAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "FlashLightModeAction:" + param + "mType:" + mType);
        if (mType.equals(SLTConstant.ACTION_TYPE_START_FLASH_LIGHT)) {
            if(TextUtils.isEmpty(param)) {
                error("status error");
                return;
            }
            if (param.equalsIgnoreCase("Front")) {
                mCameraId = "1";
            }else if (param.equalsIgnoreCase("Back")) {
                mCameraId = "0";
            }
            /*openFlashLightByCameraId(Integer.parseInt(mCameraId));
            ok();*/
            setFlashlight(true);
            if (mFlashlightEnabled) {
                ok();
            }else {
                end("open flash fail");
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_END_FLASH_LIGHT)) {
            if (TextUtils.isEmpty(param)) {
                error("status error");
                return;
            }
            if (param.equalsIgnoreCase("Front")) {
                mCameraId = "1";
            } else if (param.equalsIgnoreCase("Back")) {
                mCameraId = "0";
            }
            /*closeFlashLight(Integer.parseInt(mCameraId));
            ok();*/     
            setFlashlight(false);
            if (!mFlashlightEnabled) {
                ok();
            }else {
                end("close flash fail");
            }
        }
    }

    @Override
    public void stop() {
    }

    public void setFlashlight(boolean enabled) {
        Log.d(TAG, "enabled = "+enabled);
        if (mFlashlightEnabled != enabled) {
            mFlashlightEnabled = enabled;
            try {
                Log.d(TAG, "mCameraId = " +mCameraId);
                mCameraManager.setTorchMode(mCameraId, enabled);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Couldn't set torch mode", e);
                mFlashlightEnabled = false;
            }
        }     
    }
}