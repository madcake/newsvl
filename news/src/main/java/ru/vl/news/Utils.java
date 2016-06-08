package ru.vl.news;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author andrey.pogrebnoy
 */
public class Utils {
	private static final Pattern sDigitsPattern = Pattern.compile("[0-9]+");
	private final static String[] RUS_MONTHES = {
			"января",
			"февраля",
			"марта",
			"апреля",
			"мая",
			"июня",
			"июля",
			"августа",
			"сентября",
			"октября",
			"ноября",
			"декабря"
	};
	private final static DateFormatSymbols DATE_FORMAT_SYMBOLS = new DateFormatSymbols();
	public final static SimpleDateFormat FULL_DATE_SDF;
	public final static SimpleDateFormat TIME_SDF = new SimpleDateFormat("HH:mm", new Locale("ru", "RU"));
	public final static SimpleDateFormat SHORT_FULL_DATE_SDF = new SimpleDateFormat("HH:mm dd.MM.yy", new Locale("ru", "RU"));

	static {
		DATE_FORMAT_SYMBOLS.setMonths(RUS_MONTHES);
		FULL_DATE_SDF = new SimpleDateFormat("HH:mm, dd MMMM yyyy", DATE_FORMAT_SYMBOLS);
		FULL_DATE_SDF.setTimeZone(TimeZone.getTimeZone("GMT+11"));
		SHORT_FULL_DATE_SDF.setTimeZone(TimeZone.getTimeZone("GMT+11"));
		//TIME_SDF.setTimeZone(TimeZone.getTimeZone("GMT+11"));
	}

	public static long parseFullDate(String dateText) {
		try {
			return FULL_DATE_SDF.parse(dateText).getTime();
		} catch (ParseException e) {
			return System.currentTimeMillis();
		}
	}

	public static long parseTime(String timeText) {
		int hour = Integer.valueOf(timeText.split(":")[0]);
		int minute = Integer.valueOf(timeText.split(":")[1]);
		return minute * 60000 + hour * 3600000;
	}

	public static String formatDate(long timestamp) {
		return SHORT_FULL_DATE_SDF.format(timestamp);
	}

	public static long getCurrentDateInMilis() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+11"));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	public static int tryParseCommentsText(String commentsText, int defaultValue) {
		Matcher matcher = sDigitsPattern.matcher(commentsText);
		if (matcher.find()) {
			String digitsText = matcher.group();
			try {
				return Integer.parseInt(digitsText);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Метод безопасно пытается стартануть активити по переданному интенту. Если не удалось, показывает юзеру тост
	 *
	 * @param context
	 * @param activityIntent
	 * @param toastMessageId
	 */
	public static void startActivitySafe(Context context, Intent activityIntent, int toastMessageId) {
		try {
			context.startActivity(activityIntent);
		} catch (ActivityNotFoundException e){
			Toast.makeText(context, context.getString(toastMessageId), Toast.LENGTH_SHORT).show();
		}
	}
}
