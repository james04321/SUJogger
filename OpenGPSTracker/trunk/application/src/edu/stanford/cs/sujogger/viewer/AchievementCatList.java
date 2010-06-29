package edu.stanford.cs.sujogger.viewer;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.ScoreBoard;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Categories;
import edu.stanford.cs.sujogger.util.AchCatAdapter;
import edu.stanford.cs.sujogger.util.AchListAdapter;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

public class AchievementCatList extends ListActivity {
	private static final String TAG = "OGT.AchievementsActivity";
	private static final int MENU_REFRESH = 1;

	// Request IDs
	private static final int GET_GRP_SBS_RID = 0;

	private DatabaseHelper mDbHelper;
	private GamingServiceConnection mGameCon;
	private AchievementCatListReceiver mReceiver;
	private Handler mHandler = new Handler();

	private ProgressDialog mGetScoresDialog;
	private Button mStatisticsButton;
	private Button mLeaderboardsButton;

	private Cursor mRecAchEarned;
	private Cursor mRecAchLost;
	private SeparatedListAdapter mGroupedAdapter;

	private Runnable mRefreshTask = new Runnable() {
		public void run() {
			int[] statIds = mDbHelper.getGroupStatisticIds();
			if (statIds != null && statIds.length > 0) {
				mGetScoresDialog = ProgressDialog.show(AchievementCatList.this, "", 
						"Retrieving group statistics...", true);
				try {
					mGameCon.getScoreBoards(GET_GRP_SBS_RID, statIds);
				} catch (RemoteException e) {}
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.ach_list);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		mReceiver = new AchievementCatListReceiver();
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, AchievementCatList.class.toString());
		mGameCon.bind();

		// Create cursors
		mRecAchEarned = mDbHelper.getRecentAchievementsEarned();
		startManagingCursor(mRecAchEarned);
		mRecAchLost = mDbHelper.getRecentAchievementsLost();
		startManagingCursor(mRecAchLost);
		
		mStatisticsButton = (Button)findViewById(R.id.statisticsbutton);
		mStatisticsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(AchievementCatList.this, StatisticsView.class);
				intent.putExtra("group_id", -1);
				startActivity(intent);
			}
		});
		
		mLeaderboardsButton = (Button)findViewById(R.id.leaderboardsbutton);
		mLeaderboardsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(AchievementCatList.this, LeaderBoard.class);
				startActivity(i);
			}
		});
		
		fillData();
		registerForContextMenu(getListView());
		
		//Wait 100ms before sending request, because sometimes, the activity doesn't
		//bind to the service quickly enough
		mHandler.postDelayed(mRefreshTask, 100);
	}

	private void refreshAchievements() {
		mHandler.post(mRefreshTask);
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
		Log.d(TAG, "fillData(): dumping recent achievements cursor");
		DatabaseUtils.dumpCursor(mRecAchEarned);

	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mGameCon.unbind();
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Log.v(TAG, "position = " + position + "; id = " + id);
		Object item = mGroupedAdapter.getItem(position);
		if (item.getClass() == Integer.class) {
			Log.d(TAG, "starting AchGridView for cat = " + (Integer) item);

			Intent i = new Intent(this, AchievementList.class);
			i.putExtra(Categories.TABLE, (Integer) item);
			startActivity(i);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Log.d(TAG, "onCreateOptionsMenu()");
		
		menu.add(ContextMenu.NONE, MENU_REFRESH, ContextMenu.NONE, R.string.refresh).setIcon(
				R.drawable.ic_menu_refresh);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;

		switch (item.getItemId()) {
		case MENU_REFRESH:
			refreshAchievements();
			break;
		default:
			handled = super.onOptionsItemSelected(item);
			break;
		}

		return handled;
	}

	private void fillData() {
		mGroupedAdapter = new SeparatedListAdapter(this);

		mGroupedAdapter
				.addSection("Recently Earned", new AchListAdapter(this, mRecAchEarned, true));
		mGroupedAdapter.addSection("Recently Lost", new AchListAdapter(this, mRecAchLost, true));

		mGroupedAdapter.addSection("Difficulty", new AchCatAdapter(this, mDbHelper, 0));
		mGroupedAdapter.addSection("Type", new AchCatAdapter(this, mDbHelper, 1));

		setListAdapter(mGroupedAdapter);
	}

	private class AchievementCatListReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive()");
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());

					switch (appResponse.request_id) {
					case GET_GRP_SBS_RID:
						final ScoreBoard[] scores = (ScoreBoard[]) appResponse.object;
						AchievementCatList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scores != null) {
									mDbHelper.updateScoreboards(scores);
									Cursor newAchCursor = mDbHelper.updateAchievements();
									if (newAchCursor.getCount() > 0) {
										mRecAchEarned.requery();
										mRecAchLost.requery();
										mGroupedAdapter.notifyDataSetChanged();
										AchievementCatList.this.getListView().invalidateViews();
	
										newAchCursor.moveToNext();
										View toastLayout = getLayoutInflater().inflate(R.layout.ach_toast,
												(ViewGroup) findViewById(R.id.toast_layout_root));
										Common.displayAchievementToast(newAchCursor.getString(8), 
												newAchCursor.getInt(7), newAchCursor.getInt(4) == 0, 
												getApplicationContext(), toastLayout);
									}
									mGetScoresDialog.dismiss();
									newAchCursor.close();
								}
							}
						});
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
