package com.sprd.autoslt.action.impl.bg;

import com.sprd.autoslt.action.AbstractBackGroundAction;

public class BackgroundTestThread extends Thread{

    private static final String TAG = "BackgroundTestThread";
    private AbstractBackGroundAction mBackGroundAction = null;
    private String mParam = "";

    private boolean threadRunning = true;
    private boolean mTestStart = false;
    private boolean mTestStop = false;

    private String mThreadName = "";

    public BackgroundTestThread(String threadName,String param,AbstractBackGroundAction backGroundAction){
        super(threadName);
        mThreadName = threadName;
        mBackGroundAction = backGroundAction;
        mParam = param;
        threadRunning = true;
        mTestStart = false;
    }

    public String getThreadName(){
        return mThreadName;
    }

    @Override
    public void run() {
        while(threadRunning){
            if(mTestStart){
                if(mTestStop){
                    if(mBackGroundAction != null){
                        mBackGroundAction.stopBackground();
                    }
                    mTestStop = false;
                }
                if(mBackGroundAction != null){
                    mBackGroundAction.startBackground(mParam);
                }
                mTestStart = false;
            }
        }
        super.run();
    }

    public void startTest(){
        mTestStart = true;
    }

    public void stopTest(){
        mTestStop = true;
    }

    public void exitThread(){
        threadRunning = false;
    }

}
