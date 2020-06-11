package com.anders.SMarker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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
import com.anders.SMarker.utils.ServiceUtils;
import com.anders.SMarker.utils.SoundManager;
import com.anders.SMarker.utils.Tools;
import com.anders.SMarker.utils.WeakHandler;
import com.anders.SMarker.utils.WorkDlg;
import com.anders.SMarker.widget.LineItemDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnDismissListener{

    private ImageView imgFamilyPhoto;
    private Button btnStartStop;
    private Button btnGoConnect;

    private LinearLayout warninglyo;
    private RecyclerView alertrecyclerview;
    private String receiveString; //위험경보 string
    public static ArrayList<MainAlertList> alertLists= null;
    private AdapterMainAlertList adapterMainAlertList= null;

    private final static int LOADING_DURATION = 2500;
    private static final int FROM_CAMERA = 0;
    private static final int FROM_ALBUM = 1;
    private static final int REQUEST_IMAGE_CROP = 4444;
    private boolean permission = false;
    private static int selectItem = -1;
    File photoFile,storageDir = null;
    File fileName = null;
    private Uri imgUri, photoURI, albumURI;
    private String mCurrentPhotoPath;
    Uri contentUri;

    private boolean bIsStart = false;

    private static Context thisContext ;
    SharedPreferences auto;

    //weather
    private static String IMG_URL = "http://openweathermap.org/img/wn/";
    private static GpsTracker gpsTracker;

    //이미지업로드
    final String uploadFilePath = "storage/emulated/0/Pictures/userinfo/";
    int serverResponseCode = 0;

    private TextView txtHelmetBattery ;
    private TextView txtStripBattery;
    private TextView txtDustValue02;
    private ImageView imgStripTop ;
    private ImageView imgStripBottom ;
    private ImageView imgStripBattery;
    private Button Button_Find_Strip;
    private ImageView imgHelmetBattery;
    private Button Button_Find_Helmet;
    private Button Button_Acl_Helmet;
    private TextView TextView_ImgFamilyPhoto;

    private static Intent gattServiceIntent = null;
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private BleService mBleService;
    private boolean isService = false;

    public static BottomNavigationView navigation;

    private static final long MIN_CLICK_INTERVAL = 1500;
    private long mLastClickTime;

    private static final long STRIP_CHECK_INTERVAL = 2000;
    private long mLastStripTime;

    private String stripRequestFlag = "N";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        TextView_ImgFamilyPhoto = findViewById(R.id.TextView_ImgFamilyPhoto);

        thisContext = this;
        initComponent();
        initToolbar();
        LoadFamilyPhoto();

        warninglyo = findViewById(R.id.warninglyo);

        txtHelmetBattery = findViewById(R.id.txtHelmetBattery);
        txtStripBattery = findViewById(R.id.txtStripBattery);
        txtDustValue02 = findViewById(R.id.txtDustValue02);
        imgStripTop = findViewById(R.id.imgStripTop);
        imgStripBottom = findViewById(R.id.imgStripBottom);
        imgStripBattery = findViewById(R.id.imgStripBattery);
        Button_Find_Strip = findViewById(R.id.Button_Find_Strip);
        imgHelmetBattery = findViewById(R.id.imgHelmetBattery);
        Button_Find_Helmet = findViewById(R.id.Button_Find_Helmet);
        Button_Acl_Helmet = findViewById(R.id.Button_Acl_Helmet);
        imgFamilyPhoto = (ImageView)findViewById(R.id.imgFamilyPhoto);
        imgFamilyPhoto.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                carmeraImageSelect();
            }
        });

        btnStartStop = (Button)findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                chageWorkstate();
            }
        });

        btnGoConnect = (Button)findViewById(R.id.btnGoConnect);
        btnGoConnect.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                BleService.isForeground = false;
                if (gattServiceIntent != null) stopService(gattServiceIntent);
                startActivity(intent);
                finish();
            }
        });

        Button_Find_Strip.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                if (isService) {
                    mBleService.sendFindStripCode();
                }
            }
        });
        Button_Find_Helmet.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                long currentClickTime= SystemClock.uptimeMillis();
                long elapsedTime=currentClickTime-mLastClickTime;

                if(elapsedTime > MIN_CLICK_INTERVAL){
                    if (isService) {
                        mLastClickTime=currentClickTime;
                        mBleService.sendFindHelmetCode();
                    }
                }
            }
        });
        Button_Acl_Helmet.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                if (isService) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);

                    builder.setTitle("안내");
                    builder.setMessage("헬멧에 재접속 하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            auto = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                            if (null != BleService.instance && BleService.isService) {
                                int helmet_time_val = auto.getInt("helmet_time", 0);
                                int helmet_acl_val = auto.getInt("helmet_acl", 0);

                                byte[] msg = new byte[7];
                                msg[0] = 0x02;
                                if( helmet_time_val == 3){
                                    msg[1] = 0x03;
                                }else if( helmet_time_val == 5){
                                    msg[1] = 0x05;
                                }else if( helmet_time_val == 7){
                                    msg[1] = 0x07;
                                }else if( helmet_time_val == 10) {
                                    msg[1] = 0x0a;
                                }
                                msg[2]=0x00;
                                if (helmet_acl_val == 1) {
                                    msg[3] = 0x01;
                                } else if (helmet_acl_val == 2) {
                                    msg[3] = 0x02;
                                } else if (helmet_acl_val == 3) {
                                    msg[3] = 0x03;
                                } else if (helmet_acl_val == 4) {
                                    msg[3] = 0x04;
                                } else if (helmet_acl_val == 5) {
                                    msg[3] = 0x05;
                                } else if (helmet_acl_val == 6) {
                                    msg[3] = 0x06;
                                } else if (helmet_acl_val == 7) {
                                    msg[3] = 0x07;
                                } else if (helmet_acl_val == 8) {
                                    msg[3] = 0x08;
                                } else if (helmet_acl_val == 9) {
                                    msg[3] = 0x09;
                                } else if (helmet_acl_val == 10) {
                                    msg[3] = 0x0a;
                                } else if (helmet_acl_val == 11) {
                                    msg[3] = 0x0b;
                                }
                                msg[4] = 0x03;
                                msg[5] = 0x00;
                                msg[6] = 0x00;

                                BleService.instance.sendXYZWrite(msg);
                            }
                        }
                    });
                    builder.setNegativeButton("취소", null);
                    builder.show();
                }
            }
        });


        alertrecyclerview = (RecyclerView) findViewById(R.id.alertrecyclerview);
        gpsTracker = new GpsTracker(getApplicationContext());

        /*
        if(! getWeatherInfo()){
            showRetryDialog();
        }*/

        getWeatherInfo();
        recyclerviewAlert();
        warninglyoClick();

        //gattServiceIntent = new Intent(this, BleService.class);
        //startService(gattServiceIntent);

        if( AppVariables.isRunServiceMainView) {
            setStartService();
        }
    }

    private void initComponent() {
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.getMenu().getItem(0).setChecked(true);
        if(AppVariables.User_Permission.equals("Y")) {
            navigation.findViewById(R.id.bottom_team).setVisibility(View.VISIBLE);
        }else{
            navigation.findViewById(R.id.bottom_team).setVisibility(View.GONE);
        }
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.bottom_work:
                        Intent intent2 = new Intent(getApplicationContext(), WorkMainActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
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
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
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

    private void setStartService(){

        gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ContextCompat.startForegroundService(this, gattServiceIntent);
        else
            startService(gattServiceIntent);

        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceMessageReceiver, new IntentFilter(AppVariables.EXTRA_SERVICE_DATA));

        BleService.isForeground = false;
        mIsBound = true;
    }

    private void setUnbindService(){
        if(mIsBound){
            unbindService(mServiceConnection);
            mIsBound=false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceMessageReceiver);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.LocalBinder mBle = (BleService.LocalBinder)service;
            mBleService  = mBle.getService();
            mBleService.initPrepare();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };

    public void batteryAlarm(int batteryAmount, String gubn){
        if(batteryAmount <= 20){
            dialogPopup(batteryAmount, gubn);
        }else if(batteryAmount > 20 && batteryAmount <= 25){
            dialogPopup(batteryAmount, gubn);
        }else if(batteryAmount> 25 && batteryAmount <= 30){
            dialogPopup(batteryAmount, gubn);
        }else if(batteryAmount>30 && batteryAmount <= 35){
            dialogPopup(batteryAmount, gubn);
        }
    }

    public void dialogPopup(int batteryAmount, final String gubn){

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_battery_alarm);
        dialog.setCancelable(true);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        final  TextView txtEmergency = dialog.findViewById(R.id.txtBattery);

        final Button btnBatteryOk   = (Button)dialog.findViewById(R.id.btnBatteryOk);

        txtEmergency.setText("배터리가 "+Integer.toString(batteryAmount)+"% 입니다.");

        btnBatteryOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gubn.equals("helmet")) {
                    AppVariables.iHelmetBatteryAmountFlag = 0;
                }else if(gubn.equals("strip")){
                    AppVariables.iStripBatteryAmmountFlag = 0;
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
        /////////////////////////////////////////////////////////////////////////////

    }

    private WeakHandler<MainActivity> serviceMessageHandler = new WeakHandler<MainActivity>(this) {
        @Override
        protected void weakHandleMessage(MainActivity ref, Message msg) {
            final String smode = (String)msg.obj;
            final SoundManager soundManager = SoundManager.getInstance(thisContext);
            SharedPreferences sharedPreferences = getSharedPreferences("setting", Activity.MODE_PRIVATE);
            final double stripVolume = (sharedPreferences.getInt("str_volume", 0) + 1) * 0.2;

            String stripTimeCheckFlag = "N";

            if( smode != null){
                if (smode.equals("1") || smode.equals("2") || smode.equals("3")) {
                    stripRequestFlag = "Y";
                    stripTimeCheckFlag = "Y";
                } else {
                    stripRequestFlag = "N";
                    stripTimeCheckFlag = "N";
                }
            }

            final String stripTimeCheckFlagFinal = stripTimeCheckFlag;

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            if (stripRequestFlag.equals(stripTimeCheckFlagFinal)) {
                                if( smode != null){
                                    if (AppVariables.Config_Strip_Mode == 0) { // 하나이상 접촉 (설정)
                                        if (smode.equals("1") || smode.equals("2") || smode.equals("3")) {
                                            imgStripTop.setImageDrawable(getResources().getDrawable(R.drawable.circle_top));
                                            imgStripBottom.setImageDrawable(getResources().getDrawable(R.drawable.circle_bottom));
                                        } else {
                                            imgStripTop.setImageDrawable(getResources().getDrawable( R.drawable.circle_top_off));
                                            imgStripBottom.setImageDrawable(getResources().getDrawable(R.drawable.circle_bottom_off));
                                            imgStripBottom.setImageResource(R.drawable.circle_bottom_off);
                                        }
                                    } else {
                                        if(smode.equals("1")) {
                                            imgStripTop.setImageDrawable(getResources().getDrawable(R.drawable.circle_top));
                                            imgStripBottom.setImageDrawable(getResources().getDrawable(R.drawable.circle_bottom_off));
                                        }else if(smode.equals("2")){
                                            imgStripTop.setImageDrawable(getResources().getDrawable(R.drawable.circle_top_off));
                                            imgStripBottom.setImageDrawable(getResources().getDrawable(R.drawable.circle_bottom));
                                        }else if(smode.equals("3")){
                                            imgStripTop.setImageDrawable(getResources().getDrawable(R.drawable.circle_top));
                                            imgStripBottom.setImageDrawable(getResources().getDrawable(R.drawable.circle_bottom));
                                        }else{
                                            imgStripTop.setImageDrawable(getResources().getDrawable( R.drawable.circle_top_off));
                                            imgStripBottom.setImageDrawable(getResources().getDrawable(R.drawable.circle_bottom_off));
                                            imgStripBottom.setImageResource(R.drawable.circle_bottom_off);
                                        }
                                    }
                                }

                                if (!TextUtils.isEmpty(smode)) {
                                    int smodeConvert = Integer.parseInt(smode);

                                    //턱끈 착용 감지 설정
                                    if(AppVariables.Config_Strip_Mode == 0){//하나이상 접촉
                                        if( (BleService.mainChkMode ==0) && (smodeConvert >0)) {
                                            soundManager.play(R.raw.strap1, (float) stripVolume);
                                            sendToServerStripState(3,0);
                                        }else if( (BleService.mainChkMode > 0) && (smodeConvert==0)){
                                            soundManager.play(R.raw.strap2, (float) stripVolume);
                                            sendToServerStripState(0,0);
                                        }
                                        BleService.mainChkMode = smodeConvert;
                                    }else{//예외 시 모든 센서 접촉으로 기본 설정
                                        if((smodeConvert == 0 && BleService.mainChkMode > 0) ||  (smodeConvert == 3 && BleService.mainChkMode == 0)){
                                            if(smodeConvert==0){
                                                sendToServerStripState(smodeConvert,0);
                                                soundManager.play(R.raw.strap2, (float) stripVolume);
                                            }else if(smodeConvert==3){
                                                sendToServerStripState(smodeConvert,0);
                                                soundManager.play(R.raw.strap1, (float) stripVolume);
                                            }
                                            BleService.mainChkMode = smodeConvert;
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            };
            timer. schedule(timerTask, STRIP_CHECK_INTERVAL);

            TimerTask timerTask2 = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {

                        }
                    });
                }
            };
            timer. schedule(timerTask2, STRIP_CHECK_INTERVAL);
        }
    };

    private BroadcastReceiver mServiceMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBleService.bExeThread = false;
            String action = intent.getAction();

            if (AppVariables.EXTRA_SERVICE_BATTERY_INFO_HELMET.equals(intent.getStringExtra("action"))
                || "com.anders.SMarker.ACTION_GATT_DISCONNECTED.HELMET".equals(intent.getStringExtra("action"))) {
                String sHelmetBattery = intent.getStringExtra(AppVariables.EXTRA_SERVICE_BATTERY_INFO_HELMET);
                if (sHelmetBattery != null) {
                    txtHelmetBattery.setText(Integer.toString(AppVariables.iHelmetBatteryAmount) + "%");
                    txtHelmetBattery.setVisibility(View.VISIBLE);

                    // Anders
                    if (AppVariables.iHelmetBatteryAmount > 75) {
                        imgHelmetBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_100per));
                    } else if (AppVariables.iHelmetBatteryAmount > 50) {
                        imgHelmetBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_75per));
                    } else if (AppVariables.iHelmetBatteryAmount > 25) {
                        imgHelmetBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_50per));
                    } else {
                        imgHelmetBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_25per));
                    }
                    Button_Find_Helmet.setVisibility(View.VISIBLE);
                    Log.i("최종 헬맷 배터리===>", Integer.toString(AppVariables.iHelmetBatteryAmount));
                    if (AppVariables.iHelmetBatteryAmountFlag == -1) {
                        batteryAlarm(AppVariables.iHelmetBatteryAmount, "helmet");
                    }
                } else {
                    imgHelmetBattery.setImageDrawable(getResources().getDrawable(R.drawable.not_connect));
                    txtHelmetBattery.setVisibility(View.GONE);
                    Button_Find_Helmet.setVisibility(View.GONE);
                }
            }

            if (AppVariables.EXTRA_SERVICE_BATTERY_INFO_STRIP.equals(intent.getStringExtra("action"))
                || "com.anders.SMarker.ACTION_GATT_DISCONNECTED,STRIP".equals(intent.getStringExtra("action"))) {
                String sStripBattery = intent.getStringExtra(AppVariables.EXTRA_SERVICE_BATTERY_INFO_STRIP);
                if (sStripBattery != null) {
                    txtStripBattery.setText(Integer.toString(AppVariables.iStripBatteryAmmount) + "%");
                    txtStripBattery.setVisibility(View.VISIBLE);

                    // Anders
                    if (AppVariables.iStripBatteryAmmount > 75) {
                        imgStripBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_100per));
                    } else if (AppVariables.iStripBatteryAmmount > 50) {
                        imgStripBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_75per));
                    } else if (AppVariables.iStripBatteryAmmount > 25) {
                        imgStripBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_50per));
                    } else {
                        imgStripBattery.setImageDrawable(getResources().getDrawable(R.drawable.ic_25per));
                    }
                    Button_Find_Strip.setVisibility(View.VISIBLE);
                    Log.i("최종 턱끈 배터리===>", Integer.toString(AppVariables.iStripBatteryAmmount));

                    if (AppVariables.iStripBatteryAmmountFlag == -1) {
                        batteryAlarm(AppVariables.iStripBatteryAmmount, "strip");
                    }
                } else {
                    imgStripBattery.setImageDrawable(getResources().getDrawable(R.drawable.not_connect));
                    txtStripBattery.setVisibility(View.GONE);
                    Button_Find_Strip.setVisibility(View.GONE);
                    imgStripTop.setImageDrawable(getResources().getDrawable( R.drawable.circle_top_off));
                    imgStripBottom.setImageDrawable(getResources().getDrawable(R.drawable.circle_bottom_off));
                    imgStripBottom.setImageResource(R.drawable.circle_bottom_off);
                }
            }

            //턱끈의 상태는 신속하게 핸들러로 전달
            String smode = intent.getStringExtra(AppVariables.EXTRA_SERVICE_APPLY_INFO_STRIP);
            Message message = new Message();
            message.obj = smode;
            serviceMessageHandler.sendMessage(message);

            String emode = intent.getStringExtra(AppVariables.EXTRA_SERVICE_EMERGENCY_ON_HELMET);
            if( emode != null){
                // 긴급모드 ON

            }

            String omode = intent.getStringExtra(AppVariables.EXTRA_SERVICE_EMERGENCY_OFF_STRIP);
            if( omode != null){
                /*if(com.anders.SMarker.utils.AlertDialog.IsShow()) {
                    com.anders.SMarker.utils.AlertDialog.Close();
                }*/
            }

            String sdust = intent.getStringExtra(AppVariables.EXTRA_SERVICE_DUST_INFO_HELMET);
            if(sdust!=null){
                if(sdust.equals("10")) {
                    //AppVariables.iHelmetDust10 = 151;

                    if (AppVariables.iHelmetDust10 >= 151) {
                        txtDustValue02.setText("매우나쁨:" + Integer.toString(AppVariables.iHelmetDust10) + "㎛/㎥");
                    } else if ((AppVariables.iHelmetDust10 >= 31) && (AppVariables.iHelmetDust10 <= 80)) {
                        txtDustValue02.setText("보통:" + Integer.toString(AppVariables.iHelmetDust10) + "㎛/㎥");
                    } else if ((AppVariables.iHelmetDust10 >= 81) && (AppVariables.iHelmetDust10 <= 150)) {
                        txtDustValue02.setText("나쁨:" + Integer.toString(AppVariables.iHelmetDust10) + "㎛/㎥");
                    } else {
                        txtDustValue02.setText("좋음:" + Integer.toString(AppVariables.iHelmetDust10) + "㎛/㎥");
                    }


                    sendToServerDust(10,AppVariables.iHelmetDust10);

                    if(AppVariables.iHelmetDust10 >= 151){
                        if(!AppVariables.IsShowInfoDialog) {
                            AppVariables.showInfoDialog(thisContext, "[미세먼지 나쁨]", "마스크를 착용해 주세요");
                        }
                    }

                }else if(sdust.equals("25")){
                    sendToServerDust(25,AppVariables.iHelmetDust25);
                }
            }

            String offService = intent.getStringExtra(AppVariables.EXTRA_SERVICE_STOP);
            if(offService!=null){
                startService(gattServiceIntent);
            }

            mBleService.bExeThread=true;
        }
    };

    // anders
    private void chageWorkstate(){
        if(bIsStart){
            sendToServerStopWork();
            btnStartStop.setText("작업시작");
            btnStartStop.setBackgroundResource(R.drawable.btn_rounded_primary);
            btnStartStop.setTextColor(getResources().getColor(R.color.mainColor));
            bIsStart = false;
        }else {
            if (AppVariables.iHelmetBatteryAmount >= 50) {
                WorkDlg.showWorkDlg(this);
                WorkDlg.dialog.setOnDismissListener(this);

                btnStartStop.setText("작업종료");
                btnStartStop.setBackgroundResource(R.drawable.btn_rounded_accent);
                btnStartStop.setTextColor(getResources().getColor(R.color.overlay_light_80));
                bIsStart = true;
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("헬멧 베터리가 부족합니다.\n헬멧 충전 후 작업을 시작하세요.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface $dialog){
        int nWorkMode = WorkDlg.getWorkMode();

        Log.i("H------------------>", Integer.toString(nWorkMode));
        if (nWorkMode == 1 || nWorkMode == 3){
            if (isService) {
                byte[] workStartCommand = {(byte) 0xda, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x5a, (byte) 0x9f, (byte) 0xf5};
                mBleService.sendStartWorkCode(workStartCommand);
                SystemClock.sleep(500);
                byte[] configCommand = {(byte) 0x02, (byte) 0x20, (byte) 0x00, (byte) 0xC1, (byte) 0x03, (byte) 0x00, (byte) 0x00};
                mBleService.sendDeviceConfig(configCommand);
            }
        }
        sendToServerStartWork(nWorkMode);
    }

    private void sendToServerDust(int iType, int iVal){
        gpsTracker = new GpsTracker(this);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("CO_IDX", 1);
        addData.put("USER_IDX", Integer.parseInt(AppVariables.User_Idx));
        addData.put("DUST_TYPE", iType);
        addData.put("DUST_VALUE", iVal);
        addData.put("LOC_X",latitude);
        addData.put("LOC_Y",longitude);

        Log.i("0000000000000000000000000==>", addData.toString());

        NetworkTask networkTask = new NetworkTask(NetworkTask.API_UPDATE_DUST_VALUE, addData);
        try {
            String result = networkTask.execute().get();
            if (!result.isEmpty()) {
                resultBuilder = result.split("\\|");
                if (resultBuilder[0].equals("Y")) {
                    Log.i("UPDATE","Start Work");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServerStartWork(int ival){
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("CO_IDX", Integer.parseInt(AppVariables.User_co_idx));
        addData.put("USER_IDX", Integer.parseInt(AppVariables.User_Idx));
        addData.put("WORK_TYPE" , ival);

        NetworkTask networkTask = new NetworkTask(NetworkTask.API_UPDATE_START_WORK, addData);
        try {
            String result = networkTask.execute().get();
            if( result != null) {
                if (!result.isEmpty()) {
                    resultBuilder = result.split("\\|");
                    if (resultBuilder[0].equals("Y")) {
                        Log.i("UPDATE", "Start Work");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServerStopWork(){
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("CO_IDX", Integer.parseInt(AppVariables.User_co_idx));
        addData.put("USER_IDX", Integer.parseInt(AppVariables.User_Idx));


        NetworkTask networkTask = new NetworkTask(NetworkTask.API_UPDATE_STOP_WORK, addData);
        try {
            String result = networkTask.execute().get();
            if(result != null) {
                if (!result.isEmpty()) {
                    resultBuilder = result.split("\\|");
                    if (resultBuilder[0].equals("Y")) {
                        Log.i("UPDATE", "Start Work");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LoadFamilyPhoto(){
        //사진 있으면 뿌려주기
        SharedPreferences auto = getSharedPreferences("familyimage", Activity.MODE_PRIVATE);
        if (auto !=null)
        {
            String image=auto.getString("familyimagestrings", "");
            Bitmap bitmap = AppVariables.StringToBitMap(image);
            if(bitmap !=null) {
                TextView_ImgFamilyPhoto.setVisibility(View.GONE);
                ImageView iv = (ImageView) findViewById(R.id.imgFamilyPhoto);
                iv.setImageBitmap(bitmap);
            } else {
                TextView_ImgFamilyPhoto.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            return;
        }

        switch (requestCode){
            case FROM_ALBUM : {
                //앨범에서 가져오기(로컬)
                if(data.getData()!=null){
                    try{
                        File albumFile = null;
                        albumFile = createImageFile();
                        photoURI = data.getData();
                        albumURI = Uri.fromFile(albumFile);

                        cropImage();

                    }catch (Exception e){
                        e.printStackTrace();
                        Log.v("알림","앨범에서 가져오기 에러");
                    }
                }
                break;
            }
            case FROM_CAMERA : {
                //카메라 촬영
                try{
                    Log.v("알림", "FROM_CAMERA 처리");

                    galleryAddPic();


                    StoreImage(getApplicationContext(), contentUri, fileName);
                    Bitmap image = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    if(selectItem == 0){
                        imageSave(image);
                    }

                    if(selectItem==2) {
                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Log.d("start", "uploading started...");
                                    }
                                });
                                imageUpload(uploadFilePath + "" + fileName.getName());
                            }
                        }).start();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case REQUEST_IMAGE_CROP: {
                try{

                    // imageUpload(mCurrentPhotoPath);


                    galleryAddPic();
                    Bitmap bitmap = MediaStore.Images.Media
                            .getBitmap(getContentResolver(), albumURI);
                    if(selectItem == 1){
                        imageSave(bitmap);
                    }

                    if(selectItem==3) {
                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Log.d("start", "uploading started...");
                                    }
                                });
                                imageUpload(uploadFilePath + "" + fileName.getName());
                            }
                        }).start();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    //사진 서버 업로드
    private int imageUpload(final String fileName) {
        String urlString = NetworkTask.API_IMAGE_UPLOAD_SERVER ;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName);
        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath + "" + fileName);
            runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("Source File not exist :","error"+uploadFilePath + "" + fileName);
                }
            });
            return 0;
        }
        else
        {

            try {
                //폴더 사진 지우기
                NetworkTask networkTask = new NetworkTask(NetworkTask.API_IMAGE_DELETE_SERVER,null);
                networkTask.execute();

                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(urlString);
                // Open a HTTP  connection to  the URL

                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true); // Allow Inputs

                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""

                        + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                // Responses from the server (code and message)

                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "

                        + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {

                        public void run() {
                            Toast.makeText(MainActivity.this, "저장되었습니다.",

                                    Toast.LENGTH_SHORT).show();

                        }

                    });

                }
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                runOnUiThread(new Runnable() {

                    public void run() {
                        Toast.makeText(MainActivity.this, "연결 실패되었습니다.",

                                Toast.LENGTH_SHORT).show();

                    }

                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);

            } catch (Exception e) {
                e.printStackTrace();



                runOnUiThread(new Runnable() {

                    public void run() {

                        Log.e("Upload Exception", "Got Exception : see logcat");
                        Toast.makeText(MainActivity.this, "저장 오류가 발생했습니다. ", Toast.LENGTH_SHORT).show();

                    }

                });

                Log.e("Upload Exception", "Exception : " + e.getMessage(), e);

            }


            return serverResponseCode;



        } // End else block
    }

    private void carmeraImageSelect() {//사용자 이미지 넣기
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("사진 촬영");
        ListItems.add("앨범에서 선택");
        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("이미지 설정하기");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                selectItem = pos;
                if(tedPermission()){
                    if(pos == 0){//사진 촬영
                        takePhoto();
                    }else if(pos == 1){//앨범에서 사진 선택
                        selectAlbum();
                    }else if(pos == 2){//사진 촬영 및 서버 전송
                        takePhoto();
                    }else if(pos == 3){//앨범 선택 및 서버 전송
                        selectAlbum();
                    }
                }
            }
        });
        builder.show();
    }

    private boolean tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                //oast.makeText(UserInfoActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                if(!permission){
                    if(selectItem == 0){//사진 촬영
                        takePhoto();
                    }else if(selectItem == 1){//앨범에서 사진 선택
                        selectAlbum();
                    }else if(selectItem == 2){//사진 촬영 서버 전송
                        takePhoto();
                    }else if(selectItem == 3){//앨범에서 사진 선택, 서버 전송
                        selectAlbum();
                    }
                }
                permission = true;
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                permission = false;
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

        return permission;
    }

    public void takePhoto(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager())!=null){
                try{
                    photoFile = createImageFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(photoFile!=null){
                    Uri providerURI = FileProvider.getUriForFile(this,getPackageName(),photoFile);
                    imgUri = providerURI;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(intent, FROM_CAMERA);
                }
            }
        }else{
            //Log.v("알림", "저장공간에 접근 불가능");
            return;
        }
    }

    public File createImageFile() throws IOException{

        String imgFileName = AppVariables.User_Phone_Number + ".jpg";
        File imageFile= null;

        storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "userinfo");
        if(storageDir.exists()){
            DeleteDir();
        }else{
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir,imgFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    private void DeleteDir()
    {
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/userinfo/");
            File[] flist = file.listFiles();

            for(int i = 0 ; i < flist.length ; i++)
            {
                flist[i].delete();

                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(flist[i]));
                sendBroadcast(scanIntent);

            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "파일 삭제 실패 ", Toast.LENGTH_SHORT).show();
        }
    }

    //앨범 선택 클릭(로컬)
    public void selectAlbum(){
        //앨범에서 이미지 가져옴
        //앨범 열기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }

    public void cropImage(){//앨범선택 - 사진 자르기

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI,"image/*");
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("scale",true);
        cropIntent.putExtra("outputX", 500);
        cropIntent.putExtra("outputY", 500);
        cropIntent.putExtra("output",albumURI);

        startActivityForResult(cropIntent,REQUEST_IMAGE_CROP);

    }

    private void StoreImage(Context applicationContext, Uri imgUri, File photoFile) {
        Bitmap bm = null;
        try {
            ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
            String exifOreientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            // bm = Media.getBitmap(mContext.getContentResolver(), imageLoc);
            bm = BitmapFactory.decodeStream(applicationContext.getContentResolver().openInputStream(imgUri), null, options);
            FileOutputStream out = new FileOutputStream(photoFile);
            bm.compress(Bitmap.CompressFormat.JPEG, 20, out);
            bm.recycle();

            if(exif !=null){//사진 촬영 시 회전 현상 방지
                ExifInterface newexif = new ExifInterface(mCurrentPhotoPath);
                newexif.setAttribute(ExifInterface.TAG_ORIENTATION,exifOreientation);
                newexif.saveAttributes();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void imageSave(Bitmap bitmap){
        try {

            // 이미지를 상황에 맞게 회전시킨다
            ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            bitmap = AppVariables.rotateImage(bitmap, exifDegree);

            imgFamilyPhoto.setImageBitmap(bitmap);

            //사진 값 저장
            String image = AppVariables.BitMapToString(bitmap);
            SharedPreferences pref = getSharedPreferences("familyimage",MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("familyimagestrings",image);
            TextView_ImgFamilyPhoto.setVisibility(View.GONE);

            editor.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int exifOrientationToDegrees(int exifOrientation) {//이미지 회전현상 해결
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } return 0;
    }

    public void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        fileName = new File(mCurrentPhotoPath);
        contentUri = Uri.fromFile(fileName);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        //Toast.makeText(this,"사진이 저장되었습니다",Toast.LENGTH_SHORT).show();

    }

    private boolean getWeatherInfo()
    {
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        TextView time1,time2,time3,time4,time5,temp1,temp2,temp3,temp4,temp5,txtWindSpeed,txtHumidity,txtWindDeg;
        ImageView imgWeather1,imgWeather2,imgWeather3,imgWeather4,imgWeather5;
        time1 = findViewById(R.id.time1);
        time2 = findViewById(R.id.time2);
        time3 = findViewById(R.id.time3);
        time4 = findViewById(R.id.time4);
        time5 = findViewById(R.id.time5);
        temp1 = findViewById(R.id.temp1);
        temp2 = findViewById(R.id.temp2);
        temp3 = findViewById(R.id.temp3);
        temp4 = findViewById(R.id.temp4);
        temp5 = findViewById(R.id.temp5);
        txtWindSpeed = findViewById(R.id.txtWindSpeed);
        txtWindDeg   = findViewById(R.id.txtWindDeg);
        txtHumidity = findViewById(R.id.txtHumidity);
        imgWeather1 = findViewById(R.id.imgWeather1);
        imgWeather2 = findViewById(R.id.imgWeather2);
        imgWeather3 = findViewById(R.id.imgWeather3);
        imgWeather4 = findViewById(R.id.imgWeather4);
        imgWeather5 = findViewById(R.id.imgWeather5);

        if( (latitude !=0 && longitude !=0 )) {
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
        toolbar.setTitle("메인화면");
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

        }else if (item.getItemId()== R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    private void recyclerviewAlert(){
        recevieListView();
        alertLists = new ArrayList<>();
        alertrecyclerview.addItemDecoration(new LineItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        alertrecyclerview.setHasFixedSize(true);

        LinearLayoutManager Im = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        alertrecyclerview.setLayoutManager(Im);

        int non_read_cnt=0;
        try {
            JSONArray jsonArray = new JSONArray(receiveString);
            int arraycnt = jsonArray.length();


            for(int i=0;i<arraycnt;i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String m_co_idx = item.getString("co_idx");
                String m_gubn = item.getString("gubn");
                String m_content = item.getString("content");
                String m_year = item.getString("year");
                String m_month = item.getString("month");
                String m_day = item.getString("day");

                MainAlertList messageData = new MainAlertList();
                String date = m_year+"년 "+m_month+"월 "+m_day +"일 위험경보";

                messageData.setM_co_idx(m_co_idx);
                messageData.setM_gubn(m_gubn);
                messageData.setM_content(m_content);
                messageData.setM_date(date);
                alertLists.add(messageData);
            }

            adapterMainAlertList = new AdapterMainAlertList(MainActivity.this, alertLists);
            adapterMainAlertList.notifyDataSetChanged();
            alertrecyclerview.setAdapter(adapterMainAlertList);


        } catch (JSONException e) {

            //Log.d(TAG, "showResult : ", e);
        }
    }

    private void recevieListView()
    {
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        // 이 부분 핸드폰번호 장치에서 가져온 것으로 수정 hwang
        addData.put("phoneNB", AppVariables.User_Phone_Number);

        NetworkTask networkTask = new NetworkTask(NetworkTask.API_MAIN_ALERT_RECEIVE, addData);

        try {
            String result =  networkTask.execute().get();
            if(!result.isEmpty()){
                receiveString = result;

            }else{

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void warninglyoClick() {//위험경보 클릭 시 메세지 리스트 이동
        warninglyo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MessageListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void showRetryDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_warning);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        ((AppCompatButton) dialog.findViewById(R.id.btn_network_dlg_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeatherInfo();
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    @Override
    public void onBackPressed() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.kt_logo);

        builder.setTitle("Smarker");
        builder.setMessage("종료하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BleService.isForeground = false;
                if (gattServiceIntent != null)  stopService(gattServiceIntent);
                sendToServerExit();
                finish();
                System.exit(0);
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();

    }

    private void sendToServerExit(){
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("USER_IDX", Integer.parseInt(AppVariables.User_Idx));
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_LOG_OFF, addData);
        try {
            String result = networkTask.execute().get();
            if (!result.isEmpty()) {
                resultBuilder = result.split("\\|");
                if (resultBuilder[0].equals("Y")) {
                    Log.i("UPDATE","Log Off");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServerStripState(int ival, int iType){
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("CO_IDX", 1);
        addData.put("USER_IDX", Integer.parseInt(AppVariables.User_Idx));
        addData.put("STRIP_STATE" , ival);
        addData.put("STRIP_TYPE" , iType);
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_INSERT_STRIP_STATE, addData);
        try {
            String result = networkTask.execute().get();
            Log.d("shet", ival + "||" + iType);
            Log.d("shet", result);
            if (result.equals("Y")) {
                resultBuilder = result.split("\\|");
                if (resultBuilder[0].equals("Y")) {
                    Log.i("UPDATE","Strip State");
                }
            }else{
                Toast.makeText(this,"턱끈 정보를 서버로 전송할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"턱끈 정보를 서버로 전송할 수 없습니다.", Toast.LENGTH_LONG).show();
        }

        BleService.mainChkMode = BleService.mainsMode;
        BleService.mainsMode = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setUnbindService();
        BleService.isForeground = true;
    }
}

