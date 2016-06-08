package ru.vl.news.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import ru.vl.news.Constants;
import ru.vl.news.service.NewsFeedService;

/**
 * @author andrey.pogrebnoy
 */
public class NewsProvider extends ContentProvider {
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static final int NEWS_FEED = 0x100;
	private static final int NEWS_FEED_ID = 0x101;
	private static final int NEWS_VIEW = 0x200;
	private static final int NEWS_VIEW_ID = 0x201;

	private NewsDatabase mOpenHelper;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = NewsContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, "news_feed", NEWS_FEED);
		matcher.addURI(authority, "news_feed/#", NEWS_FEED_ID);
		matcher.addURI(authority, "news_view", NEWS_VIEW);
		matcher.addURI(authority, "news_view/#", NEWS_VIEW_ID);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new NewsDatabase(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		Cursor c;

		switch (match) {
			default: {
				final SelectionBuilder builder = buildSimpleQuery(uri);
				String limit = uri.getQueryParameter("limit");
				c = builder.where(selection, selectionArgs)
						.query(db, projection, sortOrder, limit);
			}
		}
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);

		switch (match) {
			case NEWS_FEED:
				return NewsContract.NewsFeed.CONTENT_TYPE;
			case NEWS_FEED_ID:
				return NewsContract.NewsFeed.CONTENT_ITEM_TYPE;
			case NEWS_VIEW:
				return NewsContract.NewsView.CONTENT_TYPE;
			case NEWS_VIEW_ID:
				return NewsContract.NewsView.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		long id;

		switch (match) {
			case NEWS_FEED:
			case NEWS_FEED_ID:
				id = db.insertOrThrow(NewsDatabase.Tables.NEWS_FEED, null, values);
				break;
			case NEWS_VIEW:
			case NEWS_VIEW_ID:
				id = db.insertOrThrow(NewsDatabase.Tables.NEWS_VIEW, null, values);
				break;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		Uri newUri = ContentUris.withAppendedId(uri, id);
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int match = sUriMatcher.match(uri);
		int result = values.length;

		switch (match) {
			case NEWS_FEED: {
				SQLiteDatabase db = mOpenHelper.getWritableDatabase();
				try {
					db.beginTransaction();
					for (ContentValues v : values) {
						db.insertWithOnConflict(NewsDatabase.Tables.NEWS_FEED, null, v, SQLiteDatabase.CONFLICT_IGNORE);
					}
					db.setTransactionSuccessful();
					result = values.length;
				} catch (SQLException ex) {
					ex.printStackTrace();
				} finally {
					db.endTransaction();
				}
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final int match = sUriMatcher.match(uri);

		if (uri == NewsContract.BASE_CONTENT_URI) {
			deleteDatabase();
			getContext().getContentResolver().notifyChange(uri, null, false);
			return 1;
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleQuery(uri);
		int retVal = builder
				.where(selection, selectionArgs)
				.delete(db);

		if (TextUtils.isEmpty(selection) && builder.getSelectionArgs().length == 0) {
			String table;

			switch (match) {
				case NEWS_FEED:
					table = NewsDatabase.Tables.NEWS_FEED;
					break;
				case NEWS_VIEW:
					table = NewsDatabase.Tables.NEWS_VIEW;
					break;
				default:
					throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
			db.delete("sqlite_sequence", "name=?", new String[]{table});
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	private void deleteDatabase() {
		mOpenHelper.close();
		Context context = getContext();
		NewsDatabase.deleteDatabase(context);
		mOpenHelper = new NewsDatabase(getContext());
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleQuery(uri);
		int retVal = builder.where(selection, selectionArgs).update(db, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	private SelectionBuilder buildSimpleQuery(Uri uri) {
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		String id;

		switch (match) {
			case NEWS_FEED:
				long lastUpdate = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong(Constants.SP_LAST_NEWS_FEED_UPDATE, 0);
				if (System.currentTimeMillis() - lastUpdate >= Constants.NEWS_FEED_UPDATE_RATE_MS) {
					NewsFeedService.start(getContext());
				}
				return builder.table(NewsDatabase.Tables.NEWS_FEED);
			case NEWS_FEED_ID:
				id = String.valueOf(ContentUris.parseId(uri));
				return builder.table(NewsDatabase.Tables.NEWS_FEED)
						.where(NewsContract.NewsFeed._ID + "=?", id);
			case NEWS_VIEW:
				return builder.table(NewsDatabase.Tables.NEWS_VIEW);
			case NEWS_VIEW_ID:
				id = String.valueOf(ContentUris.parseId(uri));
				return builder.table(NewsDatabase.Tables.NEWS_VIEW)
						.where(NewsContract.NewsFeed._ID + "=?", id);
			default: {
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
		}
	}
}
