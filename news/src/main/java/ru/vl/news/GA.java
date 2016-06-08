package ru.vl.news;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * @author andrey.pogrebnoy
 */
public class GA {
	public static void sendScreen(Context context, int screenStringId) {
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS)
			return;
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
		Tracker tracker = analytics.newTracker(R.xml.global_tracker);
		tracker.setScreenName(context.getString(screenStringId));
		tracker.send(new HitBuilders.AppViewBuilder().build());
	}

	public static void sendEvent(Context context, int eventCategoryStringId, int eventActionStringId) {
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS)
			return;
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
		Tracker tracker = analytics.newTracker(R.xml.global_tracker);
		tracker.send(new HitBuilders.EventBuilder()
				.setCategory(context.getString(eventCategoryStringId))
				.setAction(context.getString(eventActionStringId))
				.build());
	}

	public static void sendEvent(Context context, int eventCategoryStringId, String eventActionString) {
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS)
			return;
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
		Tracker tracker = analytics.newTracker(R.xml.global_tracker);
		tracker.send(new HitBuilders.EventBuilder()
				.setCategory(context.getString(eventCategoryStringId))
				.setAction(eventActionString)
				.build());
	}
}
