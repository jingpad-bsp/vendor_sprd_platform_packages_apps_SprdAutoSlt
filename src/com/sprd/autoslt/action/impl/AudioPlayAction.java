package com.sprd.autoslt.action.impl;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;

import com.sprd.autoslt.R;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;

public class AudioPlayAction extends AbstractAction {
    private static final String TAG = "AudioPlayAction";

    private static String mType;

    public static AudioPlayAction instance;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayerPlayRecord;
    private boolean isPlayRecord = false;
    private boolean mIsHeadsetIn;
    private boolean isSDres = false;

    private AudioPlayAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mMediaPlayerPlayRecord = new MediaPlayer();
        mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        mContext.registerReceiver(earphonePluginReceiver, filter);
    }

    public static AudioPlayAction getInstance(StatusChangedListener listener,
            String type) {
        if (instance == null) {
            instance = new AudioPlayAction(listener, type);
        }
        mType = type;
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
                    }
                    Log.d(TAG, "mIsHeadsetIn = " + mIsHeadsetIn);
                }
            }
        }
    };

    @Override
    public void start(String param) {
        Log.d(TAG, "AudioPlayAction:" + param);
        if (mType.equals(SLTConstant.ACTION_TYPE_START_AUDIO_PLAY)) {
            if (isPlayRecord) {
                mMediaPlayerPlayRecord.stop();
                mMediaPlayerPlayRecord.reset();
            }
            isPlayRecord = true;
            String path = null;
            int volume = -1;
            String[] params = SLTUtil.parseParam(param);
            if (params.length == 3) {
                try {
                    volume = Integer.parseInt(params[1]);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "exception:", e);
                    error("invalid param");
                    return;
                }
                if (volume > 15 || volume < 0) {
                    volume = mAudioManager
                            .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                }
                //path = new String(SLTConstant.SLT_SDCARD_PATH + params[2]);
                path = new String(params[2]);
                if (path.contains("/")) {
                    isSDres = true;
                }else {
                    isSDres = false;
                }
            } else {
                error("invalid param");
                return;
            }
            try {
                Log.d(TAG, "path = " +path);
                if (isSDres) {
                    mMediaPlayerPlayRecord.setDataSource(path);
                }else {
                    if (path.equals("audio_200hz_10khz.wav")) {
                        mMediaPlayerPlayRecord.setDataSource(mContext, Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.audio_200hz_10khz));
                      }
                    else if (path.equals("audio_200hz_10khz_l.wav")) {
                        mMediaPlayerPlayRecord.setDataSource(mContext, Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.audio_200hz_10khz_l));
                      }
                    else if (path.equals("audio_200hz_10khz_r.wav")) {
                        mMediaPlayerPlayRecord.setDataSource(mContext, Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.audio_200hz_10khz_r));
                      }
                    else {
                      error("invalid param");
                      return;
                    }
               }
             //   mMediaPlayerPlayRecord.setDataSource(path);
