
package com.sprd.autoslt.action.impl;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.BtTestUtil;

public class BtAction extends AbstractAction {

    private static final String TAG = "BtAction";

    String mBtDeviceName;
    private BtTestUtil btTestUtil = null;
    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<BluetoothDevice>();

    public BtAction(StatusChangedListener listener) {
        super(listener);
        btTestUtil = new BtTestUtil(mContext) {

            public void btStateChange(int newState) {
                switch (newState) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "Bluetooth ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "Bluetooth Closing");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "Bluetooth OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "Bluetooth Opening...");
                        break;
                    default:
                        Log.d(TAG, "Bluetooth state Unknown");
                        break;
                }
            }

            public void btDeviceListAdd(BluetoothDevice device) {

                if (mBluetoothDeviceList.contains(device)) {
                    return;
                }

                if (device != null) {
                    mBluetoothDeviceList.add(device);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        String name = device.getName();
                        if (name == null || name.isEmpty()) {
                            return;
                        }
                        StringBuffer deviceInfo = new StringBuffer();
                        deviceInfo.append("device name: ");
                        deviceInfo.append(name);
                        deviceInfo.append("\n");
                        Log.w(TAG, "======find bluetooth device => name : " + name
                                + "\n address :" + device.getAddress());
                        if (mBtDeviceName.equals(name)) {
                            mHandler.removeMessages(ACTION_TIME_OUT);
                            ok();
                            stop();
                            return;
                        }
                    }
                }
            }

            public void btDiscoveryFinished() {
                if (mBluetoothDeviceList != null
                        && mBluetoothDeviceList.size() > 0) {
                    for(BluetoothDevice device : mBluetoothDeviceList) {
                        if (mBtDeviceName.equals(device.getName())) {
                            btTestUtil.stopTest();
                            mHandler.removeMessages(ACTION_TIME_OUT);
                            ok();
                            stop();
                            return;
                        }
                    }
                    btTestUtil.stopTest();
                    mHandler.removeMessages(ACTION_TIME_OUT);
                    error(TAG + " discovery not found!");
                    stop();
                }
            }
        };
    }

    @Override
    public void start(final String param) {
        if (TextUtils.isEmpty(param)) {
            Log.e(TAG, " start() failed -> param is empty!");
            error(TAG + " start() failed -> param is empty!");
            return;
        }
        mBtDeviceName = param;
        btTestUtil.startTest(mContext);
        mHandler.sendEmptyMessageDelayed(ACTION_TIME_OUT, 12000);
    }

    @Override
    public void stop() {
        btTestUtil.stopTest();
        mHandler.removeMessages(ACTION_TIME_OUT);
    }
}
