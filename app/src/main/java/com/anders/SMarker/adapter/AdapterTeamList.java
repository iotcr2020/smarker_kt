package com.anders.SMarker.adapter;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ExifInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.anders.SMarker.R;
import com.anders.SMarker.model.TeamList;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdapterTeamList extends RecyclerView.Adapter<AdapterTeamList.ViewHolder> {

    public Context ctx;
    private ArrayList<TeamList> items;
    private OnClickListener onClickListener = null;

    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;

    private List<Integer> sendItems = null;
    private ExifInterface exifObject;
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView user_idx,user_nm, user_mPhone, ut_nm,user_blood;
        private ImageView userImage,helmet_state;
        public CheckBox teamchk;
        public RelativeLayout lyt_checked, lyt_image;
        public View lyt_parent;

        public ViewHolder(View view) {
            super(view);
            user_idx = (TextView) view.findViewById(R.id.user_idx);
            user_nm = (TextView) view.findViewById(R.id.user_nm);
            user_mPhone = (TextView) view.findViewById(R.id.user_mPhone);
            ut_nm = (TextView) view.findViewById(R.id.ut_nm);
            helmet_state = (ImageView) view.findViewById(R.id.helmet_state);
            userImage = (ImageView) view.findViewById(R.id.image_view);
            lyt_checked = (RelativeLayout) view.findViewById(R.id.lyt_checked);
            lyt_image = (RelativeLayout) view.findViewById(R.id.lyt_image);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);
            user_blood = (TextView)view.findViewById(R.id.user_blood);

        }
    }

    public ArrayList<TeamList> getList() {
        return items;
    }

    public AdapterTeamList(Context mContext, ArrayList<TeamList> items) {
        this.ctx = mContext;
        this.items = items;
        selected_items = new SparseBooleanArray();
        sendItems= new ArrayList<>(selected_items.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teamlist, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final TeamList teamList = items.get(position);

        teamList.setListPosition(position);
        holder.user_idx.setText(String.valueOf(teamList.user_idx));
        holder.user_nm.setText(teamList.user_nm);
        holder.user_mPhone.setText("("+teamList.user_mPhone+")");
        holder.ut_nm.setText(teamList.ut_nm);
        holder.user_blood.setText(teamList.user_blood);
        //holder.helmet_state.setText(teamList.helmet_state);
        if(teamList.helmet_state.equals("0")) {
            holder.helmet_state.setImageResource(R.drawable.ic_connect_off);
        }else{
            holder.helmet_state.setImageResource(R.drawable.ic_connect_on);
        }
        String image=teamList.userimage;

        try {

           // Bitmap bitmap = new LoadImagefromUrl(image).execute().get();
            Glide.clear(holder.itemView);
            Glide.with(ctx).load(Base64.decode(image, Base64.DEFAULT))
                    .placeholder(R.drawable.ic_noimage)
                    .error(R.drawable.ic_noimage)
                    .signature(new StringSignature((UUID.randomUUID().toString())))
                    .into(holder.userImage);
            holder.userImage.setBackground(new ShapeDrawable(new OvalShape()));
            holder.userImage.setClipToOutline(true);

        } catch (Exception e) {
            e.printStackTrace();

        }

        holder.user_idx.setVisibility(View.GONE);

        holder.lyt_parent.setActivated(selected_items.get(position, false));

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener == null) return;
                onClickListener.onItemClick(v, teamList, position);
            }
        });

        holder.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onClickListener == null) return false;
                onClickListener.onItemLongClick(v, teamList, position);
                return true;
            }
        });

        toggleCheckedIcon(holder, position);
       // displayImage(holder, teamList);

    }


    private void toggleCheckedIcon(ViewHolder holder, int position) {
        if (selected_items.get(position, false)) {
            holder.lyt_image.setVisibility(View.GONE);
            holder.lyt_checked.setVisibility(View.VISIBLE);
            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            holder.lyt_checked.setVisibility(View.GONE);
            holder.lyt_image.setVisibility(View.VISIBLE);
            if (current_selected_idx == position) resetCurrentIndex();
        }
    }

    public TeamList getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void toggleSelection(int pos,int user_idx) {
        current_selected_idx = pos;
        if (selected_items.get(pos, false)) {
            selected_items.delete(pos);
            sendItems.remove((Integer)user_idx);

        } else {
            selected_items.put(pos, true);
            sendItems.add(user_idx);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selected_items.clear();
        sendItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selected_items.size();
    }

    public List<Integer> getSelectedItems() {

        return sendItems;
    }

    public String getTeamUserIdx() {
        String user_idx ="";
        for (int i =  getSelectedItems().size() - 1; i >= 0; i--) {
            user_idx += getSelectedItems().get(i) + ",";
        }
       return user_idx;
    }

    public void removeData(int position) {
        items.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }

    public interface OnClickListener {
        void onItemClick(View view, TeamList obj, int pos);
        void onItemLongClick(View view, TeamList obj, int pos);
    }

    public void setStateChange(int idx, String state) {

        for (int i = 0; i <items.size(); i++) {

            if (idx == items.get(i).getUser_idx()) {

                if (!state.equals(items.get(i).getHelmet_state())) {
                    items.get(i).setHelmet_state(state);
                    notifyItemChanged(items.get(i).getListPosition());
                }
            }
        }
    }
}