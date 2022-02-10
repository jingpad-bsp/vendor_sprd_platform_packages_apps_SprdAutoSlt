package com.sprd.autoslt.connect;

public abstract class AbstractServer implements IServer {

    public CmdReceiverListener mCmdReceiverListener;

    public interface CmdReceiverListener {
        void onCmdReceiver(String cmdStr);
    }

    public AbstractServer(CmdReceiverListener listener) {
        mCmdReceiverListener = listener;
    }
}
