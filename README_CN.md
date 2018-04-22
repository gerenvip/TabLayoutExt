# TabLayoutExt
TabLayout-based extensions

# 常规文字类的使用

## layout方式

此种写法，不需要在java中显示的调用setupWithViewPager，等相关绑定title的操作，不需要额外的操作。

```xml
<android.support.v4.view.ViewPager
    android:id="@+id/view_pager"
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toTopOf="parent">

    <com.gerenvip.ui.tablayout.TabLayoutExt
        android:id="@+id/tab_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="top">
        <com.gerenvip.ui.tablayout.TabItemExt
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="菜单1"/>
        <com.gerenvip.ui.tablayout.TabItemExt
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="菜单2"/>
    </com.gerenvip.ui.tablayout.TabLayoutExt>
</android.support.v4.view.ViewPager>
```

## Java方式

如需要动态的去添加Tab，可以按常规官方TabLayout的使用方式setupWithViewPager()，继承FragmentPagerAdapter，
重写getPageTitle()。return自己想要展示的Tab的title即可。

```java

//若不想重写getPageTitle()，可以用如下的方式
mTabLayout.setupWithViewPager(mPager);
mTabLayout.removeAllTabs();
mTabLayout.addTab(mTabLayout.newTab().setText("菜单1"));
mTabLayout.addTab(mTabLayout.newTab().setText("菜单2"));

```

## 特殊效果用法api
```java

// TabLayoutExt.MODE_FIXED：固定模式, 宽度由最宽的一个标签确定，并且所有Tab平分宽度
// TabLayoutExt.MODE_SCROLLABLE：可滚动模式,Tab超出屏幕外,可滚动。Tab可配合setTabMinWidth()设置此Tab最小显示宽度
mTabLayout.setTabMode(TabLayoutExt.MODE_SCROLLABLE);
// TabLayoutExt.STYLE_NORMAL：普通下划线指示器
// TabLayoutExt.STYLE_BLOCK：滑块式指示器，也就是盖在Tab上边，可以为透明色
// TabLayoutExt.STYLE_DRAWABLE：图片指示器
mTabLayout.setIndicatorStyle(TabLayoutExt.STYLE_NORMAL);
// TabLayoutExt.STYLE_DRAWABLE模式下，设置资源Id
mTabLayout.setIndicatorDrawable(R.mipmap.ic_launcher);
// 只会影响STYLE_BLOCK / STYLE_NORMAL模式的高度
mTabLayout.setSelectedTabIndicatorHeight(20);
// TabLayoutExt.TAB_INDICATOR_FILL：默认下划线填充模式（占满整个Tab宽度）
// TabLayoutExt.TAB_INDICATOR_WRAP：wrap下划线填充模式（和Tab内容宽度相同）只会影响STYLE_BLOCK / STYLE_NORMAL模式
mTabLayout.setTabIndicatorMode(TabLayoutExt.TAB_INDICATOR_WRAP);
// 设置指示器的外间距(只在TAB_INDICATOR_WRAP + STYLE_NORMAL共同模式下，生效)
mTabLayout.setTabIndicatorMarginTop(20);
//attr中tabIndicatorCornerRadius设置STYLE_BLOCK / STYLE_NORMAL下的圆角

// 给0位置的Tab设置提示自定义Drawable角标，对应TabLayoutExt.Tips.TYPE_ICON模式
mTabLayout.showTips(0, R.mipmap.ic_launcher);
// 给1位置的Tab设置提示文本消息角标
mTabLayout.showTips(1, TabLayoutExt.Tips.TYPE_MSG, "10");
// 设置index 1的Tab的角标背景颜色
mTabLayout.setTipsBgColor(1, Color.BLACK);
// 设置index 1的Tab的角标文字颜色
mTabLayout.setTipsMsgColor(1,Color.RED);
// 动态取消index 0的Tab显示
mTabLayout.setTipsVisible(0,false);

mTabLayout.setTipsBgColor(2, Color.parseColor("#FFFF00"));
// 动态更换对应Tab
mTabLayout.getTabAt(3).setText("替换Tab文字");
// 将index为4的Tab更换为自定义的layout
mTabLayout.getTabAt(4).setCustomView(R.layout.view_custom);

```

> xml中的相关使用，可以参考attr中的注释

## 简单demo

```java
// 填充Tab中title式的指示器，并在0位置的Tab中增加一个黑色背景，红色title颜色的角标，String为10
mTabLayout.setTabMode(TabLayoutExt.MODE_SCROLLABLE);
mTabLayout.setTabIndicatorMode(TabLayoutExt.TAB_INDICATOR_WRAP);
mTabLayout.setIndicatorStyle(TabLayoutExt.STYLE_NORMAL);

mTabLayout.setSelectedTabIndicatorHeight(20);
mTabLayout.setTabIndicatorMarginTop(10);
mTabLayout.setTabIndicatorMarginBottom(10);

mTabLayout.showTips(0, TabLayoutExt.Tips.TYPE_MSG, "10");
mTabLayout.setTipsBgColor(0, Color.BLACK);
mTabLayout.setTipsMsgColor(0,Color.RED);
```