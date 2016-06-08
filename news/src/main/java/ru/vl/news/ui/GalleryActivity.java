package ru.vl.news.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

import ru.vl.news.R;

public class GalleryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent() == null || !getIntent().hasExtra("photos")) {
			finish();
			return;
		}
		setContentView(R.layout.activity_gallery);
		FragmentManager fm = getFragmentManager();
		GalleryFragment gallery = (GalleryFragment) fm.findFragmentByTag("gallery");

		if (gallery == null) {
			gallery = new GalleryFragment();
			gallery.setArguments(getIntent().getExtras());
		}
		fm.beginTransaction().replace(R.id.container, gallery, "gallery").commit();
	}
}
