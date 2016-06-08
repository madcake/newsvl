package ru.vl.news.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ru.vl.news.R;

public class GalleryFragment extends Fragment {
	
	private String[] mPhotos;
	private int mStartPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPhotos = getArguments().getStringArray("photos");
		mPhotos = mPhotos == null ? new String[0] : mPhotos;
		mStartPosition = getArguments().getInt("position", 0);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gallery, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final ViewPager pager = (ViewPager) getView().findViewById(R.id.gallery);
		pager.setAdapter(new GalleryAdapter(getActivity(), mPhotos));
		pager.setCurrentItem(mStartPosition);

		getActivity().getActionBar().hide();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
	}

	private class GalleryAdapter extends PagerAdapter {
		private final String[] mImages;
		Context context;

		public GalleryAdapter(Context context, String[] images) {
			this.context = context;
			mImages = images;
		}

		@Override
		public int getCount() {
			return mImages.length;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final View itemView = getActivity().getLayoutInflater().inflate(R.layout.item_gallery_image, container, false);
			Picasso.with(context)
				.load(mPhotos[position])
				.fit()
				.centerInside()
				.into((ImageView) itemView.findViewById(R.id.img));
			container.addView(itemView);
			return itemView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Picasso.with(context)
				.cancelRequest((ImageView) ((View) object).findViewById(R.id.img));
			container.removeView((View)object);

		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
