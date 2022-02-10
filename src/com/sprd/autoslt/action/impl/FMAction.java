package com.sprd.autoslt.action.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.fmradio.FmConstants.AudioPath;
import com.android.fmradio.FmManagerSelect;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import android.media.AudioSystem;

public class FMAction extends AbstractAction {

    private static final String TAG = "FMAction";
    private static volatile FMAction instance;
    private static String mType;
    private FmManagerSelect mFmManager = null;
    private AudioManager mAudioManager = null;
    private boolean isOpen = false;
    private float freq = 0;
    private boolean mIsHeadsetIn;

    public FMAction(StatusChangedListener listener) {
        super(listener);
        mFmManager = new FmManagerSelect(mContext);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    private FMAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mFmManager = new FmManagerSelect(mContext);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        mContext.registerReceiver(earphonePluginReceiver, filter);
    }

    public static FMAction getInstance(StatusChangedListener listener, String type) {
        mType = type;
        //use lazy singleinstance
        if(instance == null) {
            synchronized (FMAction.class) {
                if(null == instance) {
                    instance = new FMAction(listener, type);
                }
            }
        }
        return instance;
    }

    private BroadcastReceiver earphonePluginReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent earphoneIntent) {
            if (earphoneIntent != null && earphoneIntent.getAction() != null) {
                Log.i(TAG,
                        "earphonePluginReceiver action : "
                                + earphoneIntent.getAction());
                if (earphoneIntent.getAction().equalsIgnoreCase(
                        Intent.ACTION_HEADSET_PLUG)) {
                    int st = 0;
                    st = earphoneIntent.getIntExtra("state", 0);
                    int deviceId = earphoneIntent.getIntExtra("microphone", 0);
                    Log.d(TAG, "microphone = " + deviceId);
                    if (st > 0) {
                        mIsHeadsetIn = true;
                    } else {
                        mIsHeadsetIn = false;
                        error("headset not exit!");
                        Log.d(TAG, "earphonePluginReceiver headset not exist!");
                    }
                }
            }
        }
    };

    @Override
    public void start(String param) {
        if(mType.equals(SLTConstant.ACTION_TYPE_START_FM)) {
            if (TextUtils.isEmpty(param)) {
                Log.e(TAG, " start() failed -> param is empty!");
                error("param invalid!");
                return;
            }
            if(!SLTUtil.isHeadSetIn()) {
                error("headset not exist!");
                Log.d(TAG, "headset not exist!");
                return;
            }
            Log.d(TAG, "FmManager power up and setFreq() : " + freq);
            freq = -1;
            try {
                freq = Float.parseFloat(param);
                if(!isOpen) {
                    startPowerUpFm();
                }
                mFmManager.tuneRadio(freq);
                ok();
            } catch (NumberFormatException e) {
                Log.e(TAG, "start param is error : " + param);
                error(TAG + " start() failed -> param is error : " + param);
                return;
            }

            Log.d(TAG, "FmManager power up and setFreq() : " + freq);
        
        } else if(mType.equals(SLTConstant.ACTION_TYPE_GET_FM_RSSI)) {
            error("Not support!");
        } else if(mType.equals(SLTConstant.ACTION_TYPE_END_FM)) {
            if (isOpen) {
				powerOffFM();
			}
            mFmManager.setAudioPathEnable(AudioPath.FM_AUDIO_PATH_NONE, false);
            try {
                mContext.unregisterReceiver(earphonePluginReceiver);
                Log.d(TAG, "unregister receiver...");
            } catch (Exception e) {
                Log.d(TAG, "unregisterreceiver Exception:", e);
            }
            if (instance != null) {
            	instance = null;
			}
            ok();
        } else {
            error("cmd invalid!");
        }
    }
    private void startPowerUpFm() {
        new StartPowerUpThread().start();
    }

    class StartPowerUpThread extends Thread {
        public void run() {
            startPowerUp();
        };
    };

    private void startPowerUp() {
        Log.d(TAG, "startPowerUp");
        boolean value = false;
        try {
            mFmManager.setMute(true);
        } catch (UnsatisfiedLinkError e) {
            Log.d("huasong", "UnsatisfiedLinkError:", e);
            error("load lib error");
            return;
        }
        value = mFmManager.openDev();
        if (!value) {
            isOpen = false;
            Log.e(TAG, "openDev fail ");
            error("openDev fail");
            return;
        }
        value = mFmManager.powerUp(freq);
        if (!value) {
            isOpen = false;
            Log.e(TAG, "powerUp fail ");
            error("powerUp fail");
            return;
        }
        isOpen = true;
        Log.d(TAG, "sendMessage MSG_POWER_UP");
        startRender();
    }

    private synchronized void startRender() {
        Log.d("huasong", "startRender ");
        mFmManager.setMute(false);
        mFmManager.setAudioPathEnable(AudioPath.FM_AUDIO_PATH_HEADSET, true);
        AudioSystem.setForceUse(AudioSystem.FOR_FM, AudioSystem.FORCE_NONE);
        //int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int volume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d("huasong", "startRender volume:" + volume);
        mAudioManager.setParameter("FM_Volume", "" + volume);
    }

    public void powerOffFM() {
        Log.d(TAG,"power off fm device");
        mFmManager.setMute(true);
        mFmManager.setRdsMode(false, false);
        mFmManager.powerDown();
        mFmManager.closeDev();
        isOpen = false;   
    }

    @Override
    public void stop() {
    }

}
