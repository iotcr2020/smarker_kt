package com.anders.SMarker.fcm;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.util.Log;

import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AppVariables;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    private static String user_hp;
    private final  int PERMISSIONS_REQUEST_READ_PHONE_STATE  = 0;

    SharedPreferences auto;
    SharedPreferences.Editor editor ;
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.

        auto = getSharedPreferences("tokenSetting", Activity.MODE_PRIVATE);
        editor = auto.edit();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if(refreshedToken !=null){
            AppVariables.User_Hp_Token = refreshedToken;
            editor.putString("tcmToken", refreshedToken);
            editor.commit();
        }
        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.

        if(AppVariables.User_Phone_Number.length() > 0 ) {
            sendRegistrationToServer(refreshedToken);
        }
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(String token) {

        user_hp = AppVariables.User_Phone_Number;
        if(user_hp.length() > 0 && token !=null){

            ContentValues addData = new ContentValues();
            addData.put("user_hp_token",token);
            addData.put("user_hp",user_hp);
            NetworkTask networkTask = new NetworkTask(NetworkTask.API_FCM_HPSET, addData);

            try {
                networkTask.execute().get();
            } catch (Exception e) {

                e.printStackTrace();

            }
        }

    }
}
