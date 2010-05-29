package edu.stanford.cs.sujogger.viewer;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Group;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.util.Constants;

public class LeaderBoard extends ListActivity {
	private static final String TAG = "OGT.LeaderBoard";
	private static final int MENU_SOLOGROUP = 0;
	public static final String STAT_TYPE_KEY = "statistic_type";
	public static final String STAT_TIME_KEY = "statistic_time";
	public static final String IS_GROUP_KEY = "is_group";
	
	//Request IDs
	private static final int GET_USER_SBS_RID = 1;
	private static final int GET_GROUP_SBS_RID = 2;
	private static final int GET_APP_USERS_RID = 3;
	private static final int GET_GROUPS_RID = 4;

	private DatabaseHelper mDbHelper;
	private Cursor mScores;

	private GamingServiceConnection mGameCon;
	private LeaderBoardReceiver mReceiver;
	private Handler mWaitHandler;

	private int mStatisticType;
	private int mTimeScale;
	private boolean mIsGroup;
	private int mGetUsersGroupsProgress;
	private Spinner mSpinnerType;
	private Spinner mSpinnerTime;
	private ProgressDialog mScoreWaitDialog;

	private Runnable mWaitServiceStartTask = new Runnable() {
		public void run() {
			if (mGameCon.grs != null) {
				try {
					mGameCon.getAppsUser(GET_APP_USERS_RID);
				}
				catch (RemoteException e) {}
				mWaitHandler.removeCallbacks(mWaitServiceStartTask);
			}
			else {
				mWaitHandler.postDelayed(mWaitServiceStartTask, 100);
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.leaderboard);

		mStatisticType = savedInstanceState != null ? savedInstanceState.getInt(STAT_TYPE_KEY) : 0;
		mTimeScale = savedInstanceState != null ? savedInstanceState.getInt(STAT_TIME_KEY) : 0;
		mIsGroup = savedInstanceState != null ? savedInstanceState.getBoolean(IS_GROUP_KEY) : false;
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		mReceiver = new LeaderBoardReceiver();
		mGameCon = new GamingServiceConnection(this, mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, LeaderBoard.class.toString());
		mGameCon.bind();
		mGameCon.setUserId(6);
		
		mGetUsersGroupsProgress = 0;

		mSpinnerType = (Spinner) findViewById(R.id.lb_spinner_type);
		ArrayAdapter<String> spinnerTypeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Stats.STAT_TYPES);
		spinnerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerType.setAdapter(spinnerTypeAdapter);
		mSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				pos++;
				mStatisticType = pos;
				updateLeaderBoards();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		mSpinnerTime = (Spinner) findViewById(R.id.lb_spinner_time);
		ArrayAdapter<String> spinnerTimeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Stats.TIME_TYPES);
		spinnerTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerTime.setAdapter(spinnerTimeAdapter);
		mSpinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mTimeScale = pos;
				updateLeaderBoards();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		setTitle("Leaderboards");
		
		/*
		mScoreWaitDialog = ProgressDialog.show(this, "", "Updating scores...", true);
		try {
			mGameCon.getAppsUser(GET_APP_USERS_RID);
			mGameCon.getGroups(GET_GROUPS_RID, null, -1, -1, -1);
		}
		catch (RemoteException e) {}*/
	}

	private void getAppUsers() {
		//mWaitHandler = new Handler();
		// Poll the GamingServiceConnection every 100ms until the service starts
		//mWaitHandler.postDelayed(mWaitServiceStartTask, 100);
		
		try {
			mGameCon.getAppsUser(GET_APP_USERS_RID);
		}
		catch (RemoteException e) {}
	}
	
	private void updateLeaderBoards() {
		Log.d(TAG, "updateLeaderBoards(): statisticId = " + statisticId() + 
				"; time scale = " + mTimeScale + "; is_group = " + mIsGroup);
	}

	private int statisticId() {
		return (mTimeScale * 10) + mStatisticType;
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
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
		outState.putInt(STAT_TYPE_KEY, mStatisticType);
		outState.putInt(STAT_TIME_KEY, mTimeScale);
		outState.putBoolean(IS_GROUP_KEY, mIsGroup);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Log.d(TAG, "onCreateOptionsMenu()");

		menu.add(ContextMenu.NONE, MENU_SOLOGROUP, ContextMenu.NONE, R.string.lb_option);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;

		switch (item.getItemId()) {
		case MENU_SOLOGROUP:

			handled = true;
			break;
		default:
			handled = super.onOptionsItemSelected(item);
			break;
		}

		return handled;
	}

	private void fillData() {

	}

	class LeaderBoardReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive()");
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					switch (appResponse.request_id) {
					case GET_USER_SBS_RID:
						break;
					case GET_GROUP_SBS_RID:
						break;
					case GET_APP_USERS_RID:
						User[] users = (User[])appResponse.object;
						if (users != null) {
							mDbHelper.addUsers(users);
							mGetUsersGroupsProgress++;
						}
						break;
					case GET_GROUPS_RID:
						Group[] groups = (Group[])appResponse.object;
						if (groups != null) {
							mDbHelper.addGroupsTemp(groups);
							mGetUsersGroupsProgress++;
						}
						
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
