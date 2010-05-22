package edu.stanford.cs.sujogger.util;

import java.util.HashMap;
import java.util.Map;
import android.os.Environment;

public class Common {
	public final static String ITEM_TITLE = "title";
	public static final String CACHE_DIRECTORY = Environment.getExternalStorageDirectory() + "/.sujogger-user-image-cache/";
	
	public static Map<String, ?> createItem(String title) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_TITLE, title);
		return item;
	}
	
	public static String getCacheFileName(String url) {
		return CACHE_DIRECTORY + url.hashCode() + ".jpg";
	}
}
