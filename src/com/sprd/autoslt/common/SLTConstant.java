package com.sprd.autoslt.common;

import java.util.ArrayList;

import android.content.Context;

public interface SLTConstant {

    public static final String ACTION_TYPE_VIDEO = "1";
    public static final String ACTION_TYPE_CAMERA = "2";
    public static final String ACTION_TYPE_FRONT_CAMERA = "2.1";
    public static final String ACTION_TYPE_VIDEOCAMERA = "3";
    public static final String ACTION_TYPE_MIC_HEADPHONE = "4.1";
    public static final String ACTION_TYPE_MIC_SPREAKER = "4.2";
    public static final String ACTION_TYPE_MIC_HORN = "4.3";
    public static final String ACTION_TYPE_PHONE_NETWORK = "5";
    public static final String ACTION_TYPE_PHONE_NETWORK_2G = "5.1";
    public static final String ACTION_TYPE_PHONE_NETWORK_3G = "5.2";
    public static final String ACTION_TYPE_PHONE_NETWORK_4G = "5.3";
    public static final String ACTION_TYPE_FM = "6";
    public static final String ACTION_TYPE_WIFI = "7";
    public static final String ACTION_TYPE_BT = "8";
    public static final String ACTION_TYPE_GPS = "9";
    public static final String ACTION_TYPE_3D = "10";
    
