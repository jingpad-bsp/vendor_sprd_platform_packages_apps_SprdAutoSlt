package com.sprd.autoslt.cmd;

import com.sprd.autoslt.common.SLTConstant;

public class CmdConstant implements SLTConstant {

    public static final String CMD_OVER_STOP = "-1";
    public static final String CMD_READY = "0";

    public static final String CMD_VIDEO = ACTION_TYPE_VIDEO;
    public static final String CMD_CAMERA = ACTION_TYPE_CAMERA;
    public static final String CMD_VIDEOCAMERA = ACTION_TYPE_VIDEOCAMERA;
    public static final String CMD_MIC_HEADPHONE = ACTION_TYPE_MIC_HEADPHONE;
    public static final String CMD_MIC_SPREAKER = ACTION_TYPE_MIC_SPREAKER;
    public static final String CMD_MIC_HORN = ACTION_TYPE_MIC_HORN;
    public static final String CMD_PHONE_NETWORK = ACTION_TYPE_PHONE_NETWORK;
    public static final String CMD_PHONE_NETWORK_2G = ACTION_TYPE_PHONE_NETWORK_2G;
    public static final String CMD_PHONE_NETWORK_3G = ACTION_TYPE_PHONE_NETWORK_3G;
    public static final String CMD_PHONE_NETWORK_4G = ACTION_TYPE_PHONE_NETWORK_4G;
    public static final String CMD_FM = ACTION_TYPE_FM;
    public static final String CMD_WIFI = ACTION_TYPE_WIFI;
    public static final String CMD_BT = ACTION_TYPE_BT;
    public static final String CMD_GPS = ACTION_TYPE_GPS;

    public static final String STATUS_OK = "ok";
    public static final String STATUS_END = "end";
    public static final String STATUS_ERROR = "error";

    public static final int CMD_TYPE_START = 1;
    public static final int CMD_TYPE_END = CMD_TYPE_START + 1;
    public static final int CMD_TYPE_OVER = CMD_TYPE_END + 1;
    public static final int CMD_TYPE_RECORD_RESULT = CMD_TYPE_OVER + 1;
    public static final int CMD_TYPE_UNKNOW = -1;
}
