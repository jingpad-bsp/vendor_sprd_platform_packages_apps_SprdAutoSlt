package com.sprd.autoslt.action.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;

public class GetSensorValueAction extends AbstractAction {
    private static final String TAG = "GetSensorValueAction";

    /** the value of change color */
    private static final float VALUE_OF_CHANGE_COLOR = 0.5f;
    private static String mType;

    public static GetSensorValueAction instance;
    private SensorManager mSensorManager;
    private Sensor mProxSensor = null;
    private Sensor mLightSensor = null;
    private Sensor mAccSensor = null;
    private Sensor mMagSensor = null;
    private Sensor mGyroSensor = null;
    private SensorEventListener mProxSensorEventListener = null;
    private SensorEventListener mLightSensorEventListener = null;
    private SensorEventListener mAccSensorEventListener = null;
    private SensorEventListener mMagSensorEventListener = null;
    private SensorEventListener mGyroSensorEventListener = null;
    private int mProxResult = 0;
    private int mLightResult = 0;
    private float mAccResultX = 0;
    private float mAccResultY = 0;
    private float mAccResultZ = 0;
    private float mMagResultX = 0;
    private float mMagResultY = 0;
    private float mMagResultZ = 0;
    private float mGyroResultX = 0;
    private float mGyroResultY = 0;
    private float mGyroResultZ = 0;
    private static final int DELAY_TIME = 15000;
    private DecimalFormat fnum = new DecimalFormat("##0.00");
	private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;

    private Runnable unRegistRunnable = new Runnable() {
        @Override
        public void run() {
            mSensorManager.unregisterListener(mProxSensorEventListener);
            mSensorManager.unregisterListener(mLightSensorEventListener);
            mSensorManager.unregisterListener(mAccSensorEventListener);
            mSensorManager.unregisterListener(mMagSensorEventListener);
            mSensorManager.unregisterListener(mGyroSensorEventListener);
            instance = null;
        }
    };

    private GetSensorValueAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mProxSensorEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                float x = event.values[SensorManager.DATA_X];

                if (x <= VALUE_OF_CHANGE_COLOR) {
                    mProxResult = 1;
                } else {
                    mProxResult = 0;
                }
                Log.d(TAG, "mProxResult:" + mProxResult);
            }
        };
        mLightSensorEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                float x = event.values[SensorManager.DATA_X];
                mLightResult = (int) x;
                // SLTUtil.sleep(10);
            }
        };
        mAccSensorEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                mAccResultX = event.values[SensorManager.DATA_X];
                mAccResultY = event.values[SensorManager.DATA_Y];
                mAccResultZ = event.values[SensorManager.DATA_Z];
            }
        };
        mMagSensorEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "mmi test values: " + event.values[0] + ","
                        + event.values[1] + "," + event.values[2]);
                mMagResultX = event.values[0];
                mMagResultY = event.values[1];
                mMagResultZ = event.values[2];
            }
        };
        mGyroSensorEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
			if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                mGyroResultX += event.values[0] * dT;
                mGyroResultY += event.values[1] * dT;
                mGyroResultZ += event.values[2] * dT;
            }
            timestamp = event.timestamp;
		/*		if(mGyroResultX == 0) {
                    mGyroResultX = event.values[0];
                    mGyroResultY = event.values[1];
                    mGyroResultZ = event.values[2];
					Log.d("huasong", "x:" + mGyroResultX + " y:" + mGyroResultY + " z:" + mGyroResultZ);
				} else {
                    if (event.values[0] > mGyroResultX)
                        mGyroResultX = event.values[0];
                    if (event.values[1] > mGyroResultY)
                        mGyroResultY = event.values[1];
                    if (event.values[2] > mGyroResultZ)
                        mGyroResultZ = event.values[2];
				}*/
            }
        };
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        mProxSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(mProxSensorEventListener, mProxSensor,
                SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mLightSensorEventListener,
                mLightSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mAccSensorEventListener, mAccSensor,
                SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mMagSensorEventListener, mMagSensor,
                SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mGyroSensorEventListener, mGyroSensor,
                SensorManager.SENSOR_DELAY_UI);
        mHandler.postDelayed(unRegistRunnable, DELAY_TIME);
    }

    public static GetSensorValueAction getInstance(
            StatusChangedListener listener, String type) {
        if (instance == null) {
            instance = new GetSensorValueAction(listener, type);
        }
        mType = type;
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "GetSensorValueAction:" + param);
		Locale.setDefault(Locale.US);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        fnum.setDecimalFormatSymbols(symbols);
        if (mType.equals(SLTConstant.ACTION_TYPE_GET_PROX_SENSOR_VALUE)) {
            if (mProxSensor == null) {
                error("not support!");
                return;
            }
            end(mProxResult + "");
            mHandler.removeCallbacks(unRegistRunnable);
            mHandler.postDelayed(unRegistRunnable, DELAY_TIME);
        } else if (mType.equals(SLTConstant.ACTION_TYPE_GET_LIGHT_SENSOR_VALUE)) {
            if (mLightSensor == null) {
                error("not support!");
                return;
            }
            end(mLightResult + "");
            mHandler.removeCallbacks(unRegistRunnable);
            mHandler.postDelayed(unRegistRunnable, DELAY_TIME);
        } else if (mType.equals(SLTConstant.ACTION_TYPE_GET_ACC_SENSOR_VALUE)) {
            if (mAccSensor == null) {
                error("not support!");
                return;
            }
			Log.d(TAG, "mAccResultX = "+mAccResultX +"; mAccResultY = "+"; mAccResultZ = "+mAccResultZ);
            end(fnum.format(mAccResultX) + "^" + fnum.format(mAccResultY) + "^"
                    + fnum.format(mAccResultZ));
            mHandler.removeCallbacks(unRegistRunnable);
            mHandler.postDelayed(unRegistRunnable, DELAY_TIME);
        } else if (mType.equals(SLTConstant.ACTION_TYPE_GET_MAG_SENSOR_VALUE)) {
            if (mMagSensor == null) {
                error("not support!");
                return;
            }
            end(fnum.format(mMagResultX) + "^" + fnum.format(mMagResultY) + "^"
                    + fnum.format(mMagResultZ));
            mHandler.removeCallbacks(unRegistRunnable);
            mHandler.postDelayed(unRegistRunnable, DELAY_TIME);
        } else if (mType.equals(SLTConstant.ACTION_TYPE_GET_GYRO_SENSOR_VALUE)) {
            if (mGyroSensor == null) {
                error("not support!");
                return;
            }
            if(mGyroResultX == 0) {
                SLTUtil.sleep(10);
            }
            end(fnum.format(mGyroResultX) + "^" + fnum.format(mGyroResultY)
                    + "^" + fnum.format(mGyroResultZ));
            mHandler.removeCallbacks(unRegistRunnable);
            mHandler.postDelayed(unRegistRunnable, DELAY_TIME);
        } else if (mType.equals(SLTConstant.ACTION_TYPE_START_GET_SENSOR_VALUE)) {
            ok();
        } else if (mType.equals(SLTConstant.ACTION_TYPE_END_GET_SENSOR_VALUE)) {
            mHandler.removeCallbacks(unRegistRunnable);
            mHandler.post(unRegistRunnable);
            ok();
        }
    }

    @Override
    public void stop() {
    }
}
