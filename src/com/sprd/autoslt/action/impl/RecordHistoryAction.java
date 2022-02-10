package com.sprd.autoslt.action.impl;

import java.util.ArrayList;

import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.SLTActivity;
import com.sprd.autoslt.SLTApplication;
import com.sprd.autoslt.SLTLogManager;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.PhaseCheckParse;
import com.sprd.autoslt.util.TestItem;

public class RecordHistoryAction extends AbstractAction {
    private static final String TAG = "RecordHistoryAction";
    private static RecordHistoryAction instance;
    private static String mType;
    private EngSqlite mEngSqlite;
    private ArrayList<TestItem> historyStationList = new ArrayList<TestItem>();

    private RecordHistoryAction(StatusChangedListener listener) {
        super(listener);
        mEngSqlite = EngSqlite.getInstance(SLTApplication.getApplication().getApplicationContext());
    }
    
    public static RecordHistoryAction getInstance(StatusChangedListener listener, String type) {
        mType = type;
        if(instance == null) {
            instance = new RecordHistoryAction(listener); 
        }
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "RecordHistoryAction:" + param);
        if(SLTConstant.ACTION_TYPE_RECORD_HISTORY_INFO.equals(mType)) {
            if(TextUtils.isEmpty(param)) {
                error("param error!");
                return;
            }
            try {
                String[] keyValue = param.split("\\^");
                if(keyValue.length != 3) {
                    error("param error!");
                    return;
                }
                if(!keyValue[2].equalsIgnoreCase("pass") && !keyValue[2].equalsIgnoreCase("fail")&& !keyValue[2].equalsIgnoreCase("go")&& !keyValue[2].equalsIgnoreCase("nt")) {
                    error("param error!");
                    return;
                }
                mEngSqlite.updateHistoryData(keyValue[0], keyValue[1], keyValue[2]);
                if (keyValue[0].equalsIgnoreCase("MMI")||keyValue[0].equalsIgnoreCase("Camera")||keyValue[0].equalsIgnoreCase("Audio")
				    /*||keyValue[0].equalsIgnoreCase("Antenna")||keyValue[0].equalsIgnoreCase("WCN")*/) {
					if (keyValue[2].equalsIgnoreCase("pass")) {
						keyValue[2] = "PASS";
					}else if (keyValue[2].equalsIgnoreCase("fail")) {
						keyValue[2] = "FAIL";
					}else if (keyValue[2].equalsIgnoreCase("go")) {
						keyValue[2] = "GO";
					}else if (keyValue[2].equalsIgnoreCase("nt")) {
						keyValue[2] = "NT";
					}
                	mEngSqlite.getMap().put(keyValue[0], keyValue[2]);
					mEngSqlite.updateStationData(keyValue[0], keyValue[2]);
				}
				SLTLogManager.updateRecordHistoryInfo(param);  
                ok();                                           
            } catch (Exception e) {
                error(e.toString());
                Log.d(TAG, "error", e);
            }
        } else if(SLTConstant.ACTION_TYPE_GET_HISTORY_INFO.equals(mType)) {
        	if(TextUtils.isEmpty(param)) {
                error("param error!");
                return;
            }
            try {
            	String result = null;
            	String case_id = null;
            	historyStationList = mEngSqlite.queryHistoryData();
            	if (historyStationList.size()>0) {
            		for (int i = 0; i < historyStationList.size(); i++) {
    					if (param .equalsIgnoreCase(historyStationList.get(i).getTestCase())) {
    						result = historyStationList.get(i).getTestResult();
    						case_id = historyStationList.get(i).getTestID();
    					}
    				}
				}
            	Log.d(TAG, "RecordHistoryAction:result = " + result);
                if(TextUtils.isEmpty(result) || TextUtils.isEmpty(case_id)) {
                    end("null^" + SLTActivity.SN_number + "^NT");
                } else {
                	end(case_id+"^"+ SLTActivity.SN_number +"^"+result);
                }
            } catch (Exception e) {
                error(e.toString());
                Log.d(TAG, "error", e);
            }
        } else {
            error("invalid cmd!");
        }
    }

    @Override
    public void stop() {
    }
}
