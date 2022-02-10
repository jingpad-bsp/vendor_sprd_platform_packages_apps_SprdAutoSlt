package com.sprd.autoslt.connect.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.sprd.autoslt.SLTLogManager;
import com.sprd.autoslt.action.impl.SendFileAction;
import com.sprd.autoslt.cmd.CmdConstant;
import com.sprd.autoslt.connect.AbstractServer;

public class SLTServer extends AbstractServer implements Runnable {

    private static final String TAG = "SLTServer";

    private static final int LOCAL_PORT = 7878;

    private HandlerThread mServerHanderThread;
    private HandlerThread mStartTestHanderThread;
    private Handler mSLTServerHandler;
    private Handler mStartTestHandler;
	public static SocketChannel mClient;
    int i = 0;
    int j = 0;

    String mSendMessage;
    private boolean mIsStop = false;

    // private final ReentrantLock mRenderLock = new ReentrantLock();
    // private final Condition mFreezeCondition = mRenderLock.newCondition();

    public SLTServer(CmdReceiverListener listener) {

        super(listener);
    }

    @Override
    public void start() {
        Log.d(TAG, "SLTServer start has started!");
        mIsStop = false;

        mServerHanderThread = new HandlerThread("SLT-Server-Thread");
        mServerHanderThread.start();

        mSLTServerHandler = new Handler(mServerHanderThread.getLooper());
        mSLTServerHandler.post(this);
        mStartTestHanderThread = new HandlerThread("SLT-StartTest-Thread");
        mStartTestHanderThread.start();
        mStartTestHandler = new Handler(mStartTestHanderThread.getLooper());
        
    }

    @Override
    public void run() {
        Log.d(TAG, "SLTServer run has started!");
        Selector mSelector = null;
        ServerSocketChannel mServer = null;
        /*SocketChannel*/ mClient = null;
        try {
            mSelector = Selector.open();
            mServer = ServerSocketChannel.open();
            mServer.configureBlocking(false);

            mServer.socket().setReuseAddress(true);

            InetSocketAddress sa = new InetSocketAddress(LOCAL_PORT);
            mServer.socket().bind(sa);

            mServer.register(mSelector, SelectionKey.OP_ACCEPT);

            Log.d(TAG, "SLTServer has started!");
            while (!mIsStop && mSelector.select() > 0) {
                Iterator<SelectionKey> it = mSelector.selectedKeys().iterator();

                while (it.hasNext()) {
                    try {
						SelectionKey key = it.next();
						it.remove();

						if (key.isAcceptable()) {
						    ServerSocketChannel serverChannel = (ServerSocketChannel) key
						            .channel();
						    mClient = serverChannel.accept();
						    mClient.configureBlocking(false);
						    mClient.register(mSelector, SelectionKey.OP_READ);
						    Log.d(TAG,
						            "SLTServer has connected with client socket : "
						                    + mClient.toString());
						    SLTLogManager.sendLog("SLTServer has connected");
						}
						
						if (key.isReadable()) {
						    mClient = (SocketChannel) key.channel();
						    if (!mClient.socket().isConnected()) {
						        key.interestOps(SelectionKey.OP_ACCEPT);
						        continue;
						    }
						    ByteBuffer buffer = ByteBuffer.allocate(256);
						    StringBuilder recMessage = new StringBuilder();
						    if (mClient.read(buffer) >= 0) {
						        recMessage
						                .append(new String(buffer.array()).trim());
						    } else {
						        continue;
						    }
						    Log.d(TAG,
						            "SLTServer receiver msg -> "
						                    + recMessage.toString());
						    SLTLogManager.sendLog("-->" + recMessage.toString());
						    buffer.clear();

						    j++;
						    Log.d("huasong",
						            "j:" + j + "msg:" + recMessage.toString());
						    //receiver(recMessage.toString());
							if (recMessage.toString().equalsIgnoreCase("ack")
									|| recMessage.toString().equalsIgnoreCase("nack")
									|| recMessage.toString().equalsIgnoreCase("end")) {
								SendFileAction.setSendFileFlag(recMessage.toString());
							}else if (recMessage.toString().equalsIgnoreCase("start")) {
								mStartTestHandler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										try {
											SendFileAction.getInstance(null).sendFile(mClient, SendFileAction.file,SendFileAction.bufferSize);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
							}else{
							    receiver(recMessage.toString());
							}
						    key.interestOps(SelectionKey.OP_WRITE
						            | SelectionKey.OP_READ);
						}
						// add by ansel
						try {
						    Thread.sleep(50);
						} catch (Exception e) {
						    Log.d(TAG, "" + e);
						}
						if (key.isWritable()) {
						    // mRenderLock.lock();
						    // if (mSendMessage == null) {
						    // mFreezeCondition.awaitUninterruptibly();
						    // }
						    // mRenderLock.unlock();
						    if (mSendMessage == null) {
						        continue;
						    }
						    String sendMsg = mSendMessage;
						    mSendMessage = null;
						    Log.d("huasong", "send cmd:" + sendMsg);
						    SLTLogManager.sendLog("<--" + sendMsg);
						    // sendResult(sendMsg);
						    mClient = (SocketChannel) key.channel();
						    ByteBuffer buffer = ByteBuffer.wrap(sendMsg.getBytes());
						    mClient.write(buffer);
						    buffer.clear();
						    i++;
						    Log.d("huasong", " i: " + i);
						    // client test error, send error to PC, and stop self.
						    if (sendMsg.endsWith(CmdConstant.STATUS_ERROR)) {
						        // stop();
						        // return;
						    }
							/*if (sendMsg.contains("GetFile")) {
								SendFileAction.sendFile(mClient, SendFileAction.file);
							}*/						
						    key.interestOps(SelectionKey.OP_READ);							
                            if (mClient != null) {
						        Log.d(TAG, "normal-- mClient.close !!");
							    mClient.socket().shutdownOutput();
						        mClient.socket().close();
						        mClient.close();
						    }							
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						try {            	
							if (mClient != null) {
								Log.d(TAG, "exception-- mClient.close !!");
							    mClient.socket().shutdownOutput();				
							    mClient.socket().close();
							    mClient.close();
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}	
					}

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            SLTLogManager.sendLog("close");
        } finally {
            try {
                if (mSelector != null) {
                    mSelector.close();
                }
                if (mServer != null) {
                    mServer.socket().close();
                    mServer.close();
                }							
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receiver(final String cmdStr) {
        /*mMainHandler*/mStartTestHandler.post(new Runnable() {
            @Override
            public void run() {
                mCmdReceiverListener.onCmdReceiver(cmdStr);
                Log.d(TAG, "mCmdReceiverListener.onCmdReceiver(cmdStr); ");
            }
        });
        // mCmdReceiverListener.onCmdReceiver(cmdStr);
    }

    @Override
    public void send(final String cmdStr) {
        // mRenderLock.lock();
        Log.d(TAG, " send(): SLTServer send cmdStr: " + cmdStr);
        mSendMessage = cmdStr;
        // mFreezeCondition.signalAll();
        // mRenderLock.unlock();
    }

    @Override
    public void stop() {
        Log.d(TAG, "SLTServer will stop!");
        mIsStop = true;
    }

    @Override
    public boolean isStoped() {
        return mIsStop;
    }
}
