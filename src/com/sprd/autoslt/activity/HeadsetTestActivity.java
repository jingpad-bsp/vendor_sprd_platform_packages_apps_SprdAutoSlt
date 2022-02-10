
package com.sprd.autoslt.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.sprd.autoslt.R;
import com.sprd.autoslt.util.TestResultUtil;

public class HeadsetTestActivity extends Activity {
    private static final String TAG = "HeadsetTestActivity";
    public Handler mHandler = new Handler();
    public boolean shouldBack = false;
    public static HeadsetTestActivity instance;
    
    private TextView mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.headset_test);
        mResult = (TextView) findViewById(R.id.result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(earphonePluginReceiver, filter);
        TestResultUtil.getInstance().reset();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(earphonePluginReceiver);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("huasong", "keyCode:" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            TestResultUtil.getInstance().setCurrentStepName("hook");
            mResult.setText("hook");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
        	TestResultUtil.getInstance().setCurrentStepName("vol+");
            mResult.setText("vol+");
        	return true;
		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			TestResultUtil.getInstance().setCurrentStepName("vol-");
            mResult.setText("vol-");
        	return true;
		}
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver earphonePluginReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent earphoneIntent) {
            if (earphoneIntent != null && earphoneIntent.getAction() != null) {
                Log.i(TAG,
                        "earphonePluginReceiver action : "
                                + earphoneIntent.getAction());
                if (earphoneIntent.getAction().equalsIgnoreCase(
                        Intent.ACTION_HEADSET_PLUG)) {
                    int st = 0;
                    st = earphoneIntent.getIntExtra("state", 0);
                    if(st > 0) {
                        TestResultUtil.getInstance().setCurrentStepName("PlugIn");
                        mResult.setText("PlugIn");
                    } else {
                        TestResultUtil.getInstance().setCurrentStepName("PlugOut");
                        mResult.setText("PlugOut");
                    }
                }
            }
        }
    };
}
