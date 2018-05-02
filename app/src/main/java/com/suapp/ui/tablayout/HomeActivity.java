package com.suapp.ui.tablayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.gerenvip.ui.tablayout.TabLayoutExt;
import com.suapp.ui.test.TestAdapter;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "HomeActivity";
    private ViewPager mPager;
    private TabLayoutExt mTabLayout, mTabLayout2, mTabLayout3, mTabLayout4, mTabLayout5, mTabLayout6, mTabLayout7;
    private TestAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout2 = findViewById(R.id.tab_layout_2);
        mTabLayout3 = findViewById(R.id.tab_layout_3);
        mTabLayout4 = findViewById(R.id.tab_layout_4);
        mTabLayout5 = findViewById(R.id.tab_layout_5);
        mTabLayout6 = findViewById(R.id.tab_layout_6);
        mTabLayout7 = findViewById(R.id.tab_layout_7);

        mAdapter = new TestAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);

        configTab1();
        configTab2();
        configTab3();
        configTab4();
        configTab5();
        configTab6();
        configTab7();
    }

    private void configTab1() {
        mTabLayout.setFixedTabAlign(false);
        //第 1 个tab 使用默认红点
        mTabLayout.showTips(0, TabLayoutExt.Tips.TYPE_DOT, null);
        //第3个tab使用自定义图片做红点
        mTabLayout.showTips(2, R.drawable.ic_oval_indicator);

        //第2个tab 使用msg
        mTabLayout.showTips(1, TabLayoutExt.Tips.TYPE_MSG, "100");
        mTabLayout.setTipsMsgColor(1, Color.WHITE);
        mTabLayout.setTipsMsgTextSize(1, 10);


        mTabLayout.showTips(3, TabLayoutExt.Tips.TYPE_MSG, "10");
        mTabLayout.setTipsMsgTextSize(3, 8);
        mTabLayout.setTipsLimitCircular(3, false);
        mTabLayout.setTipsCornerRadius(3, 35);
        mTabLayout.setTipsMargin(3, 8, 0);

        int tabCount = mTabLayout.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            switch (i) {
                case 0:
                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_call_black_24dp);
                    break;
                case 1:
                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_chat_black_24dp);
                    break;
                case 2:
                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_perm_identity_black_24dp);
                    break;
                case 3:
                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_settings_black_24dp);
                    break;
            }
        }

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position < 4) {
                    mTabLayout.setScrollPosition(position, 0, true);
                }
            }
        });

        mTabLayout.addOnTabSelectedListener(new TabLayoutExt.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayoutExt.Tab tab) {
                int position = tab.getPosition();
                mPager.setCurrentItem(position, true);
            }

            @Override
            public void onTabUnselected(TabLayoutExt.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayoutExt.Tab tab) {

            }
        });

    }

    private void configTab2() {
        mTabLayout2.setFixedTabAlign(false);
        int tabCount = mTabLayout2.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            switch (i) {
                case 0:
                    mTabLayout2.getTabAt(i).setIcon(R.mipmap.ic_call_black_24dp);
                    break;
                case 1:
                    mTabLayout2.getTabAt(i).setIcon(R.mipmap.ic_chat_black_24dp);
                    break;
                case 2:
                    mTabLayout2.getTabAt(i).setIcon(R.mipmap.ic_perm_identity_black_24dp);
                    break;
            }
        }
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position < 3) {
                    mTabLayout2.setScrollPosition(position, 0, true);
                }
            }
        });

        mTabLayout2.addOnTabSelectedListener(new TabLayoutExt.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayoutExt.Tab tab) {
                int position = tab.getPosition();
                mPager.setCurrentItem(position, true);
            }

            @Override
            public void onTabUnselected(TabLayoutExt.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayoutExt.Tab tab) {

            }
        });

        mTabLayout2.showTips(0, R.drawable.ic_oval_indicator);
        mTabLayout2.setTipsBgColor(0, Color.parseColor("#8B0000"));

        mTabLayout2.showTips(2, R.mipmap.ic_whatshot_black_18dp);
        mTabLayout2.setTipsBgColor(2, Color.RED);

    }

    private void configTab3() {
        mTabLayout3.setupWithViewPager(mPager);
        mTabLayout3.showTips(0, TabLayoutExt.Tips.TYPE_DOT, null);
    }

    private void configTab4() {
        mTabLayout4.setupWithViewPager(mPager);
        mTabLayout4.showTips(1, TabLayoutExt.Tips.TYPE_DOT, null);
    }

    private void configTab5() {
        mTabLayout5.setupWithViewPager(mPager);
        mTabLayout5.showTips(2, TabLayoutExt.Tips.TYPE_DOT, null);
    }

    private void configTab6() {
        mTabLayout6.setupWithViewPager(mPager);
    }

    private void configTab7() {
        mTabLayout7.setupWithViewPager(mPager);
        mTabLayout7.showTips(1, TabLayoutExt.Tips.TYPE_DOT, null);
        mTabLayout7.showTips(3, TabLayoutExt.Tips.TYPE_MSG, "1000");
        mTabLayout7.setTipsMsgTextSize(3, 8);
        mTabLayout7.setTipsLimitCircular(3, false);
        mTabLayout7.setTipsCornerRadius(3, 30);
//        mTabLayout7.setTipsMargin(3, 8, 0);
    }

    public void update() {
        int indicatorMode = mTabLayout3.getTabIndicatorMode();
        int nextMode;
        if (indicatorMode == TabLayoutExt.TAB_INDICATOR_FILL) {
            nextMode = TabLayoutExt.TAB_INDICATOR_WRAP;
        } else {
            nextMode = TabLayoutExt.TAB_INDICATOR_FILL;
        }
        mTabLayout3.setTabIndicatorMode(nextMode);
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }
}
