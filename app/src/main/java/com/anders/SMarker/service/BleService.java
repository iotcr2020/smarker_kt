package com.anders.SMarker.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.anders.SMarker.MainActivity;
import com.anders.SMarker.R;
import com.anders.SMarker.ble.HelmetProfile;
import com.anders.SMarker.ble.StripProfile;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AlertDialog;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.SoundManager;

import java.util.Timer;
import java.util.TimerTask;


public class BleService extends Service {

    public static BleService instance;
    public static int mainChkMode = 0;
    public static int mainsMode = 0;
    public static boolean isForeground = false;

    private static final int SYSTEM_CLOCK_TIME = 0;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private final static String TAG = "------------------------>";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    //
    private int mConnectionStateStrip = STATE_DISCONNECTED;
    private int mConnectionStateHelmet = STATE_DISCONNECTED;

    //
    private BluetoothGatt mBluetoothGattStrip;
    private BluetoothGatt mBluetoothGattHelmet;
    private BluetoothGattCharacteristic helmetBattery_char;
    private BluetoothGattCharacteristic helmetEmgRead_char;
    private BluetoothGattCharacteristic helmetEmgWrite_char;
    private BluetoothGattCharacteristic helmetConfigWrite_char;
    private BluetoothGattCharacteristic stripBattery_char;
    private BluetoothGattCharacteristic stripEmgRead_char;
    private BluetoothGattCharacteristic stripOnOff_char;
    private BluetoothGattCharacteristic stripConfigWrite_char;
    private BluetoothGattCharacteristic stripFind_char;
    private BluetoothGattCharacteristic stripAclConfigWrite_char;

    private Thread mThread;
    public boolean bExeThread = true;

    private int stripMode =0 ;
    private int helmetMode =0;

    private final IBinder mBinder = new LocalBinder();

    private final static String ACTION_STRIP_GATT_CONNECTED = "com.anders.SMarker.ACTION_GATT_CONNECTED,STRIP";
    private final static String ACTION_STRIP_GATT_DISCONNECTED = "com.anders.SMarker.ACTION_GATT_DISCONNECTED,STRIP";
    private final static String ACTION_STRIP_GATT_SERVICES_DISCOVERED = "com.anders.SMarker.ACTION_GATT_SERVICES_DISCOVERED,STRIP";
    private final static String ACTION_STRIP_DATA_AVAILABLE = "com.anders.SMarker.ACTION_DATA_AVAILABLE,STRIP";

    private final static String ACTION_HELMET_GATT_CONNECTED = "com.anders.SMarker.ACTION_GATT_CONNECTED.HELMET";
    private final static String ACTION_HELMET_GATT_DISCONNECTED = "com.anders.SMarker.ACTION_GATT_DISCONNECTED.HELMET";
    private final static String ACTION_HELMET_GATT_SERVICES_DISCOVERED = "com.anders.SMarker.ACTION_GATT_SERVICES_DISCOVERED.HELMET";
    private final static String ACTION_HELMET_DATA_AVAILABLE = "com.anders.SMarker.ACTION_DATA_AVAILABLE.HELMET";

    private static final int NOTI_ID = 1653422;

    private int chkMode = 0;
    private void myLog(String msg){
        Log.i("BleService ******>> ",msg);
    }


    public static boolean isService = false;
    /************************************************************************************************************************
     * 시스템
     *************************************************************************************************************************/
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(NOTI_ID, getNotification());

