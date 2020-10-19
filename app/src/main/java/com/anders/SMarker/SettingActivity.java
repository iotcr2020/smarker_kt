package com.anders.SMarker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.service.BleService;
import com.anders.SMarker.utils.AESEncryptor;
import com.anders.SMarker.utils.AlarmDlg;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.Tools;

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private Switch SwitchWork;
    TextView str_time_val,str_battery_val,str_sensing_val, str_volume_val, str_acl_val;;
    TextView helmet_time_val,helmet_battery_val,helmet_acl_val;
    TextView etc_time_val,etc_receiver_val;
    TextView appversion;

    int str_time_set, str_battery_set, str_sensing_set, str_acl_set = 0;
    int helmet_time_set,helmet_battery_set = 0;
    int helmet_acl_set = 4;
    int str_volume_set = 2;
    int etc_time_set, etc_receiver_set = 0;

    SharedPreferences auto;
    SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        AppVariables.activitySet = SettingActivity.this;
        initToolbar();

        SwitchWork = findViewById(R.id.SwitchWork);

        str_time_val = (TextView)findViewById(R.id.str_time_val);
        str_battery_val = (TextView)findViewById(R.id.str_battery_val);
        str_sensing_val = (TextView)findViewById(R.id.str_sensing_val);
        str_volume_val = (TextView)findViewById(R.id.str_volume_val);
        str_acl_val = (TextView)findViewById(R.id.str_acl_val);
        helmet_time_val = (TextView)findViewById(R.id.helmet_id_val);
        helmet_battery_val = (TextView)findViewById(R.id.strip_id_val);
        helmet_acl_val  = (TextView)findViewById(R.id.helmet_acl_val);
        etc_time_val = (TextView)findViewById(R.id.etc_time_val);
        etc_receiver_val = (TextView)findViewById(R.id.etc_receiver_val);
        appversion = (TextView)findViewById(R.id.appversion);

        auto = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        getWorkFl();
        editor = auto.edit();
        appversion.setText("Version " + getVersionInfo(this));
        sharedPreferences();    // 설정 값 가져오기
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked ) {
        String workFl = "Y";
        switch( buttonView.getId() ) {
            case R.id.SwitchWork :
                if (isChecked) workFl = "Y";
                else workFl = "N";
                sendWorkFl(workFl);
                break;
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("환경설정");
        toolbar.setTitleTextColor(getResources().getColor(R.color.mainColor));
        toolbar.setTitleTextAppearance(getApplicationContext(),R.style.TextAppearance_Subhead_Bold);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this,R.color.grey_60);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey_60));

        MenuItem item = menu.findItem(R.id.btnEmergency);
        Drawable icon = getResources().getDrawable(R.drawable.emergency);
        icon.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item.setIcon(icon);

        MenuItem item_set = menu.findItem(R.id.action_settings);
        Drawable icon_set = getResources().getDrawable(R.drawable.popup_setting);
        icon_set.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item_set.setIcon(icon_set);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId()== R.id.btnEmergency){
            AlarmDlg.showAlarmDialog(this,"긴급"); //권한 허용 시 비상 알림 띄우기

        }else if (item.getItemId()== R.id.action_settings){
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    private void sendWorkFl(String Work_fl) {
        ContentValues addData = new ContentValues();
        // 이 부분 핸드폰번호 장치에서 가져온 것으로 수정 hwang
        String phone = "";
        AESEncryptor aESEncryptor = null;
        try {
            aESEncryptor = new AESEncryptor();
            phone = aESEncryptor.encrypt(AppVariables.User_Phone_Number);
        } catch (Exception e){}
        addData.put("phoneNB", phone);
        addData.put("workFl", Work_fl);
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_WORK_FL, addData);

        try {
            networkTask.execute().get();
        } catch (Exception e) {
        }
    }

    private void getWorkFl() {
        ContentValues addData = new ContentValues();
        // 이 부분 핸드폰번호 장치에서 가져온 것으로 수정 hwang
        String phone = "";
        AESEncryptor aESEncryptor = null;
        try {
            aESEncryptor = new AESEncryptor();
            phone = aESEncryptor.encrypt(AppVariables.User_Phone_Number);
        } catch (Exception e){}
        addData.put("phoneNB", phone);
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_GET_WORK_FL, addData);

        try {
            String result = networkTask.execute().get();
            if (result != null && !result.isEmpty()) {
                if ("Y".equals(result)) SwitchWork.setChecked(true);
                else SwitchWork.setChecked(false);
                SwitchWork.setOnCheckedChangeListener( this );
            }
        } catch (Exception e) {
        }
    }

    public String getVersionInfo(Context context){
        String version = null;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = i.versionName;
        } catch(PackageManager.NameNotFoundException e) { }
        return version;
    }

    // 설정 값 가져오기
    private void sharedPreferences() {
        str_time_set = auto.getInt("str_time",str_time_set);
        str_battery_set= auto.getInt("str_battery",str_battery_set);
        str_sensing_set = auto.getInt("str_sensing",str_sensing_set);
        helmet_time_set = auto.getInt("helmet_time",helmet_time_set);
        helmet_battery_set = auto.getInt("helmet_battery",helmet_battery_set);
        etc_time_set = auto.getInt("etc_time",etc_time_set);
        etc_receiver_set = auto.getInt("etc_receiver",etc_receiver_set);
        helmet_acl_set = auto.getInt("helmet_acl",helmet_acl_set);
        str_acl_set = auto.getInt("str_acl",str_acl_set);
        str_volume_set = auto.getInt("str_volume",str_volume_set);

        if(auto != null){
            str_time_val.setText(getResources().getStringArray(R.array.setting_str_time)[str_time_set]);
            str_battery_val.setText(getResources().getStringArray(R.array.setting_str_battery)[str_battery_set]);
            str_sensing_val.setText(getResources().getStringArray(R.array.setting_str_sensing)[str_sensing_set]);
            str_volume_val.setText(getResources().getStringArray(R.array.setting_volume)[str_volume_set]);
            str_acl_val.setText(getResources().getStringArray(R.array.setting_helmet_acl)[str_acl_set]);

            helmet_time_val.setText(getResources().getStringArray(R.array.setting_helmet_time)[helmet_time_set]);
            helmet_battery_val.setText(getResources().getStringArray(R.array.setting_helmet_battery)[helmet_battery_set]);
            helmet_acl_val.setText(getResources().getStringArray(R.array.setting_helmet_acl)[helmet_acl_set]);

            etc_time_val.setText(getResources().getStringArray(R.array.setting_etc_time)[etc_time_set]);
            etc_receiver_val.setText(getResources().getStringArray(R.array.setting_etc_receiver)[etc_receiver_set]);
        }
    }

    public void clickSetting(View view) {   // 메뉴 클릭 시 환경설정
        sharedPreferences();
        int id = view.getId();
        switch (id) {
            case R.id.str_time://턱끈-긴급발신시간
                showStrTimeChoiceDialog();
                break;
            case R.id.str_battery://턱끈-배터리 충전 알림
                showStrBatteryChoiceDialog();
                break;
            case R.id.str_sensing://턱끈-턱끈착용감지
                showStrSensingChoiceDialog();
                break;
            case R.id.str_volume://턱끈-민감도 설정
                showStrVolumeChoiceDialog();
                break;
            case R.id.str_acl://턱끈-민감도 설정
                showStrAclChoiceDialog();
                break;
            case R.id.helmet_time://안전모-긴급발신시간
                showSingleChoiceDialog();
                break;
            case R.id.helmet_battery://안전모-배터리충전알림
                showBatteryChoiceDialog();
                break;
            case R.id.helmet_acl://안전모-가속센서반응정도
                showAclChoiceDialog();
                break;

            case R.id.etc_time://기타-긴급 발신 지연 시간
                showEtcTimeChoiceDialog();
                break;
            case R.id.etc_receiver://기타-긴급수호수신자
                showEtcReceiverChoiceDialog();
                break;
        }
    }

    //턱끈 - 긴급발신시간 설정
    private String str_time_choice_selected;
    private void showStrTimeChoiceDialog() {
        str_time_choice_selected = getResources().getStringArray(R.array.setting_str_time)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("긴급 발신 시간");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_str_time, str_time_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                str_time_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_str_time)[i]);
                str_time_val.setText(str_time_choice_selected);
                editor.putInt("str_time",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                sendConfigToStrip();

                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void sendConfigToStrip()
    {
        if (BleService.isService) {
            byte[] msg = new byte[1];
            if( str_time_val.getText().equals("3초")){
                msg[0] = 0x20;
            }else if( str_time_val.getText().equals("4초")){
                msg[0] = 0x30;
            }else if( str_time_val.getText().equals("5초")){
                msg[0] = 0x40;
            }

            BleService.instance.sendConfigStrip(msg);
        }
    }

    //턱끈 - 배터리충전알림 설정
    private String str_battery_choice_selected;
    private void showStrBatteryChoiceDialog() {
        str_battery_choice_selected = getResources().getStringArray(R.array.setting_str_battery)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("배터리 충전 알림");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_str_battery, str_battery_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                str_battery_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_str_battery)[i]);
                str_battery_val.setText(str_battery_choice_selected);
                editor.putInt("str_battery",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //턱끈 - 턱끈착용감지 설정
    private String str_sensing_choice_selected;
    private void showStrSensingChoiceDialog() {
        str_sensing_choice_selected = getResources().getStringArray(R.array.setting_str_sensing)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("턱끈 착용 감지");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_str_sensing, str_sensing_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                str_sensing_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_str_sensing)[i]);
                str_sensing_val.setText(str_sensing_choice_selected);
                editor.putInt("str_sensing",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //헬맷 배터리 충전알림 설정
    private String helmet_battery_choice_selected;
    private void showBatteryChoiceDialog() {
        helmet_battery_choice_selected = getResources().getStringArray(R.array.setting_helmet_battery)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("배터리 충전 알림");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_helmet_battery, helmet_battery_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                helmet_battery_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_helmet_battery)[i]);
                helmet_battery_val.setText(helmet_battery_choice_selected);
                editor.putInt("helmet_battery",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //헬맷 긴급발신시간 설정
    private String single_choice_selected;
    private void showSingleChoiceDialog() {
        single_choice_selected = getResources().getStringArray(R.array.setting_helmet_time)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("긴급 발신 시간");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_helmet_time, helmet_time_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                single_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_helmet_time)[i]);
                helmet_time_val.setText(single_choice_selected);
                editor.putInt("helmet_time",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                sendConfigToHelmet();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //턱끈 접촉음 크기 설정
    private String str_volume_choice_selected;
    private void showStrVolumeChoiceDialog() {
        str_volume_choice_selected = getResources().getStringArray(R.array.setting_volume)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("턱끈 접촉음 크기");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_volume, str_volume_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                str_volume_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_volume)[i]);
                str_volume_val.setText(str_volume_choice_selected);
                editor.putInt("str_volume",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //턱끈 가속센서반응정도 설정
    private String str_acl_choice_selected;
    private void showStrAclChoiceDialog() {
        str_acl_choice_selected = getResources().getStringArray(R.array.setting_helmet_acl)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("가속센서 반응정도");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_helmet_acl, str_acl_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                str_acl_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_helmet_acl)[i]);
                str_acl_val.setText(str_acl_choice_selected);
                editor.putInt("str_acl",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();

                if (BleService.isService) {
                    byte[] msg = new byte[2];
                    msg[0] = 0x41;

                    if(0 == i){
                        //낮음
                        msg[1] = 0x0b;
                    }else if(1 == i){
                        //중간
                        msg[1] = 0x09;
                    }else if(2 == i){
                        //높음
                        msg[1] = 0x05;
                    }

                    BleService.instance.sendConfigAclStrip(msg);
                }
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //헬맷 가속센서반응정도 설정
    private String helmet_acl_choice_selected;
    private void showAclChoiceDialog() {
        helmet_acl_choice_selected = getResources().getStringArray(R.array.setting_helmet_acl)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("가속센서 반응정도");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_helmet_acl, helmet_acl_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                helmet_acl_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_helmet_acl)[i]);
                helmet_acl_val.setText(helmet_acl_choice_selected);
                editor.putInt("helmet_acl",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                sendConfigToHelmet();

                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void sendConfigToHelmet()
    {
        if (null != BleService.instance && BleService.isService) {
            byte[] msg = new byte[7];
            msg[0] = 0x02;
            if( helmet_time_val.getText().equals("3초")){
                msg[1] = 0x03;
            }else if( helmet_time_val.getText().equals("5초")){
                msg[1] = 0x05;
            }else if( helmet_time_val.getText().equals("7초")){
                msg[1] = 0x07;
            }else if( helmet_time_val.getText().equals("10초")) {
                msg[1] = 0x0a;
            }
            msg[2]=0x00;
            if (helmet_acl_val.getText().equals("1")) {
                msg[3] = 0x01;
            } else if (helmet_acl_val.getText().equals("2")) {
                msg[3] = 0x02;
            } else if (helmet_acl_val.getText().equals("3")) {
                msg[3] = 0x03;
            } else if (helmet_acl_val.getText().equals("4")) {
                msg[3] = 0x04;
            } else if (helmet_acl_val.getText().equals("5")) {
                msg[3] = 0x05;
            } else if (helmet_acl_val.getText().equals("6")) {
                msg[3] = 0x06;
            } else if (helmet_acl_val.getText().equals("7")) {
                msg[3] = 0x07;
            } else if (helmet_acl_val.getText().equals("8")) {
                msg[3] = 0x08;
            } else if (helmet_acl_val.getText().equals("9")) {
                msg[3] = 0x09;
            } else if (helmet_acl_val.getText().equals("10")) {
                msg[3] = 0x0a;
            } else if (helmet_acl_val.getText().equals("11")) {
                msg[3] = 0x0b;
            }
            msg[4] = 0x03;
            msg[5] = 0x00;
            msg[6] = 0x00;

            BleService.instance.sendXYZWrite(msg);
        }
    }

    //기타 - 긴급 발신 지연 시간
    private String etc_time_choice_selected;
    private void showEtcTimeChoiceDialog() {
        etc_time_choice_selected = getResources().getStringArray(R.array.setting_etc_time)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("긴급 발신 시간 지연");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_etc_time, etc_time_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                etc_time_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_etc_time)[i]);
                etc_time_val.setText(etc_time_choice_selected);
                editor.putInt("etc_time",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //기타 - 긴급신호수신자 설정
    private String etc_receiver_choice_selected;
    private void showEtcReceiverChoiceDialog() {
        etc_receiver_choice_selected = getResources().getStringArray(R.array.setting_etc_receiver)[0];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SettingThemeDialog);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("긴급 신호 수신자");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(R.array.setting_etc_receiver, etc_receiver_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                etc_receiver_choice_selected = String.valueOf(getResources().getStringArray(R.array.setting_etc_receiver)[i]);
                etc_receiver_val.setText(etc_receiver_choice_selected);
                editor.putInt("etc_receiver",i); // key, value를 이용하여 저장하는 형태
                //최종 커밋
                editor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        AppVariables.activitySet.finish();
        super.onBackPressed();
    }

    @Override
    public  void onDestroy(){
        super.onDestroy();
        AppVariables.getSettingValue(auto);
    }
}
