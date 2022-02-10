package com.sprd.autoslt.action.impl;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.action.AbstractAction.StatusChangedListener;
import com.sprd.autoslt.util.SLTUtil;

public class SensorCalibrationAction extends AbstractAction {
    private static final String TAG = "SensorCalibrationAction";

    private static volatile SensorCalibrationAction instance;
    public static final String CALIBRATOR_CMD = "/sys/class/sprd_sensorhub/sensor_hub/calibrator_cmd";
    public static final String CALIBRATOR_DATA = "/sys/class/sprd_sensorhub/sensor_hub/calibrator_data";
    private static final int SET_CMD_COMPLETE = 1;
    private static final int CALIBRATION_SUCCESS = 2;
    private static final int CALIBRATION_FAIL = 3;
    // asensor
    private static final String ACC_SET_CMD = "0 1 1"; // start calibrating
    private static final String ACC_GET_RESULT = "1 1 1";// get result of
                                                         // Calibration
    private static final String ACC_SAVE_RESULT = "3 1 1";// save the result
    // asensor
    private static final String MAG_SET_CMD = "0 2 1"; // start calibrating
    private static final String MAG_GET_RESULT = "1 2 1";// get result of
                                                         // Calibration
    private static final String MAG_SAVE_RESULT = "3 2 1";// save the result
    // asensor
    private static final String GYRO_SET_CMD = "0 4 1"; // start calibrating
    private static final String GYRO_GET_RESULT = "1 4 1";// get result of
                                                          // Calibration
    private static final String GYRO_SAVE_RESULT = "3 4 1";// save the result

    private static final String PROX_SET_CMD = "0 8 1"; // auto calibrating
    private static final String PROX_GET_RESULT = "1 8 1";// get result of
                                                               // Calibration
    private static final String PROX_SAVE_RESULT = "3 8 1";// save the
                                                                // result

    private static final String PASS_NUMBER = "0";
    private static final String TEST_OK = "2";
    private boolean isOk = false;
    private boolean saveResult = false;
    private String mCurrentType;

    private HashMap<String, String> mTestResultList = new HashMap<String, String>();
    private ArrayList<String> mTestList = new ArrayList<String>();

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SET_CMD_COMPLETE:
                new Thread(new Runnable() {
                    public void run() {
                        getResult();
                    }
                }).start();
                break;
            case CALIBRATION_SUCCESS:
                Toast.makeText(mContext, mCurrentType + " cali success",
                        Toast.LENGTH_SHORT).show();
                if (mTestList.size() == mTestResultList.size()) {
                    StringBuffer sb = new StringBuffer();
                    for(int i = 0; i < mTestResultList.size(); i++) {
                        String key = mTestList.get(i);
                        sb.append(key);
                        sb.append(":");
                        sb.append(mTestResultList.get(key));
                        sb.append("^");
                    }
                    end(sb.toString().substring(0, sb.toString().lastIndexOf("^")));
                }
                break;
            case CALIBRATION_FAIL:
                Toast.makeText(mContext, mCurrentType + " cali fail",
                        Toast.LENGTH_SHORT).show();
                if (mTestList.size() == mTestResultList.size()) {
                    StringBuffer sb = new StringBuffer();
                    for(int i = 0; i < mTestResultList.size(); i++) {
                        String key = mTestList.get(i);
                        sb.append(key);
                        sb.append(":");
                        sb.append(mTestResultList.get(key));
                        sb.append("^");
                    }
                    end(sb.toString().substring(0, sb.toString().lastIndexOf("^")));
                }
                break;
            default:
            }
        }

    };

    private SensorCalibrationAction(StatusChangedListener listener) {
        super(listener);
    }

    public static SensorCalibrationAction getInstance(StatusChangedListener listener) {
        if(instance == null) {
            synchronized (BTModeAction.class) {
                if(null == instance) {
                    instance = new SensorCalibrationAction(listener);
                }
            }
        }
        return instance;
    }

    @Override
    public void start(final String param) {
        Log.d(TAG, "SensorCalibrationAction:" + param);
        if (TextUtils.isEmpty(param)) {
            error("param error");
            return;
        }
        if(!SLTUtil.fileIsExists(CALIBRATOR_CMD)) {
            error("not support");
            return;
        }
        mTestList.clear();
        mTestResultList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] result = SLTUtil.parseParam(param);
                for (int i = 0; i < result.length; i++) {
                    mTestList.add(result[i]);
                }
                for (int i = 0; i < result.length; i++) {
                    mCurrentType = result[i];
                    if (mCurrentType.equals("Acc")) {
                        startCalibration(ACC_SET_CMD);
                    } else if (mCurrentType.equals("Mag")) {
                        startCalibration(MAG_SET_CMD);
                    } else if (mCurrentType.equals("Prox")) {
                        startCalibration(PROX_SET_CMD);
                    } else if (mCurrentType.equals("Gyro")) {
                        startCalibration(GYRO_SET_CMD);
                    }  else {
                        error("param error");
                        return;
                    }
                }
            }
        }).start();
    }

    private synchronized void startCalibration(String cmd) {
        SLTUtil.writeFile(CALIBRATOR_CMD, cmd);
        SLTUtil.sleep(4000);
        mHandler.sendMessage(mHandler.obtainMessage(SET_CMD_COMPLETE));
        SLTUtil.sleep(1000);
    }

    /**
     * start calibrating echo "2 [SENSOR_ID] 1" > calibrator_cmd cat
     * calibrator_data, if the get value is 2 ,the test is ok ,or test is fial
     **/
    private void getResult() {
        boolean isOK = false;
        if (mCurrentType.equals("Acc")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, ACC_GET_RESULT);
        } else if (mCurrentType.equals("Mag")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, MAG_GET_RESULT);
        } else if (mCurrentType.equals("Gyro")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, GYRO_GET_RESULT);
        } else if (mCurrentType.equals("Prox")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, PROX_GET_RESULT);
        }
        String getResult = SLTUtil.readFile(CALIBRATOR_DATA);
        saveResult = saveResult();
        Log.d(TAG, "the result of boolen saveResult: " + saveResult);
        Log.d(TAG, "the result of Acceleration calibration: " + getResult);
        if (saveResult && getResult != null && TEST_OK.equals(getResult.trim())) {
            isOK = true;
        }
        if (isOK) {
            mTestResultList.put(mCurrentType, "pass");
            mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_SUCCESS));
        } else {

            mTestResultList.put(mCurrentType, "fail");
            mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_FAIL));
        }
    }

    /**
     * save the result echo "3 [SENSOR_ID] 1" > calibrator_cmd cat
     * calibrator_data to save test result
     **/
    private boolean saveResult() {
        Log.d(TAG, "saveResult...");
        if (mCurrentType.equals("Acc")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, ACC_SAVE_RESULT);
        } else if (mCurrentType.equals("Mag")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, MAG_SAVE_RESULT);
        } else if (mCurrentType.equals("Prox")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, PROX_SAVE_RESULT);
        } else if (mCurrentType.equals("Gyro")) {
            SLTUtil.writeFile(CALIBRATOR_CMD, GYRO_SAVE_RESULT);
        }
        String saveResult = SLTUtil.readFile(CALIBRATOR_DATA);
        Log.d(TAG, "save result: " + saveResult);
        if (saveResult != null && PASS_NUMBER.equals(saveResult.trim())) {
            isOk = true;
            Log.d(TAG, "save result isOk: " + isOk);
        }
        return isOk;
    }

    @Override
    public void stop() {
    }
}
