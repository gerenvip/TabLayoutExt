package com.gerenvip.ui.tablayout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author wangwei on 2018/4/4.
 *         wangwei@jiandaola.com
 */
public class TabItemExt extends View {
    final CharSequence mText;
    final Drawable mIcon;
    final int mCustomLayout;

    public TabItemExt(Context context) {
        this(context, null);
    }

    public TabItemExt(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.TabItemExt);
        mText = a.getText(R.styleable.TabItemExt_android_text);
        mIcon = a.getDrawable(R.styleable.TabItemExt_android_icon);
        mCustomLayout = a.getResourceId(R.styleable.TabItemExt_android_layout, 0);
        a.recycle();
    }
}
