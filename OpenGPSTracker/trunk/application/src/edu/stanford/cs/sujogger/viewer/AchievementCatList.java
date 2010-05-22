package edu.stanford.cs.sujogger.viewer;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Categories;
import edu.stanford.cs.sujogger.util.AchCatAdapter;
import edu.stanford.cs.sujogger.util.AchListAdapter;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

public class AchievementCatList extends ListActivity {
	private static final String TAG = "OGT.AchievementsActivity";

	private DatabaseHelper mDbHelper;
	private Cursor mRecAchEarned;
	private Cursor mRecAchLost;
	private SeparatedListAdapter mGroupedAdapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.list_simple);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		// Create cursors
		mRecAchEarned = mDbHelper.getRecentAchievementsEarned();
		startManagingCursor(mRecAchEarned);
		mRecAchLost = mDbHelper.getRecentAchievementsLost();
		startManagingCursor(mRecAchLost);

		fillData();
		registerForContextMenu(getListView());
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
		getListView().invalidateViews();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();

	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		Log.d(TAG, "fillData(): dumping recent achievements cursor");
		DatabaseUtils.dumpCursor(mRecAchEarned);

	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Log.v(TAG, "position = " + position + "; id = " + id);
		Object item = mGroupedAdapter.getItem(position);
		if (item.getClass() == Integer.class) {
			Log.d(TAG, "starting AchGridView for cat = " + (Integer)item);
			
			Intent i = new Intent(this, AchievementList.class);
	        i.putExtra(Categories.TABLE, (Integer)item);
	        startActivity(i);
		}
	}

	private void fillData() {
		mGroupedAdapter = new SeparatedListAdapter(this);

		mGroupedAdapter.addSection("Recently Earned", new AchListAdapter(this, mRecAchEarned, true));
		mGroupedAdapter.addSection("Recently Lost", new AchListAdapter(this, mRecAchLost, true));

		mGroupedAdapter.addSection("Difficulty", new AchCatAdapter(this, mDbHelper, 0));
		mGroupedAdapter.addSection("Type", new AchCatAdapter(this, mDbHelper, 1));

		setListAdapter(mGroupedAdapter);
	}

}
