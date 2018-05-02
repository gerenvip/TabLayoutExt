# TabLayoutExt  

## 简介:  
基于 `Android design` 中的 `TabLayout` 扩展    
#### 主要功能:  

##### 1. 指示器扩展
```
1.支持自定义 指示器 的宽，高，左右间距，上下间距
2.指示器支持固定宽度
3.支持两种 指示器模式 TAB_INDICATOR_WRAP(长度由Tab内容宽度觉得) 和 TAB_INDICATOR_FILL(Tab 的宽度决定而非Tab的内容)
4.支持指示器在顶部
5.支持自定义指示器图片
6.支持修改指示器颜色，包括选中和未选中状态
7.支持 指示器样式 STYLE_NONE(不绘制指示器)，STYLE_NORMAL(常规的指示线)，STYLE_BLOCK(滑块样式)，STYLE_DRAWABLE(自定义指示器图片) 
8. 支持指示器圆角

```
##### 2. Tab 扩展
```
1.支持在 MODE_FIXED 模式下 第一个Tab左对齐，最后一个Tab 右对齐
2.支持 Tab Icon 和 Text 竖直布局和水平布局，TAB_ORIENTATION_VERTICAL 和 TAB_ORIENTATION_HORIZONTAL
3.支持自定义 Tab 中 Icon 和Text 的间距
4.支持Tint Icon 图标的颜色
```
##### 3.红点
```
1.支持按位置设置红点样式
2.支持三种红点样式: TYPE_DOT(默认红点，可修改颜色),TYPE_MSG(消息类型),TYPE_ICON(自定义红点图片)
3.支持设置提醒红点色值，消息类型的文字颜色和大小，边框
4.支持非圆形红点 并支持自定义圆角
5.支持设置红点间距
6.支持动态隐藏红点
```
##### 4. `design` 包中的原有功能全部保留下来

**注意变更：** `design` 包中的 `TabItem` 需要替换为 `TabItemExt`  


## 使用方式说明

##### `layout`中捆绑 `ViewPager` 使用

此种用法，不需要在代码中显示的调用 `setupWithViewPager`。

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

## 代码中使用

##### 动态添加 `Tab`

```java
mTabLayout.addTab(mTabLayout.newTab().setText("Menu1"));
mTabLayout.addTab(mTabLayout.newTab().setText("Menu2"));

```

##### 如需捆绑 `ViewPager` 

```java
mTabLayout.setupWithViewPager(mPager);
```

#### 新特性使用方式

```java
//1.修改指示器样式
// TabLayoutExt.STYLE_NORMAL：普通下划线指示器
// TabLayoutExt.STYLE_BLOCK：滑块式指示器
// TabLayoutExt.STYLE_DRAWABLE：自定义图片指示器
mTabLayout.setIndicatorStyle(TabLayoutExt.STYLE_NORMAL);
//attr app:tabIndicatorStyle="normal"

//2.自定义指示器图片
// TabLayoutExt.STYLE_DRAWABLE模式下，设置资源Id
mTabLayout.setIndicatorStyle(TabLayoutExt.STYLE_DRAWABLE);
mTabLayout.setIndicatorDrawable(R.mipmap.ic_indicator_1);
//attr app:tabIndicatorDrawable="@mipmap/ic_indicator_1"

//3.自定义指示器高度
// 只会影响STYLE_BLOCK / STYLE_NORMAL模式的高度
mTabLayout.setSelectedTabIndicatorHeight(20);
//attr app:tabIndicatorHeight="2dp"

//4.修改指示器模式
// TabLayoutExt.TAB_INDICATOR_FILL：默认下划线填充模式（占满整个Tab宽度）
// TabLayoutExt.TAB_INDICATOR_WRAP：wrap下划线填充模式（和Tab内容宽度相同）只会影响STYLE_BLOCK / STYLE_NORMAL模式
mTabLayout.setTabIndicatorMode(TabLayoutExt.TAB_INDICATOR_WRAP);
//attr app:indicatorMode="wrap" //fill

//5.修改指示器的Top间距
//只在TAB_INDICATOR_WRAP + STYLE_NORMAL共同模式下，生效
mTabLayout.setTabIndicatorMarginTop(20);
// attr app:tabIndicatorMarginTop="0dp"

//6.设置指示器的圆角
//attr中tabIndicatorCornerRadius设置STYLE_BLOCK / STYLE_NORMAL 样式下的圆角

//7.固定指示器在顶部
mTabLayout.setIndicatorFixedTop(true);
// attr app:tabIndicatorFixedTop="true"

//8.修改Tab Icon 颜色
mTabLayout.setTabIconTintColors(colorStateList);
// attr app:tabIconTint="#00000000"

//9.Tab 中 icon 和 text 的间距
mTabLayout.setTabTextIconGap(24);
// attr app:tabTextIconGap="8dp"

//10.显示红点
mTabLayout.showTips(0, R.drawable.ic_oval_tips);
mTabLayout.showTips(1, TabLayoutExt.Tips.TYPE_MSG, "10");
mTabLayout.setTipsBgColor(1, Color.parseColor("#FFFF00"));
mTabLayout.setTipsMargin(1, 8, 0);
//隐藏红点
mTabLayout.setTipsVisible(0, false);

```

## 集成方式   

使用 `jcenter`   
```groovy
 implementation 'com.suapp:tablayout_ext:1.0.1'
```
或者使用 `jitpack` 

在 工程目录下 `build.gradle` 中添加   

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
`app module` 中 添加如下依赖   
```groovy
implementation 'com.github.gerenvip:TabLayoutExt:5ffcf5517f'
```