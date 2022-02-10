package com.sprd.autoslt.util;

public class TestItem {
    private String mTestID;
    private String mTestStation;
    private String mTestCase;
    private String mTestResult;
    private String mTestNote;

    public void setTestID(String id) {
        this.mTestID = id;
    }

    public void setTestCase(String testcase) {
        this.mTestCase = testcase;
    }

    public void setTestResult(String result) {
        this.mTestResult = result;
    }

    public void setmTestNote(String mTestNote) {
        this.mTestNote = mTestNote;
    }

    public void setTestStation(String mTestStation) {
        this.mTestStation = mTestStation;
    }

    public String getTestID() {
        return this.mTestID;
    }

    public String getTestCase() {
        return this.mTestCase;
    }

    public String getTestResult() {
        return this.mTestResult;
    }

    public String getmTestNote() {
        return this.mTestNote;
    }

    public String getTestStation() {
        return this.mTestStation;
    }

}
