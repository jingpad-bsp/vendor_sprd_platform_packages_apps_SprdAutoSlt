package com.sprd.autoslt.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.util.Log;

public class ShellUtil {
    private static final String TAG = "ShellUtil";

    // exec shell cmd
    public static String execShellStr(String cmd) {
        StringBuffer retString = new StringBuffer("");
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader stdout = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), "UTF-8"), 7777);
            BufferedReader stderr = new BufferedReader(new InputStreamReader(
                    process.getErrorStream(), "UTF-8"), 7777);

            String line = null;

            while ((null != (line = stdout.readLine()))
                    || (null != (line = stderr.readLine()))) {
                if ("" != line) {
                    retString = retString.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, cmd + ":" + retString.toString() + "");
        return retString.toString();
    }
}
