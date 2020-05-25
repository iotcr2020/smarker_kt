package com.anders.SMarker.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class HelmetProfile {
    // 배터리
    public static UUID UUID_HELMET_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static UUID HELMET_BATTERY_CHARACTERISTIC = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    // 응급처리
    //public static UUID UUID_HELMET_EMERGENCY_SERVICE = UUID.fromString("b0218210-900d-ba01-6ead-cc0de151a55e5");
    //public static UUID HELMET_EMERGENCY_READ_CHARACTERISTIC = UUID.fromString("b0216415-900d-ba01-6ead-cc0de151a55e5");
    //public static UUID HELMET_EMERGENCY_WRITE_CHARACTERISTIC = UUID.fromString("b0211468-900d-ba01-6ead-cc0de151a55e5");

    // 미세먼지
    public static UUID UUID_HELMET_DUST_SERVICE = UUID.fromString("B0218210-900D-BA01-6EAD-CC0DE15A55E5");
    public static UUID HELMET_DUST_READ_CHARACTERISTIC = UUID.fromString("b0216415-900d-ba01-6ead-cc0de15a55e5");
    public static UUID HELMET_DUST_WRITE_CHARACTERISTIC = UUID.fromString("b0211468-900d-ba01-6ead-cc0de15a55e5");
                                                                          //"b0211468-900d-ba01-6ead-cc0de15a55e5"

    //
    public static UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //기기설정 b0211468-900d-ba01-6ead-cc0de15a55e5   미세먼지와 동일

    public static BluetoothGattService createBatteryService() {
        BluetoothGattService service = new BluetoothGattService(UUID_HELMET_BATTERY_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic getBatteryInfo = new BluetoothGattCharacteristic(HELMET_BATTERY_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(UUID_DESCRIPTOR,
                BluetoothGattDescriptor.PERMISSION_READ );
        getBatteryInfo.addDescriptor(configDescriptor);

        service.addCharacteristic(getBatteryInfo);

        return service;
    }



}
