package com.sprd.autoslt.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.sprd.autoslt.SLTApplication;
import com.sprd.autoslt.common.SLTConstant;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

public class SLTUtil {
    private static final String TAG = "SLTUtil";
    private static final String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";
    public static final String BRIGHTNESS_PATH = "sys/class/backlight/sprd_backlight/brightness";

    public static ComponentName getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null) {
            return runningTaskInfos.get(0).topActivity;
        } else {
            return null;
        }
    }

    public static String[] parseParam(String param) {
        if (param.contains("^")) {
            return param.split("\\^");
        }
        return new String[] { param };
    }
    
    public static void deleteFileIfExists(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
    
    public static boolean isHeadSetIn() {
        return isHeadsetExists() || isHeadsetExist();
    }

    public static boolean isHeadsetExists() {
        AudioManager mAudioManager = (AudioManager) SLTApplication
                .getApplication().getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.isWiredHeadsetOn();
    }

    public static boolean isHeadsetExist() {
        char[] buffer = new char[1024];
        int newState = 0;
        FileReader file = null;
        try {
            file = new FileReader(HEADSET_STATE_PATH);
            int len = file.read(buffer, 0, 1024);
            newState = Integer.valueOf((new String(buffer, 0, len)).trim());
            if (file != null) {
                file.close();
                file = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "isHeadsetExists Exception", e);
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                Log.d(TAG, "isHeadsetExists IOException", e);
            }
        }
        return newState != 0;
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException", e);
        }
    }

    public static synchronized String readFile(String path) {
        File file = new File(path);
        String str = new String("");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                str = str + line;
            }
        } catch (Exception e) {
            Log.d(TAG, "Read file error!!!");
            str = "readError";
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        Log.d(TAG, "read " + path + " value is " + str.trim());
        return str.trim();
    }

    public static synchronized void writeFile(String path, String cmd) {
        Log.d(TAG, "path: " + path + "; cmd: " + cmd);
        File file = new File(path);
        if (!file.exists()) {
            Log.d(TAG, "the file is not exists");
            return;
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            byte[] bytes = cmd.getBytes();
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean fileIsExists(String path) {
        try {
            File file = new File(path);
            Log.d(TAG, "fileIsExists path=" + path);
            if (!file.exists()) {
                Log.d(TAG, path + " fileIsExists false");
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, path + " fileIsExists Exception e = " + e);
            return false;
        }
        Log.d(TAG, path + " fileIsExists true");
        return true;
    }

    public static void runMonkeyScriptThread(final String scriptName) {
        new Thread(new Runnable() {
            public void run() {
                File monkeyFile = new File(SLTConstant.SLT_SDCARD_PATH
                        + scriptName);
                Log.d("MMLog", "monkeyFile = " + monkeyFile.exists());
                int count = 0;
                while (!monkeyFile.exists() && count++ < 80) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
                Log.d("MMLog", "2 monkeyFile = " + monkeyFile.exists());
                if (!monkeyFile.exists()) {
                    return;
                }

                runMonkeyScript(scriptName);
            }
        }).start();
    }

    public static boolean runMonkeyScript(String scripFile) {
        Process mProcess = null;
        try {
            Log.d("MMLog", "execMonkey enter");
            mProcess = new ProcessBuilder()
                    .command("monkey", "-f",
                            SLTConstant.SLT_SDCARD_PATH + scripFile, "1")
                    .redirectErrorStream(true).start();

            int exitValue = mProcess.waitFor();
            Log.d("MMLog", "Process.waitFor() return " + exitValue);
            Log.e("MMLog", "execMonkey exit");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (mProcess != null) {
                    mProcess.destroy();
                    mProcess = null;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return true;
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Disable the Soundeffect for Phoneloopback,return the last status
     */
    public static boolean setSoundEffect(Context context, boolean isOn) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        if (isOn) {
            audioManager.loadSoundEffects();
        } else {
            audioManager.unloadSoundEffects();
        }
        boolean lastStatus = (Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, 0) == 1);
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, isOn ? 1 : 0);
        return lastStatus;
    }

    /*
     * Disable the LockSound for Phoneloopback,return the last status
     */
    public static boolean setLockSound(Context context, boolean isOn) {
        boolean lastStatus = (Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 0) == 1);
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.LOCKSCREEN_SOUNDS_ENABLED, isOn ? 1 : 0);
        return lastStatus;
    }

    public static long setSrceenTimeout(Context context, int timeout) {
        Log.d(TAG, "setSrceenTimeout timeout=" + timeout);
        long mCurrentTime = Settings.System.getLong(
                context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 5000l);
        Log.d(TAG, "setSrceenTimeout mCurrentTime=" + mCurrentTime);
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, timeout);
        return mCurrentTime;
    }

    public static void setAudioMode(Context context, int mode) {
        Log.d(TAG, "setAudioMode mode=" + mode);
        AudioManager mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setRingerModeInternal(mode);
    }
}
