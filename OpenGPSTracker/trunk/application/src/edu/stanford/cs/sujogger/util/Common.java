package edu.stanford.cs.sujogger.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
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
	
	public static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
	
	public static String timeString(int hour, int minute) {
		int hour12 = hour;
		String ampm = "AM";
		if (hour12 >= 12) {
			ampm = "PM";
			if (hour12 > 12)
				hour12 -= 12;
		}
		else if (hour12 == 0)
			hour12 = 12;
		return new StringBuilder()
			.append(hour12).append(":")
			.append(Common.pad(minute)).append(" ")
			.append(ampm).toString();
	}
	
	public static String distanceString(Context context, double val) {
		UnitsI18n mUnits = new UnitsI18n(context, null);
		double mDistance = mUnits.conversionFromMeter(val);
		return String.format("%.2f %s", mDistance, mUnits.getDistanceUnit());
	}
	
	public static String durationString(Context context, long val) {
		long durationSec = val / 1000;
		long hours, minutes, seconds;
		hours = durationSec / 3600;
		val = durationSec - (hours * 3600);
		minutes = durationSec / 60;
		durationSec = durationSec - (minutes * 60);
		seconds = durationSec;
		
		String text = "";
		text += String.format("%02d", hours) + ":";
		text += String.format("%02d", minutes) + ":";
		text += String.format("%02d", seconds);
		return text;
	}
	
	public static String speedString(Context context, double val) {
		UnitsI18n mUnits = new UnitsI18n(context, null);
		double speed = mUnits.conversionFromMeterAndMiliseconds(val, 1);
		return String.format("%.2f %s", speed, mUnits.getSpeedUnit());
	}
}
