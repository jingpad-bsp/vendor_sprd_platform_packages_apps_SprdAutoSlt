package com.sprd.autoslt;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;
import android.graphics.Bitmap;

import com.sprd.autoslt.action.AbstractBackGroundAction;
import com.sprd.autoslt.action.AbstractBackGroundAction.BackStatusChangedListener;
import com.sprd.autoslt.activity.GetResultActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.ListAdapter;
import com.sprd.autoslt.util.LoadView;
import com.sprd.autoslt.util.PhaseCheckParse;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.ShellUtils;
import com.sprd.autoslt.util.StationListAdapter;
import com.sprd.autoslt.util.TestItem;
import com.sprd.autoslt.util.WifiAutoConnectManager;
import com.sprd.autoslt.util.WifiAutoConnectManager.WifiCipherType;
import com.sprd.autoslt.util.ZXingUtils;
import com.sprd.autoslt.camera.Util;
import com.sprd.autoslt.fingerprint.FingerprintTestService;
import com.sprd.autoslt.util.NavigationUtil;

import android.os.SystemProperties;
import android.os.Environment;
import android.os.EnvironmentEx;

public class SLTActivity extends SltBaseActivity implements OnClickListener {
    private static final String TAG = "SLTActivity";
    private Button mAutosltQuit, mGetResult, mClearLog, mFactoryReset;
    private TextView mSocketLog, mNoteTextView;
    // private CheckBox mCheckBox;
    private SLTService mSLTService;
    private ListView mListView;
    // private ListAdapter mListAdapter;
    private StationListAdapter mStationListAdapter;
    private ScrollView mLogScrollView, mNoteScrollView;
    private ListView mBackgroundActionView;
    private LoadView mView;
    private PowerManager mPowerManager;
    private SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private TextView mQRButton, mLogButton, mSNnum, mWifistateTextView;
    private ImageView mQRImageView;
    private TextView mStationResult;
    private Bitmap mBitmap;
    private EngSqlite mEngSqlite;
    private String result;
    private int resultColor;
    private ContentResolver mContentResolver;
    ArrayList<TestItem> mNoteList = new ArrayList<TestItem>();
    private WifiManager wifiManager;
    private WifiStateReceiver receiver;
    private String wifi_IP_address = null;
    private WifiInfo wifiInfo;
    private WifiAutoConnectManager mWifiAutoConnectManager;
    ConnectAsyncTask mConnectAsyncTask = null;
    boolean isLinked = false;
    private PhaseCheckParse mCheckParse = null;
    private int BtnClickNumber = 3;
    private long DURATION = 1000;
    long[] mHits = new long[BtnClickNumber];
    public static String SN_number = null;
    public String SDState = null;
    public String[] simState = new String[2];

