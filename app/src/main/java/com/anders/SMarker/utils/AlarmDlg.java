package com.anders.SMarker.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.anders.SMarker.ListTeamInfo;
import com.anders.SMarker.R;
import com.anders.SMarker.adapter.AdapterTeamList;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.service.GpsTracker;

public class AlarmDlg extends AppCompatActivity {
    //위치 정보
    private static GpsTracker gpsTracker;
    private static  CountDownTimer countDownTimer;
    private static String sGubun = "팀원";
    private static Boolean isShowState = false ;
    private static Dialog dialogClone = null;
    public static final int GPS_ENABLE_REQUEST_CODE = 2001;
    public static final int PERMISSIONS_REQUEST_CODE = 100;
    public static String[]  REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static boolean check_result = false;


    public static Context context = null;
    private static NetworkTask networkTask = null;

    public static void showAlarmDialog(Context con, String Gubun){
        context = con;
        sGubun = Gubun;
        if(sGubun != null && sGubun.isEmpty()) sGubun="팀원";


        //위치정보 권한
        if (!checkLocationServicesStatus(context)) {

            showDialogForLocationServiceSetting(context);
        }else {

            checkRunTimePermission(context);
            if(check_result){
                final Dialog dialog = new Dialog(con);
                dialogClone = dialog;
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                dialog.setContentView(R.layout.dialog_alarm);
                dialog.setCancelable(false);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                final TextView   txtAlarmSec = (TextView)dialog.findViewById(R.id.txtAlarmSec);

                //초 카운트
                countDownTimer = new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        txtAlarmSec.setText(String.format(Locale.getDefault(), "%d", millisUntilFinished / 1000L));
                    }

                    public void onFinish() {
                        gpsTracker = new GpsTracker(context);
                        Toast.makeText(context, "팀원에게 긴급상황을 요청했습니다.", Toast.LENGTH_LONG).show();
                        ContentValues addData = new ContentValues();

                        addData.put("user_hp", AppVariables.User_Phone_Number);
                        double latitude = gpsTracker.getLatitude();
                        double longitude = gpsTracker.getLongitude();

                        String address = getCurrentAddress(latitude, longitude);

                        addData.put("user_location",address);
                        addData.put("loc_x",latitude);
                        addData.put("loc_y",longitude);
                        addData.put("gubn",sGubun);
                        addData.put("Config_Send_Mode",AppVariables.Config_Send_Mode);

                        networkTask = new NetworkTask(NetworkTask.API_ALARM_SEND, addData);
                        try {
                            networkTask.execute().get();
                            //Log.d("현재 위치 주소 = " , address);
                            dialog.dismiss();
                        } catch (Exception e ) {

                            e.printStackTrace();
                            dialog.dismiss();

                        }
                    }
                }.start();

                ((AppCompatButton) dialog.findViewById(R.id.btnAlarmOk)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        countDownTimer.onFinish();
                        countDownTimer.cancel();
                        dialog.dismiss();
                        isShowState = false;
                    }
                });
                ((AppCompatButton) dialog.findViewById(R.id.btnAlarmCancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        countDownTimer.cancel();
                        dialog.dismiss();
                        isShowState = false;

                    }
                });
                isShowState = true;
                dialog.show();
                dialog.getWindow().setAttributes(lp);
            }

        }
    }

    //현재 주소 받아오기
    public static String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            //Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            //Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }
        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString();

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    public static void showDialogForLocationServiceSetting(final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {

            @Override
            public  void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                ((Activity)context).startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean bool_check = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    bool_check = false;
                    check_result = false;
                    break;
                }
            }
            if ( bool_check ) {
                showAlarmDialog(context, sGubun); //권한 허용 시 비상 알림 띄우기
                //위치 값을 가져올 수 있음
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(AlarmDlg.this, "위치 권한이 거부되었습니다. 위치 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                    // finish();
                }else {
                    Toast.makeText(AlarmDlg.this, "위치 권한이 거부되었습니다. 설정(앱 정보)에서 위치 권한을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }


    public static void checkRunTimePermission(Context context){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            check_result = true;
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                // Toast.makeText(BaseActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions((Activity)context, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions((Activity)context, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    //위치정보 권한
    public static boolean checkLocationServicesStatus(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //관리자가 팀원한테 전송
    public static void adminShowAlarmDialog(final AdapterTeamList mAdapter){
        context = mAdapter.ctx;
        //위치정보 권한

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_info_send);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final TextView txtMessage = (TextView)dialog.findViewById(R.id.txtMessage);
        final RadioGroup radioGroup = (RadioGroup)dialog.findViewById(R.id.radioGubn);


        final RadioButton btnGubn2 = (RadioButton)dialog.findViewById(R.id.btnGubn2);

        ((AppCompatButton) dialog.findViewById(R.id.btnSendOk)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(txtMessage.length() == 0){
                    Toast.makeText(context, "메세지를 입력하세요.", Toast.LENGTH_LONG).show();
                }else{
                    ContentValues addData = new ContentValues();
                    addData.put("user_hp", AppVariables.User_Phone_Number);
                    addData.put("user_idx",mAdapter.getTeamUserIdx());
                    addData.put("content",txtMessage.getText().toString());
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    //Toast.makeText(context, "팀원에게 긴급상황을 요청했습니다."+selectedId, Toast.LENGTH_LONG).show();

                    final RadioButton btnGubn = (RadioButton) dialog.findViewById(selectedId);

                    addData.put("gubn",btnGubn.getText().toString());

                    networkTask = new NetworkTask(NetworkTask.API_ADMIN_ALARM_SEND, addData);
                    try {
                        networkTask.execute().get();
                        dialog.dismiss();
                        Toast.makeText(context, "팀원에게 메세지를 전송했습니다.", Toast.LENGTH_LONG).show();

                        ListTeamInfo.mAdapter.clearSelections();
                        ListTeamInfo.actionMode.finish();

                    } catch (Exception e ) {

                        e.printStackTrace();
                        dialog.dismiss();
                        Toast.makeText(context, "메세지 전송이 실패됐습니다. 다시 전송하세요.", Toast.LENGTH_LONG).show();
                        ListTeamInfo.mAdapter.clearSelections();
                        ListTeamInfo.actionMode.finish();
                    }
                }


            }
        });

        ((AppCompatButton) dialog.findViewById(R.id.btnSendCancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                ListTeamInfo.actionMode.finish();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }


}
