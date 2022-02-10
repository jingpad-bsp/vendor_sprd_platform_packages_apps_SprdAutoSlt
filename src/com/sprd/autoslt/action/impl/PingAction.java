package com.sprd.autoslt.action.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;

public class PingAction extends AbstractAction {
    private static final String TAG = "PingAction";
    private static PingAction instance;
    private static String mType;
    private String mResault = "";
    private String mParam;

    public PingAction(StatusChangedListener listener) {
        super(listener);
    }

    private PingAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
    }

    public static PingAction getInstance(StatusChangedListener listener,
            String type) {
        if (instance == null) {
            instance = new PingAction(listener, type);
        }
        mType = type;
        return instance;
    }

    @Override
    public void start(String param) {
        Log.d(TAG, "CallAction tpye:" + mType + " param:" + param);
        mParam = param;
        if(mType.equals(SLTConstant.ACTION_TYPE_START_INTERNET_PING)) {
            mResault = "ping";
            new NetPing().execute();
            //ok();
        } else if(mType.equals(SLTConstant.ACTION_TYPE_GET_INTERNET_PING)) {
            end(mResault);
        } else {
            error("invalid cmd!");
        }
    }

    public String Ping(String str) {
        Process p;
        try {
            p = Runtime.getRuntime().exec("ping -c 1 -w 2 " + str);
            int status = p.waitFor();

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input,Charset.defaultCharset()));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            System.out.println("Return ============" + buffer.toString());
            Log.d("huasong", "return:" + buffer.toString());
            Log.d("huasong", "status:" + status);
            if (status == 0) {
                mResault = "pass";
            } else {
                mResault = "fail";
            }
            Log.d("huasong", "resault:" + mResault);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mResault;
    }

    private class NetPing extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String s = "";
            if(TextUtils.isEmpty(mParam)) {
                s = Ping("www.baidu.com");
            } else {
                s = Ping(mParam);
            }
            Log.i("ping", s);
            return s;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "NetPing result="+result);
            if("pass".equals(result)){
                ok();
            }else{
                error("fail");
            }
        }
    }


    @Override
    public void stop() {
    }
}