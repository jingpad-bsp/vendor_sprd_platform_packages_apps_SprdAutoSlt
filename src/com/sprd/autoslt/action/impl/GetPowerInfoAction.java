package com.sprd.autoslt.action.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.File;

import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;

public class GetPowerInfoAction extends AbstractAction {

    private static final String TAG = "GetPowerInfoAction";
    private static GetPowerInfoAction instance;
    private static String WORKING_VOL = "/sys/class/power_supply/sprdfgu/fgu_vol";
    private static String WORKING_CURRENT = "sys/class/power_supply/sprdfgu/fgu_current";
    private static String CHARGER_VOL = "sys/class/power_supply/battery/charger_voltage";
    // private static String CHARGER_CURRENT
    // ="sys/class/power_supply/battery/real_time_curren";

    private static String WORKING_VOL_K414 = "/sys/class/power_supply/battery/voltage_now";
    private static String WORKING_CURRENT_K414 = "/sys/class/power_supply/battery/current_now";
    private static String CHARGER_VOL_K414 = "/sys/class/power_supply/sc27xx-fgu/constant_charge_voltage";
    private boolean mIsSupportK414 = false;

    private GetPowerInfoAction(StatusChangedListener listener) {
        super(listener);
        // TODO Auto-generated constructor stub
        initSupportK414();
    }

    public static GetPowerInfoAction getInstance(StatusChangedListener listener) {
        if (instance == null) {
            instance = new GetPowerInfoAction(listener);
        }
        return instance;
    }

    private void initSupportK414() {
        File file = new File(CHARGER_VOL_K414);
        Log.d(TAG, "initSupportK414 file=" + file + ",exists=" + file.exists());
        if (file != null && file.exists()) {
            mIsSupportK414 = true;
        } else {
            mIsSupportK414 = false;
        }
    }

    private boolean isSupportK414() {
        Log.d(TAG, "isSupportK414 mIsSupportK414=" + mIsSupportK414);
        return mIsSupportK414;
    }

    @Override
    public void start(String param) {
        // TODO Auto-generated method stub
        Log.d(TAG, "GetPowerInfoAction:" + param);

        float working_vol;
        float woring_current;
        float charger_vol;
        if (isSupportK414()) {
            working_vol = getDateFromNode(WORKING_VOL_K414) / 1000;
            woring_current = getDateFromNode(WORKING_CURRENT_K414) / 1000;
            charger_vol = getDateFromNode(CHARGER_VOL_K414) / 1000;
        } else {
            working_vol = getDateFromNode(WORKING_VOL);
            woring_current = getDateFromNode(WORKING_CURRENT);
            charger_vol = getDateFromNode(CHARGER_VOL);
        }
        Log.d(TAG, "GetPowerInfoAction:  working_vol =" + working_vol
                + " woring_current =" + woring_current
                + "; charger_volString =" + charger_vol);
        // end("GetPowerInfo : "+working_vol
        // +"^"+woring_current+"^"+charger_vol+"^"+charger_current);
        /*
         * if (charger_vol.equals("discharging")) { charger_vol = "0"; }
         */
        end(working_vol + "^" + woring_current + "^" + charger_vol);
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    private float getDateFromNode(String nodeString) {
        char[] buffer = new char[1024];
        // Set a special value -100, to distinguish mChargerElectronic greater
        // than -40.
        float batteryElectronic = -100;
        FileReader file = null;
        try {
            file = new FileReader(nodeString);
            int len = file.read(buffer, 0, 1024);
            batteryElectronic = Float.valueOf((new String(buffer, 0, len)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                Log.e(TAG, "getDateFromNode fail , nodeString is:" + nodeString);
            }
        }
        return batteryElectronic;
    }
}
