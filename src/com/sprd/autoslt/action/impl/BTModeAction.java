
package com.sprd.autoslt.action.impl;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.IntentFilter;
import android.os.ConditionVariable;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.BtTestUtil;
import com.sprd.autoslt.util.SLTUtil;

public class BTModeAction extends AbstractAction {

    private static final String TAG = "BTModeAction";

    BluetoothManager mBtManager;
    IntentFilter mFilter;
    String mBtDeviceName;
    private BtTestUtil btTestUtil = null;
    private List<BluetoothDevice> mBluetoothDeviceList  = new ArrayList<BluetoothDevice>();
    private List<BluetoothDevice> mBondDeviceList = new ArrayList<BluetoothDevice>();
    private static String mType;
    private static volatile BTModeAction instance;
    private static String PIN = "0000";
    private ConditionVariable mConditionVariable = new ConditionVariable(false);

    private BTModeAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
        mBtManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
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
                synchronized (mBluetoothDeviceList) {
                    if (mBluetoothDeviceList != null
                            && mBluetoothDeviceList.contains(device)) {
                        return;
                    }
                    if (device != null) {
                        mBluetoothDeviceList.add(device);
                        // mVariable.open();
                    }
                }
            }

            public void btDiscoveryFinished() {
            }

            public void btBondDeviceListAdd(BluetoothDevice device){
                Log.d(TAG, "btBondDeviceListAdd");
                if (mBondDeviceList != null &&mBondDeviceList.contains(device)) {
                }else {
                    if (device != null) {
                        mBondDeviceList.add(device);
                    }
                }
                mConditionVariable.open();
            }
        };
    }

    public static BTModeAction getInstance(StatusChangedListener listener, String type) {
        mType = type;
        //use lazy singleinstance
        if(instance == null) {
            synchronized (BTModeAction.class) {
                if(null == instance) {
                    instance = new BTModeAction(listener, type);
                }
            }
        }
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "BTModeAction:" + param + "mType:" + mType);
        btTestUtil.registerAllReceiver(mContext);
        if (mType.equals(SLTConstant.ACTION_TYPE_SET_BT_ON)) {
 //           btTestUtil.startTest(mContext);  
            btTestUtil.startBT(mContext);
            int count = 0;
            Log.d(TAG, "set_BT_on :BluetoothAdapter().getState() = " +btTestUtil.getBluetoothAdapter().getState());
            while (btTestUtil.getBluetoothAdapter().getState() != BluetoothAdapter.STATE_ON) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                count ++;
                Log.d(TAG, "count =" +count);
                if(count >100){
                    error("open BT Fail");
                    return;
                }
            }
            ok();
            
        } else if (mType.equals(SLTConstant.ACTION_TYPE_GET_BT_SCAN_INFO)) {
            /*if(TextUtils.isEmpty(param)) {
                error("status error");
                return;
            }*/
            if (!btTestUtil.isBTEnabled()) {
                error("BT is OFF");
                return;
            }
            
            //mBtDeviceName = param;
            mBtDeviceName = getParam(parseParam(param));
            Log.d(TAG, "mBtDeviceName = " +mBtDeviceName);
            btTestUtil.setBtDiscoveryDeviceName(mBtDeviceName);
            Log.d(TAG, " scan --- mBluetoothDeviceList = " +mBluetoothDeviceList +"; mBluetoothDeviceList.size ="+mBluetoothDeviceList.size());            
            Log.d(TAG, "isDiscovery scan before = "+ btTestUtil.isDiscovery());
            if (btTestUtil.isDiscovery() == false) {
                if (mBluetoothDeviceList.size()>0) {
                    mBluetoothDeviceList.clear();
                }
                btTestUtil.startTest(mContext);
                /*try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*/
                Log.d(TAG, "isDiscovery scan after= "+ btTestUtil.isDiscovery());
            }
            int scan_count =0;
            while (btTestUtil.isDiscovery() == true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                scan_count ++;
                Log.d(TAG, "scan_count =" +scan_count);
                synchronized (mBluetoothDeviceList) {
                    Log.d(TAG, "mBluetoothDeviceList.size() =" + mBluetoothDeviceList.size());
                    if (mBluetoothDeviceList.size() > 0) {
                        for (BluetoothDevice device : mBluetoothDeviceList) {
                            if (!TextUtils.isEmpty(mBtDeviceName) && mBtDeviceName != null/*mBtDeviceName.equals(device.getName())*/) {
                                //end(device.getAddress()+ "^"+ btTestUtil.getRssi(device.getName(),device.getAddress()));
                                if (mBtDeviceName.equals(device.getAddress())) {
                                    end(btTestUtil.getRssi(device.getName(),device.getAddress()));
                                    return;
                                }               
                            }
                        }
                        StringBuffer sBuffer = new StringBuffer();
                        if (TextUtils.isEmpty(mBtDeviceName)|| mBtDeviceName == null) {
                            Log.d(TAG,"mBtDeviceName = null mBluetoothDeviceList.size() = "+ mBluetoothDeviceList.size());
                            sBuffer = sBuffer.append(mBluetoothDeviceList.size() );
                            for (int i = 0; i < mBluetoothDeviceList.size(); i++) {
                                Log.d(TAG, "i =" +i +"; mBluetoothDeviceList.get(i).getAddress() = " + mBluetoothDeviceList.get(i).getAddress());
                                sBuffer.append("^" + mBluetoothDeviceList.get(i)
                                        .getAddress());
                            }
                            end(sBuffer.toString());
                            return;
                        }
                    }
                }
                if (scan_count > 20) {
                    // btTestUtil.setDiscovery(false);
                    Log.d(TAG, "2s,There is no BT device");
                    end("fail");
                    return;

                }else {
                    if (mBluetoothDeviceList.size() == 0) {
                        Log.d(TAG, "There is no BT device");
                        end("fail");
                        return;
                    }
                }
            }

            /*int i = 0;
            Log.d(TAG, "mBluetoothDeviceList.size() =" + mBluetoothDeviceList.size());
            if (mBluetoothDeviceList != null
                    && mBluetoothDeviceList.size() > 0) {
                for(BluetoothDevice device : mBluetoothDeviceList) {
                    if (mBtDeviceName.equals(device.getName())) {
                        end(device.getAddress() + "^" + btTestUtil.getRssi(device.getName(), device.getAddress()));
                        return;
                    }else {
                        i++;
                    }
                }
                if (i == mBluetoothDeviceList.size()) {
                    error("fail");
                }
            }else {
                error("fail");
            }*/
        } else if (mType.equals(SLTConstant.ACTION_TYPE_SET_BT_OFF)) {
            btTestUtil.stopTest();
            Log.d(TAG, "mBluetoothDeviceList = " +mBluetoothDeviceList +"; mBluetoothDeviceList.size ="+mBluetoothDeviceList.size());
            Log.d(TAG, "mBondDeviceList = " +mBondDeviceList +"; mBondDeviceList.size ="+mBondDeviceList.size());
            if (mBluetoothDeviceList.size()>0) {
                mBluetoothDeviceList.clear();
            }
            if (mBondDeviceList.size()>0) {
                mBondDeviceList.clear();
            }
            int off_count = 0;
            Log.d(TAG, "set_BT_off :BluetoothAdapter().getState() = " +btTestUtil.getBluetoothAdapter().getState());
            while (btTestUtil.getBluetoothAdapter().getState() != BluetoothAdapter.STATE_OFF) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                off_count ++;
                Log.d(TAG, "off_count =" +off_count);
                if(off_count >50){
                    error("close BT Fail");
                    return;
                }
            }
            ok();
        }else if (mType.equals(SLTConstant.ACTION_TYPE_START_BT_PAIR)) {
            if(TextUtils.isEmpty(param)) {
                error("status error");
                return;
            }
            if (!btTestUtil.isBTEnabled()) {
                error("BT is OFF");
                return;
            }
            String[] nameAndPsw = SLTUtil.parseParam(param);
            Log.d(TAG, "nameAndPsw.length = " +nameAndPsw.length);
            String bt_mac = null;
            if (nameAndPsw.length == 2) {
                bt_mac = getParam(parseParam(nameAndPsw[0]));
                Log.d(TAG, "startbtpair :2 bt_mac =" + bt_mac);
                btTestUtil.setBtPairDeviceNameAndPin(bt_mac,nameAndPsw[1]);
                //btTestUtil.setBtPairDeviceNameAndPin(nameAndPsw[0],nameAndPsw[1]);
            }else if (nameAndPsw.length == 1) {
                Log.d(TAG, "startbtpair :1 bt_mac =" + bt_mac);
                btTestUtil.setBtPairDeviceNameAndPin(bt_mac,PIN);
                //btTestUtil.setBtPairDeviceNameAndPin(nameAndPsw[0], PIN);
            }
    //      btTestUtil.startTest(mContext);
            BluetoothDevice remoteDevice = null;
            if (mBluetoothDeviceList != null && mBluetoothDeviceList.size() > 0) {
                Log.d(TAG, "pair --- mBluetoothDeviceList = " +mBluetoothDeviceList +"; mBluetoothDeviceList.size ="+mBluetoothDeviceList.size());
                for (BluetoothDevice device : mBluetoothDeviceList) {
                    if (bt_mac != null && bt_mac.equals(device.getAddress())/*nameAndPsw[0].equals(device.getName())*/) {
                        remoteDevice = device;
                        break;
                    }
                }
            }
            if (remoteDevice == null ) {
                error("statue error");
                return;
            }
            Log.d(TAG, "startPairDevice");
            btTestUtil.startPairDevice(mContext, remoteDevice);
            if (!mConditionVariable.block(20000)) {
                Log.d(TAG, "block fail");
                mConditionVariable.close();
                error("block fail");    
                return ;
            }
            mConditionVariable.close();
            if(remoteDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                ok();
            }else {
                error("fail");
                try {
                    btTestUtil.removeBond(remoteDevice.getClass(), remoteDevice);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            
        }else if (mType.equals(SLTConstant.ACTION_TYPE_CANCEL_BT_PAIR)) {
            if(TextUtils.isEmpty(param)) {
                error("status error");
                return;
            }
            if (!btTestUtil.isBTEnabled()) {
                error("BT is OFF");
                return;
            }
            
            String[] macString = parseParam(param);
            String bt_MacString = getParam(macString);

            if (mBondDeviceList != null && mBondDeviceList.size()>0) {
                for (BluetoothDevice device: mBondDeviceList) {
                    if (/*param.equals(device.getName())*/bt_MacString.equals(device.getAddress())&& device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        try {
                            if (btTestUtil.removeBond(device.getClass(), device)) {
                                ok();
                            }else {
                                error("fail");
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            error("fail");
                        }
                    }else {                     
                        error("the device is not paired");
                    }
                }
            }else {
                error("No device be paired");
            }
        }
    }

    public String[] parseParam(String param) {
        if (param.contains("-")) {
            return param.split("-");
        }
        return new String[] { param };
    }

    public String getParam(String[] arg){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arg.length; i++) {
            sb.append(arg[i]);
            if (i < arg.length -1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()");
        btTestUtil.unregisterAllReceiver(mContext);
    }
}
