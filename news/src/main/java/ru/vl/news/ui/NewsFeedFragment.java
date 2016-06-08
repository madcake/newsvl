package ru.vl.news.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import ru.vl.news.Constants;
import ru.vl.news.GA;
import ru.vl.news.R;
import ru.vl.news.Utils;
import ru.vl.news.provider.NewsContract;
import ru.vl.news.service.NewsFeedService;
import ru.vl.news.view.LoadingView;

public class NewsFeedFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>,
		AdapterView.OnItemClickListener,
		AbsListView.OnScrollListener {

	private GridView mGrid;
	private Drawable mPlaceholderDrawable;

	interface Callback {
		void onItemSelected(String fullUrl, boolean isClosePanel);
		void onFeedScroll(int currentItem);
	}

	private static final Callback sDummyCallback = new Callback() {
		public void onItemSelected(String fullUrl, boolean isClosePanel) { }
		public void onFeedScroll(int currentItem) { }
	};

	private Adapter mAdapter;
	private LoadingView mLoadingView;
	private Callback mCallback = sDummyCallback;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (Callback) activity;
		mPlaceholderDrawable = getResources().getDrawable(R.drawable.stab_img);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_news_feed, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mLoadingView = (LoadingView) view.findViewById(android.R.id.empty);
		mGrid = (GridView) view.findViewById(R.id.grid);
		mGrid.setVerticalScrollBarEnabled(false);
		mLoadingView.setOnRetryListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NewsFeedService.start(v.getContext());
				mLoadingView.showLoading();
			}
		});

		mAdapter = new Adapter(getActivity());
		GA.sendScreen(view.getContext(), R.string.ga_screen_feed);
		mGrid.setAdapter(mAdapter);
		mGrid.setEmptyView(mLoadingView);
		mGrid.setOnItemClickListener(this);
		mGrid.setOnScrollListener(this);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver,
				new IntentFilter(Constants.ACTION_NEWS_FEED));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ContentValues values = new ContentValues();
		values.put(NewsContract.NewsFeed.IS_READ, 1);
		getActivity().getContentResolver().update(
			ContentUris.appendId(NewsContract.NewsFeed.CONTENT_URI.buildUpon(), id).build(), values, null, null);
		mCallback.onItemSelected(((Cursor) mAdapter.getItem(position)).getString(Query.FULL_NEW_URL), true);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mCallback.onFeedScroll(firstVisibleItem);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		mLoadingView.showLoading();
		return new CursorLoader(getActivity(),
				NewsContract.NewsFeed.CONTENT_URI,
				Query.PROJECTION,
				null, null,
				NewsContract.NewsFeed.DATE + " desc");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		getActivity().setProgressBarIndeterminateVisibility(false);
		getActivity().invalidateOptionsMenu();
		mAdapter.swapCursor(data);

		if (data != null && data.getCount() > 0 && getFragmentManager().findFragmentByTag("card") == null) {
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					mCallback.onItemSelected(((Cursor) mAdapter.getItem(0)).getString(Query.FULL_NEW_URL), false);
				}
			});
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
		getActivity().setProgressBarIndeterminateVisibility(false);
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallback = sDummyCallback;
	}

	public void showError() {
		getActivity().setProgressBarIndeterminateVisibility(false);
		getActivity().invalidateOptionsMenu();
		if (mAdapter.getCount() != 0) {
			Toast.makeText(getActivity(), R.string.err_network_problems, Toast.LENGTH_SHORT).show();
		} else {
			mLoadingView.showDefaultFail();
		}
	}

	private int getColor(int position) {
		if ((position % 5) == 0) {
			position = 280;
		} else if ((position % 4) == 0) {
			position = 210;
		} else if ((position % 3) == 0) {
			position = 140;
		} else if ((position % 2) == 0) {
			position = 70;
		} else {
			position = 10;
		}
		final float hueRange = 220;
		final float sizeRange = 300;
		final float size = Math.min(position, sizeRange);
		final float hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange;
		return Color.HSVToColor(0xff, new float[]{// df
			hue, 0.4f, .2f
		});
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().containsKey(Constants.EXTRA_ERR_CODE)) {
				showError();
			}
		}
	};

	class Adapter extends CursorAdapter {

		private final LayoutInflater mInflater;

		public Adapter(Context context) {
			super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = mInflater.inflate(R.layout.item_preview_news, parent, false);
			view.setTag(new ViewHolder(view));
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			int position = cursor.getPosition();
			ViewHolder h = (ViewHolder) view.getTag();
			RequestCreator imgDescriptor = Picasso.with(context)
				.load(cursor.getString(Query.IMG))
				.fit()
				.centerCrop()
				.placeholder(mPlaceholderDrawable);
			h.title.setText(cursor.getString(Query.TITLE));
			h.date.setText(Utils.formatDate(cursor.getLong(Query.DATE)));

			if (cursor.getInt(Query.COMMENTS_COUNT) <= 0) {
				h.comments.setVisibility(View.GONE);
			} else {
				h.comments.setVisibility(View.VISIBLE);
				h.comments.setText(cursor.getString(Query.COMMENTS_COUNT));
			}

			if (cursor.getInt(Query.IS_READ) == 1) {
				h.img.setAlpha(1.0f);
				h.body.setBackgroundColor(getColor(position));
			} else {
				h.img.setAlpha(0.1f);
				view.setBackgroundColor(getColor(cursor.getPosition()));
				h.body.setBackgroundColor(0x00000000);
			}
			imgDescriptor.into(h.img);

			if (position < mGrid.getNumColumns()) {
				TypedValue tv = new TypedValue();

				if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
					int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
						context.getResources().getDisplayMetrics());
					view.setPadding(view.getPaddingLeft(), actionBarHeight, view.getPaddingRight(), view.getPaddingBottom());
				}
			} else {
				view.setPadding(view.getPaddingLeft(), view.getPaddingBottom(), view.getPaddingRight(), view.getPaddingBottom());
			}
		}
	}

	private static class ViewHolder {
		ImageView img;
		TextView title;
		TextView date;
		TextView comments;
		View body;

		ViewHolder(View convertView) {
			img = (ImageView) convertView.findViewById(R.id.thumbnail);
			title = (TextView) convertView.findViewById(R.id.title);
			date = (TextView) convertView.findViewById(R.id.date);
			comments = (TextView) convertView.findViewById(R.id.comments);
			body = convertView.findViewById(R.id.body);
		}
	}

	interface Query {
		String[] PROJECTION = new String[]{
				NewsContract.NewsFeed._ID,
				NewsContract.NewsFeed.IMG_URL,
				NewsContract.NewsFeed.TITLE,
				NewsContract.NewsFeed.DATE,
				NewsContract.NewsFeed.COMMENTS_COUNT,
				NewsContract.NewsFeed.FULL_NEWS_URL,
				NewsContract.NewsFeed.IS_READ
		};

		int IMG = 1;
		int TITLE = 2;
		int DATE = 3;
		int COMMENTS_COUNT = 4;
		int FULL_NEW_URL = 5;
		int IS_READ = 6;
	}
}
