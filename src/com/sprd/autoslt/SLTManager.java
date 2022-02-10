package com.sprd.autoslt;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction.StatusChangedListener;
import com.sprd.autoslt.action.AbstractBackGroundAction;
import com.sprd.autoslt.action.AbstractBackGroundAction.BackStatusChangedListener;
import com.sprd.autoslt.action.ActionManager;
import com.sprd.autoslt.cmd.Cmd;
import com.sprd.autoslt.cmd.CmdConstant;
import com.sprd.autoslt.cmd.CmdUtils;
import com.sprd.autoslt.connect.AbstractServer.CmdReceiverListener;
import com.sprd.autoslt.connect.IServer;
import com.sprd.autoslt.connect.impl.SLTServer;

public final class SLTManager {

    private static final String TAG = "SLTManager";

    private IServer mServer;
    private ActionManager mAction;

    public SLTManager(Context context) {

        mServer = new SLTServer(mCmdReceiverListener);
        mAction = new ActionManager(mStatusChangedListener, context);

        mServer.start();
    }

    public void setBackStatusChangedListener(BackStatusChangedListener listener){
        if(mAction != null){
            mAction.setBackStatusChangedListener(listener);
        }
    }
    public ArrayList<AbstractBackGroundAction> getAllBackGroundAction(){
        if(mAction != null){
            return mAction.getAllBackGroundAction();
        }
        return null;
    }

    public boolean isStoped() {
        return mServer.isStoped();
    }

    public void restart() {

        if (isStoped()) {
            mServer.start();
        }
    }

    private void receiverCmd(String cmdStr) {
        boolean hasError = false;
        try {
            Cmd cmd = CmdUtils.parseCmd(cmdStr);

            if (cmd == null) {
                Log.w(TAG, "SLTManager receiverCmd empty cmd");
                return;
            }
            Log.d(TAG, "SLTManager receiverCmd : " + cmd.toString());

            int cmdType = CmdUtils.getCmdType(cmd);
            switch (cmdType) {
            case CmdConstant.CMD_TYPE_RECORD_RESULT:
                Log.d(TAG, "cmdType: " + "CMD_TYPE_RECORD_RESULT");
                if (!CmdUtils.checkRecordResultCmd(cmd.param)) {
                    Log.d(TAG, "checkRecordResultCmd: " + "error!!!");
                    SLTLogManager.sendLog("<--Invalid params:" + cmd.param);
                    sendCmd(CmdUtils
                            .getErrorCmd(mAction.getCurrentActionType()));
                } else {
                    Log.d(TAG, "checkRecordResultCmd: " + "sendUpdate");
                    SLTLogManager.sendUpdate(cmd.param);
                    boolean startResult = mAction.start(cmd.cmd, cmd.param);
                    if (!startResult) {
                        hasError = true;
                        Log.d(TAG, "hasError111 " + !startResult);
                    }
                }
                break;
            case CmdConstant.CMD_TYPE_START:
                Log.d(TAG, "cmdType: " + "CMD_TYPE_START");
                boolean startResult = mAction.start(cmd.cmd, cmd.param);
                if (!startResult) {
                    hasError = true;
                    Log.d(TAG, "hasError111 " + !startResult);
                }
                break;
            case CmdConstant.CMD_TYPE_END:
                Log.d(TAG, "cmdType: " + "CMD_TYPE_END");
                mAction.stop(cmd.cmd);
                break;
            case CmdConstant.CMD_TYPE_OVER:
                Log.d(TAG, "cmdType: " + "CMD_TYPE_OVER");
                mServer.stop();
                break;
            default:
                throw new IllegalArgumentException("Unknow cmd type!");
            }
        } catch (Exception e) {
            Log.e(TAG, "receiverCmd find Exception : ", e);
            hasError = true;
            Log.d(TAG, "hasError222 ");
        } finally {
            if (hasError) {
                sendCmd("cmd:invalid");
                Log.d(TAG, "getErrorCmd");
            }
        }
    }

    private void sendCmd(Cmd cmd) {
        Log.d(TAG, "sendCmd: cmd:" + cmd);
        mServer.send(CmdUtils.composeCmd(cmd));
    }

    private void sendCmd(String cmd) {
        Log.d(TAG, "sendCmd: cmd:" + cmd);
        mServer.send(cmd);
    }

    CmdReceiverListener mCmdReceiverListener = new CmdReceiverListener() {

        @Override
        public void onCmdReceiver(String cmdStr) {
            receiverCmd(cmdStr);
        }
    };

    StatusChangedListener mStatusChangedListener = new StatusChangedListener() {

        @Override
        public void onStatusOk() {
            sendCmd(CmdUtils.getOKCmd(mAction.getCurrentActionType()));
            Log.d(TAG, "onStatusOk");
        }

        @Override
        public void onStatusEnd() {
            sendCmd(CmdUtils.getEndCmd(mAction.getCurrentActionType()));
            Log.d(TAG, "onStatusEnd");
        }

        @Override
        public void onStatusResult(String result) {
            sendCmd(CmdUtils.getEndResultCmd(mAction.getCurrentActionType(),
                    result));
            Log.d(TAG, "onStatusResult");
        }

        @Override
        public void onStatusError(String errorMessage) {
            sendCmd(CmdUtils.getErrorCmd(mAction.getCurrentActionType()));
            Log.d(TAG, "onStatusError");
        }

        @Override
        public void onStatusResult(String cmdString, String result) {
            sendCmd(CmdUtils.getEndResultCmd(cmdString,result));
            Log.d(TAG, "onStatusResult2");
        }
    };

    public void setMainHandler(Handler handler) {
        SLTLogManager.setHandler(handler);
    }
}
