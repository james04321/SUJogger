package edu.stanford.cs.sujogger.viewer;

import java.util.Arrays;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.Users;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.UserListAdapter;

public class FriendPicker extends ListActivity {
	private static final String TAG = "OGT.FriendPicker";
	public static final String MODE_KEY = "mode";
	public static final int MODE_ADD = 0;
	public static final int MODE_RECIPIENT = 1;
	
	private int mMode;
	private long mGroupId;
	private long[] mInitialUserIds;
	private int mGroupIdServer;
	private DatabaseHelper mDbHelper;
	private Cursor mUsers;
	private UserListAdapter mUserAdapter;
	
	//Views
	private Button clearButton;
	private Button addButton;
	
	private GamingServiceConnection mGameCon;
	private FriendPickerReceiver mReceiver;
	
	private int mGetUsersFriendsProgress = 0;
	private ProgressDialog mUserWaitDialog;
	
	//Request IDs
	private static final int GRP_ADDUSER_RID = 1;
	private static final int GET_USERS_RID = 2;
	private static final int GET_FRIENDS_RID = 3;
	
	public FriendPicker() {}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendpicker);
		
		
		Bundle extras = getIntent().getExtras();
		mGroupId = extras != null ? extras.getLong(Groups.TABLE) : 0;
		mInitialUserIds = extras != null ? extras.getLongArray(Users.TABLE) : null;
		mMode = extras != null ? extras.getInt(MODE_KEY) : MODE_ADD;
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		Cursor groupCursor = mDbHelper.getGroupWithId(mGroupId);
		if (groupCursor.moveToFirst())
			mGroupIdServer = groupCursor.getInt(1);
		else
			mGroupIdServer = 0;
		groupCursor.close();
		
		Log.d(TAG, "onCreate(): groupId = " + mGroupId + "; groupIdServer = " + mGroupIdServer);
		
		mReceiver = new FriendPickerReceiver(); 
		mGameCon = new GamingServiceConnection(this, mReceiver, 
				Constants.APP_ID, Constants.APP_API_KEY, FriendPicker.class.toString());
		mGameCon.bind();
		mGameCon.setUserId(Common.getRegisteredUser(this).id);
		
		mUsers = mDbHelper.getAllUsersExcludingGroup(mGroupId, Common.getRegisteredUser(this).id);
		startManagingCursor(mUsers);
		
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
				
				if (mMode == MODE_ADD) {
					User[] checkedUsers = mUserAdapter.getCheckedUsers();
					try {
						mGameCon.addGroupUsers(GRP_ADDUSER_RID, mGroupIdServer, checkedUsers);
					} catch (RemoteException e) {}
				}
				else {
					long[] checkedUserIds = mUserAdapter.getCheckedUserIds();
					Bundle bundle = new Bundle();
					bundle.putLongArray(Users.TABLE, checkedUserIds);
					Intent i = new Intent();
					i.putExtras(bundle);
					setResult(1, i);
					finish();
				}
			}
		});
		
		fillData();
		updateButtons(mUserAdapter.getNumChecked());
		
		mUserWaitDialog = ProgressDialog.show(this, "", "Retrieving friends...", true);
		try {
			mGameCon.getAppsUser(GET_USERS_RID);
			mGameCon.getInvitableFriends(GET_FRIENDS_RID);
		} catch (RemoteException e) {}
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
		mGameCon.unbind();
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
		
		updateButtons(numChecked);
	}
	
	private void updateButtons(int numChecked) {
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
		mUserAdapter = new UserListAdapter(this, mUsers, true, mInitialUserIds);
		setListAdapter(mUserAdapter);
	}
	
	class FriendPickerReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				User[] users;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					switch(appResponse.request_id) {
					case GRP_ADDUSER_RID:
						if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_SUCCESS)) {
							Log.d(TAG, "onReceive(): users successfully added to group");
							
							long[] checkedUserIds = mUserAdapter.getCheckedUserIds();
							Log.d(TAG, "adding users to db: " + Arrays.toString(checkedUserIds));
							mDbHelper.addUsersToGroup(mGroupId, checkedUserIds);
							
							String toastString = "User successfully added";
							if (checkedUserIds.length > 1)
								toastString = "Users successfully added";
							Toast toast = Toast.makeText(FriendPicker.this, toastString, 
									Toast.LENGTH_SHORT);
							toast.show();
							FriendPicker.this.finish();
						}
						else {
							Toast toast = Toast.makeText(FriendPicker.this, 
									"Error adding users", Toast.LENGTH_SHORT);
							toast.show();
						}
						break;
					case GET_USERS_RID:
						Log.d(TAG, "onRecieve(): request is get users");
						users = (User[])appResponse.object;
						if (users != null) {
							mDbHelper.addUsers(users);
							mGetUsersFriendsProgress++;
							if (mGetUsersFriendsProgress >= 2) {
								mUserWaitDialog.dismiss();
								mUsers.requery();
								mUserAdapter.notifyDataSetChanged();
								FriendPicker.this.getListView().invalidateViews();
							}
						}
						break;
					case GET_FRIENDS_RID:
						Log.d(TAG, "onRecieve(): request is get friends");
						users = (User[])appResponse.object;
						if (users != null) {
							mDbHelper.addFriends(users);
							mGetUsersFriendsProgress++;
							if (mGetUsersFriendsProgress >= 2) {
								mUserWaitDialog.dismiss();
								mUsers.requery();
								mUserAdapter.notifyDataSetChanged();
								FriendPicker.this.getListView().invalidateViews();
							}
						}
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
