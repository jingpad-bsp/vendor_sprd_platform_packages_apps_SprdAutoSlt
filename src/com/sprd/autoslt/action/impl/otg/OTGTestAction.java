package com.sprd.autoslt.action.impl.otg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputDevice;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.action.AbstractBackGroundAction;
import com.sprd.autoslt.fingerprint.microarray.ISprdFingerDetectListener;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.StorageUtil;
import com.sprd.autoslt.util.TestColumns;
import com.sprd.autoslt.util.TestItem;
import com.sprd.autoslt.util.TestResultUtil;
import com.sprd.autoslt.R;

public class OTGTestAction extends AbstractBackGroundAction {

    private static final String TAG = "OTGTestAction";
    private static OTGTestAction instance = null;
    private static final String TEST_COLUMS = TestColumns.OTG_TEST;
    private static final int TEST_DESC = R.string.otg_testing;
    public static final String PARM_NAME = "OTG";

    private String usbMassStoragePath = "/storage/usbdisk";
    private static final String SPRD_OTG_TESTFILE = "autoslt_otgtest.txt";
    public byte mOTGTestFlag[] = new byte[1];
    byte[] result = new byte[1];
    byte[] mounted = new byte[1];
    private static final int UPDATE_TIME = 0;

    private OTGTestAction(StatusChangedListener listener,BackStatusChangedListener listener2) {
        super(listener,listener2);
        TestResultUtil.getInstance().reset();
        initTestItem();
    }

    public static OTGTestAction getInstance(StatusChangedListener listener,BackStatusChangedListener listener2) {
        if(instance == null) {
            instance = new OTGTestAction(listener, listener2);
        }
        return instance;
    }

    private void initTestItem(){
        mTestItem.setTestCase(TAG);
        String mTestNote = mContext.getString(TEST_DESC);
        mTestItem.setmTestNote(mTestNote);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, TAG + " start");
        boolean result = getBackgroundTestResult() == AbstractBackGroundAction.TEST_PASS;;
        Log.d(TAG, TAG + " result="+result);
        end(result ? TestResultUtil.TEXT_PASS : TestResultUtil.TEXT_FAIL);
    }

    @Override
    public void stop() {
        stopBackground();
    }

    private boolean mRunning = false;
    @Override
    public void startBackground(String param) {
        resetResult();
        if(mTestItem != null){
            mTestItem.setTestResult(String.valueOf(AbstractBackGroundAction.TEST_DEFAULT));
            mTestItem.setmTestNote("Testing...");
            testNoteChange();
        }
        startVtThread();
    }

    @Override
    public void stopBackground() {
        Log.d(TAG, "stopBackground mRunning="+mRunning);
        mRunning = false;
    }

    private void startVtThread() {
        Thread vtThread = new Thread() {
            public void run() {
                FileInputStream in = null;
                FileOutputStream out = null;
                result[0] = 1;
                mRunning = true;
                while(result[0] != 0){
                    try {
                        if(!mRunning){
                            return;
                        }
                        checkOTGdevices();
                        if (mounted[0] == 0) {
                            File fp = new File(usbMassStoragePath, SPRD_OTG_TESTFILE);
                            if (fp.exists())
                                fp.delete();
                            fp.createNewFile();
                            out = new FileOutputStream(fp);
                            mOTGTestFlag[0] = '7';
                            out.write(mOTGTestFlag, 0, 1);
                            out.close();
                            in = new FileInputStream(fp);
                            int count = in.read(mOTGTestFlag, 0, 1);
                            Log.d(TAG, "vtThread read count="+count);
                            if(count < 0){
                                result[0] = 1;
                                setBackgroundTestResult(AbstractBackGroundAction.TEST_FAIL);
                                return;
                            }
                            in.close();
                            if (mOTGTestFlag[0] == '7') {
                                result[0] = 0;
                                setBackgroundTestResult(AbstractBackGroundAction.TEST_PASS);
                            } else {
                                result[0] = 1;
                                setBackgroundTestResult(AbstractBackGroundAction.TEST_FAIL);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            if (out != null) {
                                out.close();
                                out = null;
                            }
                            if (in != null) {
                                in.close();
                                in = null;
                            }
                        } catch (IOException io) {
                            e.printStackTrace();
                        }
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                                out = null;
                            }
                            if (in != null) {
                                in.close();
                                in = null;
                            }
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    }
                }
                boolean result = getBackgroundTestResult() == AbstractBackGroundAction.TEST_PASS;
                if(mTestItem != null){
                    mTestItem.setTestResult(result ? String.valueOf(AbstractBackGroundAction.TEST_PASS) : String.valueOf(AbstractBackGroundAction.TEST_FAIL));
                    mTestItem.setmTestNote(result ? "Test pass" : "Test fail");
                    testNoteChange();
                }
                //stop test
                stopBackground();
            }
        };
        vtThread.start();
    }

    private void checkOTGdevices() {
        String otgPath = StorageUtil.getExternalStorageAppPath(mContext, StorageUtil.OTG_UDISK_PATH);
        if (otgPath != null) {
            mounted[0] = 0;
            Log.i(TAG, "=== OTG mount succeed ===");
            usbMassStoragePath = otgPath;
        } else {
            mounted[0] = 1;
            Log.i(TAG, "=== OTG mount Fail ===");
        }
    }

    @Override
    public boolean isSupport() {
        boolean isSupport = isSupportOTG(OTG_PATH) || isSupportOTG(OTG_PATH_k414);
        Log.d(TAG, "isSupport  isSupport:"+isSupport);
        return isSupport;
    }

    public static final String OTG_PATH = "/sys/class/dual_role_usb/sprd_dual_role_usb/supported_modes";
    public static final String OTG_PATH_k414 = "/sys/class/typec/port0/port_type";
    public static boolean isSupportOTG(String path){
        BufferedReader bReader = null;
        InputStream inputStream = null;
        try {
            Log.d(TAG, "isSupportOTG  path:"+path);
            inputStream = new FileInputStream(
                    path);
            bReader = new BufferedReader(new InputStreamReader(inputStream));
            String str = bReader.readLine();
            Log.d(TAG, "isSupportOTG  str:"+str);
            if(TextUtils.isEmpty(str)){
                Log.d(TAG, "isSupportOTG  str == NULL");
                return false;
            }
            if (str.contains("[dual] source sink") || str.contains("ufp")) {
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bReader != null) {
                    bReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

