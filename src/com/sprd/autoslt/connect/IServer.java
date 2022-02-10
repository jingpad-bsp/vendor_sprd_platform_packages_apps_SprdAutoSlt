package com.sprd.autoslt.connect;

import android.os.Handler;
import android.widget.TextView;

public interface IServer {

    void start();

    void stop();

    void receiver(String cmdStr);

    void send(String cmdStr);

    boolean isStoped();
}