//                mMediaPlayerPlayRecord.setLooping(true);
                mMediaPlayerPlayRecord.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "mMediaPlayerPlayRecord onCompletion mMediaPlayerPlayRecord 1= "
                                + mMediaPlayerPlayRecord);
                        if (mMediaPlayerPlayRecord != null) {
                            isPlayRecord = false;
                            mMediaPlayerPlayRecord.stop();
                            mMediaPlayerPlayRecord.release();
                            mMediaPlayerPlayRecord = null;
                            mAudioManager.abandonAudioFocus(audioFocusChangeListener);
                        }
                        try {
                            mContext.unregisterReceiver(earphonePluginReceiver);
                            Log.d(TAG, "setOnCompletionListener --unregister receiver...");
                        } catch (Exception e) {
                            Log.d(TAG, "setOnCompletionListener -- unregisterreceiver Exception:", e);
                        }
                        instance = null;
                        Log.d(TAG, "mMediaPlayerPlayRecord onCompletion mMediaPlayerPlayRecord 2= "
                                + mMediaPlayerPlayRecord);
                    }
                });
                if (!TextUtils.isEmpty(params[0])
                        && params[0].equals("receiver")) {
                    mMediaPlayerPlayRecord
                            .setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                    if (volume > 15 || volume < 0) {
                        volume = mAudioManager
                                .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                    }
                    mAudioManager.setStreamVolume(
                            AudioManager.STREAM_VOICE_CALL, volume, 0);
                    Log.d(TAG, "receiver requestAudioFocus = " +requestAudioFocus());
                    /*if (!requestAudioFocus()) {
                        error("");
                        return;
                    }*/
                    mMediaPlayerPlayRecord.prepare();
                    mMediaPlayerPlayRecord.start();
                    SLTUtil.sleep(500);
                    mAudioManager.setParameter("test_stream_route", "1");//1ÌýÍ²£¬2 speaker 4/8 ¶ú»ú
                    ok();
                } else if (!TextUtils.isEmpty(params[0])
                        && params[0].equals("headset")) {
                    if (!SLTUtil.isHeadSetIn()) {
                        error("headset out!");
                        return;
                    } else {
                        mMediaPlayerPlayRecord
                                .setAudioStreamType(AudioManager.STREAM_MUSIC);
                        if (volume < 0)
                            volume = 10;
                        mAudioManager.setStreamVolume(
                                AudioManager.STREAM_MUSIC, volume, 0);
                        Log.d(TAG, "headset requestAudioFocus = " +requestAudioFocus());
                        /*if (!requestAudioFocus()) {
                            error("");
                            return;
                        }*/
                        mMediaPlayerPlayRecord.prepare();
                        mMediaPlayerPlayRecord.start();
                        SLTUtil.sleep(500);
                        mAudioManager.setParameter("test_stream_route", "4");//1ÌýÍ²£¬2 speaker 4/8 ¶ú»ú
                        ok();
                    }
                } else if (!TextUtils.isEmpty(params[0])
                        && params[0].equals("speaker")) {
                    if (volume > 15 || volume < 0) {
                        volume = mAudioManager
                                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    }
                    // Speaker
                    mMediaPlayerPlayRecord
                            .setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            volume, 0);
                    Log.d(TAG, "speaker requestAudioFocus = " +requestAudioFocus());
                    /*if (!requestAudioFocus()) {
                        error("");
                        return;
                    }*/
                    mMediaPlayerPlayRecord.prepare();
                    mMediaPlayerPlayRecord.start();
                    SLTUtil.sleep(500);
                    mAudioManager.setParameter("test_stream_route", "2");//1ÌýÍ²£¬2 speaker 4/8 ¶ú»ú
                    ok();
                } else {
                    error("param error");
                    return;
                }
            } catch (Exception e) {
                Log.d(TAG, "exception", e);
                error(e.toString());
                return;
            }

        } else if (mType.equals(SLTConstant.ACTION_TYPE_END_AUDIO_PLAY)) {
            Log.d(TAG, "mMediaPlayerPlayRecord end isPlayRecord = "
                    + isPlayRecord);
            if (isPlayRecord) {
                isPlayRecord = false;
                mMediaPlayerPlayRecord.stop();
                mMediaPlayerPlayRecord.reset();
                mMediaPlayerPlayRecord.release();
                mMediaPlayerPlayRecord = null;
                mAudioManager.abandonAudioFocus(audioFocusChangeListener);
            }
            try {
                mContext.unregisterReceiver(earphonePluginReceiver);
                Log.d(TAG, "unregister receiver...");
            } catch (Exception e) {
                Log.d(TAG, "unregisterreceiver Exception:", e);
            }
            instance = null;
            ok();
        } else if (mType.equals(SLTConstant.ACTION_TYPE_PAUSE_AUDIO_PLAY)) {
            if (mMediaPlayerPlayRecord != null
                    && mMediaPlayerPlayRecord.isPlaying()&&isPlayRecord) {
                mMediaPlayerPlayRecord.pause();
                mAudioManager.abandonAudioFocus(audioFocusChangeListener);
                ok();
                } else {
                error("media player status error!");
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_RESUME_AUDIO_PLAY)) {
            if (mMediaPlayerPlayRecord != null
                    && !mMediaPlayerPlayRecord.isPlaying()&&isPlayRecord) {
                Log.d(TAG, "resume requestAudioFocus = " +requestAudioFocus());
                Log.d(TAG, "mMediaPlayerPlayRecord countine = "
                        + mMediaPlayerPlayRecord + "isplaying countine = "
                        + mMediaPlayerPlayRecord.isPlaying());
                /*if (!requestAudioFocus()) {
                    error("request focus false");
                    return;
                }*/
                mMediaPlayerPlayRecord.start();
                ok();
            } else {
                error("media player status error!");
            }
        } else {
            error("invalid param");
            return;
        }
    }

    @Override
    public void stop() {
    }
    private int requestAudioFocus() {
        int audioFocus = mAudioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        Log.d(TAG, "audioFocus = " +audioFocus);
        return audioFocus;
    }
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            // TODO Auto-generated method stub
            Log.d(TAG, "audioFocusChangeListener focusChange = " +focusChange);
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                //mMediaPlayerPlayRecord.pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                //mMediaPlayerPlayRecord.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //if (mMediaPlayerPlayRecord.isPlaying()) {
                //  mMediaPlayerPlayRecord.pause();
                //}
                break;
            default:
                break;
            }
        }
    };
}