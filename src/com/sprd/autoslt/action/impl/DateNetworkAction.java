package com.sprd.autoslt.action.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;

public class DateNetworkAction extends AbstractAction{
	private static final String TAG = "DateNetworkAction";
	private static DateNetworkAction instance;
    private static String mType;
    public TelephonyManager teleManager;
    private final int dataSubId;
    private int simCardCount;

	private DateNetworkAction(StatusChangedListener listener, String type) {
		super(listener);
		// TODO Auto-generated constructor stub
		mType = type;
		teleManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
		simCardCount = getSimCardCount();
	}

	public static DateNetworkAction getInstance(StatusChangedListener listener, String type){
		mType = type;
		if (instance == null) {
			instance = new DateNetworkAction(listener, type);
		}
		return instance;
	}
	@Override
	public void start(String param) {
		// TODO Auto-generated method stub
		Log.d(TAG, "mType:" + mType);
		Log.d(TAG, "simCardCount:" + simCardCount);
		if (simCardCount == 0) {
			error("status error");
			return;
		}
		if (mType.equals(SLTConstant.ACTION_TYPE_SET_DATA_SWITCH_ON)) {
			Log.d(TAG, "getDataEnabled11:" +teleManager.getDataEnabled(dataSubId));
			if (!teleManager.getDataEnabled(dataSubId)) {
				teleManager.setDataEnabled(dataSubId,true);
				Log.d(TAG, "getDataEnabled22:" +teleManager.getDataEnabled(dataSubId));
				if (teleManager.getDataEnabled(dataSubId)) {
					ok();
				}else {
					end("fail");
				}
			}else if (teleManager.getDataEnabled(dataSubId)) {
				ok();
			}
		/*	Log.d(TAG, "getDataEnabled11:" +getDataEnabled());
		 * if (!getDataEnabled()) {
				setDataEnabled(true);
				Log.d(TAG, "getDataEnabled22:" +getDataEnabled());
				if (getDataEnabled()) {
					ok();
				}else {
					end("fail");
				}
				
			}else if (getDataEnabled()) {
				ok();
			}
		*/	
		}else if (mType.equals(SLTConstant.ACTION_TYPE_SET_DATA_SWITCH_OFF)) {		
			Log.d(TAG, "getDataEnabled33:" +teleManager.getDataEnabled(dataSubId));
			if (teleManager.getDataEnabled(dataSubId)) {
				teleManager.setDataEnabled(dataSubId,false);
				Log.d(TAG, "getDataEnabled44:" +teleManager.getDataEnabled(dataSubId));
				if (!teleManager.getDataEnabled(dataSubId)) {
					ok();
				}else {
					end("fail");
				}				
			}else if (!teleManager.getDataEnabled(dataSubId)) {
				ok();
			}
		
			
/*			Log.d(TAG, "getDataEnabled33:" +getDataEnabled());
			if (getDataEnabled()) {
				setDataEnabled(false);
				Log.d(TAG, "getDataEnabled44:" +getDataEnabled());
				if (!getDataEnabled()) {
					ok();
				}else {
					end("fail");
				}				
			}else if (!getDataEnabled()) {
				ok();
			}
*/		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean getDataEnabled(){
		try {
			Method mGetMethod = teleManager.getClass().getMethod("getDataEnabled", int.class);
			boolean isOpen = (Boolean) mGetMethod.invoke(teleManager, dataSubId);
			return isOpen;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void setDataEnabled(boolean enable) {
		try {
			Method mSetMethod = teleManager.getClass().getMethod("setDataEnabled",int.class,boolean.class);
			mSetMethod.invoke(teleManager,dataSubId, enable);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public boolean hasIccCard(int slot){
		try {
			Method mHasIccMethod = teleManager.getClass().getMethod("getDataEnabled", int.class);
			boolean isHas = (Boolean) mHasIccMethod.invoke(teleManager, slot);
			return isHas;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public int getSimCardCount(){
		int count =0;
		for (int i = 0; i < teleManager.getPhoneCount(); i++) {
			if (teleManager.hasIccCard(i)/*hasIccCard(i)*/) {
				count ++;
			}
		}
		return count;
	}

}
