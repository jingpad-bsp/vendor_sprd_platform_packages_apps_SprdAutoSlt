
package com.sprd.autoslt.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.R.integer;
import android.content.Context;
import android.os.Environment;
import android.os.EnvironmentEx;
import android.os.storage.StorageManager;
import android.util.Log;

public class StorageUtil {
    public static final String TAG = "StorageUtil";

    public static final int EXT_EMULATED_PATH = 0;
    public static final int EXT_COMMON_PATH = 1;
    public static final int OTG_UDISK_PATH = 2;

    /*
     * type:0 --- External storage(SD card) emulated app directory.
     * type:1 --- External storage(SD card) common app directory.
     * type:2 --- USB mass storage(OTG U disk) app directory.
     */
    public static String getExternalStorageAppPath(Context context, int type) {
        String extEmulatedPath = null;
        String extCommonPath = null;
        String extOtgUdiskPath = null;
        String otgUdiskPath = "null";

        List<File> allDirPaths = new ArrayList<>();
        Collections.addAll(allDirPaths, context.getExternalFilesDirs(null));

        File[] otgPaths = EnvironmentEx.getUsbdiskVolumePaths();
        for (File file : otgPaths) {
            if (Environment.MEDIA_MOUNTED.equals(EnvironmentEx.getUsbdiskVolumeState(file))) {
                Log.d(TAG, "otg udisk mounted, otg path is " + file.getPath());
                otgUdiskPath = file.getPath();
            } else {
                Log.i(TAG, "otg udisk not mounted, otg path is null");
                otgUdiskPath = "null";
            }
        }

        for (File file : allDirPaths) {
            if (file != null) {
                String path = file.getAbsolutePath();
                if (path.startsWith("/storage/emulated/0")) {
                    Log.d(TAG, "external storage emulated path is: " + path);
                    extEmulatedPath = path;
                } else if (path.startsWith(otgUdiskPath)) {
                    Log.d(TAG, "external storage otg udisk path is: " + path);
                    extOtgUdiskPath = path;
                } else {
                    Log.d(TAG, "external storage common path is: " + path);
                    extCommonPath = path;
                }
            }
        }

        if (type == EXT_EMULATED_PATH) {
            return extEmulatedPath;
        } else if (type == EXT_COMMON_PATH) {
            return extCommonPath;
        } else if (type == OTG_UDISK_PATH) {
            return extOtgUdiskPath;
        }else{
            Log.d(TAG, "type is incorrect!");
            return null;
        }
    }
}