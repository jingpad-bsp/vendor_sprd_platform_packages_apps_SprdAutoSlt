package com.sprd.autoslt.util;

public class TestResultUtil {

    private static TestResultUtil mTestResultUtil;
    private static String mCurrentStepName;
    private static String mCurrentStepStatus;
    private static String mCurrentResult;

    public static final String TEXT_PASS = "pass";
    public static final String TEXT_FAIL = "fail";
    public static final String TEXT_OK = "ok";

	public static TestResultUtil getInstance() {
        if(mTestResultUtil == null) {
            mTestResultUtil = new TestResultUtil();
        }
        return mTestResultUtil;
    }

    public void reset() {
        mCurrentStepName = null;
        mCurrentStepStatus = null;
        mCurrentResult = null;
    }
    
    public String getCurrentStepName() {
        return mCurrentStepName;
    }
    
    public String getCurrentStepStatus() {
        return mCurrentStepStatus;
    }
    
    public void setCurrentStepName(String currentStepName) {
        mCurrentStepName = currentStepName;
    }
    
    public void setCurrentStepStatus(String currentStepStatus) {
        mCurrentStepStatus = currentStepStatus;
    }
    public static String getCurrentResult() {
		return mCurrentResult;
	}

	public static void setCurrentResult(String CurrentResult) {
		mCurrentResult = CurrentResult;
	}

}