        // BlueTooth Filter
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND);    //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);   //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mBluetoothStateReceiver, stateFilter);

        instance = this;

        // Bluetooth Manager & Adapter
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                myLog("Unable to initialize BluetoothManager.");
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            myLog("Unable to obtain a BluetoothAdapter.");
        }else{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(AppVariables.helmetDevice !=null) {

                    mBluetoothGattHelmet = AppVariables.helmetDevice.connectGatt(BleService.this, false, mGattCallbackHelmet, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
                }
                if(AppVariables.stripDevice != null) {
                    mBluetoothGattStrip = AppVariables.stripDevice.connectGatt(BleService.this, false, mGattCallbackStrip, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
                }
            }
            else {
                if(AppVariables.helmetDevice !=null) {

                    mBluetoothGattHelmet = AppVariables.helmetDevice.connectGatt(BleService.this, false, mGattCallbackHelmet, BluetoothDevice.TRANSPORT_LE);
                }
                if(AppVariables.stripDevice != null) {
                    mBluetoothGattStrip = AppVariables.stripDevice.connectGatt(BleService.this, false, mGattCallbackStrip, BluetoothDevice.TRANSPORT_LE);
                }
            }
        }
        isService = true;
        bExeThread = true;
        startScanThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myLog("onStartCommand");

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public boolean stopService(Intent name) {
        myLog("stopService");
        return super.stopService(name);
    }

    //anders
    @Override
    public void onDestroy() {
        super.onDestroy();

        if( mBluetoothGattStrip != null) {
            if( mBluetoothGattStrip.connect()) mBluetoothGattStrip.disconnect();
            mBluetoothGattStrip.close();
            mBluetoothGattStrip = null;
        }

        if( mBluetoothGattHelmet != null) {
            if( mBluetoothGattHelmet.connect()) mBluetoothGattHelmet.disconnect();
            mBluetoothGattHelmet.close();
            mBluetoothGattHelmet = null;
        }

        if( mBluetoothAdapter !=null) {
            //mBluetoothAdapter.disable();
            mBluetoothAdapter = null;
            mBluetoothManager = null;
        }

        if(mainChkMode > 0) {
            sendToServerStripState(0, 1);
        }

        instance = null;

        this.unregisterReceiver(mBluetoothStateReceiver);
        stopSelf();

        myLog("onDestroy");
    }

    /************************************************************************************************************************
     * 통신
     *************************************************************************************************************************/

    private void startScanThread(){
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        //anders
                        Thread.sleep(1000 * 2 );
                        if( bExeThread) {
                            if (mConnectionStateStrip == STATE_DISCONNECTED || mConnectionStateHelmet == STATE_DISCONNECTED) {
                                if (mBluetoothAdapter != null) {
                                    if (!mBluetoothAdapter.isDiscovering()) {
                                        mBluetoothAdapter.startDiscovery();
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        myLog("Thread Error : "+e.toString());
                    }
                }

            }
        });
        mThread.start();
    }



    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bExeThread=false;
            final String action = intent.getAction();

            BluetoothDevice receiverDevice;
            String addressDevice ="";
            String nameDevice = "";

            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED: //블루투스의 연결 상태 변경
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state) {
                        case BluetoothAdapter.STATE_OFF: myLog("STATE_OFF:" +nameDevice); return;   //break  //hwang 2019.10.30
                        case BluetoothAdapter.STATE_TURNING_OFF: myLog("STATE_TURNING_OFF:" +nameDevice); return;  //break   //hwang 2019.10.30
                        case BluetoothAdapter.STATE_ON: myLog("STATE_ON:" +nameDevice); break;
                        case BluetoothAdapter.STATE_TURNING_ON: myLog("STATE_TURNING_ON:" +nameDevice); break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:  //블루투스 기기 연결
                    myLog("ACTION_ACL_CONNECTED:" +nameDevice);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    myLog("ACTION_BOND_STATE_CHANGED:" +nameDevice);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:   //블루투스 기기 끊어짐
                    sendToServerStripState(0,0);
                    myLog("ACTION_ACL_DISCONNECTED:" +nameDevice);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED: //블루투스 기기 검색 시작
                    myLog("ACTION_DISCOVERY_STARTED:" +nameDevice);
                    break;
                case BluetoothDevice.ACTION_FOUND:  //블루투스 기기 검색 됨, 블루투스 기기가 근처에서 검색될 때마다 수행됨
                    receiverDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    addressDevice = receiverDevice.getAddress();
                    nameDevice = receiverDevice.getName();

                    myLog("ACTION_FOUND:" +nameDevice + "   " + addressDevice);

                    if( AppVariables.Strip_Mac_Adress.equals(addressDevice)){
                        AppVariables.stripDevice = receiverDevice;
                        if(AppVariables.stripDevice != null && mConnectionStateStrip == STATE_DISCONNECTED) {
                            mBluetoothAdapter.cancelDiscovery();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mBluetoothGattStrip = AppVariables.stripDevice.connectGatt(BleService.this, false, mGattCallbackStrip, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
                            }
                            else {
                                mBluetoothGattStrip = AppVariables.stripDevice.connectGatt(BleService.this, false, mGattCallbackStrip, BluetoothDevice.TRANSPORT_LE);
                            }
                        }
                    }
                    if( AppVariables.Helmet_Mac_Adress.equals(addressDevice) ){ //FA:65:ED:63:E9:79
                        AppVariables.helmetDevice = receiverDevice;
                        if(AppVariables.helmetDevice != null && mConnectionStateHelmet == STATE_DISCONNECTED) {
                            mBluetoothAdapter.cancelDiscovery();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mBluetoothGattHelmet = AppVariables.helmetDevice.connectGatt(BleService.this, false, mGattCallbackHelmet, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
                            }
                            else {
                                mBluetoothGattHelmet = AppVariables.helmetDevice.connectGatt(BleService.this, false, mGattCallbackHelmet, BluetoothDevice.TRANSPORT_LE);
                            }
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    myLog("ACTION_PAIRING_REQUEST:" +nameDevice);
                    break;
            }
            bExeThread=true;
        }
    };

    /**
     * 턱끈 민감도 초기값 전송
     */
    private void initStrAclValue() {

        SharedPreferences auto;
        auto = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        int acl_value = auto.getInt("str_acl", 0);

        byte[] msg = new byte[2];
        msg[0] = 0x41;

        if(0 == acl_value){
            //낮음
            msg[1] = 0x0b;
        }else if(1 == acl_value){
            //중간
            msg[1] = 0x09;
        }else if(2 == acl_value){
            //높음
            msg[1] = 0x05;
        }

        sendConfigAclStrip(msg);
    }

    private final BluetoothGattCallback mGattCallbackStrip = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_STRIP_GATT_CONNECTED;
                mConnectionStateStrip = STATE_CONNECTED;
                AppVariables.bStripConnect = true;
                gatt.discoverServices();
                broadcastStripUpdate(intentAction);
                SoundManager.getInstance(getApplicationContext()).play(R.raw.connect, 1f);

                new Timer().schedule(new TimerTask() {
                    public void run() {
                        initStrAclValue();
                    }
                }, 2000); // 2초후 실행

                myLog("Connected to Strip GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                AppVariables.bStripConnect = false;
                intentAction = ACTION_STRIP_GATT_DISCONNECTED;
                mConnectionStateStrip = STATE_DISCONNECTED;
                broadcastStripUpdate(intentAction);
                SoundManager.getInstance(getApplicationContext()).play(R.raw.disconnect, 1f);
                myLog("Disconnected from Strip GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                myLog("Strip-->Discover sucess!!");
                prepareReadStrip(gatt);
            } else {
                myLog("onServicesDiscovered Strip-->received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,  BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastStripUpdate(ACTION_STRIP_DATA_AVAILABLE, characteristic);
                myLog("Strip-->onCharacteristicRead" + characteristic.getUuid().toString()+"::" + Integer.toString(status));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            broadcastStripUpdate(ACTION_STRIP_DATA_AVAILABLE, characteristic);
            myLog("Strip-->onCharacteristicChanged" + characteristic.getUuid().toString());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            myLog("Strip-->onCharacteristicWrite::" + characteristic.getUuid().toString()+"::" + Integer.toString(status));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            myLog("Strip DescriptorRead-->" + descriptor.getUuid().toString()+"::" + Integer.toString(status));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            myLog("Strip-->onDescriptorWrite::" + descriptor.getUuid().toString()+"::" + Integer.toString(status));

            BluetoothGattDescriptor clientConfig01;
            if(stripMode==0){
                stripMode = 1;
                gatt.setCharacteristicNotification(stripEmgRead_char, true);
                clientConfig01 = stripEmgRead_char.getDescriptor(HelmetProfile.UUID_DESCRIPTOR);
                clientConfig01.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(clientConfig01);
            }else if(stripMode==1){
                stripMode = 2;
                gatt.setCharacteristicNotification(stripOnOff_char, true);
                clientConfig01 = stripOnOff_char.getDescriptor(HelmetProfile.UUID_DESCRIPTOR);
                clientConfig01.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(clientConfig01);
            }else{
                stripMode=3;
                gatt.readCharacteristic(stripBattery_char);
            }
        }
    };
    private void prepareReadStrip(BluetoothGatt gatt){
        //턱끈 착용
        stripMode = 0;
        stripOnOff_char = gatt.getService(StripProfile.UUID_STRIP_APPLY_SERVICE).getCharacteristic(StripProfile.STRIP_APPLY_CHARACTERISTIC);
        gatt.setCharacteristicNotification(stripOnOff_char, true);
        BluetoothGattDescriptor clientConfig02 = stripOnOff_char.getDescriptor(HelmetProfile.UUID_DESCRIPTOR);
        clientConfig02.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(clientConfig02);

        //베터리
        stripBattery_char = gatt.getService(StripProfile.UUID_STRIP_BATTERY_SERVICE).getCharacteristic(StripProfile.STRIP_BATTERY_CHARACTERISTIC);
        gatt.setCharacteristicNotification(stripBattery_char, true);

        // 긴급해제
        stripEmgRead_char = mBluetoothGattStrip.getService(StripProfile.UUID_STRIP_EMERGENCY_OFF).getCharacteristic(StripProfile.STRIP_EMERGENCY_OFF_CHARACTERISTIC);

        // 디바이스 찾기
        stripFind_char = gatt.getService(StripProfile.UUID_STRIP_FIND_DEVICE_SERVICE).getCharacteristic(StripProfile.STRIP_FIND_DEVICE_CHARACTERISTIC);

        //지연시간 세팅
        stripConfigWrite_char = gatt.getService(StripProfile.UUID_STRIP_APPLY_SERVICE).getCharacteristic(StripProfile.STRIP_WRITE_CHARACTERISTIC);

        //민감도 세팅
        stripAclConfigWrite_char = gatt.getService(StripProfile.UUID_STRIP_CONFIG_DEVICE_SERVICE).getCharacteristic(StripProfile.STRIP_CONFIG_ACL_CHARACTERISTIC);
    }

    private final BluetoothGattCallback mGattCallbackHelmet = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_HELMET_GATT_CONNECTED;
                mConnectionStateHelmet = STATE_CONNECTED;
                AppVariables.bHelmetConnect = true;
                gatt.discoverServices();
                broadcastHelmetUpdate(intentAction);
                SoundManager.getInstance(getApplicationContext()).play(R.raw.connect, 1f);
                myLog("Connected to Helmet GATT server.");

                long curDate = System.currentTimeMillis();
                if( ((curDate - AppVariables.sendHpDate)/1000) > 60 ){
                    sendHelmetPhoneNumber();
                    AppVariables.sendHpDate = System.currentTimeMillis();
                }

              } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_HELMET_GATT_DISCONNECTED;
                mConnectionStateHelmet = STATE_DISCONNECTED;
                AppVariables.bHelmetConnect = false;
                broadcastHelmetUpdate(intentAction);
                myLog("Disconnected from Helmet GATT server.");
                SoundManager.getInstance(getApplicationContext()).play(R.raw.disconnect, 1f);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                prepareReadHelmet(gatt);
                broadcastHelmetUpdate(ACTION_HELMET_GATT_SERVICES_DISCOVERED);
                myLog("onServicesDiscovered Helmet received: " + status);
            } else {
                myLog("onServicesDiscovered Helmet received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                myLog("Helmet-->onCharacteristicRead" + characteristic.getUuid().toString()+"::" + Integer.toString(status));
                broadcastHelmetUpdate(ACTION_HELMET_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            myLog("Helmet-->onCharacteristicChanged" + characteristic.getUuid().toString());
            broadcastHelmetUpdate(ACTION_HELMET_DATA_AVAILABLE, characteristic);
            gatt.readCharacteristic(helmetBattery_char);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            myLog("Helmet-->onCharacteristicWrite::" + characteristic.getUuid().toString()+"::" + Integer.toString(status));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            myLog("Helmet DescriptorRead-->" + descriptor.getUuid().toString()+"::" + Integer.toString(status));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            myLog("Helmet-->onDescriptorWrite::" + descriptor.getUuid().toString()+"::" + Integer.toString(status));
            gatt.readCharacteristic(helmetBattery_char);
        }
    };

    private void prepareReadHelmet(BluetoothGatt gatt){
        helmetMode = 0;

        // 미세먼지 & 응급
        helmetEmgRead_char = gatt.getService(HelmetProfile.UUID_HELMET_DUST_SERVICE).getCharacteristic(HelmetProfile.HELMET_DUST_READ_CHARACTERISTIC);
        gatt.setCharacteristicNotification(helmetEmgRead_char, true);
        BluetoothGattDescriptor clientConfig02 = helmetEmgRead_char.getDescriptor(HelmetProfile.UUID_DESCRIPTOR);
        clientConfig02.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(clientConfig02);

        //베터리
        helmetBattery_char = gatt.getService(HelmetProfile.UUID_HELMET_BATTERY_SERVICE).getCharacteristic(HelmetProfile.HELMET_BATTERY_CHARACTERISTIC);
        gatt.setCharacteristicNotification(helmetBattery_char, true);

        helmetEmgWrite_char = gatt.getService(HelmetProfile.UUID_HELMET_DUST_SERVICE).getCharacteristic(HelmetProfile.HELMET_DUST_WRITE_CHARACTERISTIC);
    }

    /************************************************************************************************************************
     * 화면 UI
     *************************************************************************************************************************/
    private void sendMessageUpdateUI( String action, final String smode){

        if (isForeground) {
            int smodeConvert = Integer.parseInt(smode);

            //턱끈 착용 감지 설정
            if(AppVariables.Config_Strip_Mode == 0){//하나이상 접촉
                if( (mainChkMode ==0) && (smodeConvert >0)) {
                    SoundManager.getInstance(BleService.this).play(R.raw.strap1, 1f);
                    sendToServerStripState(3,0);
                }else if( (mainChkMode > 0) && (smodeConvert==0)){
                    SoundManager.getInstance(BleService.this).play(R.raw.strap2, 1f);
                    sendToServerStripState(0,0);
                }
                mainChkMode = smodeConvert;
            }else{//예외 시 모든 센서 접촉으로 기본 설정
                if((smodeConvert == 0 && mainChkMode > 0) ||  (smodeConvert == 3 && mainChkMode == 0)){
                    if(smodeConvert==0){
                        sendToServerStripState(smodeConvert,0);
                        SoundManager.getInstance(BleService.this).play(R.raw.strap2, 1f);
                    }else if(smodeConvert==3){
                        sendToServerStripState(smodeConvert,0);
                        SoundManager.getInstance(BleService.this).play(R.raw.strap1, 1f);
                    }
                    mainChkMode = smodeConvert;
                }
            }
        }
        else {
            Intent intent = new Intent(AppVariables.EXTRA_SERVICE_DATA);
            intent.putExtra(action, smode);
            intent.putExtra("action", action);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
            if (result.equals("Y")) {
                resultBuilder = result.split("\\|");
                if (resultBuilder[0].equals("Y")) {
                    Log.i("UPDATE","Strip State");
                }
            }else{
                Log.i("UPDATE", "턱끈 정보를 서버로 전송할 수 없습니다.");
            }


        } catch (Exception e) {
            Log.i("UPDATE",e.toString());
        }


        mainChkMode = mainsMode;
        mainsMode = -1;
    }

    private void broadcastStripUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendMessageUpdateUI(action,"");
    }

    private void broadcastHelmetUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendMessageUpdateUI(action,"");
    }

    private void broadcastStripUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        bExeThread = false;
