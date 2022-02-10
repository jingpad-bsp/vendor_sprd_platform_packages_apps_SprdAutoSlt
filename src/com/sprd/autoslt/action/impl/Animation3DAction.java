package com.sprd.autoslt.action.impl;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.Tester3dCube;
import com.sprd.autoslt.activity.VideoTestActivity;
import com.sprd.autoslt.common.SLTConstant;

public class Animation3DAction extends AbstractAction {

    private static final String TAG = "Animation3DAction";
    private static Animation3DAction instance;
    private static String mType;

    public Animation3DAction(StatusChangedListener listener) {
        super(listener);
    }

    private Animation3DAction(StatusChangedListener listener, String type) {
        super(listener);
        mType = type;
    }

    public static Animation3DAction getInstance(StatusChangedListener listener, String type) {
        mType = type;
        if(instance == null) {
            instance = new Animation3DAction(listener, type);
        }
        return instance;
    }

    @Override
    public void start(String fileName) {
        Log.d(TAG, "Animation3DAction start");
        if (SLTConstant.ACTION_TYPE_START_3D_ANIMATION.equals(mType)) {
            if (Tester3dCube.instance.is3DRuning/*is3DPlaying()*/) {
                error("already running..");
                return;
            }
            Intent intent = new Intent(mContext, Tester3dCube.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            ok();
        } else if (SLTConstant.ACTION_TYPE_END_3D_ANIMATION.equals(mType)) {
            Tester3dCube.instance.finish();
			Tester3dCube.instance.setIs3DRuning(false);
            ok();
        } else {
            error("invalid cmd!");
        }
    }

    @Override
    public void stop() {
    }
}
