package com.sprd.autoslt.action.impl.shell;

import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.ShellUtil;

public class ShellScriptAction extends AbstractAction {
    private static final String TAG = "ShellScriptAction";

    public ShellScriptAction(StatusChangedListener listener) {
        super(listener);
    }

    @Override
    public void start(String param) {
        Log.d(TAG, TAG + " start param=" + param);
        String[] params = SLTUtil.parseParam(param);
        if (params != null) {
            if(TextUtils.isEmpty(params[0]) || !SLTUtil.fileIsExists(params[0])){
                error("file is not exists!");
                return;
            }
            final StringBuffer buffer = new StringBuffer();
            buffer.append("sh ");
            for (String cmd : params) {
                buffer.append(cmd + " ");
            }
            Log.d(TAG, TAG + " start shell cmd=" + buffer.toString());
            new Thread(new Runnable() {
                public void run() {
                    String ret = ShellUtil.execShellStr(buffer.toString());
                    Log.d(TAG, TAG + " shell ret=" + ret);
                }
            }).start();
            ok();
        }else{
            end("params is NULL!");
        }
    }

    @Override
    public void stop() {
        end();
    }
}
