package com.anders.SMarker.fcm;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.anders.SMarker.R;

import java.util.List;


public class RestartService extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context ct, Intent arg1) {
        context= ct;
        final boolean foreground =  isAppIsInBackground(context);
        Log.d("foreeee=",foreground+"");


      //  ct.startActivity(alarmIntent);
        String message = arg1.getExtras().getString("message");
        String location = arg1.getExtras().getString("location");
        String gubn = arg1.getExtras().getString("gubn");
        String sound = arg1.getExtras().getString("sound");

        if(gubn.equals(context.getResources().getString(R.string.dialogAlaram3)) || gubn.equals("장비")) {

            Intent popupIntent = new Intent(context, CustomDialogTeam.class);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            popupIntent.putExtra("message", message);
            popupIntent.putExtra("location", location);
            popupIntent.putExtra("gubn", gubn);
            popupIntent.putExtra("sound", sound);
            popupIntent.putExtra("background", foreground);

            context.startActivity(popupIntent);
        }else if(gubn.equals(context.getResources().getString(R.string.dialogAlaram1))){
            Intent popupIntent = new Intent(context, CustomDialogNormal.class);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            popupIntent.putExtra("message", message);
            popupIntent.putExtra("location", location);
            popupIntent.putExtra("gubn", gubn);
            popupIntent.putExtra("sound", sound);
            popupIntent.putExtra("background", foreground);

            context.startActivity(popupIntent);

        }else if(gubn.equals(context.getResources().getString(R.string.dialogAlaram2))){
            Intent popupIntent = new Intent(context, CustomDialogEmergency.class);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            popupIntent.putExtra("message", message);
            popupIntent.putExtra("location", location);
            popupIntent.putExtra("gubn", gubn);
            popupIntent.putExtra("sound", sound);
            popupIntent.putExtra("background", foreground);

            context.startActivity(popupIntent);
        }

    }
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}