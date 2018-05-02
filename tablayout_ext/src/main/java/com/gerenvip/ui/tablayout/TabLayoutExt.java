package com.gerenvip.ui.tablayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pools;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.TooltipCompat;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

/**
 * <p> 使用示例:
 * <pre>
 * TabLayoutExt tabLayout = ...;
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
 * </pre>
 * <p>也可以在布局中添加 Tab:</p>
 * <pre>
 * &lt;TabLayoutExt
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_width=&quot;match_parent&quot;&gt;
 *
 *     &lt;TabItemExt
 *             android:text=&quot;@string/tab_text&quot;/&gt;
 *
 *     &lt;TabItemExt
 *             android:icon=&quot;@drawable/ic_android&quot;/&gt;
 *
 * &lt;/TabLayoutExt&gt;
 * </pre>
 * 注意，一旦{@link #setupWithViewPager(ViewPager)} 集成了 {@link ViewPager} 在布局中添加的 Tab 将被清除
 * <p>
 * <h3>ViewPager 集成</h3>
 * <p>
 * 调用 {@link #setupWithViewPager(ViewPager)} 集成
 * 该控件会自动调用{@link PagerAdapter#getPageTitle(int)} 获取 Tab 的 text 文案
 * <p>
 * 也可以在布局中集成:</p>
 * <p>
 * <pre>
 * &lt;android.support.v4.view.ViewPager
 *     android:layout_width=&quot;match_parent&quot;
 *     android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;TabLayoutExt
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_gravity=&quot;top&quot; /&gt;
 *
 * &lt;/android.support.v4.view.ViewPager&gt;
 * </pre>
 *
 * @author wangwei on 2018/4/4.
 *         wangwei@jiandaola.com
 */
@ViewPager.DecorView
public class TabLayoutExt extends HorizontalScrollView {

    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72; // dps
    static final int DEFAULT_GAP_TEXT_ICON = 8; // dps
    private static final int INVALID_WIDTH = -1;
    private static final int DEFAULT_HEIGHT = 48; // dps
    private static final int TAB_MIN_WIDTH_MARGIN = 56; //dps
    static final int FIXED_WRAP_GUTTER_MIN = 16; //dps
    static final int MOTION_NON_ADJACENT_OFFSET = 24;

    private static final int DEFAULT_INDICATOR_MARGIN_4_BLOCK_STYLE = 24;

    private static final int ANIMATION_DURATION = 300;

    private static final Pools.Pool<Tab> sTabPool = new Pools.SynchronizedPool<>(16);

    private static Drawable sDotTipsIcon;

    /**
     * 可滚动标签模式,该模式下，标签可以超出屏幕外,标签除了拥有一个最小宽度<code>@dimen/tab_scrollable_min_width</code>
     * 当超过这个最小宽度后，这个tab 使用真实宽度
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_SCROLLABLE = 0;

    /**
     * 固定标签模式, 该模式下, 标签具有相同的宽度，宽度由最宽的一个标签确定,这些标签会平分宽度
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_FIXED = 1;


    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    /**
     * Tab 指示器宽度和 tabview 相同,同时指示器在 tabView 下面,不再固定在底部,
     * 不过可以通过  R.styleable#TabLayoutExt_tabIndicatorMarginTop 属性 调整与 tabView的间距
     * {@link }
     */
    public static final int TAB_INDICATOR_WRAP = 0;

