package ru.vl.news.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import ru.vl.news.Constants;
import ru.vl.news.parser.NewsViewParser;
import ru.vl.news.provider.NewsContract;

/**
 * @author andrey.pogrebnoy
 */
public class NewsViewService extends IntentService {

	private static final String TAG = NewsViewService.class.getSimpleName();
	private static final String EXTRA_FULL_NEWS_URL = "full_news_url";

	public static void start(Context context, String fullNewsUrl) {
		Intent intent = new Intent(context, NewsViewService.class);
		intent.putExtra(EXTRA_FULL_NEWS_URL, fullNewsUrl);
		context.startService(intent);
	}

	public NewsViewService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String newsUrl = intent.getStringExtra(EXTRA_FULL_NEWS_URL);
		if (newsUrl == null) {
			return;
		}
		Cursor cursor = getContentResolver().query(NewsContract.NewsView.CONTENT_URI,
				new String[]{NewsContract.NewsView._ID},
				NewsContract.NewsView.FULL_URL + "=?",
				new String[]{newsUrl}, null);
		if (cursor.getCount() > 0) {
			return;
		}
		cursor.close();

		long start = System.currentTimeMillis();
		Document doc;
		try {
			doc = Jsoup.connect(newsUrl).get();
		} catch (Exception e) {
			broadcastFail(Constants.ERR_LOADING);
			return;
		}
		try {
			Log.d(TAG, "download time=" + (System.currentTimeMillis() - start));
			NewsViewParser.NewsViewValues newsView = new NewsViewParser(newsUrl).parse(doc);
			Log.d(TAG, "+parse time=" + (System.currentTimeMillis() - start));
			getContentResolver().insert(NewsContract.NewsView.CONTENT_URI, newsView.contentValues);

			if (!TextUtils.isEmpty(newsView.bigImageUrl)) {
				ContentValues updateValues = new ContentValues();
				updateValues.put(NewsContract.NewsFeed.IMG_URL, newsView.bigImageUrl);
				getContentResolver().update(
						NewsContract.NewsFeed.CONTENT_URI,
						updateValues,
						NewsContract.NewsFeed.FULL_NEWS_URL + "=?",
						new String[]{newsUrl});
			}
		} catch (Exception e) {
			broadcastFail(Constants.ERR_PARSE);
		}
	}

	private void broadcastFail(int errCode) {
		Intent intent = new Intent();
		intent.setAction(Constants.ACTION_NEWS_VIEW);
		intent.putExtra(Constants.EXTRA_ERR_CODE, errCode);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
