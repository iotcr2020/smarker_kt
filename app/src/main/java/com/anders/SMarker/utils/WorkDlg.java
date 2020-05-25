package com.anders.SMarker.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.anders.SMarker.R;

public class WorkDlg extends AppCompatActivity {

    public static Context mContext=null;
    public static Dialog dialog = null;
    private static DialogInterface.OnDismissListener _listener ;


    private static   Button[] mButton = new Button[4];
    private static  int iWorkMode;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //choi - 작업 위치 dialog

    }

    public static void showWorkDlg(Context con){
        dialog = new Dialog(con);

        mContext = con;

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.dialog_work_select);
        dialog.setCancelable(false); //false이면 바깥 레이어, 뒤로가기 터치 안됨


        mButton[0] = (Button)dialog.findViewById(R.id.btnWork1);
        mButton[1] = (Button)dialog.findViewById(R.id.btnWork2);
        mButton[2] = (Button)dialog.findViewById(R.id.btnWork3);
        mButton[3] = (Button)dialog.findViewById(R.id.btnWork4);

        for(int i = 0 ; i < 4 ; i++)
        {

            // 버튼의 포지션(배열에서의 index)를 태그로 저장
            mButton[i].setTag(i);
            // 클릭 리스너 등록

            mButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 클릭된 뷰를 버튼으로 받아옴
                    Button newButton = (Button) view;
                    int i = 0;

                    // 향상된 for문을 사용, 클릭된 버튼을 찾아냄
                    for(Button tempButton : mButton)
                    {
                        mButton[i].setBackgroundResource(R.drawable.btn_rounded_work);
                        mButton[i].setTextColor(mContext.getResources().getColor(R.color.mainColor));
                        i++;
                        // 클릭된 버튼을 찾았으면
                        if(tempButton == newButton)
                        {
                            int position2 = (Integer)view.getTag();
                            mButton[position2].setBackgroundResource(R.drawable.btn_rounded_work_accent);
                            mButton[position2].setTextColor(mContext.getResources().getColor(R.color.overlay_light_80));

                            if(newButton.getText().equals("통신주")){
                                iWorkMode = 1;
                            }else if(newButton.getText().equals("실내")){
                                iWorkMode = 2;
                            }else if(newButton.getText().equals("옥외")){
                                iWorkMode = 3;
                            }else if(newButton.getText().equals("밀폐 공간")){
                                iWorkMode = 4;
                            }else{
                                iWorkMode = 3;
                            }
                            Log.i("S---------------->", Integer.toString(iWorkMode));

                            if( _listener == null ) {} else {
                                _listener.onDismiss( dialog ) ;
                            }

                            dialog.dismiss();
                        }
                    }
                }
            });

        }

        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        dialog.show();

    }

    public void setOnDismissListener( DialogInterface.OnDismissListener $listener ) {
        _listener = $listener ;
    }

    public static int getWorkMode() {
        return iWorkMode;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
