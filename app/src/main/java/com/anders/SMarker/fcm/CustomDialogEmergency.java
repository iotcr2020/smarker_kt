package com.anders.SMarker.fcm;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.anders.SMarker.R;
import com.anders.SMarker.SplashActivity;
import com.anders.SMarker.service.BleService;
import com.anders.SMarker.utils.AppVariables;

import java.util.List;

public class CustomDialogEmergency extends AppCompatActivity {

    TextView txtContent;
    Button btnAlarmOk ;
    Context mContext;
    String message = "";

    private SoundPool soundPoolEmer, soundPoolNormal;
    private int emerAlarm ,normalAlarm;
    private Dialog dialog;
    private int soundEmerId,soundNormalId = -1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_emergency_receive);

        soundPoolEmer = AppVariables.soundPoolEmer;
        soundPoolNormal = AppVariables.soundPoolNormal;
        emerAlarm = AppVariables.emerAlarm;
        normalAlarm = AppVariables.normalAlarm;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        Intent intent = getIntent();

      //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String message = intent.getExtras().getString("message");
        String location = intent.getExtras().getString("location");
        String gubn = intent.getExtras().getString("gubn");
        final boolean background = intent.getExtras().getBoolean("background");
        final String sound = intent.getExtras().getString("sound"); //fcm일때는 소리 알림(1), 메세지 관리 클릭시에는 소리 끔(0)

        final TextView txtContent = (TextView)findViewById(R.id.txtContent);
        final Button btnAlarmOk = (Button)findViewById(R.id.btnAlarmOk);
        txtContent.setText(message);


        soundEmerId =  soundPoolEmer.play(emerAlarm, 1, 1, 0, 0, 1);

        btnAlarmOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(soundPoolEmer !=null) {
                    soundPoolEmer.stop(soundEmerId);
                }

                finish();
                if(background){
                    ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> task_info = activityManager.getRunningTasks(9999);
                    if (task_info == null || task_info.size() <= 1)
                        startActivity(new Intent(CustomDialogEmergency.this, SplashActivity.class));
                }

            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(soundPoolEmer !=null) {
            soundPoolEmer.stop(soundEmerId);
        }
        if(soundPoolNormal !=null) {
            soundPoolNormal.stop(soundNormalId);
        }
    }




}



