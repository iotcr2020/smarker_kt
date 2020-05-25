package com.anders.SMarker.adapter.message;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anders.SMarker.R;
import com.anders.SMarker.model.MainAlertList;

import java.util.ArrayList;
import java.util.List;

public class AdapterMainAlertList extends RecyclerView.Adapter<AdapterMainAlertList.ViewHolder> {

    public Context ctx;
    private ArrayList<MainAlertList> items;

    private SparseBooleanArray selected_items;
    private List<Integer> sendItems = null;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        public TextView m_date;

        public ViewHolder(View view) {
            super(view);

            m_date = (TextView)view.findViewById(R.id.m_date);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            MainAlertList item = items.get(getAdapterPosition()) ;

            dialogPopup(item.m_content,item.m_gubn);
        }
    }

    public AdapterMainAlertList(Context mContext, ArrayList<MainAlertList> items) {
        this.ctx = mContext;
        this.items = items;
        selected_items = new SparseBooleanArray();

        sendItems= new ArrayList<>(selected_items.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_alert, parent, false);
        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MainAlertList mainAlertList = items.get(position);


        holder.m_date.setText(mainAlertList.m_date);


    }
    public void dialogPopup(String contet, String gubn){
        if(gubn.equals(ctx.getString(R.string.dialogAlaram3))){
            final Dialog dialog = new Dialog(ctx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
            dialog.setContentView(R.layout.activity_dialog_emergency);

            dialog.setCancelable(true);

            // Build the dialog
            final AlertDialog.Builder mAlertDlgBuilder = new AlertDialog.Builder(ctx);

            LinearLayout linear = (LinearLayout)View.inflate(ctx, R.layout.dialog_alarm_receive,null );

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


            final  TextView txtEmergency = (TextView)linear.findViewById(R.id.txtEmergency);
            final TextView txtLocation  = (TextView)linear.findViewById(R.id.txtLocation);
            final Button btnAlarmOk   = (Button)linear.findViewById(R.id.btnAlarmOk);

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