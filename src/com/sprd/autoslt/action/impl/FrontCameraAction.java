
package com.sprd.autoslt.action.impl;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.camera.CameraTestActivity;

public class FrontCameraAction extends AbstractAction {

    private static final String TAG = "SLTCameraAction";
    private Context sContext;

    public FrontCameraAction(StatusChangedListener listener, Context context) {
        super(listener);
        sContext = context;
    }

    @Override
    public void start(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "start() failed : file name is empty!");
            error("CameraAction start() failed : file name is empty!");
            return;
        }

        if (!CameraTestActivity.mIsActivityInFront) {
            Intent intent = new Intent(sContext, CameraTestActivity.class);
            intent.putExtra("front", true);
            intent.putExtra("fileName", fileName);
            intent.putExtra("mIsAuto", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sContext.startActivity(intent);
            ok();
        } else {
            Log.d(TAG, "CameraTestActivity already in the foreground");
        }
    }

    @Override
    public void stop() {
        end();
    }

}
