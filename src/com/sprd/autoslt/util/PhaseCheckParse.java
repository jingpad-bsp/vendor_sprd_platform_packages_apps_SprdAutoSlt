
package com.sprd.autoslt.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

public class PhaseCheckParse {
    private static String TAG = "PhaseCheckParse";

    private static int TYPE_STATION_SP05 = 0;
    private static int TYPE_STATION_SP09 = 1;
    private static int TYPE_STATION_MAX = 3;

    private static int TYPE_GET_SN1 = 0;
    private static int TYPE_GET_SN2 = 1;
    private static int TYPE_WRITE_STATION_TESTED = 2;
    private static int TYPE_WRITE_STATION_PASS = 3;
    private static int TYPE_WRITE_STATION_FAIL = 4;
    private static int TYPE_GET_PHASECHECK = 5;
    private static int TYPE_WRITE_CHARGE_SWITCH = 6;

    private int stationType = TYPE_STATION_SP09;
    private static int BUF_SIZE = 4096;

    //private byte[] stream = new byte[300];
    private AdaptBinder binder;

    public PhaseCheckParse() {
        try {
            binder = new AdaptBinder();

            if(binder != null)
                Log.e(TAG, "Get The service connect!");
            else
                Log.e(TAG, "connect Error!!");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private boolean hasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches())
            flag = true;
        return flag;
    }

    private String StationTested(char testSign, char item) {
        if(testSign=='0' && item=='0') return "PASS";
        if(testSign=='0' && item=='1') return "FAIL";
        return "UnTested";
    }
	
	public boolean isStationExsit(String station_name){
    	int rs = getStationTest(station_name);
    	if (rs == -1) {
			return false;
		}else {
			return true;
		}
    }

