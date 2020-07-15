package com.anders.SMarker.service;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AppVariables;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
            sendToServerExit();
        }
    }

    private void sendToServerExit() {
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("USER_IDX", Integer.parseInt(AppVariables.User_Idx));
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_LOG_OFF, addData);

        try {
            String result = networkTask.execute().get();

            if (result != null && !result.isEmpty()) {
                resultBuilder = result.split("\\|");

                if (resultBuilder[0].equals("Y")) {
                    Log.i("UPDATE","Log Off");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}