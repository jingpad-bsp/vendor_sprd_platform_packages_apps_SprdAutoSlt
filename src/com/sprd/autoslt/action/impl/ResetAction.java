package com.sprd.autoslt.action.impl;

import android.content.Intent;

import com.sprd.autoslt.SLTActivity;
import com.sprd.autoslt.SLTApplication;
import com.sprd.autoslt.action.AbstractAction;

public class ResetAction extends AbstractAction {

        private static final String TAG = "ResetAction";
        private static ResetAction instance;
        public ResetAction(StatusChangedListener listener) {
               super(listener);
               // TODO Auto-generated constructor stub
        }

        public static ResetAction getInstance(StatusChangedListener listener){
            if (instance == null) {
            instance = new ResetAction(listener);
        }
        return instance;
        }
       
        @Override
        public void start(String param) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                intent.setPackage("android");
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
                intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
                SLTApplication.getApplication().getApplicationContext().sendBroadcast(intent);
                ok();
        }

        @Override
        public void stop() {
           // TODO Auto-generated method stub

        }


}
