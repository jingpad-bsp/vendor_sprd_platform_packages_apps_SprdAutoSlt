
package com.sprd.autoslt.util;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import android.util.Log;


public class SocketUtils {

    private static final String TAG = "SocketUtils";
    public static final String OK = "OK";
    public static final String FAIL = "FAIL";

    private  String mSocketName = null;
    private  LocalSocket mSocketClient = null;
    private  OutputStream mOutputStream;
    private  InputStream mInputStream;
    private  LocalSocketAddress mSocketAddress;
    public SocketUtils(String socketName){
        mSocketName = socketName;
        Log.d(TAG,  " mSocketName is " + mSocketName);
    }
    public  String sendCmdAndRecResult(String strcmd) {
        Log.d(TAG, mSocketName +" send cmd: " + strcmd);
        byte[] buf = new byte[255];
        int retryCount = 5;
        String result = null;
        if (mOutputStream == null || mInputStream == null ) {
            mSocketClient = new LocalSocket();
            mSocketAddress = new LocalSocketAddress(mSocketName, LocalSocketAddress.Namespace.ABSTRACT);
            for (int i = 0; i < retryCount; i++) {
                try {
                    mSocketClient.connect(mSocketAddress);
                    if (mSocketClient.isConnected()) {
                        break;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.w(TAG, "connect "+mSocketName+" for "+ i +" times failed", e);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
            if (mSocketClient.isConnected()) {
                Log.i(TAG, "connect " + mSocketName + " success");
                try {
                    mOutputStream = mSocketClient.getOutputStream();
                    mInputStream = mSocketClient.getInputStream();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.w(TAG, "getOutputStream or getInputStream failed", e);
                    try {
                        mSocketClient.close();
                        mSocketClient = null;
                        mOutputStream = null;
                        mInputStream = null;
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    return null;
                }
            }else {
                mSocketClient = null;
                return null;
            }
        }
        try {
            final StringBuilder cmdBuilder = new StringBuilder(strcmd).append('\0');
            final String cmd = cmdBuilder.toString();
            mOutputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
            mOutputStream.flush();
            Log.d(TAG, strcmd+"result read beg...");
            int count = mInputStream.read(buf, 0, 255);
            Log.d(TAG, strcmd+"result read done");
            result = "";
            result = new String(buf, "utf-8");
            Log.d(TAG, "count = " + count + ", result is " + result);
        } catch (IOException e) {
            Log.e(TAG, "send cmd error or read result error" + e.toString());
            closeSocket();
            return null;
        }
        Log.d(TAG, strcmd+"handle over and result is :"+result);
        return result;
    }
    public  void sendCmd(String strcmd) {
        Log.d(TAG, mSocketName +" send cmd: " + strcmd);
        int retryCount = 5;
        String result = null;
        if (mOutputStream == null || mInputStream == null ) {
            mSocketClient = new LocalSocket();
            mSocketAddress = new LocalSocketAddress(mSocketName, LocalSocketAddress.Namespace.ABSTRACT);
            for (int i = 0; i < retryCount; i++) {
                try {
                    mSocketClient.connect(mSocketAddress);
                    if (mSocketClient.isConnected()) {
                        break;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.w(TAG, "connect "+mSocketName+" for "+ i +" times failed", e);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
            if (mSocketClient.isConnected()) {
                Log.i(TAG, "connect " + mSocketName + " success");
                try {
                    mOutputStream = mSocketClient.getOutputStream();
                    mInputStream = mSocketClient.getInputStream();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.w(TAG, "getOutputStream or getInputStream failed", e);
                    try {
                        mSocketClient.close();
                        mSocketClient = null;
                        mOutputStream = null;
                        mInputStream = null;
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    return ;
                }
            }else {
                mSocketClient = null;
                return ;
            }
        }
        try {
            final StringBuilder cmdBuilder = new StringBuilder(strcmd).append('\0');
            final String cmd = cmdBuilder.toString();
            mOutputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
            mOutputStream.flush();
            Log.d(TAG, strcmd+"result read beg...");
        
        } catch (IOException e) {
            Log.e(TAG, "send cmd error or read result error" + e.toString());
            closeSocket();
       
            return ;
        } 
        Log.d(TAG, strcmd+"handle over and result is :"+result);
 
    }
    public String sendCmdAndRecResult(
            String strcmd, int retryCount) {
        while (retryCount-- != 0) {
            String tmp = sendCmdAndRecResult( strcmd);
            if (tmp != null)
                return tmp;
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            Log.d(TAG, "try again" + retryCount);
        }
        return null;
    }

    public  void closeSocket() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
            if (mInputStream != null) {
                mInputStream.close();
                mOutputStream = null;
            }
            if (mSocketClient != null && mSocketClient.isConnected()) {
                mSocketClient.close();
                mSocketClient = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "catch exception is " + e);
        }
    }
}
