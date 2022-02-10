package com.sprd.autoslt.action.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.SLTUtil;

public class SendFileAction extends AbstractAction {
    private static final String TAG = "SendFileAction";
    private static SendFileAction instance;
    public static File file;
    public static String SendFileFlag = null;
    public static int bufferSize = 0;

    public static String getSendFileFlag() {
        return SendFileFlag;
    }

    public static void setSendFileFlag(String Flag) {
        SendFileFlag = Flag;
    }

    private SendFileAction(StatusChangedListener listener) {
        super(listener);
        // TODO Auto-generated constructor stub
    }

    public static SendFileAction getInstance(StatusChangedListener listener) {
        if (instance == null) {
            instance = new SendFileAction(listener);
        }
        return instance;
    }

    @Override
    public void start(String param) {
        // TODO Auto-generated method stub
        Log.d(TAG, "SendFileAction:" + param);
        String mFileName = null;
        if (!TextUtils.isEmpty(param)) {
            String[] fileAndbuffer = SLTUtil.parseParam(param);
            if (fileAndbuffer.length == 2) {
                mFileName = fileAndbuffer[0];
                bufferSize = Integer.parseInt(fileAndbuffer[1]);
                file = new File(mFileName);
                if (!file.exists()) {
                    Log.d(TAG, "file not exists!");
                    error("file not exists!");
                    Toast.makeText(mContext,
                            "File " + mFileName + " not exist!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                end(String.valueOf(file.length()) + "^" + getMD5File(file));
            } else {
                error("invalid param");
            }
        }
    }

    @Override
    public void stop() {
    }

    public void sendFile(SocketChannel socketChannel, File file, int bsize)
            throws IOException {
        Log.d(TAG, "sendfile begin!");
        String note = null;
        FileInputStream fis = null;
        FileChannel channel = null;
        try {
            fis = new FileInputStream(file);
            channel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * bsize);
            int size = 0;
            while ((size = channel.read(buffer)) > -1) {
                Log.d(TAG, "size = " + size);
                buffer.rewind();
                buffer.limit(size);
                socketChannel.write(buffer);
                buffer.clear();
                int m = 0;
                do {
                    Log.d(TAG, "SendFileFlag = " + SendFileFlag);
                    if (SendFileFlag != null
                            && SendFileFlag.equalsIgnoreCase("nack")) {
                        Log.d(TAG, "SendFileFlag == nack");
                        buffer.rewind();
                        buffer.limit(size);
                        socketChannel.write(buffer);
                        buffer.clear();
                        SendFileFlag = null;
                        m = 0;
                    }
                    try {
                        Log.d(TAG, "sleep m =" + m);
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    m++;
                    if (m > 2000) {
                        break;
                    }
                } while (SendFileFlag == null
                        || SendFileFlag.equalsIgnoreCase("")
                        || SendFileFlag.equalsIgnoreCase("nack"));
                if (SendFileFlag != null
                        && SendFileFlag.equalsIgnoreCase("ack")) {
                    Log.d(TAG, "SendFileFlag == ack");
                    buffer.clear();
                    SendFileFlag = null;
                    m = 0;
                } else if (SendFileFlag != null
                        && SendFileFlag.equalsIgnoreCase("end")) {
                    Log.d(TAG, "SendFileFlag == end");
                    buffer.clear();
                    SendFileFlag = null;
                    note = "end";
                    m = 0;
                    break;
                }
                if (m > 2000 && SendFileFlag == null) {
                    Log.d(TAG, "time out ");
                    note = "timeout";
                    m = 0;
                    break;
                }
            }
            if (size == -1) {
                Log.d(TAG, "size == -1 ,end");
                buffer.clear();
                SendFileFlag = null;
                note = "end";
            }
            // if(size == -1){
            // Log.d(TAG, "sendfile sum end and MD5!");
            // buffer.put(End.getBytes()).put(getMD5File(file).getBytes());
            // socketChannel.write(buffer);
            // buffer.clear();
            // }
            // socketChannel.socket().shutdownOutput();
        } finally {
            try {
                Log.d(TAG, "channel.close ");
                channel.close();
            } catch (Exception ex) {
            }
            try {
                Log.d(TAG, "fis.close() " + "note =" + note);
                fis.close();
                SendFileFlag = null;
                end("end", note);
            } catch (Exception ex) {
            }
        }
    }

    public static String getMD5File(File file) {

        Log.d(TAG, "getFile MD5!");

        if (!file.isFile()) {
            System.out.println("File is not exsit!!!");
            return null;
        }
        int bufferSize = 128 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            digestInputStream = new DigestInputStream(fileInputStream,
                    messageDigest);
            byte[] buffer = new byte[bufferSize];
            if (digestInputStream.read(buffer) > 0) {
                messageDigest = digestInputStream.getMessageDigest();
            }
            byte[] resultByteArray = messageDigest.digest();
            return byteArrayToHex(resultByteArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }

}
