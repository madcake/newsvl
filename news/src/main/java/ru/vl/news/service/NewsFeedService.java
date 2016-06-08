package ru.vl.news.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import ru.vl.news.Constants;
import ru.vl.news.parser.NewsFeedParser;
import ru.vl.news.provider.NewsContract;

/**
 * @author andrey.pogrebnoy
 */
public class NewsFeedService extends IntentService {

	private static final String TAG = NewsFeedService.class.getSimpleName();

	public static void start(Context context) {
		Intent intent = new Intent(context, NewsFeedService.class);
		context.startService(intent);
	}

	public NewsFeedService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Document doc;

//		long start = System.currentTimeMillis();
//		try {
//			doc = Jsoup.connect("http://www.newsvl.ru/vlad").get();
//		} catch (IOException e) {
//			broadcastFail(Constants.ERR_LOADING);
//			return;
//		}
//		try {
//			Log.d(TAG, "download time=" + (System.currentTimeMillis() - start));
//			ContentValues[] newsFeed = new NewsFeedParser().parse(doc);
//			Log.d(TAG, "+parse time=" + (System.currentTimeMillis() - start));
//			getContentResolver().bulkInsert(NewsContract.NewsFeed.CONTENT_URI, newsFeed);
//
//			PreferenceManager.getDefaultSharedPreferences(this)
//					.edit()
//					.putLong(Constants.SP_LAST_NEWS_FEED_UPDATE, System.currentTimeMillis())
//					.apply();
//		} catch (Exception e) {
//			broadcastFail(Constants.ERR_PARSE);
//		}

		try {
			doc = Jsoup.connect("http://www.newsvl.ru/rss").get();
			Elements items = doc.getElementsByTag("item");
			int len = items.size();
			ContentValues[] values = new ContentValues[len];

			for (int i = 0; i < len; i++) {
				Element item = items.get(i);
				ContentValues cv = new ContentValues();
				cv.put(NewsContract.NewsFeed.TITLE, item.getElementsByTag("title").first().text());
				cv.put(NewsContract.NewsFeed.DATE, item.getElementsByTag("pubDate").first().text());
				cv.put(NewsContract.NewsFeed.COMMENTS_COUNT, 0);
				cv.put(NewsContract.NewsFeed.FULL_NEWS_URL, item.getElementsByTag("guid").first().text());
				cv.put(NewsContract.NewsFeed.IMG_URL, item.getElementsByTag("enclosure").first().attr("url"));
				values[i] = cv;
			}
			getContentResolver().bulkInsert(NewsContract.NewsFeed.CONTENT_URI, values);

			PreferenceManager.getDefaultSharedPreferences(this)
					.edit()
					.putLong(Constants.SP_LAST_NEWS_FEED_UPDATE, System.currentTimeMillis())
					.apply();
		} catch (IOException e) {
			broadcastFail(Constants.ERR_LOADING);
		}
	}

	private void broadcastFail(int errCode) {
		Intent intent = new Intent();
		intent.setAction(Constants.ACTION_NEWS_FEED);
		intent.putExtra(Constants.EXTRA_ERR_CODE, errCode);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
