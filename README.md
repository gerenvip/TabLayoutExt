# TabLayoutExt
[![](https://www.jitpack.io/v/gerenvip/TabLayoutExt.svg)](https://www.jitpack.io/#gerenvip/TabLayoutExt)[ ![Download](https://api.bintray.com/packages/gerenvip/maven/TabLayoutExt/images/download.svg?version=1.0.2) ](https://bintray.com/gerenvip/maven/TabLayoutExt/1.0.2/link)

[中文文档](https://github.com/gerenvip/TabLayoutExt/blob/master/README_CN.md)
## Introduction:
Based on the `TabLayout` extension in `Android design`


 ![](https://github.com/gerenvip/TabLayoutExt/blob/master/demo.gif?raw=true)
#### The main function:
##### 1.Indicator extension
```
1. Support custom indicator width, height, left and right spacing, up and down spacing
2. Indicator supports fixed width
3. Support two kinds of indicator modes TAB_INDICATOR_WRAP (length is determined by Tab content width) and TAB_INDICATOR_FILL (the width of Tab determines the content of Tab instead of Tab)
4. Support indicator at the top
5. Support custom indicator images
6. Support to modify the indicator color, including checked and unchecked
7. Support Indicator style STYLE_NONE (no draw indicator), STYLE_NORMAL (conventional indicator line), STYLE_BLOCK (slider style), STYLE_DRAWABLE (custom indicator picture)
8. Support rounded indicator
```

##### 2.Tab extension
```
1. Support in MODE_FIXED mode, the first tab is left-aligned, and the last tab is right-aligned.
2. Support Tab Icon and Text vertical layout and horizontal layout
3. Support the spacing of Icon and Text in custom Tab
4. Support the color of the Tint Icon icon
```

##### 3.Badge(Red dot)
```
1. Support setting red dot style by position
2. Support three red dot styles: TYPE_DOT (default red dot, can modify color)
3. Support to set the red dot color value, the text color and size of the message type, and the border.
4. Support non-circular red dots and support custom rounded corners
5. Support setting red dot spacing
6. Support dynamic hidden red dot
```

##### 4.The original features in the `design` package are all retained.

**Note changes:** `TabItem` in the `design` package needs to be replaced with `TabItemExt`

When the project uses the Android support 28 compatibility package, the attribute `app:tabMode` and the attribute `app:tabGravity` are incompatible.
Therefore, after the v1.0.2 version and later, the attribute tabMode and the attribute tabGravity are changed to `app:tab_mode` and `app:tab_gravity`.
Please upgrade the code.

## How to use
##### Bundle `ViewPager` in `layout`
For this usage, there is no need to call `setupWithViewPager` in the code.

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
            android:text="Menu1"/>
        <com.gerenvip.ui.tablayout.TabItemExt
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Menu2"/>
    </com.gerenvip.ui.tablayout.TabLayoutExt>
</android.support.v4.view.ViewPager>
```

## Used in code

##### Dynamically add `Tab`

```java
mTabLayout.addTab(mTabLayout.newTab().setText("Menu1"));
mTabLayout.addTab(mTabLayout.newTab().setText("Menu2"));
```
If you need to bundle `ViewPager`

```java
mTabLayout.setupWithViewPager(mPager);
```

#### How to use new features

```java
//1.Modify indicator style
// TabLayoutExt.STYLE_NORMAL：General underline indicator
// TabLayoutExt.STYLE_BLOCK：Slider block indicator
// TabLayoutExt.STYLE_DRAWABLE：Custom picture indicator
mTabLayout.setIndicatorStyle(TabLayoutExt.STYLE_NORMAL);
//attr app:tabIndicatorStyle="normal"

//2.Custom indicator image
// In the mode TabLayoutExt.STYLE_DRAWABLE，set the resource ID
mTabLayout.setIndicatorStyle(TabLayoutExt.STYLE_DRAWABLE);
mTabLayout.setIndicatorDrawable(R.mipmap.ic_indicator_1);
//attr app:tabIndicatorDrawable="@mipmap/ic_indicator_1"

//3.Custom indicator height
// Will only affect the height of the STYLE_BLOCK / STYLE_NORMAL mode
mTabLayout.setSelectedTabIndicatorHeight(20);
//attr app:tabIndicatorHeight="2dp"

//4.Modify indicator mode
// TabLayoutExt.TAB_INDICATOR_FILL：Default underline fill mode (Fill the entire width of the Tab)
// TabLayoutExt.TAB_INDICATOR_WRAP：Wrap underline fill mode (same width as Tab content) only affects STYLE_BLOCK / STYLE_NORMAL mode
mTabLayout.setTabIndicatorMode(TabLayoutExt.TAB_INDICATOR_WRAP);
//attr app:indicatorMode="wrap" //fill

//5.Modify the top spacing of the indicator
//Effective only in the TAB_INDICATOR_WRAP + STYLE_NORMAL common mode
mTabLayout.setTabIndicatorMarginTop(20);
// attr app:tabIndicatorMarginTop="0dp"

//6.Set the rounded corners of the indicator
//The attribute tabIndicatorCornerRadius only takes effect in the STYLE_BLOCK or STYLE_NORMAL style

//7.Fixed indicator at the top
mTabLayout.setIndicatorFixedTop(true);
// attr app:tabIndicatorFixedTop="true"

//8.Modify the Tab Icon color
mTabLayout.setTabIconTintColors(colorStateList);
// attr app:tabIconTint="#00000000"

//9.Modify the spacing of icon and text in the Tab
mTabLayout.setTabTextIconGap(24);
// attr app:tabTextIconGap="8dp"

//10.Display Badge
mTabLayout.showTips(0, R.drawable.ic_oval_tips);
mTabLayout.showTips(1, TabLayoutExt.Tips.TYPE_MSG, "10");
mTabLayout.setTipsBgColor(1, Color.parseColor("#FFFF00"));
mTabLayout.setTipsMargin(1, 8, 0);
//Hidden Badge
mTabLayout.setTipsVisible(0, false);

```

## How to integrate

Use `jcenter`
```groovy
 implementation 'com.suapp:tablayout_ext:1.0.2'
```

Use `jitpack`
Add in the build directory `build.gradle`

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
In `app module` Add the following dependency
```groovy
implementation 'com.github.gerenvip:TabLayoutExt:1.0.1'
```