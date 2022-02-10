package com.sprd.autoslt.fingerprint;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.action.AbstractBackGroundAction;
import com.sprd.autoslt.fingerprint.microarray.ISprdFingerDetectListener;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.TestColumns;
import com.sprd.autoslt.util.TestItem;
import com.sprd.autoslt.util.TestResultUtil;
import android.hardware.fingerprint.FingerprintManager;
import com.sprd.autoslt.R;

public class FingerprintTestAction extends AbstractBackGroundAction {

    private static final String TAG = "FingerprintTestAction";
    private static FingerprintTestAction instance = null;
    private FingerprintTestImpl mFingerprintTestImpl = null;
    private static final String TEST_COLUMS = TestColumns.FINGERPRINT;
    public static final String PARM_NAME = "Fingerprint";

    private FingerprintTestAction(StatusChangedListener listener,BackStatusChangedListener listener2) {
        super(listener,listener2);
        TestResultUtil.getInstance().reset();
//        mFingerprintTestImpl = new FingerprintTestImpl();
        initTestItem();
    }

    public static FingerprintTestAction getInstance(StatusChangedListener listener,BackStatusChangedListener listener2) {
        if(instance == null) {
            instance = new FingerprintTestAction(listener, listener2);
        }
        return instance;
    }

    private void initTestItem(){
        mTestItem.setTestCase(TAG);
        String mTestNote = mContext.getString(R.string.fingerprint_testing);
        mTestItem.setmTestNote(mTestNote);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "FingerprintTestAction start");
        FingerprintManager mFingerprintManager = (FingerprintManager) mContext
                .getSystemService(Context.FINGERPRINT_SERVICE);
        Log.d(TAG, "FingerprintTestAction mFingerprintManager="+mFingerprintManager);
        if(mFingerprintManager == null){
            Log.d(TAG, "mFingerprintManager=null");
            error("mFingerprintManager=null");
            return;
        }
        if (mFingerprintManager != null && !mFingerprintManager.isHardwareDetected()) {
            Log.d(TAG, "mFingerprintManager=false");
            error("isHardwareDetected=false");
            return;
        }
        boolean result = getBackgroundTestResult() == AbstractBackGroundAction.TEST_PASS;
        Log.d(TAG, "FingerprintTestAction result="+result);
        end(result ? TestResultUtil.TEXT_PASS : TestResultUtil.TEXT_FAIL);
    }

    @Override
    public void stop() {
        stopBackground();
    }

    private boolean mRuning = false;
    @Override
    public void startBackground(String param) {
        boolean result = false;
        try {
            resetResult();
            stop();
            if(mTestItem != null){
                mTestItem.setTestResult(String.valueOf(AbstractBackGroundAction.TEST_DEFAULT));
                mTestItem.setmTestNote("Testing...");
                testNoteChange();
            }
            mFingerprintTestImpl = new FingerprintTestImpl();
            if (mFingerprintTestImpl != null) {
                int ret = -1;
                // 1.init
                ret = mFingerprintTestImpl.factory_init();
                if (ret != 0) {
                    setTestResultWork(result);
                    return;
                }
                // 2.spi_test
                ret = mFingerprintTestImpl.spi_test();
                if (ret != 0) {
                    setTestResultWork(result);
                    return;
                }
                // 3.interrupt_test
                ret = mFingerprintTestImpl.interrupt_test();
                if (ret != 0) {
                    setTestResultWork(result);
                    return;
                }
                // 4.deadpixel_test
                ret = mFingerprintTestImpl.deadpixel_test();
                if (ret != 0) {
                    setTestResultWork(result);
                    return;
                }
                // 5.finger_detect
                mRuning = true;
                do {
                    Log.d(TAG, "Wait for finger detect!");
                    if(!mRuning || mFingerprintTestImpl == null){
                        return;
                    }
                    ret = mFingerprintTestImpl
                            .finger_detect(mFingerDetectListener);
                    Log.d(TAG, "finger_detect ret=" + ret);
                    Thread.sleep(100);
                } while (ret != 0);
                result = ret == 0;
            }else{
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTestResultWork(result);
    }

    private void setTestResultWork(boolean result){
        Log.d(TAG, "startBackground result="+result);
        setBackgroundTestResult(result ? AbstractBackGroundAction.TEST_PASS : AbstractBackGroundAction.TEST_FAIL);
        if(mTestItem != null){
            mTestItem.setTestResult(result ? String.valueOf(AbstractBackGroundAction.TEST_PASS) : String.valueOf(AbstractBackGroundAction.TEST_FAIL));
            mTestItem.setmTestNote(result ? "Test pass" : "Test fail");
            testNoteChange();
        }
        stopBackground();
    }

    @Override
    public void stopBackground() {
        mRuning = false;
        if(mFingerprintTestImpl != null){
            mFingerprintTestImpl.factory_exit();
            mFingerprintTestImpl = null;
        }
    }

    ISprdFingerDetectListener mFingerDetectListener = new ISprdFingerDetectListener() {
        @Override
        public void on_finger_detected(int status) {
            Log.d(TAG, "ISprdFingerDetectListener fd ret = " + status);
        }
    };

    public boolean isSupport() {
        FingerprintManager mFingerprintManager = (FingerprintManager) mContext
                .getSystemService(Context.FINGERPRINT_SERVICE);
        Log.d(TAG, "FingerprintTestAction mFingerprintManager="+mFingerprintManager);
        if(mFingerprintManager == null){
            Log.d(TAG, "mFingerprintManager=null");
            return false;
        }
        if (mFingerprintManager != null && !mFingerprintManager.isHardwareDetected()) {
            Log.d(TAG, "mFingerprintManager=false");
            return false;
        }
        return true;
    }
}

