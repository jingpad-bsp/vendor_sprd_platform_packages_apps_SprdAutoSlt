
package com.sprd.autoslt.util;

import android.content.Context;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Display;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.os.SystemProperties;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManager.LayoutParams;

public class NavigationUtil {

    public static void hideNavigationBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            decorView.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // for new api versions. View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static int getHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }

    public static int getRealHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else {
            display.getMetrics(dm);
        }
        int realHeight = dm.heightPixels;
        return realHeight;
    }

    public static boolean getNavigationBarShowState(Context context) {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(context.getDisplayId());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}