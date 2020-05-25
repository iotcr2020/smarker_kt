package com.anders.SMarker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anders.SMarker.adapter.message.AdapterMainAlertList;
import com.anders.SMarker.http.JSONWeatherTask;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.model.MainAlertList;
import com.anders.SMarker.model.Weather;
import com.anders.SMarker.service.BleService;
import com.anders.SMarker.service.GpsTracker;
import com.anders.SMarker.utils.AlarmDlg;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.BottomNavigationViewHelper;
import com.anders.SMarker.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class WorkMainActivity extends AppCompatActivity {

    private ImageView imgDustState1,imgDustState2,imgWeather1,imgWeather2,imgWeather3,imgWeather4,imgWeather5;

    private TextView time1,time2,time3,time4,time5,temp1,temp2,temp3,temp4,temp5,txtWindSpeed,txtHumidity,txtWindDeg;

    private TextView txtDustValue02;
    //weather
    private static String IMG_URL = "http://openweathermap.org/img/wn/";
    private static GpsTracker gpsTracker;



    public static Context context = null;
    private BottomNavigationView navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_main_view);
        AppVariables.activitySet = WorkMainActivity.this;
        initComponent();
        initToolbar();


        context = getApplicationContext();



        imgDustState1 = (ImageView)findViewById(R.id.imgDustState1);
        imgDustState2 = (ImageView)findViewById(R.id.imgDustState2);


        time1 = (TextView)findViewById(R.id.time1);
        time2 = (TextView)findViewById(R.id.time2);
        time3 = (TextView)findViewById(R.id.time3);
        time4 = (TextView)findViewById(R.id.time4);
        time5 = (TextView)findViewById(R.id.time5);
        temp1 = (TextView)findViewById(R.id.temp1);
        temp2 = (TextView)findViewById(R.id.temp2);
        temp3 = (TextView)findViewById(R.id.temp3);
        temp4 = (TextView)findViewById(R.id.temp4);
        temp5 = (TextView)findViewById(R.id.temp5);
        txtDustValue02 =(TextView)findViewById(R.id.txtDustValue02);

        txtWindSpeed = (TextView)findViewById(R.id.txtWindSpeed);
        txtWindDeg   = (TextView)findViewById(R.id.txtWindDeg);
        txtHumidity = (TextView)findViewById(R.id.txtHumidity);
        imgWeather1 = (ImageView)findViewById(R.id.imgWeather1);
        imgWeather2 = (ImageView)findViewById(R.id.imgWeather2);
        imgWeather3 = (ImageView)findViewById(R.id.imgWeather3);
        imgWeather4 = (ImageView)findViewById(R.id.imgWeather4);
        imgWeather5 = (ImageView)findViewById(R.id.imgWeather5);


        gpsTracker = new GpsTracker(getApplicationContext());
        //getWeatherInfo();
        getDustInfo();
    }

    private void getDustInfo(){
        String sVal = "";
        if (AppVariables.iHelmetDust10 >= 151) {
            imgDustState1.setImageDrawable(getResources().getDrawable( R.drawable.duststate4));
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate4));
            sVal="매우나쁨:";
        } else if ((AppVariables.iHelmetDust10 >= 31) && (AppVariables.iHelmetDust10 <= 80)) {
            imgDustState1.setImageDrawable(getResources().getDrawable( R.drawable.duststate2));
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate2));
            sVal="보통:";
        } else if ((AppVariables.iHelmetDust10 >= 81) && (AppVariables.iHelmetDust10 <= 150)) {
            imgDustState1.setImageDrawable(getResources().getDrawable( R.drawable.duststate3));
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate3));
            sVal="나쁨:";
        } else {
            imgDustState1.setImageDrawable(getResources().getDrawable( R.drawable.duststate1));
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate1));
            sVal="좋음:";
        }
        txtDustValue02.setText(sVal  + Integer.toString(AppVariables.iHelmetDust10) + "㎛/㎥");

        if (AppVariables.iHelmetDust10 >= 141) {
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate4));
        } else if ((AppVariables.iHelmetDust10 >= 29) && (AppVariables.iHelmetDust10 <= 70)) {
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate2));
        } else if ((AppVariables.iHelmetDust10 >= 71) && (AppVariables.iHelmetDust10 <= 140)) {
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate3));
        } else {
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate1));
        }

        if(AppVariables.iHelmetDust10 == 0){
            imgDustState1.setImageDrawable(getResources().getDrawable( R.drawable.duststate2));
            imgDustState2.setImageDrawable(getResources().getDrawable( R.drawable.duststate2));
        }
    }


    private void initComponent() {


        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().findItem(R.id.bottom_work).setChecked(true);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        if(AppVariables.User_Permission.equals("Y")) {
            navigation.findViewById(R.id.bottom_team).setVisibility(View.VISIBLE);
        }else{
            navigation.findViewById(R.id.bottom_team).setVisibility(View.GONE);
        }
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                BottomNavigationViewHelper.disableShiftMode(navigation);
                switch (item.getItemId()) {
                    case R.id.bottom_main:
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        MainActivity.navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);

                       // onBackPressed();
                        return true;
                    case R.id.bottom_message:
                        Intent intent3 = new Intent(getApplicationContext(), MessageListActivity.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent3);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                    case R.id.bottom_info:
                        Intent intent2 = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                    case R.id.bottom_team:
                        Intent intent4 = new Intent(getApplicationContext(), ListTeamInfo.class);
                        intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent4);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                }
                return false;
            }
        });

    }
    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onPause(){
        super.onPause();

    }



    private boolean getWeatherInfo()
    {
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        if((latitude !=0 && longitude !=0 )) {
            String api_key = NetworkTask.API_OPENWEATHER_KEY;

            String addData = "appid="+api_key+"&lon="+longitude+"&lat="+latitude+"&mode=json&units=metric&cnt=7";

            JSONWeatherTask task = new JSONWeatherTask(addData);

            //http://api.openweathermap.org/data/2.5/forecast?q=Liverpool,gb&mode=json&appid=db1f92d4b15d1b1fe4f6b55dfe7b8f65&units=metric&cnt=8
            try {
                // Get our weather date in Date format
                SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());


                SimpleDateFormat todayFormat = new  SimpleDateFormat("yyyy-MM-dd");


                Date date = new Date();
                String todayDate = todayFormat.format(date);

                ArrayList<Weather> result = task.execute().get();
                int i=0;
                for(Weather weather: result) {
                    i++;
                    Date start_date = dateFormat.parse(weather.getStart_date());
                    Date end_date = dateFormat.parse(weather.getEnd_date());

                    if(date.after(start_date) && end_date.after(date)){

                        txtWindSpeed.setText(weather.getWind_speed()+"m/s");//풍속
                        txtHumidity.setText(weather.getHumidity()+"%");//습도
                        txtWindDeg.setText(weather.getWind_deg());//풍향
                    }

                    if(i==1){
                        time1.setText(weather.getTime());
                        temp1.setText(weather.getTemperature()+"ºC");
                        Glide.clear(imgWeather1);
                        String imgurl = IMG_URL+weather.getIcon()+".png";
                        Glide.with(getApplicationContext()).load(imgurl)
                                .placeholder(R.drawable.bg_multi_selection)
                                .override(150, 150)
                                .fitCenter()
                                .error(R.drawable.bg_multi_selection)
                                .signature(new StringSignature((UUID.randomUUID().toString())))
                                .into(imgWeather1);
                    }else if(i==2){
                        time2.setText(weather.getTime());
                        temp2.setText(weather.getTemperature()+"ºC");
                        Glide.clear(imgWeather2);
                        String imgurl = IMG_URL+weather.getIcon()+".png";
                        Glide.with(getApplicationContext()).load(imgurl)
                                .placeholder(R.drawable.bg_multi_selection)
                                .override(150, 150)
                                .fitCenter()
                                .error(R.drawable.bg_multi_selection)
                                .signature(new StringSignature((UUID.randomUUID().toString())))
                                .into(imgWeather2);
                    }else if(i==3){
                        time3.setText(weather.getTime());
                        temp3.setText(weather.getTemperature()+"ºC");
                        Glide.clear(imgWeather3);
                        String imgurl = IMG_URL+weather.getIcon()+".png";
                        Glide.with(getApplicationContext()).load(imgurl)
                                .placeholder(R.drawable.bg_multi_selection)
                                .override(150, 150)
                                .fitCenter()
                                .error(R.drawable.bg_multi_selection)
                                .signature(new StringSignature((UUID.randomUUID().toString())))
                                .into(imgWeather3);
                    }else if(i==4){
                        time4.setText(weather.getTime());
                        temp4.setText(weather.getTemperature()+"ºC");
                        Glide.clear(imgWeather4);
                        String imgurl = IMG_URL+weather.getIcon()+".png";
                        Glide.with(getApplicationContext()).load(imgurl)
                                .placeholder(R.drawable.bg_multi_selection)
                                .override(150, 150)
                                .fitCenter()
                                .error(R.drawable.bg_multi_selection)
                                .signature(new StringSignature((UUID.randomUUID().toString())))
                                .into(imgWeather4);
                    }else if(i==5){
                        time5.setText(weather.getTime());
                        temp5.setText(weather.getTemperature()+"ºC");
                        Glide.clear(imgWeather5);
                        String imgurl = IMG_URL+weather.getIcon()+".png";
                        Glide.with(getApplicationContext()).load(imgurl)
                                .placeholder(R.drawable.bg_multi_selection)
                                .override(150, 150)
                                .fitCenter()
                                .error(R.drawable.bg_multi_selection)
                                .signature(new StringSignature((UUID.randomUUID().toString())))
                                .into(imgWeather5);
                    }
                }




                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            return false;
        }
    }




    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("나의 작업화면");
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

        if(AppVariables.User_Permission.equals("Y")){
            //MenuItem item_p = menu.findItem(R.id.bottom_team);
            //item_p.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }else if (item.getItemId()== R.id.btnEmergency){
            AlarmDlg.showAlarmDialog(this,""); //권한 허용 시 비상 알림 띄우기

        }else if (item.getItemId()== R.id.action_settings){
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        MainActivity.navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);
        super.onBackPressed();

    }


}

