package edu.stanford.cs.sujogger.viewer;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Group;
import edu.stanford.cs.gaming.sdk.model.ScoreBoard;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.LeaderBoardAdapter;
import edu.stanford.cs.sujogger.util.SegmentedControl;

public class LeaderBoard extends ListActivity {
	private static final String TAG = "OGT.LeaderBoard";
	public static final String STAT_TYPE_KEY = "statistic_type";
	public static final String STAT_TIME_KEY = "statistic_time";
	public static final String IS_GROUP_KEY = "is_group";
	public static final String ALREADY_UPDATED_KEY = "already_updated";
	
	//Request IDs
	private static final int GET_USER_SBS_RID = 1;
	private static final int GET_GROUP_SBS_RID = 2;
	private static final int GET_APP_USERS_RID = 3;
	private static final int GET_GROUPS_RID = 4;

	private DatabaseHelper mDbHelper;
	private Cursor mScores;
	private LeaderBoardAdapter lbAdapter;

	private GamingServiceConnection mGameCon;
	private LeaderBoardReceiver mReceiver;
	private SharedPreferences mSharedPreferences;
	private Handler mHandler = new Handler();
	
	ArrayAdapter<String> mSpinnerTypeAdapterSolo;
	ArrayAdapter<String> mSpinnerTypeAdapterGroup;
	
	private int mStatisticType;
	private int mTimeScale;
	private boolean mIsGroup;
	private int mGetUsersGroupsProgress;
	private int mGetUsersGroupsGoal = 0;
	private Spinner mSpinnerTypeSolo;
	private Spinner mSpinnerTypeGroup;
	private Spinner mSpinnerTime;
	private ProgressDialog mUserGroupWaitDialog;
	private ProgressDialog mScoreWaitDialog;
	
	private Runnable mRefreshTask = new Runnable() {
		public void run() {
			long timeNow = System.currentTimeMillis();
			boolean updateUsers = timeNow - mSharedPreferences.getLong(Constants.ALL_USERS_UPDATE_KEY, 0) > 
				Constants.UPDATE_INTERVAL;
			boolean updateGroups = timeNow - mSharedPreferences.getLong(Constants.ALL_GROUPS_UPDATE_KEY, 0) > 
				Constants.UPDATE_INTERVAL;
			if (updateUsers || updateGroups) {
				mUserGroupWaitDialog = ProgressDialog.show(LeaderBoard.this, "", "Updating users and groups...", true);
				try {
					if (updateUsers) {
						mGetUsersGroupsGoal++;
						mGameCon.getAppsUser(GET_APP_USERS_RID);
					}
					if (updateGroups) {
						mGetUsersGroupsGoal++;
						mGameCon.getGroups(GET_GROUPS_RID, null, -1, -1, -1);
					}
				}
				catch (RemoteException e) {}
			}
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Common.log(TAG, "onCreate()");
		this.setContentView(R.layout.leaderboard);

		mStatisticType = savedInstanceState != null ? savedInstanceState.getInt(STAT_TYPE_KEY) : 1;
		mTimeScale = savedInstanceState != null ? savedInstanceState.getInt(STAT_TIME_KEY) : 0;
		mIsGroup = savedInstanceState != null ? savedInstanceState.getBoolean(IS_GROUP_KEY) : false;
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		mReceiver = new LeaderBoardReceiver();
		mGameCon = new GamingServiceConnection(this, mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, LeaderBoard.class.toString());
		mGameCon.bind();
		User user = Common.getRegisteredUser(this);
		mGameCon.setUserId(user.id, user.fb_id, user.fb_token);
		
		mGetUsersGroupsProgress = 0;
		
		mSpinnerTypeAdapterSolo = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Stats.STAT_TYPES_SOLO);
		mSpinnerTypeAdapterSolo.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		mSpinnerTypeAdapterGroup = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Stats.STAT_TYPES_GROUP);
		mSpinnerTypeAdapterGroup.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		
		
		mSpinnerTypeSolo = (Spinner) findViewById(R.id.lb_spinner_type_solo);
		mSpinnerTypeGroup = (Spinner) findViewById(R.id.lb_spinner_type_group);
		
