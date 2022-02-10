package com.sprd.autoslt.action.impl;

import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.SLTUtil;

public class InitTestcaseAction extends AbstractAction {
    private static final String TAG = "InitTestcaseAction";

    private EngSqlite mEngSqlite = null;

    public InitTestcaseAction(StatusChangedListener listener) {
        super(listener);
        mEngSqlite = EngSqlite.getInstance(mContext);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "InitTestcaseAction:" + param);
        String[] params = SLTUtil.parseParam(param);
        if(params != null){
            int len = params.length;
            Log.d(TAG, "InitTestcaseAction len:" + len);
            if(len > 1){
                String station = params[0];
                if(mEngSqlite.queryStation(station)){
                    //1.delete all testcase in station
                    mEngSqlite.deleteByStation(station);
                }else{
                    Log.d(TAG, "station not init!");
                }
                //2.update new testcase
                String[] testCases = new String[len - 1];
                for(int i=0; i < len - 1; i++){
                    testCases[i] = params[i + 1];
                    mEngSqlite.updateData(station, testCases[i], "NT", "");
                }
            }else{
                if(len == 1){
                    String station = params[0];
                    if(mEngSqlite.queryStation(station)){
                        //1.delete all testcase in station
                        mEngSqlite.deleteByStation(station);
                    }else{
                        Log.d(TAG, "station not init!");
                    }
                }else{
                    Log.d(TAG, "params == NULL");
                    error("param invalided!");
                }
            }
        }else{
            Log.d(TAG, "params == NULL");
            error("params == NULL!");
        }
        ok();
    }

    @Override
    public void stop() {
    }
}
