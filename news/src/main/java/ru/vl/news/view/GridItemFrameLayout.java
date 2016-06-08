package ru.vl.news.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class GridItemFrameLayout extends FrameLayout {
	public GridItemFrameLayout(Context context) {
		super(context);
	}

	public GridItemFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GridItemFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
		requestLayout();
	}
}
