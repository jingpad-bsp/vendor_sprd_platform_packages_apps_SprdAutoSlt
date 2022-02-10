package com.sprd.autoslt.cmd;

import android.text.TextUtils;

public class Cmd {

    public static final String KEY_CMD = "cmd";
    public static final String KEY_PARAM = "param";
    public static final String KEY_STATUS = "status";
    public static final String KEY_RESULT = "result";

    public String cmd;
    public String param;
    public String param1;
    public String param2;
    public String status;
    public String result;

    public Cmd(String cmd, String param, String status, String result) {
        this.cmd = cmd;
        this.param = param;
        this.status = status;
        this.result = result;
    }

    public Cmd(String cmd, String param, String param1, String status, String result) {
        this.cmd = cmd;
        this.param = param;
        this.param1 = param1;
        this.status = status;
        this.result = result;
    }

    public Cmd(String cmd, String param, String param1, String param2, String status, String result) {
        this.cmd = cmd;
        this.param = param;
        this.param1 = param1;
        this.param2 = param2;
        this.status = status;
        this.result = result;
    }

    public Cmd() {
        cmd = "";
        param = "";
        param1 = "";
        param2 = "";
        status = "";
        result = "";
    }

    @Override
    public String toString() {
        if (TextUtils.isEmpty(cmd)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(KEY_CMD).append(":").append(cmd);
        sb.append("][");
        sb.append(KEY_PARAM).append(":").append(param);
        sb.append("][");
        sb.append(KEY_STATUS).append(":").append(status);
        sb.append("][");
        sb.append(KEY_RESULT).append(":").append(result);
        sb.append("]");
        return sb.toString();
    }

    public String toCmdString() {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        sb.append(KEY_CMD).append(":").append(cmd);

        if (!TextUtils.isEmpty(param)) {
            sb.append(",").append(KEY_PARAM).append(":").append(param);
        }

        if (!TextUtils.isEmpty(status)) {
            sb.append(",").append(KEY_STATUS).append(":").append(status);
        }

        if (!TextUtils.isEmpty(result)) {
            sb.append(",").append(KEY_RESULT).append(":").append(result);
        }

        return sb.toString();
    }
}
