package com.sprd.autoslt.action.impl;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.PhaseCheckParse;
import android.util.Log;

public class ChargerTestAction extends AbstractAction{
	private static String TAG = "ChargerTestAction";
	private static ChargerTestAction instance;
	private static String mType;
	private PhaseCheckParse mCheckParse;

	private ChargerTestAction(StatusChangedListener listener ,String type) {
		super(listener);
		// TODO Auto-generated constructor stub			
		mCheckParse = new PhaseCheckParse();
		
	}

	public static ChargerTestAction getInstance(StatusChangedListener listener ,String type){
		mType = type;
		Log.d(TAG, "instance = " + instance );
		if(instance == null) {			
            instance = new ChargerTestAction(listener,type);
        }		
        return instance;
	}
	
	@Override
	public void start(String param) {
		// TODO Auto-generated method stub
		Log.d(TAG, "mType = " + mType );
		if (mCheckParse == null) {
			mCheckParse = new PhaseCheckParse();
		}
		if (mType.equals(SLTConstant.ACTION_TYPE_SET_USB_CHARGE_ON)) {
			Log.d(TAG, "start charge on" );
			if (mCheckParse.writeChargeSwitch(0)) {
				ok();
			}else {
				error("false");
			}
		}else if (mType.equals(SLTConstant.ACTION_TYPE_SET_USB_CHARGE_OFF)) {
			Log.d(TAG, "stop charge on" );
			if (mCheckParse.writeChargeSwitch(1)) {
				ok();
			}else {
				error("false");
			}
		}
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
