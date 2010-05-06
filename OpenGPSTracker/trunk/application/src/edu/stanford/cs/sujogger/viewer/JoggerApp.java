package edu.stanford.cs.sujogger.viewer;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class JoggerApp extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TabHost host = getTabHost();
		host.addTab(host.newTabSpec("tracks").setIndicator("Tracks").
				setContent(new Intent(this, TrackList.class)));
		host.addTab(host.newTabSpec("achievements").setIndicator("Achievements").
				setContent(new Intent(this, AchievementsActivity.class)));
		/*
		host.addTab(host.newTabSpec("groups").setIndicator("Groups").
				setContent(new Intent(this, null)));
		host.addTab(host.newTabSpec("feed").setIndicator("Feed").
				setContent(new Intent(this, null)));*/
	}

}
