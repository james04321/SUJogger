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
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.util.GroupListAdapter;

public class GroupPicker extends ListActivity {
	private static final String TAG = "OGT.GroupPicker";
	
	private DatabaseHelper mDbHelper;
	private Cursor mGroupsCursor;
	private GroupListAdapter mGroupAdapter;
	
	public static final int GRP_SELECT_RESULT = 1;
	
	public GroupPicker() {}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_simple);
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		// Create cursors
		mGroupsCursor = mDbHelper.getGroups();
		startManagingCursor(mGroupsCursor);
		
		fillData();
	}
	
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		Log.d(TAG, "onResume(): dumping users cursor");
		DatabaseUtils.dumpCursor(mGroupsCursor);
	}
	
	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		
		Bundle bundle = new Bundle();
		bundle.putLong(Groups.TABLE, (Integer)mGroupAdapter.getItem(position));
		Intent i = new Intent();
		i.putExtras(bundle);
		Log.d(TAG, "onListItemClick(): groupId = " + (Integer)mGroupAdapter.getItem(position));
		setResult(GRP_SELECT_RESULT, i);
		finish();
	}
	
	private void fillData() {
		mGroupAdapter = new GroupListAdapter(this, mGroupsCursor, true);
		setListAdapter(mGroupAdapter);
	}
}
