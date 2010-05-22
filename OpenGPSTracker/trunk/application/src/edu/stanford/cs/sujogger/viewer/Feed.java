package edu.stanford.cs.sujogger.viewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.util.Constants;

public class Feed extends ListActivity {
	private static final String TAG = "OGT.Feed";
	private static final int FILTER_MODE_ALL = 0;
	private static final int FILTER_MODE_MSG = 1;
	private static final int FILTER_MODE_BCAST = 2;
	
	private DatabaseHelper mDbHelper;
	private Cursor mGroupsCursor;
	private int mFilterMode;
	
	private GamingServiceConnection mGameCon;
	private FeedReceiver mReceiver;
	
	//Views
	private RadioGroup mFilterOptions;
	
	//Options menu items
	private static final int MENU_FILTER = 1;
	private static final int MENU_COMPOSE = 2;
	private static final int MENU_REFRESH = 3;
	
	//Dialogs
	private static final int DIALOG_FILTER = 1;
	
	//Listeners
	private final RadioGroup.OnCheckedChangeListener mFilterOptionsListener =
		new RadioGroup.OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.d(TAG, "checkedId = " + checkedId);
				Feed.this.dismissDialog(DIALOG_FILTER);
			}
		};
	
	public Feed() {}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.list_simple);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mReceiver = new FeedReceiver(); 
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, 
				Constants.APP_ID, Constants.APP_API_KEY, Feed.class.toString());
		mGameCon.bind();
		
		//registerForContextMenu(getListView());
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
		
		menu.add(ContextMenu.NONE, MENU_FILTER, ContextMenu.NONE, R.string.feed_menu_filter);
		menu.add(ContextMenu.NONE, MENU_COMPOSE, ContextMenu.NONE, R.string.feed_menu_compose);
		menu.add(ContextMenu.NONE, MENU_REFRESH, ContextMenu.NONE, R.string.feed_menu_refresh);
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;
		
		switch (item.getItemId()) {
		case MENU_FILTER:
			showDialog(DIALOG_FILTER);
			handled = true;
			break;
		case MENU_COMPOSE:
			handled = true;
			break;
		case MENU_REFRESH:
			handled = true;
			break;
		default:
			handled = super.onOptionsItemSelected(item);
			break;
		}
		
		return handled;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		View view = null;
		Builder builder = null;
		
		switch(id) {
		case DIALOG_FILTER:
			builder = new AlertDialog.Builder(this);
			view = LayoutInflater.from(this).inflate(R.layout.feed_filter_menu, null);
			mFilterOptions = (RadioGroup)view.findViewById(R.id.filter_menu);
			mFilterOptions.setOnCheckedChangeListener(mFilterOptionsListener);
			builder.setTitle("Filter").setView(view);
			return builder.create();
		default: return super.onCreateDialog(id);
		}
	}
	
	class FeedReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					switch(appResponse.request_id) {
					/*
					case GRP_CREATE_RID:
						GroupList.this.toggleNewGroupItemState();
						Integer groupId = (Integer)(appResponse.object);
						Group newGroup = (Group)(appResponse.appRequest.object);
						Log.d(TAG, "onReceive(): groupId = " + groupId + "; groupName = " + newGroup.name);
						GroupList.this.mDbHelper.addGroup(groupId.longValue(), newGroup.name, 1);
						GroupList.this.mGroupsCursor.requery();
						GroupList.this.mGroupAdapter.notifyDataSetChanged();
						GroupList.this.getListView().invalidateViews();
						break;*/
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
