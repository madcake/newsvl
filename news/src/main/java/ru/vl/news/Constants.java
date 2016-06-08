package ru.vl.news;

/**
 * @author andrey.pogrebnoy
 */
public class Constants {
	public static final int ERR_LOADING = 0;
	public static final int ERR_PARSE = 1;

	public static final String EXTRA_ERR_CODE = "extra_err_code";
	public static final String ACTION_NEWS_FEED = "action_news_feed";
	public static final String ACTION_NEWS_VIEW = "action_news_view";

	public static final String SP_LAST_NEWS_FEED_UPDATE = "sp_last_news_feed_update";

	/**
	 * 1 час
	 */
	public static final long NEWS_FEED_UPDATE_RATE_MS = 3600000;
}
