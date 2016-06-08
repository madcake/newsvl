package ru.vl.news.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author andrey.pogrebnoy
 */
public class NewsContract {
	interface NewsFeedColumns extends BaseColumns {
		String TITLE = "title";
		String DATE = "date";
		String COMMENTS_COUNT = "comments_count";
		String FULL_NEWS_URL = "full_news_url";
		String IMG_URL = "pic_url";
		String IS_READ = "is_read";
	}

	interface NewsViewColumns extends BaseColumns {
		String FULL_URL = "full_url";
		String TITLE = "title";
		String DATE = "date";
		String TEXT = "text";
		/**
		 * Список картинок, найденных в новости, через ";"
		 */
		String IMAGES = "images";
		String COMMENTS_COUNT = "comments_count";
		String COMMENTS_URL = "comments_url";
		String VIDEO_URL = "video_url";
	}

	public static final String CONTENT_AUTHORITY = "ru.vl.news";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final String PATH_NEWS_FEED = "news_feed";
	private static final String PATH_NEWS_VIEW = "news_view";

	public static abstract class NewsFeed implements NewsFeedColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS_FEED).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.vl.ri.news_feed";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.vl.ri.news_feed";
	}

	public static abstract class NewsView implements NewsViewColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS_VIEW).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.vl.ri.news_view";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.vl.ri.news_view";
	}
}
