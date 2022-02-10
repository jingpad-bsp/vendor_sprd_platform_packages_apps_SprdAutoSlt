package com.sprd.autoslt.action.impl.rtc;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractBackGroundAction;
import com.sprd.autoslt.util.TestColumns;
import com.sprd.autoslt.util.TestResultUtil;
import com.sprd.autoslt.R;

public class RTCTestAction extends AbstractBackGroundAction{
    private static final String TAG = "RTCTestAction";
    private static RTCTestAction instance = null;
    private static final int DEFAULT_TIME = 3;
    private static final String TEST_COLUMS = TestColumns.RTC_TEST;
    private static final int TEST_DESC = R.string.rtc_testing;
    public static final String PARM_NAME = "RTC";

    private RTCTestAction(StatusChangedListener listener,BackStatusChangedListener listener2) {
        super(listener,listener2);
        TestResultUtil.getInstance().reset();
        initTestItem();
    }

    private void initTestItem(){
        mTestItem.setTestCase(TAG);
        String mTestNote = mContext.getString(TEST_DESC);
        mTestItem.setmTestNote(mTestNote);
    }

    public static RTCTestAction getInstance(StatusChangedListener listener,BackStatusChangedListener listener2) {
        if(instance == null) {
            instance = new RTCTestAction(listener, listener2);
        }
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, TAG + " start");
        boolean result = getBackgroundTestResult() == AbstractBackGroundAction.TEST_PASS;;
        Log.d(TAG, TAG + " result="+result);
        end(result ? TestResultUtil.TEXT_PASS : TestResultUtil.TEXT_FAIL);
    }

    @Override
    public void stop() {
    }

    @Override
    public void startBackground(String param) {
        resetResult();
        if(mTestItem != null){
            mTestItem.setTestResult(String.valueOf(AbstractBackGroundAction.TEST_DEFAULT));
            mTestItem.setmTestNote("Testing...");
            testNoteChange();
        }
        Log.d(TAG, "RTCTestAction startBackground");
        RtcTestAsyncTask asyncTask = new RtcTestAsyncTask();
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, param);
    }

    class RtcTestAsyncTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {
            String time = params[0];
            int timeInt = DEFAULT_TIME;
            try {
                if(!TextUtils.isEmpty(time)){
                    timeInt = Integer.valueOf(time);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "RtcTestAsyncTask timeInt="+timeInt);
            long currentTime1 = System.currentTimeMillis();
            long currentTime2 = System.currentTimeMillis();
            for(int i=0;i<timeInt;i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentTime2 = System.currentTimeMillis();
            }
            int tiemAfter = (int) ((currentTime2 - currentTime1) / 1000);
            Log.d(TAG, "RtcTestAsyncTask tiemAfter="+tiemAfter);
            return tiemAfter == timeInt;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result != null){
                setBackgroundTestResult(result ? AbstractBackGroundAction.TEST_PASS : AbstractBackGroundAction.TEST_FAIL);
            }else{
                setBackgroundTestResult(AbstractBackGroundAction.TEST_FAIL);
            }
            if(mTestItem != null && result != null){
                mTestItem.setTestResult(result ? String.valueOf(AbstractBackGroundAction.TEST_PASS) : String.valueOf(AbstractBackGroundAction.TEST_FAIL));
                mTestItem.setmTestNote(result ? "Test pass" : "Test fail");
                testNoteChange();
            }
        }
    }
}

