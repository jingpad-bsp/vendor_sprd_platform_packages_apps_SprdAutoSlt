
package com.sprd.autoslt.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageButton;

import com.sprd.autoslt.R;
import com.sprd.autoslt.util.TestResultUtil;

public class KeyTestActivity extends Activity {
    private static final String TAG = "KeyTestActivity";
    private ImageButton mVolumeUpButton;
    private ImageButton mVolumeDownButton;
    private byte keyPressedFlag = 0;
    private byte keySupportFlag = 0;
    private boolean isHideCamera = false;
    private AlertDialog mCameraConfirmDialog;
    public Handler mHandler = new Handler();
    public boolean shouldBack = false;
    public static KeyTestActivity instance;
    public static boolean isActivityInFront;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.key_test);
        mVolumeUpButton = (ImageButton) findViewById(R.id.volume_up_button);
        mVolumeDownButton = (ImageButton) findViewById(R.id.volume_down_button);
        TestResultUtil.getInstance().reset();
    }

    @Override
    protected void onResume() {
        isActivityInFront = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isActivityInFront = false;
        super.onPause();
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
        if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            mVolumeUpButton.setPressed(true);
            keyPressedFlag |= 1;
            TestResultUtil.getInstance();
            TestResultUtil.getInstance().setCurrentStepName("vol+");
        } else if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            mVolumeDownButton.setPressed(true);
            keyPressedFlag |= 2;
            TestResultUtil.getInstance().setCurrentStepName("vol-");
        }else if (KeyEvent.KEYCODE_BACK == keyCode) {
            finish();
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
}
