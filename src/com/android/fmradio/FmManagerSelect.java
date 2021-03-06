package com.android.fmradio;

import com.android.fmradio.FmConstants.*;
//import com.android.fmradio.FmConstantsForBrcm.*;

import android.util.Log;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.content.Context;
import android.media.AudioSystem;

//import com.broadcom.fm.fmreceiver.FmProxy;

public class FmManagerSelect {
    private final Context mContext;
    // private FmManagerForBrcm mFmManagerForBrcm = null;

    private static final String LOGTAG = "FmManagerSelect";
    // add for universe_ui_support
    // private static boolean mIsUseBrcmFmChip =
    // false;//SystemProperties.getBoolean("ro.fm.chip.port.UART.androidm",
    // false);

    private AudioManager mAudioManager = null;

    public FmManagerSelect(Context context) {
        mContext = context;

        mAudioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean powerUp(float frequency) {
        return FmNative.powerUp(frequency);
    }

    public boolean powerDown() {
        return FmNative.powerDown(0);
    }

    public boolean openDev() {
        return FmNative.openDev();
    }

    public float seek(float frequency, boolean isUp) {

        return FmNative.seek(frequency, isUp);

    }

    public boolean closeDev() {
        return FmNative.closeDev();
    }

    public boolean stopScan() {

        return FmNative.stopScan();

    }

    public int[] autoScan(int start_freq) {
        short[] stationsInShort = null;
        int[] stations = null;
        stationsInShort = FmNative.autoScan(start_freq);
        if (null != stationsInShort) {
            int size = stationsInShort.length;
            stations = new int[size];
            for (int i = 0; i < size; i++) {
                stations[i] = stationsInShort[i];
            }
        }
        return stations;

    }

    public boolean tuneRadio(float frequency) {
        return FmNative.tune(frequency);

    }

    public boolean tuneRadioAgain(float frequency) {
        return true;

    }

    public int getFreq() {
        return -1;// mFmManager.getFreq();
    }

    public boolean setVolume(int volume) {
        return false;
    }

    public int setMute(boolean isMute) {
        return FmNative.setMute(isMute);
    }

    public boolean setAudioPathEnable(AudioPath path, boolean enable) {

        // Disable for BUILD
        if (enable) {
            AudioSystem.setDeviceConnectionState(
                    AudioManager.DEVICE_OUT_FM_HEADSET,
                    AudioSystem.DEVICE_STATE_AVAILABLE, "", "",
                    AudioSystem.AUDIO_FORMAT_DEFAULT);
            return true;
        } else {
            AudioSystem.setDeviceConnectionState(
                    AudioManager.DEVICE_OUT_FM_HEADSET,
                    AudioSystem.DEVICE_STATE_UNAVAILABLE, "", "",
                    AudioSystem.AUDIO_FORMAT_DEFAULT);
            return true;
        }
    }

    /*
     * SPRD,change for Bug641845Change setForceUse to setFmSpeakerOn,audio
     * implement this interface for error processing when app setForceUse and
     * quit abnormally.Parameter: true - speaker,false - headset
     */
    public boolean setSpeakerEnable(AudioPath path, boolean isSpeaker) {

        if (isSpeaker) {
            AudioSystem.setForceUse(AudioSystem.FOR_FM,
                    AudioSystem.FORCE_SPEAKER);
            mAudioManager.setFmSpeakerOn(true);
        } else {
            AudioSystem.setForceUse(AudioSystem.FOR_FM, AudioSystem.FORCE_NONE);
            mAudioManager.setFmSpeakerOn(false);
        }
        return true;

    }

    // add begain for new feature RDS bug-448080
    public int setRdsMode(boolean rdsMode, boolean enableAf) {
        return FmNative.setRds(rdsMode);

    }

    public int isRdsSupported() {

        return FmNative.isRdsSupport();
    }

    // add end;

    private int convertAudioPathForSprd(AudioPath path) {
        if (path == AudioPath.FM_AUDIO_PATH_HEADSET) {
            return AudioManager.DEVICE_OUT_FM_HEADSET;
        } else if (path == AudioPath.FM_AUDIO_PATH_NONE) {
            return AudioManager.DEVICE_OUT_FM_HEADSET;
        } else {
            return AudioManager.DEVICE_OUT_FM_HEADSET;
        }
    }

    public boolean isFmOn() {
        return false;// mFmManager.isFmOn();
    }

    public boolean setStepType(StepType type) {

        return false;// mFmManager.setStepType(convertStepTypeForSprd(type));

    }

    public boolean isUseBrcmFmChip() {
        return false;
    }

    /*
     * private FmStepType convertStepTypeForSprd(StepType type) { if (type ==
     * StepType.FM_STEP_50KHZ) { return FmStepType.FM_STEP_50KHZ; } else if
     * (type == StepType.FM_STEP_100KHZ) { return FmStepType.FM_STEP_100KHZ; }
     * else { return FmStepType.FM_STEP_UNKNOWN; } }
     */
}
