package edu.stanford.cs.sujogger.viewer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

public class GroupList extends ListActivity {
	private static final String TAG = "OGT.GroupList";

	private static final int DIALOG_GRPNAME = 1;

	private DatabaseHelper mDbHelper;
	private Cursor mGroupsCursor;
	private SeparatedListAdapter mGroupAdapter;
	private List<Map<String, ?>> actions;
	private int mGroupIdTemp;

	private GamingServiceConnection mGameCon;
	private GroupListReceiver mReceiver;
	private Handler mHandler = new Handler();

	private static final int MENU_REFRESH = 0;

	// Request IDs
	private static final int GRP_CREATE_RID = 1;
	private static final int GRP_GET_RID = 2;
	private static final int SB_CREATE_RID = 3;
	private static final int SB_GET_RID = 4;

	// Views
	private EditText mGroupNameView;
	private ProgressDialog mCreateDialog;
	private ProgressDialog mRefreshDialog;

	// Listeners
	private final DialogInterface.OnClickListener mGroupNameDialogListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			String groupName = mGroupNameView.getText().toString();
			Log.d(TAG, "mGroupNameDialogListener: " + groupName);
			mCreateDialog = ProgressDialog.show(GroupList.this, "", "Creating group...", true);

			try {
				mGameCon.createGroup(GRP_CREATE_RID, new Group(groupName));
			}
			catch (RemoteException e) {

			}
		}
	};
	
	private Runnable mRefreshTask = new Runnable() {
		public void run() {
			try {
				mGameCon.getGroups(GRP_GET_RID, null, Common.getRegisteredUser(GroupList.this).id, -1, -1);
				mRefreshDialog = ProgressDialog.show(GroupList.this, "", "Refreshing groups...", true);
			}
			catch (RemoteException e) {}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.list_simple);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		mReceiver = new GroupListReceiver();
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, GroupList.class.toString());
		mGameCon.bind();
		mGroupIdTemp = 0;

		// Create cursors
		mGroupsCursor = mDbHelper.getGroups();
		startManagingCursor(mGroupsCursor);

		actions = new LinkedList<Map<String, ?>>();
		actions.add(Common.createItem("New group"));

		fillData();
		registerForContextMenu(getListView());
		
		//Wait 100ms before sending request, because sometimes, the activity doesn't
		//bind to the service quickly enough
		mHandler.postDelayed(mRefreshTask, 100);
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
		mGroupAdapter.notifyDataSetChanged();
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
	protected void onStop() {

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mGameCon.unbind();
		super.onDestroy();
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
			refreshGroups();
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

		if (position == 1) {
			Log.d(TAG, "pulling up new group dialog");
			showDialog(DIALOG_GRPNAME);
		}
		else {
			Object item = mGroupAdapter.getItem(position);
			if (item.getClass() == Integer.class) {
				Log.d(TAG, "starting GroupDetail for group_id = " + (Integer) item);
				long groupId = ((Integer) item).longValue();
				startGroupDetail(groupId);
			}
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

	private void fillData() {
		mGroupAdapter = new SeparatedListAdapter(this);

		mGroupAdapter.addSection("", new SimpleAdapter(this, actions, R.layout.list_item_simple,
				new String[] { Common.ITEM_TITLE }, new int[] { R.id.list_simple_title }));

		mGroupAdapter.addSection("My Groups", new GroupListAdapter(this, mGroupsCursor, true));

		setListAdapter(mGroupAdapter);
	}

	private void refreshGroups() {
		mHandler.post(mRefreshTask);
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
					ScoreBoard[] scores;
					switch (appResponse.request_id) {
					case GRP_GET_RID:
						final Group[] groups = (Group[]) (appResponse.object);
						GroupList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (groups != null) {
									ArrayList<Group> newGroups = mDbHelper.updateGroups(groups);
									if (newGroups != null && newGroups.size() > 0) {
										GroupList.this.mGroupsCursor.requery();
										GroupList.this.mGroupAdapter.notifyDataSetChanged();
										GroupList.this.getListView().invalidateViews();
										Log.d(TAG, "onReceive(): getting scoreboards for new groups");
										try {
											//Get ScoreBoards for each previously unseen group
											for (int i = 0; i < newGroups.size(); i++)
												mGameCon.getScoreBoards(SB_GET_RID, -1, newGroups.get(i).id, null, null);
										} catch (RemoteException e) {}
									}
									else
										mRefreshDialog.dismiss();
								}
								else {
									mRefreshDialog.dismiss();
								}
							}
						});
						break;
					case SB_GET_RID:
						scores = (ScoreBoard[])appResponse.object;
						if (scores != null) {
							mDbHelper.insertScoreboards(scores);
							mRefreshDialog.dismiss();
						}
						break;
					case GRP_CREATE_RID:
						Integer groupId = (Integer) (appResponse.object);
						Group newGroup = (Group) (appResponse.appRequest.object);
						Log.d(TAG, "onReceive(): groupId = " + groupId + "; groupName = "
								+ newGroup.name);
						GroupList.this.mDbHelper.addGroup(groupId.longValue(), newGroup.name, 1);
						GroupList.this.mDbHelper.addUsersToGroup(groupId, new long[] { Common
								.getRegisteredUser(GroupList.this).id });
						GroupList.this.mGroupsCursor.requery();
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
						break;
					case SB_CREATE_RID:
						Integer[] scoreIds = (Integer[]) appResponse.object;
						if (scoreIds != null) {
							ScoreBoard score;
							int[] stats = Stats.GROUP_STAT_IDS;
							scores = new ScoreBoard[stats.length];
							for (int i = 0; i < stats.length; i++) {
								score = new ScoreBoard();
								score.id = scoreIds[i];
								score.group_id = mGroupIdTemp;
								score.value = 0;
								score.sb_type = String.valueOf(stats[i]);
								scores[i] = score;
							}
							mDbHelper.insertScoreboards(scores);
						}
						mGroupIdTemp = 0;
						mCreateDialog.dismiss();
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
}
