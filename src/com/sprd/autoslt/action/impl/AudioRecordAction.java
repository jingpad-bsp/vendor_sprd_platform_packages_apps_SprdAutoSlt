package com.sprd.autoslt.action.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;

public class AudioRecordAction extends AbstractAction {
    private static final String TAG = "AudioRecordAction";

    private static String mType;
    private AudioManager mAudioManager;
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private static int sampleRateInHz = 44100;//48000;
    //private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = 0;

    private String mAudioName;
    private String mNewmAudioName;

    private AudioRecord audioRecord;
    private boolean isRecord = false;
    private boolean mIsHeadsetTest;
    public static final String AUDIO_FILE_PATH = "/sdcard/slt/";

    public static AudioRecordAction instance;

    private AudioRecordAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        mContext.registerReceiver(earphonePluginReceiver, filter);
        Log.d(TAG, "regist receiver...");
    }
    
    public static AudioRecordAction getInstance(StatusChangedListener listener, String type) {
        if (instance == null) {
            instance = new AudioRecordAction(listener, type);
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
                    Log.d(TAG, "st = " + st);
                    if (st > 0) {
                    } else {
                        if(mIsHeadsetTest) {
                            if(isRecord) {
                                stopRecord();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        error("headset not exist!");
                                    }
                                });
                                Log.d(TAG, "earphonePluginReceiver headset not exist");
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    public void start(String param) {
        Log.d(TAG, "AudioRecordAction:" + param);
        mIsHeadsetTest = false;
        if (mType.equals(SLTConstant.ACTION_TYPE_START_AUDIO_RECORD)) {
            /*if(!Environment.getStorageState(new File("/storage/sdcard0/")).equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(mContext, "no sdcard!", Toast.LENGTH_SHORT).show();
                error("no sdcard!");
                return;
            }*/
            File file = new File(AUDIO_FILE_PATH/*"/storage/sdcard0/slt"*/);
            if(!file.exists()) file.mkdirs();
            if ("MainMIC".equals(param)) {
                /*if(SLTUtil.isHeadSetIn() || mIsHeadsetIn) {
                    error("headset exist!");
                    Log.d(TAG, "headset exist");
                    return;
                }*/
//                mAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record_tmp.wav";
//                mNewmAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record.wav";
                mAudioName = AUDIO_FILE_PATH + param + "_Record_tmp.wav";
                mNewmAudioName = AUDIO_FILE_PATH + param + "_Record.wav";
                mAudioManager.setParameters("test_in_stream_route=4");
                initMainMicSource();
            } else if ("AuxMIC".equals(param)) {
//                mAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record_tmp.wav";
//                mNewmAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record.wav";
                mAudioName = AUDIO_FILE_PATH + param + "_Record_tmp.wav";
                mNewmAudioName = AUDIO_FILE_PATH + param + "_Record.wav";
                mAudioManager.setParameters("test_in_stream_route=32");
                initAuxMicSource();
            } else if ("HeadMIC".equals(param)) {
                if(!SLTUtil.isHeadSetIn()) {
                    error("headset not exist!");
                    Log.d(TAG, "headset exist");
                    return;
                }
//                mAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record_tmp.wav";
//                mNewmAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record.wav";
                mAudioName = AUDIO_FILE_PATH + param + "_Record_tmp.wav";
                mNewmAudioName = AUDIO_FILE_PATH + param + "_Record.wav";
                mAudioManager.setParameters("test_in_stream_route=16");
                mIsHeadsetTest = true;
                initMainMicSource();
            } else if ("BluetoothMIC".equals(param)) {
//                mAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record_tmp.wav";
//                mNewmAudioName = SLTConstant.SLT_SDCARD_PATH + param + "_Record.wav";
                mAudioName = AUDIO_FILE_PATH + param + "_Record_tmp.wav";
                mNewmAudioName = AUDIO_FILE_PATH + param + "_Record.wav";
                mAudioManager.setParameters("test_in_stream_route=8");
                initMainMicSource();
            } else {
                error("param error!");
                return;
            }
            try {
                Log.d(TAG, "safrans: start record");
                new File(mAudioName).delete();
                new File(mNewmAudioName).delete();
                init();
                startRecord();
                ok();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                stopRecord();
                Log.d(TAG, "safrans: stop record");
                e.printStackTrace();
                error(e.toString());
            }
        } else if (mType.equals(SLTConstant.ACTION_TYPE_END_AUDIO_RECORD)) {
            if(!isRecord) {
                ok();
                return;
            }
            stopRecord();
            try {
                mContext.unregisterReceiver(earphonePluginReceiver);
                Log.d(TAG, "unregister receiver...");
            } catch (Exception e) {
                Log.d(TAG, "unregisterreceiver Exception:", e);
            }
            instance = null;
            ok();
        }
    }
    private void initAuxMicSource() {
        audioSource = MediaRecorder.AudioSource.CAMCORDER;
    }
    private void initMainMicSource() {
        audioSource = MediaRecorder.AudioSource.MIC;
    }

    private void init() {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
    }

    private void startRecord() {
        audioRecord.startRecording();
        isRecord = true;
        new Thread(new AudioRecordThread()).start();
    }

    private void stopRecord() {
        if (audioRecord != null) {
            Log.d(TAG, "safrans: enter stopRecord");
            isRecord = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    @Override
    public void stop() {
    }

    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();
            copyWaveFile(mAudioName, mNewmAudioName);
        }
    }

    private void writeDateTOFile() {
        Log.d(TAG, "writeDateTOFile");
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(mAudioName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (isRecord) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            Log.d(TAG, "readsize:" + readsize);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    Log.d(TAG, "fos write exception", e);
                    error("fos write IOException");
                } catch (Exception e) {
                    Log.d(TAG, "fos write exception", e);
                    error("fos write IOException");
                }
            }
        }
        try {
            fos.close();
        } catch (IOException e) {
            Log.d(TAG, "fos close exception", e);
            error("fos close IOException");
        }
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        Log.d(TAG, "copyWaveFile inFilename:" + inFilename + " outFilename:" + outFilename);
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRateInHz;
       // int channels = 2;
        int channels = 1;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException", e);
            error("FileNotFoundException");
        } catch (IOException e) {
            Log.d(TAG, "IOException", e);
            error("IOException");
        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new File(inFilename).delete();
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
       // header[32] = (byte) (2 * 16 / 8); // block align
        header[32] = (byte) (1 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}