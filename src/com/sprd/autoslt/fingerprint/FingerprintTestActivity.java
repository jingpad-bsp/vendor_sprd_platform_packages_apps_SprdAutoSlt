package com.sprd.autoslt.fingerprint;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.sprd.autoslt.R;

import com.sprd.autoslt.action.impl.FingerPrintAction;
import com.sprd.autoslt.fingerprint.FingerprintTestImpl;
import com.sprd.autoslt.fingerprint.microarray.FactoryTestImpl;
import com.sprd.autoslt.fingerprint.microarray.IFactoryTestImpl;
import com.sprd.autoslt.fingerprint.microarray.ISprdFingerDetectListener;
import com.sprd.autoslt.fingerprint.microarray.MicroarrayFactoryTestImpl;
import com.sprd.autoslt.util.TestResultUtil;

public class FingerprintTestActivity extends Activity {

    private static final String TAG = "FingerprintTestActivity";
    private Context mContext;
    private TextView mFingerDetectTips;
    private TextView mFingerDetectResult;
    private Button mFingerDetect;
    private static final int RESULT_OK = 0;
    private static final int INIT_SUCCESS = 1;
    private static final int CALIBRATION_FAIL = 2;
    private static final int TEST_SUCCESS= 3;
    private static final int TEST_FAIL = 4;
	private static final int Test_ERROR = 5;
    private FingerPrintAction fingerPrintAction = null;
    IFactoryTestImpl theTestImpl = null;
	private Boolean FINGER_DETECTED_RESULT = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finger_print);
        Log.d(TAG, "onCreate");

        mContext = this;

        mFingerDetectTips = (TextView) findViewById(R.id.finger_detect_tips);
        mFingerDetectResult = (TextView) findViewById(R.id.result);
        mFingerDetect = (Button) findViewById(R.id.finger_detect_button);
        mFingerDetect.setVisibility(View.GONE);
        //removeButton();
        //theTestImpl = new FactoryTestImpl(this);
		theTestImpl = new FingerprintTestImpl();
        fingerPrintAction = FingerPrintAction.getInstance(null);
        TestResultUtil.getInstance().reset();
        initSensor();
    }

    private void initSensor() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                /* SPRD bug 753816 : Add all test item to 10c10 */
                try {
                    int ret = theTestImpl.factory_init();
                    Log.d(TAG, "factory_init = " + ret);
                    if (-1 == ret) {
                        theTestImpl = new FingerprintTestImpl();
                        ret = theTestImpl.factory_init();
                        /*SPRD bug 773174:Change init dislpay*/
                        /*mHandler.sendMessage(mHandler
                                .obtainMessage(INIT_SUCCESS));*/
                    }
                    Log.d(TAG, "factory_init ret2= " + ret);
                    //if (-1 == ret) {
                    //    theTestImpl = new MicroarrayFactoryTestImpl(
                    //            FingerprintTestActivity.this);
                    //    ret = theTestImpl.factory_init();
                        /*SPRD bug 773174:Change init dislpay*/
                        /*mHandler.sendMessage(mHandler
                                .obtainMessage(INIT_SUCCESS));*/
                    //}
                    if (0 == ret) {
                        /*SPRD bug 773174:Change init dislpay*/
                        mHandler.sendMessage(mHandler
                                .obtainMessage(INIT_SUCCESS));
                        ret = theTestImpl.spi_test();
                        Log.d(TAG, "spi_test = " + ret);
                        //Test fail while one step fail.
                        if(ret == -1){
                            Log.e(TAG, "spi_test fail!!");
                            mHandler.sendMessage(mHandler
                                    .obtainMessage(CALIBRATION_FAIL));
                            mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                            return;
                        }

                        ret = theTestImpl.interrupt_test();
                        Log.d(TAG, "interrupt_test = " + ret);
                        if(ret == -1){
                            Log.e(TAG, "interrupt_test fail!!");
                            mHandler.sendMessage(mHandler
                                    .obtainMessage(CALIBRATION_FAIL));
                            mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                            return;
                        }

                        ret = theTestImpl.deadpixel_test();
                        Log.d(TAG, "deadpixel_test = " + ret);
                        if(ret == -1){
                            Log.e(TAG, "deadpixel_test fail!!");
                            mHandler.sendMessage(mHandler
                                    .obtainMessage(CALIBRATION_FAIL));
                            mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                            return;
                        }

                        //Change finger_detect step
                        //theTestImpl.finger_detect(listener_fd);
 /*                       mHandler.sendMessage(mHandler
                                .obtainMessage(INIT_SUCCESS));*/
                        theTestImpl.finger_detect(listener_fd);
                        mHandler.sendEmptyMessageDelayed(TEST_FAIL, 5000);
                    } else {
                        mHandler.sendMessage(mHandler
                                .obtainMessage(CALIBRATION_FAIL));
                        mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                    }
                } catch (UnsatisfiedLinkError e) {
                    // TODO: handle exception
                    e.printStackTrace();
/*                    mHandler.sendMessage(mHandler
                            .obtainMessage(INIT_SUCCESS));*/
                    mHandler.sendMessage(mHandler.obtainMessage(Test_ERROR));
                } catch (Exception e) {
					// TODO: handle exception
                    e.printStackTrace();
                    mHandler.sendMessage(mHandler.obtainMessage(Test_ERROR));
				}
                /* @} */
            }
        }).start();
    }


    ISprdFingerDetectListener listener_fd = new ISprdFingerDetectListener(){

        @Override
        public void on_finger_detected(int status) {
            Log.d(TAG,"ISprdFingerDetectListener fd ret = " + status);
            if (status == RESULT_OK) {
                mHandler.removeMessages(TEST_FAIL);
                mHandler.sendMessage(mHandler.obtainMessage(TEST_SUCCESS));
			    FINGER_DETECTED_RESULT = true;
            }else {
            	FINGER_DETECTED_RESULT = false;
			}
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        int ret =0;
        mHandler.removeMessages(TEST_FAIL);
        Log.d(TAG, "onPause, deInit sensor");
        /* SPRD bug 753816 : Add all test item to 10c10 */
        try {
            ret = theTestImpl.factory_exit();
        } catch (UnsatisfiedLinkError e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        Log.d(TAG,"deinit fd ret = " + ret);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_SUCCESS:
                    mFingerDetectTips.setText(mContext.getResources().getString(
                            R.string.finger_print_tips));
                    break;
                case CALIBRATION_FAIL:
                    mFingerDetectTips.setText(mContext.getResources().getString(
                            R.string.finger_print_init_fail));
                    break;
                case TEST_SUCCESS:
                    mFingerDetectResult.setText(mContext.getResources().getString(
                            R.string.have_a_finger));
                    //storeRusult(true);
                    Toast.makeText(FingerprintTestActivity.this, R.string.text_pass,
                        Toast.LENGTH_SHORT).show();
					fingerPrintAction.fingerPrintHandler.sendEmptyMessage(FingerPrintAction.FINGERPRINT_TEST_SUCCESS);
				    TestResultUtil.getInstance().setCurrentStepName(mContext.getResources().getString(
	                        R.string.have_a_finger));
		            TestResultUtil.getInstance().setCurrentResult(TestResultUtil.TEXT_PASS);
                    finish();
                    break;
                case TEST_FAIL:
                    mFingerDetectResult.setText(mContext.getResources().getString(
                            R.string.no_finger));
                    //storeRusult(false);
                    Toast.makeText(FingerprintTestActivity.this, R.string.no_finger,
                            Toast.LENGTH_SHORT).show();
					fingerPrintAction.fingerPrintHandler.sendEmptyMessage(FingerPrintAction.FINGERPRINT_TEST_FAIL);
				    TestResultUtil.getInstance().setCurrentStepName(mContext.getResources().getString(
	                        R.string.no_finger));
		            TestResultUtil.getInstance().setCurrentResult(TestResultUtil.TEXT_FAIL);
                    finish();
                    break;
				case Test_ERROR:
                	fingerPrintAction.fingerPrintHandler.sendEmptyMessage(FingerPrintAction.FINGERPRINT_TEST_ERROR);
                    finish();
					break;
                default:
            }
        }
    };
}
