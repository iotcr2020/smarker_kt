package com.anders.SMarker;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.anders.SMarker.adapter.message.AdapterMessageList;
import com.anders.SMarker.adapter.message.MessageReceiveFragment;
import com.anders.SMarker.adapter.message.MessageSendFragment;
import com.anders.SMarker.adapter.message.MessageTabPagerAdapter;
import com.anders.SMarker.utils.AlarmDlg;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.BottomNavigationViewHelper;
import com.anders.SMarker.utils.Tools;

public class MessageListActivity extends AppCompatActivity {

    private View parent_view;


    private AdapterMessageList mAdapter;
    private ActionMode actionMode;
    private Toolbar toolbar;
    private static String TAG = "phptest";
    private TabLayout tab_layout;
    private ViewPager view_pager;

    private BottomNavigationView navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_tab_list);
        AppVariables.activitySet = MessageListActivity.this;
        initComponent2();
        initToolbar();
        initComponent();

    }

    private void initComponent2() {


        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.getMenu().findItem(R.id.bottom_message).setChecked(true);
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
                    case R.id.bottom_team:
                        Intent intent4 = new Intent(getApplicationContext(), ListTeamInfo.class);
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
        toolbar.setTitle("메세지 리스트");
        toolbar.setTitleTextColor(getResources().getColor(R.color.mainColor));
        toolbar.setTitleTextAppearance(getApplicationContext(),R.style.TextAppearance_Subhead_Bold);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this,R.color.grey_60);

    }
    private void initComponent() {

        view_pager = (ViewPager) findViewById(R.id.view_pager);
        setupViewPager(view_pager);

        tab_layout = (TabLayout) findViewById(R.id.tab_layout);
        tab_layout.setupWithViewPager(view_pager);

        view_pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab_layout));
        tab_layout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                view_pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
    private void setupViewPager(final ViewPager viewPager) {
        MessageTabPagerAdapter adapter = new MessageTabPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(MessageReceiveFragment.newInstance(1), "받은 메시지");
        adapter.addFragment(MessageSendFragment.newInstance(2), "보낸 메시지");
        viewPager.setAdapter(adapter);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey_60));

        MenuItem item = menu.findItem(R.id.btnEmergency);
        Drawable icon = getResources().getDrawable(R.drawable.emergency);
        icon.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item.setIcon(icon);

        MenuItem item_set = menu.findItem(R.id.action_settings);
        Drawable icon_set = getResources().getDrawable(R.drawable.popup_setting);
        icon_set.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item_set.setIcon(icon_set);

        if(AppVariables.User_Permission.equals("Y")){
           // MenuItem item_p = menu.findItem(R.id.bottom_team);
           // item_p.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId()== R.id.btnEmergency){
            AlarmDlg.showAlarmDialog(this,""); //권한 허용 시 비상 알림 띄우기

        }else if (item.getItemId()== R.id.action_settings){
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        MainActivity.navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);
        super.onBackPressed();

    }


}