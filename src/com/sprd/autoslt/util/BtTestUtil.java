package com.sprd.autoslt.util;

import java.lang.reflect.Method;
import java.util.HashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

public class BtTestUtil {
    private static String TAG = "BtTestUtil";
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDiscoveryReceiver btDiscoveryReceiver = null;
    private BlueToothStateReceiver btStateReceiver = null;
    private BluetoothBondStateReceiver btBondStateReceiver = null;
    private HashMap<String, String> mRssiMap = new HashMap<String, String>();
    private String btPairDeviceName;
    private String pairPin ;
    private String btScanName;
    private boolean isDiscovery = false;

    public boolean isDiscovery() {
        return isDiscovery;
    }

    public void setDiscovery(boolean discovery) {
        isDiscovery = discovery;
    }

    public BtTestUtil(Context context) {
        Log.w(TAG, "BtTestUtil()");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void registerAllReceiver(Context context) {
        // register receiver for bt search
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intent.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        btDiscoveryReceiver = new BluetoothDiscoveryReceiver();
        context.registerReceiver(btDiscoveryReceiver, intent);
        // register reveiver for bt state change
        btStateReceiver = new BlueToothStateReceiver();
        IntentFilter filter = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(btStateReceiver, filter);
        
        btBondStateReceiver = new BluetoothBondStateReceiver();
        IntentFilter btBondfilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(btBondStateReceiver, btBondfilter);
    }

    public void unregisterAllReceiver(Context context) {
        if (btDiscoveryReceiver != null) {
            context.unregisterReceiver(btDiscoveryReceiver);
            btDiscoveryReceiver = null;
        }
        if (btStateReceiver != null) {
            context.unregisterReceiver(btStateReceiver);
            btStateReceiver = null;
        }
        if (btBondStateReceiver != null) {
            context.unregisterReceiver(btBondStateReceiver);
            btStateReceiver = null;
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void startTest(Context context) {
 //       registerAllReceiver();
        if (mBluetoothAdapter.isEnabled()){
            btPageScan();
            btStartDiscovery();
        }
    }

    public void stopTest() {
        mBluetoothAdapter.cancelDiscovery();
//        unregisterAllReceiver();
        mBluetoothAdapter.disable();
    }

    public boolean isBTEnabled(){
        return mBluetoothAdapter.isEnabled();
    }
    public void startBT(Context context) {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    public void stopBT() {
        if (mBluetoothAdapter.isEnabled()) {
             mBluetoothAdapter.disable();
        }
    }

    private void btStartDiscovery() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.startDiscovery();
            Log.w(TAG, "============startDiscovery===============");
            isDiscovery = true;
        }
    }

    public void btStateChange(int newState) {
        // for override
    }

    public void btPageScan() {
        /* bt should send scan enable command to accommodate CP2 */
        if (mBluetoothAdapter != null) {
            // mBluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            Log.w(TAG,
                    "============btPageScan SCAN_MODE_CONNECTABLE_DISCOVERABLE===============");
        }
    }

    public void btDeviceListAdd(BluetoothDevice newDevice) {
        // for override
    }

    public void btDiscoveryFinished() {
        // for override
    }

    public void btBondDeviceListAdd(BluetoothDevice newDevice){
        //for override
    }
    
    private class BlueToothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "BlueToothStateReceiver newState = " + mBluetoothAdapter.getState());
            int newState = mBluetoothAdapter.getState();
            switch (newState) {
            case BluetoothAdapter.STATE_ON:
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                break;
            case BluetoothAdapter.STATE_OFF:
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                break;
            default:
            }
            btStateChange(newState);
        }
    }

    public String getRssi(String name, String address) {
        if(mRssiMap != null && mRssiMap.containsKey(name)) {
            return mRssiMap.get(name);
        }
        if(mRssiMap != null && mRssiMap.containsKey(address)) {
            return mRssiMap.get(address);
        }
        return null;
    }

    private class BluetoothDiscoveryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.v(TAG, "found bluetooth device");              
                if (device != null) {
//                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        btDeviceListAdd(device);
                        Log.d(TAG, "find rssi! device name:" + device.getName() + " addr:" + device.getAddress() + " rssi:" + intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                        mRssiMap.put(device.getName(), intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE) + "");
                        mRssiMap.put(device.getAddress(), intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE) + "");
                        if (!TextUtils.isEmpty(btScanName) &&!TextUtils.isEmpty(device.getAddress()) && device.getAddress().equals(btScanName)) {
                            Log.d(TAG, "device.getAddress().equals(btScanName)= "+device.getAddress().equals(btScanName));
                            mBluetoothAdapter.cancelDiscovery();
                            isDiscovery = false;
                         }
//                    }
                } else {
                    Log.w(TAG, "not find any device");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.v(TAG, "=============discovery finished !");
                btDiscoveryFinished();
                isDiscovery = false;
            }else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.v(TAG, "======== pair request ========");
                if (device.getAddress().equals(btPairDeviceName)/*device.getName().equals(btPairDeviceName)*/) {
                    Log.e(TAG, "OKOKOK");
                    try {
                        boolean result1 = device.setPairingConfirmation(true);
                        Log.d(TAG, "isOrderedBroadcast:"+isOrderedBroadcast()+",isInitialStickyBroadcast:"+isInitialStickyBroadcast());
                        abortBroadcast();
                        boolean ret = device.setPin(pairPin.getBytes());
                        Log.d(TAG, "setPin ="+ret + "result1="+result1);

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }else {
                    Log.e(TAG, "this device is not device to pair");

                }
            }
        }
    }
    
    private class BluetoothBondStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                Log.d(TAG, "bonded");
                btBondDeviceListAdd(device);
//              btPairCondition();
                break;
            case BluetoothDevice.BOND_BONDING:
                Log.d(TAG, "Bonding");
                break;
            case BluetoothDevice.BOND_NONE:
                Log.d(TAG, "cancle bond");
                btBondDeviceListAdd(null);
            default:
                break;
            }
        }
    }
    public void setBtDiscoveryDeviceName(String DiscoveryName){
        btScanName = DiscoveryName;
    }
    
    public void setBtPairDeviceNameAndPin(String Name,String Pin) {
        btPairDeviceName = Name;
        pairPin = Pin;
    }

    public void startPairDevice(Context context,BluetoothDevice remoteDevice){
        boolean createBond = remoteDevice.createBond();
        Log.d(TAG, "createBond ="+createBond);
    }

    public boolean removeBond(Class<?> btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
}
