package ru.vl.news.parser;

import android.content.ContentValues;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.vl.news.Utils;
import ru.vl.news.provider.NewsContract;

/**
 * @author andrey.pogrebnoy
 */
public class NewsViewParser implements Parser<NewsViewParser.NewsViewValues, Document> {
	private static final String TAG = NewsViewParser.class.getSimpleName();

	private final String mFullUrl;

	public NewsViewParser(String fullUrl) {
		mFullUrl = fullUrl;
	}

	@Override
	public NewsViewValues parse(Document data) {
		Element mainNewsDiv = data.body().select(".mainNews").first();

		String date = mainNewsDiv.select(".datas2").first().text();
		String title = mainNewsDiv.select("h1").first().text();

		Elements textHrefs = mainNewsDiv.select("p").select("a");
		for (Element textHref : textHrefs) {
			textHref.attr("href", textHref.absUrl("href"));
		}
		String text = mainNewsDiv.select("p").outerHtml();

		Element commentsLink = mainNewsDiv.select("div[style=float:left]").first();
		String commentsUrl = null;
		int commentsCount = 0;
		if (commentsLink != null) {
			commentsCount = Utils.tryParseCommentsText(commentsLink.text(), 0);
			commentsUrl = commentsLink.select("a").first().absUrl("href");
		}

		// Парсим картинки
		String imagesString = "";
		String bigImageUrl = null;
		try {
			Element bigImageElement = mainNewsDiv.select("img").get(1);
			if (bigImageElement != null) {
				bigImageUrl = bigImageElement.absUrl("src");
				imagesString += bigImageUrl + ";";
			}
		} catch (Exception ignored) {
		}

		Elements imagesElements = mainNewsDiv.select(".images").select("a");
		for (Element imageElement : imagesElements) {
			imagesString += imageElement.absUrl("href") + ";";
		}

		if (imagesString.length() > 0) {
			imagesString = imagesString.substring(0, imagesString.length() - 1);
		}

		// Парсим видео
		String videoUrl = "";
		Element videoElement = mainNewsDiv.select("iframe").first();
		if (videoElement != null) {
			videoUrl = videoElement.absUrl("src");
		} else {
			videoElement = mainNewsDiv.select("embed").first();
			if (videoElement != null){
				videoUrl = videoElement.absUrl("src");
			}
		}

		ContentValues contentValues = newCV(mFullUrl, title, date, text, commentsUrl, commentsCount, imagesString, videoUrl);
		return new NewsViewValues(contentValues, bigImageUrl);
	}

	private ContentValues newCV(String fullUrl, String title, String date, String text, String commentsUrl, int commentsCount, String images, String videoUrl) {
		ContentValues cv = new ContentValues();
		cv.put(NewsContract.NewsView.FULL_URL, fullUrl);
		cv.put(NewsContract.NewsView.TITLE, title);
		cv.put(NewsContract.NewsView.DATE, date);
		cv.put(NewsContract.NewsView.TEXT, text);
		cv.put(NewsContract.NewsView.COMMENTS_URL, commentsUrl);
		cv.put(NewsContract.NewsView.COMMENTS_COUNT, commentsCount);
		cv.put(NewsContract.NewsView.IMAGES, images);
		cv.put(NewsContract.NewsView.VIDEO_URL, videoUrl);
		return cv;
	}

	public static class NewsViewValues {
		public final ContentValues contentValues;
		public final String bigImageUrl;

		public NewsViewValues(ContentValues contentValues, String bigImageUrl) {
			this.contentValues = contentValues;
			this.bigImageUrl = bigImageUrl;
		}
	}
}
