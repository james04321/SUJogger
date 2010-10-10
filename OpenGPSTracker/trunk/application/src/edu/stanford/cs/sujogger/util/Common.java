package edu.stanford.cs.sujogger.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.GPStracking.Achievements;

public class Common {
	public static final String ITEM_TITLE = "title";
	public static final String CACHE_DIRECTORY = Environment.getExternalStorageDirectory() + 
		"/.sujogger-user-image-cache/";
	
	public static Map<String, ?> createItem(String title) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_TITLE, title);
		return item;
	}
	
	public static String getCacheFileName(String url) {
		if (url == null) return null;
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
	
	public static User getRegisteredUser(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
		User user = new User();
		
		user.id = prefs.getInt(Constants.USERREG_ID_KEY, 0);
		user.first_name = prefs.getString(Constants.USERREG_FIRSTNAME_KEY, null);
		user.last_name = prefs.getString(Constants.USERREG_LASTNAME_KEY, null);;
		user.email = prefs.getString(Constants.USERREG_EMAIL_KEY, null);;
		user.fb_id = prefs.getLong(Constants.USERREG_FBID_KEY, 0);
		user.fb_photo = prefs.getString(Constants.USERREG_PICTURE_KEY, null);;
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
		durationSec = durationSec - (hours * 3600);
		minutes = durationSec / 60;
		durationSec = durationSec - (minutes * 60);
		seconds = durationSec;
		
		String text = "";
		text += String.format("%02d", hours) + ":";
		text += String.format("%02d", minutes) + ":";
		text += String.format("%02d", seconds);
		return text;
	}
	
	public static String dateString(long val) {
		Date date = new Date(val);
	    return DateFormat.getInstance().format(date);
	}
	
	public static String speedString(Context context, double val) {
		UnitsI18n mUnits = new UnitsI18n(context, null);
		double speed = mUnits.conversionFromMeterAndMiliseconds(val, 1);
		return String.format("%.2f %s", speed, mUnits.getSpeedUnit());
	}
	
	public static void displayAchievementToast(String title, int imgRes, boolean earned, Context context, View toastLayout) {
		ImageView image = (ImageView) toastLayout.findViewById(R.id.toast_ach_image);
		image.setImageResource(imgRes);
		TextView text = (TextView) toastLayout.findViewById(R.id.toast_ach_desc);
		String suffix = "achievement earned!";
		if (!earned) suffix = "achievement lost";
		text.setText(title + " " + suffix);
		
		Toast achToast = new Toast(context);
		achToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		achToast.setDuration(Toast.LENGTH_LONG);
		achToast.setView(toastLayout);
		achToast.show();
	}
}
