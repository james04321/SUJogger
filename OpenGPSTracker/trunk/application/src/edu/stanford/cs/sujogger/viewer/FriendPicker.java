package edu.stanford.cs.sujogger.viewer;

import java.util.Arrays;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Group;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.util.UserListAdapter;
import edu.stanford.cs.sujogger.viewer.GroupList.GroupListReceiver;

public class FriendPicker extends ListActivity {
	private static final String TAG = "OGT.FriendPicker";
	
	private long mGroupId;
	private DatabaseHelper mDbHelper;
	private Cursor mUsers;
	private UserListAdapter mUserAdapter;
	
	//Views
	private Button clearButton;
	private Button addButton;
	
	private GamingServiceConnection mGameCon;
	private FriendPickerReceiver mReceiver;
	
	//Request IDs
	private static final int GRP_ADDUSER_RID = 1;
	
	public FriendPicker() {}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendpicker);
		
		clearButton = (Button)findViewById(R.id.fp_clearbutton);
		clearButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mUserAdapter.clearAllChecked();
				clearButton.setEnabled(false);
				clearButton.setText(R.string.fp_clearbutton);
				addButton.setEnabled(false);
				addButton.setText(R.string.fp_addbutton);
				getListView().invalidateViews();
			}
		});
		addButton = (Button)findViewById(R.id.fp_addbutton);
		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				long[] checkedUsers = mUserAdapter.getCheckedUsers();
				Log.d(TAG, "addButton onClick(): " + Arrays.toString(checkedUsers));
				mDbHelper.addUsersToGroup(mGroupId, checkedUsers);
				finish();
			}
		});
		
		mGroupId = savedInstanceState != null ? savedInstanceState.getLong(Groups.TABLE) : 0;

		if (mGroupId == 0) {
			Bundle extras = getIntent().getExtras();
			mGroupId = extras != null ? extras.getLong(Groups.TABLE) : 0;
		}
		
		Log.d(TAG, "onCreate(): groupId = " + mGroupId);
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mUsers = mDbHelper.getAllUsersExcludingGroup(mGroupId);
		startManagingCursor(mUsers);
		
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
		
		int numChecked = mUserAdapter.toggleItemAtPosition(position);
		Log.d(TAG, "onListItemClicked(): numChecked = " + numChecked);
		
		if (numChecked > 0) {
			clearButton.setEnabled(true);
			if (numChecked == 1)
				clearButton.setText("Clear");
			else
				clearButton.setText("Clear all");
			addButton.setEnabled(true);
			addButton.setText("Add (" + numChecked + ")");
		}
		else {
			clearButton.setEnabled(false);
			clearButton.setText(R.string.fp_clearbutton);
			addButton.setEnabled(false);
			addButton.setText(R.string.fp_addbutton);
		}
	}
	
	private void fillData() {
		mUserAdapter = new UserListAdapter(this, mUsers, true);
		setListAdapter(mUserAdapter);
	}
	
	class FriendPickerReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					switch(appResponse.request_id) {
					case GRP_ADDUSER_RID:
						//GroupList.this.toggleNewGroupItemState();
						Integer groupId = (Integer)(appResponse.object);
						Group newGroup = (Group)(appResponse.appRequest.object);
						Log.d(TAG, "onReceive(): groupId = " + groupId + "; groupName = " + newGroup.name);
						//GroupList.this.mDbHelper.addGroup(groupId.longValue(), newGroup.name, 1);
						break;
					default: break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
