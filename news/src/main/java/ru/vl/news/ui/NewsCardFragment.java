package ru.vl.news.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.vl.news.Constants;
import ru.vl.news.GA;
import ru.vl.news.R;
import ru.vl.news.Utils;
import ru.vl.news.VideoUtils;
import ru.vl.news.provider.NewsContract;
import ru.vl.news.service.NewsViewService;
import ru.vl.news.view.LoadingView;
import ru.vl.news.view.ScrollContainer;

public class NewsCardFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>,
		View.OnClickListener,
		ViewPager.OnPageChangeListener,
		ScrollContainer.OnScrollChanged {

	private static final String EXTRA_NEWS_URL = "news_url";

	public static NewsCardFragment newInstance(String fullUrl) {
		NewsCardFragment fragment = new NewsCardFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_NEWS_URL, fullUrl);
		fragment.setArguments(args);
		return fragment;
	}

	private String mTitle;
	private View mNext;
	private View mPageContainer;
	private String mCommentsUrl;
	private ViewPager mPager;
	private TextView mCommentCount;
	private TextView mDate;
	private TextView mText;
	private String mNewsUrl;
	private LoadingView mLoadingView;
	private View mThumbnailContainer;
	private ImageView mVideoThumbnail;
	private float mDensity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mNewsUrl = getArguments().getString(EXTRA_NEWS_URL);
		NewsViewService.start(activity, mNewsUrl);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mDensity = inflater.getContext().getResources().getDisplayMetrics().density;
		return inflater.inflate(R.layout.fragment_news_card, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mPager = (ViewPager) view.findViewById(R.id.pager);
		mPageContainer = view.findViewById(R.id.page_container);
		mCommentCount = (TextView) view.findViewById(R.id.comments);
		mLoadingView = (LoadingView) view.findViewById(R.id.loading_view);
		mDate = (TextView) view.findViewById(R.id.date);
		mText = (TextView) view.findViewById(R.id.text);
		mThumbnailContainer = view.findViewById(R.id.thumbnail_container);
		mVideoThumbnail = (ImageView) view.findViewById(R.id.video_thumbnail);
		ScrollContainer mScrollContainer = (ScrollContainer) view.findViewById(R.id.scroll_container);
		mScrollContainer.setOnScrollListener(this);

		mLoadingView.setOnRetryListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoadingView.showLoading();
				NewsViewService.start(v.getContext(), mNewsUrl);
			}
		});
		mNext = view.findViewById(R.id.next);
		mNext.setOnClickListener(this);
		mText.setMovementMethod(LinkMovementMethod.getInstance());
		mPager.setOnPageChangeListener(this);
		mPager.setOnClickListener(this);

		GA.sendScreen(view.getContext(), R.string.ga_screen_view);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.share, menu);
		MenuItem item = menu.findItem(R.id.menu_item_share);
		ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();
		shareActionProvider.setShareIntent(getShareIntent());
		shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
			@Override
			public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
				GA.sendEvent(getActivity(), R.string.ga_event_category_common, R.string.ga_event_share);
				return false;
			}
		});

		if (!TextUtils.isEmpty(mTitle)) {
			getActivity().setTitle(mTitle);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver,
				new IntentFilter(Constants.ACTION_NEWS_VIEW));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		mLoadingView.showLoading();
		return new CursorLoader(getActivity(),
				NewsContract.NewsView.CONTENT_URI,
				Query.PROJECTION,
				NewsContract.NewsView.FULL_URL + "=?",
				new String[]{mNewsUrl},
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
		if (data.getCount() <= 0 || !data.moveToFirst()) {
			return;
		}
		mTitle = data.getString(Query.TITLE);
		getActivity().invalidateOptionsMenu();

		if (data.getInt(Query.COMMENTS_COUNT) <= 0) {
			mCommentCount.setVisibility(View.GONE);
		} else {
			mCommentCount.setVisibility(View.VISIBLE);
			mCommentCount.setText(getString(R.string.read_comments, data.getString(Query.COMMENTS_COUNT)));
			mCommentCount.setOnClickListener(this);
			mCommentsUrl = data.getString(Query.COMMENTS_URL);
		}
		mDate.setText(data.getString(Query.DATE));
		mText.setText(Html.fromHtml(data.getString(Query.TEXT)));
		String imagesSrc = data.getString(Query.IMAGES);

		if (TextUtils.isEmpty(imagesSrc)) {
			mPageContainer.setVisibility(View.GONE);
		} else {
			mPageContainer.setVisibility(View.VISIBLE);
			String[] images = imagesSrc.split(";");
			mPager.setAdapter(new ImageAdapter(getActivity(), images, this));

			if (images.length == 1) {
				mNext.setVisibility(View.GONE);
			} else {
				mNext.setVisibility(View.VISIBLE);
			}
		}

		// Загрузка видео, если есть
		String videoUrl = data.getString(Query.VIDEO_URL);
		if (!TextUtils.isEmpty(videoUrl)) {
			final String youtubeId = VideoUtils.tryGetYouTubeId(videoUrl);
			if (youtubeId != null) {
				Picasso.with(getActivity()).load(VideoUtils.getYouTubeThumbnailUrl(youtubeId)).into(mVideoThumbnail);
				mThumbnailContainer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Utils.startActivitySafe(v.getContext(), VideoUtils.makeYouTubeIntent(youtubeId), R.string.err_app_not_found);
					}
				});
			} else {
				mThumbnailContainer.setVisibility(View.GONE);
			}
		} else {
			mThumbnailContainer.setVisibility(View.GONE);
		}
		mLoadingView.showLoaded();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.next:
				if (mPager.getCurrentItem() < mPager.getAdapter().getCount() - 1) {
					mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
					GA.sendEvent(getActivity(), R.string.ga_event_category_common, R.string.ga_event_image_scrolled_by_next);
				}
				break;
			case R.id.comments:
				GA.sendEvent(getActivity(), R.string.ga_event_category_common, R.string.ga_event_comments);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mCommentsUrl));
				startActivity(i);
				break;
			case R.id.img:
				Intent intent = new Intent(getActivity(), GalleryActivity.class);
				intent.putExtra("photos", ((ImageAdapter) mPager.getAdapter()).getImages());
				intent.putExtra("position", mPager.getCurrentItem());
				getActivity().startActivity(intent);
				break;
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		if (position + 1 == mPager.getAdapter().getCount()) {
			mNext.setVisibility(View.GONE);
		} else {
			mNext.setVisibility(View.VISIBLE);
		}
		GA.sendEvent(getActivity(), R.string.ga_event_category_common, R.string.ga_event_image_scrolled);
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onScroll(ScrollContainer view, int x, int y, int oldX, int oldY) {
		float offset = 102 * mDensity;
		if (y + view.getHeight() >= view.getChildAt(0).getHeight())
			return;
		if (y > oldY && y > offset) {
			getActivity().getActionBar().hide();
		} else if ((y < oldY + offset || y < offset) && !getActivity().getActionBar().isShowing()) {
			getActivity().getActionBar().show();
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().containsKey(Constants.EXTRA_ERR_CODE)) {
				mLoadingView.showDefaultFail();
			}
		}
	};

	public Intent getShareIntent() {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
		i.putExtra(Intent.EXTRA_TEXT, mNewsUrl);
		return i;
	}

	private static class ImageAdapter extends PagerAdapter {
		private final String[] mImages;
		private final Context mContext;
		private final LayoutInflater mInflater;
		private final View.OnClickListener mOnClickListener;

		ImageAdapter(Context context, String[] images, View.OnClickListener onClickListener) {
			mContext = context;
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mImages = images;
			mOnClickListener = onClickListener;
		}

		@Override
		public int getCount() {
			return mImages.length;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final View itemView = mInflater.inflate(R.layout.item_news_card_image, container, false);
			final ImageView photoView = (ImageView) itemView.findViewById(R.id.img);
			Picasso.with(mContext)
					.load(mImages[position])
					.placeholder(R.drawable.stab_img)
					.fit()
					.centerInside()
					.into(photoView);
			container.addView(itemView);
			photoView.setOnClickListener(mOnClickListener);
			return itemView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Picasso.with(mContext)
					.cancelRequest((ImageView) ((View) object).findViewById(R.id.img));
			container.removeView((View) object);

		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		public String[] getImages() {
			return mImages;
		}
	}

	interface Query {
		String[] PROJECTION = new String[]{
				NewsContract.NewsView._ID,
				NewsContract.NewsView.DATE,
				NewsContract.NewsView.IMAGES,
				NewsContract.NewsView.TEXT,
				NewsContract.NewsView.TITLE,
				NewsContract.NewsView.COMMENTS_COUNT,
				NewsContract.NewsView.COMMENTS_URL,
				NewsContract.NewsView.VIDEO_URL
		};

		int DATE = 1;
		int IMAGES = 2;
		int TEXT = 3;
		int TITLE = 4;
		int COMMENTS_COUNT = 5;
		int COMMENTS_URL = 6;
		int VIDEO_URL = 7;
	}
}
