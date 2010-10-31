package edu.stanford.cs.sujogger.viewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;

import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Message;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.GameMessages;
import edu.stanford.cs.sujogger.logger.SettingsDialog;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.GameMessageAdapter;
import edu.stanford.cs.sujogger.util.MessageObject;

public class Feed extends ListActivity {
	private static final String TAG = "OGT.Feed";
	private static final String FILTER_MODE_KEY = "filter_mode";
	private static final int FILTER_MODE_ALL = 0;
	private static final int FILTER_MODE_MSG = 1;
	private static final int FILTER_MODE_BCAST = 2;
	
	private DatabaseHelper mDbHelper;
	private Cursor mMessages;
	private int mFilterMode;
	private GameMessageAdapter mAdapter;
	private SharedPreferences mSharedPreferences;
	private Handler mHandler = new Handler();
	
	private Button mComposeButton;
	
	private GamingServiceConnection mGameCon;
	private FeedReceiver mReceiver;
	private boolean isUpdating;
	
	//Request IDs
	private static final int GET_MSG_RID = 1;
	
	//Views
	private RadioGroup mFilterOptions;
	
	//Options menu items
	//private static final int MENU_FILTER = 0;
	private static final int MENU_REFRESH = 2;
	private static final int MENU_SETTINGS = 10;
	
	//Dialogs
	private static final int DIALOG_FILTER = 1;
	
	//Listeners
	private final RadioGroup.OnCheckedChangeListener mFilterOptionsListener =
		new RadioGroup.OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Common.log(TAG, "checkedId = " + checkedId);
				switch(checkedId) {
				case R.id.filter_menu_all: mFilterMode = FILTER_MODE_ALL; break;
				case R.id.filter_menu_msg: mFilterMode = FILTER_MODE_MSG; break;
				case R.id.filter_menu_bcast: mFilterMode = FILTER_MODE_BCAST; break;
				default: break;
				}
				
