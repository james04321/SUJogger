package edu.stanford.cs.sujogger.viewer;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Users;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.SegmentedControl;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

public class UserDetail extends ListActivity {
	private static final String TAG = "OGT.UserDetail";
	
	private long mUserId;
	private Cursor mUser;
	private DatabaseHelper mDbHelper;
	private SeparatedListAdapter mGroupedAdapter;
	private int mDisplayMode;
	
	private GamingServiceConnection mGameCon;
	private UserDetailReceiver mReceiver;
	
	// Display modes
	private static final int DISP_GROUPS = 0;
	private static final int DISP_TRACKS = 1;
	
	// Request IDs
	private static final int GRP_GET_RID = 1;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		
		mUserId = savedInstanceState != null ? savedInstanceState.getLong(Users.USER_ID) : 0;
		if (mUserId == 0) {
			Bundle extras = getIntent().getExtras();
			mUserId = extras != null ? extras.getLong(Users.USER_ID) : 0;
		}
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mReceiver = new UserDetailReceiver();
		mGameCon = new GamingServiceConnection(this, mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, UserDetail.class.toString());
		mGameCon.bind();
		mGameCon.setUserId(Common.getRegisteredUser(this).id);
		
		LinearLayout bottomControlBar = (LinearLayout)findViewById(R.id.bottom_control_bar);
		bottomControlBar.addView(new SegmentedControl(this, new String[] {"Groups", "Tracks"}, 
				mDisplayMode, new SegmentedControl.SegmentedControlListener() {
			public void onValueChanged(int newValue) {
				
			}
		}),
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		fillData();
	}
	
	private void fillData() {
		
	}
	
	class UserDetailReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					
					switch (appResponse.request_id) {
					case GRP_GET_RID:
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
