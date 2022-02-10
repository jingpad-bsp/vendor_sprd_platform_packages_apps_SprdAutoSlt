package com.sprd.autoslt.action.impl;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.VideoTestActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;

public class VideoAction extends AbstractAction {

    private static final String TAG = "VideoAction";
    private static final String VIDEO_PACKAGENAME = "com.android.gallery3d";
    PackageManager mPackageManager;
    ActivityManager mActivityManager;
    private static VideoAction instance;
    private static String mType;

    public VideoAction(StatusChangedListener listener) {
        super(listener);
        mPackageManager = mContext.getPackageManager();
        mActivityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
    }

    private VideoAction(StatusChangedListener listener, String type) {
        super(listener);
        mPackageManager = mContext.getPackageManager();
        mActivityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        mType = type;
    }

    public static VideoAction getInstance(StatusChangedListener listener, String type) {
        mType = type;
        if(instance == null) {
            instance = new VideoAction(listener, type);
        }
        return instance;
    }

    @Override
    public void start(String fileName) {
        Log.d(TAG, "video start");
        if(!TextUtils.isEmpty(mType) && mType.equals(SLTConstant.ACTION_TYPE_START_VIDEO_PLAY)) {
            if (SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.VideoTestActivity")) {
                ok();
                return;
            }
            if (TextUtils.isEmpty(fileName)) {
                //fileName = "slt_video.mp4";
                fileName = "/sdcard/slt/slt_video.mp4";
            }

            //String filePath = SLTConstant.SLT_SDCARD_PATH + fileName;
             String filePath = fileName;
            Log.d(TAG, "filePath:" + filePath);
            File file = new File(filePath);
            Log.d(TAG, "file.exists():" + file.exists());
            if (!file.exists()) {
                error("VideoAction start() failed, file is not exists : "
                        + filePath);
                return;
            }
            if (isVideoPlaying()) {
                error("already running..");
                return;
            }
            Uri fileUri = Uri.fromFile(file);
            Log.d(TAG, "fileUri: " +fileUri);
            Intent intent = new Intent(mContext, VideoTestActivity.class).setDataAndType(fileUri,
                    "video/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            ok();
        } else if(!TextUtils.isEmpty(mType) && mType.equals(SLTConstant.ACTION_TYPE_END_VIDEO_PLAY)) {
            Log.d(TAG, "stop");
            try {
                stopPlayVideo();
            } catch (Exception e) {
                Log.w(TAG,
                        "find exception when stop video play : " + e.getMessage());
            }
            ok();
        }
    }

    @Override
    public void stop() {
    }

    private void killVideoApp() {
        if (isVideoProcessRunning()) {
            mActivityManager.forceStopPackage(VIDEO_PACKAGENAME);
        }
    }
    

    private void stopPlayVideo() {
        if (isVideoPlaying()) {
            /*Intent intent = new Intent(mContext, VideoTestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("stop", true);
            mContext.startActivity(intent);*/
            VideoTestActivity.instance.finish();
        }
    }

    private boolean isVideoPlaying() {
        ComponentName componentName = getTopActivityPackageName();
        if(componentName == null) return false;
        String name = componentName.toString();
        if(TextUtils.isEmpty(name)) return false;
        return name.contains("com.sprd.autoslt.activity.VideoTestActivity");
    }

    private boolean isVideoProcessRunning() {

        List<ActivityManager.RunningAppProcessInfo> procList = mActivityManager
                .getRunningAppProcesses();
        if ((procList == null) || (procList.size() == 0)) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcInfo : procList) {
            if (appProcInfo != null) {
                int pkgsize = appProcInfo.pkgList.length;
                for (int j = 0; j < pkgsize; j++) {
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = mPackageManager.getApplicationInfo(
                                appProcInfo.pkgList[j],
                                PackageManager.GET_UNINSTALLED_PACKAGES);
                        Log.d(TAG, "appInfo : " + appInfo.toString());
                        if (VIDEO_PACKAGENAME
                                .equalsIgnoreCase(appInfo.packageName)) {
                            return true;
                        }
                    } catch (NameNotFoundException e) {
                        Log.w(TAG, "Error retrieving ApplicationInfo for pkg:"
                                + appProcInfo.pkgList[j]);
                        continue;
                    }
                }
            }
        }
        return false;
    }
    
    private ComponentName getTopActivityPackageName() {
        ActivityManager activityManager = (ActivityManager) (mContext
                .getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<RunningTaskInfo> runningTaskInfos = activityManager
                .getRunningTasks(1);
        if (runningTaskInfos != null) {
            return runningTaskInfos.get(0).topActivity;
        }
        return null;
    }

}