    private int getStationTest(String station_name) {
        Log.d(TAG, "getStationTest: "+station_name);
        if (stationType < TYPE_STATION_SP05 || stationType > TYPE_STATION_MAX ) {
            Log.d(TAG, "getStationTest return err:  stationType = "+stationType);
            return -1;
        }

        int ret = -1;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            binder.transact(TYPE_GET_PHASECHECK, data, reply, 0);
            Log.e(TAG, "transact SUCESS!!");
            int testSign = reply.readInt();
            int item = reply.readInt();
            String stationName = reply.readString();
            String []str = stationName.split(Pattern.quote("|"));
            String strTestSign = Integer.toBinaryString(testSign);
            String strItem = Integer.toBinaryString(item);
            Log.e(TAG, "strTestSign = " + strTestSign + " strItem = " + strItem);
            for(int i=0; i<str.length; i++) {
                Log.e(TAG, "str =  "+str[i]);
                if (station_name.equalsIgnoreCase(str[i])) {
                    ret = i;
                }
            }

            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "huasong Exception " + ex.getMessage());
            return -1;
        }

        Log.d(TAG, "getStationTest return "+ret);
        return ret;
    }

    public String getSn() {
        String result = null;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            binder.transact(0, data, reply, 0);
            Log.e(TAG, "transact end");
            String sn1 = reply.readString();
            for(int i = 0; i < 5; i++) {
                if(hasDigit(sn1)) {
                    break;
                }
                binder.transact(TYPE_GET_SN1, data, reply, 0);
                sn1 = reply.readString();
            }
            binder.transact(TYPE_GET_SN2, data, reply, 0);
            String sn2 = reply.readString();
            for(int i = 0; i < 5; i++) {
                if(hasDigit(sn2)) {
                    break;
                }
                binder.transact(1, data, reply, 0);
                sn2 = reply.readString();
            }
            if(!sn1.isEmpty() && sn1.length() > 24 && !sn2.isEmpty() && sn2.length() > 0) {
                /*SPRD bug 838344:Read sn issue.*/
                if(sn1.length() > sn2.length() && sn1.contains(sn2)){
                    sn1 = sn1.substring(0, sn1.length() - sn2.length());
                    Log.e(TAG, "sn1 contains sn2 ,SN1 = " +  sn1 + "\n SN2=" + sn2);
                }
                /*@}*/
            }
             // result = "SN1:" + sn1 + "\n" + "SN2:" + sn2;
            Log.e(TAG, "SN1 = " +  sn1 + " SN2=" + sn2);
            Log.e(TAG, "sn1.length() = " +  sn1.length() + " sn2.length()=" + sn2.length());
            if (sn1.length()<=1) {
                sn1 = "";
            }else {
                sn1 = sn1.substring(0, sn1.length() - 1);
            }
            if (sn2.length()<=1) {
                sn2 = "";
            }else {
                sn2 = sn2.substring(0, sn2.length() - 1);
            }
            Log.e(TAG, "2222 SN1 = " +  sn1 + " SN2=" + sn2);
          //  result = sn1;
            result = sn1 + "," + sn2;
            Log.e(TAG, "result = " +  result );
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "Exception :" + ex);
            ex.printStackTrace();
            result = "get SN fail:" + ex.getMessage();
        }
        return result;
    }

    public boolean writeStationTested(int station) {
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            data.writeInt(station);
            binder.transact(TYPE_WRITE_STATION_TESTED, data, reply, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }

    public boolean writeStationTested(String station) {
        int stat = getStationTest(station);
        Log.e(TAG, "stat = " + stat);
        if (stat == -1) {
            return false;
        }else {
            return writeStationTested(stat);
        }
    }

    public boolean writeStationPass(int station) {
        try{
            Parcel data = new Parcel();
			Parcel reply = new Parcel();
            data.writeInt(station);
            binder.transact(TYPE_WRITE_STATION_PASS, data, reply, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }

    public boolean writeStationPass(String station) {
        int stat = getStationTest(station);
        Log.e(TAG, "stat = " + stat);
        if (stat == -1) {
            return false;
        }else {
            return writeStationPass(stat);
        }
    }

    public boolean writeChargeSwitch(int value) {
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            data.writeInt(value);
            binder.transact(TYPE_WRITE_CHARGE_SWITCH, data, reply, 0);
            Log.e(TAG, "writeChargeSwitch data = " + reply.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " , ex);
            return false;
        }
    }

    public boolean writeStationFail(int station) {
        try{
            Parcel data = new Parcel();
			Parcel reply = new Parcel();
            data.writeInt(station);
            binder.transact(TYPE_WRITE_STATION_FAIL, data, reply, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }

    public boolean writeStationFail(String station) {
        int stat = getStationTest(station);
        Log.e(TAG, "stat = " + stat);
        if (stat == -1) {
            return false;
        }else {
            return writeStationFail(stat);
        }
    }

    /*public byte readOffsetValue(int offset) {
        byte value = (byte)0xFF;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            data.writeInt(offset);
            binder.transact(TYPE_READ_OFFSET, data, reply, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            value = reply.readByteArray(value);
            Log.e(TAG, "value = "+value);
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
        }

        return value;
    }

    public boolean writeOffsetValue(int offset, byte value) {
        try{
            Parcel data = new Parcel();
            data.writeInt(offset);
            //data.writeByte(value);
            data.writeByteArray(b, offset, len)
            binder.transact(TYPE_WRITE_OFFSET, data, null, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }*/

    public String getPhaseCheck() {
        String result = null;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            binder.transact(TYPE_GET_PHASECHECK, data, reply, 0);
            Log.e(TAG, "transact SUCESS!!");
            int testSign = reply.readInt();
            int item = reply.readInt();
            String stationName = reply.readString();
            String []str = stationName.split(Pattern.quote("|"));
            String strTestSign = Integer.toBinaryString(testSign);
            String strItem = Integer.toBinaryString(item);
            char[] charSign = strTestSign.toCharArray();
            char[] charItem = strItem.toCharArray();
            StringBuffer sb = new StringBuffer();
            Log.e(TAG, "strTestSign = " + strTestSign + " strItem = " + strItem);
            for(int i=0; i<str.length; i++) {
                sb.append(str[i]+":"+StationTested(charSign[charSign.length-i-1], charItem[charItem.length-i-1])+"\n");
            }
            result = sb.toString();
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "huasong Exception " + ex.getMessage());
            result = "get phasecheck fail:" + ex.getMessage();
        }
        return result;
    }

    /* SPRD: 435125 The serial number shows invalid in ValidationTools @{*/
    public static String getSerialNumber(){
        return android.os.Build.SERIAL;
    }
    /* @}*/

    public boolean writeLedlightSwitch(int code, int value) {
        try {
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            Log.e(TAG, "writeLedlightSwitch light code = " + code+",value="+value);
            logLedLight(code);
            data.writeInt(value);
            binder.transact(code, data, reply, 0);
            Log.e(TAG, "writeLedlightSwitch light data = " + reply.readString() + " SUCESS!!");
            data.recycle();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "Exception ", ex);
            return false;
        }
    }
    private void logLedLight(int code){
        switch (code) {
        case 7:
            Log.d(TAG, "Blue light!");
            break;
        case 8:
            Log.d(TAG, "Blue light!");
            break;
        case 9:
            Log.d(TAG, "Blue light!");
            break;

        default:
            Log.d(TAG, "Unknow light!");
            break;
        }
    }

    class AdaptParcel {
        int code;
        int dataSize;
        int replySize;
        byte[] data;
    }

    private static String SOCKET_NAME = "phasecheck_srv";
    class AdaptBinder {
        private LocalSocket socket = new LocalSocket();
        private LocalSocketAddress socketAddr = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT);
        private OutputStream mOutputStream;
        private InputStream mInputStream;
        private AdaptParcel mAdpt;

        public AdaptBinder() {
            mAdpt = new AdaptParcel();
            mAdpt.data = new byte[BUF_SIZE];
            mAdpt.code = 0;
            mAdpt.dataSize = 0;
            mAdpt.replySize = 0;
        }

        private void int2byte(byte[] dst, int offset, int value) {
            dst[offset+3] = (byte)(value >> 24 & 0xff);
            dst[offset+2] = (byte)(value >> 16 & 0xff);
            dst[offset+1] = (byte)(value >> 8 & 0xff);
            dst[offset] = (byte)(value & 0xff);
        }

        public int byte2Int(byte[] bytes, int off) {
            int b0 = bytes[off] & 0xFF;
            int b1 = bytes[off + 1] & 0xFF;
            int b2 = bytes[off + 2] & 0xFF;
            int b3 = bytes[off + 3] & 0xFF;
            return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
        }

        public synchronized void sendCmdAndRecResult(AdaptParcel adpt) {
            Log.d(TAG, "send cmd: ");
            //LogArray(adpt.data, 19);
            byte[] buf = new byte[BUF_SIZE];
            int2byte(buf, 0, adpt.code);
            int2byte(buf, 4, adpt.dataSize);
            int2byte(buf, 8, adpt.replySize);

            //LogArray(adpt.data, 19);
            System.arraycopy(adpt.data, 0, buf, 12, adpt.dataSize+adpt.replySize);
            Log.d(TAG, "code = "+adpt.code);
            Log.d(TAG, "dataSize = "+adpt.dataSize);
            Log.d(TAG, "replySize = "+adpt.replySize);
            //LogArray(buf, 19);

            try {
                socket = new LocalSocket();
                if (!socket.isConnected()) {
                    Log.d(TAG, "isConnected...");
                    socket.connect(socketAddr);
                }

                Log.d(TAG, "mSocketClient connect is " + socket.isConnected());
                mOutputStream = socket.getOutputStream();
                if (mOutputStream != null) {
                    Log.d(TAG, "write...");
                    mOutputStream.write(buf);
                    mOutputStream.flush();
                    Log.d(TAG, "write succ...");
                }
                mInputStream = socket.getInputStream();
                Log.d(TAG, "read ....");
                int count = mInputStream.read(buf, 0, BUF_SIZE);
                Log.d(TAG, "count = " + count + "");
                //LogArray(buf, 19);

                adpt.code = byte2Int(buf, 0);
                adpt.dataSize = byte2Int(buf, 4);
                adpt.replySize = byte2Int(buf, 8);

                Log.d(TAG, "code = "+adpt.code);
                Log.d(TAG, "dataSize = "+adpt.dataSize);
                Log.d(TAG, "replySize = "+adpt.replySize);

                System.arraycopy(buf, 12, adpt.data, 0, adpt.dataSize+adpt.replySize);

                //LogArray(adpt.data, 19);

            } catch (IOException e) {
                Log.e(TAG, "Failed get output stream: " + e.toString());
                return ;
            } finally {
                try {
                    buf = null;
                    if (mOutputStream != null) {
                        mOutputStream.close();
                    }
                    if (mInputStream != null) {
                        mInputStream.close();
                    }
                    if (socket != null) {
                        if (socket.isConnected()) {
                            socket.close();
                            socket = null;
                        } else {
                            socket = null;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "catch exception is " + e);
                    return ;
                }
            }
        }

        private void convertParcel(AdaptParcel adpt, int code, Parcel data, Parcel reply) {
            data.setDataPosition(0);
            reply.setDataPosition(0);

            data.writeByteArrayInternal(adpt.data, 0, adpt.dataSize);
            reply.writeByteArrayInternal(adpt.data, adpt.dataSize, adpt.replySize);

            Log.e(TAG, "convertParcel: dataSize = "+data.dataSize()+", replySize = "+ reply.dataSize());

            data.setDataPosition(0);
            reply.setDataPosition(0);
        }

        private void convertAdaptParcel(int code, Parcel data, Parcel reply) {
            if(mAdpt == null){
                Log.e(TAG, "convertAdaptParcel2: mAdpt == null!");
                return;
            }
            mAdpt.code = code;

            data.setDataPosition(0);
            reply.setDataPosition(0);

            data.LogArray();
            byte[] bData = new byte[data.dataSize()];
            data.readByteArray(bData);
            for(int i = 0; i < data.dataSize(); i++){
                mAdpt.data[i] = bData[i];
            }

            byte[] bReply = new byte[reply.dataSize()];
            reply.readByteArray(bReply);
            for(int i = 0; i < reply.dataSize(); i++){
                mAdpt.data[i+data.dataSize()] = bReply[i];
            }
            mAdpt.dataSize = data.dataSize();
            mAdpt.replySize = reply.dataSize();
            Log.e(TAG, "convertAdaptParcel2: dataSize = "+data.dataSize()+", replySize = "+ reply.dataSize());
            //LogArray(mAdpt.data, 19);

            data.setDataPosition(0);
            reply.setDataPosition(0);
        }

        public void transact(int code, Parcel data, Parcel reply, int flags) throws Exception {
            Log.d(TAG, "transact start....");
            convertAdaptParcel(code, data, reply);
            sendCmdAndRecResult(mAdpt);
            convertParcel(mAdpt, code, data, reply);

            Log.d(TAG, "transact end....");
        }
    }

    static class Parcel {
        private int mDataSize;
        private int mPos;
        private byte[] mData;
        private Parcel() {
            mData = new byte[BUF_SIZE];
            mPos = 0;
            mDataSize = 0;
        }

        public void writeByteArray(byte[] b, int offset, int len) {
            if (len == 0) return;
            writeInt(len);
            writeByteArrayInternal(b, offset, len);
        }

        public void writeByteArrayInternal(byte[] b, int offset, int len) {
            if (len == 0) return;
            System.arraycopy(b, offset, mData, mPos, len);
            mPos += len;
            mDataSize += len;
        }

        public void readByteArray(byte[] val) {
            System.arraycopy(mData, mPos, val, 0, val.length);
            mPos += val.length;
        }

        public int dataSize() {
            return mDataSize;
        }

        public void writeInt(int i) {
            Log.d(TAG, "ningbiao writeInt i="+i);
            mData[mPos+3] = (byte)(i >> 24 & 0xff);
            mData[mPos+2] = (byte)(i >> 16 & 0xff);
            mData[mPos+1] = (byte)(i >> 8 & 0xff);
            mData[mPos] = (byte)(i & 0xff);
            mPos += 4;
            mDataSize += 4;
        }

        public int readInt() {
            int b0 = mData[mPos] & 0xFF;
            int b1 = mData[mPos + 1] & 0xFF;
            int b2 = mData[mPos + 2] & 0xFF;
            int b3 = mData[mPos + 3] & 0xFF;
            mPos += 4;
            return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
        }

        public void setDataPosition(int i) {
            mPos = i;
        }

        public String readString() throws Exception{
            int nNum = readInt();
            byte[] b = new byte[nNum];
            Log.d(TAG, "readString num = "+nNum);
            readByteArray(b);

            return new String(b, 0, nNum, "utf-8");
        }

        public void recycle() {
            reset();
        }

        public void reset() {
            mPos = 0;
            mDataSize = 0;
        }

        public void LogArray(){
            Log.e(TAG, "array length = "+mData.length);
            for(int i = 0; i < mData.length; i++){
                if (i > 19) break;
                Log.e(TAG, "Parcel LogArray : ("+i+") = "+mData[i]);
            }
        }
    }
	
	/*
    private static String TAG = "PhaseCheckParse";

    private IBinder binder;
    private static PhaseCheckParse instance;
    private static int TYPE_GET_SN1 = 0;
    private static int TYPE_GET_SN2 = 1;
    private static int TYPE_WRITE_STATION_TESTED = 2;
    private static int TYPE_WRITE_STATION_PASS = 3;
    private static int TYPE_WRITE_STATION_FAIL = 4;
    private static int TYPE_GET_PHASECHECK = 5;
    private static int TYPE_WRITE_CHARGE_SWITCH = 6;

    private  PhaseCheckParse() {
        binder = ServiceManager.getService("phasechecknative");

        if(binder != null)
            Log.e(TAG, "Get The service connect!");
        else
            Log.e(TAG, "connect Error!!");
    }
    
    public static PhaseCheckParse getInstance() {
        if(instance == null) {
            instance = new PhaseCheckParse();
        }
        return instance;
    }

    private boolean hasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches())
            flag = true;
        return flag;
    }

    private boolean isInvalid(String content) {
        boolean flag = true;
        Pattern p = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher m = p.matcher(content);
        if (m.matches())
            flag = false;
        return flag;
    }

    private String StationTested(char testSign, char item) {
        if(testSign=='0' && item=='0') return "PASS";
        if(testSign=='0' && item=='1') return "FAIL";
        return "UnTested";
    }

    public String getSn() {
        String result = null;
        try{
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            binder.transact(0, data, reply, 0);
            Log.e(TAG, "transact end");
            String sn1 = reply.readString();
            for(int i = 0; i < 5; i++) {
                if(hasDigit(sn1)) {
                    break;
                }
                binder.transact(TYPE_GET_SN1, data, reply, 0);
                sn1 = reply.readString();
            }
            binder.transact(TYPE_GET_SN2, data, reply, 0);
            String sn2 = reply.readString();
            for(int i = 0; i < 5; i++) {
                if(hasDigit(sn2)) {
                    break;
                }
                binder.transact(1, data, reply, 0);
                sn2 = reply.readString();
            }
            SPRD: Add for bug556367, Equipment serial number are unintelligible string {@ 
            if (!sn1.isEmpty() && isInvalid(sn1)) {
                sn1 = "invalid";
            }
            if (!sn2.isEmpty() && isInvalid(sn2)) {
                sn2 = "invalid";
            }
             {@ 
            result = "SN1:" + sn1 + "\n" + "SN2:" + sn2;
            Log.e(TAG, "SN1 = " +  sn1 + " SN2=" + sn2);
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            result = "get SN fail:" + ex.getMessage();
        }
        return result;
    }

    public String getPhaseCheck() {
        String result = null;
        try{
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            binder.transact(TYPE_GET_PHASECHECK, data, reply, 0);
            Log.e(TAG, "transact SUCESS!!");
            int testSign = reply.readInt();
            int item = reply.readInt();
            String stationName = reply.readString();
            String []str = stationName.split(Pattern.quote("|"));
            String strTestSign = Integer.toBinaryString(testSign);
            String strItem = Integer.toBinaryString(item);
            char[] charSign = strTestSign.toCharArray();
            char[] charItem = strItem.toCharArray();
            StringBuffer sb = new StringBuffer();
            Log.e(TAG, "strTestSign = " + strTestSign + " strItem = " + strItem);
            for(int i=0; i<str.length; i++) {
                sb.append(str[i]+":"+StationTested(charSign[charSign.length-i-1], charItem[charItem.length-i-1])+"\n");
            }
            result = sb.toString();
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "huasong Exception " + ex.getMessage());
            result = "get phasecheck fail:" + ex.getMessage();
        }
        return result;
    }

    public String getPhaseCheckResult(String caseName){
    	String allRe = null;
    	String result = null;
    	allRe = getPhaseCheck();
    	Log.d(TAG, "allRe = " +allRe);
    	String[] value = allRe.split("\n");
    	Log.d(TAG, "value.length() = " +value.length);
    	for (int i = 0; i < value.length; i++) {
			if (value[i].contains(caseName)) {
				result = value[i];
			}
		}
    	Log.d(TAG, "result = " +result);
    	if (result != null) {
    		String[] keyValue = result.split(":");
    		result = keyValue[1];
		}
    	return result;
    }
	
    public static String getSerialNumber(){
        return android.os.Build.SERIAL;
    }
	
    public boolean writeChargeSwitch(int value) {
        try{
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInt(value);
            binder.transact(TYPE_WRITE_CHARGE_SWITCH, data, reply, 0);
            Log.e(TAG, "writeChargeSwitch data = " + reply.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " , ex);
            return false;
        }
    }*/
}