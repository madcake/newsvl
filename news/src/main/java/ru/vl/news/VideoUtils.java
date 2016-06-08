package ru.vl.news;

import android.content.Intent;
import android.net.Uri;

/**
 * @author andrey.pogrebnoy
 */
public class VideoUtils {
	public static String tryGetYouTubeId(String url) {
		String videoId = null;
		if (!url.contains("youtu")) {
			return null;
		}
		if (url.contains("/embed/")) {
			int endIndex = url.indexOf('?');
			if (endIndex == -1) {
				videoId = url.substring(url.indexOf("/embed/") + 7);
			} else {
				videoId = url.substring(url.indexOf("/embed/") + 7, endIndex);
			}
		}
		if (videoId == null && url.contains("v=")) {
			videoId = url.substring(url.indexOf("v=") + 2);
			int nextParamIndex = videoId.indexOf('&');
			if (nextParamIndex != -1) {
				videoId = videoId.substring(0, nextParamIndex);
			}
		}
		if (videoId == null && url.contains("/v/")) {
			int endIndex = url.indexOf('?');
			if (endIndex == -1) {
				videoId = url.substring(url.indexOf("/v/") + 3);
			} else {
				videoId = url.substring(url.indexOf("/v/") + 3, endIndex);
			}
		}
		return videoId;
	}

	public static String getYouTubeThumbnailUrl(String videoId) {
		return "http://img.youtube.com/vi/" + videoId + "/0.jpg";
	}

	public static Intent makeYouTubeIntent(String videoId) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
		intent.putExtra("VIDEO_ID", videoId);
		return intent;
	}
}