    public static final int TAB_INDICATOR_FILL = 1;

    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {TAB_INDICATOR_WRAP, TAB_INDICATOR_FILL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicatorMode {
    }

    public static final int TAB_ORIENTATION_HORIZONTAL = 0;
    public static final int TAB_ORIENTATION_VERTICAL = 1;

    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {TAB_ORIENTATION_HORIZONTAL, TAB_ORIENTATION_VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabOrientation {
    }

    /**
     * 横向填充满宽度,当 tab 数比较少时,在{@link #MODE_FIXED} 模式下，tab 会横向填充满宽度
     * 该模式仅仅影响{@link #MODE_FIXED}. 在 {@link #MODE_SCROLLABLE} 模式下 依然保持居左
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_FILL = 0;

    /**
     * 当 tab 数比较少的情况下 tab 的最小宽度并不能铺满整个宽度，使用此模式，会将所有tab 居中。
     * 同样 仅仅影响{@link #MODE_FIXED}.
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_CENTER = 1;

    @RestrictTo(LIBRARY_GROUP)
    @IntDef(flag = true, value = {GRAVITY_FILL, GRAVITY_CENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    public static final int STYLE_NONE = -1;

    /**
     * 指示器默认样式 - 下划线样式
     */
    public static final int STYLE_NORMAL = 0;
    /**
     * 指示器 滑块样式
     */
    public static final int STYLE_BLOCK = 1;
    /**
     * 指示器 自定义 drawable 样式
     */
    public static final int STYLE_DRAWABLE = 2;

    @IntDef(value = {STYLE_NONE, STYLE_NORMAL, STYLE_BLOCK, STYLE_DRAWABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicatorStyle {
    }

    /**
     * MODE_FIXED 模式下，该模式让第一个Tab左对齐，最后一个Tab 右对齐
     */
    private static final boolean MODE_FIXED_ALIGN = true;

    /**
     * Callback interface invoked when a tab's selection state changes.
     */
    public interface OnTabSelectedListener {

        /**
         * Called when a tab enters the selected state.
         *
         * @param tab The tab that was selected
         */
        public void onTabSelected(Tab tab);

        /**
         * Called when a tab exits the selected state.
         *
         * @param tab The tab that was unselected
         */
        public void onTabUnselected(Tab tab);

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category.
         *
         * @param tab The tab that was reselected.
         */
        public void onTabReselected(Tab tab);
    }

    private final ArrayList<Tab> mTabs = new ArrayList<>();
    private Tab mSelectedTab;

    private final SlidingTabStrip mTabStrip;

    int mTabPaddingStart;
    int mTabPaddingTop;
    int mTabPaddingEnd;
    int mTabPaddingBottom;

    int mTabTextAppearance;
    ColorStateList mTabTextColors;
    ColorStateList mTabIconTintColors;
    float mTabTextSize;
    float mTabTextMultiLineSize;

    final int mTabBackgroundResId;

    int mTabMaxWidth = Integer.MAX_VALUE;
    private final int mRequestedTabMinWidth;
    private final int mRequestedTabMaxWidth;
    /**
     * {@link #MODE_SCROLLABLE} 模式下 tab 的最小宽度
     */
    private int mScrollableTabMinWidth;
    /**
     * {@link #MODE_FIXED} 模式下 tab 的最小宽度
     */
    private int mFixedModelTabMinWidth;

    /**
     * 在{@link #STYLE_BLOCK} 样式下 indicator 水平方向margin值
     * 竖直方向 不支持 margin 值,如果要调整 高度，可以调节 指示器高度
     * {@link #setSelectedTabIndicatorHeight(int)}或在xml 中配置
     * <code>TabLayoutExt_tabIndicatorHeight</code>
     */
    private int mIndicatorBlockStyleHorizontalMargin;

    private boolean mIndicatorFixedTop = false;

    private boolean mSetMinWidthFromCode = false;

    private int mContentInsetStart;

    @TabGravity
    int mTabGravity;
    @Mode
    int mMode;
    @IndicatorMode
    int mIndicatorMode;
    @TabOrientation
    int mTabOrientation = TAB_ORIENTATION_VERTICAL;

    @IndicatorStyle
    int mTabIndicatorStyle;

    Drawable mIndicatorDrawable;

    /**
     * {@link #TAB_INDICATOR_WRAP}模式+{@link #STYLE_NORMAL}样式下生效, 指示器距tab view的间距
     */
    int mTabIndicatorMarginTop;

    /**
     * {@link #TAB_INDICATOR_FILL}模式 或{@link #STYLE_DRAWABLE}样式下生效,控制指示器距 tab view 的底部间距
     */
    int mTabIndicatorMarginBottom;

    /**
     * 指示器额外的 padding,修正 indicator 的长度,该值会增加在指示器两端
     */
    private int mIndicatorAdditionalPadding = 0;

    /**
     * Tab 在 {@link #MODE_FIXED} 模式下 设置 左右对齐
     */
    private boolean mTabFixedAlign = !MODE_FIXED_ALIGN;

    private int mTextIconGap;

    /**
     * 是否固定指示器宽度,如果 为 true ,不管 在{@link #TAB_INDICATOR_FILL} 或{@link #TAB_INDICATOR_WRAP}模式下，都生效
     */
    private boolean mTabIndicatorWidthFixed;
    private int mTabIndicatorFixedWidth;

    private float mTabIndicatorCornerRadius;

    private OnTabSelectedListener mSelectedListener;
    private final ArrayList<OnTabSelectedListener> mSelectedListeners = new ArrayList<>();
    private OnTabSelectedListener mCurrentVpSelectedListener;

    private ValueAnimator mScrollAnimator;

    ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private DataSetObserver mPagerAdapterObserver;
    private TabLayoutOnPageChangeListener mPageChangeListener;
    private AdapterChangeListener mAdapterChangeListener;
    private boolean mSetupViewPagerImplicitly;

    // Pool we use as a simple RecyclerBin
    private final Pools.Pool<TabView> mTabViewPool = new Pools.SimplePool<>(12);

    public TabLayoutExt(Context context) {
        this(context, null);
    }

    public TabLayoutExt(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayoutExt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);

        // Add the TabStrip
        mTabStrip = new SlidingTabStrip(context);
        super.addView(mTabStrip, 0, new HorizontalScrollView.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayoutExt,
                defStyleAttr, R.style.TabLayoutExt);

        mTabStrip.setSelectedIndicatorHeight(
                a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabIndicatorHeight, 0));
        mTabStrip.setSelectedIndicatorColor(a.getColor(R.styleable.TabLayoutExt_tabIndicatorColor, 0));

        mTabPaddingStart = mTabPaddingTop = mTabPaddingEnd = mTabPaddingBottom = a
                .getDimensionPixelSize(R.styleable.TabLayoutExt_tabPadding, 0);
        mTabPaddingStart = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabPaddingStart,
                mTabPaddingStart);
        mTabPaddingTop = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabPaddingTop,
                mTabPaddingTop);
        mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabPaddingEnd,
                mTabPaddingEnd);
        mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabPaddingBottom,
                mTabPaddingBottom);

        mTabTextAppearance = a.getResourceId(R.styleable.TabLayoutExt_tabTextAppearance,
                R.style.TextAppearance_TabLayoutExt_Tab);

        // Text colors/sizes come from the text appearance first
        @SuppressLint("CustomViewStyleable") final TypedArray ta = context.obtainStyledAttributes(mTabTextAppearance, R.styleable.TabTextAppearance);

        try {
            mTabTextSize = ta.getDimensionPixelSize(R.styleable.TabTextAppearance_android_textSize, 0);
            mTabTextColors = ta.getColorStateList(R.styleable.TabTextAppearance_android_textColor);
        } finally {
            ta.recycle();
        }

        if (a.hasValue(R.styleable.TabLayoutExt_tabTextColor)) {
            mTabTextColors = a.getColorStateList(R.styleable.TabLayoutExt_tabTextColor);
        }

        if (a.hasValue(R.styleable.TabLayoutExt_tabSelectedTextColor)) {
            final int selected = a.getColor(R.styleable.TabLayoutExt_tabSelectedTextColor, 0);
            mTabTextColors = createColorStateList(mTabTextColors.getDefaultColor(), selected);
        }

        if (a.hasValue(R.styleable.TabLayoutExt_tabIconTint)) {
            mTabIconTintColors = a.getColorStateList(R.styleable.TabLayoutExt_tabIconTint);
        }
        if (a.hasValue(R.styleable.TabLayoutExt_tabSelectedIconTint)) {
            final int selected = a.getColor(R.styleable.TabLayoutExt_tabSelectedIconTint, 0);
            int defaultColor = Color.TRANSPARENT;
            if (mTabIconTintColors != null) {
                defaultColor = mTabIconTintColors.getDefaultColor();
            }
            mTabIconTintColors = createColorStateList(defaultColor, selected);
        }

        mRequestedTabMinWidth = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabMinWidth,
                INVALID_WIDTH);
        mRequestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabMaxWidth,
                INVALID_WIDTH);
        mTabBackgroundResId = a.getResourceId(R.styleable.TabLayoutExt_tabBackground, 0);
        mContentInsetStart = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabContentStart, 0);
        mMode = a.getInt(R.styleable.TabLayoutExt_tabMode, MODE_FIXED);
        mTabGravity = a.getInt(R.styleable.TabLayoutExt_tabGravity, GRAVITY_FILL);
        mIndicatorMode = a.getInt(R.styleable.TabLayoutExt_indicatorMode, TAB_INDICATOR_FILL);
        mTabOrientation = a.getInt(R.styleable.TabLayoutExt_tabOrientation, TAB_ORIENTATION_VERTICAL);
        mIndicatorAdditionalPadding = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabIndicatorAdditionalPadding, 0);
        mTabIndicatorMarginTop = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabIndicatorMarginTop, 0);
        mTabIndicatorMarginBottom = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabIndicatorMarginBottom, 0);
        mTextIconGap = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabTextIconGap, dpToPx(DEFAULT_GAP_TEXT_ICON));
        mTabIndicatorWidthFixed = a.getBoolean(R.styleable.TabLayoutExt_tabIndicatorWidthFixed, false);
        mTabIndicatorFixedWidth = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabIndicatorFixedWidth, 0);
        mTabIndicatorCornerRadius = a.getDimension(R.styleable.TabLayoutExt_tabIndicatorCornerRadius, 0);
        mIndicatorDrawable = a.getDrawable(R.styleable.TabLayoutExt_tabIndicatorDrawable);
        setIndicatorDrawable(mIndicatorDrawable);
        mTabIndicatorStyle = a.getInt(R.styleable.TabLayoutExt_tabIndicatorStyle, STYLE_NORMAL);
        mIndicatorBlockStyleHorizontalMargin = a.getDimensionPixelSize(R.styleable.TabLayoutExt_tabIndicatorBlockStyleHorizontalMargin, DEFAULT_INDICATOR_MARGIN_4_BLOCK_STYLE);
        mIndicatorFixedTop = a.getBoolean(R.styleable.TabLayoutExt_tabIndicatorFixedTop, false);
        a.recycle();

        final Resources res = getResources();
        mTabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.tab_text_size_2line);
        mScrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.tab_scrollable_min_width);

        // Now apply the tab mode and gravity
        applyModeAndGravity();

        sDotTipsIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_oval_tips, null);
    }

    /**
     * 自定义指示器图片，需要同时设置{@link #setIndicatorStyle(int)}为{@link #STYLE_DRAWABLE}才生效
     *
     * @param resId indicator drawable resource
     * @see #setIndicatorStyle(int)
     */
    public void setIndicatorDrawable(@DrawableRes int resId) {
        mIndicatorDrawable = ResourcesCompat.getDrawable(getResources(), resId, null);
        updateIndicatorDrawable();
    }

    /**
     * 自定义指示器图片，需要同时设置{@link #setIndicatorStyle(int)}为{@link #STYLE_DRAWABLE}才生效
     *
     * @param drawable indicator drawable
     * @see #setIndicatorStyle(int)
     */
    public void setIndicatorDrawable(@Nullable Drawable drawable) {
        if (drawable != mIndicatorDrawable) {
            mIndicatorDrawable = drawable;
            updateIndicatorDrawable();
        }
    }

    private void updateIndicatorDrawable() {
        postInvalidate();
    }

    /**
     * 是否将指示器固定在顶部
     * <p>
     * 仅仅在 {@link #STYLE_NORMAL} 和 {@link #STYLE_DRAWABLE} 样式下生效
     *
     * @param fixedTop true ，指示器显示在tab 顶部，反之 底部
     */
    public void setIndicatorFixedTop(boolean fixedTop) {
        if (fixedTop != mIndicatorFixedTop) {
            mIndicatorFixedTop = fixedTop;
            postInvalidate();
        }
    }

    /**
     * 设置 指示器样式，目前支持四种样式:
     * <p>
     * {@link #STYLE_NONE}:不显示指示器;
     * <p>
     * 要隐藏还可以通过 {@link #setSelectedTabIndicatorHeight(int)})} 将高度设置为0来实现，不过只在{@link #STYLE_NORMAL} 样式下生效;
     * 建议通过设置 style 为 {@link #STYLE_NONE} 方式 隐藏指示器
     * <p>
     * {@link #STYLE_NORMAL}:默认模式,底部下划线;
     * <p>
     * {@link #STYLE_BLOCK}: 方块样式,覆盖在 Tab上;
     * <p>
     * {@link #STYLE_DRAWABLE}:自定义 指示器图片资源
     *
     * @param style {@link #STYLE_NORMAL},{@link #STYLE_BLOCK},{@link #STYLE_DRAWABLE},{@link #STYLE_NONE}
     */
    public void setIndicatorStyle(@IndicatorStyle int style) {
        if (style != mTabIndicatorStyle) {
            mTabIndicatorStyle = style;
            updateIndicatorDrawable();
        }
    }

    @IndicatorStyle
    public int getIndicatorStyle() {
        return mTabIndicatorStyle;
    }

    /**
     * Tab 在 {@link #MODE_FIXED} 模式下 设置 对齐,即 第一个tab 左对齐，最后一个tab 右对齐
     *
     * @param align
     */
    public void setFixedTabAlign(boolean align) {
        mTabFixedAlign = align;
        updateTabViews(true);
    }

    /**
     * 修改 tab 的 Text 和 Icon 的间距
     *
     * @param gap pixel unit
     */
    public void setTabTextIconGap(int gap) {
        if (gap != mTextIconGap) {
            mTextIconGap = gap;
            updateAllTabs();
            mTabStrip.updateIndicatorPosition();
            mTabStrip.resetIndicatorTopAndBottom();
        }
    }

    /**
     * 设置 指示器的颜色
     *
     * @param color 指示器的颜色 {@link ColorInt}
     * @attr ref R.styleable#TabLayoutExt_tabIndicatorColor
     */
    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        mTabStrip.setSelectedIndicatorColor(color);
    }

    /**
     * 设置 指示器的高度
     *
     * @param height height to use for the indicator in pixels
     * @attr ref R.styleable#TabLayoutExt_tabIndicatorHeight
     */
    public void setSelectedTabIndicatorHeight(int height) {
        mTabStrip.setSelectedIndicatorHeight(height);
    }


    private void updateTabTips(Tab tab) {
        tab.updateView();
    }

    /**
     * 设置 tab 的滚动位置, 一般配合 {@link android.support.v4.view.ViewPager} 使用
     * <p>
     * 调用该方法并不会更新选择的 tab, 仅仅用于绘制
     *
     * @param position           当前滚动位置,一般是 {@link android.support.v4.view.ViewPager} 的 一个 position
     * @param positionOffset     Value from [0, 1) indicating the offset from {@code position}.
     * @param updateSelectedText 是否更新文字的选中状态.
     */
    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        setScrollPosition(position, positionOffset, updateSelectedText, true);
    }

    void setScrollPosition(int position, float positionOffset, boolean updateSelectedText,
                           boolean updateIndicatorPosition) {
        final int roundedPosition = Math.round(position + positionOffset);
        if (roundedPosition < 0 || roundedPosition >= mTabStrip.getChildCount()) {
            return;
        }

        // Set the indicator position, if enabled
        if (updateIndicatorPosition) {
            mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
        }

        // Now update the scroll position, canceling any running animation
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            mScrollAnimator.cancel();
        }
        scrollTo(calculateScrollXForTab(position, positionOffset), 0);

        // Update the 'selected state' view as we scroll, if enabled
        if (updateSelectedText) {
            setSelectedTabView(roundedPosition);
        }
    }

    private float getScrollPosition() {
        return mTabStrip.getIndicatorPosition();
    }

    /**
     * 添加一个新的Tab，新的tab 会被添加到列表的最后
     *
     * @param tab Tab to add
     */
    public void addTab(@NonNull Tab tab) {
        addTab(tab, mTabs.isEmpty());
    }

    /**
     * 在指定位置添加一个新的tab
     *
     * @param tab      The tab to add
     * @param position The new position of the tab
     */
    public void addTab(@NonNull Tab tab, int position) {
        addTab(tab, position, mTabs.isEmpty());
    }

    /**
     * 添加一个新的Tab，新的tab 会被添加到列表的最后.
     *
     * @param tab         Tab to add
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        addTab(tab, mTabs.size(), setSelected);
    }

    /**
     * 在指定位置添加一个新的tab.
     *
     * @param tab         The tab to add
     * @param position    The new position of the tab
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayoutExt.");
        }
        configureTab(tab, position);
        addTabView(tab);

        if (setSelected) {
            tab.select();
        }
    }

    private void addTabFromItemView(@NonNull TabItemExt item) {
        final Tab tab = newTab();
        if (item.mText != null) {
            tab.setText(item.mText);
        }
        if (item.mIcon != null) {
            tab.setIcon(item.mIcon);
        }
        if (item.mCustomLayout != 0) {
            tab.setCustomView(item.mCustomLayout);
        }
        if (!TextUtils.isEmpty(item.getContentDescription())) {
            tab.setContentDescription(item.getContentDescription());
        }
        addTab(tab);
    }

    /**
     * @deprecated Use {@link #addOnTabSelectedListener(OnTabSelectedListener)} and
     * {@link #removeOnTabSelectedListener(OnTabSelectedListener)}.
     */
    @Deprecated
    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        if (mSelectedListener != null) {
            removeOnTabSelectedListener(mSelectedListener);
        }
        mSelectedListener = listener;
        if (listener != null) {
            addOnTabSelectedListener(listener);
        }
    }

    /**
     * 添加 {@link OnTabSelectedListener} 监听
     *
     * @param listener listener to add
     * @see {@link #removeOnTabSelectedListener(OnTabSelectedListener)}
     */
    public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener);
        }
    }

    /**
     * 移除给定的 {@link OnTabSelectedListener} 监听
     * {@link #addOnTabSelectedListener(OnTabSelectedListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        mSelectedListeners.remove(listener);
    }

    /**
     * 移除所有的 {@link OnTabSelectedListener}监听
     */
    public void clearOnTabSelectedListeners() {
        mSelectedListeners.clear();
    }

    /**
     * 创建并返回一个 {@link Tab}，然后您就可以将返回的{@link Tab}通过{@link #addTab(Tab)} 或其他相关方法添加
     *
     * @return A new Tab
     * @see #addTab(Tab)
     */
    @NonNull
    public Tab newTab() {
        Tab tab = sTabPool.acquire();
        if (tab == null) {
            tab = new Tab();
        }
        tab.mParent = this;
        tab.mView = createTabView(tab);
        return tab;
    }

    /**
     * 返回当前 tab 个个数
     *
     * @return Tab count
     */
    public int getTabCount() {
        return mTabs.size();
    }

    /**
     * Returns the tab at the specified index.
     */
    @Nullable
    public Tab getTabAt(int index) {
        return (index < 0 || index >= getTabCount()) ? null : mTabs.get(index);
    }

    /**
     * Returns the position of the current selected tab.
     *
     * @return selected tab position, or {@code -1} if there isn't a selected tab.
     */
    public int getSelectedTabPosition() {
        return mSelectedTab != null ? mSelectedTab.getPosition() : -1;
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected
     * and another tab will be selected if present.
     *
     * @param tab The tab to remove
     */
    public void removeTab(Tab tab) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab does not belong to this ");
        }

        removeTabAt(tab.getPosition());
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected
     * and another tab will be selected if present.
     *
     * @param position Position of the tab to remove
     */
    public void removeTabAt(int position) {
        final int selectedTabPosition = mSelectedTab != null ? mSelectedTab.getPosition() : 0;
        removeTabViewAt(position);

        final Tab removedTab = mTabs.remove(position);
        if (removedTab != null) {
            removedTab.reset();
            sTabPool.release(removedTab);
        }

        final int newTabCount = mTabs.size();
        for (int i = position; i < newTabCount; i++) {
            mTabs.get(i).setPosition(i);
        }

        if (selectedTabPosition == position) {
            selectTab(mTabs.isEmpty() ? null : mTabs.get(Math.max(0, position - 1)));
        }
    }

    /**
     * Remove all tabs from the action bar and deselect the current tab.
     */
    public void removeAllTabs() {
        // Remove all the views
        for (int i = mTabStrip.getChildCount() - 1; i >= 0; i--) {
            removeTabViewAt(i);
        }

        for (final Iterator<Tab> i = mTabs.iterator(); i.hasNext(); ) {
            final Tab tab = i.next();
            i.remove();
            tab.reset();
            sTabPool.release(tab);
        }

        mSelectedTab = null;
    }

    /**
     * Set the behavior mode for the Tabs in this layout. The valid input options are:
     * <ul>
     * <li>{@link #MODE_FIXED}: Fixed tabs display all tabs concurrently and are best used
     * with content that benefits from quick pivots between tabs.</li>
     * <li>{@link #MODE_SCROLLABLE}: Scrollable tabs display a subset of tabs at any given moment,
     * and can contain longer tab labels and a larger number of tabs. They are best used for
     * browsing contexts in touch interfaces when users don’t need to directly compare the tab
     * labels. This mode is commonly used with a {@link android.support.v4.view.ViewPager}.</li>
     * </ul>
     *
     * @param mode one of {@link #MODE_FIXED} or {@link #MODE_SCROLLABLE}.
     * @attr ref android.support.design.R.styleable#TabLayout_tabMode
     */
    public void setTabMode(@Mode int mode) {
        if (mode != mMode) {
            mMode = mode;
            applyModeAndGravity();
        }
    }

    /**
     * Returns the current mode used by this {@link TabLayoutExt}.
     *
     * @see #setTabMode(int)
     */
    @Mode
    public int getTabMode() {
        return mMode;
    }

    /**
     * 设置 tab 的 gravity 模式
     * <p>
     * 仅仅影响{@link #MODE_FIXED} {@link #setTabMode(int)}
     *
     * @param gravity one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     * @attr ref R.styleable#TabLayoutExt_tabGravity
     */
    public void setTabGravity(@TabGravity int gravity) {
        if (mTabGravity != gravity) {
            mTabGravity = gravity;
            applyModeAndGravity();
        }
    }

    /**
     * The current gravity used for laying out tabs.
     *
     * @return one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     */
    @TabGravity
    public int getTabGravity() {
        return mTabGravity;
    }

    /**
     * 设置 指示器显示模式
     *
     * @param mode {@link #TAB_INDICATOR_FILL} - default,
     *             {@link #TAB_INDICATOR_WRAP}
     */
    public void setTabIndicatorMode(@IndicatorMode int mode) {
        if (mode != mIndicatorMode) {
            mIndicatorMode = mode;
            applyIndicatorMode();
        }
    }

    @IndicatorMode
    public int getTabIndicatorMode() {
        return mIndicatorMode;
    }

    /**
     * 设置额外的 指示器 padding 值, 修正 指示器长度
     * 设置的额外长度会增加到指示器两端
     * <p>只在{@link #TAB_INDICATOR_WRAP} 模式下生效
     *
     * @param padding
     * @attr ref R.styleable#TabLayoutExt_tabIndicatorAdditionalPadding
     */
    public void setTabIndicatorAdditionalPadding(int padding) {
        mIndicatorAdditionalPadding = padding;
        mTabStrip.updateIndicatorPosition();
    }

    /**
     * 获取当前 tab view 的布局方向
     *
     * @return
     */
    @TabOrientation
    public int getTabOrientation() {
        return mTabOrientation;
    }

    /**
     * 设置 tabView 的布局方向
     *
     * @param orientation
     */
    public void setTabOrientation(@TabOrientation int orientation) {
        if (orientation != mTabOrientation) {
            mTabOrientation = orientation;
            updateTabsOrientation();
            //update indicator position
            mTabStrip.updateIndicatorPosition();
            mTabStrip.resetIndicatorTopAndBottom();
        }
    }

    /**
     * 设置 指示器 的上间距,仅仅在{@link #TAB_INDICATOR_WRAP} + {@link #STYLE_NORMAL}模式下生效
     *
     * @param marginTop
     */
    public void setTabIndicatorMarginTop(int marginTop) {
        if (marginTop != mTabIndicatorMarginTop) {
            mTabIndicatorMarginTop = marginTop;
            mTabStrip.updateIndicatorPosition();
            mTabStrip.resetIndicatorTopAndBottom();
        }
    }

    /**
     * 设置指示器的底部间距
     * 注意 在{@link #STYLE_NORMAL} + {@link #TAB_INDICATOR_WRAP} 模式下 无效
     *
     * @param marginBottom
     */
    public void setTabIndicatorMarginBottom(int marginBottom) {
        if (marginBottom != mTabIndicatorMarginBottom) {
            mTabIndicatorMarginBottom = marginBottom;
            mTabStrip.updateIndicatorPosition();
            mTabStrip.resetIndicatorTopAndBottom();
        }
    }

    /**
     * 在{@link #STYLE_BLOCK} 样式下 indicator 水平方向margin值
     * 竖直方向 不支持 margin 值,如果要调整 高度，可以调节 指示器高度
     * {@link #setSelectedTabIndicatorHeight(int)}或在xml 中配置
     * <code>TabLayoutExt_tabIndicatorHeight</code>
     *
     * @param horizontalMargin pixel unit
     */
    public void setIndicatorHorizontalMargin4BlockStyle(int horizontalMargin) {
        if (horizontalMargin != mIndicatorBlockStyleHorizontalMargin) {
            mIndicatorBlockStyleHorizontalMargin = horizontalMargin;
            postInvalidate();
        }
    }

    /**
     * 设置 tab 的最小宽度
     * 目前仅仅在{@link #MODE_SCROLLABLE} 模式小有效果
     * {@link #MODE_FIXED}模式下 设置 minWidth 没有什么意义
     *
     * @param minWidth piexl unit
     */
    public void setTabMinWidth(int minWidth) {
        mScrollableTabMinWidth = minWidth;
        mFixedModelTabMinWidth = minWidth;
        mSetMinWidthFromCode = true;
    }

    /**
     * 设置 tab 的 字体颜色,包含 normal 状态和 selected 状态
     *
     * @see #getTabTextColors()
     */
    public void setTabTextColors(@Nullable ColorStateList textColor) {
        if (mTabTextColors != textColor) {
            mTabTextColors = textColor;
            updateAllTabs();
        }
    }

    /**
     * Gets the text colors for the different states (normal, selected) used for the tabs.
     */
    @Nullable
    public ColorStateList getTabTextColors() {
        return mTabTextColors;
    }

    /**
     * Sets the text colors for the different states (normal, selected) used for the tabs.
     *
     * @attr ref R.styleable#TabLayoutExt_tabTextColor
     * @attr ref R.styleable#TabLayoutExt_tabSelectedTextColor
     */
    public void setTabTextColors(int normalColor, int selectedColor) {
        setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    /**
     * 为 tab icon着色
     *
     * @param iconTintColor <code>ColorStateList</code>
     */
    public void setTabIconTintColors(@Nullable ColorStateList iconTintColor) {
        if (mTabIconTintColors != iconTintColor) {
            mTabIconTintColors = iconTintColor;
            updateAllTabs();
        }
    }

    /**
     * 为 tab icon着色
     *
     * @param normalColor   <code>ColorInt</code>
     * @param selectedColor <code>ColorInt</code>
     */
    public void setTabIconTintColors(@ColorInt int normalColor, @ColorInt int selectedColor) {
        setTabIconTintColors(createColorStateList(normalColor, selectedColor));
    }

    /**
     * 设置 tab 选中状态下 icon 的颜色
     *
     * @param selectedColor <code>ColorInt</code>
     */
    public void setTabSelectedIconTintColor(@ColorInt int selectedColor) {
        setTabIconTintColors(Color.TRANSPARENT, selectedColor);
    }

    /**
     * 设置提醒,
     * 调用该方法会立即显示tips，如果不需要显示，请调用{@link #setTipsVisible(int, boolean)}
     *
     * @param position 提醒tab位置
     * @param tipsType 提醒类型{@link Tips#TYPE_DOT},{@link Tips#TYPE_MSG},{@link Tips#TYPE_ICON}
     * @param msg      {@link Tips#TYPE_MSG} 类型 需要设置文案
     */
    private void showTips(int position, @Tips.Type int tipsType, @Nullable String msg, @Nullable Drawable icon) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsType(tipsType)
                    .setTipsIcon(icon)
                    .setTipsMsg(msg)
                    .setTipsVisible(true)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置提醒，提醒类型为 自定义icon 方式
     *
     * @param position 提醒tab位置
     * @param resIcon  tips icon
     */
    public void showTips(int position, @DrawableRes int resIcon) {
        showTips(position, Tips.TYPE_ICON, null, ResourcesCompat.getDrawable(getResources(), resIcon, null));
    }

    /**
     * 设置提醒，提醒类型为 自定义icon 方式
     *
     * @param position 提醒tab位置
     * @param tipsIcon tips icon
     */
    public void showTips(int position, Drawable tipsIcon) {
        showTips(position, Tips.TYPE_ICON, null, tipsIcon);
    }

    /**
     * 设置提醒
     *
     * @param position 提醒tab位置
     * @param tipsType 提醒类型{@link Tips#TYPE_DOT},{@link Tips#TYPE_MSG},{@link Tips#TYPE_ICON}
     * @param msg      {@link Tips#TYPE_MSG} 类型 需要设置文案
     */
    public void showTips(int position, @Tips.Type int tipsType, @Nullable String msg) {
        showTips(position, tipsType, msg, null);
    }

    public void setTipsVisible(int position, boolean visible) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsVisible(visible)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置tips 的背景色，注意{@link Tips#TYPE_ICON} 类型下无效
     *
     * @param position 提醒tab位置
     * @param bgColor  tips 背景色
     */
    public void setTipsBgColor(int position, @ColorInt int bgColor) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsBgColor(bgColor)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置 tips msg 的字体颜色，注意 只有在{@link Tips#TYPE_MSG}有效
     *
     * @param position 提醒tab位置
     * @param color    msg 字体颜色
     */
    public void setTipsMsgColor(int position, @ColorInt int color) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsMsgColor(color)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置 tips 的间距
     * <p>
     * margin 未设置保护值，请合理设置，否则可能会超出视图区
     *
     * @param position 提醒tab位置
     * @param top      上间距 可以为负
     * @param left     左间距 可以为负
     */
    public void setTipsMargin(int position, int top, int left) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsMargins(left, top)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置 提醒消息字体大小
     *
     * @param position 提醒tab位置
     * @param size     sp unit
     */
    public void setTipsMsgTextSize(int position, int size) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsMsgTextSize(size)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置 提醒边框宽度,仅在{@link Tips#TYPE_MSG}下有效
     *
     * @param position    提醒tab位置
     * @param strokeWidth pixel unit
     */
    public void setTipsStrokeWidth(int position, int strokeWidth) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsStrokeWidth(strokeWidth)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 是否限定提醒为圆形
     *
     * @param position 提醒tab位置
     * @param limit    boolean
     */
    public void setTipsLimitCircular(int position, boolean limit) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsLimitCircular(limit)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置 提醒 背景圆角 ,仅在{@link Tips#TYPE_MSG}下有效
     *
     * @param position     提醒tab位置
     * @param cornerRadius pixel unit
     */
    public void setTipsCornerRadius(int position, int cornerRadius) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsCornerRadius(cornerRadius)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 设置 提醒边框颜色,仅在{@link Tips#TYPE_MSG}下有效
     *
     * @param position    提醒tab位置
     * @param strokeColor color
     */
    public void setTipsStrokeColor(int position, @ColorInt int strokeColor) {
        int tabCount = getTabCount();
        if (position >= tabCount) {
            throw new IllegalArgumentException("position out of bound of tab size");
        }
        Tab tab = getTabAt(position);
        if (tab != null) {
            tab.setTipsStrokeColor(strokeColor)
                    .notifyUpdateTips(tab);
        }
    }

    /**
     * 关联 ViewPager
     *
     * @param viewPager the ViewPager to link to, or {@code null} to clear any previous link
     */
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        setupWithViewPager(viewPager, true);
    }

    public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
        setupWithViewPager(viewPager, autoRefresh, false);
    }

    private void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh,
                                    boolean implicitSetup) {
        if (mViewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (mPageChangeListener != null) {
                mViewPager.removeOnPageChangeListener(mPageChangeListener);
            }
            if (mAdapterChangeListener != null) {
                mViewPager.removeOnAdapterChangeListener(mAdapterChangeListener);
            }
        }

        if (mCurrentVpSelectedListener != null) {
            // If we already have a tab selected listener for the ViewPager, remove it
            removeOnTabSelectedListener(mCurrentVpSelectedListener);
            mCurrentVpSelectedListener = null;
        }

        if (viewPager != null) {
            mViewPager = viewPager;

            // Add our custom OnPageChangeListener to the ViewPager
            if (mPageChangeListener == null) {
                mPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            mPageChangeListener.reset();
            viewPager.addOnPageChangeListener(mPageChangeListener);

            // Now we'll add a tab selected listener to set ViewPager's current item
            mCurrentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(mCurrentVpSelectedListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, autoRefresh);
            }

            // Add a listener so that we're notified of any adapter changes
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = new AdapterChangeListener();
            }
            mAdapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(mAdapterChangeListener);

            // Now update the scroll position to match the ViewPager's current item
            setScrollPosition(viewPager.getCurrentItem(), 0f, true);
        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            mViewPager = null;
            setPagerAdapter(null, false);
        }

        mSetupViewPagerImplicitly = implicitSetup;
    }

    /**
     * @deprecated Use {@link #setupWithViewPager(ViewPager)} to link a TabLayoutExt with a ViewPager
     * together. When that method is used, the TabLayoutExt will be automatically updated
     * when the {@link PagerAdapter} is changed.
     */
    @Deprecated
    public void setTabsFromPagerAdapter(@Nullable final PagerAdapter adapter) {
        setPagerAdapter(adapter, false);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        // Only delay the pressed state if the tabs can scroll
        return getTabScrollRange() > 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager == null) {
            // If we don't have a ViewPager already, check if our parent is a ViewPager to
            // setup with it automatically
            final ViewParent vp = getParent();
            if (vp instanceof ViewPager) {
                // If we have a ViewPager parent and we've been added as part of its decor, let's
                // assume that we should automatically setup to display any titles
                setupWithViewPager((ViewPager) vp, true, true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mSetupViewPagerImplicitly) {
            // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
            setupWithViewPager(null);
            mSetupViewPagerImplicitly = false;
        }
    }

    private int getTabScrollRange() {
        return Math.max(0, mTabStrip.getWidth() - getWidth() - getPaddingLeft()
                - getPaddingRight());
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (mPagerAdapter != null && mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mPagerAdapter.unregisterDataSetObserver(mPagerAdapterObserver);
        }

        mPagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(mPagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {
        removeAllTabs();

        if (mPagerAdapter != null) {
            final int adapterCount = mPagerAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                addTab(newTab().setText(mPagerAdapter.getPageTitle(i)), false);
            }
            // update tab views for mTabFixedAlign
            updateTabViews(true);

            // Make sure we reflect the currently set ViewPager item
            if (mViewPager != null && adapterCount > 0) {
                final int curItem = mViewPager.getCurrentItem();
                if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
                    selectTab(getTabAt(curItem));
                }
            }
        }
    }

    private void updateAllTabs() {
        for (int i = 0, z = mTabs.size(); i < z; i++) {
            mTabs.get(i).updateView();
        }
    }

    private TabView createTabView(@NonNull final Tab tab) {
        TabView tabView = mTabViewPool != null ? mTabViewPool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }
        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(getTabMinWidth());
        return tabView;
    }

    private void configureTab(Tab tab, int position) {
        tab.setPosition(position);
        mTabs.add(position, tab);

        final int count = mTabs.size();
        for (int i = position + 1; i < count; i++) {
            mTabs.get(i).setPosition(i);
        }
    }

    private void addTabView(Tab tab) {
        final TabView tabView = tab.mView;
        mTabStrip.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
    }

    @Override
    public void addView(View child) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    private void addViewInternal(final View child) {
        if (child instanceof TabItemExt) {
            addTabFromItemView((TabItemExt) child);
        } else {
            throw new IllegalArgumentException("Only TabItemExt instances can be added to TabLayoutExt");
        }
    }

    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp) {
        if (mMode == MODE_FIXED && mTabGravity == GRAVITY_FILL) {
            lp.width = 0;
            lp.weight = 1;
        } else {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.weight = 0;
        }
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If we have a MeasureSpec which allows us to decide our height, try and use the default
        // height
        final int idealHeight = dpToPx(getDefaultHeight()) + getPaddingTop() + getPaddingBottom();
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)),
                        MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, MeasureSpec.EXACTLY);
                break;
        }

        final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            // If we don't have an unspecified width spec, use the given size to calculate
            // the max tab width
            mTabMaxWidth = mRequestedTabMaxWidth > 0
                    ? mRequestedTabMaxWidth
                    : specWidth - dpToPx(TAB_MIN_WIDTH_MARGIN);
        }

        // Now super measure itself using the (possibly) modified height spec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 1) {
            // If we're in fixed mode then we need to make the tab strip is the same width as us
            // so we don't scroll
            final View child = getChildAt(0);
            boolean remeasure = false;

            switch (mMode) {
                case MODE_SCROLLABLE:
                    // We only need to resize the child if it's smaller than us. This is similar
                    // to fillViewport
                    remeasure = child.getMeasuredWidth() < getMeasuredWidth();
                    break;
                case MODE_FIXED:
                    // Resize the child so that it doesn't scroll
                    remeasure = child.getMeasuredWidth() != getMeasuredWidth();
                    break;
            }

            if (remeasure) {
                // Re-measure the child with a widthSpec set to be exactly our measure width
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop()
                        + getPaddingBottom(), child.getLayoutParams().height);
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                        getMeasuredWidth(), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    private void removeTabViewAt(int position) {
        final TabView view = (TabView) mTabStrip.getChildAt(position);
        mTabStrip.removeViewAt(position);
        if (view != null) {
            view.reset();
            mTabViewPool.release(view);
        }
        requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition == Tab.INVALID_POSITION) {
            return;
        }

        if (getWindowToken() == null || !ViewCompat.isLaidOut(this)
                || mTabStrip.childrenNeedLayout()) {
            // If we don't have a window token, or we haven't been laid out yet just draw the new
            // position now
            setScrollPosition(newPosition, 0f, true);
            return;
        }

        final int startScrollX = getScrollX();
        final int targetScrollX = calculateScrollXForTab(newPosition, 0);

        if (startScrollX != targetScrollX) {
            ensureScrollAnimator();

            mScrollAnimator.setIntValues(startScrollX, targetScrollX);
            mScrollAnimator.start();
        }

        // Now animate the indicator
        mTabStrip.animateIndicatorToPosition(newPosition, ANIMATION_DURATION);
    }

    private void ensureScrollAnimator() {
        if (mScrollAnimator == null) {
            mScrollAnimator = new ValueAnimator();
            mScrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            mScrollAnimator.setDuration(ANIMATION_DURATION);
            mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    scrollTo((int) animator.getAnimatedValue(), 0);
                }
            });
        }
    }

    void setScrollAnimatorListener(Animator.AnimatorListener listener) {
        ensureScrollAnimator();
        mScrollAnimator.addListener(listener);
    }

    private void setSelectedTabView(int position) {
        final int tabCount = mTabStrip.getChildCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; i++) {
                final View child = mTabStrip.getChildAt(i);
                child.setSelected(i == position);
            }
        }
    }

    void selectTab(Tab tab) {
        selectTab(tab, true);
    }

    void selectTab(final Tab tab, boolean updateIndicator) {
        final Tab currentTab = mSelectedTab;

        if (currentTab == tab) {
            if (currentTab != null) {
                dispatchTabReselected(tab);
                animateToTab(tab.getPosition());
            }
        } else {
            final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
            if (updateIndicator) {
                if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION)
                        && newPosition != Tab.INVALID_POSITION) {
                    // If we don't currently have a tab, just draw the indicator
                    setScrollPosition(newPosition, 0f, true);
                } else {
                    animateToTab(newPosition);
                }
                if (newPosition != Tab.INVALID_POSITION) {
                    setSelectedTabView(newPosition);
                }
            }
            if (currentTab != null) {
                dispatchTabUnselected(currentTab);
            }
            mSelectedTab = tab;
            if (tab != null) {
                dispatchTabSelected(tab);
            }
        }
    }

    private void dispatchTabSelected(@NonNull final Tab tab) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabSelected(tab);
        }
    }

    private void dispatchTabUnselected(@NonNull final Tab tab) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabUnselected(tab);
        }
    }

    private void dispatchTabReselected(@NonNull final Tab tab) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabReselected(tab);
        }
    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        if (mMode == MODE_SCROLLABLE) {
            final View selectedChild = mTabStrip.getChildAt(position);
            final View nextChild = position + 1 < mTabStrip.getChildCount()
                    ? mTabStrip.getChildAt(position + 1)
                    : null;
            final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
            final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

            // base scroll amount: places center of tab in center of parent
            int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
            // offset amount: fraction of the distance between centers of tabs
            int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);

            return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR)
                    ? scrollBase + scrollOffset
                    : scrollBase - scrollOffset;
        }
        return 0;
    }

    private void updateTabsOrientation() {
        updateAllTabs();
    }

    private void applyIndicatorMode() {
        mTabStrip.changeIndicatorMode();
    }

    private void applyModeAndGravity() {
        int paddingStart = 0;
        if (mMode == MODE_SCROLLABLE) {
            // If we're scrollable, or fixed at start, inset using padding
            paddingStart = Math.max(0, mContentInsetStart - mTabPaddingStart);
        }
        ViewCompat.setPaddingRelative(mTabStrip, paddingStart, 0, 0, 0);

        switch (mMode) {
            case MODE_FIXED:
                mTabStrip.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case MODE_SCROLLABLE:
                mTabStrip.setGravity(GravityCompat.START);
                break;
        }

        updateTabViews(true);
    }

    void updateTabViews(final boolean requestLayout) {
        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            View child = mTabStrip.getChildAt(i);
            child.setMinimumWidth(getTabMinWidth());

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
            updateTabViewLayoutParams(lp);
            //support mTabFixedAlign
            if (mMode == MODE_FIXED) {
                TabView tabView = (TabView) child;
                if (mTabFixedAlign) {
                    if (i == 0) {
                        tabView.setGravity(GravityCompat.START | Gravity.CENTER_VERTICAL);
                    }
//                    lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    if (i == mTabStrip.getChildCount() - 1) {
                        tabView.setGravity(GravityCompat.END | Gravity.CENTER_VERTICAL);
                    }
                } else {
                    tabView.setGravity(Gravity.CENTER);
                }
            }
            if (requestLayout) {
                child.requestLayout();
            }
        }
    }

    /**
     * Tab 上的提醒
     */
    public static final class Tips {
        /**
         * 红点
         */
        public static final int TYPE_DOT = 0;
        /**
         * 文本消息
         */
        public static final int TYPE_MSG = 1;
        /**
         * 图片,例如 Hot 图片
         */
        public static final int TYPE_ICON = 2;

        @IntDef(value = {TYPE_DOT, TYPE_MSG, TYPE_ICON})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Type {
        }

        int BG_COLOR_NO_SET = Integer.MAX_VALUE;

        /**
         * 可以是数字字符串,在 {@link #TYPE_MSG} 模式下有效
         */
        String mMsg;

        /**
         * 在 {@link #TYPE_ICON} 类型下生效
         */
        Drawable mIcon;

        /**
         * 提示背景色，在{@link #TYPE_MSG} 类型下生效
         */
        @ColorInt
        int mBgColor = BG_COLOR_NO_SET;
        @ColorInt
        int mMsgColor = BG_COLOR_NO_SET;

        @Type
        int type = TYPE_DOT;

        int mMarginTop;
        int mMarginLeft;

        int mTextSize;

        int mTipsStrokeWidth;

        @ColorInt
        int mTipsStrokeColor = BG_COLOR_NO_SET;

        boolean mLimitCircular = true;

        int mCornerRadius;

        boolean isBgColorAvailable() {
            return mBgColor != BG_COLOR_NO_SET;
        }

        boolean isMsgColorAvailable() {
            return mMsgColor != BG_COLOR_NO_SET;
        }

        boolean isTipsStrokeColorAvailable() {
            return mTipsStrokeColor != BG_COLOR_NO_SET;
        }
    }

    /**
     * A tab in this layout. Instances can be created via {@link #newTab()}.
     */
    public static final class Tab {

        /**
         * An invalid position for a tab.
         *
         * @see #getPosition()
         */
        public static final int INVALID_POSITION = -1;

        private Object mTag;
        private Drawable mIcon;
        private CharSequence mText;
        private CharSequence mContentDesc;
        private int mPosition = INVALID_POSITION;
        private View mCustomView;

        TabLayoutExt mParent;
        TabView mView;

        private Tips mTips;
        private boolean mTipsVisible;

        Tab() {
            // Private constructor
        }

        /**
         * 是否显示提醒
         *
         * @param visible
         */
        public Tab setTipsVisible(boolean visible) {
            if (visible != mTipsVisible) {
                mTipsVisible = visible;
                ensureTips();
            }
            return this;
        }

        /**
         * 是否显示 tips
         *
         * @return boolean
         */
        public boolean isTipsVisible() {
            return mTipsVisible;
        }

        private void notifyUpdateTips(Tab tab) {
            if (mParent != null) {
                mParent.updateTabTips(tab);
            }
        }

        private void ensureTips() {
            if (mTips == null) {
                mTips = new Tips();
            }
        }

        /**
         * 外部设置 提醒对象
         *
         * @param tips {@link Tips}
         */
        Tab setTips(@Nullable Tips tips) {
            mTips = tips;
            return this;
        }

        /**
         * 设置提醒类型
         * <p>
         * 需要调用{@link #notifyUpdateTips(Tab)} 生效
         *
         * @param tipsType
         */
        @NonNull
        Tab setTipsType(@Tips.Type int tipsType) {
            ensureTips();
            if (tipsType != mTips.type) {
                mTips.type = tipsType;
            }
            return this;
        }

        /**
         * 设置提醒消息
         * <p>
         * 需要调用{@link #notifyUpdateTips(Tab)} 生效
         *
         * @param msg
         */
        @NonNull
        Tab setTipsMsg(@Nullable String msg) {
            ensureTips();
            if (!TextUtils.equals(msg, mTips.mMsg)) {
                mTips.mMsg = msg;
            }
            return this;
        }

        /**
         * 设置提醒icon
         * <p>
         * 需要调用{@link #notifyUpdateTips(Tab)} 生效
         *
         * @param resId DrawableRes
         */
        @NonNull
        Tab setTipsIcon(@DrawableRes int resId) {
            ensureTips();
            Drawable icon = ResourcesCompat.getDrawable(mParent.getResources(), resId, null);
            mTips.mIcon = icon;
            return this;
        }

        /**
         * 设置提醒Icon
         * <p>
         * 需要调用{@link #notifyUpdateTips(Tab)} 生效
         *
         * @param icon drawable
         */
        @NonNull
        Tab setTipsIcon(@Nullable Drawable icon) {
            ensureTips();
            if (icon != mTips.mIcon) {
                mTips.mIcon = icon;
            }
            return this;
        }

        /**
         * 设置提醒背景色
         * <p>
         * 需要调用{@link #notifyUpdateTips(Tab)} 生效
         *
         * @param color <code>@ColorInt</code>
         */
        @NonNull
        Tab setTipsBgColor(@ColorInt int color) {
            ensureTips();
            if (color != mTips.mBgColor) {
                mTips.mBgColor = color;
            }
            return this;
        }

        /**
         * 设置提醒消息字体颜色
         *
         * @param color <code>@ColorInt</code>
         */
        Tab setTipsMsgColor(@ColorInt int color) {
            ensureTips();
            if (color != mTips.mMsgColor) {
                mTips.mMsgColor = color;
            }
            return this;
        }

        /**
         * 给 tips 设置间距
         * <p>
         * 需要调用{@link #notifyUpdateTips(Tab)} 生效
         *
         * @param left pixel
         * @param top  pixel
         */
        @NonNull
        Tab setTipsMargins(int left, int top) {
            ensureTips();
            mTips.mMarginLeft = left;
            mTips.mMarginTop = top;
            return this;
        }

        /**
         * 设置 提醒消息字体大小
         *
         * @param size 单位 sp
         */
        Tab setTipsMsgTextSize(int size) {
            ensureTips();
            if (size != mTips.mTextSize) {
                mTips.mTextSize = size;
            }
            return this;
        }

        /**
         * 设置 提醒消息的边框，仅在{@link Tips#TYPE_MSG}下有效
         *
         * @param color <code>@ColorInt</code>
         */
        Tab setTipsStrokeColor(@ColorInt int color) {
            ensureTips();
            if (color != mTips.mTipsStrokeColor) {
                mTips.mTipsStrokeColor = color;
            }
            return this;
        }

        /**
         * 设置 边框宽度
         *
         * @param strokeWidth pixel
         */
        Tab setTipsStrokeWidth(int strokeWidth) {
            ensureTips();
            if (strokeWidth != mTips.mTipsStrokeWidth) {
                mTips.mTipsStrokeWidth = strokeWidth;
            }
            return this;
        }

        /**
         * 限制 提醒消息必须为圆形
         *
         * @param limit true or false
         */
        public Tab setTipsLimitCircular(boolean limit) {
            ensureTips();
            if (limit != mTips.mLimitCircular) {
                mTips.mLimitCircular = limit;
            }
            return this;
        }

        /**
         * 设置圆角
         *
         * @param cornerRadius pixel
         */
        public Tab setTipsCornerRadius(int cornerRadius) {
            ensureTips();
            if (cornerRadius != mTips.mCornerRadius) {
                mTips.mCornerRadius = cornerRadius;
            }
            return this;
        }

        /**
         * @return This Tab's tag object.
         */
        @Nullable
        public Object getTag() {
            return mTag;
        }

        /**
         * Give this Tab an arbitrary object to hold for later use.
         *
         * @param tag Object to store
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setTag(@Nullable Object tag) {
            mTag = tag;
            return this;
        }

        /**
         * Returns the custom view used for this tab.
         *
         * @see #setCustomView(View)
         * @see #setCustomView(int)
         */
        @Nullable
        public View getCustomView() {
            return mCustomView;
        }

        /**
         * Set a custom view to be used for this tab.
         * <p>
         * If the provided view contains a {@link TextView} with an ID of
         * {@link android.R.id#text1} then that will be updated with the value given
         * to {@link #setText(CharSequence)}. Similarly, if this layout contains an
         * {@link ImageView} with ID {@link android.R.id#icon} then it will be updated with
         * the value given to {@link #setIcon(Drawable)}.
         * </p>
         *
         * @param view Custom view to be used as a tab.
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@Nullable View view) {
            mCustomView = view;
            updateView();
            return this;
        }

        /**
         * Set a custom view to be used for this tab.
         * <p>
         * If the inflated layout contains a {@link TextView} with an ID of
         * {@link android.R.id#text1} then that will be updated with the value given
         * to {@link #setText(CharSequence)}. Similarly, if this layout contains an
         * {@link ImageView} with ID {@link android.R.id#icon} then it will be updated with
         * the value given to {@link #setIcon(Drawable)}.
         * </p>
         *
         * @param resId A layout resource to inflate and use as a custom tab view
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@LayoutRes int resId) {
            final LayoutInflater inflater = LayoutInflater.from(mView.getContext());
            return setCustomView(inflater.inflate(resId, mView, false));
        }

        /**
         * 是否是定义 tab view
         *
         * @return
         */
        public boolean isCustomView() {
            return getCustomView() != null;
        }

        /**
         * Return the icon associated with this tab.
         *
         * @return The tab's icon
         */
        @Nullable
        public Drawable getIcon() {
            return mIcon;
        }

        /**
         * Return the current position of this tab in the action bar.
         *
         * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in
         * the action bar.
         */
        public int getPosition() {
            return mPosition;
        }

        void setPosition(int position) {
            mPosition = position;
        }

        /**
         * Return the text of this tab.
         *
         * @return The tab's text
         */
        @Nullable
        public CharSequence getText() {
            return mText;
        }

        /**
         * Set the icon displayed on this tab.
         *
         * @param icon The drawable to use as an icon
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setIcon(@Nullable Drawable icon) {
            mIcon = icon;
            updateView();
            return this;
        }

        /**
         * Set the icon displayed on this tab.
         *
         * @param resId A resource ID referring to the icon that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setIcon(@DrawableRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayoutExt");
            }
            return setIcon(AppCompatResources.getDrawable(mParent.getContext(), resId));
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display
         * the entire string.
         *
         * @param text The text to display
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@Nullable CharSequence text) {
            mText = text;
            updateView();
            return this;
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display
         * the entire string.
         *
         * @param resId A resource ID referring to the text that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@StringRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayoutExt");
            }
            return setText(mParent.getResources().getText(resId));
        }

        /**
         * Select this tab. Only valid if the tab has been added to the action bar.
         */
        public void select() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayoutExt");
            }
            mParent.selectTab(this);
        }

        /**
         * Returns true if this tab is currently selected.
         */
        public boolean isSelected() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayoutExt");
            }
            return mParent.getSelectedTabPosition() == mPosition;
        }

        /**
         * Set a description of this tab's content for use in accessibility support. If no content
         * description is provided the title will be used.
         *
         * @param resId A resource ID referring to the description text
         * @return The current instance for call chaining
         * @see #setContentDescription(CharSequence)
         * @see #getContentDescription()
         */
        @NonNull
        public Tab setContentDescription(@StringRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayoutExt");
            }
            return setContentDescription(mParent.getResources().getText(resId));
        }

        /**
         * Set a description of this tab's content for use in accessibility support. If no content
         * description is provided the title will be used.
         *
         * @param contentDesc Description of this tab's content
         * @return The current instance for call chaining
         * @see #setContentDescription(int)
         * @see #getContentDescription()
         */
        @NonNull
        public Tab setContentDescription(@Nullable CharSequence contentDesc) {
            mContentDesc = contentDesc;
            updateView();
            return this;
        }

        /**
         * Gets a brief description of this tab's content for use in accessibility support.
         *
         * @return Description of this tab's content
         * @see #setContentDescription(CharSequence)
         * @see #setContentDescription(int)
         */
        @Nullable
        public CharSequence getContentDescription() {
            return mContentDesc;
        }

        void updateView() {
            if (mView != null) {
                mView.update();
            }
        }

        void reset() {
            mParent = null;
            mView = null;
            mTag = null;
            mIcon = null;
            mText = null;
            mContentDesc = null;
            mPosition = INVALID_POSITION;
            mCustomView = null;
        }
    }

    class TabView extends LinearLayout {
        private Tab mTab;
        private TextView mTextView;
        private ImageView mIconView;

        private View mCustomView;
        private TextView mCustomTextView;
        private ImageView mCustomIconView;

        private int mDefaultMaxLines = 2;

        private TipsMsgView mTipsMsgView;
        private Drawable mTipsIcon;
        private final Drawable sDotTipsIcon;

        public TabView(Context context) {
            super(context);
            if (mTabBackgroundResId != 0) {
                ViewCompat.setBackground(
                        this, AppCompatResources.getDrawable(context, mTabBackgroundResId));
            }
            ViewCompat.setPaddingRelative(this, mTabPaddingStart, mTabPaddingTop,
                    mTabPaddingEnd, mTabPaddingBottom);
            setGravity(Gravity.CENTER);
            if (mTabOrientation == TAB_ORIENTATION_VERTICAL) {
                setOrientation(VERTICAL);
            } else if (mTabOrientation == TAB_ORIENTATION_HORIZONTAL) {
                setOrientation(HORIZONTAL);
            } else {
                setOrientation(VERTICAL);
            }
            setClickable(true);
            ViewCompat.setPointerIcon(this,
                    PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
            sDotTipsIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_oval_tips, null);
        }

        @Override
        public boolean performClick() {
            final boolean handled = super.performClick();

            if (mTab != null) {
                if (!handled) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                mTab.select();
                return true;
            } else {
                return handled;
            }
        }

        @Override
        public void setSelected(final boolean selected) {
            final boolean changed = isSelected() != selected;

            super.setSelected(selected);

            if (changed && selected && Build.VERSION.SDK_INT < 16) {
                // Pre-JB we need to manually send the TYPE_VIEW_SELECTED event
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            }

            // Always dispatch this to the child views, regardless of whether the value has
            // changed
            if (mTextView != null) {
                mTextView.setSelected(selected);
            }
            if (mIconView != null) {
                mIconView.setSelected(selected);
            }
            if (mCustomView != null) {
                mCustomView.setSelected(selected);
            }
            updateIconTint4Before_LOLLIPOP();
        }

        @Override
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            // This view masquerades as an action bar tab.
            event.setClassName(ActionBar.Tab.class.getName());
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            // This view masquerades as an action bar tab.
            info.setClassName(ActionBar.Tab.class.getName());
        }

        @Override
        public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
            final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
            final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
            final int maxWidth = getTabMaxWidth();

            final int widthMeasureSpec;
            final int heightMeasureSpec = origHeightMeasureSpec;

            if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED
                    || specWidthSize > maxWidth)) {
                // If we have a max width and a given spec which is either unspecified or
                // larger than the max width, update the width spec using the same mode
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mTabMaxWidth, MeasureSpec.AT_MOST);
            } else {
                // Else, use the original width spec
                widthMeasureSpec = origWidthMeasureSpec;
            }

            // Now lets measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // We need to switch the text size based on whether the text is spanning 2 lines or not
            if (mTextView != null) {
                final Resources res = getResources();
                float textSize = mTabTextSize;
                int maxLines = mDefaultMaxLines;

                if (mIconView != null && mIconView.getVisibility() == VISIBLE) {
                    // If the icon view is being displayed, we limit the text to 1 line
                    maxLines = 1;
                } else if (mTextView != null && mTextView.getLineCount() > 1) {
                    // Otherwise when we have text which wraps we reduce the text size
                    textSize = mTabTextMultiLineSize;
                }

                final float curTextSize = mTextView.getTextSize();
                final int curLineCount = mTextView.getLineCount();
                final int curMaxLines = TextViewCompat.getMaxLines(mTextView);

                if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
                    // We've got a new text size and/or max lines...
                    boolean updateTextView = true;

                    if (mMode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
                        // If we're in fixed mode, going up in text size and currently have 1 line
                        // then it's very easy to get into an infinite recursion.
                        // To combat that we check to see if the change in text size
                        // will cause a line count change. If so, abort the size change and stick
                        // to the smaller size.
                        final Layout layout = mTextView.getLayout();
                        if (layout == null || approximateLineWidth(layout, 0, textSize)
                                > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
                            updateTextView = false;
                        }
                    }

                    if (updateTextView) {
                        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        mTextView.setMaxLines(maxLines);
                        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    }
                }
            }
        }

        void setTab(@Nullable final Tab tab) {
            if (tab != mTab) {
                mTab = tab;
                update();
            }
        }

        void reset() {
            setTab(null);
            setSelected(false);
            //restore gravity
            setGravity(Gravity.CENTER);
            //restore orientation
            setOrientation(VERTICAL);
        }

        private void checkUpdateOrientation() {
            int orientation = getOrientation();
            if (mTabOrientation == TAB_ORIENTATION_HORIZONTAL && orientation != HORIZONTAL) {
                setOrientation(HORIZONTAL);
            }
            if (mTabOrientation == TAB_ORIENTATION_VERTICAL && orientation != VERTICAL) {
                setOrientation(VERTICAL);
            }
        }

        final void update() {
            //检测更新布局方向
            checkUpdateOrientation();

            final Tab tab = mTab;
            final View custom = tab != null ? tab.getCustomView() : null;
            if (custom != null) {
                final ViewParent customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        ((ViewGroup) customParent).removeView(custom);
                    }
                    addView(custom);
                }
                mCustomView = custom;
                if (mTextView != null) {
                    mTextView.setVisibility(GONE);
                }
                if (mIconView != null) {
                    mIconView.setVisibility(GONE);
                    mIconView.setImageDrawable(null);
                }

                mCustomTextView = (TextView) custom.findViewById(android.R.id.text1);
                if (mCustomTextView != null) {
                    mDefaultMaxLines = TextViewCompat.getMaxLines(mCustomTextView);
                }
                mCustomIconView = (ImageView) custom.findViewById(android.R.id.icon);
            } else {
                // We do not have a custom view. Remove one if it already exists
                if (mCustomView != null) {
                    removeView(mCustomView);
                    mCustomView = null;
                }
                mCustomTextView = null;
                mCustomIconView = null;
            }

            if (mCustomView == null) {
                // If there isn't a custom view, we'll us our own in-built layouts
                if (mIconView == null) {
                    ImageView iconView = (ImageView) LayoutInflater.from(getContext())
                            .inflate(R.layout.ext_layout_tab_icon, this, false);
                    addView(iconView, 0);
                    mIconView = iconView;
                }
                if (mTextView == null) {
                    TextView textView = (TextView) LayoutInflater.from(getContext())
                            .inflate(R.layout.ext_layout_tab_text, this, false);
                    addView(textView);
                    mTextView = textView;
                    mDefaultMaxLines = TextViewCompat.getMaxLines(mTextView);
                }
                TextViewCompat.setTextAppearance(mTextView, mTabTextAppearance);
                if (mTabTextColors != null) {
                    mTextView.setTextColor(mTabTextColors);
                }
                updateTextAndIcon(mTextView, mIconView);
                // updateTextAndIcon 之后更新
                if (mIconView != null && mTabIconTintColors != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mIconView.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                        mIconView.setImageTintList(mTabIconTintColors);
                    } else {
                        if (isSelected()) {
                            int selectedColor = mTabIconTintColors.getColorForState(SELECTED_STATE_SET, mTabIconTintColors.getDefaultColor());
                            mIconView.setColorFilter(selectedColor);
                        } else {
                            mIconView.setColorFilter(mTabIconTintColors.getDefaultColor());
                        }
                    }
                }
            } else {
                // Else, we'll see if there is a TextView or ImageView present and update them
                if (mCustomTextView != null || mCustomIconView != null) {
                    updateTextAndIcon(mCustomTextView, mCustomIconView);
                }
            }

            if (tab != null) {
                Tips tips = tab.mTips;
                if (tips != null) {
                    mTipsIcon = tips.mIcon;
                    int tintColor = tips.mBgColor;
                    if (mTipsIcon != null && tips.isBgColorAvailable()) {
                        tintDrawable(mTipsIcon, tintColor);
                    }
                }
            }

            // Finally update our selected state
            setSelected(tab != null && tab.isSelected());

        }

        private void tintDrawable(@NonNull Drawable drawable, @ColorInt int tintColor) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTintMode(PorterDuff.Mode.SRC_IN);
                drawable.setTint(tintColor);
            } else {
                drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            final Tab tab = mTab;
            drawTips(canvas, tab);
        }

        private void drawTips(Canvas canvas, Tab tab) {
            if (tab == null) {
                return;
            }
            if (!tab.isTipsVisible()) {
                return;
            }
            Tips tips = tab.mTips;
            if (tips == null) {
                return;
            }
            int type = tips.type;
            if (type == Tips.TYPE_MSG) {
                if (mTipsMsgView == null) {
                    mTipsMsgView = TipsMsgView.newInstance(this);
                }
                mTipsMsgView.setTips(tips);

                int width = mTipsMsgView.caculateWidth(tips);
                int height = mTipsMsgView.caculateHeight(tips);
                int limitBottom = getTabContentTop();
                int limitTop = getPaddingTop();
                int limitLeft = getTabContentRight();
                int limitRight = getWidth();
                int left = limitLeft;
                int right = left + width;
                int top = limitBottom - height;
                int bottom = limitBottom;
                if (right > limitRight) {
                    right = limitRight;
                    left = limitRight - width;
                }
                if (top < limitTop) {
                    top = limitTop;
                    bottom = top + height;
                }

                // 设置 margin
                int marginTop = tips.mMarginTop;
                int marginLeft = tips.mMarginLeft;
                left += marginLeft;
                right += marginLeft;
                top += marginTop;
                bottom += marginTop;
                Rect rec = new Rect(left, top, right, bottom);
                mTipsMsgView.prepareBeforeDraw(rec);

                canvas.save();
                canvas.translate(rec.left, rec.top);
                mTipsMsgView.draw(canvas);
                canvas.restore();
            } else if (type == Tips.TYPE_ICON || type == Tips.TYPE_DOT) {
                Drawable icon = tips.mIcon;
                if (type == Tips.TYPE_DOT) {
                    icon = sDotTipsIcon;
                    int tintColor = tips.mBgColor;
                    if (icon != null && tips.isBgColorAvailable()) {
                        tintDrawable(icon, tintColor);
                    }
                }

                if (icon == null) {
                    return;
                }
                int desireW = icon.getIntrinsicWidth();
                int desireH = icon.getIntrinsicHeight();
                float ratio = desireW / (float) desireH;
                int limitTop = getTabContentTop();
                int top = limitTop - icon.getIntrinsicHeight();
                int bottom = limitTop;

                int limitLeft = getTabContentRight();
                int left = limitLeft;
                if (limitLeft == getRight()) {
                    left = limitLeft - icon.getIntrinsicWidth();
                }
                int right = left + icon.getIntrinsicWidth();
                if (right > getWidth()) {
                    //宽度不够现实，缩放图片
                    desireW = getWidth() - limitLeft;
                    desireH = (int) (desireW / ratio);
                    right = getWidth();
                    top = limitTop - desireW;
                } else if (top < 0) {
                    //高度现实不下，缩放图片
                    desireH = limitTop;
                    desireW = (int) (ratio * desireH);
                    top = 0;
                    right = left + desireW;
                }

                // 设置 margin
                int marginTop = tips.mMarginTop;
                int marginLeft = tips.mMarginLeft;
                left += marginLeft;
                right += marginLeft;
                top += marginTop;
                bottom += marginTop;

                icon.setBounds(left, top, right, bottom);
                icon.draw(canvas);
            }
        }

        private void updateIconTint4Before_LOLLIPOP() {
            if (mIconView != null && mTabIconTintColors != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    if (isSelected()) {
                        int selectedColor = mTabIconTintColors.getColorForState(SELECTED_STATE_SET, mTabIconTintColors.getDefaultColor());
                        mIconView.setColorFilter(selectedColor);
                    } else {
                        mIconView.setColorFilter(mTabIconTintColors.getDefaultColor());
                    }
                }
            }
        }

        private void updateTextAndIcon(@Nullable final TextView textView,
                                       @Nullable final ImageView iconView) {
            final Drawable icon = mTab != null ? mTab.getIcon() : null;
            final CharSequence text = mTab != null ? mTab.getText() : null;
            final CharSequence contentDesc = mTab != null ? mTab.getContentDescription() : null;

            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    iconView.setVisibility(GONE);
                    iconView.setImageDrawable(null);
                }
                iconView.setContentDescription(contentDesc);
            }

            final boolean hasText = !TextUtils.isEmpty(text);
            if (textView != null) {
                if (hasText) {
                    textView.setText(text);
                    textView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    textView.setVisibility(GONE);
                    textView.setText(null);
                }
                textView.setContentDescription(contentDesc);
            }

            if (iconView != null) {
                MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
                int margin = 0;
                if (hasText && iconView.getVisibility() == VISIBLE) {
                    // If we're showing both text and icon, add some margin bottom to the icon
                    margin = mTextIconGap;
                }
                if (mTabOrientation == TAB_ORIENTATION_HORIZONTAL) {
                    if (margin != lp.rightMargin) {
                        lp.rightMargin = margin;
                        iconView.requestLayout();
                    }
                } else if (mTabOrientation == TAB_ORIENTATION_VERTICAL) {
                    if (margin != lp.bottomMargin) {
                        lp.bottomMargin = margin;
                        iconView.requestLayout();
                    }
                }
            }
            TooltipCompat.setTooltipText(this, hasText ? null : contentDesc);
        }

        public Tab getTab() {
            return mTab;
        }

        /**
         * Approximates a given lines width with the new provided text size.
         */
        private float approximateLineWidth(Layout layout, int line, float textSize) {
            return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
        }

        /**
         * 计算 指示器 left 边界
         *
         * @return
         */
        int getIndicatorLeftInner() {
            return getIndicatorLeftInner(false);
        }

        /**
         * 计算 指示器 left 边界
         *
         * @return
         */
        int getIndicatorLeftInner(boolean ignoreWidthFixed) {
            int left = getLeft();
            boolean isBlockStyle = mTabIndicatorStyle == STYLE_BLOCK;
            // block style 不再支持 TAB_INDICATOR_WRAP 模式
            if (mIndicatorMode == TAB_INDICATOR_WRAP && !isBlockStyle) {
                int orientation = getOrientation();
                if (mCustomView != null && mCustomView.getVisibility() != GONE) {
                    left += mCustomView.getLeft();
                } else if (orientation == HORIZONTAL) {
                    int layoutDirection = ViewCompat.getLayoutDirection(this);
                    if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        //右利模式下 icon 在右边，text 在左边，考虑 text 的left
                        if (mTextView != null && mTextView.getVisibility() != GONE) {
                            left += mTextView.getLeft();
                        } else if (mIconView != null && mIconView.getVisibility() != GONE) {
                            left += mIconView.getLeft();
                        }
                    } else {
                        // icon 在 文字 左边,如果 icon 存在，只需要考虑 icon 的 left
                        if (mIconView != null && mIconView.getVisibility() != GONE) {
                            left += mIconView.getLeft();
                        } else if (mTextView != null && mTextView.getVisibility() != GONE) {
                            left += mTextView.getLeft();
                        }
                    }
                } else if (orientation == VERTICAL) {
                    int iconLeft = 0;
                    int textLeft = 0;
                    if (mIconView != null && mIconView.getVisibility() != GONE) {
                        iconLeft = mIconView.getLeft();
                    }
                    if (mTextView != null && mTextView.getVisibility() != GONE) {
                        textLeft = mTextView.getLeft();
                    }
                    int minLeft = Math.min(iconLeft, textLeft);
                    if (minLeft == 0) {
                        minLeft = Math.max(iconLeft, textLeft);
                    }
                    left += minLeft;
                }
                if (mIndicatorAdditionalPadding > 0) {
                    int newLeft = left - mIndicatorAdditionalPadding;
                    left = Math.max(newLeft, 0);
                }
            }

            //指示器宽度固定
            if (mTabIndicatorWidthFixed && !ignoreWidthFixed) {
                int desireLength = mTabIndicatorFixedWidth;
                if (desireLength > getWidth()) {
                    desireLength = getWidth();
                }
                int right = getIndicatorRightInner(true);
                if (isBlockStyle) {
                    right = getRight();
                }
                if (right > left) {
                    int length = right - left;
                    if (desireLength > length) {
                        left -= Math.abs(desireLength - length) / 2;
                    } else {
                        left += Math.abs(length - desireLength) / 2;
                    }
                }
            }

            //如果是滑块样式，先把滑块的 水平间距去掉
            if (isBlockStyle) {
                int horizontalMargin = mIndicatorBlockStyleHorizontalMargin;
                if (horizontalMargin > getWidth()) {
                    horizontalMargin = getWidth();
                }
                left += horizontalMargin;
            }
            return left;
        }

        int getIndicatorRightInner() {
            return getIndicatorRightInner(false);
        }

        int getIndicatorRightInner(boolean ignoreWidthFixed) {
            int right = getRight();
            boolean isBlockStyle = mTabIndicatorStyle == STYLE_BLOCK;
            // block style 不再支持 TAB_INDICATOR_WRAP 模式
            if (mIndicatorMode == TAB_INDICATOR_WRAP && !isBlockStyle) {
                int orientation = getOrientation();
                if (mCustomView != null && mCustomView.getVisibility() != GONE) {
                    right -= mCustomView.getRight();
                } else if (orientation == HORIZONTAL) {
                    int layoutDirection = ViewCompat.getLayoutDirection(this);
                    if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        //右利模式 icon 在右边，text 在左边，考虑 icon 的 right
                        if (mIconView != null && mIconView.getVisibility() != GONE) {
                            right -= (right - (getLeft() + mIconView.getRight()));
                        } else if (mTextView != null && mTextView.getVisibility() != GONE) {
                            right -= (right - (getLeft() + mTextView.getRight()));
                        }
                    } else {
                        // icon 在 文字 左边,如果 text 存在，只需要考虑 text 的 right
                        if (mTextView != null && mTextView.getVisibility() != GONE) {
                            right -= (right - (getLeft() + mTextView.getRight()));
                        } else if (mIconView != null && mIconView.getVisibility() != GONE) {
                            right -= (right - (getLeft() + mIconView.getRight()));
                        }
                    }
                } else if (orientation == VERTICAL) {
                    int iconRight = 0;
                    int textRight = 0;
                    if (mIconView != null && mIconView.getVisibility() != GONE) {
                        iconRight = mIconView.getRight();
                    }
                    if (mTextView != null && mTextView.getVisibility() != GONE) {
                        textRight = mTextView.getRight();
                    }
                    int maxRight = Math.max(iconRight, textRight);
                    right -= (right - (getLeft() + maxRight));
                }
                if (mIndicatorAdditionalPadding > 0) {
                    int newRight = right + mIndicatorAdditionalPadding;
                    right = Math.min(getRight(), newRight);
                }
            }

            //指示器宽度固定
            if (mTabIndicatorWidthFixed && !ignoreWidthFixed) {
                int desireLength = mTabIndicatorFixedWidth;
                if (desireLength > getWidth()) {
                    desireLength = getWidth();
                }
                int left = getIndicatorLeftInner(true);
                if (isBlockStyle) {
                    left = getLeft();
                }
                if (right > left) {
                    int length = right - left;
                    if (desireLength > length) {
                        right += Math.abs(desireLength - length) / 2;
                    } else {
                        right -= Math.abs(length - desireLength) / 2;
                    }
                }
            }

            if (isBlockStyle) {
                int horizontalMargin = mIndicatorBlockStyleHorizontalMargin;
                if (horizontalMargin > getWidth()) {
                    horizontalMargin = getWidth();
                }
                right -= horizontalMargin;
//                    if (right < 0) {
//                        right = 0;
//                    }
            }
            return right;
        }

        /**
         * 计算当前选择 tab 的指示器的 top 值
         *
         * @param ignoreIndicatorMode 是否考虑设置的margin值
         */
        int getIndicatorTopInner(boolean ignoreIndicatorMode) {
            int top = -1;
            if (ignoreIndicatorMode || mIndicatorMode == TAB_INDICATOR_WRAP) {
                int orientation = getOrientation();
                if (mCustomView != null && mCustomView.getVisibility() != GONE) {
                    return getTop() + mCustomView.getBottom();
                } else if (orientation == HORIZONTAL) {
                    int iconBottom = 0;
                    int textBottom = 0;
                    // icon 在 text 的左边，需要同时考虑 icon 和 text 的 bottom，取最大者
                    if (mIconView != null && mIconView.getVisibility() != GONE) {
                        iconBottom = mIconView.getBottom();
                    }
                    if (mTextView != null && mTextView.getVisibility() != GONE) {
                        textBottom = mTextView.getBottom();
                    }
                    int max = Math.max(iconBottom, textBottom);
                    if (max > 0) {
                        top = getTop() + max;
                    }
                } else if (orientation == VERTICAL) {
                    if (mTextView != null && mTextView.getVisibility() != GONE) {
                        top = getTop() + mTextView.getBottom();
                    } else if (mIconView != null && mIconView.getVisibility() != GONE) {
                        top = getTop() + mIconView.getBottom();
                    }
                }
            }
            return top;
        }

        int getTabContentRight() {
            int right = getRight();
            int orientation = getOrientation();
            if (mCustomView != null && mCustomView.getVisibility() != GONE) {
                right = mCustomView.getRight();
            } else if (orientation == HORIZONTAL) {
                int layoutDirection = ViewCompat.getLayoutDirection(this);
                if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    //右利模式 icon 在右边，text 在左边，考虑 icon 的 right
                    if (mIconView != null && mIconView.getVisibility() != GONE) {
                        right = mIconView.getRight();
                    } else if (mTextView != null && mTextView.getVisibility() != GONE) {
                        right = mTextView.getRight();
                    }
                } else {
                    // icon 在 文字 左边,如果 text 存在，只需要考虑 text 的 right
                    if (mTextView != null && mTextView.getVisibility() != GONE) {
                        right = mTextView.getRight();
                    } else if (mIconView != null && mIconView.getVisibility() != GONE) {
                        right = mIconView.getRight();
                    }
                }
            } else if (orientation == VERTICAL) {
                int iconRight = 0;
                int textRight = 0;
                if (mIconView != null && mIconView.getVisibility() != GONE) {
                    iconRight = mIconView.getRight();
                }
                if (mTextView != null && mTextView.getVisibility() != GONE) {
                    textRight = mTextView.getRight();
                }
                right = Math.max(iconRight, textRight);
            }
            return right;
        }

        /**
         * 计算 TabView的内容View在 TabView 中的top 值
         */
        int getTabContentTop() {
            int orientation = getOrientation();
            int top = getPaddingTop();
            if (mCustomView != null && mCustomView.getVisibility() != GONE) {
                return mCustomView.getTop();
            } else if (orientation == HORIZONTAL) {
                int iconTop = getPaddingTop();
                int textTop = getPaddingTop();
                if (mIconView != null && mIconView.getVisibility() != GONE) {
                    iconTop = mIconView.getTop();
                }
                if (mTextView != null && mTextView.getVisibility() != GONE) {
                    textTop = mTextView.getTop();
                }
                top = Math.min(iconTop, textTop);
            } else if (orientation == VERTICAL) {
                if (mIconView != null && mIconView.getVisibility() != GONE) {
                    top = mIconView.getTop();
                } else if (mTextView != null && mTextView.getVisibility() != GONE) {
                    top = mTextView.getTop();
                }
            }
            return top;
        }

        /**
         * 计算 TabView 的内容View在 TabView 中的 bottom 值
         */
        int getTabContentBottom() {
            int orientation = getOrientation();
            int bottom = getHeight() - getPaddingBottom();
            if (mCustomView != null && mCustomView.getVisibility() != GONE) {
                return mCustomView.getBottom();
            } else if (orientation == HORIZONTAL) {
                int iconBottom = getPaddingTop();
                int textBottom = getPaddingTop();
                if (mIconView != null && mIconView.getVisibility() != GONE) {
                    iconBottom = mIconView.getBottom();
                }
                if (mTextView != null && mTextView.getVisibility() != GONE) {
                    textBottom = mTextView.getBottom();
                }
                bottom = Math.max(iconBottom, textBottom);
            } else if (orientation == VERTICAL) {
                if (mTextView != null && mTextView.getVisibility() != GONE) {
                    bottom = mTextView.getBottom();
                } else if (mIconView != null && mIconView.getVisibility() != GONE) {
                    bottom = mIconView.getBottom();
                }
            }
            return bottom;
        }
    }

    private class SlidingTabStrip extends LinearLayout {
        private int mSelectedIndicatorHeight;
        private final Paint mSelectedIndicatorPaint;

        int mSelectedPosition = -1;
        float mSelectionOffset;

        private int mLayoutDirection = -1;

        private int mIndicatorLeft = -1;
        private int mIndicatorRight = -1;

        private ValueAnimator mIndicatorAnimator;

        private float mCachedIndicatorTop = -1;
        private float mCachedIndicatorBottom = -1;

        SlidingTabStrip(Context context) {
            super(context);
            setWillNotDraw(false);
            mSelectedIndicatorPaint = new Paint();
            mSelectedIndicatorPaint.setAntiAlias(true);
        }

        void setSelectedIndicatorColor(int color) {
            if (mSelectedIndicatorPaint.getColor() != color) {
                mSelectedIndicatorPaint.setColor(color);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        void setSelectedIndicatorHeight(int height) {
            if (mSelectedIndicatorHeight != height) {
                mSelectedIndicatorHeight = height;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        boolean childrenNeedLayout() {
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (child.getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                mIndicatorAnimator.cancel();
            }

            mSelectedPosition = position;
            mSelectionOffset = positionOffset;
            updateIndicatorPosition();
        }

        float getIndicatorPosition() {
            return mSelectedPosition + mSelectionOffset;
        }

        /**
         * 更改指示器显示模式
         */
        void changeIndicatorMode() {
            //更改指示器位置
            updateIndicatorPosition();
            //重置 指示器的 top and bottom
            resetIndicatorTopAndBottom();
            postInvalidate();
        }

        private void resetIndicatorTopAndBottom() {
            mCachedIndicatorTop = -1;
            mCachedIndicatorBottom = -1;
        }

        @Override
        public void onRtlPropertiesChanged(int layoutDirection) {
            super.onRtlPropertiesChanged(layoutDirection);

            // Workaround for a bug before Android M where LinearLayout did not relayout itself when
            // layout direction changed.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //noinspection WrongConstant
                if (mLayoutDirection != layoutDirection) {
                    requestLayout();
                    mLayoutDirection = layoutDirection;
                }
            }
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
                // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
                return;
            }

            if (mMode == MODE_FIXED && mTabGravity == GRAVITY_CENTER) {
                final int count = getChildCount();

                // First we'll find the widest tab
                int largestTabWidth = 0;
                for (int i = 0, z = count; i < z; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == VISIBLE) {
                        largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
                    }
                }

                if (largestTabWidth <= 0) {
                    // If we don't have a largest child yet, skip until the next measure pass
                    return;
                }

                final int gutter = dpToPx(FIXED_WRAP_GUTTER_MIN);
                boolean remeasure = false;

                if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
                    // If the tabs fit within our width minus gutters, we will set all tabs to have
                    // the same width
                    for (int i = 0; i < count; i++) {
                        final LinearLayout.LayoutParams lp =
                                (LinearLayout.LayoutParams) getChildAt(i).getLayoutParams();
                        if (lp.width != largestTabWidth || lp.weight != 0) {
                            lp.width = largestTabWidth;
                            lp.weight = 0;
                            remeasure = true;
                        }
                    }
                } else {
                    // If the tabs will wrap to be larger than the width minus gutters, we need
                    // to switch to GRAVITY_FILL
                    mTabGravity = GRAVITY_FILL;
                    updateTabViews(false);
                    remeasure = true;
                }

                if (remeasure) {
                    // Now re-measure after our changes
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);

            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                // If we're currently running an animation, lets cancel it and start a
                // new animation with the remaining duration
                mIndicatorAnimator.cancel();
                final long duration = mIndicatorAnimator.getDuration();
                animateIndicatorToPosition(mSelectedPosition,
                        Math.round((1f - mIndicatorAnimator.getAnimatedFraction()) * duration));
            } else {
                // If we've been layed out, update the indicator position
                updateIndicatorPosition();
            }
        }

        private void updateIndicatorPosition() {
            final TabView selectedTitle = (TabView) getChildAt(mSelectedPosition);
            int left, right;

            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                left = selectedTitle.getIndicatorLeftInner();
                right = selectedTitle.getIndicatorRightInner();

                if (mSelectionOffset > 0f && mSelectedPosition < getChildCount() - 1) {
                    // Draw the selection partway between the tabs
                    TabView nextTitle = (TabView) getChildAt(mSelectedPosition + 1);
                    left = (int) (mSelectionOffset * nextTitle.getIndicatorLeftInner() +
                            (1.0f - mSelectionOffset) * left);
                    right = (int) (mSelectionOffset * nextTitle.getIndicatorRightInner() +
                            (1.0f - mSelectionOffset) * right);
                }
            } else {
                left = right = -1;
            }

            setIndicatorPosition(left, right);
        }

        void setIndicatorPosition(int left, int right) {
            if (left != mIndicatorLeft || right != mIndicatorRight) {
                // If the indicator's left/right has changed, invalidate
                mIndicatorLeft = left;
                mIndicatorRight = right;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        void animateIndicatorToPosition(final int position, int duration) {
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                mIndicatorAnimator.cancel();
            }

            final boolean isRtl = ViewCompat.getLayoutDirection(this)
                    == ViewCompat.LAYOUT_DIRECTION_RTL;

            final View targetView = getChildAt(position);
            if (targetView == null) {
                // If we don't have a view, just update the position now and return
                updateIndicatorPosition();
                return;
            }

            TabView tabView = (TabView) targetView;
            final int targetLeft = tabView.getIndicatorLeftInner();
            final int targetRight = tabView.getIndicatorRightInner();
            final int startLeft;
            final int startRight;

            if (Math.abs(position - mSelectedPosition) <= 1) {
                // If the views are adjacent, we'll animate from edge-to-edge
                startLeft = mIndicatorLeft;
                startRight = mIndicatorRight;
            } else {
                // Else, we'll just grow from the nearest edge
                final int offset = dpToPx(MOTION_NON_ADJACENT_OFFSET);
                if (position < mSelectedPosition) {
                    // We're going end-to-start
                    if (isRtl) {
                        startLeft = startRight = targetLeft - offset;
                    } else {
                        startLeft = startRight = targetRight + offset;
                    }
                } else {
                    // We're going start-to-end
                    if (isRtl) {
                        startLeft = startRight = targetRight + offset;
                    } else {
                        startLeft = startRight = targetLeft - offset;
                    }
                }
            }

            if (startLeft != targetLeft || startRight != targetRight) {
                ValueAnimator animator = mIndicatorAnimator = new ValueAnimator();
                animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator.setDuration(duration);
                animator.setFloatValues(0, 1);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        final float fraction = animator.getAnimatedFraction();
                        setIndicatorPosition(
                                AnimationUtils.lerp(startLeft, targetLeft, fraction),
                                AnimationUtils.lerp(startRight, targetRight, fraction));
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mSelectedPosition = position;
                        mSelectionOffset = 0f;
                    }
                });
                animator.start();
            }
        }

        /**
         * 计算指示器的 top 值, 会考虑 设置的 margin 值
         *
         * @return
         */
        private float getIndicatorTop() {
            final TabView selectedTitle = (TabView) getChildAt(mSelectedPosition);
            if (mIndicatorFixedTop) {//指示器在顶端
                if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                    int limitTop = getPaddingTop() + selectedTitle.getTabContentTop();
                    int top = getPaddingTop();
                    int marginTop = mTabIndicatorMarginTop;
                    int newTop = top + marginTop;
                    if (newTop + mSelectedIndicatorHeight > limitTop) {
                        newTop = limitTop - mSelectedIndicatorHeight;
                    }
                    return newTop;
                }
                return getPaddingTop();
            }
            int oldTop = getHeight() - mSelectedIndicatorHeight;

            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                if (mIndicatorMode == TAB_INDICATOR_WRAP) {
                    int marginTop = mTabIndicatorMarginTop;
                    int top = selectedTitle.getIndicatorTopInner(false);
                    int newTop = top + marginTop;
                    if (newTop > oldTop) {
                        return oldTop;
                    }
                    return newTop;
                } else if (mIndicatorMode == TAB_INDICATOR_FILL) {
                    int marginBottom = mTabIndicatorMarginBottom;
                    int limitTop = selectedTitle.getIndicatorTopInner(true);
                    int newTop = oldTop - marginBottom;
                    if (newTop < limitTop) {
                        return limitTop;
                    }
                    return newTop;
                }
            }
            return oldTop;
        }

        /**
         * 计算指示器的上边界
         */
        private float getIndicatorLimitTop() {
            int oldTop = getHeight() - mSelectedIndicatorHeight;
            final TabView selectedTitle = (TabView) getChildAt(mSelectedPosition);
            if (mIndicatorFixedTop) {//指示器在顶端
                if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                    return getPaddingTop() + selectedTitle.getTabContentTop();
                }
                return oldTop;
            }
            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                return selectedTitle.getIndicatorTopInner(true);
            }
            return oldTop;
        }

        private float getIndicatorBottom() {
            if (mIndicatorMode == TAB_INDICATOR_WRAP || mIndicatorMode == TAB_INDICATOR_FILL) {
                return getIndicatorTop() + mSelectedIndicatorHeight;
            }
            return getHeight();
        }

        private int getSelectedTabMiniHeight() {
            final TabView selectedTitle = (TabView) getChildAt(mSelectedPosition);
            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                return selectedTitle.getTabContentBottom() - selectedTitle.getTabContentTop();
            }
            return -1;
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            // Thick colored underline below the current selection
            if (mIndicatorLeft >= 0 && mIndicatorRight > mIndicatorLeft && mTabIndicatorStyle != STYLE_NONE) {
                if (mCachedIndicatorTop == -1) {
                    mCachedIndicatorTop = getIndicatorTop();
                }
                float top = mCachedIndicatorTop;
                if (mCachedIndicatorBottom == -1) {
                    mCachedIndicatorBottom = getIndicatorBottom();
                }
                float bottom = mCachedIndicatorBottom;
                @IndicatorStyle
                int indicatorStyle = mTabIndicatorStyle;

                if (indicatorStyle == STYLE_BLOCK) {
                    int limitHeight = getHeight();
                    int desireHeight = mSelectedIndicatorHeight;
                    int miniHeight = getSelectedTabMiniHeight();
                    if (desireHeight > limitHeight) {
                        desireHeight = limitHeight - 2 * DEFAULT_INDICATOR_MARGIN_4_BLOCK_STYLE;
                    }
                    if (desireHeight < miniHeight) {
                        desireHeight = limitHeight - 2 * DEFAULT_INDICATOR_MARGIN_4_BLOCK_STYLE;
                    }
                    int blockTop = (getHeight() - desireHeight) / 2;
                    int blockBottom = blockTop + desireHeight;
                    float cornerRadius = mTabIndicatorCornerRadius;
                    if (cornerRadius < -1 || cornerRadius > desireHeight / 2) {
                        cornerRadius = desireHeight / 2;
                    }
                    if (cornerRadius > 0) {
                        canvas.drawRoundRect(new RectF(mIndicatorLeft, blockTop, mIndicatorRight, blockBottom), cornerRadius, cornerRadius, mSelectedIndicatorPaint);
                    } else {
                        canvas.drawRect(mIndicatorLeft, blockTop,
                                mIndicatorRight, blockBottom, mSelectedIndicatorPaint);
                    }

                } else if (indicatorStyle == STYLE_DRAWABLE) {
                    Drawable drawable = mIndicatorDrawable;
                    if (drawable != null) {
                        float ratio = drawable.getIntrinsicWidth() / (float) drawable.getIntrinsicHeight();
                        int desireW = drawable.getIntrinsicWidth();
                        int desireH = drawable.getIntrinsicHeight();

                        boolean needAdjustSize = false;
                        int limitTop = (int) getIndicatorLimitTop();

                        int boundTop = getHeight() - desireH;
                        int boundBottom = getHeight();
                        if (mIndicatorFixedTop) {
                            int marginTop = mTabIndicatorMarginTop;
                            boundTop = getPaddingTop();
                            if (marginTop > 0) {
                                if (marginTop > limitTop) {
                                    marginTop = limitTop;
                                }
                                boundTop += marginTop;
                            }
                            boundBottom = boundTop + desireH;
                            if (boundBottom > limitTop) {
                                boundBottom = limitTop;
                                needAdjustSize = true;
                            }

                            if (needAdjustSize) {// indicator drawable 高度被压缩,宽度需要等比例调整
                                desireW = (int) (ratio * (boundBottom - boundTop));
                                desireH = boundBottom - boundTop;
                            }
                        } else {
                            int marginBottom = mTabIndicatorMarginBottom;
                            if (marginBottom > 0) {//计算设置的 mTabIndicatorMarginBottom
                                boundTop -= marginBottom;
                                boundBottom = getHeight() - marginBottom;
                            }
                            if (boundTop < limitTop) {//保证 indicator 不会和 tab 内容重合
                                boundTop = limitTop;
                                needAdjustSize = true;
                            }

                            if (needAdjustSize) { // indicator drawable 高度被压缩,宽度需要等比例调整
                                desireW = (int) (ratio * (boundBottom - boundTop));
                                desireH = boundBottom - boundTop;
                            }
                            if (false && (desireW > mIndicatorRight - mIndicatorLeft)) {//指示器的大小不能老是变化，所以 避免根据宽度适配高度
                                desireW = mIndicatorRight - mIndicatorLeft;
                                desireH = (int) (desireW / ratio);
                                boundTop = getHeight() - desireH;
                            }
                        }
                        int boundLeft = mIndicatorLeft + (mIndicatorRight - mIndicatorLeft - desireW) / 2;
                        int boundRight = boundLeft + desireW;

                        drawable.setBounds(boundLeft, boundTop, boundRight, boundBottom);
                        drawable.draw(canvas);
                    }
                } else { //default
                    if (mTabIndicatorCornerRadius > 0) {
                        float radius = mTabIndicatorCornerRadius;
                        canvas.drawRoundRect(new RectF(mIndicatorLeft, top, mIndicatorRight, bottom), radius, radius, mSelectedIndicatorPaint);
                    } else {
                        canvas.drawRect(mIndicatorLeft, top,
                                mIndicatorRight, bottom, mSelectedIndicatorPaint);
                    }
                }
            }
        }
    }

    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;

        return new ColorStateList(states, colors);
    }

    private int getDefaultHeight() {
        boolean hasIconAndText = false;
        for (int i = 0, count = mTabs.size(); i < count; i++) {
            Tab tab = mTabs.get(i);
            if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
                hasIconAndText = true;
                break;
            }
        }
        return hasIconAndText ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
    }

    private int getTabMinWidth() {
        if (mRequestedTabMinWidth != INVALID_WIDTH && !mSetMinWidthFromCode) {
            // If we have been given a min width, use it
            return mRequestedTabMinWidth;
        }
        return mMode == MODE_SCROLLABLE ? mScrollableTabMinWidth : mFixedModelTabMinWidth;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return generateDefaultLayoutParams();
    }

    int getTabMaxWidth() {
        return mTabMaxWidth;
    }

    /**
     * A {@link ViewPager.OnPageChangeListener} class which contains the
     * necessary calls back to the provided {@link TabLayoutExt} so that the tab position is
     * kept in sync.
     * <p>
     * <p>This class stores the provided TabLayoutExt weakly, meaning that you can use
     * {@link ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
     * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and
     * not cause a leak.
     */
    public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final WeakReference<TabLayoutExt> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;

        public TabLayoutOnPageChangeListener(TabLayoutExt tabLayout) {
            mTabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset,
                                   final int positionOffsetPixels) {
            final TabLayoutExt tabLayout = mTabLayoutRef.get();
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                final boolean updateText = mScrollState != SCROLL_STATE_SETTLING ||
                        mPreviousScrollState == SCROLL_STATE_DRAGGING;
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                final boolean updateIndicator = !(mScrollState == SCROLL_STATE_SETTLING
                        && mPreviousScrollState == SCROLL_STATE_IDLE);
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            final TabLayoutExt tabLayout = mTabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                final boolean updateIndicator = mScrollState == SCROLL_STATE_IDLE
                        || (mScrollState == SCROLL_STATE_SETTLING
                        && mPreviousScrollState == SCROLL_STATE_IDLE);
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
            }
        }

        void reset() {
            mPreviousScrollState = mScrollState = SCROLL_STATE_IDLE;
        }
    }

    /**
     * A {@link OnTabSelectedListener} class which contains the necessary calls back
     * to the provided {@link ViewPager} so that the tab position is kept in sync.
     */
    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onTabSelected(Tab tab) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(Tab tab) {
            // No-op
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
        private boolean mAutoRefresh;

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager,
                                     @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (mViewPager == viewPager) {
                setPagerAdapter(newAdapter, mAutoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            mAutoRefresh = autoRefresh;
        }
    }
}
