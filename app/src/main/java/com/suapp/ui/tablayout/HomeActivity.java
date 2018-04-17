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
    private TabLayoutExt mTabLayout;
    private TestAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);
//        mTabLayout.setTabMode(TabLayoutExt.MODE_SCROLLABLE);
        mTabLayout.setFixedTabAlign(false);
//        mTabLayout.setTabIndicatorMode(TabLayoutExt.TAB_INDICATOR_WRAP);
//        mTabLayout.setTabGravity(TabLayoutExt.GRAVITY_CENTER);
//        mTabLayout.setTabOrientation(TabLayoutExt.TAB_ORIENTATION_VERTICAL);
        mTabLayout.setupWithViewPager(mPager);
        mAdapter = new TestAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
//        mTabLayout.setTipsVisible(0, true);
        mTabLayout.showTips(0, R.drawable.ic_oval_tips);
//        mTabLayout.showTips(1, TabLayoutExt.Tips.TYPE_MSG, "10");
//        mTabLayout.setTipsBgColor(1, Color.parseColor("#FFFF00"));
        mTabLayout.setTipsMsgColor(1, Color.WHITE);
        mTabLayout.setTipsLimitCircular(1, false);
        mTabLayout.setTipsMsgTextSize(1, 8);
        mTabLayout.setTipsCornerRadius(1, 35);
        mTabLayout.setTipsMargin(1, 8, 0);

        int tabCount = mTabLayout.getTabCount();
//        for (int i = 0; i < tabCount; i++) {
//            if (tabCount > 4) {
//                mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_launcher_round);
//                continue;
//            }
//            switch (i) {
//                case 0:
//                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_call_black_24dp);
//                    break;
//                case 1:
//                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_chat_black_24dp);
//                    break;
//                case 2:
//                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_perm_identity_black_24dp);
//                    break;
//                case 3:
//                    mTabLayout.getTabAt(i).setIcon(R.mipmap.ic_settings_black_24dp);
//                    break;
//            }
//        }

    }

    public void update() {
        int indicatorMode = mTabLayout.getTabIndicatorMode();
        int nextMode;
        if (indicatorMode == TabLayoutExt.TAB_INDICATOR_FILL) {
            nextMode = TabLayoutExt.TAB_INDICATOR_WRAP;
        } else {
            nextMode = TabLayoutExt.TAB_INDICATOR_FILL;
        }
        mTabLayout.setTabIndicatorMode(nextMode);
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }
}
