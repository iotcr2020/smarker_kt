package com.anders.SMarker.adapter.message;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessageTabPagerAdapter  extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    public static final List<String> mFragmentTitleList = new ArrayList<>();

    int mNumofTabs;

    public MessageTabPagerAdapter(FragmentManager manager) {

        super(manager);


    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                MessageReceiveFragment tab1 = new MessageReceiveFragment();
                return tab1;
            }
            case 1: {
                MessageSendFragment tab2 = new MessageSendFragment();
                return tab2;
            }
            default:{
                return null;
            }

        }

    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
