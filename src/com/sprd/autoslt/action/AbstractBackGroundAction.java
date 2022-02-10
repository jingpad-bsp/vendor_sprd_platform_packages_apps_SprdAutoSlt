package com.sprd.autoslt.action;

import java.util.ArrayList;

import com.sprd.autoslt.util.TestItem;

import android.util.Log;

public abstract class AbstractBackGroundAction extends AbstractAction implements IBackgroundAction {

    public static final String TAG = "AbstractBackGroundAction";
    public static final int TEST_DEFAULT = 0;
    public static final int TEST_FAIL = 1;
    public static final int TEST_PASS = 2;

    private int mTestResult = TEST_DEFAULT;
    public BackStatusChangedListener mBackStatusChangedListener;
    public TestItem mTestItem = null;

    public AbstractBackGroundAction(StatusChangedListener listener,BackStatusChangedListener listener2) {
        super(listener);
        mBackStatusChangedListener = listener2;
        mTestItem = new TestItem();
    }

    public interface BackStatusChangedListener {
        void onStatusBackgroundStart();

        void onStatusBackgroundStop();

        void onStatusTestNoteChange();

        void onBackgroundAtionsChange(ArrayList<AbstractBackGroundAction> actions);
    }

    public TestItem getTestItem(){
        return mTestItem;
    }

    public int getBackgroundTestResult() {
        Log.d(TAG, "getBackgroundTestResult mTestResult="+mTestResult);
        return mTestResult;
    }

    public void setBackgroundTestResult(int result) {
        Log.d(TAG, "setBackgroundTestResult mTestResult="+mTestResult);
        mTestResult = result;
    }

    @Override
    public void startBackground(String param) {
        mBackStatusChangedListener.onStatusBackgroundStart();
    }

    @Override
    public void stopBackground() {
        mBackStatusChangedListener.onStatusBackgroundStop();
    }

    @Override
    public void testNoteChange() {
        mBackStatusChangedListener.onStatusTestNoteChange();
    }

    public boolean isSupport(){
        return true;
    }

    public void resetResult(){
        mTestResult = TEST_DEFAULT;
    }
}
