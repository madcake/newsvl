package ru.vl.news.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GridImageView extends ImageView {

	public GridImageView(Context context) {
		super(context);
	}

	public GridImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}
}