//        final Intent intent = new Intent(action);

        try {
            byte[] readByte = characteristic.getValue();

            // 턱끈 긴급해제
            if(characteristic.getUuid().equals(StripProfile.STRIP_EMERGENCY_OFF_CHARACTERISTIC))
            {
                if(readByte[0]== (byte)0x00){
                    myLog("STRIP==> 턱끈 긴급해제 들어옴 ");
                    if(AlertDialog.IsShow()) {
                        AlertDialog.Close();
                        sendMessageUpdateUI(AppVariables.EXTRA_SERVICE_EMERGENCY_OFF_STRIP, "0x0000");
                    }
                } else {
                    if(readByte[0]== (byte)0xA1) {
                        myLog("shet==> 턱끈 긴급상황 발생");
                        AlertDialogShow("Y","장비");
                    }
                }
            } else if(characteristic.getUuid().equals(StripProfile.STRIP_APPLY_CHARACTERISTIC)) { //턱끈착용상태
                String smode = "0";
                chkMode = 0;
                if(readByte[0] == (byte)0x01){
                    smode = "1"; //위
                    chkMode = 1;
                }else if(readByte[0] == (byte)0x10){
                    smode ="2"; // 아래
                    chkMode = 2;
                }else if(readByte[0] == (byte)0x11){
                    smode ="3";
                    chkMode = 3;
                }
                AppVariables.sStripApplyMode = smode;
                myLog("STRIP==>턱끈 착용상태 들어옴 " + smode);
                sendMessageUpdateUI(AppVariables.EXTRA_SERVICE_APPLY_INFO_STRIP, smode);
            } else if(characteristic.getUuid().equals(StripProfile.STRIP_BATTERY_CHARACTERISTIC)) {
                int battery_data = readByte[0]  ; //s & 0xff;
                AppVariables.iStripBatteryAmmount = Integer.parseInt(String.format("%d", battery_data));
                //if(AppVariables.iStripBatteryAmmount > 100) AppVariables.iStripBatteryAmmount -= 128;
                Log.d("STRIP==>", "베터리 정보 들어옴 :" + Integer.toString(AppVariables.iStripBatteryAmmount));
                sendMessageUpdateUI(AppVariables.EXTRA_SERVICE_BATTERY_INFO_STRIP,String.format("%d", battery_data));
            }
            bExeThread=true;
        } catch(Exception e) {
            bExeThread=true;
            myLog("STRIP UI Exception==>"+e.toString());
            e.printStackTrace();
        }
    }

    private void broadcastHelmetUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        bExeThread=false;
