package com.sprd.autoslt.action.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.os.SystemProperties;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.PhaseCheckParse;
import com.sprd.autoslt.util.SystemPropertiesInvoke;

public class GetVersionInfo extends AbstractAction {
    private static final String TAG = "GetVersionInfo";
    private PhaseCheckParse mCheckParse;

    public GetVersionInfo(StatusChangedListener listener) {
        super(listener);
        mCheckParse = new PhaseCheckParse();
    }

    @Override
    public void start(String param) {
        if (mCheckParse == null) {
            mCheckParse = new PhaseCheckParse();
        }
        end("Android Version:" + Build.VERSION.RELEASE + "^Prop Version:"
                + getPropVersion() + "^Build Number:"
                + SystemProperties.get("ro.build.display.id", "unknown")
                + "^Device Sn:" + getSn());
    }

    @Override
    public void stop() {

    }

    private String getPropVersion() {
        BufferedReader bReader = null;
        StringBuffer sBuffer = new StringBuffer();

        try {
            FileInputStream fi = new FileInputStream("/proc/version");
            bReader = new BufferedReader(new InputStreamReader(fi));
            String str = bReader.readLine();

            while (str != null) {
                sBuffer.append(str + "\n");
                str = bReader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sBuffer.toString();
    }

    private String getFormattedKernelVersion() {
        String procVersionStr;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
            try {
                procVersionStr = reader.readLine();
            } finally {
                reader.close();
            }
            if(TextUtils.isEmpty(procVersionStr)){
                return "Unavailable";
            }
            final String PROC_VERSION_REGEX =
                    "\\w+\\s+" + /* ignore: Linux */
                            "\\w+\\s+" + /* ignore: version */
                            "([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
                            "\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /*
                                                                         * group
                                                                         * 2:
                                                                         * (xxxxxx
                                                                         * @
                                                                         * xxxxx
                                                                         * .
                                                                         * constant
                                                                         * )
                                                                         */
                            "\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /*
                                                                      * ignore:
                                                                      * (gcc ..)
                                                                      */
                            "([^\\s]+)\\s+" + /* group 3: #26 */
                            "(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
                            "(.+)"; /* group 4: date */

            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);

            if (!m.matches()) {
                Log.e(TAG, "Regex did not match on /proc/version: " + procVersionStr);
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                Log.e(TAG, "Regex match on /proc/version only returned " + m.groupCount()
                        + " groups");
                return "Unavailable";
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {
            Log.e(TAG,
                    "IO Exception when getting kernel version for Device Info screen",
                    e);

            return "Unavailable";
        }
    }

    private String getSn() {
      //  return PhaseCheckParse.getInstance().getSn();
        return mCheckParse.getSn();
    }
}
