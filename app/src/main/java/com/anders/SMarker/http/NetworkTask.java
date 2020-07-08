package com.anders.SMarker.http;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkTask extends AsyncTask<Void, Void, String> {
    public static final String API_SERVER_ADRESS ="http://52.78.198.132:8080/ktsmarkerapi";
    public static final String API_CHECK_AGREE = API_SERVER_ADRESS + "/agreecheck";
    public static final String API_UPDATE_AGREE = API_SERVER_ADRESS + "/agree";
    public static final String API_CHECK_PHONE_NUMBER = API_SERVER_ADRESS + "/phone";
    public static final String API_FCM_HPSET = API_SERVER_ADRESS + "/fcm";
    public static final String API_LOG_OFF = API_SERVER_ADRESS + "/logout";
    public static final String API_MAIN_ALERT_RECEIVE = API_SERVER_ADRESS + "/mainmessage";
    public static final String API_UPDATE_USER_STRIP_MAC =API_SERVER_ADRESS + "/stripmac";
    public static final String API_UPDATE_USER_Helmet_MAC =API_SERVER_ADRESS + "/helmetmac";
    public static final String API_INSERT_STRIP_STATE = API_SERVER_ADRESS + "/stripstate";
    public static final String API_UPDATE_START_WORK = API_SERVER_ADRESS + "/startwork";
    public static final String API_UPDATE_STOP_WORK = API_SERVER_ADRESS + "/stopwork";
    public static final String API_MESSAGE_RECEIVE_LIST = API_SERVER_ADRESS + "/messagereceivelist";
    public static final String API_MESSAGE_SEND_LIST = API_SERVER_ADRESS + "/messagesendlist";
    public static final String API_MESSAGE_READ_CHK = API_SERVER_ADRESS + "/messagereadchk";
    public static final String API_ADMIN_ACTION_SEND = API_SERVER_ADRESS + "/adminaction";
    public static final String API_UPDATE_DUST_VALUE = API_SERVER_ADRESS + "/dust";
    public static final String API_TEAM_LIST = API_SERVER_ADRESS + "/userlist";
    public static final String API_ALARM_SEND = API_SERVER_ADRESS +"/useralarm";
    public static final String API_ADMIN_ALARM_SEND = API_SERVER_ADRESS +"/adminalarm";
    public static final String API_IMAGE_UPLOAD_SERVER= API_SERVER_ADRESS + "/photoupload";

    /*public static final String API_SERVER_ADRESS ="https://smarker.co.kr/kt";
    public static final String API_CHECK_AGREE = API_SERVER_ADRESS + "/api/select_use_agree.php";
    public static final String API_UPDATE_AGREE = API_SERVER_ADRESS + "/api/update_use_agree.php";
    public static final String API_CHECK_PHONE_NUMBER = API_SERVER_ADRESS + "/api/check_phone_number.php";
    public static final String API_FCM_HPSET = API_SERVER_ADRESS +"/api/user_hp_token.php";
    public static final String API_LOG_OFF = API_SERVER_ADRESS + "/api/LogOff.php";
    public static final String API_MAIN_ALERT_RECEIVE = API_SERVER_ADRESS + "/api/message_receive_main.php";
    public static final String API_UPDATE_USER_STRIP_MAC =API_SERVER_ADRESS + "/api/UpdateUserStripMac.php";
    public static final String API_UPDATE_USER_Helmet_MAC =API_SERVER_ADRESS + "/api/UpdateUserHelmetMac.php";
    public static final String API_INSERT_STRIP_STATE = API_SERVER_ADRESS + "/api/InsertStripState.php";
    public static final String API_UPDATE_START_WORK = API_SERVER_ADRESS + "/api/update_Start_Work.php";
    public static final String API_UPDATE_STOP_WORK = API_SERVER_ADRESS + "/api/update_Stop_Work.php";
    public static final String API_MESSAGE_RECEIVE_LIST = API_SERVER_ADRESS+"/api/message_receive.php";
    public static final String API_MESSAGE_SEND_LIST = API_SERVER_ADRESS+"/api/message_send.php";
    public static final String API_MESSAGE_READ_CHK = API_SERVER_ADRESS+"/api/message_read_chk.php";
    public static final String API_ADMIN_ACTION_SEND = API_SERVER_ADRESS + "/api/Admin_Action_Send.php";
    public static final String API_UPDATE_DUST_VALUE = API_SERVER_ADRESS + "/api/Update_Dust_Value.php";
    public static final String API_TEAM_LIST = API_SERVER_ADRESS + "/api/select_team_list.php";
    public static final String API_ALARM_SEND = API_SERVER_ADRESS +"/api/user_team_alarm.php";
    public static final String API_ADMIN_ALARM_SEND = API_SERVER_ADRESS +"/api/admin_team_alarm.php";
    public static final String API_IMAGE_UPLOAD_SERVER= API_SERVER_ADRESS + "/user_images/UploadToServer.php";
    public static final String API_IMAGE_DELETE_SERVER = API_SERVER_ADRESS + "/user_images/UploadToServerRemove.php";*/

    public static final String API_OPENWEATHER_KEY ="db1f92d4b15d1b1fe4f6b55dfe7b8f65";

    private String url;
    private ContentValues values;
    private String _method="POST";

    public NetworkTask(String url, ContentValues values) {
        this.url = url;
        this.values = values;
        this._method ="POST";
    }

    public NetworkTask(String url, ContentValues values, String _me) {
        this.url = url;
        this.values = values;
        this._method =_me;
    }

    @Override
    protected String doInBackground(Void... params) {

        String result; // 요청 결과를 저장할 변수.
        RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
        result = requestHttpURLConnection.request(url, values,_method); // 해당 URL로 부터 결과물을 얻어온다.

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        Log.d("결과값===",s+"");
    }
}