//        final Intent intent = new Intent(action);

        try {
            byte[] readByte = characteristic.getValue();

            if (characteristic.getUuid().equals(HelmetProfile.HELMET_DUST_READ_CHARACTERISTIC)) {
                byte[] copyData10 = new byte[readByte.length];
                byte[] copyData25 = new byte[readByte.length];
                int data10 = 0;
                int data25 = 0;
                String dataType = "10";

                //긴급신호  즉시
                if(readByte[0] == (byte)0xDA && readByte[1] == (byte)0xA1  && readByte[3]==(byte)0x01){
                    myLog("HELMET==>안전모 긴급신호 즉시");
                    //sendMessageUpdateUI(AppVariables.EXTRA_SERVICE_EMERGENCY_ON_HELMET, "1");
                    AlertDialogShow("Y","낙하");
                //긴급신호 지연
                }else if(readByte[0] == (byte)0xDA && readByte[1] == (byte)0xA1  && readByte[3]==(byte)0x00){
                    myLog("HELMET==>안전모 긴급신호 지연");
                    AlertDialogShow("Y","장비");
                    //sendMessageUpdateUI(AppVariables.EXTRA_SERVICE_EMERGENCY_ON_HELMET, "0");
                //미세먼지 측정
                }else if(readByte[0] == (byte)0xDA) {
                    if (readByte[1] == (byte) 0x10) {

                        System.arraycopy(readByte, 0, copyData10, 0, readByte.length);
                        int length_value10 = readByte[2] & 0xff;
                        data10 = readByte[length_value10 + 2] & 0xFF;

                        myLog("READ BYTE =====>" + readByte[length_value10 + 2] );

                        AppVariables.iHelmetDust10 = data10;
                    } else if (readByte[1] == (byte) 0x25) {

                        System.arraycopy(readByte, 0, copyData25, 0, readByte.length);
                        int length_value25 = readByte[2] & 0xff;
                        data25 = readByte[length_value25 + 2];
                        AppVariables.iHelmetDust25 = data25;
                        dataType = "25";
                    }
                    myLog("HELMET==>DUST 들어옴 : " + Integer.toString(data10) +","+Integer.toString(data25));
                    sendMessageUpdateUI(AppVariables.EXTRA_SERVICE_DUST_INFO_HELMET, dataType);
                }
                //배터리정보
            } else if (characteristic.getUuid().equals(HelmetProfile.HELMET_BATTERY_CHARACTERISTIC)) {
                //int battery_data = readByte[0] ; //& 0xff;
                int battery_data = readByte[0] ; //& 0xff;
                AppVariables.iHelmetBatteryAmount = Integer.parseInt(String.format("%d", battery_data));
                //if(AppVariables.iHelmetBatteryAmount > 100) AppVariables.iHelmetBatteryAmount -= 128;
                myLog("HELMET==>베터리 정보 들어옴 : " + Integer.toString(AppVariables.iHelmetBatteryAmount));
                sendMessageUpdateUI(AppVariables.EXTRA_SERVICE_BATTERY_INFO_HELMET,String.format("%d", battery_data));
            }
            bExeThread=true;
        }
        catch(Exception e)
        {
            bExeThread=true;
            myLog(e.toString());
            e.printStackTrace();
        }
    }

    private void AlertDialogShow(String count, String gubn){
        Intent popupIntent = new Intent(this, AlertDialog.class);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        popupIntent.putExtra("message", "긴급상황발생");
        popupIntent.putExtra("location", "");
        popupIntent.putExtra("gubn", gubn);
        popupIntent.putExtra("sound", "1");
        popupIntent.putExtra("countChk", count);
        popupIntent.putExtra("count", AppVariables.Config_Delay_Time);

        startActivity(popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /************************************************************************************************************************
     * Write data
     *************************************************************************************************************************/
    private boolean writeData(int index, byte[] data)
    {
        boolean bResult = false;
        try{
            BluetoothGattCharacteristic mWrite;
            BluetoothGatt mGatt ;

            if(index==2) {
                mWrite = stripFind_char;
                mGatt = mBluetoothGattStrip;
            }else if(index==3){
                mWrite = stripConfigWrite_char;
                mGatt = mBluetoothGattStrip;
            }
            else if (index==4){
                //민감도
                mWrite = stripAclConfigWrite_char;
                mGatt = mBluetoothGattStrip;
            }
            else {
                mWrite = helmetEmgWrite_char;
                mGatt = mBluetoothGattHelmet;
            }
            byte[] sendData;
            sendData = null;
            boolean result = false;
            if(null != mGatt && mGatt.connect())
            {
                if(mWrite == null){
                    myLog("Write Data : Write gatt characteristic is null");
                } else {
                    int dataLen = data.length;

                    String sendDataG = "";
                    for(int i=0; i<data.length; i++){
                        sendDataG += String.format("%02x", data[i]);
                    }
                    myLog("Write Data : " + sendDataG);
                    sendData = data;

                    try
                    {
                        Thread.sleep(10);
                        mWrite.setValue(sendData);
                        if(index==2) {
                            bResult = mBluetoothGattStrip.writeCharacteristic(mWrite);
                        }else if(index==3){
                            bResult = mBluetoothGattStrip.writeCharacteristic(mWrite);
                        }else if(index==4){
                            bResult = mBluetoothGattStrip.writeCharacteristic(mWrite);
                        }
                        else {
                            bResult = mBluetoothGattHelmet.writeCharacteristic(mWrite);
                        }
                    }
                    catch (Exception e){e.printStackTrace();}
                    result = true;
                }
            }
            else
            {
                myLog("Write Data : Bluetooth gatt is not connected");
            }
        }
        catch (Exception e)
        {
            myLog("Write Data Exception : "+ e.toString());
            e.printStackTrace();
        }

        return bResult;
    }

    public void sendStartWorkCode(byte[] command){
        writeData(1,command);
    }

    public  void  sendXYZWrite(byte[] command){
        writeData(1,command);
    }

    public void sendConfigStrip(byte[] command){
        writeData(3, command);
    }

    public void sendConfigAclStrip(byte[] command){
        writeData(4, command);
    }

    public void sendStopWorkCode(byte[] command){

        //writeData(CHAR_WRITE_HELMET_DUST,command);
    }

    public void sendHelmetPhoneNumber(){
        String sNum = AppVariables.User_Phone_Number.substring(3);
        byte[] bHP = sNum.getBytes();
        byte[] msg = new byte[10];
        msg[0] =  (byte) (0x03);
        for(int i =1 ; i <= 8; i++) msg[i] = bHP[i-1];
        msg[9] = (byte) (0x04);

        writeData(1,msg);
        myLog("------------------------>sendHelmetPhoneNumber");
    }

    public void sendEmergencyOff(){
        byte[] emergency_release = {(byte) 0xda, (byte) 0xa1, (byte) 0x01, (byte) 0x00, (byte) 0x00};
        //writeData(CHAR_WRITE_HELMET_EMERGENCY,emergency_release);
    }

    public void sendDeviceConfig(byte[] command){
        //
    }
    public void sendFindStripCode(){
        byte[] Command = {(byte) 0xA6};
        writeData(2,Command);
    }

    public void sendStartSoundStrip(){
        byte[] Command = {(byte) 0xD0};
        writeData(2,Command);
    }

    public void sendStopSoundStrip(){
        byte[] Command = {(byte) 0xD2};
        writeData(2,Command);
    }

    public void sendFindHelmetCode(){
        byte[] Command = {(byte) 0x06};
        writeData(1,Command);
    }

    /************************************************************************************************************************
     * 기타
     *************************************************************************************************************************/
    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){

        String NOTIFICATION_CHANNEL_ID = getPackageName();
        String channelName = getString(R.string.app_name);
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_service))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(getIntent())
                .build();
        startForeground(NOTI_ID, notification);
    }

    private Notification getNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_service))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(getIntent());

        return builder.build();
    }

    private PendingIntent getIntent() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        return contentIntent;
    }

    public void initPrepare() {

        if (null != mBluetoothGattStrip && null != mBluetoothGattStrip.getService(StripProfile.UUID_STRIP_APPLY_SERVICE))
            prepareReadStrip(mBluetoothGattStrip);

        if (null != mBluetoothGattHelmet && null != mBluetoothGattHelmet.getService(HelmetProfile.UUID_HELMET_DUST_SERVICE))
            prepareReadHelmet(mBluetoothGattHelmet);
    }

}
