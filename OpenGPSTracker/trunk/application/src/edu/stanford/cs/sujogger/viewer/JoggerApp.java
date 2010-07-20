package edu.stanford.cs.sujogger.viewer;

import java.io.IOException;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import edu.stanford.cs.sujogger.db.DatabaseHelper;

public class JoggerApp extends TabActivity {
	private static final int TAB_HEIGHT = 40;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DatabaseHelper dbHelper = new DatabaseHelper(this);
		try {
			dbHelper.createDatabase();
		} catch (IOException e) {
			throw new Error("Unable to create database");
		}
		dbHelper.close();
		
		TabHost host = getTabHost();
		host.addTab(host.newTabSpec("tracks").setIndicator("Tracks").
				setContent(new Intent(this, TrackList.class)));
		host.addTab(host.newTabSpec("achievements").setIndicator("Badges").
				setContent(new Intent(this, AchievementCatList.class)));
		host.addTab(host.newTabSpec("groups").setIndicator("People").
				setContent(new Intent(this, GroupList.class)));
		host.addTab(host.newTabSpec("feed").setIndicator("Feed").
				setContent(new Intent(this, Feed.class)));
		
		// Scale tab heights down to 40dip
		final float scale = this.getResources().getDisplayMetrics().density;
		for (int i = 0; i < 4; i++)
			host.getTabWidget().getChildAt(i).getLayoutParams().height = (int)(TAB_HEIGHT * scale);
	}
}
