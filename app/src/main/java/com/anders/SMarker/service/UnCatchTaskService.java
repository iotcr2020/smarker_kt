package com.anders.SMarker.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AppVariables;

public class UnCatchTaskService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) { //핸들링 하는 부분
        Log.e("-----------******^^^^^^------------->","onTaskRemoved - " + rootIntent);

        // 핸들링 해 적용할 내용
        sendToServerExit();
        stopSelf(); //서비스도 같이 종료
    }

    private void sendToServerExit(){
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