package com.sprd.autoslt.action.impl;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.WifiAutoConnectManager;

public class ShutDownAction extends AbstractAction{
	private static final String TAG = "ShutDown";

	public ShutDownAction(StatusChangedListener listener) {
		super(listener);
	}

	@Override
	public void start(String param) {
		Log.d(TAG, "broadcast->shutdown");
		try {
			colseWifi(mContext);
			Intent shutdown = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
			shutdown.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
			shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(shutdown);
		} catch (Exception e) {
			e.printStackTrace();
			error("shut down error");
			return;
		}
		ok();
	}

	@Override
	public void stop() {
	}

	private void colseWifi(Context context){
		Log.d(TAG, "colseWifi start");
		try {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiAutoConnectManager manager = WifiAutoConnectManager.newInstance(wifiManager);
			int networkId = WifiAutoConnectManager.getNetworkId();
			Log.d(TAG, "colseWifi networkId="+networkId);
			if(networkId < 0){
				String ssid = WifiAutoConnectManager.getBSSID();
				Log.d(TAG, "colseWifi networkId="+networkId);
				wifiManager.disableEphemeralNetwork(ssid);
			}else{
				manager.forgetWifi(networkId);
			}
			manager.closeWifi();	
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
