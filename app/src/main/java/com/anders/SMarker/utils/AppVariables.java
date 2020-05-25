package com.anders.SMarker.utils;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.anders.SMarker.R;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class AppVariables {
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 3;

    public static long sendHpDate = System.currentTimeMillis() ;

    public static boolean reConnectStrip = false;

    public static boolean bStripConnect = false;
    public static boolean bHelmetConnect = false;

    public static String User_Phone_Number = "";
    public static String User_Name ="";
    public static String User_Email ="";
    public static String User_Team ="";
    public static String Helmet_Mac_Adress ="";
    public static String Strip_Mac_Adress ="";
    public static String User_Hp_Token="";
    public static String User_Idx ="";
    public static String User_Permission ="N";
    public static String User_co_idx = "";

    public static BluetoothDevice helmetDevice = null;
    public static BluetoothDevice stripDevice = null;

    public static int Config_Strip_Time =0;
    public static int Config_Strip_Battery_amount = 0;
    public static int Config_Strip_Mode = 0;
    public static int Config_Halmet_TIme=0;
    public static int Config_Halmet_Battery_amount =0;
    public static int Config_Delay_Time =0;
    public static int Config_Send_Mode =0;

    public static boolean isRunServiceMainView = false;

    public static int iStripBatteryAmmount = 0;
    public static int iHelmetBatteryAmount = 0;
    public static int iHelmetDust10 = 0;
    public static int iHelmetDust25 = 0;
    public static String sStripApplyMode = "0";

    public static final String EXTRA_SERVICE_DATA = "com.anders.SMarker.SERVICE_EXTRA_DATA";
    public static final String EXTRA_SERVICE_BATTERY_INFO_HELMET = "com.anders.SMarker.SERVICE_EXTRA_DATA.BATTERY.HELMET";
    public static final String EXTRA_SERVICE_BATTERY_INFO_STRIP = "com.anders.SMarker.SERVICE_EXTRA_DATA.BATTERY.STRIP";
    public static final String EXTRA_SERVICE_APPLY_INFO_STRIP = "com.anders.SMarker.SERVICE_EXTRA_DATA.APPLY.STRIP";
    public static final String EXTRA_SERVICE_EMERGENCY_ON_HELMET ="com.anders.SMarker.SERVICE_EXTRA_DATA.EMERGENCY.ON.HELMET";
    public static final String EXTRA_SERVICE_DUST_INFO_HELMET= "com.anders.SMarker.SERVICE_EXTRA_DATA.DUST.INFO.HELMET";
    public static final String EXTRA_SERVICE_EMERGENCY_OFF_STRIP= "com.anders.SMarker.SERVICE_EXTRA_DATA.EMERGENCY.OFF.STRIP";
    public static final String EXTRA_SERVICE_STOP= "com.anders.SMarker.SERVICE_EXTRA_DATA.STOP";

    public static Activity activitySet;

    public static Boolean IsShowInfoDialog = false;

    public static  SoundPool soundPoolEmer = null, soundPoolNormal = null;
    public static  int emerAlarm = -1, normalAlarm = -1;
    public static int iHelmetBatteryAmountFlag = -1;
    public static int iStripBatteryAmmountFlag = -1;

    public static Bitmap StringToBitMap(String encodedString){ //  string ->to bitmap
        try{
            byte [] encodeByte =  Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte,0,encodeByte.length);
            return bitmap;
        }catch (Exception e){
            e.getMessage();
            return null;
        }
    }

    public static String BitMapToString(Bitmap bitmap){ //bitmap ->to string
        ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,BAOS);
        byte [] b = BAOS.toByteArray();
        String temp = Base64.encodeToString(b,Base64.DEFAULT);
        return temp;
    }

