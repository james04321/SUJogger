/*------------------------------------------------------------------------------
 **     Ident: Innovation en Inspiration > Google Android 
 **    Author: rene
 ** Copyright: (c) Jan 22, 2009 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced  
 ** Distributed Software Engineering |  or transmitted in any form or by any        
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the      
 ** 4131 NJ Vianen                   |  purpose, without the express written    
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edu.stanford.cs.sujogger.viewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;

import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.ScoreBoard;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.actions.Statistics;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.logger.GPSLoggerServiceManager;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.SegmentedControl;
import edu.stanford.cs.sujogger.util.TrackListAdapter;

/**
 * Show a list view of all tracks, also doubles for showing search results
 * 
 * @version $Id: TrackList.java 468 2010-03-28 13:47:13Z rcgroot $
 * @author rene (c) Jan 11, 2009, Sogeti B.V.
 */
public class TrackList extends ListActivity {
	private static final String TAG = "OGT.TrackList";
	private static final int MENU_DETELE = 0;
	private static final int MENU_SHARE = 1;
	private static final int MENU_RENAME = 2;
	private static final int MENU_STATS = 3;

	public static final int DIALOG_FILENAME = 0;
	private static final int DIALOG_RENAME = 23;
	private static final int DIALOG_DELETE = 24;
	
	public static final int PUBLISH_TRACK = 100;
	public static final int DOWNLOAD_TRACK = 101;

	// Request IDs
	public static final int CREATE_SB_RID = 1;
	public static final int GET_SBS_RID = 2;
	public static final int USERREG_RID = 3;
	public static final int GET_CG_RID = 4;

	private SharedPreferences mSharedPreferences;
	private ProgressDialog mDialogFriendInit;
	private ProgressDialog mDialogUserInit;
	private Button mStartButton;

	private DatabaseHelper mDbHelper;
	private GamingServiceConnection mGameCon;
	private ScoreboardReceiver mReceiver;
	
	//TODO: Facebook
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	//private WebAuth mWa;

	// Temp attribute to store FB friends until we get everything we need
	private long[] mFriendFbIds;

	private EditText mTrackNameView;
	private Uri mDialogUri;
	private String mDialogCurrentName = "";
	
	private TrackListAdapter trackAdapter;
	private boolean mDownloadedTracks = false;
	private final String DOWNLOADEDTRACKSFLAG = "DOWNLOADEDTRACKS";

