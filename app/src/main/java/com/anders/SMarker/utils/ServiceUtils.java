package com.anders.SMarker.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

public class ServiceUtils {

    /**
     * 특정 서비스가 현재 구동중인지 여부 확인
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isServiceRunningCheck(Context context, String packageName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (packageName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
