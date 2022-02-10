package com.sprd.autoslt.action.impl;

//import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.util.PhaseCheckParse;

public class GetPhaseInfoAction extends AbstractAction {

    private static final String TAG = "GetPhaseInfoAction";
    private static GetPhaseInfoAction instance;
    private PhaseCheckParse mCheckParse;

    private GetPhaseInfoAction(StatusChangedListener listener) {
        super(listener);
        mCheckParse = new PhaseCheckParse();
    }

    public static GetPhaseInfoAction getInstance(StatusChangedListener listener) {
        if (instance == null) {
            instance = new GetPhaseInfoAction(listener);
        }
        return instance;
    }

    @Override
    public void start(String param) {
 //   String reString = PhaseCheckParse.getInstance().getPhaseCheckResult("ANTENNA");
 //   Log.d(TAG, "reString = " +reString);
     //   end(PhaseCheckParse.getInstance().getPhaseCheck());
       if (mCheckParse == null) {
            mCheckParse = new PhaseCheckParse();
        }
        String pcResultString = mCheckParse.getPhaseCheck();
    if (pcResultString == null ||pcResultString.equals("")) {
         error("read phasecheck error");
      }else {
          end(pcResultString);
      }
      //end(mCheckParse.getPhaseCheck());
    }

    @Override
    public void stop() {
    }

}