    private boolean mSavedSoundEffect = false;
    private boolean mSavedLockSound = false;
    private static final int SCREEN_TIMEOUT = 60 * 1000;//1 min
    private long mLastScreenTimeout = 5000l;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SLTConstant.UPDATE_LOG:
                mSocketLog.append(format.format(new Date()) + ":"
                        + msg.obj.toString() + "\n");
                mHandler.post(new Runnable() {
                    public void run() {
                        mLogScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;
            case SLTConstant.UPDATE_RESULT:
                /*
                 * String[] keyValue = msg.obj.toString().split("\\^");
                 * if(keyValue[1].equalsIgnoreCase("pass")) {
                 * mListAdapter.addItem(keyValue[0], "PASS"); } else
                 * if(keyValue[1].equalsIgnoreCase("fail")) {
                 * mListAdapter.addItem(keyValue[0], "FAIL"); } else {
                 * mListAdapter.addItem(keyValue[0], "ERROR"); }
                 */
                break;
            case SLTConstant.CLEAR_LOG:
                if (mSocketLog != null) {
                    mSocketLog.setText("");
                }
                break;
            case SLTConstant.CLEAR_RESULT:
                if (mStationListAdapter != null) {
                    mStationListAdapter.clearData();
                }
                break;
            case SLTConstant.UPDATE_RECORD_RESULT:
                /*
                 * removeView(); addView();
                 */
                break;
            case SLTConstant.UPDATA_RECORD_HISTOTY_INFO:
                Log.d("pei.li",
                        "mEngSqlite.getVaule() 2 =" + mEngSqlite.getVaule());
                if (mEngSqlite.getMap().size() == 0
                        || mEngSqlite.getMap().isEmpty()
                        || mEngSqlite.getVaule() == 3) {
                    mStationResult.setBackgroundColor(Color.GRAY);
                    mStationResult.setText("NT");
                } else if (mEngSqlite.getVaule() == 1) {
                    mStationResult.setBackgroundColor(Color.GREEN);
                    mStationResult.setText("PASS");
                    /*if (mCheckParse.isStationExsit("MMI")) {
                        mCheckParse.writeStationTested("MMI");
                        mCheckParse.writeStationPass("MMI");
                    }*/
                } else if (mEngSqlite.getVaule() == 2) {
                    mStationResult.setBackgroundColor(Color.RED);
                    mStationResult.setText("FAIL");
                    /*if (mCheckParse.isStationExsit("MMI")) {
                        mCheckParse.writeStationTested("MMI");
                        mCheckParse.writeStationFail("MMI");
                    }*/
                } else if (mEngSqlite.getVaule() == 4) {
                    mStationResult.setBackgroundColor(Color.YELLOW);
                    mStationResult.setText("GO");
                }
                if (mEngSqlite.getVaule() == 1) {
                    // mEngSqlite.updateBootFlag("0");
                    Util.setSharedPreference(SLTApplication.getApplication()
                            .getApplicationContext(), "SLT_bootflag", 0);
                } else {
                    // mEngSqlite.updateBootFlag("1");
                    Util.setSharedPreference(SLTApplication.getApplication()
                            .getApplicationContext(), "SLT_bootflag", 1);
                }
                String[] historykeyValue = msg.obj.toString().split("\\^");
                Log.d("pei.li", "historykeyValue[0] =" + historykeyValue[0]);
                Log.d("pei.li", "historykeyValue[2] =" + historykeyValue[2]);
                Log.d("pei.li", "mStationListAdapter.getCount() ="
                        + mStationListAdapter.getCount());
                for (int i = 0; i < mStationListAdapter.getCount(); i++) {
                    if (mStationListAdapter.getItemName(i).equalsIgnoreCase(
                            historykeyValue[0])) {
                        Log.d("pei.li", "mListAdapter.getItemName(" + i + ")"
                                + mStationListAdapter.getItemName(i));
                        if (historykeyValue[2].equalsIgnoreCase("pass")) {
                            mStationListAdapter.setItemResult(i, "PASS");
                        } else if (historykeyValue[2].equalsIgnoreCase("fail")) {
                            mStationListAdapter.setItemResult(i, "FAIL");
                        } else if (historykeyValue[2].equalsIgnoreCase("go")) {
                            mStationListAdapter.setItemResult(i, "GO");
                        } else if (historykeyValue[2].equalsIgnoreCase("nt")) {
                            mStationListAdapter.setItemResult(i, "NT");
                        } else {
                            mStationListAdapter.addItem(historykeyValue[0],
                                    "ERROR");
                        }
                    }
                }
                break;
            case SLTConstant.UPDATE_FAIL_NOTE_INFO:
                Log.d("pei.li", " updata fail note info");
                mNoteList = mEngSqlite.queryData();
                String noteMsg = null;
                StringBuffer sBuffer = new StringBuffer();
                if (mNoteList.size() != 0 || !mNoteList.isEmpty()) {
                    for (int i = 0; i < mNoteList.size(); i++) {
                        noteMsg = mNoteList.get(i).getmTestNote();
                        if (mNoteList.get(i).getTestResult()
                                .equalsIgnoreCase("fail")
                                && noteMsg != null && !noteMsg.equals("")) {
                            sBuffer.append(noteMsg + ";  ");
                            Log.d(TAG,
                                    "sBuffer.toString() = "
                                            + sBuffer.toString());
                        }
                    }
                    mNoteTextView.setText(sBuffer.toString());
                } else {
                    mNoteTextView.setText(noteMsg);
                }
                break;
            case SLTConstant.UPDATE_IMAGE_INFO:
                Log.d(TAG, " updata image info :wifi_IP_address = "
                        + wifi_IP_address);
                // Log.d(TAG, "SN : "+ SN_number);
                mQRImageView.setVisibility(View.VISIBLE);
                mSNnum.setVisibility(View.VISIBLE);
                if (wifi_IP_address != null
                        && !wifi_IP_address.equals("0.0.0.0")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (wifi_IP_address != null
                                    && !wifi_IP_address.equals("0.0.0.0")) {
                                mBitmap = ZXingUtils
                                        .createQRImage(
                                                /* SN_number + ","+ */wifi_IP_address
                                                        + ","
                                                        + SDState
                                                        + ","
                                                        + simState[0]
                                                        + ","
                                                        + simState[1],
                                                getApplicationContext()
                                                        .getResources()
                                                        .getDimensionPixelSize(
                                                                R.dimen.activity_image),
                                                getApplicationContext()
                                                        .getResources()
                                                        .getDimensionPixelSize(
                                                                R.dimen.activity_image));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mQRImageView.setImageBitmap(mBitmap);
                                        mSNnum.setText(SN_number + ","
                                                + wifi_IP_address);
                                    }
                                });
                            }
                        }
                    }).start();
                }
                break;
            case SLTConstant.UPDATE_WIFI_TEXTVIEW:
                Log.d(TAG, " updata wifi textview info");
                mWifistateTextView.setText(msg.obj.toString());
                break;
            case SLTConstant.CLEAR_IMAGE_INFO:
                mQRImageView.setVisibility(View.INVISIBLE);
                mSNnum.setVisibility(View.INVISIBLE);
                break;
            }
            super.handleMessage(msg);
        }
    };

    private long mPreTime = 0;
    private static final int EXIT_TIME = 2000;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mPreTime > EXIT_TIME) {
            Toast.makeText(this, getString(R.string.exit_app_text), Toast.LENGTH_SHORT).show();
            mPreTime = System.currentTimeMillis();
            return;
        } else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slt);
        //DISMISS_KEYGUARD and KEEP_SCREEN_ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        observeWifiSwitch();
        /*
         * new
         * ShellUtil().execShellStr("rm -rf /data/misc/wifi/wpa_supplicant.conf"
         * );
         */
        mWifiAutoConnectManager = WifiAutoConnectManager
                .newInstance(wifiManager);
        mContentResolver = SLTApplication.getApplication()
                .getApplicationContext().getContentResolver();

        // Log.d(TAG, "service.adb.tcp.port1 ="+
        // SystemProperties.getInt("service.adb.tcp.port", 0));
        // ShellUtils.runCommand("stop adbd");
        // if (SystemProperties.getInt("service.adb.tcp.port", 0) != 5555) {
        // SystemProperties.set("service.adb.tcp.port","5555");
        //
        // ShellUtils.runCommand("start adbd");
        // Log.d(TAG, "service.adb.tcp.port2 ="+
        // SystemProperties.getInt("service.adb.tcp.port", 0));

        if (mConnectAsyncTask != null) {
            mConnectAsyncTask.cancel(true);
            mConnectAsyncTask = null;
        }
        // mConnectAsyncTask = new
        // ConnectAsyncTask("AP_SZ_SZC_6F248","szc02468",WifiCipherType.WIFICIPHER_WPA);
        mCheckParse = new PhaseCheckParse();
        mConnectAsyncTask = new ConnectAsyncTask("autoslt", "autoslt1234",
                WifiCipherType.WIFICIPHER_WPA);
        mConnectAsyncTask.execute();
        mWifistateTextView = (TextView) findViewById(R.id.wifi_state_textview);
        if (WifiAutoConnectManager.wifiManager.isWifiEnabled()) {
            mHandler.sendMessage(mHandler.obtainMessage(
                    SLTConstant.UPDATE_WIFI_TEXTVIEW, "WiFi:已打开"));
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(
                    SLTConstant.UPDATE_WIFI_TEXTVIEW, "WiFi:未打开"));
        }

        mAutosltQuit = (Button) findViewById(R.id.btn_autoslt_quit);
        mAutosltQuit.setOnClickListener(this);
        mGetResult = (Button) findViewById(R.id.btn_getresult);
        mGetResult.setOnClickListener(this);
        /*
         * mClearLog = (Button) findViewById(R.id.btn_clear_log);
         * mClearLog.setOnClickListener(this);
         */
        mSocketLog = (TextView) findViewById(R.id.socket_Log);
        mNoteScrollView = (ScrollView) findViewById(R.id.note_scrollview);
        mNoteTextView = (TextView) findViewById(R.id.getresult_note);
        mNoteTextView.setClickable(false);
        mNoteTextView.setLongClickable(false);

        mNoteScrollView.setVisibility(View.GONE);
        mNoteTextView.setVisibility(View.GONE);

        TextView mVersion = (TextView) findViewById(R.id.app_version);
        String versionName = SLTUtil.getVersionName(getApplicationContext());
        Log.d(TAG, "App versionName=" + versionName);
        if (!TextUtils.isEmpty(versionName)) {
            mVersion.setText(versionName);
        }
        /*
         * mQRButton = (TextView) findViewById(R.id.textview_qrcode); mLogButton
         * = (TextView) findViewById(R.id.textview_log);
         * mQRButton.setOnClickListener(this);
         * mLogButton.setOnClickListener(this);
         */
        /*
         * Factory reset
         */
        // mFactoryReset = (Button) findViewById(R.id.btn_factoryreset);
        // mFactoryReset.setOnClickListener(this);
        /*
         * Factory reset
         */
        mSNnum = (TextView) findViewById(R.id.textview_sn_num);
        // mSNnum.setText(PhaseCheckParse.getSerialNumber());
        mQRImageView = (ImageView) findViewById(R.id.qrcode_image);
        Intent intent = new Intent(SLTActivity.this, SLTService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.d("huasong1", "onCreate");
        mEngSqlite = EngSqlite.getInstance(SLTApplication.getApplication()
                .getApplicationContext());
        /*
         * String antenna_result =
         * PhaseCheckParse.getInstance().getPhaseCheckResult("ANT""ANTENNA");
         * String re = "NT"; // Log.d("huasong1", "antenna_result = "
         * +antenna_result); if (antenna_result != null &&
         * !antenna_result.equals("") && antenna_result.equals("PASS") ) { re =
         * "PASS"; }else if (antenna_result != null &&
         * !antenna_result.equals("") && antenna_result.equals("FAIL")) { re =
         * "FAIL"; } mEngSqlite.updateStationData("Antenna", re);
         */
        mListView = (ListView) findViewById(R.id.station_listview_layout);
        mStationListAdapter = new StationListAdapter(this,
                mEngSqlite.queryStationData());
        mListView.setAdapter(mStationListAdapter);
        mLogScrollView = (ScrollView) findViewById(R.id.log_scrollview);
        mStationResult = (TextView) findViewById(R.id.station_result_image);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!mPowerManager.isScreenOn()) {
            sendPowerKey();
        }

        /*
         * new Thread(new Runnable() {
         * 
         * @Override public void run() { mBitmap = ZXingUtils.createQRImage( "<"
         * +PhaseCheckParse.getSerialNumber()+"," +wifi_IP_address+">"
         * PhaseCheckParse. getInstance().getSn() , 500, 500); runOnUiThread(new
         * Runnable() {
         * 
         * @Override public void run() { mQRImageView.setImageBitmap(mBitmap); }
         * }); } }).start();
         */
        // mHandler.sendEmptyMessage(SLTConstant.UPDATE_IMAGE_INFO);
        mEngSqlite.setStationList(mEngSqlite.queryStationData());
        Log.d("pei.li", "mEngSqlite.getResult() = " + mEngSqlite.getResult());
        if (mEngSqlite.queryStationData().size() == 0
                || mEngSqlite.queryStationData().isEmpty()
                || mEngSqlite.getResult() == 3) {
            mStationResult.setBackgroundColor(Color.GRAY);
            mStationResult.setText("NT");
        } else if (mEngSqlite.getResult() == 1) {
            mStationResult.setBackgroundColor(Color.GREEN);
            mStationResult.setText("PASS");
            /*if (mCheckParse.isStationExsit("MMI")) {
                mCheckParse.writeStationTested("MMI");
                mCheckParse.writeStationPass("MMI");
            }*/
        } else if (mEngSqlite.getResult() == 2) {
            mStationResult.setBackgroundColor(Color.RED);
            mStationResult.setText("FAIL");
            /*if (mCheckParse.isStationExsit("MMI")) {
                mCheckParse.writeStationTested("MMI");
                mCheckParse.writeStationFail("MMI");
            }*/
        } else if (mEngSqlite.getResult() == 4) {
            mStationResult.setBackgroundColor(Color.YELLOW);
            mStationResult.setText("GO");
        }
        if (mEngSqlite.getResult() == 1) {
            // mEngSqlite.updateBootFlag("0");
            Util.setSharedPreference(SLTApplication.getApplication()
                    .getApplicationContext(), "SLT_bootflag", 0);
        } else {
            // mEngSqlite.updateBootFlag("1");
            Util.setSharedPreference(SLTApplication.getApplication()
                    .getApplicationContext(), "SLT_bootflag", 1);
        }
        /*
         * if (Settings.Global.getInt(this.getContentResolver(),
         * Settings.Global.ADB_ENABLED, 0) == 0) {
         * Settings.Global.putInt(this.getContentResolver(),
         * Settings.Global.ADB_ENABLED, 1); }
         */

        /*
         * mCheckBox = (CheckBox)findViewById(R.id.bt_checkbox);
         * if(mEngSqlite.queryBootFlag()!= null&&
         * !mEngSqlite.queryBootFlag().equals("")
         * &&mEngSqlite.queryBootFlag().equals("1")){
         * mCheckBox.setChecked(true); }else { mCheckBox.setChecked(false); }
         * mCheckBox.setOnCheckedChangeListener(new
         * CompoundButton.OnCheckedChangeListener() {
         * 
         * @Override public void onCheckedChanged(CompoundButton buttonView,
         * boolean isChecked) { // TODO Auto-generated method stub
         * Log.d("pei.li", "mCheckBox.isChecked() = " + mCheckBox.isChecked());
         * if (mCheckBox.isChecked()) { mEngSqlite.updateBootFlag("1"); }else {
         * mEngSqlite.updateBootFlag("0"); }
         * } });
         */
        SN_number = mCheckParse.getSn();
        Log.d(TAG, "SN : " + SN_number + "; length = " + SN_number.length());
        mHandler.sendEmptyMessage(SLTConstant.UPDATE_FAIL_NOTE_INFO);
        mHandler.post(new Runnable() {
            public void run() {
                mNoteScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        mSavedSoundEffect = SLTUtil.setSoundEffect(this, false);
        mSavedLockSound = SLTUtil.setLockSound(this, false);
        mLastScreenTimeout = SLTUtil.setSrceenTimeout(this, SCREEN_TIMEOUT);
        SLTUtil.setAudioMode(this, AudioManager.RINGER_MODE_SILENT);
    }

    private class ItemListViewAdapter extends BaseAdapter {
        private ArrayList<TestItem> mItemList;
        private LayoutInflater mInflater;

        public ItemListViewAdapter(Context c, ArrayList<TestItem> mItemsLists) {
            mItemList = mItemsLists;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void updateArrayList(ArrayList<TestItem> items) {
            if (mItemList != null) {
                mItemList.clear();
                mItemList = null;
            }
            mItemList = items;
        }

        @Override
        public int getCount() {
            if (mItemList != null) {
                return mItemList.size();
            }
            return 0;
        }

        @Override
        public TestItem getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            TestItem item = getItem(position);
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item, parent, false);
            } else {
                view = convertView;
            }
            if (item != null) {
                String testresult = item.getTestResult();
                if (testresult != null) {
                    if (testresult.equals(String
                            .valueOf(AbstractBackGroundAction.TEST_PASS))) {
                        view.setBackgroundColor(Color.GREEN);
                    } else if (testresult.equals(String
                            .valueOf(AbstractBackGroundAction.TEST_FAIL))) {
                        view.setBackgroundColor(Color.RED);
                    } else {
                        view.setBackgroundColor(Color.YELLOW);
                    }
                } else {
                    view.setBackgroundColor(Color.YELLOW);
                }
                TextView textView = (TextView) view
                        .findViewById(R.id.text_item);
                textView.setText(item.getTestCase());
                TextView textViewNote = (TextView) view
                        .findViewById(R.id.text_value);
                textViewNote.setText(item.getmTestNote());
            }
            return view;
        }
    }

    private ItemListViewAdapter mItemListViewAdapter = null;

    private void startBackgroundAction() {
        if (mSLTService != null) {
            mSLTService
                    .setBackStatusChangedListener(mBackStatusChangedListener);
            initBackgroundListview();
        }
    }

    private ArrayList<TestItem> mItemsLists = new ArrayList<TestItem>();

    private void initBackgroundListview() {
        mBackgroundActionView = (ListView) findViewById(R.id.backgrpund_action_list);
        mItemListViewAdapter = new ItemListViewAdapter(this, mItemsLists);
        mBackgroundActionView.setAdapter(mItemListViewAdapter);
    }

    private ArrayList<TestItem> getBackgroundActionList(
            ArrayList<AbstractBackGroundAction> mAllActions) {
        if (mSLTService == null)
            return null;
        ArrayList<TestItem> mItemsLists = new ArrayList<TestItem>();
        // ArrayList<AbstractBackGroundAction> mAllActions =
        // mSLTService.getAllBackGroundAction();
        for (AbstractBackGroundAction backGroundAction : mAllActions) {
            TestItem item = backGroundAction.getTestItem();
            if (item != null) {
                Log.d(TAG, "getBackgroundActionList item=" + item.getTestCase());
                mItemsLists.add(item);
            }
        }
        return mItemsLists;
    }

    private void notifyItemListDataSetChanged() {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mItemListViewAdapter != null) {
                        mItemListViewAdapter.notifyDataSetChanged();
                    }
                    if (mBackgroundActionView != null) {
                        mBackgroundActionView.requestLayout();
                    }
                }
            });
        }
    }

    private ArrayList<AbstractBackGroundAction> mBackGroundActions = new ArrayList<AbstractBackGroundAction>();
    private BackStatusChangedListener mBackStatusChangedListener = new BackStatusChangedListener() {
        @Override
        public void onStatusTestNoteChange() {
            notifyItemListDataSetChanged();
        }

        @Override
        public void onStatusBackgroundStop() {
            notifyItemListDataSetChanged();
        }

        @Override
        public void onStatusBackgroundStart() {
            notifyItemListDataSetChanged();
        }

        public void onBackgroundAtionsChange(
                java.util.ArrayList<AbstractBackGroundAction> actions) {
            Log.d(TAG, "onBackgroundAtionsChange");
            if (actions != null) {
                Log.d(TAG,
                        "onBackgroundAtionsChange actions size="
                                + actions.size());
            }else{
                return;
            }
            mBackGroundActions.addAll(actions);
            if (mItemsLists != null) {
                mItemsLists.clear();
            }
            mItemsLists = getBackgroundActionList(actions);
            if (mItemListViewAdapter != null) {
                mItemListViewAdapter.updateArrayList(mItemsLists);
            }
            notifyItemListDataSetChanged();
        };
    };

    private void sendPowerKey() {
        KeyEvent ev = new KeyEvent(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, 26, 0, 0, -1,
                0, KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                InputDevice.SOURCE_KEYBOARD);
        injectKeyEvent(ev);
        ev = new KeyEvent(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, 26, 0, 0, -1,
                0, KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                InputDevice.SOURCE_KEYBOARD);
        injectKeyEvent(ev);
    }

    private void injectKeyEvent(KeyEvent event) {
        InputManager.getInstance().injectInputEvent(event,
                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSLTService = ((SLTService.ServiceBinder) service).getService();
            mSLTService.setMainHandler(mHandler);
            startBackgroundAction();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            if (mSLTService != null) {
                mSLTService
                        .setBackStatusChangedListener(null);
            }
            mSLTService = null;
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.slt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_autoslt_quit:
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                Log.d(TAG, "mHits[0]=:" + mHits[0] + ", SystemClock.uptimeMillis()=:" + SystemClock.uptimeMillis());
                mHits = new long[BtnClickNumber];
                //startValidationToolsActivity();
                finish();
            }
            break;
        case R.id.btn_getresult:
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                mHits = new long[BtnClickNumber];
                startActivity(new Intent(SLTActivity.this,
                        GetResultActivity.class));
            }
            // startActivity(new Intent(SLTActivity.this,
            // GetResultActivity.class));
            break;
        /*
         * case R.id.btn_factoryreset:{ // mEngSqlite.updateBootFlag("0");
         * AlertDialog dialog = new AlertDialog.Builder(this)
         * .setTitle(R.string.reset) .setMessage(R.string.factory_reset_message)
         * .setNegativeButton(android.R.string.ok, new
         * DialogInterface.OnClickListener() {
         * 
         * @Override public void onClick(DialogInterface dialog,int which) {
         * Intent intent = new Intent( Intent.ACTION_FACTORY_RESET);
         * intent.setPackage("android");
         * intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
         * intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
         * intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE,false);
         * SLTActivity.this.sendBroadcast(intent); }
         * }).setPositiveButton(android.R.string.cancel, null).show(); } break;
         */
        /*
         * try { startActivity(new Intent(Settings.ACTION_PRIVACY_SETTINGS)); }
         * catch (Exception e) { // TODO Auto-generated catch block
         * e.printStackTrace(); } break;
         */
        /*
         * case R.id.btn_clear_log: SLTLogManager.clearLog();
         * SLTLogManager.clearResult(); break;
         */
        /*
         * case R.id.textview_qrcode: ((ScrollView)
         * findViewById(R.id.code_scrollview)).setVisibility(View.VISIBLE);
         * ((ScrollView)
         * findViewById(R.id.log_scrollview)).setVisibility(View.GONE); break;
         * case R.id.textview_log: ((ScrollView)
         * findViewById(R.id.code_scrollview)).setVisibility(View.GONE);
         * ((ScrollView)
         * findViewById(R.id.log_scrollview)).setVisibility(View.VISIBLE);
         * break;
         */
        }
    }

    @Override
    protected void onDestroy() {
        SLTUtil.setSoundEffect(this, mSavedSoundEffect);
        SLTUtil.setLockSound(this, mSavedLockSound);
        SLTUtil.setSrceenTimeout(this, (int )mLastScreenTimeout);
        SLTUtil.setAudioMode(this, AudioManager.RINGER_MODE_NORMAL);
        unbindService(conn);
        if (receiver != null) {
            SLTApplication.getApplication().getApplicationContext()
                    .unregisterReceiver(receiver);
            receiver = null;
        }
        SLTLogManager.clearLog();
        SLTLogManager.clearResult();
        //Reset all background result.
        if(mBackGroundActions != null){
            for(AbstractBackGroundAction action : mBackGroundActions){
                action.resetResult();
            }
        }
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // removeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // addView();
        if (NavigationUtil.getNavigationBarShowState(this)) {
            Log.d(TAG, "SLTActivity, hideNavigationBar...");
            NavigationUtil.hideNavigationBar(this);
        }
    }

    /*
     * private void addView() { mView = new LoadView(this);
     * WindowManager.LayoutParams params = new WindowManager.LayoutParams(
     * WindowManager.LayoutParams.MATCH_PARENT,
     * WindowManager.LayoutParams.WRAP_CONTENT,
     * WindowManager.LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY,
     * WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
     * WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
     * params.gravity = Gravity.END | Gravity.TOP;
     * params.setTitle("Check Calibration");
     * ((WindowManager)getSystemService(WINDOW_SERVICE)).addView(mView, params);
     * }
     * 
     * private void removeView() {
     * ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mView);
     * mView = null; }
     */

    private void startValidationToolsActivity() {
        try {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.sprd.validationtools",
                    "com.sprd.validationtools.ValidationToolsMainActivity");
            intent.setComponent(comp);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "current not support", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void observeWifiSwitch() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (receiver == null) {
            receiver = new WifiStateReceiver();
        }
        SLTApplication.getApplication().getApplicationContext()
                .registerReceiver(receiver, filter);
    }

    class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION
                    .equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, 0);
                if (wifiState == WifiManager.WIFI_STATE_ENABLED
                        || wifiState == WifiManager.WIFI_STATE_ENABLING) {
                    Log.i(TAG, "wifi connected");
                    wifi_IP_address = null;
                    // mHandler.sendEmptyMessage(SLTConstant.UPDATE_IMAGE_INFO);
                    mHandler.sendMessage(mHandler.obtainMessage(
                            SLTConstant.UPDATE_WIFI_TEXTVIEW, "WiFi:已开启"));

                } else if (wifiState == WifiManager.WIFI_STATE_DISABLED
                        || wifiState == WifiManager.WIFI_STATE_DISABLING) {
                    Log.i(TAG, "wifi disconnected");
                    wifi_IP_address = null;
                    // mHandler.sendEmptyMessage(SLTConstant.UPDATE_IMAGE_INFO);
                    mHandler.sendEmptyMessage(SLTConstant.CLEAR_IMAGE_INFO);
                    mHandler.sendMessage(mHandler.obtainMessage(
                            SLTConstant.UPDATE_WIFI_TEXTVIEW, "WiFi:已关闭"));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent
                    .getAction())) {
                NetworkInfo info = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "--NetworkInfo--" + info.toString());
                if (NetworkInfo.State.DISCONNECTED == info.getState()) {
                    Log.d(TAG, "wifi没连接上");
                    isLinked = false;
                    wifi_IP_address = null;
                    // mHandler.sendEmptyMessage(SLTConstant.UPDATE_IMAGE_INFO);
                    mHandler.sendEmptyMessage(SLTConstant.CLEAR_IMAGE_INFO);
                    mHandler.sendMessage(mHandler.obtainMessage(
                            SLTConstant.UPDATE_WIFI_TEXTVIEW, "WiFi:没连接上"));
                } else if (NetworkInfo.State.CONNECTED == info.getState()) {
                    Log.d(TAG, "wifi连接上了");
                    isLinked = true;
                    wifi_IP_address = WifiAutoConnectManager.getIpAddress();
                    Log.i(TAG, "wifi IP :" + wifi_IP_address);
                    if (wifi_IP_address != null
                            && !wifi_IP_address.equals("0.0.0.0")) {
                        SDState = checkSDCard() ? "1" : "0";
                        simState[0] = getSimResult(0) ? "1" : "0";
                        simState[1] = getSimResult(1) ? "1" : "0";
                        Log.d(TAG, "checkSDCard() = " + checkSDCard()
                                + "; sim0= " + simState[0] + "; sim1= "
                                + simState[1]);

                        mHandler.sendEmptyMessage(SLTConstant.UPDATE_IMAGE_INFO);
                        mHandler.sendMessage(mHandler.obtainMessage(
                                SLTConstant.UPDATE_WIFI_TEXTVIEW, "WiFi:已连接"
                                        + WifiAutoConnectManager.getSSID()));
                    }

                } else if (NetworkInfo.State.CONNECTING == info.getState()) {
                    Log.d(TAG, "wifi正在连接");
                    isLinked = false;
                    wifi_IP_address = null;
                    // mHandler.sendEmptyMessage(SLTConstant.UPDATE_IMAGE_INFO);
                    mHandler.sendMessage(mHandler.obtainMessage(
                            SLTConstant.UPDATE_WIFI_TEXTVIEW, "WiFi:正在连接"));
                }

            }
        }

        private boolean checkSDCard() {
            // TODO Auto-generated method stub
            if (EnvironmentEx.getExternalStoragePathState().equals(
                    Environment.MEDIA_MOUNTED)) {
                return true;
            } else {
                return false;
            }
        }
    }

    class ConnectAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private String ssid;
        private String password;
        private WifiAutoConnectManager.WifiCipherType type;
        WifiConfiguration tempConfig;

        public ConnectAsyncTask(String ssid, String password,
                WifiAutoConnectManager.WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            mWifiAutoConnectManager.openWifi();
            while (mWifiAutoConnectManager.wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Log.e(TAG, ie.toString());
                }
            }

            Log.d(TAG, "ssid :" + ssid + ";  password:" + password
                    + "; type : " + type);
            tempConfig = mWifiAutoConnectManager.isExsits(ssid, password);
            Log.d(TAG, "tempConfig = " + tempConfig);
            for (WifiConfiguration c : mWifiAutoConnectManager.wifiManager
                    .getConfiguredNetworks()) {
                mWifiAutoConnectManager.wifiManager.disableNetwork(c.networkId);
            }
            if (tempConfig != null) {
                Log.d(TAG, ssid + "configed!");
                boolean result = mWifiAutoConnectManager.wifiManager
                        .enableNetwork(tempConfig.networkId, true);
                boolean connected = mWifiAutoConnectManager.wifiManager
                        .reconnect();
                if (!isLinked
                        && type != WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA) {
                    Log.d(TAG, "================");
                    try {
                        Thread.sleep(5000);
                        if (!isLinked) {
                            Log.d(TAG, ssid + "isLinked=" + isLinked);
                            mWifiAutoConnectManager.wifiManager
                                    .disableNetwork(tempConfig.networkId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "result=" + result + "connected = " + connected);
                return result;
            } else {
                Log.d(TAG, ssid + "not configed!");
                if (type != WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            WifiConfiguration wifiConfig = mWifiAutoConnectManager
                                    .createWifiInfo(ssid, password, type);
                            if (wifiConfig == null) {
                                Log.d(TAG, "wifiConfig is null!");
                                return;
                            }
                            Log.d(TAG, wifiConfig.SSID);

                            int netID = mWifiAutoConnectManager.wifiManager
                                    .addNetwork(wifiConfig);
                            boolean enabled = mWifiAutoConnectManager.wifiManager
                                    .enableNetwork(netID, true);
                            // mWifiAutoConnectManager.wifiManager.connect(wifiConfig,
                            // null);
                            Log.d(TAG, "enableNetwork status enable=" + enabled);
                        }
                    }).start();

                } else {
                    WifiConfiguration wifiConfig = mWifiAutoConnectManager
                            .createWifiInfo(ssid, password, type);
                    if (wifiConfig == null) {
                        Log.d("wifidemo", "wifiConfig is null!");
                        return false;
                    }
                    Log.d(TAG, wifiConfig.SSID);
                    int netID = mWifiAutoConnectManager.wifiManager
                            .addNetwork(wifiConfig);
                    boolean enabled = mWifiAutoConnectManager.wifiManager
                            .enableNetwork(netID, true);
                    // mWifiAutoConnectManager.wifiManager.connect(wifiConfig,
                    // null);
                    Log.d(TAG, "enableNetwork status enable=" + enabled);
                    return enabled;
                }
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mConnectAsyncTask = null;
        }
    }

    private boolean getSimResult(int simId) {
        TelephonyManager mTelephonyManager = (TelephonyManager) this
                .getSystemService(SLTActivity.this.TELEPHONY_SERVICE + simId);
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager) this
                    .getSystemService(SLTActivity.this.TELEPHONY_SERVICE);
            if (mTelephonyManager == null) {
                return false;
            }
        }
        if (mTelephonyManager.getSimState(simId) == TelephonyManager.SIM_STATE_ABSENT
                || mTelephonyManager.getSimState(simId) == TelephonyManager.SIM_STATE_UNKNOWN) {
            return false;
        }
        return true;
    }
}