	private OnClickListener mDeleteOnClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			getContentResolver().delete(mDialogUri, null, null);
			getListView().invalidateViews();
		}
	};
	private OnClickListener mRenameOnClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			// Log.d( TAG,
			// "Context item selected: "+mDialogUri+" with name "+mDialogCurrentName
			// );

			String trackName = mTrackNameView.getText().toString();
			ContentValues values = new ContentValues();
			values.put(Tracks.NAME, trackName);
			TrackList.this.getContentResolver().update(mDialogUri, values, null, null);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.tracklist);
		/*
		 * Log.d(TAG, "DOWNLOADEDTRACKS BEFORE " + mDownloadedTracks); if
		 * (savedInstanceState != null) { Log.d(TAG,
		 * "STATE OF DOWNLOADEDTRACK IS " +
		 * savedInstanceState.getBoolean("mDownloadedTrack")); }
		 */
		mDownloadedTracks = savedInstanceState != null ? savedInstanceState
				.getBoolean(DOWNLOADEDTRACKSFLAG) : false;
		Log.d(TAG, "DOWNLOADEDTRACKS AFTER " + mDownloadedTracks);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		mReceiver = new ScoreboardReceiver();
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, TrackList.class.toString());
		mGameCon.bind();
		
		Log.d(TAG, "ONCREATE DOWNLOADEDTRACKS IS " + mDownloadedTracks);

		mStartButton = (Button) findViewById(R.id.startbutton);
		mStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v("TrackList", "creating new track");
				Intent intent = new Intent();
				intent.setClass(TrackList.this, LoggerMap.class);
				startActivity(intent);
			}
		});
		
		LinearLayout topControlBar = (LinearLayout)findViewById(R.id.top_control_bar);
		topControlBar.addView(new SegmentedControl(this, new String[] {"My tracks", "Downloaded tracks"}, 
				mDownloadedTracks ? 1 : 0, new SegmentedControl.SegmentedControlListener() {
			public void onValueChanged(int newValue) {
				mDownloadedTracks = newValue == 1;
				displayIntent(getIntent());
			}
		}), 
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		displayIntent(getIntent());

		// Add the context menu (the long press thing)
		registerForContextMenu(getListView());
		
		//TODO: Facebook
		if (!mSharedPreferences.getBoolean(Constants.USER_REGISTERED, false)) {
			mFacebook = new Facebook();
			mAsyncRunner = new AsyncFacebookRunner(mFacebook);

			mFacebook.authorize(this, Constants.FB_APP_ID, Constants.FB_PERMISSIONS,
					new LoginDialogListener());
		}
		
		/*
		if (!mSharedPreferences.getBoolean(Constants.USER_REGISTERED, false)) {
			mWa = new WebAuth();
			mWa.authorize(this, new LoginDialogListener());
		}*/
	}

	private void initializeSelfStatistics() {
		try {
			ScoreBoard score;
			int[] allStats = Stats.ALL_STAT_IDS;
			ScoreBoard[] scores = new ScoreBoard[allStats.length];
			for (int i = 0; i < allStats.length; i++) {
				score = new ScoreBoard();
				score.app_id = Constants.APP_ID;
				score.user_id = Common.getRegisteredUser(this).id;
				score.group_id = 0;
				score.value = 0;
				score.sb_type = String.valueOf(allStats[i]);
				scores[i] = score;
			}
			mGameCon.createScoreBoards(CREATE_SB_RID, scores);
		}
		catch (RemoteException e) {
		}
	}

	@Override
	public void onNewIntent(Intent newIntent) {
		Log.d(TAG, "onNewIntent()");
		displayIntent(newIntent);
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");

		trackAdapter.notifyDataSetChanged();
		getListView().invalidate();
		getListView().invalidateViews();
		super.onRestart();
	}

	@Override
	protected void onResume() {
		trackAdapter.notifyDataSetChanged();
		getListView().invalidate();
		getListView().invalidateViews();
		
		int state = GPSLoggerServiceManager.getLoggingState();
		switch (state) {
		case Constants.STOPPED:
			mStartButton.setText(R.string.track_start);
			break;
		case Constants.LOGGING:
		case Constants.PAUSED:
			mStartButton.setText(R.string.track_return);
			break;
		default:
			break;
		}
		
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		Log.v("TrackList", "onRestoreInstanceState");
		mDialogUri = state.getParcelable("URI");
		mDialogCurrentName = state.getString("NAME");
		// mDownloadedTracks = state.getBoolean("mDownloadedTracks");
		// Log.d(TAG, "RESTORING DOWNLOADED TRACKS " + mDownloadedTracks);

		// setTrackList();

		super.onRestoreInstanceState(state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "SAVING DOWNLOADED TRACKS " + mDownloadedTracks);

		outState.putParcelable("URI", mDialogUri);
		outState.putString("NAME", mDialogCurrentName);
		outState.putBoolean(DOWNLOADEDTRACKSFLAG, mDownloadedTracks);

		super.onSaveInstanceState(outState);
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
		Log.v("TrackList", "position = " + position + "; id = " + id);
		
		Intent intent = new Intent();
		intent.setData(ContentUris.withAppendedId(Tracks.CONTENT_URI, id));
		intent.setClass(this, LoggerMap.class);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
			AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
			TextView textView = (TextView) itemInfo.targetView.findViewById(android.R.id.text1);
			if (textView != null) {
				menu.setHeaderTitle(textView.getText());
			}
		}
		menu.add(0, MENU_STATS, 0, R.string.menu_statistics);
		menu.add(0, MENU_SHARE, 0, R.string.menu_shareTrack);
		menu.add(0, MENU_RENAME, 0, R.string.menu_renameTrack);
		menu.add(0, MENU_DETELE, 0, R.string.menu_deleteTrack);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean handled = false;
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		}
		catch (ClassCastException e) {
			Log.e(TAG, "Bad menuInfo", e);
			return handled;
		}
		
		long trackId = trackAdapter.getItemId(info.position);
		
		Uri trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId);
		Log.d(TAG, "onContextItemSelected(): trackUri=" + trackUri);
		ContentResolver resolver = this.getApplicationContext().getContentResolver();
		Cursor trackCursor = null;
		long remoteTrackId = 0;
		try {
			trackCursor = resolver.query(trackUri, new String[] { Tracks.NAME, Tracks.TRACK_ID }, null, null, null);
			if (trackCursor != null && trackCursor.moveToLast())
				remoteTrackId = trackCursor.getLong(1);
			
			mDialogUri = trackUri;
			mDialogCurrentName = trackCursor.getString(0);
			switch (item.getItemId()) {
			case MENU_DETELE: {
				showDialog(DIALOG_DELETE);
				handled = true;
				break;
			}
			case MENU_SHARE: {
				Intent actionIntent;
				if (remoteTrackId == 0)
					actionIntent = new Intent("android.intent.action.PUBLISH");
				else
					actionIntent = new Intent(Intent.ACTION_RUN);
				actionIntent.setDataAndType(mDialogUri, Tracks.CONTENT_ITEM_TYPE);
				actionIntent.putExtra("name", mDialogCurrentName);
				actionIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivity(Intent.createChooser(actionIntent, getString(R.string.chooser_title)));
				handled = true;
				break;
			}
			case MENU_RENAME: {
				showDialog(DIALOG_RENAME);
				handled = true;
				break;
			}
			case MENU_STATS: {
				Intent actionIntent = new Intent(this, Statistics.class);
				actionIntent.setData(mDialogUri);
				startActivity(actionIntent);
				handled = true;
				break;
			}
			default:
				handled = super.onContextItemSelected(item);
				break;
			}
		}
		finally {
			if (trackCursor != null) {
				trackCursor.close();
			}
		}

		return handled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		Builder builder = null;
		View view;
		switch (id) {
		case DIALOG_RENAME:
			LayoutInflater factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.namedialog, null);
			mTrackNameView = (EditText) view.findViewById(R.id.nameField);

			builder = new AlertDialog.Builder(this).setTitle(R.string.dialog_routename_title)
					.setMessage(R.string.dialog_routename_message).setIcon(
							android.R.drawable.ic_dialog_alert).setPositiveButton(
							R.string.btn_okay, mRenameOnClickListener).setNegativeButton(
							R.string.btn_cancel, null).setView(view);
			dialog = builder.create();
			return dialog;
		case DIALOG_DELETE:
			builder = new AlertDialog.Builder(TrackList.this).setTitle(R.string.dialog_deletetitle)
					.setIcon(android.R.drawable.ic_dialog_alert).setNegativeButton(
							android.R.string.cancel, null).setPositiveButton(android.R.string.ok,
							mDeleteOnClickListener);
			dialog = builder.create();
			String messageFormat = this.getResources()
					.getString(R.string.dialog_deleteconfirmation);
			String message = String.format(messageFormat, "");
			((AlertDialog) dialog).setMessage(message);
			return dialog;
		default:
			return super.onCreateDialog(id);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_RENAME:
			if (mDialogCurrentName == null) {
				mTrackNameView.setText("");
				mTrackNameView.setSelection(0, 0);
			}
			else {
				mTrackNameView.setText(mDialogCurrentName);
				mTrackNameView.setSelection(0, mDialogCurrentName.length());
			}
			break;
		case DIALOG_DELETE:
			AlertDialog alert = (AlertDialog) dialog;
			String messageFormat = this.getResources()
					.getString(R.string.dialog_deleteconfirmation);
			String message = String.format(messageFormat, mDialogCurrentName);
			alert.setMessage(message);
			break;
		}
	}

	private void displayIntent(Intent intent) {
		Log.d(TAG, "displayIntent()");
		
		TextView emptyView = (TextView)getListView().getEmptyView();
		emptyView.setText(mDownloadedTracks ? 
				R.string.no_downloaded_tracks : R.string.no_tracks);
		
		final String queryAction = intent.getAction();
		Cursor tracksCursor = null;
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			// Got to SEARCH a query for tracks, make a list
			tracksCursor = doSearchWithIntent(intent);
			displayCursor(tracksCursor);
		}
		else if (Intent.ACTION_VIEW.equals(queryAction)) {
			// Got to VIEW a single track, instead had it of to the LoggerMap
			Intent notificationIntent = new Intent(this, LoggerMap.class);
			notificationIntent.setData(intent.getData());
			startActivity(notificationIntent);
		}
		else {
			// Got to nothing, make a list of everything
			if (mDownloadedTracks) {
				tracksCursor = mDbHelper.getDownloadedTracks(Common.getRegisteredUser(this).id);
				startManagingCursor(tracksCursor);
			}
			else {
				String whereClause = null;
				whereClause = Tracks.USER_ID + (mDownloadedTracks ? "!=" : "=")
						+ Common.getRegisteredUser(this).id + " AND " + Tracks.NAME + " <> ''";
				Log.d(TAG, "WHERECLAUSE IS " + whereClause);
				tracksCursor = managedQuery(Tracks.CONTENT_URI, new String[] { Tracks._ID, Tracks.NAME,
						Tracks.CREATION_TIME, Tracks.DURATION, Tracks.DISTANCE, Tracks.TRACK_ID, Tracks.USER_ID },
						whereClause, null, null);
			}
			Log.d(TAG, "displayIntent(): displaying all tracks. count = "
							+ tracksCursor.getCount());
			displayCursor(tracksCursor);
		}

	}

	private void displayCursor(Cursor tracksCursor) {
		Log.d(TAG, "displayCursor(): " + DatabaseUtils.dumpCursorToString(tracksCursor));
		trackAdapter = new TrackListAdapter(this, tracksCursor);
		setListAdapter(trackAdapter);
	}

	private Cursor doSearchWithIntent(final Intent queryIntent) {
		final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
		Cursor cursor = managedQuery(Tracks.CONTENT_URI, new String[] { Tracks._ID, Tracks.NAME,
				Tracks.CREATION_TIME, Tracks.DURATION, Tracks.DISTANCE, Tracks.TRACK_ID },
				"name LIKE ?" + (new String[] { "%" + queryString + "%" }) + " and user_id "
						+ (mDownloadedTracks ? "!=" : "=") + Common.getRegisteredUser(this).id,
				null, null);
		return cursor;
	}

	private void registerUser() {
		//TODO: Facebook
		if (Common.getRegisteredUser(this) == null || mFriendFbIds == null)
			return;
		
		//TODO: Stanford WebAuth
		//if (Common.getRegisteredUser(this) == null)
		//	return;
		
		//TODO: Facebook
		mDialogFriendInit.dismiss();
		mDialogUserInit = ProgressDialog.show(this, "", "Initializing user profile...", true);
		User user = Common.getRegisteredUser(this);
		user.friend_fb_ids = mFriendFbIds;
		//user.friend_fb_ids = null; // when using Stanford WebAuth
		try {
			mGameCon.registerUser(USERREG_RID, user);
		}
		catch (RemoteException e) {}
	}

	private class ScoreboardReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive()");
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						TrackList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mDialogUserInit != null) mDialogUserInit.dismiss();
								new AlertDialog.Builder(TrackList.this).setMessage(R.string.connection_error_toast)
								.setCancelable(false)
								.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										//TODO:Facebook
										mFacebook.authorize(TrackList.this, Constants.FB_APP_ID, Constants.FB_PERMISSIONS,
												new LoginDialogListener());
										
										//mWa.authorize(TrackList.this, new LoginDialogListener());
									}
								}).show();
							}
						});
						continue;
					}
					
					switch (appResponse.request_id) {
					case GET_SBS_RID:
						final ScoreBoard[] scores = (ScoreBoard[]) appResponse.object;
						TrackList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scores == null) {
									Log.d(TAG, "onReceive(): no scores available");
									initializeSelfStatistics();
								}
								else {
									Log.d(TAG, "onReceive(): scores found");
									mDbHelper.updateSoloScoreboards(scores);
		
									Editor editorGetSb = mSharedPreferences.edit();
									editorGetSb.putBoolean(Constants.USER_REGISTERED, true);
									editorGetSb.commit();
									mDialogUserInit.dismiss();
								}
							}
						});
						break;
					case CREATE_SB_RID:
						final Integer[] scoreIds = (Integer[]) appResponse.object;
						TrackList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scoreIds != null)
									mDbHelper.updateSoloScoreboardIds(scoreIds);
		
								Editor editorCreateSb = mSharedPreferences.edit();
								editorCreateSb.putBoolean(Constants.USER_REGISTERED, true);
								editorCreateSb.commit();
								mDialogUserInit.dismiss();
							}
						});
						break;
					case USERREG_RID:
						final int userId = (Integer) appResponse.object;
						TrackList.this.runOnUiThread(new Runnable() {
							public void run() {
								Log.d(TAG, "onReceive(): user registered");
								Editor editorUser = mSharedPreferences.edit();
								editorUser.putInt(Constants.USERREG_ID_KEY, userId);
								editorUser.putLong(Constants.FB_UPDATE_KEY, System.currentTimeMillis());
								editorUser.commit();
								try {
									mGameCon.getScoreBoards(GET_SBS_RID, userId, -1, null, null);
								}
								catch (RemoteException e) {
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
	
	//TODO: Facebook
	
	private final class LoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {
			// SessionEvents.onLoginSuccess();
			Log.d(TAG, "Facebook login successfull!!!");
			if (mFacebook.getAccessToken() != null) {
				Editor editor = mSharedPreferences.edit();
				editor.putString(Constants.FB_ACCESS_TOKEN_KEY, mFacebook.getAccessToken());
				editor.commit();
			}
			mDialogFriendInit = ProgressDialog.show(TrackList.this, "",
					"Retrieving your friends...", true);
			mAsyncRunner.request("me", new UserInfoRequestListener());
			mAsyncRunner.request("me/friends", new FriendsRequestListener());
		}

		public void onFacebookError(FacebookError error) {
			// SessionEvents.onLoginError(error.getMessage());
		}

		public void onError(DialogError error) {
			// SessionEvents.onLoginError(error.getMessage());
			new AlertDialog.Builder(TrackList.this).setMessage(error.getMessage())
				.setCancelable(false)
				.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					mFacebook.authorize(TrackList.this, Constants.FB_APP_ID, Constants.FB_PERMISSIONS,
							new LoginDialogListener());
				}
			}).show();
		}

		public void onCancel() {
			Log.d(TAG, "onCancel()");
			if (!mSharedPreferences.getBoolean(Constants.USER_REGISTERED, false)) {
				Toast toast = Toast.makeText(TrackList.this.getApplicationContext(),
						"Facebook login is required", Toast.LENGTH_SHORT);
				toast.show();
				mFacebook.authorize(TrackList.this, Constants.FB_APP_ID, Constants.FB_PERMISSIONS,
						new LoginDialogListener());
			}
		}
	}

	private class UserInfoRequestListener implements RequestListener {

		public void onComplete(final String response) {
			try {
				// process the response here: executed in background thread
				Log.d(TAG, "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);

				Editor editor = mSharedPreferences.edit();
				editor.putLong(Constants.USERREG_FBID_KEY, json.getLong("id"));
				editor.putString(Constants.USERREG_EMAIL_KEY, json.getString("email"));
				editor.putString(Constants.USERREG_FIRSTNAME_KEY, json.getString("first_name"));
				editor.putString(Constants.USERREG_LASTNAME_KEY, json.getString("last_name"));
				editor.putString(Constants.USERREG_PICTURE_KEY, Constants.GRAPH_BASE_URL
						+ json.getLong("id") + "/picture");
				editor.putString(Constants.USERREG_TOKEN_KEY, mFacebook.getAccessToken());
				editor.commit();

				TrackList.this.runOnUiThread(new Runnable() {
					public void run() {
						registerUser();
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

		public void onFileNotFoundException(FileNotFoundException e) {}

		public void onIOException(IOException e) {}

		public void onMalformedURLException(MalformedURLException e) {}
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
				
				TrackList.this.runOnUiThread(new Runnable() {
					public void run() {
						try {
							long[] fbIds = new long[friends.length()];
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
							
							mFriendFbIds = fbIds;
							registerUser();
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

		public void onFileNotFoundException(FileNotFoundException e) {}

		public void onIOException(IOException e) {}

		public void onMalformedURLException(MalformedURLException e) {}
	}
	
	//TODO: Stanford WebAuth
	/*
	private final class LoginDialogListener implements DialogListener {
    	public void onComplete(Bundle values) {
    		Log.d(TAG, values.toString());
    		
    		Editor editor = mSharedPreferences.edit();
			editor.putLong(Constants.USERREG_FBID_KEY, 0);
			editor.putString(Constants.USERREG_EMAIL_KEY, 
					values.getString(WebAuth.SUID_KEY) + WebAuth.EMAIL_SUFFIX);
			editor.putString(Constants.USERREG_FIRSTNAME_KEY, values.getString(WebAuth.FIRSTNAME_KEY));
			editor.putString(Constants.USERREG_LASTNAME_KEY, values.getString(WebAuth.LASTNAME_KEY));
			editor.putString(Constants.USERREG_PICTURE_KEY, null);
			editor.commit();
    		
    		registerUser();
    	}
    	
        public void onError(DialogError e) {
        	if (e.getErrorCode() != WADialog.USERINFO_ERROR)
				new AlertDialog.Builder(TrackList.this).setMessage(e.getMessage())
					.setCancelable(false)
					.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						mWa.authorize(TrackList.this, new LoginDialogListener());
					}
				}).show();
        }
    }*/
}
