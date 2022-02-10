package com.sprd.autoslt.util;

import android.os.Build;
import android.os.SystemProperties;

public class PropUtils {
	public static final String YLOG_SOCKET_NAME_HIDL = "ylog_cli_cmd";

	public static void setProp(String prop, String value) {
		SystemProperties.set(prop, value);
	}

	public static String getProp(String prop) {
		String string = SystemProperties.get(prop);
		return string;

	}

	public static boolean getBoolean(String prop, boolean defualt) {
		int len = -1;
		boolean result = defualt;
		String value = getProp(prop);
		if (value != null) {
			value = value.trim();
			len = value.length();
			if (len == 1) {
				if (value.equals("0") || value.equals("n"))
					result = false;
				else if (value.equals("1") || value.equals("y"))
					result = true;
			} else if (len > 1) {
				if (value.equals("no") || value.equals("false")
						|| value.equals("off")) {
					result = false;
				} else if (value.equals("yes") || value.equals("true")
						|| value.equals("on")) {
					result = true;
				}
			}
		}
		return result;
	}

	public static String getString(String prop, String defualt) {
		String result = defualt;
		String value = getProp(prop);
		if (value != null) {
			value = value.trim();
			if (value.length() >= 1) {
				result = value;
			}
		}
		return result;
	}

	public static int getInt(String prop, int defualt) {
		int result = defualt;
		String value = getProp(prop);
		if (value != null) {
			value = value.trim();
			if (value.length() > 0) {
				try {
					Integer.parseInt(value);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					result = defualt;
				}

			}
		}
		return result;
	}
}
