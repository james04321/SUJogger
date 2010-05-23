package edu.stanford.cs.sujogger.util;

import java.util.HashMap;
import java.util.Map;

import android.os.Environment;
import edu.stanford.cs.gaming.sdk.model.User;

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
	
	public static String nameListForUsers(User[] users) {
		String nameList = "";
		for (int i = 0; i < users.length; i++) {
			if (i == 0)
				nameList += users[i].first_name + " " + users[i].last_name;
			else
				nameList += ", " + users[i].first_name + " " + users[i].last_name;
		}
		                                   
		return nameList;
	}
	
	public static User getRegisteredUser() {
		User user = new User();
		user.id = 6;
		user.first_name = "James";
		user.last_name = "Yang";
		user.email = "jkyang09@stanford.edu";
		user.fb_id = 12345;
		user.fb_photo = "http://profile.ak.fbcdn.net/v224/841/70/n213003_7394.jpg";
		
		return user;
	}
}
