package ru.vl.news.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;

import ru.vl.news.GA;
import ru.vl.news.R;
import ru.vl.news.service.NewsFeedService;
import ru.vl.news.view.SlidingPaneLayout;


public class MainActivity extends Activity implements
		NewsFeedFragment.Callback,
		SlidingPaneLayout.PanelSlideListener {

	private SlidingPaneLayout mPane;
	private String mLastUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

		setContentView(R.layout.activity_pane);
		mPane = (SlidingPaneLayout) findViewById(R.id.pane);
		mPane.openPane();
		mPane.setParallaxDistance((int) (300 * getResources().getDisplayMetrics().density));
		mPane.setShadowResource(R.drawable.slidepane_shadow);
		mPane.setPanelSlideListener(this);

		FragmentManager fm = getFragmentManager();
		Fragment list = fm.findFragmentByTag("list");

		if (list == null) {
			list = new NewsFeedFragment();
		}
		fm.beginTransaction()
				.replace(R.id.list, list, "list")
				.commit();

		if (savedInstanceState != null) {
			mLastUrl = savedInstanceState.getString("lastUrl");
			onItemSelected(mLastUrl, false);
		}
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		GA.sendEvent(this, R.string.ga_event_category_common,
			getString(R.string.ga_event_start_in_orientation, display.getRotation()));
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		if (mPane.isOpen()) {
			menu.clear();
			getMenuInflater().inflate(R.menu.refresh, menu);
		} else {
			Fragment frg = getFragmentManager().findFragmentByTag("card");
			if (frg != null) {
				frg.onCreateOptionsMenu(menu, getMenuInflater());
			}
		}

		if (!getActionBar().isShowing()) {
			getActionBar().show();
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_refresh) {
			NewsFeedService.start(this);
			setProgressBarIndeterminateVisibility(true);
			item.setVisible(false);
			GA.sendEvent(this, R.string.ga_event_category_common, R.string.ga_event_refresh_feed);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(String fullUrl, boolean isClose) {
		setProgressBarIndeterminateVisibility(false);
		mLastUrl = fullUrl;
		invalidateOptionsMenu();
		getFragmentManager().beginTransaction()
				.replace(R.id.card, NewsCardFragment.newInstance(fullUrl), "card")
				.commit();

		if (isClose) {
			mPane.closePane();
		}
	}

	@Override
	public void onFeedScroll(int currentItem) {
		if (!mPane.isOpen()) {
			return;
		}

		if (currentItem > 2) {
			getActionBar().hide();
		} else {
			getActionBar().show();
		}
	}

	@Override
	public void onPanelSlide(View panel, float slideOffset) {

	}

	@Override
	public void onPanelOpened(View panel) {
		setTitle(R.string.app_name);
		GA.sendEvent(this, R.string.ga_event_category_common, R.string.ga_event_close_news);
		getActionBar().setDisplayShowHomeEnabled(true);
		invalidateOptionsMenu();
	}

	@Override
	public void onPanelClosed(View panel) {
		getActionBar().setDisplayShowHomeEnabled(false);
		invalidateOptionsMenu();
		GA.sendEvent(this, R.string.ga_event_category_common, R.string.ga_event_open_news);
	}

	@Override
	public void onBackPressed() {
		if (!mPane.isOpen()) {
			mPane.openPane();
			GA.sendEvent(this, R.string.ga_event_category_common, R.string.ga_event_close_news_by_back);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("lastUrl", mLastUrl);
		super.onSaveInstanceState(outState);
	}
}
