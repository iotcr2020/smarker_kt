package com.anders.SMarker.fcm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.anders.SMarker.ConnectActivity;
import com.anders.SMarker.R;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AppVariables;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pchmn.materialchips.R2;

import java.util.List;
import java.util.Map;

public class FireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static String user_hp;
    private final  int PERMISSIONS_REQUEST_READ_PHONE_STATE  = 0;

    SharedPreferences auto;
    SharedPreferences.Editor editor ;


    PowerManager powerManager;
    private static PowerManager.WakeLock wakeLock;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @SuppressLint({"InvalidWakeLockTag", "WrongThread"})
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //메세지 수신 시 실행되는 메소드
        // TODO(developer): Handle FCM messages here.

        if(AppVariables.soundPoolEmer == null || AppVariables.soundPoolNormal == null){
            AppVariables.notifySound(getApplicationContext());//알림 소리 load
        }
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKELOCK");

        wakeLock.acquire(); // WakeLock 깨우기
        if (remoteMessage.getData().size() > 0) {
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }

        Map<String, String> data = remoteMessage.getData();
        String send_idx="";
        String loc_x="";
        String loc_y="";
        String gubn="";
        String title="";
        String message="";
        String location="";
        if(data.get("send_idx")!= null){
            gubn = data.get("gubn");
            title = data.get("title");
            message = data.get("content");
            location = data.get("location");

        }

        sendNotification(title,message,location,gubn);
        releaseWakeLock();
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        /*
         * 기존의 FirebaseInstanceIdService에서 수행하던 토큰 생성, 갱신 등의 역할은 이제부터
         * FirebaseMessaging에 새롭게 추가된 위 메소드를 사용하면 된다.
         */
        auto = getSharedPreferences("tokenSetting", Activity.MODE_PRIVATE);
        editor = auto.edit();
        String refreshedToken = s;
        if(refreshedToken !=null){
            AppVariables.User_Hp_Token = refreshedToken;
            editor.putString("tcmToken", refreshedToken);
            editor.commit();
        }
        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.

        if(AppVariables.User_Phone_Number.length() > 0 ) {
            sendRegistrationToServer(refreshedToken);
        }
    }

    public static void sendRegistrationToServer(String token) {

        user_hp = AppVariables.User_Phone_Number;
        if(user_hp.length() > 0 && token !=null){

            ContentValues addData = new ContentValues();
            addData.put("user_hp_token",token);
            addData.put("user_hp",user_hp);
            NetworkTask networkTask = new NetworkTask(NetworkTask.API_FCM_HPSET, addData);

            try {
                networkTask.execute().get();
            } catch (Exception e) {

                e.printStackTrace();

            }
        }

    }

    public static void releaseWakeLock(){
        if(wakeLock != null){
            wakeLock.release();
            wakeLock = null;
        }

    }

    public void isServiceRunningCheck() {
        String strPackage = "";
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> proceses = am.getRunningAppProcesses();

//프로세서 전체를 반복
        for(ActivityManager.RunningAppProcessInfo process : proceses)
        {
            if(process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                strPackage = process.processName; //package이름과 동일함.
                Log.d("TEST", strPackage);
            }
        }

    }


    // [END receive_message]

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    /**
     * 메시지가 수신되었을 때 실행되는 메소드
     * **/
    private void sendNotification(String title, String message,String location,String gubn) {


        Intent intent = new Intent(this, RestartService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelName = "NOTIFICATION";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        }
        //notificationManager.notify(0, notificationBuilder.build());

        Intent intent2 = new Intent(getApplicationContext(), RestartService.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent2.putExtra("message",message);
        intent2.putExtra("location",location);
        intent2.putExtra("gubn",gubn);
        intent2.putExtra("sound","1"); //fcm일때는 소리 알림(1), 메세지 관리 클릭시에는 소리 끔(0)

        PendingIntent mAlarmSender = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager amgr = (AlarmManager) getApplicationContext()
                .getSystemService(getApplicationContext().ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            amgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() , mAlarmSender);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            amgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() , mAlarmSender);
        } else {
            amgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() , mAlarmSender);
        }

    }





}

