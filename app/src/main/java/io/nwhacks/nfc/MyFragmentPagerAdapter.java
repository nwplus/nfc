package io.nwhacks.nfc;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rice on 11/30/17.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    List<NFCFragment> fragmentsList;
    public MyFragmentPagerAdapter(FragmentManager fm, List<NFCFragment> fragments) {
        super(fm);

        fragmentsList = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentsList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }
}


