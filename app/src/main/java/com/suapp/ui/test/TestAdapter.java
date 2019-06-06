package com.suapp.ui.test;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangwei on 2018/4/4.
 *         wangwei@jiandaola.com
 */
public class TestAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 6;
    private List<TestFragment> fragments = new ArrayList<>();

    public TestAdapter(FragmentManager fm) {
        super(fm);
        fragments.clear();
        for (int i = 0; i < COUNT; i++) {
            fragments.add(TestFragment.instance("Page: " + (i + 1)));
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        TestFragment fragment = fragments.get(position);
        if (position == 5) {
            return "6---------------";
        }
        if (position == 4) {
            return "5";
        }
        return fragment.getPage();
    }

}
