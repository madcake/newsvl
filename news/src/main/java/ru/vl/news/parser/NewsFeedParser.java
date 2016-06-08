package ru.vl.news.parser;

import android.content.ContentValues;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;

import ru.vl.news.Utils;
import ru.vl.news.provider.NewsContract;

/**
 * Парсер для странички http://www.newsvl.ru/vlad
 * @author andrey.pogrebnoy
 */
public class NewsFeedParser implements Parser<ContentValues[], Document> {
	private static final String TAG = NewsFeedParser.class.getSimpleName();

	@Override
	public ContentValues[] parse(Document data) {
		Element body = data.body();

		List<ContentValues> values = new LinkedList<ContentValues>();

		try {
			values.add(parseFirstNews(body));
		} catch (Exception e) {
			Log.d(TAG, "can't parse first news", e);
		}

		try {
			values.addAll(parseOldNews(body));
		} catch (Exception e) {
			Log.d(TAG, "can't parse old news", e);
		}

		return values.toArray(new ContentValues[values.size()]);
	}

	private ContentValues parseFirstNews(Element body) {
		Element firstNewsElement = body.select("table.main-news").get(0);
		String picUrl = firstNewsElement.select("img").get(0).absUrl("src");
		String date = firstNewsElement.select("div.datas").get(0).text().trim();

		Element titleHrefElement = firstNewsElement.select("h2").select("a").first();
		String title = titleHrefElement.text();
		String fullNewsUrl = titleHrefElement.absUrl("href");

		String commentsText = firstNewsElement.select("div.headn").select("a").text();
		int commentsCount = Utils.tryParseCommentsText(commentsText, 0);

		return newCV(title, Utils.parseFullDate(date), commentsCount, fullNewsUrl, picUrl);
	}

	private List<ContentValues> parseOldNews(Element body) {
		Elements newsColumns = body.select(".colmask");
		List<ContentValues> oldNewsList = new LinkedList<ContentValues>();
		Elements leftColumnNews = newsColumns.select("#newsLenta div.picLenta");
		Elements rightColumnNews = newsColumns.select(".col2 div.picLenta");
		for (Element leftNews : leftColumnNews) {
			try {
				oldNewsList.add(parseOldNewsValue(leftNews));
			} catch (Exception e) {
				Log.d(TAG, "can't parse old news value", e);
			}

		}
		for (Element rightNews : rightColumnNews) {
			try {
				oldNewsList.add(parseOldNewsValue(rightNews));
			} catch (Exception e) {
				Log.d(TAG, "can't parse old news value", e);
			}

		}
		return oldNewsList;
	}

	private ContentValues parseOldNewsValue(Element newsDiv) {
		Element infoLine = newsDiv.select(".info-line").get(0);
		String date = infoLine.select(".date-city-s").text();
		int commentsCount = Utils.tryParseCommentsText(infoLine.select(".comment-link").text(), 0);
		Element titleAndUrl = newsDiv.select("h3").select("a").get(0);
		String title = titleAndUrl.text();
		String fullNewsUrl = titleAndUrl.absUrl("href");

		String imgUrl = newsDiv.select("img").get(0).absUrl("src");
		return newCV(title, Utils.parseFullDate(date), commentsCount, fullNewsUrl, imgUrl);
	}


	private ContentValues newCV(String title, long date, int commentsCount, String fullNewsUrl, String imgUrl) {
		ContentValues cv = new ContentValues();
		cv.put(NewsContract.NewsFeed.TITLE, title);
		cv.put(NewsContract.NewsFeed.DATE, date);
		cv.put(NewsContract.NewsFeed.COMMENTS_COUNT, commentsCount);
		cv.put(NewsContract.NewsFeed.FULL_NEWS_URL, fullNewsUrl);
		cv.put(NewsContract.NewsFeed.IMG_URL, imgUrl);
		return cv;
	}
}
