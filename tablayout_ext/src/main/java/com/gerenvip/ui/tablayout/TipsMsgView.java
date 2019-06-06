package com.gerenvip.ui.tablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.BoringLayout;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * <a href="https://github.com/H07000223/FlycoTabLayout/blob/master/FlycoTabLayout_Lib/src/main/java/com/flyco/tablayout/widget/MsgView.java">
 * reference FlycoTabLayout MsgView</a> thk Flyco.
 *
 * @author wangwei on 2018/4/13.
 *         wangwei@jiandaola.com
 */
public class TipsMsgView extends AppCompatTextView {

    private Context context;
    private GradientDrawable mBackground = new GradientDrawable();
    @ColorInt
    private int mBackgroundColor;
    private int mCornerRadius;
    private int mStrokeWidth;
    private int mStrokeColor;
    private boolean isRadiusHalfHeight;
    private boolean isWidthHeightEqual;
    private float mDensity;
    private int mPadding;

    private static final int DEFAULT_PADDING_DP = 2;
    private static final int MIN_WIDTH_DP = 18;
    private static final int MAX_WIDTH_DP = 26;
    private static final int TEXT_SIZE_SP = 10;
    private int mMiniWidth;
    private int mMaxWidth;

    public TipsMsgView(Context context) {
        this(context, null);
    }

    public TipsMsgView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipsMsgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        obtainAttributes(context, attrs);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mDensity = displayMetrics.density;
        mPadding = (int) (DEFAULT_PADDING_DP * mDensity);
        setPadding(mPadding, mPadding, mPadding, mPadding);
        setMaxLines(1);
        mMiniWidth = (int) (MIN_WIDTH_DP * mDensity);
        mMaxWidth = (int) (MAX_WIDTH_DP * mDensity);
    }

    public static TipsMsgView newInstance(@NonNull ViewGroup parent) {
        return (TipsMsgView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_tips, parent, false);
    }

    public int caculateWidth(@NonNull TabLayoutExt.Tips tips) {
        String msg = tips.mMsg;
        float desireW = -1;

        BoringLayout.Metrics boring = BoringLayout.isBoring(msg, getPaint());
        if (boring != null) {
            desireW = boring.width;
        }
        if (desireW < 0) {
            desireW = (float) Math.ceil(Layout.getDesiredWidth(msg, getPaint()));
        }
        if (desireW < 0) {
            desireW = getPaint().measureText(msg);
        }
        desireW += getCompoundPaddingLeft() + getCompoundPaddingRight();
        int lineHeight = getLineHeight() + getCompoundPaddingTop() + getCompoundPaddingBottom();
        return (int) Math.min(mMaxWidth, Math.max(mMiniWidth, Math.max(desireW, lineHeight)));
    }

    public int caculateHeight(@NonNull TabLayoutExt.Tips tips) {
        int lineHeight = getLineHeight() + getCompoundPaddingTop() + getCompoundPaddingBottom();
        if (!tips.mLimitCircular) {
            return lineHeight + 2 * mPadding;
        }
        return caculateWidth(tips);
    }

    public void prepareBeforeDraw(Rect rect) {
        int widthSpec = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY);
        measure(widthSpec, heightSpec);
        layout(rect.left, rect.top, rect.right, rect.bottom);
        setPadding(mPadding, mPadding, mPadding, mPadding);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TipsMsgView);
        mBackgroundColor = ta.getColor(R.styleable.TipsMsgView_tips_backgroundColor, Color.TRANSPARENT);
        mCornerRadius = ta.getDimensionPixelSize(R.styleable.TipsMsgView_tips_cornerRadius, 0);
        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.TipsMsgView_tips_strokeWidth, 0);
        mStrokeColor = ta.getColor(R.styleable.TipsMsgView_tips_strokeColor, Color.TRANSPARENT);
        isRadiusHalfHeight = ta.getBoolean(R.styleable.TipsMsgView_tips_isRadiusHalfHeight, false);
        isWidthHeightEqual = ta.getBoolean(R.styleable.TipsMsgView_tips_isWidthHeightEqual, false);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isWidthHeightEqual() && getWidth() > 0 && getHeight() > 0) {
            int max = Math.max(getWidth(), getHeight());
            int measureSpec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
            super.onMeasure(measureSpec, measureSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (isRadiusHalfHeight()) {
            setCornerRadius(getHeight() / 2);
        } else {
            setBgSelector();
        }
    }

    public void setTips(TabLayoutExt.Tips tips) {
        if (tips == null) {
            return;
        }
        String msg = tips.mMsg;
        if (tips.isBgColorAvailable()) {
            int bgColor = tips.mBgColor;
            setBackgroundColor(bgColor);
        }
        if (tips.isMsgColorAvailable()) {
            int color = tips.mMsgColor;
            setTextColor(color);
        } else {
            setTextColor(Color.WHITE);
        }
        if (tips.mTextSize > 0) {
            setTextSize(tips.mTextSize);
        } else {
            setTextSize(TEXT_SIZE_SP);
        }
        setStrokeWidth(tips.mTipsStrokeWidth);
        if (tips.isTipsStrokeColorAvailable()) {
            setStrokeColor(tips.mTipsStrokeColor);
        }

        if (tips.mLimitCircular) {
            setCornerRadius(getHeight() / 2);
        } else {
            setCornerRadius(tips.mCornerRadius);
        }
        setText(msg);
        try {
            Typeface typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            if (typeface != null) {
                getPaint().setTypeface(typeface);
            }
        } catch (Exception e) {
            getPaint().setFakeBoldText(true);
        }
        setGravity(Gravity.CENTER);
    }

    /**
     * 设置背景颜色
     *
     * @param backgroundColor ColorInt
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        setBgSelector();
    }

    public void setCornerRadius(int cornerRadius) {
        this.mCornerRadius = cornerRadius;
        setBgSelector();
    }

    /**
     * 设置背景边框宽度
     *
     * @param strokeWidth pixel
     */
    public void setStrokeWidth(int strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        setBgSelector();
    }

    /**
     * 设置背景边框颜色
     *
     * @param strokeColor ColorInt
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.mStrokeColor = strokeColor;
        setBgSelector();
    }

    /**
     * 设置 圆角是高度的一半
     *
     * @param isRadiusHalfHeight boolean
     */
    public void setIsRadiusHalfHeight(boolean isRadiusHalfHeight) {
        this.isRadiusHalfHeight = isRadiusHalfHeight;
        setBgSelector();
    }

    /**
     * 设置等宽等高
     *
     * @param isWidthHeightEqual boolean
     */
    public void setIsWidthHeightEqual(boolean isWidthHeightEqual) {
        this.isWidthHeightEqual = isWidthHeightEqual;
        setBgSelector();
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public boolean isRadiusHalfHeight() {
        return isRadiusHalfHeight;
    }

    public boolean isWidthHeightEqual() {
        return isWidthHeightEqual;
    }

    protected int dp2px(float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected int sp2px(float sp) {
        final float scale = this.context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    private void setDrawable(GradientDrawable gd, int color, int strokeColor) {
        gd.setColor(color);
        gd.setCornerRadius(mCornerRadius);
        gd.setStroke(mStrokeWidth, strokeColor);
    }

    private void setBgSelector() {
        StateListDrawable bg = new StateListDrawable();

        setDrawable(mBackground, mBackgroundColor, mStrokeColor);
        bg.addState(new int[]{-android.R.attr.state_pressed}, mBackground);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//16
            setBackground(bg);
        } else {
            setBackgroundDrawable(bg);
        }
    }
}
