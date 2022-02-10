package com.sprd.autoslt.fingerprint;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.R;

import com.sprd.autoslt.fingerprint.microarray.ISprdFingerDetectListener;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.TestColumns;

public class FingerprintTestService extends Service {

    private static final String TAG = "FingerprintTestService";
    private Context mContext = null;
    private FingerprintTestImpl mFingerprintTestImpl = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mFingerprintTestImpl = new FingerprintTestImpl();
    }

    @Override
    public void onDestroy() {
        if(mFingerprintTestImpl != null){
            mFingerprintTestImpl.factory_exit();
            mFingerprintTestImpl = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand " + intent + " flags " + flags + ",startId="
                + startId);
        if (intent == null) {
            return Service.START_STICKY;
        }
        //reset to default data
        EngSqlite.getInstance(mContext).updateData(TestColumns.FINGERPRINT, "0", "");
        FingerprintTestAsyncTask asyncTask = new FingerprintTestAsyncTask();
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        return Service.START_NOT_STICKY;
    }

    class FingerprintTestAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            if (mFingerprintTestImpl != null) {
                int ret = -1;
                // 1.init
                ret = mFingerprintTestImpl.factory_init();
                if (ret != 0) {
                    return -1;
                }
                // 2.spi_test
                ret = mFingerprintTestImpl.spi_test();
                if (ret != 0) {
                    return -1;
                }
                // 3.interrupt_test
                ret = mFingerprintTestImpl.interrupt_test();
                if (ret != 0) {
                    return -1;
                }
                // 4.deadpixel_test
                ret = mFingerprintTestImpl.deadpixel_test();
                if (ret != 0) {
                    return -1;
                }
                // 5.finger_detect
                do {
                    Log.d(TAG, "Wait for finger detect!");
                    ret = mFingerprintTestImpl
                            .finger_detect(mFingerDetectListener);
                    Log.d(TAG, "finger_detect ret=" + ret);
                } while (ret != 0);
                return ret;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != null) {
                if (result == 0) {
                    if(mHandler != null){
                        mHandler.sendEmptyMessage(TEST_SUCCESS);
                    }
                }else{
                    if(mHandler != null){
                        mHandler.sendEmptyMessage(TEST_FAIL);
                    }
                }
            }
            super.onPostExecute(result);
        }

        ISprdFingerDetectListener mFingerDetectListener = new ISprdFingerDetectListener() {
            @Override
            public void on_finger_detected(int status) {
                Log.d(TAG, "ISprdFingerDetectListener fd ret = " + status);
            }
        };
    }

    private static final int TEST_SUCCESS = 1;
    private static final int TEST_FAIL = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TEST_SUCCESS:
                Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT)
                        .show();
                EngSqlite.getInstance(mContext).updateData(TestColumns.FINGERPRINT, "2", "");
                break;
            case TEST_FAIL:
                Toast.makeText(mContext, R.string.no_finger, Toast.LENGTH_SHORT)
                        .show();
                EngSqlite.getInstance(mContext).updateData(TestColumns.FINGERPRINT, "1", "");
                break;
            default:
                break;
            }
        }
    };
}
