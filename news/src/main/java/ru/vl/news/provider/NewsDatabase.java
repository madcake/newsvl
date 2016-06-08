package ru.vl.news.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author andrey.pogrebnoy
 */
public class NewsDatabase extends SQLiteOpenHelper {

	private static final String DB_NAME = "newsvl.db";
	private static final int DB_VERSION = 2;

	interface Tables {
		String NEWS_FEED = "news_feed";
		String NEWS_VIEW = "news_view";
	}

	public NewsDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.NEWS_FEED + " ("
				+ NewsContract.NewsFeed._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ NewsContract.NewsFeed.TITLE + " text,"
				+ NewsContract.NewsFeed.DATE + " int,"
				+ NewsContract.NewsFeed.COMMENTS_COUNT + " int,"
				+ NewsContract.NewsFeed.FULL_NEWS_URL + " text unique,"
				+ NewsContract.NewsFeed.IMG_URL + " text,"
				+ NewsContract.NewsFeed.IS_READ + " int default 0"
				+ ")");

		db.execSQL("CREATE TABLE " + Tables.NEWS_VIEW + " ("
				+ NewsContract.NewsView._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ NewsContract.NewsView.FULL_URL + " text unique,"
				+ NewsContract.NewsView.TITLE + " text,"
				+ NewsContract.NewsView.DATE + " text,"
				+ NewsContract.NewsView.COMMENTS_COUNT + " int,"
				+ NewsContract.NewsView.COMMENTS_URL + " text,"
				+ NewsContract.NewsView.IMAGES + " text,"
				+ NewsContract.NewsView.TEXT + " text,"
				+ NewsContract.NewsView.VIDEO_URL + " text"
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+Tables.NEWS_VIEW);
			db.execSQL("DROP TABLE IF EXISTS "+Tables.NEWS_FEED);
			onCreate(db);
		}
	}

	public static void deleteDatabase(Context context) {
		context.deleteDatabase(DB_NAME);
	}
}
