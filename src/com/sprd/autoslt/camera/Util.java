package com.sprd.autoslt.camera;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.Camera;

import com.sprd.autoslt.R;

public class Util {

    public static Tuple<Integer, Integer> getOptimalSize(int sWidth,
            int sHeight, int width, int height, boolean screen) {
        width = Math.max(width, height);
        height = Math.min(width, height);
        double ratio = (1 / (((double) width) / ((double) height)));
        return getOptimalSize(sWidth, sHeight, ratio, screen);
    }

    public static Tuple<Integer, Integer> getOptimalSize(int screenWidth,
            int screenHeight, double ratio, boolean screen) {
        Tuple<Integer, Integer> result = new Tuple<Integer, Integer>(
                screenWidth, screenHeight);
        int max = -1, min = -1, width = -1, height = -1;
        if (ratio > 1D)
            ratio = (1 / ratio);

        if (screen) {
            max = Math.max(screenWidth, screenHeight);
            min = ((int) (max * ratio));
            if (screenWidth < screenHeight) {
                width = min;
                height = max;
            } else {
                height = min;
                width = max;
            }
        } else {
            min = Math.min(screenWidth, screenHeight);
            max = ((int) (min / ratio));
            if (screenWidth > screenHeight) {
                width = max;
                height = min;
            } else {
                width = min;
                height = max;
            }
        }
        if (screenWidth != width || screenHeight != height) {
            result = new Tuple<Integer, Integer>(width, height);
        }
        return result;
    }

    /** Return a properly configured SharedPreferences instance */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("sprd_auto_slt",
                Context.MODE_PRIVATE);
    }

    /**
     * Asynchronously sets the preference with the given key to the given value
     * 
     * @param context
     *            the context to use to get preferences from
     * @param key
     *            the key of the preference to set
     * @param value
     *            the value to set
     */
    public static void setSharedPreference(Context context, String key,
            String value) {
        SharedPreferences prefs = getSharedPreferences(context);
        prefs.edit().putString(key, value).apply();
    }

    public static String getSharedPreference(Context context, String key,
            String defaultValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static int getSharedPreference(Context context, String key,
            int defaultValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(key, defaultValue);
    }

    public static void setSharedPreference(Context context, String key,
            int value) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

	public static int bestVideoSize(List<Camera.Size> videoSizeList,int _w){
        Collections.sort(videoSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.width > rhs.width) {
                    return -1;
                } else if (lhs.width == rhs.width) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for(int i=0;i<videoSizeList.size();i++){
            if(videoSizeList.get(i).width<_w){
                return i;
            }
        }
        return 0;
    }
}
