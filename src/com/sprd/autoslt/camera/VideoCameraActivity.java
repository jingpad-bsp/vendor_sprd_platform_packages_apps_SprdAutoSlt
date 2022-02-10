package com.sprd.autoslt.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sprd.autoslt.R;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.TestResultUtil;

public class VideoCameraActivity extends Activity {

	private static final String TAG = "VideoCameraActivity";
	private MovieRecorderView movieRV;
	public static boolean mIsActivityInFront = false;
	private Button mStartRecordBtn, mFlashLightBtn;
	public static String FLASH_MODE = "off";
	public static final String FLASH_MODE_OFF = "off";
	public static final String FLASH_MODE_ON = "torch";
	private static final String FLASH_MODE_NAME = "flash_mode";
	private SharedPreferences mPrefs;
	private boolean mIsClick = false;
	private boolean mIsAuto = false;
	private String mFileName = null;
	private Handler mHandler = null;
	private SharedPreferences.Editor mEditor;
	public static VideoCameraActivity instance;
	// public static boolean mIsRecording = false;

	private Runnable mR1 = new Runnable() {
		public void run() {
			movieRV.record(new MovieRecorderView.OnRecordFinishListener() {
				@Override
				public void onRecordFinish() {

				}
			});
			// mHandler.postDelayed(mR2, 7000);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_video_layout);
		mIsActivityInFront = true;
		mPrefs = Util.getSharedPreferences(this);
		initViews();
		initEvents();
		mHandler = new Handler();
		Intent intent = getIntent();
		mIsAuto = intent.getBooleanExtra("isAutoVedio", false);
		mFileName = intent.getStringExtra("mFileName");
		FLASH_MODE = mPrefs.getString(FLASH_MODE_NAME, FLASH_MODE_OFF);

		if (FLASH_MODE.equals(FLASH_MODE_OFF)) {
			mFlashLightBtn
					.setBackgroundResource(R.drawable.ic_flash_off_holo_light);
		} else if (FLASH_MODE.equals(FLASH_MODE_ON)) {
			mFlashLightBtn
					.setBackgroundResource(R.drawable.ic_flash_on_holo_light);
		}
		mEditor = mPrefs.edit();
		mEditor.putBoolean("mIsAuto", mIsAuto);
		mEditor.putString("mFileName", mFileName);
		mEditor.apply();
		if (mIsAuto) {
			mHandler.postDelayed(mR1, 1000);
		}
		TestResultUtil.getInstance().reset();
		instance = this;
	}

	private void initViews() {
		movieRV = (MovieRecorderView) findViewById(R.id.moive_rv);
		mFlashLightBtn = (Button) findViewById(R.id.camera_fashlight_1);
		mStartRecordBtn = (Button) findViewById(R.id.start_btn);
	}

	private void initEvents() {

		mStartRecordBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsAuto) {
					Log.d(TAG, "Automatic Slt being tested!");
					return;
				}

				if (mIsClick) {
					movieRV.stopRecording();
					mIsClick = false;
				} else {
					movieRV.record(new MovieRecorderView.OnRecordFinishListener() {
						@Override
						public void onRecordFinish() {

						}
					});
					mIsClick = true;
				}

			}
		});
	}

	@Override
	protected void onPause() {

		if (movieRV != null) {
			movieRV.stop();
		}
		mIsActivityInFront = false;
		super.onPause();
	}

	public void stop() {
		if (movieRV != null) {
			movieRV.stop();
		}
	}

	public void stopRecord() {
		if (movieRV != null) {
			movieRV.stopRecording();
		}
		mIsActivityInFront = false;
	}
}