    public static final String ACTION_TYPE_CHECK_BEAT = "CheckBeat";
    public static final String ACTION_TYPE_RECORD_RESULT = "RecordResult";
    public static final String ACTION_TYPE_INIT_TESTCASE = "InitTestCase";
    public static final String ACTION_TYPE_GET_VERSION_INFO = "GetVersionInfo";
    public static final String ACTION_TYPE_GET_MEMORY_INFO = "GetMemoryInfo";
    public static final String ACTION_TYPE_GET_SIM_RESULT = "GetSIMResult";
    public static final String ACTION_TYPE_GET_FLASH_INFO = "GetTFlashInfo";
    public static final String ACTION_TYPE_START_KEY_MODE = "StartKeyMode";
    public static final String ACTION_TYPE_END_KEY_MODE = "EndKeyMode";
    public static final String ACTION_TYPE_GET_KEY_RESULT = "GetKeyResult";
    public static final String ACTION_TYPE_START_HEADSET_MODE = "StartHeadsetMode";
    public static final String ACTION_TYPE_END_HEADSET_MODE = "EndHeadsetMode";
    public static final String ACTION_TYPE_GET_HEADSET_RESULT = "GetHeadsetResult";
    public static final String ACTION_TYPE_START_TP_PATTERN = "StartTPPattern";
    public static final String ACTION_TYPE_END_TP_PATTERN = "EndTPPattern";
    public static final String ACTION_TYPE_GET_TP_PATTERN_RESULT = "GetTPPatternResult";
    public static final String ACTION_TYPE_END_TP_PATTERN_RESULT = "EndTPPatternResult";
    public static final String ACTION_TYPE_START_LCD_MODE = "StartLCDMode";
    public static final String ACTION_TYPE_END_LCD_MODE = "EndLCDMode";
    public static final String ACTION_TYPE_START_VIBRATOR = "StartVibrator";
    public static final String ACTION_TYPE_END_VIBRATOR = "EndVibrator";
    public static final String ACTION_TYPE_START_FLASH_LIGHT = "StartFlashlight";
    public static final String ACTION_TYPE_END_FLASH_LIGHT = "EndFlashlight";
    public static final String ACTION_TYPE_SET_WIFI_ON = "SetWiFiOn";
    public static final String ACTION_TYPE_SET_WIFI_OFF = "SetWiFiOff";
    public static final String ACTION_TYPE_START_WIFI_CONNECT = "StartWiFiConnect";
    public static final String ACTION_TYPE_GET_WIFI_INFO = "GetWiFiInfo";
    public static final String ACTION_TYPE_SET_WIFI_AP_ON = "SetWiFiAPOn";
    public static final String ACTION_TYPE_SET_WIFI_AP_OFF = "SetWiFiAPOff";
    public static final String ACTION_TYPE_FORGET_WIFI = "ForgetWifi";
    public static final String ACTION_TYPE_SET_BT_ON = "SetBTOn";
    public static final String ACTION_TYPE_GET_BT_SCAN_INFO = "GetBTScanInfo";
    public static final String ACTION_TYPE_SET_BT_OFF = "SetBTOff";
    public static final String ACTION_TYPE_START_BT_PAIR = "StartBTPair";
    public static final String ACTION_TYPE_CANCEL_BT_PAIR ="CancelBTPair";
    public static final String ACTION_TYPE_START_CAMERA_SHOT = "StartCameraShot";
    public static final String ACTION_TYPE_START_CAMERA_VIDEO = "StartCameraVideo";
    public static final String ACTION_TYPE_END_CAMERA_VIDEO = "EndCameraVideo";
    public static final String ACTION_TYPE_START_AUDIO_RECORD = "StartAudioRecord";
    public static final String ACTION_TYPE_END_AUDIO_RECORD = "EndAudioRecord";
    public static final String ACTION_TYPE_START_AUDIO_PLAY = "StartAudioPlay";
    public static final String ACTION_TYPE_END_AUDIO_PLAY = "EndAudioPlay";
    public static final String ACTION_TYPE_PAUSE_AUDIO_PLAY = "PauseAudioPlay";
    public static final String ACTION_TYPE_RESUME_AUDIO_PLAY = "ContinueAudioPlay";
    public static final String ACTION_TYPE_GET_PROX_SENSOR_VALUE = "GetProxSensorValue";
    public static final String ACTION_TYPE_GET_LIGHT_SENSOR_VALUE = "GetLightSensorValue";
    public static final String ACTION_TYPE_GET_ACC_SENSOR_VALUE = "GetAccSensorValue";
    public static final String ACTION_TYPE_GET_MAG_SENSOR_VALUE = "GetMagSensorValue";
    public static final String ACTION_TYPE_GET_GYRO_SENSOR_VALUE = "GetGyroSensorValue";
    public static final String ACTION_TYPE_START_GET_SENSOR_VALUE = "StartGetSensorValue";
    public static final String ACTION_TYPE_END_GET_SENSOR_VALUE = "EndGetSensorValue";
    public static final String ACTION_TYPE_START_STATUS_LED = "StartStatusLED";
    public static final String ACTION_TYPE_END_STATUS_LED = "EndStatusLED";
    public static final String ACTION_TYPE_START_SENSOR_CALI = "StartSensorCalib";
    public static final String ACTION_TYPE_START_FM = "StartFM";
    public static final String ACTION_TYPE_GET_FM_RSSI = "GetFMRSSI";
    public static final String ACTION_TYPE_END_FM = "EndFM";
    public static final String ACTION_TYPE_START_MAKE_CALL = "StartMakeCall";
    public static final String ACTION_TYPE_CHECK_CALL_STATUS = "CheckCallStatus";
    public static final String ACTION_TYPE_END_CALL = "EndCall";
    public static final String ACTION_TYPE_GET_CFT_INFO = "GetCFTInfo";
    public static final String ACTION_TYPE_GET_PHASE_INFO = "GetPhaseInfo";
    public static final String ACTION_TYPE_SET_PHASECHECK = "SetPhaseCheck";
    public static final String ACTION_TYPE_START_VIDEO_PLAY = "StartVideoPlay";
    public static final String ACTION_TYPE_END_VIDEO_PLAY = "EndVideoPlay";
    public static final String ACTION_TYPE_SET_NETWORK_CELLULAR = "SetNetworkCellular";
    public static final String ACTION_TYPE_GET_NETWORK_CELLULAR = "GetNetworkCellular";
    public static final String ACTION_TYPE_START_3D_ANIMATION = "Start3DAnimation";
    public static final String ACTION_TYPE_END_3D_ANIMATION = "End3DAnimation";
    public static final String ACTION_TYPE_START_GPS_LOCATION = "StartGPSLocation";
    public static final String ACTION_TYPE_DEL_GPS_AID_DATA = "DelGPSAidData";
    public static final String ACTION_TYPE_GET_GPS_INFO = "GetGPSInfo";
    public static final String ACTION_TYPE_END_GPS_LOCATION = "EndGPSLocation";
    public static final String ACTION_TYPE_START_INTERNET_PING = "StartInternetPing";
    public static final String ACTION_TYPE_GET_INTERNET_PING = "GetPingStatus";
    public static final String ACTION_TYPE_RECORD_HISTORY_INFO = "RecordHistoryInfo";
    public static final String ACTION_TYPE_GET_HISTORY_INFO = "GetHistoryInfo";
    public static final String ACTION_TYPE_START_TPBUTTON = "StartTPButton";
    public static final String ACTION_TYPE_GET_TPBUTTON_RESULT = "GetTPButtonResult";
    public static final String ACTION_TYPE_END_TPBUTTON = "EndTPButton";
    public static final String ACTION_TYPE_SET_DATA_SWITCH_ON ="SetDataSwitchOn";
    public static final String ACTION_TYPE_SET_DATA_SWITCH_OFF ="SetDataSwitchOff";
    public static final String ACTION_TYPE_GET_POWER_INFO = "GetPowerInfo";
    public static final String ACTION_TYPE_SET_USB_CHARGE_ON = "SetUSBChargeOn";
    public static final String ACTION_TYPE_SET_USB_CHARGE_OFF = "SetUSBChargeOff";
    public static final String ACTION_TYPE_SHUT_DOWN = "PowerOff";
    public static final String ACTION_TYPE_DUAL_CAMERA_CHECK = "DualCameraCheck";
    public static final String ACTION_TYPE_FINGER_PRINT_CHECK = "FingerprintCheck";
    public static final String ACTION_TYPE_GET_FILE ="GetFile";
    public static final String ACTION_TYPE_RESET = "Reset";
    public static final String ACTION_TYPE_RTC_TEST = "GetRTCResult";
    public static final String ACTION_TYPE_OTG_TEST = "OTGCheck";

