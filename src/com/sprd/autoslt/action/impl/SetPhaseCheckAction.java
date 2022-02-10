package com.sprd.autoslt.action.impl;

import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.action.AbstractAction.StatusChangedListener;
import com.sprd.autoslt.util.PhaseCheckParse;
import com.sprd.autoslt.util.SLTUtil;

public class SetPhaseCheckAction extends AbstractAction {
        private static final String TAG = "SetPhaseCheckAction";
        private static SetPhaseCheckAction instance;
        private PhaseCheckParse checkParse = null;

        public SetPhaseCheckAction(StatusChangedListener listener) {
                super(listener);
                // TODO Auto-generated constructor stub
                checkParse = new PhaseCheckParse();
        }

        public static SetPhaseCheckAction getInstance(StatusChangedListener listener) {
                if (instance == null) {
                       instance = new SetPhaseCheckAction(listener);
                }
               return instance;
        }
//PASS/FAIL/UnTested
        @Override
        public void start(String param) {
                // TODO Auto-generated method stub
                Log.d(TAG, "SetPhaseCheckAction: param" + param);
                String[] params = SLTUtil.parseParam(param);
                String stationName;
                String stationResult;
                if (params.length == 2) {
                        stationName = params[0];
                        stationResult = params[1];
                        if (checkParse.isStationExsit(stationName)) {
                                if (stationResult.equalsIgnoreCase("pass")||stationResult.equalsIgnoreCase("fail")) {
                                        checkParse.writeStationTested(stationName);
                                        if (stationResult.equalsIgnoreCase("pass")) {
                                              checkParse.writeStationPass(stationName);
                                        }else if (stationResult.equalsIgnoreCase("fail")) {
                                              checkParse.writeStationFail(stationName);
                                        }else {
                                              error("invalid param");
                                        }
                                }
                                ok();
                        }else {
                              error("invalid station");
                        }
                }else {
                       error("invalid param");
                       return;
                }
        }

        @Override
        public void stop() {
          // TODO Auto-generated method stub
        }

}
