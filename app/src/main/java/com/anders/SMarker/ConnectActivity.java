package com.anders.SMarker;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.annotation.RequiresApi;

import com.anders.SMarker.fcm.FirebaseInstanceIDService;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AlarmDlg;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.Tools;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;

public class ConnectActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean mScanning;
    private Handler mHandler;

    private ScanCallback mScanCallback = null;
    private BleScanListner mBleScanListner;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private String[] permisionlist = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}; //내 앱에 부여할 퍼미션
    private final int PERMISSION_REQ_CODE = 225;

    private TextView txtStripInfo = null;
    private TextView txtHelmetInfo = null;

    private String sStripMacAddress = "";
    private String sHelmetMacAddress ="";

    //앱 종료
    private long backKeyPressedTime = 0;

    public static final int ANIMATION_DELAY_TIME = 500;
    private Animation buttonAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        initToolbar();
        tokenCheck();//토큰값 체크

        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button_animation);

        Button btnGoToMain = (Button)findViewById(R.id.btnGoToMain);
        Button btnGoToMainVIew = (Button)findViewById(R.id.btnGoToMainVIew);
        //Button btnGoToWeb = (Button)findViewById(R.id.btnGoToWeb);

        /*if(AppVariables.User_Permission.equals("Y")){
            btnGoToWeb.setVisibility(View.VISIBLE);
        }*/

        final ImageView btnReScanStrip = (ImageView)findViewById(R.id.btnReScanStrip);
        ImageView btnReScanHelmet = (ImageView)findViewById(R.id.btnReScanHelmet);

        txtStripInfo = (TextView)findViewById(R.id.txtStripInfo);
        txtHelmetInfo = (TextView)findViewById(R.id.txtHelmetInfo);




        txtStripInfo.setText("기기를 찾을 수 없습니다.");

        txtHelmetInfo.setText("기기를 찾을 수 없습니다.");

        if(IsPermision() == true) {
            mHandler = new Handler();
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "Bluetooth 기기를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        btnGoToMain.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){

                if( checkMacAddress()) {
                    stopScan();
                    AppVariables.isRunServiceMainView = true;
                    startMainActivity();
                }
            }
        });

        btnGoToMainVIew.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                stopScan();
                AppVariables.isRunServiceMainView = false;
                startMainActivity();
            }
        });


        /*btnGoToWeb.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                //
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri u = Uri.parse(NetworkTask.API_SERVER_ADRESS);
                i.setData(u);
                startActivity(i);
            }
        });*/

        btnReScanStrip.setOnClickListener(new ImageView.OnClickListener(){
            @Override
            public void onClick(View view){
                view.startAnimation(buttonAnimation);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if(mScanning){
                            scanLeDevice(false);
                        }
                        scanLeDevice(true);
                    }
                }, ANIMATION_DELAY_TIME < 0 ? 0 : ANIMATION_DELAY_TIME );
            }
        });

        btnReScanHelmet.setOnClickListener(new ImageView.OnClickListener(){
            @Override
            public void onClick(View view){
                view.startAnimation(buttonAnimation);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if(mScanning){
                            scanLeDevice(false);
                        }
                        scanLeDevice(true);
                    }
                }, ANIMATION_DELAY_TIME < 0 ? 0 : ANIMATION_DELAY_TIME );
            }
        });

        //if( AppVariables.Strip_Mac_Adress.isEmpty() || AppVariables.Helmet_Mac_Adress.isEmpty()) {
        scanLeDevice(true);
        //}
    }

    //토큰 체크
    private void tokenCheck() {
        if(AppVariables.User_Hp_Token.equals("") || AppVariables.User_Hp_Token.length() == 0){
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
            dialog.setContentView(R.layout.dialog_token_warning);
            dialog.setCancelable(false);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


            ((AppCompatButton) dialog.findViewById(R.id.btn_token_dlg_close)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    FirebaseInstanceIDService.sendRegistrationToServer(refreshedToken);
                    dialog.dismiss();
                }
            });

            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }
    }

    private boolean checkMacAddress(){
        boolean bResult = false;
        if(AppVariables.Strip_Mac_Adress.isEmpty() && ! sStripMacAddress.isEmpty()){
            //처음 스캔해서 Mac 을 가져왔을 경우
            AppVariables.Strip_Mac_Adress = sStripMacAddress;
            sendToServerStripMacAdress();
        }
        if(!AppVariables.Strip_Mac_Adress.equals(sStripMacAddress) && !sStripMacAddress.isEmpty()){
            //장비가 바뀐경우
            AppVariables.Strip_Mac_Adress = sStripMacAddress;
            sendToServerStripMacAdress();
        }

        if(AppVariables.Helmet_Mac_Adress.isEmpty() && ! sHelmetMacAddress.isEmpty()){
            //처음 스캔해서 Mac 을 가져왔을 경우
            AppVariables.Helmet_Mac_Adress = sHelmetMacAddress;
            sendToServerHelmetMacAdress();
        }
        if(!AppVariables.Helmet_Mac_Adress.equals(sHelmetMacAddress) && !sHelmetMacAddress.isEmpty()){
            //장비가 바뀐경우
            AppVariables.Helmet_Mac_Adress = sHelmetMacAddress;
            sendToServerHelmetMacAdress();
        }

        Log.i("---------->",AppVariables.Strip_Mac_Adress);
        Log.i("---------->",AppVariables.Helmet_Mac_Adress);

        if( AppVariables.helmetDevice !=null  || AppVariables.stripDevice !=null) bResult = true;
        return bResult;
    }

    private void sendToServerStripMacAdress(){
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("phoneNB", AppVariables.User_Phone_Number);
        addData.put("stripMAC", AppVariables.Strip_Mac_Adress);
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_UPDATE_USER_STRIP_MAC, addData);
        Log.i("UPDATE==>","Strip Mac");
        try {
            String result = networkTask.execute().get();
            if (!result.isEmpty()) {
                resultBuilder = result.split("\\|");
                if (resultBuilder[0].equals("Y")) {
                    Log.i("UPDATE","STRIP Mac Update ok");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServerHelmetMacAdress(){
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        addData.put("phoneNB", AppVariables.User_Phone_Number);
        addData.put("helmetMAC", AppVariables.Helmet_Mac_Adress);
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_UPDATE_USER_Helmet_MAC, addData);

        Log.i("UPDATE==>","Helmet Mac");
        try {
            String result = networkTask.execute().get();
            if (!result.isEmpty()) {
                resultBuilder = result.split("\\|");
                if (resultBuilder[0].equals("Y")) {
                    Log.i("UPDATE","Helmet Mac Update ok");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    stopScan();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            startScan();
        } else {
            mScanning = false;
            stopScan();
        }
    }
    public boolean startScan()
    {
        //if(mBleDevicelist != null) mBleDevicelist.clear();
        if(mBluetoothAdapter == null) return false;

        if(mBluetoothAdapter.isEnabled() == false)
        {
            mBluetoothAdapter.enable();
        }

        try
        {
            if(mScanCallback == null) makeCallBakc();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            }
            else
            {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public void stopScan()
    {
        if(mBluetoothAdapter == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        }
        else
        {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void makeCallBakc()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mScanCallback = new ScanCallback()
            {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    scanProcess(result.getDevice());
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }
                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };
        }
        else
        {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    scanProcess(bluetoothDevice);
                }
            };
        }
    }

    private void scanProcess(BluetoothDevice device)
    {
        if(device == null) return;
        String deviceName = device.getName();
        if(deviceName == null) return;

        Log.d("device===",device.getName() + ":" + "S" + AppVariables.User_Phone_Number.substring(3));
        String stripName = "S" + AppVariables.User_Phone_Number.substring(3) ;
        String helmetName = "H" + AppVariables.User_Phone_Number.substring(3) ;

        if( device.getName().trim().equals(stripName)){
            Log.d("mac Adress==",device.getAddress());
            AppVariables.stripDevice = device;
            sStripMacAddress = device.getAddress();
            txtStripInfo.setText(device.getName()+":"+device.getAddress());
            //device.createBond();
        }else if( device.getName().trim().equals(helmetName)){
            AppVariables.helmetDevice = device;
            sHelmetMacAddress = device.getAddress();
            txtHelmetInfo.setText(device.getName()+":"+device.getAddress());
            //device.createBond();
        }

        if(mBleScanListner != null)
            mBleScanListner.scanResult(device);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            };

    private boolean IsPermision()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            for(String permision : permisionlist)
            {
                int permisionState = checkSelfPermission(permision);
                if(permisionState == -1)
                {
                    requestPermissions(permisionlist, PERMISSION_REQ_CODE); //권한 요청
                    return false;
                }
            }
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {

                }else if (resultCode == RESULT_CANCELED){
                    Toast.makeText(getApplicationContext(), "bluetooth를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void startMainActivity(){
        finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("기기연결");
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
            onBackPressed();
        }else if (item.getItemId()== R.id.btnEmergency){
            AlarmDlg.showAlarmDialog(this,""); //권한 허용 시 비상 알림 띄우기

        }else if (item.getItemId()== R.id.action_settings){
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) //권한 요청에 대한 응답
        {
            try {
                boolean bPemision = true;

                for(int i = 0; i < permisionlist.length; i++)
                {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        bPemision = false;
                        break;
                    }
                }

                if(bPemision == true)
                {
                    //
                }
                else {
                    Toast.makeText(this, "권한요청이 정상적으로 이루어 지지않아 앱을 종료 합니다.", Toast.LENGTH_LONG).show();
                }
            }
            catch (Exception e) {
            }
        }
    }

    public void setScanListner(BleScanListner bleScanListner)
    {
        mBleScanListner = bleScanListner;
    }

    public interface BleScanListner
    {
        public void scanResult(BluetoothDevice device);
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
                sendToServerExit();
                finish();
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mScanning){
            scanLeDevice(false);
        }
        if( mBleScanListner !=null){
            mBleScanListner = null;
        }
        if (mBluetoothAdapter != null) {
            //mBluetoothAdapter.disable();
            mBluetoothAdapter = null;
        }

    }


}

