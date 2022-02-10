package com.sprd.autoslt.action.impl;

import android.util.Log;

import com.sprd.autoslt.SLTApplication;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.SLTLogManager;

public class RecordResultAction extends AbstractAction {
    private static final String TAG = "RecordResultAction";

    public RecordResultAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "RecordResultAction:" + param);
        try {
            EngSqlite mEngSqlite = EngSqlite.getInstance(SLTApplication.getApplication().getApplicationContext());
            String[] keyValue = param.split("\\^");
            if (keyValue.length == 3) {
//              mEngSqlite.updateData(keyValue[0], keyValue[1],keyValue[2]);
                mEngSqlite.updateData(keyValue[0], keyValue[1],keyValue[2],false);
            }else if (keyValue.length == 2) {
//              mEngSqlite.updateData(keyValue[0], keyValue[1],"");
                mEngSqlite.updateData(keyValue[0], keyValue[1],"",false);
            }else {
                error("state error");
                return;
            }
            SLTLogManager.updateFailNoteInfo(param);
            ok();
        } catch (Exception e) {
            error(e.toString());
            Log.d(TAG, "error", e);
        }
    }

    @Override
    public void stop() {
    }
}
