package com.sprd.autoslt.action;

import static com.sprd.autoslt.common.SLTConstant.*;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.text.style.BackgroundColorSpan;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction.StatusChangedListener;
import com.sprd.autoslt.action.AbstractBackGroundAction.BackStatusChangedListener;
import com.sprd.autoslt.action.impl.*;
import com.sprd.autoslt.action.impl.bg.BackgroundTestAction;
import com.sprd.autoslt.action.impl.otg.OTGTestAction;
import com.sprd.autoslt.action.impl.rtc.RTCTestAction;
import com.sprd.autoslt.action.impl.shell.ShellScriptAction;
import com.sprd.autoslt.fingerprint.FingerprintTestAction;

public class ActionManager {

    private static final String TAG = "ActionManager";

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_DOING = 1;

    private int mCurrentStatus = STATUS_IDLE;

    private String mCurrentActionType = ACTION_TYPE_UNKNOW;

    private StatusChangedListener mStatusChangedListener;
    private IAction mCurrentAction;
    private Context mContext;
    //Background action
    private ArrayList<AbstractBackGroundAction> mBackGroundActions = new ArrayList<AbstractBackGroundAction>();
    private BackStatusChangedListener mBackStatusChangedListener;
    private BackgroundTestAction mBackgroundTestAction = null;

    public ActionManager(StatusChangedListener listener, Context context) {
        mStatusChangedListener = listener;
        setCurrentAction(null);
        mContext = context;
    }

    public void setBackStatusChangedListener(BackStatusChangedListener listener){
        mBackStatusChangedListener = listener;
    }

    public ArrayList<AbstractBackGroundAction> getAllBackGroundAction(){
        if(mBackgroundTestAction != null){
            mBackGroundActions = mBackgroundTestAction.getAllBackGroundAction();
        }
        return mBackGroundActions;
    }

