package com.sprd.autoslt.action.impl;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.activity.MutiTouchTestActivity;
import com.sprd.autoslt.activity.TpPatternDiagActivity;
import com.sprd.autoslt.activity.TpPatternSquareActivity;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.SLTUtil;
import com.sprd.autoslt.util.TestResultUtil;

public class TPPatternAction extends AbstractAction {
	private static final String TAG = "TPPatternAction";

	private String mType;

	public TPPatternAction(StatusChangedListener listener, String type) {
		super(listener);
		mType = type;
	}

	@Override
	public void start(String param) {
		Log.d(TAG, "TPPatternAction:" + param + "mType:" + mType);
		if (mType.equals(SLTConstant.ACTION_TYPE_START_TP_PATTERN)) {
			String[] actionName = SLTUtil.parseParam(param);
			if (actionName.length == 1) {
				Log.d(TAG, "start param " + actionName[0].equals("diag"));
				if (actionName[0].equals("diag")) {
					if (SLTUtil
							.getTopActivity(mContext)
							.toString()
							.contains(
									"com.sprd.autoslt.activity.TpPatternDiagActivity")) {
						// error("status error");
						ok();
						return;
					}
					Log.d(TAG, "start intent");
					Intent intent = new Intent(mContext,
							TpPatternDiagActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
					new Thread(new Runnable() {
						@Override
						public void run() {
                            int at = 0;
							while (true) {
								Log.d(TAG, "TPPatternAction Thread.sleep 200ms");
								try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								Log.d(TAG, "getTopActivity = "+ SLTUtil.getTopActivity(mContext).toString());
								if (SLTUtil
										.getTopActivity(mContext)
										.toString()
										.contains(
												"com.sprd.autoslt.activity.TpPatternDiagActivity")) {
									ok();
									break;
								}
                                at++;
                                if (at >= 10 &&!SLTUtil
                                .getTopActivity(mContext)
                                .toString().contains("com.sprd.autoslt.activity.TpPatternDiagActivity")) {
                                    end("fail");
                                    break;
                               }
                            }
                        }
					}).start();
				} else if (actionName[0].equals("square")) {
					if (SLTUtil
							.getTopActivity(mContext)
							.toString()
							.contains(
									"com.sprd.autoslt.activity.TpPatternSquareActivity")) {
						// error("status error");
						ok();
						return;
					}
					Intent intent = new Intent(mContext,
							TpPatternSquareActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
					new Thread(new Runnable() {
						@Override
						public void run() {
                            int bt = 0;
							while (true) {
								Log.d(TAG, "TPPatternAction Thread.sleep 200ms");
								try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								Log.d(TAG, "getTopActivity = "+ SLTUtil.getTopActivity(mContext).toString());
								if (SLTUtil
										.getTopActivity(mContext)
										.toString()
										.contains(
												"com.sprd.autoslt.activity.TpPatternSquareActivity")) {
									ok();
									break;
								}
                                bt++;
                                if (bt >= 10 &&!SLTUtil.getTopActivity(mContext).toString()
                                    .contains("com.sprd.autoslt.activity.TpPatternSquareActivity")) {
                                        end("fail");
                                        break;
                                }
                            }
						}
					}).start();
				}
			} else if (actionName.length == 2) {
				if (actionName[0].equals("two")) {
					if (SLTUtil
							.getTopActivity(mContext)
							.toString()
							.contains(
									"com.sprd.autoslt.activity.MutiTouchTestActivity")) {
						// error("status error");
						ok();
						return;
					}
					Intent intent = new Intent(mContext,
							MutiTouchTestActivity.class);
					Bundle argBundle = new Bundle();
					argBundle.putString("distance", actionName[1]);
					intent.putExtra("bundle", argBundle);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
					new Thread(new Runnable() {
						@Override
						public void run() {
                            int ct = 0;
							while (true) {
								Log.d(TAG, "TPPatternAction Thread.sleep 200ms");
								try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								Log.d(TAG, "getTopActivity = "+ SLTUtil.getTopActivity(mContext).toString());
								if (SLTUtil
										.getTopActivity(mContext)
										.toString()
										.contains(
												"com.sprd.autoslt.activity.MutiTouchTestActivity")) {
									ok();
									break;
								}
                                ct++;
                                if (ct >= 10 &&!SLTUtil.getTopActivity(mContext).toString()
                                    .contains("com.sprd.autoslt.activity.MutiTouchTestActivity")) {
                                        end("fail");
                                        break;
                                }
                            }
						}
					}).start();
				}
			} else {
				error("status error");
			}
		} else if (mType.equals(SLTConstant.ACTION_TYPE_END_TP_PATTERN)) {
			Log.d(TAG, "EndTPPattern----gettopacticity:"
					+ SLTUtil.getTopActivity(mContext).toString());
			if (SLTUtil
					.getTopActivity(mContext)
					.toString()
					.contains(
							"com.sprd.autoslt.activity.TpPatternSquareActivity")) {
				TpPatternSquareActivity.instance.finish();
				ok();
			} else if (SLTUtil
					.getTopActivity(mContext)
					.toString()
					.contains("com.sprd.autoslt.activity.TpPatternDiagActivity")) {
				TpPatternDiagActivity.instance.finish();
				ok();
			} else if (SLTUtil
					.getTopActivity(mContext)
					.toString()
					.contains("com.sprd.autoslt.activity.MutiTouchTestActivity")) {
				MutiTouchTestActivity.instance.finish();
				ok();
			}else if (!SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.TpPatternSquareActivity")
					&& !SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.TpPatternDiagActivity")
					&& !SLTUtil.getTopActivity(mContext).toString().contains("com.sprd.autoslt.activity.MutiTouchTestActivity")) {
				ok();
			}
		} else if (mType.equals(SLTConstant.ACTION_TYPE_GET_TP_PATTERN_RESULT)) {
			Log.d(TAG, "GetTPPatternResult----gettopacticity:"
					+ SLTUtil.getTopActivity(mContext).toString());
			if (SLTUtil
					.getTopActivity(mContext)
					.toString()
					.contains(
							"com.sprd.autoslt.activity.TpPatternSquareActivity")) {
				// TpPatternSquareActivity.instance.finish();
				if (TextUtils.isEmpty(TestResultUtil.getInstance()
						.getCurrentStepStatus())
						|| TestResultUtil.getInstance().getCurrentStepStatus()
								.equals("fail")) {
					end("fail");
				} else {
					end("pass");
				}
			} else if (SLTUtil
					.getTopActivity(mContext)
					.toString()
					.contains("com.sprd.autoslt.activity.TpPatternDiagActivity")) {
				// TpPatternDiagActivity.instance.finish();
				if (TextUtils.isEmpty(TestResultUtil.getInstance()
						.getCurrentStepStatus())
						|| TestResultUtil.getInstance().getCurrentStepStatus()
								.equals("fail")) {
					end("fail");
				} else {
					end("pass");
				}
			} else if (SLTUtil
					.getTopActivity(mContext)
					.toString()
					.contains("com.sprd.autoslt.activity.MutiTouchTestActivity")) {
				// TpPatternDiagActivity.instance.finish();
				if (TextUtils.isEmpty(TestResultUtil.getInstance()
						.getCurrentStepStatus())
						|| TestResultUtil.getInstance().getCurrentStepStatus()
								.equals("fail")) {
					end("fail");
				} else {
					end("pass");
				}
			} else {
				error("status error");
			}

		}/*
		 * else if(mType.equals(SLTConstant.ACTION_TYPE_END_TP_PATTERN_RESULT))
		 * { Log.d(TAG, "gettopacticity:" +
		 * SLTUtil.getTopActivity(mContext).toString()); if
		 * (SLTUtil.getTopActivity(mContext).toString().contains(
		 * "com.sprd.autoslt.activity.TpPatternSquareActivity")) {
		 * TpPatternSquareActivity.instance.finish();
		 * if(TextUtils.isEmpty(TestResultUtil
		 * .getInstance().getCurrentStepStatus()) ||
		 * TestResultUtil.getInstance().getCurrentStepStatus().equals("fail")) {
		 * end("fail"); } else { end("pass"); } } else if
		 * (SLTUtil.getTopActivity(mContext).toString().contains(
		 * "com.sprd.autoslt.activity.TpPatternDiagActivity")) {
		 * TpPatternDiagActivity.instance.finish();
		 * if(TextUtils.isEmpty(TestResultUtil
		 * .getInstance().getCurrentStepStatus()) ||
		 * TestResultUtil.getInstance().getCurrentStepStatus().equals("fail")) {
		 * end("fail"); } else { end("pass"); } } else { error("status error");
		 * } }
		 */
	}

	@Override
	public void stop() {
	}
}