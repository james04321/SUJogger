package edu.stanford.cs.sujogger.viewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.AsyncFacebookRunner.RequestListener;

import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Group;
import edu.stanford.cs.gaming.sdk.model.ScoreBoard;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.GroupListAdapter;
import edu.stanford.cs.sujogger.util.SegmentedControl;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;
import edu.stanford.cs.sujogger.util.UserListAdapter;

public class GroupList extends ListActivity {
	private static final String TAG = "OGT.GroupList";

	private static final int DIALOG_GRPNAME = 1;
	public static final String IS_FRIEND_KEY = "is_friend";

	private DatabaseHelper mDbHelper;
	private Cursor mCursor;
	private Cursor mUnregFriendsCursor;
	private GroupListAdapter mGroupAdapter;
	private UserListAdapter mUserAdapter;
	private UserListAdapter mUnregFriendsAdapter;
	private SeparatedListAdapter mGroupedAdapter;
	private int mGroupIdTemp;
	private SharedPreferences mSharedPreferences;
	private boolean mDisplayFriends;
	
	private Button mNewGroupButton;
	private LinearLayout mBottomControlBar;
	
	private GamingServiceConnection mGameCon;
	private GroupListReceiver mReceiver;
	private Handler mHandler = new Handler();
	
	//TODO: Facebook
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;

	private static final int MENU_REFRESH = 0;
	
	// Request IDs
	private static final int GRP_CREATE_RID = 1;
	private static final int GRP_GET_RID = 2;
	private static final int SB_CREATE_RID = 3;
	private static final int SB_GET_RID = 4;
	private static final int GET_FRIENDS_RID = 5;
	private static final int USERREG_RID = 6;

	// Views
	private EditText mGroupNameView;
	private ProgressDialog mCreateDialog;

	// Listeners
	private final DialogInterface.OnClickListener mGroupNameDialogListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			String groupName = mGroupNameView.getText().toString();
			Log.d(TAG, "mGroupNameDialogListener: " + groupName);
			mCreateDialog = ProgressDialog.show(GroupList.this, "", "Creating group...", true);
			Group newGroup = new Group(groupName);
			newGroup.owner_id = Common.getRegisteredUser(GroupList.this).id;
			