				Feed.this.dismissDialog(DIALOG_FILTER);
				updateFiltering();
			}
		};
	
	private Runnable mRefreshTask = new Runnable() {
		public void run() {
			int lastConciergeId = mSharedPreferences.getInt(
					Constants.LAST_CONCIERGE_ID_KEY, 1);
			Common.log(TAG, "lastConciergeId = " + lastConciergeId);
			int limit = lastConciergeId == 1 ? 10 : -1;
			try {
				mGameCon.getMessages(GET_MSG_RID, lastConciergeId, limit);
			} catch (RemoteException e) {}
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Common.log(TAG, "onCreate()");
		this.setContentView(R.layout.feed);
		
		mFilterMode = savedInstanceState != null ? savedInstanceState.getInt(FILTER_MODE_KEY) : -1;
		if (mFilterMode == -1) {
			Bundle extras = getIntent().getExtras();
			mFilterMode = extras != null ? extras.getInt(FILTER_MODE_KEY) : 0;
		}
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		mMessages = null;
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mReceiver = new FeedReceiver(); 
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, 
				Constants.APP_ID, Constants.APP_API_KEY, Feed.class.toString());
		mGameCon.bind();
		User user = Common.getRegisteredUser(this);
		mGameCon.setUserId(user.id, user.fb_id, user.fb_token);
		isUpdating = false;
		
		mComposeButton = (Button)findViewById(R.id.composebutton);
		mComposeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Feed.this, MessageSender.class);
				startActivity(i);
			}
		});
		
		if (Constants.AD_TEST) AdManager.setTestDevices(new String[] { "3468678E351E95A5F7A64D2271BCB7BF" });
		AdView adView = (AdView)View.inflate(this, R.layout.adview, null);
		getListView().addHeaderView(adView);
		
		updateFiltering();
		fillData();
		
		if (System.currentTimeMillis() - 
				mSharedPreferences.getLong(Constants.FEED_UPDATE_KEY, 0) > 
					Constants.UPDATE_INTERVAL)
			//Wait 100ms before sending request, because sometimes, the activity doesn't
			//bind to the service quickly enough
			isUpdating = true;
			mHandler.postDelayed(mRefreshTask, 100);
	}
	
	@Override
	protected void onRestart() {
		Common.log(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Common.log(TAG, "onResume()");
		super.onResume();
		mMessages.requery();
		mAdapter.notifyDataSetChanged();
		getListView().invalidateViews();
		if (Constants.SHOW_DEBUG) DatabaseUtils.dumpCursor(mMessages);
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mGameCon.unbind();
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Common.log(TAG, "onSaveInstanceState()");
		super.onSaveInstanceState(outState);
		outState.putInt(FILTER_MODE_KEY, mFilterMode);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		
		position--; //ignore ad header
		
		long gmId = (Long)mAdapter.getItem(position);
		Intent i = new Intent(this, GameMessageDetail.class);
		i.putExtra(GameMessages.TABLE, gmId);
		startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Common.log(TAG, "onCreateOptionsMenu()");
		
		//menu.add(ContextMenu.NONE, MENU_FILTER, ContextMenu.NONE, R.string.feed_menu_filter)
		//	.setIcon(R.drawable.ic_menu_agenda);
		menu.add(ContextMenu.NONE, MENU_REFRESH, ContextMenu.NONE, R.string.refresh)
			.setIcon(R.drawable.ic_menu_refresh);
		menu.add(ContextMenu.NONE, MENU_SETTINGS, ContextMenu.NONE, "Settings")
			.setIcon(R.drawable.ic_menu_preferences);
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;
		
		switch (item.getItemId()) {
		/*
		case MENU_FILTER:
			showDialog(DIALOG_FILTER);
			handled = true;
			break;*/
		case MENU_REFRESH:
			if (isUpdating) {
				Common.log(TAG, "BLOCKING UPDATE REQUEST!!!!!!!!!!!");
				break;
			}
			Common.log(TAG, "refreshing...");
			isUpdating = true;
			mHandler.post(mRefreshTask);
			break;
		case MENU_SETTINGS:
			startActivity(new Intent(this, SettingsDialog.class));
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
			switch(mFilterMode) {
			case FILTER_MODE_ALL: mFilterOptions.check(R.id.filter_menu_all); break;
			case FILTER_MODE_MSG: mFilterOptions.check(R.id.filter_menu_msg); break;
			case FILTER_MODE_BCAST: mFilterOptions.check(R.id.filter_menu_bcast); break;
			default: break;
			}
			mFilterOptions.setOnCheckedChangeListener(mFilterOptionsListener);
			builder.setTitle("Filter").setView(view);
			return builder.create();
		default: return super.onCreateDialog(id);
		}
	}
	
	private void updateFiltering() {
		if (mMessages != null) mMessages.close();
		
		switch(mFilterMode) {
		case FILTER_MODE_ALL:
			mMessages = mDbHelper.getAllMessagesWithFromUsers();
			break;
		case FILTER_MODE_MSG: 
			mMessages = mDbHelper.getMessagesWithFromUsers(false);
			break;
		case FILTER_MODE_BCAST: 
			mMessages = mDbHelper.getMessagesWithFromUsers(true);
			break;
		default: break;
		}
		
		startManagingCursor(mMessages);
		if (mAdapter != null)
			mAdapter.changeCursor(mMessages);
	}
	
	private void refresh() {
		int lastConciergeId = mSharedPreferences.getInt(
				Constants.LAST_CONCIERGE_ID_KEY, 1);
		Common.log(TAG, "lastConciergeId = " + lastConciergeId);
		int limit = lastConciergeId == 1 ? 10 : -1;
		try {
			mGameCon.getMessages(GET_MSG_RID, lastConciergeId, limit);
		} catch (RemoteException e) {}
	}
	
	private void fillData() {
		Common.log(TAG, "fillData()");
		mAdapter = new GameMessageAdapter(this, mMessages, false);
		setListAdapter(mAdapter);
	}
	
	class FeedReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Common.log(TAG, "onReceive()");
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Common.log(TAG, appResponse.toString());
					
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						Feed.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast toast = Toast.makeText(Feed.this, 
										R.string.connection_error_toast, Toast.LENGTH_SHORT);
								toast.show();
								isUpdating = false;
							}
						});
						continue;
					}
					
					switch(appResponse.request_id) {
					case GET_MSG_RID:
						final Message[] msgs = (Message[]) appResponse.object;
						final int lastConciergeId = appResponse.last_concierge_id;
						Feed.this.runOnUiThread(new Runnable() {
							public void run() {
								if (msgs != null) {
									for (int i=0; i < msgs.length; i++) {
										Message msg = msgs[i];
										if (msg != null) {
											Common.log(TAG, "onReceive(): lastConciergeId = " + lastConciergeId);
											Editor editor = mSharedPreferences.edit();
											editor.putInt(Constants.LAST_CONCIERGE_ID_KEY, lastConciergeId);
											editor.commit();
											
											User fromUser = msg.fromUser;
											Common.log(TAG, "onReceive(): sender firstName = " + fromUser.first_name);
											Common.log(TAG, "onReceive(): sender lastName = " + fromUser.last_name);
											Common.log(TAG, "onReceive(): msg = " + ((MessageObject) msg.msg).mBody);
											
											//Ignore messages that, for some reason, has come from the same person
											if (fromUser.id == Common.getRegisteredUser(Feed.this).id) continue;
											
											mDbHelper.insertGameMessage(fromUser, msg.toUsers, msg.dateTime, 
													(MessageObject)msg.msg);
											
											mMessages.requery();
											mAdapter.notifyDataSetChanged();
											Feed.this.getListView().invalidateViews();
											
											editor.putLong(Constants.FEED_UPDATE_KEY, System.currentTimeMillis());
											editor.commit();
										}
									}
								}
						
								Toast toast = Toast.makeText(Feed.this, 
										"Inbox up to date", Toast.LENGTH_SHORT);
								toast.show();
								isUpdating = false;
							}
						});
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
