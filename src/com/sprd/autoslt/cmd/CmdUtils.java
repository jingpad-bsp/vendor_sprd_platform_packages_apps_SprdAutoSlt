package com.sprd.autoslt.cmd;

import static com.sprd.autoslt.cmd.CmdConstant.*;

import com.sprd.autoslt.common.SLTConstant;

import android.text.TextUtils;
import android.util.Log;

public class CmdUtils {

    public static Cmd parseCmd(String cmdStr) {
        if (isEmpty(cmdStr)) {
            return null;
        }

        Cmd cmd = new Cmd();
        String[] cmds = cmdStr.split(",");
        for (String s : cmds) {
            String[] keyAndValue = s.split(":");
            if (keyAndValue.length != 2) {
                throw new IllegalStateException("cmd parse error : " + cmdStr);
            }
            String key = keyAndValue[0].trim();
            String value = keyAndValue[1].trim();
            if (key.equals(Cmd.KEY_CMD)) {
                cmd.cmd = value;
            } else if (key.equals(Cmd.KEY_PARAM)) {
                cmd.param = value;
            } else if (key.equals(Cmd.KEY_STATUS)) {
                cmd.status = value;
            } else if (key.equals(Cmd.KEY_RESULT)) {
                cmd.result = value;
            }
        }

        return cmd;
    }

    public static String composeCmd(Cmd cmd) {
        if (null != cmd) {
            return cmd.toCmdString();
        }
        return null;
    }

    public static int getCmdType(Cmd cmd) {
        if (cmd.cmd.equals(CMD_OVER_STOP)) {
            return CMD_TYPE_OVER;
        } else if (isRecordResultCmd(cmd)) {
            return CMD_TYPE_RECORD_RESULT;
        } else if (isStartCmd(cmd)) {
            return CMD_TYPE_START;
        } else if (!isEmpty(cmd.status) && cmd.status.equals(STATUS_END)) {
            return CMD_TYPE_END;
        } else {
            return CMD_TYPE_UNKNOW;
        }
    }

    public static boolean isRecordResultCmd(Cmd cmd) {
        if (isEmpty(cmd.cmd)) {
            throw new IllegalStateException("cmmand can not empty!");
        }
        return cmd.cmd.equals(SLTConstant.ACTION_TYPE_RECORD_RESULT);
    }

    public static boolean isStartCmd(Cmd cmd) {
        if (!isEmpty(cmd.status) || !isEmpty(cmd.result)) {
            return false;
        }

        if (isEmpty(cmd.cmd)) {
            throw new IllegalStateException("cmmand can not empty!");
        }
        return true;
    }

    private static final boolean isEmpty(String s) {
        if (s != null && s.length() > 0) {
            return false;
        }
        return true;
    }

    public static Cmd getOKCmd(String cmd) {
        Cmd okCmd = new Cmd();
        okCmd.cmd = cmd;
        okCmd.status = STATUS_OK;

        return okCmd;
    }

    public static Cmd getEndCmd(String cmd) {
        Cmd endCmd = new Cmd();
        endCmd.cmd = cmd;
        endCmd.status = STATUS_OK;// STATUS_END

        return endCmd;
    }

    public static Cmd getEndResultCmd(String cmd, String result) {
        Cmd resultCmd = getEndCmd(cmd);
        resultCmd.result = result;

        return resultCmd;
    }

    public static Cmd getErrorCmd(String cmd) {
        Cmd errorCmd = new Cmd();
        errorCmd.cmd = cmd;
        errorCmd.status = STATUS_ERROR;

        return errorCmd;
    }

    public static boolean checkRecordResultCmd(String obj) {
        String[] keyValue = obj.split("\\^");
        Log.d("huasong",
                "checkRecordResultCmd: " + obj + " keyValue:"
                        + obj.contains("^") + "keyValue[1]:" + keyValue[1]);
		Log.d("huasong","keyValue.length = " +keyValue.length);
        if (TextUtils.isEmpty(obj)
                || (keyValue.length != 3 && keyValue.length != 2)
                || (!keyValue[1].equalsIgnoreCase("pass")
                        && !keyValue[1].equalsIgnoreCase("fail")
                        && !keyValue[1].equalsIgnoreCase("error") && !keyValue[1]
                            .equalsIgnoreCase("nt"))) {
            return false;
        }
        return true;
    }

}