//    public static void playSound(final Context context, final String  fileName){
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        try{
//            AssetFileDescriptor adf = context.getAssets().openFd( fileName);
//            mediaPlayer.setDataSource(adf.getFileDescriptor(), adf.getStartOffset(),adf.getLength());
//            adf.close();
//            mediaPlayer.prepare();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        mediaPlayer.start();
//    }

    public static void notifySound(final Context context){

        try{
            soundPoolEmer = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
            soundPoolNormal = new SoundPool(1, AudioManager.STREAM_MUSIC,0);

            emerAlarm = soundPoolEmer.load(context, R.raw.notifyalarm,1);
            normalAlarm = soundPoolNormal.load(context, R.raw.message,1);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {//사진 회전
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static void getSettingValue(SharedPreferences share)
    {
        int iReadValue =0 ;

        iReadValue = share.getInt("str_time",0);
        switch (iReadValue){
            default:Config_Strip_Time=3; break;
            case 1: Config_Strip_Time=4; break;
            case 2: Config_Strip_Time=5; break;
        }

        iReadValue= share.getInt("str_battery",0);
        switch (iReadValue){
            default:Config_Strip_Battery_amount = 0;  break;
            case 1: Config_Strip_Battery_amount = 20; break;
            case 2: Config_Strip_Battery_amount = 25; break;
            case 3: Config_Strip_Battery_amount = 30; break;
            case 4: Config_Strip_Battery_amount = 35; break;
        }

        iReadValue = share.getInt("str_sensing",0);
        switch (iReadValue){
            default:Config_Strip_Mode =0; break;
            case 1: Config_Strip_Mode =1; break;
        }

        iReadValue  = share.getInt("helmet_time",0);
        switch (iReadValue){
            default:Config_Halmet_TIme=3; break;
            case 1: Config_Halmet_TIme=5; break;
            case 2: Config_Halmet_TIme=7; break;
            case 3: Config_Halmet_TIme=10; break;
        }

        iReadValue = share.getInt("helmet_battery",0);
        switch (iReadValue){
            default:Config_Halmet_Battery_amount = 0;  break;
            case 1: Config_Halmet_Battery_amount = 20; break;
            case 2: Config_Halmet_Battery_amount = 25; break;
            case 3: Config_Halmet_Battery_amount = 30; break;
            case 4: Config_Halmet_Battery_amount = 35; break;
        }

        iReadValue = share.getInt("etc_time",10000);
        switch (iReadValue){
            default:Config_Delay_Time =10000; break;
            case 1: Config_Delay_Time =30000; break;
            case 2: Config_Delay_Time =60000; break;
            case 3: Config_Delay_Time =120000; break;
        }

        iReadValue = share.getInt("etc_receiver",0);
        switch (iReadValue){
            default:Config_Send_Mode =0; break; // 모두 알림
            case 1: Config_Send_Mode =1; break; // 현장 관리자
            case 2: Config_Send_Mode =2; break; // 현장 팀원
        }
    }


    public static int getConnectivityStatus(Context context){ //해당 context의 서비스를 사용하기위해서 context객체를 받는다.
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo != null){
            int type = networkInfo.getType();
            if(type == ConnectivityManager.TYPE_MOBILE){    // 3G or LTE
                return TYPE_MOBILE;
            }else if(type == ConnectivityManager.TYPE_WIFI){    // Wifi
                return TYPE_WIFI;
            }
        }
        return TYPE_NOT_CONNECTED;  // not connect Network
    }

    public static void showInfoDialog(Context con, String title, String content) {
        final Dialog dialog = new Dialog(con);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_info);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView txtTile = (TextView)dialog.findViewById(R.id.txt_dlg_info_title);
        TextView txtContent = (TextView)dialog.findViewById(R.id.txt_dlg_info_content);

        txtTile.setText(title);
        txtContent.setText(content);

        ((AppCompatButton) dialog.findViewById(R.id.btn_dlg_info_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                IsShowInfoDialog = false;
            }
        });

        dialog.show();
        IsShowInfoDialog = true;
        dialog.getWindow().setAttributes(lp);
    }

}
