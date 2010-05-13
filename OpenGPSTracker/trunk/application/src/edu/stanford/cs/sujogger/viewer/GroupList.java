package edu.stanford.cs.sujogger.viewer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.util.GroupListAdapter;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

public class GroupList extends ListActivity {
	private static final String TAG = "OGT.GroupList";
	private final static String ITEM_TITLE = "title";
	
	private DatabaseHelper mDbHelper;
	private Cursor mGroupsCursor;
	private SeparatedListAdapter mGroupAdapter;
	private List<Map<String,?>> actions;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.achievementcatlist);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		// Create cursors
		mGroupsCursor = mDbHelper.getGroups();
		startManagingCursor(mGroupsCursor);
		
		actions = new LinkedList<Map<String,?>>();
		actions.add(createItem("New group"));

		fillData();
		registerForContextMenu(getListView());
	}
	
	private Map<String,?> createItem(String title) {
		Map<String,String> item = new HashMap<String,String>();
		item.put(ITEM_TITLE, title);
		return item;
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
		Log.d(TAG, "onResume(): dumping groups cursor");
		DatabaseUtils.dumpCursor(mGroupsCursor);
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}
	
	private void fillData() {
		mGroupAdapter = new SeparatedListAdapter(this);
		
		mGroupAdapter.addSection("", new SimpleAdapter(this, actions, R.layout.list_item_simple,
  			  new String[] {ITEM_TITLE}, new int[] {R.id.list_simple_title}));
		
		mGroupAdapter.addSection("My Groups", new GroupListAdapter(this, mGroupsCursor, true));
		
		setListAdapter(mGroupAdapter);
	}
	
}
