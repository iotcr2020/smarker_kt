package com.anders.SMarker;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.ViewAnimation;
import com.anders.SMarker.utils.Tools;

public class LoginActivity extends AppCompatActivity {

    private View parent_view;
    private final static int LOADING_DURATION = 2500;
    private RecyclerView recyclerView;
    private TextView txtServerErrorMessage;
    private Button btnRetryServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        parent_view = findViewById(android.R.id.content);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtServerErrorMessage = (TextView)findViewById(R.id.txtServerErrorMessage);
        btnRetryServer = (Button)findViewById(R.id.btnRetryServer);

        Tools.setSystemBarColor(this,R.color.mainColor);

        loadingAndDisplayContent();
        if( !serverLoginCheck() ){
            txtServerErrorMessage.setVisibility(View.VISIBLE);
            btnRetryServer.setVisibility(View.VISIBLE);
        }

        btnRetryServer.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                loadingAndDisplayContent();
                serverLoginCheck();
            }
        });
    }

    private boolean serverLoginCheck()
    {
        //if(AppVariables.getConnectivityStatus(getApplicationContext()) != AppVariables.TYPE_NOT_CONNECTED) {
            String[] resultBuilder = null;
            ContentValues addData = new ContentValues();
            // 이 부분 핸드폰번호 장치에서 가져온 것으로 수정 hwang
            addData.put("phoneNB", AppVariables.User_Phone_Number);
            NetworkTask networkTask = new NetworkTask(NetworkTask.API_CHECK_PHONE_NUMBER, addData);

            try {
                String result = networkTask.execute().get();
                if (result != null && !result.isEmpty()) {
                    resultBuilder = result.split("\\|");
                    if (resultBuilder[0].equals("Y")) {
                        // 성공 ConnectActivity
                        startConnectActivity();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        //}else{
            //return false;
        //}
    }

    private void startConnectActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
                startActivity(intent);
                finish();
            }
        }, LOADING_DURATION-1500);
    }

    private void loadingAndDisplayContent() {
        final LinearLayout lyt_progress = (LinearLayout) findViewById(R.id.lyt_progress);
        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);
        recyclerView.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewAnimation.fadeOut(lyt_progress);
            }
        }, LOADING_DURATION);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initComponent();
            }
        }, LOADING_DURATION + 400);
    }

    private void initComponent() {
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }
}
