package com.sprd.autoslt.action;

public interface IAction{

    void start(String param);

    void ok();

    void end();

    void end(String result);
	
	void end(String cmdString ,String result);

    void stop();

    void error(String errorMessage);
}
