package ru.vl.news.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollContainer extends ScrollView {
	private OnScrollChanged mOnScrollListener;

	public ScrollContainer(Context context) {
		super(context);
	}

	public ScrollContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnScrollListener(OnScrollChanged listener) {
		mOnScrollListener = listener;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(this, l, t, oldl, oldt);
		}
	}

	public interface OnScrollChanged {
		void onScroll(ScrollContainer view, int x, int y, int oldX, int oldY);
	}
}
