package edu.stanford.cs.sujogger.viewer;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;
import edu.stanford.cs.sujogger.util.UserListAdapter;

public class GroupDetail extends ListActivity {
	private static final String TAG = "OGT.GroupDetail";

	private long mGroupId;
	private boolean mIsOwner;
	private DatabaseHelper mDbHelper;
	private Cursor mUsers;
	private SeparatedListAdapter mGroupedAdapter;
	
	private Button mAddButton;
	private Button mSendButton;
	private Button mStatsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groupdetail);

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
		
		mAddButton = (Button) findViewById(R.id.addbutton);
		if (mIsOwner) mAddButton.setVisibility(View.VISIBLE);
		mAddButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(GroupDetail.this, FriendPicker.class);
				i.putExtra(Groups.TABLE, mGroupId);
				startActivity(i);
			}
		});
		
		mSendButton = (Button) findViewById(R.id.sendbutton);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(GroupDetail.this, MessageSender.class);
				i.putExtra(Groups.TABLE, mGroupId);
				startActivity(i);
			}
		});

		mStatsButton = (Button) findViewById(R.id.statsbutton);
		mStatsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(GroupDetail.this, StatisticsView.class);
				intent.putExtra("group_id", mGroupId);
				startActivity(intent);
			}
		});

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
		
		int userId = (Integer) mGroupedAdapter.getItem(position);
		Log.d(TAG, "userID = " + userId);
		Intent i = new Intent(this, PeopleTrackList.class);
		i.putExtra("userId", userId);
		startActivity(i);
		return;
	}

	private void fillData() {
		mGroupedAdapter = new SeparatedListAdapter(this);
		mGroupedAdapter.addSection("Members", new UserListAdapter(this, mUsers, true, false, null));

		setListAdapter(mGroupedAdapter);
	}

}