    public boolean createAction(String actionType) {
        Log.d("huasong", "actionType" + actionType);
        if (null == mStatusChangedListener) {
            Log.e(TAG, "createAction but mStatusChangedListener is null");
            return false;
        }

        if (mCurrentAction != null && mCurrentStatus != STATUS_IDLE) {
            releseCurrentActionIfNeed();
        }

        if (ACTION_TYPE_CHECK_BEAT.equals(actionType)) {
            setCurrentAction(new CheckbeatAction(mStatusChangedListener));
        }

        if (ACTION_TYPE_GET_VERSION_INFO.equals(actionType)) {
            setCurrentAction(new GetVersionInfo(mStatusChangedListener));
        }

        if (ACTION_TYPE_RECORD_RESULT.equals(actionType)) {
            setCurrentAction(new RecordResultAction(mStatusChangedListener));
        }

        if (ACTION_TYPE_INIT_TESTCASE.equals(actionType)) {
            setCurrentAction(new InitTestcaseAction(mStatusChangedListener));
        }

        if (ACTION_TYPE_VIDEO.equals(actionType)) {
            setCurrentAction(new VideoAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_FRONT_CAMERA.equals(actionType)) {
            setCurrentAction(new FrontCameraAction(mStatusChangedListener, mContext));
        }
        if (ACTION_TYPE_CAMERA.equals(actionType)) {
            setCurrentAction(new CameraAction(mStatusChangedListener, mContext));
        }
        if (ACTION_TYPE_VIDEOCAMERA.equals(actionType)) {
            setCurrentAction(new VideoCameraAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_FM.equals(actionType)) {
            setCurrentAction(new FMAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_BT.equals(actionType)) {
            setCurrentAction(new BtAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_WIFI.equals(actionType)) {
            setCurrentAction(new WifiAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_GPS.equals(actionType)) {
            setCurrentAction(new GpsAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_GET_MEMORY_INFO.equals(actionType)) {
            setCurrentAction(new GetMemoryInfoAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_GET_SIM_RESULT.equals(actionType)) {
            setCurrentAction(GetSIMResultAction.getInstance(mStatusChangedListener));
        }
        if (ACTION_TYPE_GET_FLASH_INFO.equals(actionType)) {
            setCurrentAction(new GetFlashInfoAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_START_KEY_MODE.equals(actionType)) {
            setCurrentAction(new KeyModeAction(mStatusChangedListener, ACTION_TYPE_START_KEY_MODE));
        }
        if (ACTION_TYPE_END_KEY_MODE.equals(actionType)) {
            setCurrentAction(new KeyModeAction(mStatusChangedListener, ACTION_TYPE_END_KEY_MODE));
        }
        if (ACTION_TYPE_GET_KEY_RESULT.equals(actionType)) {
            setCurrentAction(new KeyModeAction(mStatusChangedListener, ACTION_TYPE_GET_KEY_RESULT));
        }
        if (ACTION_TYPE_START_HEADSET_MODE.equals(actionType)) {
            setCurrentAction(new HeadsetModeAction(mStatusChangedListener, ACTION_TYPE_START_HEADSET_MODE));
        }
        if (ACTION_TYPE_END_HEADSET_MODE.equals(actionType)) {
            setCurrentAction(new HeadsetModeAction(mStatusChangedListener, ACTION_TYPE_END_HEADSET_MODE));
        }
        if (ACTION_TYPE_GET_HEADSET_RESULT.equals(actionType)) {
            setCurrentAction(new HeadsetModeAction(mStatusChangedListener, ACTION_TYPE_GET_HEADSET_RESULT));
        }
        if (ACTION_TYPE_START_TP_PATTERN.equals(actionType)) {
            setCurrentAction(new TPPatternAction(mStatusChangedListener, ACTION_TYPE_START_TP_PATTERN));
        }
        if (ACTION_TYPE_END_TP_PATTERN.equals(actionType)) {
            setCurrentAction(new TPPatternAction(mStatusChangedListener, ACTION_TYPE_END_TP_PATTERN));
        }
        if (ACTION_TYPE_GET_TP_PATTERN_RESULT.equals(actionType)) {
            setCurrentAction(new TPPatternAction(mStatusChangedListener, ACTION_TYPE_GET_TP_PATTERN_RESULT));
        }
        /*if (ACTION_TYPE_END_TP_PATTERN_RESULT.equals(actionType)) {
            setCurrentAction(new TPPatternAction(mStatusChangedListener, ACTION_TYPE_END_TP_PATTERN_RESULT));
        }*/
        if (ACTION_TYPE_START_LCD_MODE.equals(actionType)) {
            setCurrentAction(new LCDModeAction(mStatusChangedListener, ACTION_TYPE_START_LCD_MODE));
        }
        if (ACTION_TYPE_END_LCD_MODE.equals(actionType)) {
            setCurrentAction(new LCDModeAction(mStatusChangedListener, ACTION_TYPE_END_LCD_MODE));
        }
        if (ACTION_TYPE_START_VIBRATOR.equals(actionType)) {
            setCurrentAction(new VibratorModeAction(mStatusChangedListener, ACTION_TYPE_START_VIBRATOR));
        }
        if (ACTION_TYPE_END_VIBRATOR.equals(actionType)) {
            setCurrentAction(new VibratorModeAction(mStatusChangedListener, ACTION_TYPE_END_VIBRATOR));
        }
        if (ACTION_TYPE_START_FLASH_LIGHT.equals(actionType)) {
            setCurrentAction(new FlashLightModeAction(mStatusChangedListener, ACTION_TYPE_START_FLASH_LIGHT));
        }
        if (ACTION_TYPE_END_FLASH_LIGHT.equals(actionType)) {
            setCurrentAction(new FlashLightModeAction(mStatusChangedListener, ACTION_TYPE_END_FLASH_LIGHT));
        }
        if (ACTION_TYPE_SET_WIFI_ON.equals(actionType)) {
            setCurrentAction(new WifiModeAction(mStatusChangedListener, ACTION_TYPE_SET_WIFI_ON));
        }
        if (ACTION_TYPE_SET_WIFI_OFF.equals(actionType)) {
            setCurrentAction(new WifiModeAction(mStatusChangedListener, ACTION_TYPE_SET_WIFI_OFF));
        }
        if (ACTION_TYPE_START_WIFI_CONNECT.equals(actionType)) {
            setCurrentAction(new WifiModeAction(mStatusChangedListener, ACTION_TYPE_START_WIFI_CONNECT));
        }
        if (ACTION_TYPE_GET_WIFI_INFO.equals(actionType)) {
            setCurrentAction(new WifiModeAction(mStatusChangedListener, ACTION_TYPE_GET_WIFI_INFO));
        }
        if (ACTION_TYPE_SET_WIFI_AP_ON.equals(actionType)) {
            setCurrentAction(new WifiModeAction(mStatusChangedListener, ACTION_TYPE_SET_WIFI_AP_ON));
        }
        if (ACTION_TYPE_SET_WIFI_AP_OFF.equals(actionType)) {
            setCurrentAction(new WifiModeAction(mStatusChangedListener, ACTION_TYPE_SET_WIFI_AP_OFF));
        }
        if (ACTION_TYPE_FORGET_WIFI.equals(actionType)) {
            setCurrentAction(new WifiModeAction(mStatusChangedListener, ACTION_TYPE_FORGET_WIFI));
        }
        if (ACTION_TYPE_SET_BT_ON.equals(actionType)) {
            setCurrentAction(BTModeAction.getInstance(mStatusChangedListener, ACTION_TYPE_SET_BT_ON));
        }
        if (ACTION_TYPE_GET_BT_SCAN_INFO.equals(actionType)) {
            setCurrentAction(BTModeAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_BT_SCAN_INFO));
        }
        if (ACTION_TYPE_SET_BT_OFF.equals(actionType)) {
            setCurrentAction(BTModeAction.getInstance(mStatusChangedListener, ACTION_TYPE_SET_BT_OFF));
        }
        if (ACTION_TYPE_START_BT_PAIR.equals(actionType)) {
            setCurrentAction(BTModeAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_BT_PAIR));
        }
        if (ACTION_TYPE_CANCEL_BT_PAIR.equals(actionType)) {
            setCurrentAction(BTModeAction.getInstance(mStatusChangedListener, ACTION_TYPE_CANCEL_BT_PAIR));
        }
        
        if (ACTION_TYPE_START_CAMERA_SHOT.equals(actionType)) {
            setCurrentAction(new CameraAction(mStatusChangedListener, mContext));
        }
        if (ACTION_TYPE_START_CAMERA_VIDEO.equals(actionType)) {
            setCurrentAction(VideoCameraAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_CAMERA_VIDEO));
        }
        if (ACTION_TYPE_END_CAMERA_VIDEO.equals(actionType)) {
            setCurrentAction(VideoCameraAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_CAMERA_VIDEO));
        }
        if (ACTION_TYPE_START_AUDIO_RECORD.equals(actionType)) {
            setCurrentAction(AudioRecordAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_AUDIO_RECORD));
        }
        if (ACTION_TYPE_END_AUDIO_RECORD.equals(actionType)) {
            setCurrentAction(AudioRecordAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_AUDIO_RECORD));
        }
        if (ACTION_TYPE_START_AUDIO_PLAY.equals(actionType)) {
            setCurrentAction(AudioPlayAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_AUDIO_PLAY));
        }
        if (ACTION_TYPE_END_AUDIO_PLAY.equals(actionType)) {
            setCurrentAction(AudioPlayAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_AUDIO_PLAY));
        }
        if (ACTION_TYPE_PAUSE_AUDIO_PLAY.equals(actionType)) {
            setCurrentAction(AudioPlayAction.getInstance(mStatusChangedListener, ACTION_TYPE_PAUSE_AUDIO_PLAY));
        }
        if (ACTION_TYPE_RESUME_AUDIO_PLAY.equals(actionType)) {
            setCurrentAction(AudioPlayAction.getInstance(mStatusChangedListener, ACTION_TYPE_RESUME_AUDIO_PLAY));
        }
        if (ACTION_TYPE_GET_PROX_SENSOR_VALUE.equals(actionType)) {
            setCurrentAction(GetSensorValueAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_PROX_SENSOR_VALUE));
        }
        if (ACTION_TYPE_GET_LIGHT_SENSOR_VALUE.equals(actionType)) {
            setCurrentAction(GetSensorValueAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_LIGHT_SENSOR_VALUE));
        }
        if (ACTION_TYPE_GET_ACC_SENSOR_VALUE.equals(actionType)) {
            setCurrentAction(GetSensorValueAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_ACC_SENSOR_VALUE));
        }
        if (ACTION_TYPE_GET_MAG_SENSOR_VALUE.equals(actionType)) {
            setCurrentAction(GetSensorValueAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_MAG_SENSOR_VALUE));
        }
        if (ACTION_TYPE_GET_GYRO_SENSOR_VALUE.equals(actionType)) {
            setCurrentAction(GetSensorValueAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_GYRO_SENSOR_VALUE));
        }
        if (ACTION_TYPE_START_GET_SENSOR_VALUE.equals(actionType)) {
            setCurrentAction(GetSensorValueAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_GET_SENSOR_VALUE));
        }
        if (ACTION_TYPE_END_GET_SENSOR_VALUE.equals(actionType)) {
            setCurrentAction(GetSensorValueAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_GET_SENSOR_VALUE));
        }
        if (ACTION_TYPE_START_STATUS_LED.equals(actionType)) {
            setCurrentAction(LedStatusAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_STATUS_LED));
        }
        if (ACTION_TYPE_END_STATUS_LED.equals(actionType)) {
            setCurrentAction(LedStatusAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_STATUS_LED));
        }
        if (ACTION_TYPE_START_SENSOR_CALI.equals(actionType)) {
            setCurrentAction(SensorCalibrationAction.getInstance(mStatusChangedListener));
        }
        if (ACTION_TYPE_START_FM.equals(actionType)) {
            setCurrentAction(FMAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_FM));
        }
        if (ACTION_TYPE_GET_FM_RSSI.equals(actionType)) {
            setCurrentAction(FMAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_FM_RSSI));
        }
        if (ACTION_TYPE_END_FM.equals(actionType)) {
            setCurrentAction(FMAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_FM));
        }
        if (ACTION_TYPE_START_MAKE_CALL.equals(actionType)) {
            setCurrentAction(CallAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_MAKE_CALL));
        }
        if (ACTION_TYPE_CHECK_CALL_STATUS.equals(actionType)) {
            setCurrentAction(CallAction.getInstance(mStatusChangedListener, ACTION_TYPE_CHECK_CALL_STATUS));
        }
        if (ACTION_TYPE_END_CALL.equals(actionType)) {
            setCurrentAction(CallAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_CALL));
        }
        if (ACTION_TYPE_GET_CFT_INFO.equals(actionType)) {
            setCurrentAction(ATAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_CFT_INFO));
        }
        if (ACTION_TYPE_GET_PHASE_INFO.equals(actionType)) {
            setCurrentAction(GetPhaseInfoAction.getInstance(mStatusChangedListener));
        }
        if (ACTION_TYPE_SET_PHASECHECK.equals(actionType)) {
            setCurrentAction(SetPhaseCheckAction.getInstance(mStatusChangedListener));
        }
        if (ACTION_TYPE_START_VIDEO_PLAY.equals(actionType)) {
            setCurrentAction(VideoAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_VIDEO_PLAY));
        }
        if (ACTION_TYPE_END_VIDEO_PLAY.equals(actionType)) {
            setCurrentAction(VideoAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_VIDEO_PLAY));
        }
        if (ACTION_TYPE_SET_NETWORK_CELLULAR.equals(actionType)) {
            setCurrentAction(NetWorkCelluarAction.getInstance(mStatusChangedListener, ACTION_TYPE_SET_NETWORK_CELLULAR));
        }
        if (ACTION_TYPE_GET_NETWORK_CELLULAR.equals(actionType)) {
            setCurrentAction(NetWorkCelluarAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_NETWORK_CELLULAR));
        }
        if (ACTION_TYPE_START_3D_ANIMATION.equals(actionType)) {
            setCurrentAction(Animation3DAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_3D_ANIMATION));
        }
        if (ACTION_TYPE_END_3D_ANIMATION.equals(actionType)) {
            setCurrentAction(Animation3DAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_3D_ANIMATION));
        }
        if (ACTION_TYPE_START_GPS_LOCATION.equals(actionType)) {
            setCurrentAction(GpsAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_GPS_LOCATION));
        }
        if (ACTION_TYPE_DEL_GPS_AID_DATA.equals(actionType)) {
            setCurrentAction(GpsAction.getInstance(mStatusChangedListener, ACTION_TYPE_DEL_GPS_AID_DATA));
        }
        if (ACTION_TYPE_GET_GPS_INFO.equals(actionType)) {
            setCurrentAction(GpsAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_GPS_INFO));
        }
        if (ACTION_TYPE_END_GPS_LOCATION.equals(actionType)) {
            setCurrentAction(GpsAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_GPS_LOCATION));
        }
        if (ACTION_TYPE_START_INTERNET_PING.equals(actionType)) {
            setCurrentAction(PingAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_INTERNET_PING));
        }
        if (ACTION_TYPE_GET_INTERNET_PING.equals(actionType)) {
            setCurrentAction(PingAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_INTERNET_PING));
        }
        if (ACTION_TYPE_RECORD_HISTORY_INFO.equals(actionType)) {
            setCurrentAction(RecordHistoryAction.getInstance(mStatusChangedListener, ACTION_TYPE_RECORD_HISTORY_INFO));
        }
        if (ACTION_TYPE_GET_HISTORY_INFO.equals(actionType)) {
            setCurrentAction(RecordHistoryAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_HISTORY_INFO));
        }
        if (ACTION_TYPE_START_TPBUTTON.equals(actionType)) {
            setCurrentAction(TPButtonAction.getInstance(mStatusChangedListener, ACTION_TYPE_START_TPBUTTON));
        }
        if (ACTION_TYPE_GET_TPBUTTON_RESULT.equals(actionType)) {
            setCurrentAction(TPButtonAction.getInstance(mStatusChangedListener, ACTION_TYPE_GET_TPBUTTON_RESULT));
        }
        if (ACTION_TYPE_END_TPBUTTON.equals(actionType)) {
            setCurrentAction(TPButtonAction.getInstance(mStatusChangedListener, ACTION_TYPE_END_TPBUTTON));
        }
        if (ACTION_TYPE_SET_DATA_SWITCH_ON.equals(actionType)) {
            setCurrentAction(DateNetworkAction.getInstance(mStatusChangedListener, ACTION_TYPE_SET_DATA_SWITCH_ON));
        }
        if (ACTION_TYPE_SET_DATA_SWITCH_OFF.equals(actionType)) {
            setCurrentAction(DateNetworkAction.getInstance(mStatusChangedListener, ACTION_TYPE_SET_DATA_SWITCH_OFF));
        }
        if (ACTION_TYPE_GET_POWER_INFO.equals(actionType)) {
            setCurrentAction(GetPowerInfoAction.getInstance(mStatusChangedListener));
        }
        if (ACTION_TYPE_SET_USB_CHARGE_ON.equals(actionType)) {
            setCurrentAction(ChargerTestAction.getInstance(mStatusChangedListener, ACTION_TYPE_SET_USB_CHARGE_ON));
        }
        if (ACTION_TYPE_SET_USB_CHARGE_OFF.equals(actionType)) {
            setCurrentAction(ChargerTestAction.getInstance(mStatusChangedListener, ACTION_TYPE_SET_USB_CHARGE_OFF));
        }
        if (ACTION_TYPE_SHUT_DOWN.equals(actionType)) {
            setCurrentAction(new ShutDownAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_DUAL_CAMERA_CHECK.equals(actionType)) {
            setCurrentAction(DualCameraCheckAction.getInstance(mStatusChangedListener));
        }
        if (ACTION_TYPE_FINGER_PRINT_CHECK.equals(actionType)) {
            //setCurrentAction(FingerPrintAction.getInstance(mStatusChangedListener));
            setCurrentAction(FingerprintTestAction.getInstance(mStatusChangedListener,mBackStatusChangedListener));
        }
        if (ACTION_TYPE_GET_FILE.equals(actionType)) {
            setCurrentAction(SendFileAction.getInstance(mStatusChangedListener));
        }
        if (ACTION_TYPE_RESET.equals(actionType)) {
               setCurrentAction(ResetAction.getInstance(mStatusChangedListener));
        }
        /*if (ACTION_TYPE_MIC_HEADPHONE.equals(actionType)) {
            setCurrentAction(new MicStubHeadsetAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_MIC_SPREAKER.equals(actionType)) {
            setCurrentAction(new MicStubHandsetAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_MIC_HORN.equals(actionType)) {
            setCurrentAction(new MicStubHandfreeAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_PHONE_NETWORK.equals(actionType)) {
            setCurrentAction(new TeleNetworkGetCurrentAction(
                    mStatusChangedListener));
        }
        if (ACTION_TYPE_PHONE_NETWORK_2G.equals(actionType)) {
            setCurrentAction(new TeleNetwork2GAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_PHONE_NETWORK_3G.equals(actionType)) {
            setCurrentAction(new TeleNetwork3GAction(mStatusChangedListener));
        }
        if (ACTION_TYPE_PHONE_NETWORK_4G.equals(actionType)) {
            setCurrentAction(new TeleNetwork4GAction(mStatusChangedListener));
        }*/

        if (ACTION_TYPE_3D.equals(actionType)) {
            setCurrentAction(new NenaMark2Action(mStatusChangedListener,
                    mContext));
        }
        //change to background action test
        if (ACTION_TYPE_RTC_TEST.equals(actionType)) {
            setCurrentAction(RTCTestAction.getInstance(mStatusChangedListener,mBackStatusChangedListener));
        }
        if (ACTION_TYPE_OTG_TEST.equals(actionType)) {
            setCurrentAction(OTGTestAction.getInstance(mStatusChangedListener,mBackStatusChangedListener));
        }
        if (ACTION_TYPE_START_MANUL_TEST.equals(actionType)) {
            mBackgroundTestAction = BackgroundTestAction.getInstance(mStatusChangedListener, mBackStatusChangedListener);
            setCurrentAction(mBackgroundTestAction);
        }
        if (ACTION_TYPE_SHELL_SCRIPT.equals(actionType)) {
            setCurrentAction(new ShellScriptAction(mStatusChangedListener));
        }
        if (mCurrentAction != null) {
            mCurrentActionType = actionType;
            return true;
        }

        throw new IllegalArgumentException("can not match all action type : "
                + actionType);
    }

    private void setCurrentAction(IAction currentAction) {
        this.mCurrentAction = currentAction;
        Log.d(TAG, "mCurrentAction:" + mCurrentAction);
    }

    public IAction getCurrentAction() {
        return mCurrentAction;
    }

    public String getCurrentActionType() {
        return mCurrentActionType;
    }

    public boolean start(String actionType, String param) {
        Log.d(TAG, "actionType:" + actionType);
        if (createAction(actionType)) {
            mCurrentAction.start(param);
            mCurrentStatus = STATUS_DOING;
            return true;
        }
        return false;
    }

    public void stop(String actionType) {
        Log.d(TAG, "Stop current action : " + mCurrentActionType
                + ", param action : " + actionType);
        if (mCurrentActionType.equals(actionType)) {
            releseCurrentActionIfNeed();
            return;
        }
        throw new IllegalArgumentException(
                "Stop action not equal current action!");
    }

    private void releseCurrentActionIfNeed() {
        if (mCurrentAction != null) {
            mCurrentAction.stop();
            mCurrentStatus = STATUS_IDLE;
            mCurrentAction = null;
        }
    }

}