		mSpinnerTypeSolo.setAdapter(mSpinnerTypeAdapterSolo);
		mSpinnerTypeSolo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (mIsGroup) return;
				pos++;
				mStatisticType = pos;
				updateLBSelection(false);
			}

			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		mSpinnerTypeGroup.setAdapter(mSpinnerTypeAdapterGroup);
		mSpinnerTypeGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (!mIsGroup) return;
				pos++;
				mStatisticType = pos;
				updateLBSelection(false);
			}

			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		mSpinnerTime = (Spinner) findViewById(R.id.lb_spinner_time);
		ArrayAdapter<String> spinnerTimeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Stats.TIME_TYPES);
		spinnerTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerTime.setAdapter(spinnerTimeAdapter);
		mSpinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mTimeScale = pos;
				updateLBSelection(false);
			}

			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		LinearLayout bottomControlBar = (LinearLayout)findViewById(R.id.bottom_control_bar);
		bottomControlBar.addView(new SegmentedControl(this, new String[] {"Individuals", "Groups"}, 
				mIsGroup ? 1 : 0, new SegmentedControl.SegmentedControlListener() {
			public void onValueChanged(int newValue) {
				mIsGroup = newValue == 1;
				updateLBSelection(true);
			}
		}), 
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		setTitle("Leaderboards");
		
		if (Constants.AD_TEST) AdManager.setTestDevices(new String[] { "3468678E351E95A5F7A64D2271BCB7BF" });
		AdView adView = (AdView)View.inflate(this, R.layout.adview, null);
		getListView().addHeaderView(adView);
		
		fillData();
		
		mHandler.postDelayed(mRefreshTask, 100);
	}
	
	private void updateLBSelection(boolean retrieveScores) {
		Common.log(TAG, "updateLBSelection(): statisticId = " + statisticId() + 
				"; time scale = " + mTimeScale + "; is_group = " + mIsGroup);
		if (mIsGroup) {
			if (mStatisticType > 4)
				mStatisticType = 4;
			mSpinnerTypeGroup.setSelection(mStatisticType-1);
			mSpinnerTime.setVisibility(View.GONE);
			mSpinnerTypeSolo.setVisibility(View.GONE);
			mSpinnerTypeGroup.setVisibility(View.VISIBLE);
			
			if (retrieveScores && (System.currentTimeMillis() - 
					mSharedPreferences.getLong(Constants.LB_GROUPSCORES_UPDATE_KEY, 0) > 
						Constants.UPDATE_INTERVAL)) {
				mScoreWaitDialog = ProgressDialog.show(this, "", "Retrieving scores...", true);
				try {
					mGameCon.getGroupScoreBoards(GET_GROUP_SBS_RID);
				} catch (RemoteException e) {}
			}
			else {
				fillData();
			}
		}
		else {
			mSpinnerTypeSolo.setSelection(mStatisticType-1);
			mSpinnerTime.setVisibility(View.VISIBLE);
			mSpinnerTypeSolo.setVisibility(View.VISIBLE);
			mSpinnerTypeGroup.setVisibility(View.GONE);
			
			if (retrieveScores && (System.currentTimeMillis() - 
					mSharedPreferences.getLong(Constants.LB_USERSCORES_UPDATE_KEY, 0) > 
						Constants.UPDATE_INTERVAL)) {
				mScoreWaitDialog = ProgressDialog.show(this, "", "Retrieving scores...", true);
				try {
					mGameCon.getUserScoreBoards(GET_USER_SBS_RID);
				} catch (RemoteException e) {}
			}
			else {
				fillData();
			}
		}
		
		Common.log(TAG, "updateLBSelection(): statisticId = " + statisticId() + 
				"; time scale = " + mTimeScale + "; is_group = " + mIsGroup);
	}

	private int statisticId() {
		return (mTimeScale * 10) + mStatisticType;
	}

	@Override
	protected void onRestart() {
		Common.log(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
	}

	@Override
	protected void onPause() {
		Common.log(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Common.log(TAG, "onResume()");
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
		Common.log(TAG, "onSaveInstanceState(): mStatisticType = " + mStatisticType);
		outState.putInt(STAT_TYPE_KEY, mStatisticType);
		outState.putInt(STAT_TIME_KEY, mTimeScale);
		outState.putBoolean(IS_GROUP_KEY, mIsGroup);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		position--;
		if (mIsGroup) {
			
		}
		else {
			int userId = (Integer) lbAdapter.getItem(position);
			Common.log(TAG, "userID = " + userId);
			Intent i = new Intent(this, PeopleTrackList.class);
			i.putExtra("userId", userId);
			startActivity(i);
		}
	}

	private void fillData() {
		if (mScores != null) mScores.close();
		
		if (mIsGroup)
			mScores = mDbHelper.getScoresWithGroups(statisticId());
		else
			mScores = mDbHelper.getScoresWithUsers(statisticId());
		
		startManagingCursor(mScores);
		
		Common.log(TAG, "fillData()");
		if (Constants.SHOW_DEBUG) DatabaseUtils.dumpCursor(mScores);
		
		if (lbAdapter == null) {
			lbAdapter = new LeaderBoardAdapter(this, mScores, statisticId(), mIsGroup);
			setListAdapter(lbAdapter);
		}
		else
			lbAdapter.changeCursor(mScores, statisticId(), mIsGroup);
	}

	class LeaderBoardReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Common.log(TAG, "onReceive()");
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Common.log(TAG, appResponse.toString());
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						LeaderBoard.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mUserGroupWaitDialog != null) mUserGroupWaitDialog.dismiss();
								if (mScoreWaitDialog != null) mScoreWaitDialog.dismiss();
								fillData();
								Toast toast = Toast.makeText(LeaderBoard.this, 
										R.string.connection_error_toast, Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						continue;
					}
					else if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_VERSION_ERROR)) {
						LeaderBoard.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mUserGroupWaitDialog != null) mUserGroupWaitDialog.dismiss();
								if (mScoreWaitDialog != null) mScoreWaitDialog.dismiss();
								fillData();
								Common.displayUpgradeDialog(LeaderBoard.this);
							}
						});
						continue;
					}
					
					final ScoreBoard[] scores;
					switch (appResponse.request_id) {
					case GET_USER_SBS_RID:
						scores = (ScoreBoard[])appResponse.object;
						LeaderBoard.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scores != null) {
									mDbHelper.fillScoreBoardTemp(scores, false);
									fillData();
								}
								Editor editor = mSharedPreferences.edit();
								editor.putLong(Constants.LB_USERSCORES_UPDATE_KEY, 
											System.currentTimeMillis());
								editor.commit();
								mScoreWaitDialog.dismiss();
							}
						});
						break;
					case GET_GROUP_SBS_RID:
						scores = (ScoreBoard[])appResponse.object;
						LeaderBoard.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scores != null) {
									mDbHelper.fillScoreBoardTemp(scores, true);
									fillData();
								}
								Editor editor = mSharedPreferences.edit();
								editor.putLong(	Constants.LB_GROUPSCORES_UPDATE_KEY, 
											System.currentTimeMillis());
								editor.commit();
								mScoreWaitDialog.dismiss();
							}
						});
						break;
					case GET_APP_USERS_RID:
						final User[] users = (User[])appResponse.object;
						LeaderBoard.this.runOnUiThread(new Runnable() {
							public void run() {
								if (users != null)
									mDbHelper.addUsers(users);
								
								Editor editor = mSharedPreferences.edit();
								editor.putLong(Constants.ALL_USERS_UPDATE_KEY, 
											System.currentTimeMillis());
								editor.commit();
								mGetUsersGroupsProgress++;
								if (mGetUsersGroupsProgress >= mGetUsersGroupsGoal) {
									mUserGroupWaitDialog.dismiss();
									updateLBSelection(true);
								}
							}
						});
						break;
					case GET_GROUPS_RID:
						final Group[] groups = (Group[])appResponse.object;
						LeaderBoard.this.runOnUiThread(new Runnable() {
							public void run() {
								if (groups != null)
									mDbHelper.addGroupsTemp(groups);
								
								Editor editor = mSharedPreferences.edit();
								editor.putLong(Constants.ALL_GROUPS_UPDATE_KEY, 
											System.currentTimeMillis());
								editor.commit();
								mGetUsersGroupsProgress++;
								if (mGetUsersGroupsProgress >= mGetUsersGroupsGoal) {
									mUserGroupWaitDialog.dismiss();
									updateLBSelection(true);
								}
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
}
