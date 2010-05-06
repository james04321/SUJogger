package edu.stanford.cs.sujogger.viewer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

public class AchievementsActivity extends ListActivity {
	private static final String TAG = "OGT.AchievementsActivity";

	private List<Map<String, ?>> diffItems;
	private List<Map<String, ?>> typeItems;
	private List<Map<String, ?>> extItems;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.achievementcatlist);
		populateListSections();
		fillData();
		registerForContextMenu(getListView());
	}

	private void populateListSections() {
		diffItems = new LinkedList<Map<String, ?>>();
		diffItems.add(createItem("Easy", "0/4"));
		diffItems.add(createItem("Medium", "0/5"));
		diffItems.add(createItem("Hard", "0/5"));
		
		typeItems = new LinkedList<Map<String, ?>>();
		typeItems.add(createItem("Speed", "0/4"));
		typeItems.add(createItem("Endurance", "0/5"));
		typeItems.add(createItem("Determination", "0/5"));
		typeItems.add(createItem("Charisma", "0/5"));
		
		extItems = new LinkedList<Map<String, ?>>();
		extItems.add(createItem("5k", "0/12"));
		extItems.add(createItem("Half Marathon", "0/12"));
		extItems.add(createItem("Marathon", "0/12"));
	}

	private Map<String, ?> createItem(String title, String fraction) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_TITLE, title);
		item.put(ITEM_FRACTION, fraction);
		return item;
	}

	private final static String ITEM_TITLE = "title";
	private final static String ITEM_FRACTION = "fraction";

	private void fillData() {
		SeparatedListAdapter groupedAdapter = new SeparatedListAdapter(this);

		// Difficulty section
		groupedAdapter.addSection("Difficulty", new SimpleAdapter(this, diffItems, R.layout.achievementcatitem,
				new String[] { ITEM_TITLE, ITEM_FRACTION }, new int[] { R.id.achievementcat_name,
						R.id.achievementcat_fraction }));

		// Type section
		groupedAdapter.addSection("Type", new SimpleAdapter(this, typeItems, R.layout.achievementcatitem,
				new String[] { ITEM_TITLE, ITEM_FRACTION }, new int[] { R.id.achievementcat_name,
						R.id.achievementcat_fraction }));
		
		// Extended section
		groupedAdapter.addSection("Extended", new SimpleAdapter(this, extItems, R.layout.achievementcatitem,
				new String[] { ITEM_TITLE, ITEM_FRACTION }, new int[] { R.id.achievementcat_name,
						R.id.achievementcat_fraction }));
		
		// groupedAdapter.addSection("My Tracks", trackAdapter);

		setListAdapter(groupedAdapter);
	}

}
