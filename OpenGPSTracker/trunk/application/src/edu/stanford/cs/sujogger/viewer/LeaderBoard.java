package edu.stanford.cs.sujogger.viewer;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.util.Constants;

public class LeaderBoard extends ListActivity {
	private static final String TAG = "OGT.LeaderBoard";
	
	private DatabaseHelper mDbHelper;
	
	private GamingServiceConnection mGameCon;
	private LeaderBoardReceiver mReceiver;
	
	private Spinner mSpinner;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.leaderboard);
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mReceiver = new LeaderBoardReceiver(); 
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, 
				Constants.APP_ID, Constants.APP_API_KEY, Feed.class.toString());
		mGameCon.bind();
		mGameCon.setUserId(6);
		
		mSpinner = (Spinner)findViewById(R.id.lb_spinner);
		
		fillData();
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
	
	private void fillData() {
		
	}
	
	class LeaderBoardReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive()");
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					switch(appResponse.request_id) {
					
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
