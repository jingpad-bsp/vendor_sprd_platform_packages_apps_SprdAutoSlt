
package com.sprd.autoslt.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.util.Log;

import com.sprd.autoslt.R;
import com.sprd.autoslt.util.TestResultUtil;

public class TPButtonTestActivity extends Activity {
    private static final String TAG = "KeyTestActivity";
    private ImageButton mBackButton;
    private ImageButton mMenuButton;
    private ImageButton mHomeButton;
    private byte keyPressedFlag = 0;
    private byte keySupportFlag = 0;
    private boolean isHideCamera = false;
    private AlertDialog mCameraConfirmDialog;
    public Handler mHandler = new Handler();
    public boolean shouldBack = false;
    public static TPButtonTestActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.tp_button_test);
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mMenuButton = (ImageButton) findViewById(R.id.menu_button);
        mHomeButton = (ImageButton) findViewById(R.id.home_button);
        TestResultUtil.getInstance().reset();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent != null) {
            if(intent.getBooleanExtra("stop", false)) {
                finish();
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode = "+keyCode);
        TestResultUtil.getInstance();
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            mBackButton.setPressed(true);
            keyPressedFlag |= 1;
            TestResultUtil.getInstance().setCurrentStepName("back");
        } else if (999 ==keyCode ||KeyEvent.KEYCODE_MENU == keyCode || KeyEvent.KEYCODE_APP_SWITCH == keyCode) {
            mMenuButton.setPressed(true);
            keyPressedFlag |= 2;
            TestResultUtil.getInstance().setCurrentStepName("menu");
        } else if (KeyEvent.KEYCODE_HOME == keyCode) {
            mHomeButton.setPressed(true);
            keyPressedFlag |= 4;
            TestResultUtil.getInstance().setCurrentStepName("home");
        }else if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            finish();
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
}
