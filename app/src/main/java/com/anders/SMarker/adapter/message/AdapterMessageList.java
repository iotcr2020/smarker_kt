package com.anders.SMarker.adapter.message;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anders.SMarker.R;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.model.MessageList;
import com.anders.SMarker.utils.AppVariables;

import java.util.ArrayList;
import java.util.List;

public class AdapterMessageList extends RecyclerView.Adapter<AdapterMessageList.ViewHolder> {

    public Context ctx;
    private ArrayList<MessageList> items;

    private SparseBooleanArray selected_items;
    private List<Integer> sendItems = null;
    private Integer curPos = 0;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;

        public TextView message_gubn, message_content, reg_dt, message_co_idx,message_user_nm,message_read_chk,send_receive_chk, action_content;
        public ImageView img_alert,message_chk_img;
        public LinearLayout message_parent,lyo_send;

        public CheckBox read_chk;
        public ViewHolder(View view) {
            super(view);

            message_co_idx = (TextView)view.findViewById(R.id.message_co_idx);
            message_parent = (LinearLayout)view.findViewById(R.id.message_parent);
            lyo_send       = (LinearLayout)view.findViewById(R.id.lyo_send);
            message_user_nm = (TextView)view.findViewById(R.id.message_user_nm);
            img_alert       = (ImageView)view.findViewById(R.id.img_alert);
            message_read_chk = (TextView)view.findViewById(R.id.message_read_chk);
            send_receive_chk =(TextView)view.findViewById(R.id.send_receive_chk);
            message_chk_img     = (ImageView)view.findViewById(R.id.message_chk_img);
           // message_ut_nm = (TextView) view.findViewById(R.id.message_ut_nm);
            message_gubn = (TextView) view.findViewById(R.id.message_gubn);
            message_content = (TextView) view.findViewById(R.id.message_content);
            reg_dt = (TextView) view.findViewById(R.id.reg_dt);
            action_content = (TextView) view.findViewById(R.id.action_content);

            read_chk = (CheckBox) ((Activity)ctx).findViewById(R.id.non_read_chk);
            message_co_idx.setVisibility(View.GONE);
            send_receive_chk.setVisibility(View.GONE);
            message_read_chk.setVisibility(View.GONE);
            mView= view;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            curPos = getAdapterPosition();
            MessageList item = items.get(getAdapterPosition()) ;
            String co_idx = item.getMessage_co_idx();
            ContentValues addData = new ContentValues();
            // 이 부분 핸드폰번호 장치에서 가져온 것으로 수정 hwang
            addData.put("co_idx", co_idx);

            NetworkTask networkTask = new NetworkTask(NetworkTask.API_MESSAGE_READ_CHK, addData);

            try {
                String result =  networkTask.execute().get();
                //if(!result.isEmpty()){
                    dialogPopup(item.getMessage_content(), item.getMessage_gubn(), co_idx, item.getAction_content());

                    CheckBox non_read_chk = (CheckBox) ((Activity)ctx).findViewById(R.id.non_read_chk);
                   // if(non_read_chk.isChecked()){
                        if(item.send_receive_chk.equals("R")) {
                            int position = getAdapterPosition();
                            items.get(position).setRead_chk("Y");
                            if(non_read_chk.isChecked()) {
                                //items.remove(position);
                            }
                            TextView non_message_cnt = (TextView) ((Activity)ctx).findViewById(R.id.non_message_cnt);

                            String cnt = non_message_cnt.getText().toString();
                            int cnt_ = Integer.parseInt(cnt);
                            non_message_cnt.setText(Integer.toString(cnt_-1));
                        }

                    view.findViewById(R.id.message_parent).setBackgroundColor(ctx.getResources().getColor(R.color.white_transparency));
                    view.findViewById(R.id.message_chk_img).setBackground(ctx.getDrawable(R.drawable.bg_multi_selection));


                //}else{

                //}
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    public AdapterMessageList(Context mContext, ArrayList<MessageList> items) {
        this.ctx = mContext;
        this.items = items;
        selected_items = new SparseBooleanArray();

        sendItems= new ArrayList<>(selected_items.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_message, parent, false);
        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MessageList messageList = items.get(position);

        //Toast.makeText(ctx, ""+position,Toast.LENGTH_SHORT).show();
        String message = messageList.message_content;
        String action_message = messageList.action_content;

        if(message.length()>28){

            message = message.substring(0,28)+"...";
        }

        if(action_message!=null) {
            if (action_message.length() > 28) {

                action_message = action_message.substring(0, 28) + "...";
            }
        }

        holder.message_content.setText(message);
        holder.message_gubn.setText(messageList.message_gubn);

        holder.reg_dt.setText(messageList.reg_dt);
        holder.message_co_idx.setText(messageList.message_co_idx);
        holder.message_user_nm.setText(messageList.message_user_nm);
        holder.message_read_chk.setText(messageList.read_chk);
        holder.send_receive_chk.setText(messageList.send_receive_chk);
        holder.action_content.setText(action_message);

        if(holder.send_receive_chk.getText().toString().equals("S")){
            if(messageList.message_user_nm.isEmpty()){
                holder.message_user_nm.setVisibility(View.GONE);
            }

        }

        if(messageList.send_receive_chk.equals("R")) {
            if (messageList.read_chk.equals("N")) {
                holder.message_parent.setBackgroundColor(ctx.getResources().getColor(R.color.blue_grey_200));
                if (messageList.read_chk.equals("Y")) {
                    holder.message_parent.setBackgroundColor(Color.WHITE);
                    holder.message_chk_img.setBackground(ctx.getDrawable(R.drawable.bg_multi_selection));

                }
            }
        }
        if(messageList.message_gubn.equals("긴급")) {
            holder.img_alert.setImageResource(R.drawable.ic_alert);
        }else if(messageList.message_gubn.equals("팀원")){
            holder.img_alert.setImageResource(R.drawable.ic_group);
        }else{
            holder.img_alert.setImageResource(R.drawable.ic_info);
        }





    }
    public void dialogPopup(String contet, String gubn, final String co_idx, String action_message){
        Log.i("~~~~~~~~","dialogPopup");
        final String  act_msg = action_message;
         if(gubn.equals(ctx.getString(R.string.dialogAlaram3)) || gubn.equals("장비") || gubn.equals("낙하") || gubn.equals("긴급")) {
                final Dialog dialog = new Dialog(ctx);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                dialog.setContentView(R.layout.activity_dialog_emergency);
                dialog.setCancelable(true);

                // Build the dialog
                final AlertDialog.Builder mAlertDlgBuilder = new AlertDialog.Builder(ctx);

                LinearLayout linear = (LinearLayout) View.inflate(ctx, R.layout.dialog_alarm_receive, null);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


                final TextView txtEmergency = (TextView) linear.findViewById(R.id.txtEmergency);
                final TextView txtLocation = (TextView) linear.findViewById(R.id.txtLocation);
                final Button btnAlarmOk = (Button) linear.findViewById(R.id.btnAlarmOk);
                final Button btnAdminOk = (Button) linear.findViewById(R.id.btnAdminOk);

                if( act_msg != null) {
                    if (!act_msg.isEmpty()) {
                        btnAdminOk.setText("보기");
                    }
                }

                if (AppVariables.User_Permission.equals("Y")) {
                    btnAdminOk.setVisibility(View.VISIBLE);
                } else {
                    btnAdminOk.setVisibility(View.GONE);
                }

                txtEmergency.setText(contet);
                txtLocation.setText("");
                mAlertDlgBuilder.setCancelable(true);
                mAlertDlgBuilder.setInverseBackgroundForced(true);
                mAlertDlgBuilder.setView(linear);
                final AlertDialog mAlertDialog = mAlertDlgBuilder.create();

                btnAlarmOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDlgBuilder.setCancelable(true);
                        mAlertDialog.dismiss();
                    }
                });

                btnAdminOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAlertDlgBuilder.setCancelable(true);
                        mAlertDialog.dismiss();

                        adminShowAlarmDialog(co_idx, act_msg);
                    }
                });

                mAlertDialog.show();

                mAlertDialog.getWindow().setAttributes(lp);

                /////////////////////////////////////////////////////////////////////////////
        }else if(gubn.equals(ctx.getString(R.string.dialogAlaram1))){
            final Dialog dialog = new Dialog(ctx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
            dialog.setContentView(R.layout.activity_dialog_emergency);

            dialog.setCancelable(true);

            // Build the dialog
            final AlertDialog.Builder mAlertDlgBuilder = new AlertDialog.Builder(ctx);

            LinearLayout linear = (LinearLayout)View.inflate(ctx, R.layout.dialog_info_receive,null );

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            final TextView txtContent = (TextView)linear.findViewById(R.id.txt_dlg_info_content);
            final Button closebtn = (Button)linear.findViewById(R.id.btn_dlg_info_close);

            txtContent.setText(contet);
            mAlertDlgBuilder.setCancelable(true);
            mAlertDlgBuilder.setInverseBackgroundForced(true);
            mAlertDlgBuilder.setView(linear);
            final AlertDialog mAlertDialog = mAlertDlgBuilder.create();

            closebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDlgBuilder.setCancelable(true);
                    mAlertDialog.dismiss();
                }
            });

            mAlertDialog.show();

            mAlertDialog.getWindow().setAttributes(lp);

        }else if(gubn.equals(ctx.getString(R.string.dialogAlaram2))){
            final Dialog dialog = new Dialog(ctx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
            dialog.setContentView(R.layout.activity_dialog_emergency);

            dialog.setCancelable(true);

            // Build the dialog
            final AlertDialog.Builder mAlertDlgBuilder = new AlertDialog.Builder(ctx);

            LinearLayout linear = (LinearLayout)View.inflate(ctx, R.layout.dialog_emergency_receive,null );

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            final TextView txtContent = (TextView)linear.findViewById(R.id.txtContent);
            final Button closebtn = (Button)linear.findViewById(R.id.btnAlarmOk);

            txtContent.setText(contet);
            mAlertDlgBuilder.setCancelable(true);
            mAlertDlgBuilder.setInverseBackgroundForced(true);
            mAlertDlgBuilder.setView(linear);
            final AlertDialog mAlertDialog = mAlertDlgBuilder.create();

            closebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDlgBuilder.setCancelable(true);
                    mAlertDialog.dismiss();
                }
            });

            mAlertDialog.show();

            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    //관리자가 팀원한테 전송
    public void adminShowAlarmDialog(final String co_idx, String msg){

        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_action_send);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final TextView txtActionContent = (TextView)dialog.findViewById(R.id.txtActionContent);


        Button btnActionOk = dialog.findViewById(R.id.btnActionOk);

        if(msg != null && !msg.isEmpty()){
            txtActionContent.setText(msg);
            btnActionOk.setVisibility(View.GONE);
        }

        ((AppCompatButton) dialog.findViewById(R.id.btnActionOk)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(txtActionContent.length() == 0){
                    Toast.makeText(ctx, "메세지를 입력하세요.", Toast.LENGTH_LONG).show();
                }else{
                    ContentValues addData = new ContentValues();

                    addData.put("co_idx",co_idx);
                    addData.put("action_person", AppVariables.User_Idx);
                    addData.put("action_content",txtActionContent.getText().toString());

                    NetworkTask networkTask = new NetworkTask(NetworkTask.API_ADMIN_ACTION_SEND, addData);
                    try {
                        String result = networkTask.execute().get();

                        if(!result.isEmpty()) {

                            MessageList item = items.get(curPos) ;
                            item.setAction_content(txtActionContent.getText().toString());
                            notifyDataSetChanged();

                            dialog.dismiss();
                            Toast.makeText(ctx, "조치 사항을 전송했습니다.", Toast.LENGTH_LONG).show();

                        }else{
                            dialog.dismiss();
                            Toast.makeText(ctx, "조치 전송이 실패했습니다. 다시 전송하세요.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e ) {

                        e.printStackTrace();
                        dialog.dismiss();
                        Toast.makeText(ctx, "조치 전송이 실패했습니다. 다시 전송하세요.", Toast.LENGTH_LONG).show();

                    }
                }


            }
        });

        ((AppCompatButton) dialog.findViewById(R.id.btnActionCancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Integer> getSelectedItems() {

        return sendItems;
    }

}