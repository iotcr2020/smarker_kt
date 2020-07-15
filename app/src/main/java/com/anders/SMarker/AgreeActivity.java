package com.anders.SMarker;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;

import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.utils.AppVariables;

public class AgreeActivity extends AppCompatActivity {

    private final  int PERMISSIONS_REQUEST_READ_PHONE_STATE  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree);

        TextView txtAgree01 = findViewById(R.id.txtAgree01);
        TextView txtAgree02 = findViewById(R.id.txtAgree02);

        Button btnAgree = findViewById(R.id.btnAgree);

        txtAgree01.setMovementMethod(new ScrollingMovementMethod());
        txtAgree02.setMovementMethod(new ScrollingMovementMethod());

        btnAgree.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){

                TextView txtAgreeTitle = findViewById(R.id.txtAgreeTitle);
                CheckBox chkAgree01 = findViewById(R.id.chkAgree01);
                CheckBox chkAgree02 = findViewById(R.id.chkAgree02);

                if(!chkAgree01.isChecked()){
                    Toast.makeText(AgreeActivity.this, "동의에 체크해 주세요", Toast.LENGTH_LONG).show();
                    chkAgree01.requestFocus();
                    return;
                }
                if(!chkAgree02.isChecked()){
                    Toast.makeText(AgreeActivity.this, "동의에 체크해 주세요", Toast.LENGTH_LONG).show();
                    chkAgree02.requestFocus();
                    return;
                }

                updeateUserAgree();

            }
        });
    }

    private void updeateUserAgree(){
        if (AppVariables.User_Phone_Number.length() !=0){
            String[] resultBuilder = null;
            ContentValues addData = new ContentValues();
            addData.put("phoneNB", AppVariables.User_Phone_Number);
            NetworkTask networkTask = new NetworkTask(NetworkTask.API_UPDATE_AGREE, addData);

            try {
                String result = networkTask.execute().get();
                if (result != null && !result.isEmpty()) {
                    resultBuilder = result.split("\\|");
                    if (resultBuilder[0].equals("Y")) {
                        startLoginActivity();
                    } else {
                        showRetryDialog("N");
                    }
                } else {
                    showRetryDialog("EMPTY");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showRetryDialog("EXCEPTION");
            }
        }else{
            showRetryDialog("PHONE");
        }
    }

    private void startLoginActivity(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showRetryDialog(String strError){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_warning);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView tt = findViewById(R.id.txt_warning);
        tt.setText(strError + ":" + AppVariables.User_Phone_Number + ":" + AppVariables.User_Hp_Token);

        Toast.makeText(this, "관리자에게 문의하세요", Toast.LENGTH_LONG).show();

        ((AppCompatButton) dialog.findViewById(R.id.btn_network_dlg_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updeateUserAgree();
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
                finish();
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();

    }
}

