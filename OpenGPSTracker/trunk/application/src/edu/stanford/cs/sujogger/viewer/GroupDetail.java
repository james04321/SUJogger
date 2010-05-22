package edu.stanford.cs.sujogger.viewer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;
import edu.stanford.cs.sujogger.util.UserListAdapter;

public class GroupDetail extends ListActivity {
	private static final String TAG = "OGT.GroupDetail";
	
	private long mGroupId;
	private boolean mIsOwner;
	private DatabaseHelper mDbHelper;
	private Cursor mUsers;
	private SeparatedListAdapter mGroupedAdapter;
	
	private List<Map<String,?>> mActions;
	private List<Map<String,?>> mInfo;
	
	public GroupDetail() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_simple);

		mGroupId = savedInstanceState != null ? savedInstanceState.getLong(Groups.TABLE) : 0;

		if (mGroupId == 0) {
			Bundle extras = getIntent().getExtras();
			mGroupId = extras != null ? extras.getLong(Groups.TABLE) : 0;
		}
		
		Log.d(TAG, "onCreate(): groupId = " + mGroupId);
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mUsers = mDbHelper.getUsersForGroup(mGroupId);
		startManagingCursor(mUsers);
		
		Cursor groupInfo = mDbHelper.getGroupWithId(mGroupId);
		String groupName = null;
		if (groupInfo.moveToFirst()) {
			groupName = groupInfo.getString(2);
			mIsOwner = groupInfo.getInt(3) == 1 ? true : false;
		}
		groupInfo.close();
		
		if (groupName == null)
			this.setTitle("Unknown group");
		else
			this.setTitle(groupName);
		
		mActions = new LinkedList<Map<String,?>>();
		if (mIsOwner) mActions.add(Common.createItem("Add members"));
		mActions.add(Common.createItem("Send message"));
		
		mInfo = new LinkedList<Map<String,?>>();
		mInfo.add(Common.createItem("Statistics"));
		mInfo.add(Common.createItem("Achievements"));
		
		fillData();
	}
	
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		mGroupedAdapter.notifyDataSetChanged();
		getListView().invalidateViews();
		super.onRestart();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		Log.d(TAG, "onResume(): dumping users cursor");
		DatabaseUtils.dumpCursor(mUsers);
	}
	
	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Groups.TABLE, mGroupId);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		
		int tempPosition = position;
		
		//Actions section
		tempPosition -=1;
		if (mIsOwner) {
			if (tempPosition == 0) {
				Log.d(TAG, "onListItemClick(): add members");
				Intent i = new Intent(this, FriendPicker.class);
				i.putExtra(Groups.TABLE, mGroupId);
				startActivity(i);
				return;
			}
			else if (tempPosition == 1) {
				Log.d(TAG, "onListItemClick(): send message");
				if (mUsers.getCount() > 0) {
					Intent i = new Intent(this, MessageSender.class);
					i.putExtra(Groups.TABLE, mGroupId);
					startActivity(i);
					return;
				}
			}
		}
		else if (tempPosition == 0) {
			Log.d(TAG, "onListItemClick(): send message");
			if (mUsers.getCount() > 0) {
				Intent i = new Intent(this, MessageSender.class);
				i.putExtra(Groups.TABLE, mGroupId);
				startActivity(i);
				return;
			}
		}
		
		//Info section
		tempPosition = tempPosition - (mActions.size() + 1);
		if (tempPosition == 0) {
			Log.d(TAG, "onListItemClick(): statistics");
			return;
		}
		else if (tempPosition == 1) {
			Log.d(TAG, "onListItemClick(): achievements");
			return;
		}
		
		//Users section
		int userId = (Integer)mGroupedAdapter.getItem(position);
		Log.d(TAG, "userID = " + userId);
	}
	
	private void fillData() {
		mGroupedAdapter = new SeparatedListAdapter(this);
		mGroupedAdapter.addSection("Actions", new SimpleAdapter(this, mActions, R.layout.list_item_simple,
				new String[] {Common.ITEM_TITLE}, new int[] {R.id.list_simple_title}));
		mGroupedAdapter.addSection("Info", new SimpleAdapter(this, mInfo, R.layout.list_item_simple,
				new String[] {Common.ITEM_TITLE}, new int[] {R.id.list_simple_title}));
		mGroupedAdapter.addSection("Members", new UserListAdapter(this, mUsers, true, false));
		
		setListAdapter(mGroupedAdapter);
	}

}
