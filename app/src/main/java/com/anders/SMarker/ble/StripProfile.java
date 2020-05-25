package com.anders.SMarker.ble;

import java.util.UUID;

public class StripProfile {
    // 배터리
    public static UUID UUID_STRIP_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static UUID STRIP_BATTERY_CHARACTERISTIC = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    // 턱끈 착용여부
    public static UUID UUID_STRIP_APPLY_SERVICE = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
    public static UUID STRIP_APPLY_CHARACTERISTIC = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
    public static UUID STRIP_WRITE_CHARACTERISTIC = UUID.fromString("00001523-1212-efde-1525-785feabcd123");

    // 턱끈 긴급해제신호
    public static UUID UUID_STRIP_EMERGENCY_OFF = UUID.fromString("b0212018-900d-ba01-c612-cc0de15a55e5");
    public static UUID STRIP_EMERGENCY_OFF_CHARACTERISTIC= UUID.fromString("b0210111-900d-ba01-c612-cc0de15a55e5");

    // 턱끈 내기기 찾기
    public static UUID UUID_STRIP_FIND_DEVICE_SERVICE = UUID.fromString("b021107c-900d-ba01-c612-cc0de15a55e5");
    public static UUID STRIP_FIND_DEVICE_CHARACTERISTIC = UUID.fromString("b021e1ec-900d-ba01-c612-cc0de15a55e5");

    // 턱끈 설정
    public static UUID UUID_STRIP_CONFIG_DEVICE_SERVICE = UUID.fromString("B021107c-900D-BA01-C612-CC0DE15A55E5");
    public static UUID STRIP_CONFIG_ACL_CHARACTERISTIC = UUID.fromString("B021e1ec-900D-BA01-C612-CC0DE15A55E5");


    // 기기설정 00001525-1212-efde-1523-0785feabcd123
    /*
    00002a05-0000-1000-8000-00805f9b34fb
    00002a19-0000-1000-8000-00805f9b34fb
    b0210111-900d-ba01-c612-cc0de15a55e5
    00001524-1212-efde-1523-785feabcd123
    b021dc20-900d-ba01-c612-cc0de15a55e5*/
}