    public static final String ACTION_TYPE_START_MANUL_TEST = "StartManualItem";
    public static final String ACTION_TYPE_SHELL_SCRIPT = "ShellScript";

    public static final String ACTION_TYPE_READY = "0";
    public static final String ACTION_TYPE_UNKNOW = "-1";

    public static final String SLT_SDCARD_PATH = "/sdcard/slt";//"/storage/sdcard0/slt/";
    public static final String SLT_SD_PATH = "/storage/sdcard0/";
    public static final String SLT_INTERNAL_PATH = "/storage/emulated/0/slt/";

    public static final String DB_NAME = "/mnt/vendor/productinfo/autoslt.db";
    public static final int DB_VERSION = 1;
    public static final String ENG_STRING2INT_TABLE = "str2int";
    public static final String ENG_STRING2INT_ID = "_id";
    public static final String ENG_STRING2INT_NAME = "name";
    public static final String ENG_STRING2INT_VALUE = "value";
    public static final String ENG_STRING2INT_NOTE = "note";
    public static final String ENG_GROUPID_VALUE = "groupid";

    public static final String ENG_HISTORY_TABLE = "history_info";
    public static final String ENG_HISTORY_TABLE_ID = "id";
    public static final String ENG_HISTORY_TABLE_CASE = "case_name";
    public static final String ENG_HISTORY_TABLE_RESULT = "result";
    
    public static final String ENG_STATION_TABLE = "station_info";
    public static final String ENG_STATION_TABLE_ID = "sId";
    public static final String ENG_STATION_TABLE_NAME = "sName";
    public static final String ENG_STATION_TABLE_RESULT = "sResult";

    public static final String ENG_STRING2INT_STATION = ENG_STATION_TABLE_NAME;
    
    public static final String BOOT_FLAG = "boot_info";
    public static final String BOOT_FLAG_RESULT = "flag";
    
    public static final String WIFI_CONFIG_PATH = "/data/misc/wifi/wpa_supplicant.conf";

    public static final int UPDATE_LOG = 1;
    public static final int UPDATE_RESULT = UPDATE_LOG + 1;
    public static final int CLEAR_LOG = UPDATE_RESULT + 1;
    public static final int CLEAR_RESULT = CLEAR_LOG + 1;
    public static final int UPDATE_RECORD_RESULT = CLEAR_RESULT + 1;
    public static final int UPDATA_RECORD_HISTOTY_INFO = UPDATE_RECORD_RESULT +1;
    public static final int UPDATE_FAIL_NOTE_INFO = UPDATA_RECORD_HISTOTY_INFO +1;
    public static final int UPDATE_IMAGE_INFO = UPDATE_FAIL_NOTE_INFO +1;
    public static final int UPDATE_WIFI_TEXTVIEW = UPDATE_IMAGE_INFO +1;
    public static final int CLEAR_IMAGE_INFO = UPDATE_WIFI_TEXTVIEW +1;
}
