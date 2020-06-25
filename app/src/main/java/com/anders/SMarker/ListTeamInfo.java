package com.anders.SMarker;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.anders.SMarker.adapter.AdapterTeamList;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.model.TeamList;
import com.anders.SMarker.utils.AlarmDlg;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.BottomNavigationViewHelper;
import com.anders.SMarker.utils.Tools;
import com.anders.SMarker.widget.LineItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ListTeamInfo extends AppCompatActivity {

    private int REFRESH_PERIOD = 3000;

    private View parent_view;

    private RecyclerView recyclerView;
    public static CheckBox teamchk;
    public static AdapterTeamList mAdapter;
    private ActionModeCallback actionModeCallback;
    public static ActionMode actionMode;
    private Toolbar toolbar;
    private static String TAG = "phptest";

    private String mJsonString;
    private ArrayList<TeamList> mArrayList;
    private BottomNavigationView navigation;

    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_list);

        AppVariables.activitySet = ListTeamInfo.this;
        initComponent2();
        parent_view = findViewById(R.id.lyt_parent);
        teamchk = (CheckBox)findViewById(R.id.teamchk);
        mArrayList = new ArrayList<>();

        initToolbar();

        teamListView();
        initComponent();
    }

    private void initTimer() {

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        teamListView();

                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(mJsonString);
                            for(int i=0;i<jsonArray.length();i++){

                                JSONObject item = jsonArray.getJSONObject(i);

                                int user_idx = item.getInt("user_idx");
                                String helmet_state = item.getString("HELMET_STATE");

                                mAdapter.setStateChange(user_idx, helmet_state);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        timer.schedule(timerTask, 0, REFRESH_PERIOD); //Timer 실행
    }

    private void initComponent2() {

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.getMenu().findItem(R.id.bottom_team).setChecked(true);
        if(AppVariables.User_Permission.equals("Y")) {
            navigation.findViewById(R.id.bottom_team).setVisibility(View.VISIBLE);
        }else{
            navigation.findViewById(R.id.bottom_team).setVisibility(View.GONE);
        }
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_main:
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        MainActivity.navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);
                        //onBackPressed();
                        return true;
                    case R.id.bottom_work:
                        Intent intent2 = new Intent(getApplicationContext(), WorkMainActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                    case R.id.bottom_info:
                        Intent intent3 = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent3);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                    case R.id.bottom_message:
                        Intent intent4 = new Intent(getApplicationContext(), MessageListActivity.class);
                        intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent4);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                }
                return false;
            }
        });

    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("팀원관리");
        toolbar.setTitleTextColor(getResources().getColor(R.color.mainColor));
        toolbar.setTitleTextAppearance(getApplicationContext(),R.style.TextAppearance_Subhead_Bold);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this,R.color.grey_60);

    }

    private void listRefresh(){
        try {
            JSONArray jsonArray = new JSONArray(mJsonString);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                int user_idx = item.getInt("user_idx");
                String user_nm = item.getString("user_nm");
                String user_mPhone = item.getString("user_mPhone");
                String helmet_state = item.getString("HELMET_STATE");
                String ut_nm = item.getString("ut_nm");
                String userPhoto = item.getString("userPhoto");
                String user_blood = item.getString("user_blood");
                if(user_blood.length()>0){
                    user_blood =  user_blood+"형";
                }
                TeamList teamData = new TeamList();

                teamData.setUser_idx(user_idx);
                teamData.setUser_nm(user_nm);
                teamData.setUser_mPhone(user_mPhone);
                teamData.setHelmet_state(helmet_state);
                teamData.setUt_nm(ut_nm);
                teamData.setUserimage(userPhoto);
                teamData.setUser_blood(user_blood);

                mAdapter.getItem(i).setHelmet_state(helmet_state);

                mArrayList.add(teamData);

            }

            mAdapter.notifyDataSetChanged();

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LineItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        Log.i("json --------------->>", mJsonString.toString());

        try {
            JSONArray jsonArray = new JSONArray(mJsonString);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                int user_idx = item.getInt("user_idx");
                String user_nm = item.getString("user_nm");
                String user_mPhone = item.getString("user_mPhone");
                String helmet_state = item.getString("HELMET_STATE");
                String ut_nm = item.getString("ut_nm");
                String userPhoto = item.getString("userPhoto");
                String user_blood = item.getString("user_blood");
                if(user_blood.length()>0){
                    user_blood =  user_blood+"형";
                }
                TeamList teamData = new TeamList();

                teamData.setUser_idx(user_idx);
                teamData.setUser_nm(user_nm);
                teamData.setUser_mPhone(user_mPhone);
                teamData.setHelmet_state(helmet_state);
                teamData.setUt_nm(ut_nm);
                teamData.setUserimage(userPhoto);
                teamData.setUser_blood(user_blood);

                mArrayList.add(teamData);

            }

            mAdapter = new AdapterTeamList(this, mArrayList);
            recyclerView.setAdapter(mAdapter);
            teamchk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int count = mAdapter.getItemCount();
                    if(b){
                        for(int i=0;i<count;i++){
                            int user_idx = mAdapter.getItem(i).getUser_idx();
                            enableActionMode(i,user_idx);

                        }
                    }else{
                        mAdapter.clearSelections();
                        actionMode = null;
                    }

                }
            });

            mAdapter.setOnClickListener(new AdapterTeamList.OnClickListener() {
                @Override
                public void onItemClick(View view, TeamList obj, int pos) {
                    if (mAdapter.getSelectedItemCount() > 0) {
                        int user_idx = mAdapter.getItem(pos).getUser_idx();

                        enableActionMode(pos,user_idx);
                    } else {
                        TeamList timeList = mAdapter.getItem(pos);

                    }
                }

                @Override
                public void onItemLongClick(View view, TeamList obj, int pos) {
                    int user_idx = mAdapter.getItem(pos).getUser_idx();
                    enableActionMode(pos,user_idx);
                }
            });

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }
        actionModeCallback = new ActionModeCallback();

        //initTimer();
    }

    private void teamListView()
    {
        ContentValues addData = new ContentValues();
        addData.put("phoneNB", AppVariables.User_Phone_Number);
        addData.put("serverURL", NetworkTask.API_SERVER_ADRESS);
        NetworkTask networkTask = new NetworkTask(NetworkTask.API_TEAM_LIST, addData);

        try {
            String result =  networkTask.execute().get();
            if(!result.isEmpty()){
                mJsonString = result;
            }else{

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void enableActionMode(int position,int user_idx) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position,user_idx);
    }

    private void toggleSelection(int position,int user_idx) {
        mAdapter.toggleSelection(position,user_idx);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            //actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    public static class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_activity_admin, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            int id = item.getItemId();
            if (id == R.id.action_message_send) {
                AlarmDlg.adminShowAlarmDialog(mAdapter);
              //mode.finish();
                return true;
            }


            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
            if(teamchk.isChecked()){
                teamchk.setChecked(false);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey_60));

        MenuItem item = menu.findItem(R.id.btnEmergency);
        Drawable icon = getResources().getDrawable(R.drawable.emergency);
        icon.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item.setIcon(icon);

        MenuItem item_r = menu.findItem(R.id.btnRefresh);
        Drawable icon_r = getResources().getDrawable(R.drawable.ic_refresh);
        icon_r.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item_r.setIcon(icon_r);
        item_r.setVisible(true);

        MenuItem item_set = menu.findItem(R.id.action_settings);
        Drawable icon_set = getResources().getDrawable(R.drawable.popup_setting);
        icon_set.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item_set.setIcon(icon_set);

        if(AppVariables.User_Permission.equals("Y")){
            //MenuItem item_p = menu.findItem(R.id.bottom_team);
            //item_p.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId()== R.id.btnEmergency) {
            AlarmDlg.showAlarmDialog(this, "긴급"); //권한 허용 시 비상 알림 띄우기
        }else if(item.getItemId()==R.id.btnRefresh){
            mArrayList = new ArrayList<>();
            teamListView();
            listRefresh();
        }else if (item.getItemId()== R.id.action_settings){
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (item.getItemId() == R.id.action_message_send) {

            List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
            if(selectedItemPositions.size() == 0){
                Toast.makeText(getApplicationContext(), "메세지를 전송할 팀원을 선택하세요.", Toast.LENGTH_SHORT).show();
            }
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        MainActivity.navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != timer)
            timer.cancel();
    }
}