			try {
				mGameCon.createGroup(GRP_CREATE_RID, newGroup);
			}
			catch (RemoteException e) {}
		}
	};
	
	private Runnable mGroupRefreshTask = new Runnable() {
		public void run() {
			try {
				mGameCon.getGroups(GRP_GET_RID, null, Common.getRegisteredUser(GroupList.this).id, -1, -1);
			}
			catch (RemoteException e) {}
		}
	};
	
	private Runnable mFriendRefreshTask = new Runnable() {
		public void run() {
			try {
				mGameCon.getInvitableFriends(GET_FRIENDS_RID);
			}
			catch (RemoteException e) {}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.grouplist);
		
		mDisplayFriends = savedInstanceState != null ? savedInstanceState.getBoolean(IS_FRIEND_KEY) : false;
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		mReceiver = new GroupListReceiver();
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, GroupList.class.toString());
		mGameCon.bind();
		User user = Common.getRegisteredUser(this);
		mGameCon.setUserId(user.id, user.fb_id, user.fb_token);
		
		//TODO: Facebook
		mFacebook = null;
		mAsyncRunner = null;
		if (mSharedPreferences.getBoolean(Constants.USER_REGISTERED, false)) {
			mFacebook = new Facebook();
			String accessToken = mSharedPreferences.getString(Constants.FB_ACCESS_TOKEN_KEY, null);
			mFacebook.setAccessToken(accessToken);
			
			mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		}
		
		mGroupIdTemp = 0;
		
		mNewGroupButton = (Button)findViewById(R.id.newgroupbutton);
		mNewGroupButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_GRPNAME);
			}
		});
		
		mBottomControlBar = (LinearLayout)findViewById(R.id.bottom_control_bar);
		
		LinearLayout topControlBar = (LinearLayout)findViewById(R.id.top_control_bar);
		topControlBar.addView(new SegmentedControl(this, new String[] {"Groups", "Friends"}, 
				mDisplayFriends ? 1 : 0, new SegmentedControl.SegmentedControlListener() {
			public void onValueChanged(int newValue) {
				mDisplayFriends = newValue == 1;
				fillData();
				refreshData(false, false);
			}
		}), 
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		fillData();
		
		//Wait 100ms before sending request, because sometimes, the activity doesn't
		//bind to the service quickly enough
		refreshData(true, false);
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
		if (mGroupAdapter != null)
			mGroupAdapter.notifyDataSetChanged();
		if (mUserAdapter != null)
			mUserAdapter.notifyDataSetChanged();
		getListView().invalidateViews();
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
		outState.putBoolean(IS_FRIEND_KEY, mDisplayFriends);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Log.d(TAG, "onCreateOptionsMenu()");

		menu.add(ContextMenu.NONE, MENU_REFRESH, ContextMenu.NONE, R.string.refresh)
			.setIcon(R.drawable.ic_menu_refresh);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;

		switch (item.getItemId()) {
		case MENU_REFRESH:
			refreshData(false, true);
			handled = true;
			break;
		default:
			handled = super.onOptionsItemSelected(item);
			break;
		}

		return handled;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		if (mDisplayFriends) {
			Object item = mGroupedAdapter.getItem(position);
			if (item.getClass() == Integer.class) {
				int userId = (Integer) item;
				Log.d(TAG, "userID = " + userId);
				Intent i = new Intent(this, PeopleTrackList.class);
				i.putExtra("userId", userId);
				startActivity(i);
			}
			else if (item.getClass() == Long.class) {
				long fbId = (Long) item;
				Log.d(TAG, "fbId = " + fbId);
				Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://m.facebook.com/inbox/?compose&ids=" + fbId));  
				startActivity(viewIntent);
			}
		}
		else {
			Object item = mGroupAdapter.getItem(position);
			Log.d(TAG, "starting GroupDetail for group_id = " + (Integer) item);
			long groupId = ((Integer) item).longValue();
			startGroupDetail(groupId);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		LayoutInflater factory = null;
		View view = null;
		Builder builder = null;

		switch (id) {
		case DIALOG_GRPNAME:
			builder = new AlertDialog.Builder(this);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.namedialog, null);
			mGroupNameView = (EditText) view.findViewById(R.id.nameField);
			mGroupNameView.setHint(R.string.dialog_newgrpname_hint);
			builder.setTitle(R.string.dialog_newgrpname_title).setIcon(
					android.R.drawable.ic_dialog_alert).setPositiveButton(R.string.btn_okay,
					mGroupNameDialogListener).setNegativeButton(R.string.btn_cancel, null).setView(
					view);
			dialog = builder.create();
			return dialog;
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_GRPNAME:
			mGroupNameView.setText("");
			break;
		default:
			break;
		}
	}

	private void startGroupDetail(long groupId) {
		Intent i = new Intent(this, GroupDetail.class);
		i.putExtra(Groups.TABLE, groupId);
		startActivity(i);
	}
	
	private void refreshData(boolean delay, boolean force) {
		if (mDisplayFriends) {
			if ((System.currentTimeMillis() - 
					mSharedPreferences.getLong(Constants.FB_UPDATE_KEY, 0) >
						Constants.FB_UPDATE_INTERVAL || force) && 
						mSharedPreferences.getString(Constants.FB_ACCESS_TOKEN_KEY, null) != null) {
				mAsyncRunner.request("me/friends", new FriendsRequestListener());
			}
			else if (System.currentTimeMillis() - 
					mSharedPreferences.getLong(Constants.ALL_USERS_UPDATE_KEY, 0) > 
						Constants.UPDATE_INTERVAL || force)
				if (delay)
					mHandler.postDelayed(mFriendRefreshTask, 100);
				else
					mHandler.post(mFriendRefreshTask);
		}
		else {
			if (System.currentTimeMillis() - 
					mSharedPreferences.getLong(Constants.GROUPS_UPDATE_KEY, 0) > 
						Constants.UPDATE_INTERVAL || force)
				if (delay)
					mHandler.postDelayed(mGroupRefreshTask, 100);
				else
					mHandler.post(mGroupRefreshTask);
		}
	}

	private void fillData() {
		TextView emptyView = (TextView)getListView().getEmptyView();
		emptyView.setText(mDisplayFriends ? 
				R.string.no_friends : R.string.no_groups);
		if (mDisplayFriends) {
			mBottomControlBar.setVisibility(View.GONE);
			if (mUserAdapter == null) {
				mCursor = mDbHelper.getAllUsers(true, true);
				mUserAdapter = new UserListAdapter(this, mCursor, false, null);
			}
			if (mUnregFriendsAdapter == null) {
				mUnregFriendsCursor = mDbHelper.getAllUsers(true, false);
				startManagingCursor(mUnregFriendsCursor);
				mUnregFriendsAdapter = new UserListAdapter(this, mUnregFriendsCursor, false, null);
			}
			if (mGroupedAdapter == null) {
				mGroupedAdapter = new SeparatedListAdapter(this);
				mGroupedAdapter.addSection("Friends on Happy Feet", mUserAdapter);
				mGroupedAdapter.addSection("Unregistered Friends", mUnregFriendsAdapter);
			}
			setListAdapter(mGroupedAdapter);
		}
		else {
			mBottomControlBar.setVisibility(View.VISIBLE);
			if (mGroupAdapter == null) {
				mCursor = mDbHelper.getGroups();
				mGroupAdapter = new GroupListAdapter(this, mCursor, true);
			}
			setListAdapter(mGroupAdapter);
		}
		
		startManagingCursor(mCursor);
		
		Log.d(TAG, "fillData()");
		DatabaseUtils.dumpCursor(mCursor);
	}

	private void initializeStatsForGroup(int groupId) {
		ScoreBoard score;
		int[] stats = Stats.GROUP_STAT_IDS;
		ScoreBoard[] scores = new ScoreBoard[stats.length];
		for (int i = 0; i < stats.length; i++) {
			score = new ScoreBoard();
			score.app_id = Constants.APP_ID;
			score.user_id = 0;
			score.group_id = groupId;
			score.value = 0;
			score.sb_type = String.valueOf(stats[i]);
			scores[i] = score;
		}

		try {
			mGameCon.createScoreBoards(SB_CREATE_RID, scores);
		}
		catch (RemoteException e) {
		}
	}

	class GroupListReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mCreateDialog != null) mCreateDialog.dismiss();
								Toast toast = Toast.makeText(GroupList.this, 
										R.string.connection_error_toast, Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						continue;
					}
					
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_FAILURE)) {
						final String errorMsg;
							if (appResponse.error != null && appResponse.error.length > 0)
								errorMsg = appResponse.error[0];
							else errorMsg = "Unknown error";
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mCreateDialog != null) mCreateDialog.dismiss();
								Toast toast = Toast.makeText(GroupList.this, 
										errorMsg, Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						continue;
					}
					
					switch (appResponse.request_id) {
					case USERREG_RID:
						final int userId = (Integer) appResponse.object;
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								Log.d(TAG, "onReceive(): user registered");
								Editor editor = mSharedPreferences.edit();
								editor.putLong(Constants.FB_UPDATE_KEY, System.currentTimeMillis());
								editor.commit();
								mHandler.post(mFriendRefreshTask);
							}
						});
						break;
					case GRP_GET_RID:
						final Group[] groups = (Group[]) (appResponse.object);
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (groups != null) {
									ArrayList<Group> newGroups = mDbHelper.updateGroups(groups, 
											Common.getRegisteredUser(GroupList.this).id);
									if (newGroups != null && newGroups.size() > 0) {
										GroupList.this.mCursor.requery();
										GroupList.this.mGroupAdapter.notifyDataSetChanged();
										GroupList.this.getListView().invalidateViews();
										Log.d(TAG, "onReceive(): getting scoreboards for new groups");
										try {
											//Get ScoreBoards for each previously unseen group
											for (int i = 0; i < newGroups.size(); i++)
												mGameCon.getScoreBoards(SB_GET_RID, -1, newGroups.get(i).id, null, null);
										} catch (RemoteException e) {}
										return;
									}
								}
								Editor editor = mSharedPreferences.edit();
								editor.putLong(Constants.GROUPS_UPDATE_KEY, System.currentTimeMillis());
								editor.commit();
								Toast toast = Toast.makeText(GroupList.this, 
										"Groups up to date", Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						break;
					case SB_GET_RID:
						final ScoreBoard[] scores = (ScoreBoard[])appResponse.object;
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scores != null) {
									mDbHelper.insertScoreboards(scores);
									Editor editor = mSharedPreferences.edit();
									editor.putLong(Constants.GROUPS_UPDATE_KEY, System.currentTimeMillis());
									editor.commit();
									Toast toast = Toast.makeText(GroupList.this, 
											"Groups up to date", Toast.LENGTH_SHORT);
									toast.show();
								}
							}
						});
						break;
					case GRP_CREATE_RID:
						final Integer groupId = (Integer) (appResponse.object);
						final Group newGroup = (Group) (appResponse.appRequest.object);
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								Log.d(TAG, "onReceive(): groupId = " + groupId + "; groupName = "
										+ newGroup.name);
								GroupList.this.mDbHelper.addGroup(groupId.longValue(), newGroup.name, 1);
								GroupList.this.mDbHelper.addUsersToGroup(groupId, new long[] { Common
										.getRegisteredUser(GroupList.this).id });
								GroupList.this.mCursor.requery();
								GroupList.this.mGroupAdapter.notifyDataSetChanged();
								GroupList.this.getListView().invalidateViews();
		
								// After creating a group, create the scoreboards for
								// that group and add self to the group
								mGroupIdTemp = groupId;
								initializeStatsForGroup(groupId.intValue());
								try {
									mGameCon.addGroupUsers(-1, groupId, 
											new User[] {Common.getRegisteredUser(GroupList.this)});
								} catch (RemoteException e) {}
							}
						});
						break;
					case SB_CREATE_RID:
						final Integer[] scoreIds = (Integer[]) appResponse.object;
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scoreIds != null) {
									ScoreBoard score;
									int[] stats = Stats.GROUP_STAT_IDS;
									ScoreBoard[] newScores = new ScoreBoard[stats.length];
									for (int i = 0; i < stats.length; i++) {
										score = new ScoreBoard();
										score.id = scoreIds[i];
										score.group_id = mGroupIdTemp;
										score.value = 0;
										score.sb_type = String.valueOf(stats[i]);
										newScores[i] = score;
									}
									mDbHelper.insertScoreboards(newScores);
								}
								mGroupIdTemp = 0;
								mCreateDialog.dismiss();
							}
						});
						break;
					case GET_FRIENDS_RID:
						final User[] users = (User[])appResponse.object;
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (users != null) {
									mDbHelper.addFriends(users);
									mCursor.requery();
									mUserAdapter.notifyDataSetChanged();
									GroupList.this.getListView().invalidateViews();
								}
								Editor editor = mSharedPreferences.edit();
								editor.putLong(Constants.ALL_USERS_UPDATE_KEY, System.currentTimeMillis());
								editor.commit();
								Toast toast = Toast.makeText(GroupList.this, 
										"Friends up to date", Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						break;
					default:
						break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class FriendsRequestListener implements RequestListener {

		public void onComplete(final String response) {
			try {
				// process the response here: executed in background thread
				Log.d(TAG, "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);

				final JSONArray friends = json.getJSONArray("data");
				if (friends == null)
					return;
				
				GroupList.this.runOnUiThread(new Runnable() {
					public void run() {
						try {
							final long[] fbIds = new long[friends.length()];
							User newFriend = new User();
							JSONObject friend;
							mDbHelper.mDb.beginTransaction();
							try {
								for (int i = 0; i < friends.length(); i++) {
									friend = friends.getJSONObject(i);
									fbIds[i] = friend.getInt("id");
									Log.d(TAG, "fb_id = " + fbIds[i]);
									
									newFriend.fb_id = friend.getInt("id");
									newFriend.fb_photo = Constants.GRAPH_BASE_URL+ newFriend.fb_id + "/picture";
									newFriend.last_name = friend.getString("name");
									mDbHelper.addFriend(newFriend);
								}
								mDbHelper.mDb.setTransactionSuccessful();
							} finally {
								mDbHelper.mDb.endTransaction();
							}
					
							User user = Common.getRegisteredUser(GroupList.this);
							user.friend_fb_ids = fbIds;
							
							try {
								mGameCon.registerUser(USERREG_RID, user);
							}
							catch (RemoteException e) {}
						}
						catch (JSONException e) {
							Log.w("Facebook-Example", "JSON Error in response");
						}
					}
				});
			}
			catch (JSONException e) {
				Log.w("Facebook-Example", "JSON Error in response");
			}
			catch (FacebookError e) {
				Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
			}
		}

		public void onFacebookError(FacebookError e) {}

		public void onFileNotFoundException(FileNotFoundException e) {
			Log.d(TAG, "onFileNotFoundException");
		}

		public void onIOException(IOException e) {
			GroupList.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast toast = Toast.makeText(GroupList.this, 
							R.string.connection_error_toast, Toast.LENGTH_SHORT);
					toast.show();
				}
			});
		}

		public void onMalformedURLException(MalformedURLException e) {}
	}
}
