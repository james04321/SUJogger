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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.catchnotes.integration.IntentIntegrator;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.ScoreBoard;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.actions.Statistics;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Media;
import edu.stanford.cs.sujogger.db.GPStracking.Segments;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.db.GPStracking.Waypoints;
import edu.stanford.cs.sujogger.logger.GPSLoggerServiceManager;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.Track;
import edu.stanford.cs.sujogger.util.UnitsI18n;
 
/**
 * Main activity showing a track and allowing logging control
 * 
 * @version $Id: LoggerMap.java 479 2010-04-18 13:18:18Z rcgroot $
 * @author rene (c) Jan 18, 2009, Sogeti B.V.
 */
public class LoggerMap extends MapActivity {
	private static final int ZOOM_LEVEL = 19;
	
	// Menus
	//private static final int MENU_SETTINGS = 1;
	private static final int MENU_TRACKLIST = 3;
	private static final int MENU_STATS = 4;
	private static final int MENU_ABOUT = 5;
	private static final int MENU_LAYERS = 6;
	private static final int MENU_CLEARTRACK = 7;
	//private static final int MENU_NOTE = 7;
	private static final int MENU_NAME = 8;
	private static final int MENU_PICTURE = 9;
	private static final int MENU_TEXT = 10;
	private static final int MENU_VOICE = 11;
	private static final int MENU_VIDEO = 12;
	private static final int MENU_SHARE = 13;
	private static final int MENU_CURRENT_LOCATION = 14;

	// Dialogs
	private static final int DIALOG_TRACKNAME = 23;
	//private static final int DIALOG_LAYERS = 31;
	private static final int DIALOG_TEXT = 32;
	private static final int DIALOG_NAME = 33;
	
	// Views
	private MapView mMapView = null;
	private MyLocationOverlay mMylocation;
	//private CheckBox mSatellite;
	//private CheckBox mTraffic;
	//private CheckBox mSpeed;
	//private CheckBox mCompass;
	//private CheckBox mLocation;
	private EditText mTrackNameView;
	private TextView[] mSpeedtexts = null;
	private TextView mLastGPSSpeedView = null;
	private EditText mNoteNameView;
	private EditText mNoteTextView;
	private Button mStartButton;
	private Button mResumeButton;
	
	private static final String TAG = "OGT.LoggerMap";
	public static final String PARTNER_RUN_KEY = "partner_run";
	//private double mAverageSpeed = 4.4704; //in m/s
	private double mAverageSpeed = 2.2352; //in m/s
	//ASLAI
	private ArrayList<Track> mTrackIds = new ArrayList<Track>();
	private ArrayList<Track> mTrackIdsForDialog;
//	private long mTrackId = -1;
	private boolean statisticsPresent = false;
	private long mLastSegment = -1;
	private long mLastWaypoint = -1;
	private UnitsI18n mUnits;
	private WakeLock mWakeLock = null;
	private MapController mMapController = null;
	private SharedPreferences mSharedPreferences;
//	private GPSLoggerServiceManager GPSLoggerServiceManager;
	private DatabaseHelper mDbHelper;
	private boolean mIsPartnerRun;
	private boolean mTrackNameDialogShowing;
	private boolean mStatsUpdating;
	private double mCalculatedDistance;
	private long mCalculatedDuration;
	
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	
	public static final int UPDATE_SBS_RID = 1;
	public static final int GET_SBS_RID = 2;
	
	private GamingServiceConnection mGameCon;
	private ScoreboardUpdateReceiver mReceiver;
	private ProgressDialog mDialogUpdate;

	//ASLAI: created new function to do animateTo
	private void animateTo() {		
		int state = GPSLoggerServiceManager.getLoggingState();
		if (mTrackIds.size() > 0 && Track.visibleExists(mTrackIds)) {
			if (state == Constants.LOGGING || state == Constants.PAUSED)   {
				Common.log(TAG, "ASLAI: GOING TO LAST TRACK POINT");
				mMapView.getController().animateTo(getLastTrackPoint());
			} else { 
				Common.log(TAG, "ASLAI: GOING TO FIRST TRACK POINT");

				mMapView.getController().animateTo(getFirstTrackPoint());
			}
		}
		
	}	
	
	private ArrayList<Long> toTrackIdsLongArray(long[] longArray) {
		if (longArray == null || longArray.length <=0) {
			return new ArrayList<Long>();
		}
		ArrayList<Long> arrayList = new ArrayList<Long>();
		for (int i=0; i < longArray.length; i++) {
			arrayList.add(longArray[i]);
		}
		return arrayList;
	}
	private long[] getTrackIdsLongArray() {
		int arraySize = mTrackIds.size();
		if (arraySize <= 0) {
			return new long[0];
		}
		long[] longArray = new long[arraySize];
		for (int i = 0; i < arraySize; i++) {
			longArray[i] = mTrackIds.get(i).id;
		}
		return longArray;
	}
	private void addTrackIds(long val, boolean startLogging) {

		
		for (int i=0; i < mTrackIds.size(); i++) {
			if (mTrackIds.get(i).id == val) {
				mTrackIds.remove(i);
			}
		}
		int state = GPSLoggerServiceManager.getLoggingState();
		Common.log(TAG, "ADDTRACKIDS STATE IS " + state);
		long loggingTrackId = GPSLoggerServiceManager.isLogging();
		if (loggingTrackId != -1 && val != loggingTrackId) 
            mTrackIds.add(mTrackIds.size()-1, new Track(val));
		else
            mTrackIds.add(mTrackIds.size(), new Track(val));

/*
        if ((state == Constants.LOGGING || state == Constants.PAUSED) && !startLogging
        		&& mTrackIds.size() > 0) {        
        	Common.log(TAG, "ASLAI: Adding track while logging");
            mTrackIds.add(mTrackIds.size()-1, new Track(val));
        } else {
        	Common.log(TAG, "ASLAI: Adding track while not logging");

		  mTrackIds.add(new Track(val));
         }
         */
		  Common.log(TAG, "ASLAI1: " + getTrackIdsString());
        
	}
	private String getTrackIdsString() {
		String str = "Track ids are: ";

		for (Track track : mTrackIds) {
			str += "" + track.id + ", ";
		}
		return str;
	}
	private long getLastTrackId() {
		long trackId = -1;
		if ((trackId = GPSLoggerServiceManager.isLogging()) != -1) {
			Common.log(TAG, "LastTrackId: " + trackId);
			return trackId;
		}
		if (mTrackIds.size() == 0) {
			return trackId;
		}
		int i = 1;
		while (mTrackIds.size() - i >=0 && !mTrackIds.get(mTrackIds.size()-i).visible)
			i++;
		if (mTrackIds.size() -i >=0)
		   trackId = mTrackIds.get(mTrackIds.size()-i).id;
		else
			trackId = -1;
		Common.log(TAG, "LastTrackId: " + trackId);
			
		return trackId;
	}
	private String trackIdsToPreference() {
		
		if (mTrackIds == null || mTrackIds.size() == 0)
			return "";
		int size = mTrackIds.size();
		String str = "" + mTrackIds.get(0).id + "qwertyuioplkjh" + mTrackIds.get(0).visible;
		for (int i = 1; i < size; i++) {
			str += "zxcvbnmlkjh"+ mTrackIds.get(i).id+ "qwertyuioplkjh" + mTrackIds.get(i).visible;
		}
		return str;
	}
	
	private ArrayList<Track> preferenceToTrackIds(SharedPreferences sharedPreferences) {
//		return new ArrayList<Long>();
		try {
		String value = sharedPreferences.getString("mTrackIds", "");
		Common.log(TAG, "VALUE IS: " + value);
		if ("".equals(value)) {
			return new ArrayList<Track>();
		}
		ArrayList<Track> arrList = new ArrayList<Track>();
		String[] strArray = value.split("zxcvbnmlkjh");
		for (int i=0; i < strArray.length; i++) {
			String[] trackArray = strArray[i].split("qwertyuioplkjh");
			Common.log(TAG, "TRACKARRAY[0]: " + trackArray[0] + " TRACKARRAY[1]: " + trackArray[1]);
			arrList.add(new Track(new Long(trackArray[0]), new Boolean(trackArray[1])));
		}
		return arrList;
		} catch (Exception e)	{
			e.printStackTrace();
			Common.log(TAG, "ASLAI: EXCEPTION CONVERTING TO TRACKS");
			return new ArrayList<Track>();
		}
	}
	private final ContentObserver mTrackSegmentsObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfUpdate) {
			if (!selfUpdate) {
				Common.log(TAG, "mTrackSegmentsObserver " + getTrackIdsString());
				LoggerMap.this.updateDataOverlays();
			}
			else {
				Common.log(TAG, "mTrackSegmentsObserver skipping change on " + mLastSegment);
			}
		}
	};
	private final ContentObserver mSegmentWaypointsObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfUpdate) {
			if (!selfUpdate) {
				Common.log(TAG, "mSegmentWaypointsObserver " + mLastSegment);
				LoggerMap.this.createSpeedDisplayNumbers();
				if (mLastSegmentOverlay != null) {
					moveActiveViewWindow();
					mLastSegmentOverlay.calculateTrack();
					mMapView.postInvalidate();
				}
			}
			else {
				Common.log(TAG, "mSegmentWaypointsObserver skipping change on " + mLastSegment);
			}
		}
	};
	private final ContentObserver mTrackMediasObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfUpdate) {
			if (!selfUpdate) {
				Common.log(TAG, "mTrackMediasObserver " + getTrackIdsString());
				if (mLastSegmentOverlay != null) {
					mLastSegmentOverlay.calculateMedia();
					mMapView.postInvalidate();
				}
			}
			else {
				Common.log(TAG, "mTrackMediasObserver skipping change on " + mLastSegment);
			}
		}
	};
	private final View.OnClickListener mTrackNameDialogListener = new View.OnClickListener() {
		public void onClick(View view) {
			String trackName = mTrackNameView.getText().toString();
			Common.log(TAG, "mTrackNameDialogListener: " + trackName);
			
			if (trackName == null || trackName.trim().equals("")) {
				Toast toast = Toast.makeText(LoggerMap.this.getApplicationContext(), 
						"Track name cannot be empty", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			
			ContentValues values = new ContentValues();
			values.put(Tracks.NAME, trackName);
			values.put(Tracks.USER_ID, Common.getRegisteredUser(LoggerMap.this).id);
			Uri uri = ContentUris.withAppendedId(Tracks.CONTENT_URI, LoggerMap.this.getLastTrackId());
			getContentResolver().update(uri, values, null, null);
			getContentResolver().notifyChange(uri, null);
			updateTitleBar();
			mTrackNameDialogShowing = false;
			if (mStatsUpdating)
				mDialogUpdate = ProgressDialog.show(LoggerMap.this, "", 
						getString(R.string.dialog_updating_stats), true);
			else
				updateAchievements();
			
			/*
			// Create a sample location
			Location location = new Location(LocationManager.NETWORK_PROVIDER);
			location.setLatitude(30.267153);
			location.setLongitude(-97.743061);
			  
			// Create the note
			IntentIntegrator notesIntent = new IntentIntegrator(context);
			notesIntent.createNote("#sample", location);
			*/
			LoggerMap.this.dismissDialog(DIALOG_TRACKNAME);
		}
	};
	
//	private final OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
//		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//			int which = buttonView.getId();
//			switch (which) {
//			case R.id.layer_satellite:
//				setSatelliteOverlay(isChecked);
//				break;
//			case R.id.layer_traffic:
//				setTrafficOverlay(isChecked);
//				break;
//			case R.id.layer_speed:
//				setSpeedOverlay(isChecked);
//				break;
//			// TODO: remove unnecessary preferences
//			
//			 //case R.id.layer_compass: setCompassOverlay( isChecked ); break;
//			 //case R.id.layer_location: setLocationOverlay( isChecked ); break;
//			 
//			default:
//				break;
//			}
//		}
//	};
	private final OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			// TODO: Clear removed preferences
			/*
			 * if( key.equals( Constants.TRACKCOLORING ) ) { int
			 * trackColoringMethod = new Integer( sharedPreferences.getString(
			 * Constants.TRACKCOLORING, "3" ) ).intValue();
			 * updateSpeedbarVisibility(); List<Overlay> overlays =
			 * LoggerMap.this.mMapView.getOverlays(); for (Overlay overlay :
			 * overlays) { if( overlay instanceof SegmentOverlay ) { (
			 * (SegmentOverlay) overlay ).setTrackColoringMethod(
			 * trackColoringMethod, mAverageSpeed ); } } }
			 */
			// else if( key.equals( Constants.DISABLEBLANKING ) )
			// {
			// updateBlankingBehavior();
			// }
			if (key.equals(Constants.SPEED)) {
				updateSpeedDisplayVisibility();
			}
			// else if( key.equals( Constants.COMPASS ) )
			// {
			// updateCompassDisplayVisibility();
			// }
			else if (key.equals(Constants.TRAFFIC)) {
				LoggerMap.this.mMapView.setTraffic(sharedPreferences.getBoolean(key, false));
			}
			else if (key.equals(Constants.SATELLITE)) {
				LoggerMap.this.mMapView.setSatellite(sharedPreferences.getBoolean(key, false));
			}
			// else if( key.equals( Constants.LOCATION ) )
			// {
			// updateLocationDisplayVisibility();
			// }
		}
	};
	private final UnitsI18n.UnitsChangeListener mUnitsChangeListener = new UnitsI18n.UnitsChangeListener() {
		public void onUnitsChange() {
			createSpeedDisplayNumbers();
			updateSpeedbarVisibility();
		}
	};
	private final OnClickListener mNoteTextDialogListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			String noteText = mNoteTextView.getText().toString();
			Calendar c = Calendar.getInstance();
			String newName = String.format("Textnote_%tY-%tm-%td_%tH%tM%tS.txt", c, c, c, c, c, c);
			String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
			File file = new File(sdcard + Constants.EXTERNAL_DIR + newName);
			FileWriter filewriter = null;
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
				filewriter = new FileWriter(file);
				filewriter.append(noteText);
				filewriter.flush();
			}
			catch (IOException e) {
				Log.e(TAG, "Note storing failed", e);
				CharSequence text = e.getLocalizedMessage();
				Toast toast = Toast.makeText(LoggerMap.this.getApplicationContext(), text,
						Toast.LENGTH_LONG);
				toast.show();
			}
			finally {
				if (filewriter != null) {
					try {
						filewriter.close();
					}
					catch (IOException e) { /* */
					}
				}
			}

			GPSLoggerServiceManager.storeMediaUri(Uri.fromFile(file));
		}

	};
	private final OnClickListener mNoteNameDialogListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			String name = mNoteNameView.getText().toString();
			Uri media = Uri.withAppendedPath(Constants.NAME_URI, Uri.encode(name));
			GPSLoggerServiceManager.storeMediaUri(media);
		}

	};
	private SegmentOverlay mLastSegmentOverlay;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	protected void onCreate(Bundle load) {
		Common.log(TAG, "onCreate()");
		super.onCreate(load);
		this.startService(new Intent(Constants.SERVICENAME));
		/*
		if (load != null) {
			Common.log(TAG, "BUNDLE IS NOT NULL");
          mTrackIds = toTrackIdsLongArray(load.getLongArray("trackIds"));
		} else {
			Common.log(TAG, "BUNDLE IS NULL");
		  mTrackIds = new ArrayList<Long>();
		}
		*/
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mTrackIds = preferenceToTrackIds(mSharedPreferences);
		Common.log(TAG, "ASLAI: GETTRACKIDSSTRING" + getTrackIdsString());
		/*
		Object previousInstanceData = getLastNonConfigurationInstance();
		if (previousInstanceData != null && previousInstanceData instanceof GPSLoggerServiceManager) {
			GPSLoggerServiceManager = (GPSLoggerServiceManager) previousInstanceData;
			Common.log(TAG, "getting previous GPSLoggerServiceManager");
		}
		else {
			GPSLoggerServiceManager = new GPSLoggerServiceManager((Context) this);
			Common.log(TAG, "creating new GPSLoggerServiceManager");
		}
		*/
		GPSLoggerServiceManager.setContext(this);
		GPSLoggerServiceManager.startup();
		
		mDbHelper = new DatabaseHelper(this);
		
		mReceiver = new ScoreboardUpdateReceiver(); 
		mGameCon = new GamingServiceConnection(this, mReceiver, 
				Constants.APP_ID, Constants.APP_API_KEY, LoggerMap.class.toString());
		mGameCon.bind();
		User user = Common.getRegisteredUser(this);
		mGameCon.setUserId(user.id, user.fb_id, user.fb_token);
		
		mFacebook = new Facebook(Constants.FB_APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		mFacebook.setAccessToken(mSharedPreferences.getString(Constants.USERREG_TOKEN_KEY, null));
		
		mUnits = new UnitsI18n(this, mUnitsChangeListener);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPreferences
				.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

		setContentView(R.layout.map);
		mMapView = (MapView) findViewById(R.id.myMapView);
		mMylocation = new FixedMyLocationOverlay(this, mMapView);
		mMapController = this.mMapView.getController();
		
		//ASLAI: Added
		mMylocation.enableMyLocation();
		mMylocation.enableCompass();
		
		mMapView.setBuiltInZoomControls(true);
		mMapView.displayZoomControls(true);
		
		mMapView.setClickable(true);
		mMapView.setStreetView(false);
		mMapView.setSatellite(mSharedPreferences.getBoolean(Constants.SATELLITE, false));
		mMapView.setTraffic(mSharedPreferences.getBoolean(Constants.TRAFFIC, false));

		//ASLAI: Added
		mMapController.setZoom(ZOOM_LEVEL);
		
		mStartButton = (Button)findViewById(R.id.startbutton);
		mStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int state = GPSLoggerServiceManager.getLoggingState();
				switch (state) {
				case Constants.STOPPED:					
					Common.log(TAG, "mLoggingControlListener: start GPS logging...");
					long loggerTrackId = GPSLoggerServiceManager.startGPSLogging(null);
					moveToTrack(loggerTrackId, true, true);
					//showDialog(DIALOG_TRACKNAME);
					break;
				case Constants.LOGGING:
				case Constants.PAUSED:
					GPSLoggerServiceManager.stopGPSLogging();
					Common.log(TAG, "stopped GPS logging!!!!!!!!!!!!!!!!!!!!");
					if (calculateTrackStatistics()) {
						mTrackNameDialogShowing = true;
						showDialog(DIALOG_TRACKNAME);
						syncGroupStats();
					}
					else {
						Toast toast = Toast.makeText(LoggerMap.this.getApplicationContext(), 
								"Discarding track of zero length", Toast.LENGTH_SHORT);
						toast.show();
					}
					break;
				default:
					break;
				}
				updateTrackingButtons();
			}
		});
		
		mResumeButton = (Button)findViewById(R.id.resumebutton);
		mResumeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int state = GPSLoggerServiceManager.getLoggingState();
				switch (state) {
				case Constants.LOGGING:
					GPSLoggerServiceManager.pauseGPSLogging();
					break;
				case Constants.PAUSED:
					GPSLoggerServiceManager.resumeGPSLogging();
					break;
				default:
					break;
				}
				updateTrackingButtons();
			}
		});

		TextView[] speeds = { (TextView) findViewById(R.id.speedview02),
				(TextView) findViewById(R.id.speedview01),
				(TextView) findViewById(R.id.speedview00) };
		mSpeedtexts = speeds;
		mLastGPSSpeedView = (TextView) findViewById(R.id.currentSpeed);
		
		Bundle extras = getIntent().getExtras();
		mIsPartnerRun = extras != null ? extras.getBoolean(PARTNER_RUN_KEY) : false;
		
		onRestoreInstanceState(load);
		
		//ASLAI: Added
		Common.log(TAG, "ASLAI: Enabling my location");
		List<Overlay> overlays = this.mMapView.getOverlays();
		overlays.clear();

		updateTitleBar();
		updateDataOverlays();
		updateSpeedbarVisibility();	
		mMylocation.enableMyLocation();	
		mMylocation.enableCompass();
		overlays.add(mMylocation);
		mMapView.invalidate();
		Common.log(TAG, "ASLAI: Enabled my location");
	}

	protected void onPause() {
		super.onPause();
		Common.log(TAG, "onPause()");
		
		mDbHelper.close();
		
		if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
			this.mWakeLock.release();
			Log.w(TAG, "onPause(): Released lock to keep screen on!");
		}
		if (mTrackIds.size() > 0) {
			ContentResolver resolver = this.getApplicationContext().getContentResolver();
			resolver.unregisterContentObserver(this.mTrackSegmentsObserver);
			resolver.unregisterContentObserver(this.mSegmentWaypointsObserver);
			resolver.unregisterContentObserver(this.mTrackMediasObserver);
		}
//		mMylocation.disableMyLocation();
//		mMylocation.disableCompass();
	}

	protected void onResume() {
		super.onResume();
		Common.log(TAG, "onResume");
		mDbHelper.openAndGetDb();
		updateTitleBar();
		updateSpeedbarVisibility();
		updateSpeedDisplayVisibility();
		mMylocation.enableCompass();
		mMylocation.enableMyLocation();
		
		updateTrackingButtons();

		if (mTrackIds.size() >= 0) {
			ContentResolver resolver = this.getApplicationContext().getContentResolver();
			Uri trackUri = Uri.withAppendedPath(Tracks.CONTENT_URI, getLastTrackId() + "/segments");
			Uri lastSegmentUri = Uri.withAppendedPath(Tracks.CONTENT_URI, getLastTrackId() + "/segments/"
					+ mLastSegment + "/waypoints");
			Uri mediaUri = ContentUris.withAppendedId(Media.CONTENT_URI, getLastTrackId());

			resolver.unregisterContentObserver(this.mTrackSegmentsObserver);
			resolver.unregisterContentObserver(this.mSegmentWaypointsObserver);
			resolver.unregisterContentObserver(this.mTrackMediasObserver);
			resolver.registerContentObserver(trackUri, false, this.mTrackSegmentsObserver);
			resolver.registerContentObserver(lastSegmentUri, true, this.mSegmentWaypointsObserver);
			resolver.registerContentObserver(mediaUri, true, this.mTrackMediasObserver);

		}
		updateDataOverlays();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onDestroy() {
		Common.log(TAG, "onDestroy");
		if (mMylocation != null)
		mMylocation.disableMyLocation();		
		Editor editor = mSharedPreferences.edit();
		editor.putString("mTrackIds", trackIdsToPreference());
		editor.commit();		
		GPSLoggerServiceManager.shutdown();
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
			Log.w(TAG, "onDestroy(): Released lock to keep screen on!");
		}
		mSharedPreferences
				.unregisterOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
		if (GPSLoggerServiceManager.getLoggingState() == Constants.STOPPED) {
			stopService(new Intent(Constants.SERVICENAME));
		}
		
		mGameCon.unbind(); 
		super.onDestroy();
	}

	/**
	 * @param newIntent
	 *            an Intent containing the track to move the map to
	 * @see com.google.android.maps.MapActivity#onNewIntent(android.content.Intent)
	 */
	@Override
	public void onNewIntent(Intent newIntent) {
		Common.log(TAG, "ASLAI: ONNEWINTENT");
		Uri data = newIntent.getData();
		if (data != null) {
			moveToTrack(Long.parseLong(data.getLastPathSegment()), true, false);
		}
	}
 
	@Override
	protected void onRestoreInstanceState(Bundle load) {
		if (load != null) {
			Common.log(TAG, "Restoring the previous map ");
			super.onRestoreInstanceState(load);
		}
//ASLAI TODO
		Uri data = this.getIntent().getData();
		if (load != null && load.containsKey("track")) // 1st track from a
														// previous instance of
														// this activity
		{
			Common.log(TAG, "FIRST TRACK RESTORE");
			long loadTrackId = load.getLong("track");
			Common.log(TAG, "Moving to restored track " + loadTrackId);
			moveToTrack(loadTrackId, false, false);
		}
		else if (data != null) // 2nd track ordered to make
		{
			Common.log(TAG, "SECOND TRACK RESTORE");

			long loadTrackId = Long.parseLong(data.getLastPathSegment());
			Common.log(TAG, "Moving to intented track " + loadTrackId);
			moveToTrack(loadTrackId, true, false);
            setIntent(new Intent());

		}
		else {
			
			Common.log(TAG, "Moving to last track ");
//			moveToLastTrack(); // 3rd just try the last track
		}
		
		if (load != null && load.containsKey(PARTNER_RUN_KEY))
			mIsPartnerRun = load.getBoolean(PARTNER_RUN_KEY);

		if (load != null && load.containsKey("zoom")) {
			this.mMapController.setZoom(load.getInt("zoom"));
		}
		else {
			this.mMapController.setZoom(ZOOM_LEVEL);
		}

		if (load != null && load.containsKey("e6lat") && load.containsKey("e6long")) {
			GeoPoint storedPoint = new GeoPoint(load.getInt("e6lat"), load.getInt("e6long"));
			Common.log(TAG, "ASLAI: ANIMATING TO STORED POINT");
			this.mMapView.getController().animateTo(storedPoint);
		}
		else {
			GeoPoint lastPoint = getLastTrackPoint();
			animateTo();
//			this.mMapView.getController().animateTo(lastPoint);
		}
		redrawOverlays();
		Common.log(TAG, "onRestoreInstanceState(): mIsPartnerRun = " + mIsPartnerRun);
	}

	@Override
	protected void onSaveInstanceState(Bundle save) {
		super.onSaveInstanceState(save);
		Common.log(TAG, "ONSAVEINSTANCESTATE: " + getTrackIdsString());
		save.putLongArray("trackIds", this.getTrackIdsLongArray());
//		save.putLong("track", this.mTrackId);
		save.putInt("zoom", this.mMapView.getZoomLevel());
		GeoPoint point = this.mMapView.getMapCenter();
		save.putInt("e6lat", point.getLatitudeE6());
		save.putInt("e6long", point.getLongitudeE6());
		save.putBoolean(PARTNER_RUN_KEY, mIsPartnerRun);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	/*
	@Override
	public Object onRetainNonConfigurationInstance() {
		Object nonConfigurationInstance = GPSLoggerServiceManager;
		return nonConfigurationInstance;
	}
*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean propagate = true;
		switch (keyCode) {
		case KeyEvent.KEYCODE_T:
			propagate = this.mMapView.getController().zoomIn();
			break;
		case KeyEvent.KEYCODE_G:
			propagate = this.mMapView.getController().zoomOut();
			break;
		case KeyEvent.KEYCODE_S:
			setSatelliteOverlay(!this.mMapView.isSatellite());
			propagate = false;
			break;
		case KeyEvent.KEYCODE_A:
			setTrafficOverlay(!this.mMapView.isTraffic());
			propagate = false;
			break;
			/*
		case KeyEvent.KEYCODE_F:
			moveToTrack(this.mTrackId - 1, true);
			propagate = false;
			break;
		case KeyEvent.KEYCODE_H:
			moveToTrack(this.mTrackId + 1, true);
			propagate = false;
			break;
			*/
		default:
			propagate = super.onKeyDown(keyCode, event);
			break;
		}
		return propagate;
	}
	
	private void setTrafficOverlay(boolean b) {
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(Constants.TRAFFIC, b);
		editor.commit();
	}

	private void setSatelliteOverlay(boolean b) {
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(Constants.SATELLITE, b);
		editor.commit();
	}

	private void setSpeedOverlay(boolean b) {
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(Constants.SPEED, b);
		editor.commit();
	}

	// TODO: remove unnecessary preferences
	/*
	 * private void setCompassOverlay( boolean b ) { Editor editor =
	 * mSharedPreferences.edit(); editor.putBoolean( Constants.COMPASS, b );
	 * editor.commit(); }
	 * 
	 * private void setLocationOverlay( boolean b ) { Editor editor =
	 * mSharedPreferences.edit(); editor.putBoolean( Constants.LOCATION, b );
	 * editor.commit(); }
	 */
	/**
	 * Adds items into the main menu (map screen)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		Common.log(TAG, "onCreateOptionsMenu()");
		//menu.add(ContextMenu.NONE, MENU_TRACKING, ContextMenu.NONE, R.string.menu_tracking)
		//		.setIcon(R.drawable.ic_menu_movie).setAlphabeticShortcut('T');
		menu.add(ContextMenu.NONE, MENU_LAYERS, ContextMenu.NONE, R.string.menu_showLayers)
				.setIcon(R.drawable.ic_menu_mapmode).setAlphabeticShortcut('L');
		// SubMenu notemenu = menu.addSubMenu( ContextMenu.NONE, MENU_NOTE,
		// ContextMenu.NONE, R.string.menu_insertnote ).setIcon(
		// R.drawable.ic_menu_myplaces );
//		menu.add(ContextMenu.NONE, MENU_CLEARTRACK, ContextMenu.NONE, R.string.menu_clear_track)
//		.setIcon(R.drawable.ic_menu_close_clear_cancel).setAlphabeticShortcut('C');
		menu.add(ContextMenu.NONE, MENU_STATS, ContextMenu.NONE, R.string.menu_statistics).setIcon(
				R.drawable.ic_menu_picture).setAlphabeticShortcut('S');
		menu.add(ContextMenu.NONE, MENU_SHARE, ContextMenu.NONE, R.string.menu_shareTrack).setIcon(
				R.drawable.ic_menu_share).setAlphabeticShortcut('I');
		menu.add(ContextMenu.NONE, MENU_CURRENT_LOCATION, ContextMenu.NONE, R.string.menu_current_location).setIcon(
				R.drawable.ic_menu_myplaces).setAlphabeticShortcut('C');
		// More

		// menu.add( ContextMenu.NONE, MENU_TRACKLIST, ContextMenu.NONE,
		// R.string.menu_tracklist ).setIcon( R.drawable.ic_menu_show_list
		// ).setAlphabeticShortcut( 'P' );
		//menu.add(ContextMenu.NONE, MENU_SETTINGS, ContextMenu.NONE, R.string.menu_settings)
		//		.setIcon(R.drawable.ic_menu_preferences).setAlphabeticShortcut('C');
		// menu.add( ContextMenu.NONE, MENU_ABOUT, ContextMenu.NONE,
		// R.string.menu_about ).setIcon( R.drawable.ic_menu_info_details
		// ).setAlphabeticShortcut( 'A' );
		/*
		 * notemenu.add( ContextMenu.NONE, MENU_NAME, ContextMenu.NONE,
		 * R.string.menu_notename ); notemenu.add( ContextMenu.NONE, MENU_TEXT,
		 * ContextMenu.NONE, R.string.menu_notetext ); notemenu.add(
		 * ContextMenu.NONE, MENU_VOICE, ContextMenu.NONE,
		 * R.string.menu_notespeech ); notemenu.add( ContextMenu.NONE,
		 * MENU_PICTURE, ContextMenu.NONE, R.string.menu_notepicture );
		 * notemenu.add( ContextMenu.NONE, MENU_VIDEO, ContextMenu.NONE,
		 * R.string.menu_notevideo );
		 */
		return result;
	}

	/**
	 * Enables or disables the "Make note" and "Share track" items when
	 * appropriate
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// MenuItem notemenu = menu.findItem( MENU_NOTE );
		// notemenu.setEnabled( GPSLoggerServiceManager.isMediaPrepared() );
		long loggingTrackId = GPSLoggerServiceManager.isLogging();		
		
		long remoteTrackId = 0;
		Intent actionIntent = null;
		ContentResolver resolver = this.getApplicationContext().getContentResolver();
		Uri trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, this.getLastTrackId());
		Cursor trackCursor = null;			
		try {
			trackCursor = resolver.query(trackUri, new String[] { Tracks.NAME, Tracks.TRACK_ID }, null, null, null);
			if (trackCursor != null && trackCursor.moveToLast()) {
				remoteTrackId = trackCursor.getLong(1);
			}	
		}	finally {
				if (trackCursor != null) {
					trackCursor.close();
				}
			}	
		
		MenuItem sharemenu = menu.findItem(MENU_SHARE);
		sharemenu.setEnabled(mTrackIds.size() > 0 && loggingTrackId == -1 && remoteTrackId == 0);
		
		MenuItem layermenu = menu.findItem(MENU_LAYERS);
		layermenu.setEnabled(mTrackIds.size() > 1 || (mTrackIds.size() == 1 && loggingTrackId == -1));	
		MenuItem statsmenu = menu.findItem(MENU_STATS);
		statsmenu.setEnabled(mTrackIds.size() > 0);				
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Big switch statement for handling menu selection
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;

		Uri trackUri;
		switch (item.getItemId()) {
		case MENU_LAYERS:
//			showDialog(DIALOG_LAYERS);
			Dialog dialog = null;
			LayoutInflater factory = null;
			View view = null;
			Builder builder = null;			
			builder = new AlertDialog.Builder(this);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.layerdialog, null);

			builder.setTitle(R.string.dialog_layer_title).setIcon(android.R.drawable.ic_dialog_map)
					.setPositiveButton(R.string.btn_okay, null).setView(view);
			Common.log(TAG, "" + Track.tracksToCharSequence(mTrackIds, this) + "===" + Track.tracksToVisibleBoolArray(mTrackIds));
			int state = GPSLoggerServiceManager.getLoggingState();
	        if (state == Constants.LOGGING || state == Constants.PAUSED) {  
	        	Common.log(TAG, "ASLAI: COPYING WHEN WE ARE TRACKING");
				mTrackIdsForDialog = Track.shadowCopy(mTrackIds, true);
	        } else {
	        	Common.log(TAG, "ASLAI: COPYING WHEN WE ARE NOT TRACKING");

				mTrackIdsForDialog = Track.shadowCopy(mTrackIds, false);
	        }
			if (mTrackIdsForDialog.size() > 0) {
			builder.setMultiChoiceItems(Track.tracksToCharSequence(mTrackIdsForDialog, this), Track.tracksToVisibleBoolArray(mTrackIdsForDialog), new OnMultiChoiceClickListener() {
				
				public void onClick(DialogInterface dialog, int whichButton,
                        boolean isChecked) {
					Track track = mTrackIdsForDialog.get(whichButton);
					if (isChecked) {
						Common.log(TAG, "SETTING TRACK VISIBLE FOR POSITION: " + whichButton);
						track.visible = true;
						LoggerMap.this.moveToTrack(track.id, true, false);
					} else {
						Common.log(TAG, "SETTING TRACK INVISIBLE FOR POSITION: " + whichButton);
						LoggerMap.this.setTrackInvisible(Track.findTrackPosById(mTrackIds, track.id));
					}
						
					
				}
			}).setNegativeButton(R.string.btn_remove_all, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					LoggerMap.this.clearOverlays();
					dialog.cancel();
				}
			}).setNeutralButton(R.string.btn_clear_all, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					LoggerMap.this.setAllTracksInvisible(mTrackIdsForDialog);
				}
			});
			}

			dialog = builder.create();
			dialog.show();
			
			handled = true;
			break;
		//case MENU_SETTINGS:
		//	Intent i = new Intent(this, SettingsDialog.class);
		//	startActivity(i);
		//	handled = true;
		//	break;
		case MENU_CLEARTRACK: 
			clearOverlays();
			handled = true;
			break;			
		case MENU_TRACKLIST:
			Intent tracklistIntent = new Intent(this, TrackList.class);
			tracklistIntent.putExtra(Tracks._ID, this.getLastTrackId());
			startActivityForResult(tracklistIntent, MENU_TRACKLIST);
			break;
		case MENU_STATS:
			if (this.mTrackIds.size() > 0) {
				Intent actionIntent = new Intent(this, Statistics.class);
				trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, this.getLastTrackId());
				actionIntent.setData(trackUri);
				startActivity(actionIntent);
				handled = true;
				break;
			}
			handled = true;
			break;
		case MENU_SHARE:
			// Don't really need to check whether the remoteTrackId exists, 
			// as we now checking it in onCreateMenu
			long remoteTrackId = 0;
			Intent actionIntent = null;
			ContentResolver resolver = this.getApplicationContext().getContentResolver();
			trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, this.getLastTrackId());
			Cursor trackCursor = null;			
			String trackName = this.getTitle().toString();
			try {
				trackCursor = resolver.query(trackUri, new String[] { Tracks.NAME, Tracks.TRACK_ID }, null, null, null);
				if (trackCursor != null && trackCursor.moveToLast()) {
					trackName = trackCursor.getString(0);
					remoteTrackId = trackCursor.getLong(1);
					this.setTitle(trackName);
				}	
			}	finally {
					if (trackCursor != null) {
						trackCursor.close();
					}
				}	
			if (remoteTrackId == 0)
				actionIntent = new Intent("android.intent.action.PUBLISH");
			else
				actionIntent = new Intent(Intent.ACTION_RUN);			
//			Intent actionIntent = new Intent(Intent.ACTION_RUN);
			actionIntent.setDataAndType(trackUri, Tracks.CONTENT_ITEM_TYPE);
			actionIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			actionIntent.putExtra("name", trackName);
			Common.log(TAG, "MENU_TRACKLIST " + this.getTitle());
			startActivity(Intent.createChooser(actionIntent, getString(R.string.chooser_title)));

			handled = true;			
			break;
		case MENU_CURRENT_LOCATION:
			if (mMylocation != null && mMylocation.getMyLocation() != null)
				mMapView.getController().animateTo(mMylocation.getMyLocation());
			else 
				Toast.makeText(this, R.string.msg_my_location_not_available, Toast.LENGTH_SHORT).show();
			handled = true;
			break;
		case MENU_PICTURE:
			addPicture();
			handled = true;
			break;
		case MENU_VIDEO:
			addVideo();
			handled = true;
			break;
		case MENU_VOICE:
			addVoice();
			handled = true;
			break;
		case MENU_TEXT:
			showDialog(DIALOG_TEXT);
			handled = true;
			break;
		case MENU_NAME:
			showDialog(DIALOG_NAME);
			handled = true;
			break;
		default:
			handled = super.onOptionsItemSelected(item);
			break;
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
		LayoutInflater factory = null;
		View view = null;
		Builder builder = null;
		switch (id) {
		case DIALOG_TRACKNAME:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.namedialog);
			dialog.setTitle(R.string.dialog_routename_title);
			dialog.setCancelable(false);
			mTrackNameView = (EditText)dialog.findViewById(R.id.nameField);
			Button okButton = (Button)dialog.findViewById(R.id.ok_button);
			okButton.setVisibility(View.VISIBLE);
			okButton.setOnClickListener(mTrackNameDialogListener);
			return dialog;
			/*
		case DIALOG_LAYERS:
			
			builder = new AlertDialog.Builder(this);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.layerdialog, null);

			mSatellite = (CheckBox) view.findViewById(R.id.layer_satellite);
			mSatellite.setOnCheckedChangeListener(mCheckedChangeListener);
			mTraffic = (CheckBox) view.findViewById(R.id.layer_traffic);
			mTraffic.setOnCheckedChangeListener(mCheckedChangeListener);
			mSpeed = (CheckBox) view.findViewById(R.id.layer_speed);
			mSpeed.setOnCheckedChangeListener(mCheckedChangeListener);

			// TODO: remove unnecessary preferences
			// mCompass = (CheckBox) view.findViewById( R.id.layer_compass );
			// mCompass.setOnCheckedChangeListener( mCheckedChangeListener );
			// mLocation = (CheckBox) view.findViewById( R.id.layer_location );
			// mLocation.setOnCheckedChangeListener( mCheckedChangeListener );
			builder.setTitle(R.string.dialog_layer_title).setIcon(android.R.drawable.ic_dialog_map)
					.setPositiveButton(R.string.btn_okay, null).setView(view);
			Common.log(TAG, "" + Track.tracksToCharSequence(mTrackIds, this) + "===" + Track.tracksToVisibleBoolArray(mTrackIds));
			if (mTrackIds.size() > 0) {
			builder.setMultiChoiceItems(Track.tracksToCharSequence(mTrackIds, this), Track.tracksToVisibleBoolArray(mTrackIds), new OnMultiChoiceClickListener() {
				
				public void onClick(DialogInterface dialog, int whichButton,
                        boolean isChecked) {
					Track track = mTrackIds.get(whichButton);
					if (isChecked) {
						Common.log(TAG, "SETTING TRACK VISIBLE FOR POSITION: " + whichButton);
						track.visible = true;
						LoggerMap.this.moveToTrack(track.id, true, false);
					} else {
						Common.log(TAG, "SETTING TRACK INVISIBLE FOR POSITION: " + whichButton);
						LoggerMap.this.setTrackInvisible(whichButton);
					}
						
					
				}
			});
			}

			dialog = builder.create();
			*/
			/*
			LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layer);
			View checkboxView = factory.inflate(R.layout.checkbox_item, null);
			linearLayout.addView(checkboxView);
			checkboxView = factory.inflate(R.layout.checkbox_item, null);
			linearLayout.addView(checkboxView);
			*/
			/*

			//			CheckBox checkBox = (CheckBox) findViewById(R.layout.checkbox_item);
//			dialog.addContentView(checkboxView, mSatellite.getLayoutParams());
//			dialog.addContentView(checkBox, checkBox.getLayoutParams());
	
			return dialog;
			*/
		case DIALOG_TEXT:
			builder = new AlertDialog.Builder(this);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.notetextdialog, null);
			mNoteTextView = (EditText) view.findViewById(R.id.notetext);
			builder.setTitle(R.string.dialog_notetexttitle).setMessage(
					R.string.dialog_notetext_message).setIcon(android.R.drawable.ic_dialog_map)
					.setPositiveButton(R.string.btn_okay, mNoteTextDialogListener)
					.setNegativeButton(R.string.btn_cancel, null).setView(view);
			dialog = builder.create();
			return dialog;
		case DIALOG_NAME:
			builder = new AlertDialog.Builder(this);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.notenamedialog, null);
			mNoteNameView = (EditText) view.findViewById(R.id.notename);
			builder.setTitle(R.string.dialog_notenametitle).setMessage(
					R.string.dialog_notename_message).setIcon(android.R.drawable.ic_dialog_map)
					.setPositiveButton(R.string.btn_okay, mNoteNameDialogListener)
					.setNegativeButton(R.string.btn_cancel, null).setView(view);
			dialog = builder.create();
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
		
		/*
		switch (id) {
		case DIALOG_LAYERS:
			mSatellite.setChecked(mSharedPreferences.getBoolean(Constants.SATELLITE, false));
			mTraffic.setChecked(mSharedPreferences.getBoolean(Constants.TRAFFIC, false));
			mSpeed.setChecked(mSharedPreferences.getBoolean(Constants.SPEED, false));
			break;
		default:
			break;
		}
		*/
		super.onPrepareDialog(id, dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode != RESULT_CANCELED) {
			String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
			File file;
			Uri uri;
			File newFile;
			String newName;
			Uri fileUri;
			android.net.Uri.Builder builder;
			switch (requestCode) {
			case MENU_TRACKLIST:
				Uri trackUri = intent.getData();
				long trackId = Long.parseLong(trackUri.getLastPathSegment());
				moveToTrack(trackId, true, false);
				break;
			case MENU_ABOUT:
				break;
			case MENU_PICTURE:
				file = new File(sdcard + Constants.TMPICTUREFILE_PATH);
				Calendar c = Calendar.getInstance();
				newName = String.format("Picture_%tY-%tm-%td_%tH%tM%tS.jpg", c, c, c, c, c, c);
				newFile = new File(sdcard + Constants.EXTERNAL_DIR + newName);
				file.getParentFile().mkdirs();
				file.renameTo(newFile);

				Bitmap bm = BitmapFactory.decodeFile(newFile.getAbsolutePath());
				String height = Integer.toString(bm.getHeight());
				String width = Integer.toString(bm.getWidth());
				bm.recycle();
				bm = null;
				builder = new Uri.Builder();
				fileUri = builder.scheme("file").appendEncodedPath("/").appendEncodedPath(
						newFile.getAbsolutePath()).appendQueryParameter("width", width)
						.appendQueryParameter("height", height).build();
				GPSLoggerServiceManager.storeMediaUri(fileUri);
				mLastSegmentOverlay.calculateMedia();
				mMapView.postInvalidate();
				break;
			case MENU_VIDEO:
				file = new File(sdcard + Constants.TMPICTUREFILE_PATH);
				c = Calendar.getInstance();
				newName = String.format("Video_%tY%tm%td_%tH%tM%tS.3gp", c, c, c, c, c, c);
				newFile = new File(sdcard + Constants.EXTERNAL_DIR + newName);
				file.getParentFile().mkdirs();
				file.renameTo(newFile);
				builder = new Uri.Builder();
				fileUri = builder.scheme("file").appendPath(newFile.getAbsolutePath()).build();
				GPSLoggerServiceManager.storeMediaUri(fileUri);
				mLastSegmentOverlay.calculateMedia();
				mMapView.postInvalidate();
				break;
			case MENU_VOICE:
				uri = Uri.parse(intent.getDataString());
				GPSLoggerServiceManager.storeMediaUri(uri);
				mLastSegmentOverlay.calculateMedia();
				mMapView.postInvalidate();
				break;
			default:
				Log.e(TAG, "Returned form unknow activity: " + requestCode);
				break;
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}
	
	// OnDoubleTapListener methods
	
	public boolean onDoubleTap(MotionEvent e) {
		Common.log(TAG, "onDoubleTap()");
		return true;
	}
	
	public boolean onDoubleTapEvent(MotionEvent e) {
		return true;
	}
	
	public boolean onSingleTapConfirmed (MotionEvent e) {
		return true;
	}
	
	private void updateTrackingButtons() {
		int state = GPSLoggerServiceManager.getLoggingState();
		switch (state) {
		case Constants.STOPPED:
			mStartButton.setText(R.string.map_start);
			mStartButton.setBackgroundResource(R.drawable.custom_btn_green);
			mResumeButton.setVisibility(View.GONE);
			break;
		case Constants.LOGGING:
			mStartButton.setText(R.string.map_stop);
			mStartButton.setBackgroundResource(R.drawable.custom_btn_red);
			mResumeButton.setVisibility(View.VISIBLE);
			mResumeButton.setText(R.string.map_pause);
			break;
		case Constants.PAUSED:
			mStartButton.setText(R.string.map_stop);
			mStartButton.setBackgroundResource(R.drawable.custom_btn_red);
			mResumeButton.setVisibility(View.VISIBLE);
			mResumeButton.setText(R.string.map_resume);
			break;
		default:
			break;
		}
	}

	private void updateTitleBar() {
		if (mTrackIds.size() == 0 || !Track.visibleExists(mTrackIds)) {
			this.setTitle(R.string.map_title);
			return;
		}
		ContentResolver resolver = this.getApplicationContext().getContentResolver();
		Cursor trackCursor = null;
		try {
			trackCursor = resolver.query(ContentUris.withAppendedId(Tracks.CONTENT_URI,
					this.getLastTrackId()), new String[] { Tracks.NAME }, null, null, null);
			if (trackCursor != null && trackCursor.moveToLast()) {
				String trackName = trackCursor.getString(0);
				this.setTitle(trackName);
			}
		}
		finally {
			if (trackCursor != null) {
				trackCursor.close();
			}
		}
	}

	private void updateSpeedbarVisibility() {
		View speedbar = findViewById(R.id.speedbar);
		drawSpeedTexts(mAverageSpeed);
		speedbar.setVisibility(View.VISIBLE);
		for (int i = 0; i < mSpeedtexts.length; i++)
			mSpeedtexts[i].setVisibility(View.VISIBLE);
	}

	private void updateSpeedDisplayVisibility() {
		boolean showspeed = mSharedPreferences.getBoolean(Constants.SPEED, false);
		if (showspeed) {
			mLastGPSSpeedView.setVisibility(View.VISIBLE);
			mLastGPSSpeedView.setText("");
		}
		else {
			mLastGPSSpeedView.setVisibility(View.INVISIBLE);
		}
	}

	/*
	 * private void updateCompassDisplayVisibility() { boolean compass =
	 * mSharedPreferences.getBoolean( Constants.COMPASS, false ); if( compass )
	 * { mMylocation.enableCompass(); } else { mMylocation.disableCompass(); } }
	 * 
	 * private void updateLocationDisplayVisibility() { boolean location =
	 * mSharedPreferences.getBoolean( Constants.LOCATION, false ); if( location
	 * ) { mMylocation.enableMyLocation(); } else {
	 * mMylocation.disableMyLocation(); } }
	 */
	protected void createSpeedDisplayNumbers() {
		ContentResolver resolver = this.getApplicationContext().getContentResolver();
		Cursor waypointsCursor = null;
		try {
			Uri lastSegmentUri = Uri.withAppendedPath(Tracks.CONTENT_URI, this.getTaskId()
					+ "/segments/" + mLastSegment + "/waypoints");
			waypointsCursor = resolver.query(lastSegmentUri, new String[] { Waypoints.SPEED },
					null, null, null);
			if (waypointsCursor != null && waypointsCursor.moveToLast()) {
				double speed = waypointsCursor.getDouble(0);
				speed = mUnits.conversionFromMetersPerSecond(speed);
				String speedText = String.format("%.0f %s", speed, mUnits.getSpeedUnit());
				mLastGPSSpeedView.setText(speedText);
			}
		}
		finally {
			if (waypointsCursor != null) {
				waypointsCursor.close();
			}
		}
	}

	/**
	 * For the current track identifier the route of that track is drawn by
	 * adding a OverLay for each segments in the track
	 * 
	 * @param trackId
	 * @see SegmentOverlay
	 */
	private void createDataOverlays() {
		Common.log(TAG, "CREATEDATAOVERLAYS");
		mLastSegmentOverlay = null;
		List<Overlay> overlays = this.mMapView.getOverlays();
		//ASLAI HERE
		Long lastTrackId = getLastTrackId();

		overlays.clear(); 
		overlays.add(mMylocation);

		ContentResolver resolver = this.getApplicationContext().getContentResolver();
		Cursor segments = null;
		int trackColoringMethod = SegmentOverlay.DRAW_MEASURED;// new Integer(
		// mSharedPreferences.getString(
		// Constants.TRACKCOLORING,
		// "2" )
		// ).intValue();
		for (Track track: mTrackIds) {
			if (track.visible) {
				long trackId = track.id;
				Common.log(TAG, "CREATEDATAOVERLAYS FOR TRACK ID: " + trackId);
				try {

					Uri segmentsUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments");
					segments = resolver.query(segmentsUri, new String[] { Segments._ID }, null, null, null);
					if (segments != null && segments.moveToFirst()) {
						do {
							long segmentsId = segments.getLong(0);
							Uri segmentUri = ContentUris.withAppendedId(segmentsUri, segmentsId);
							SegmentOverlay segmentOverlay = new SegmentOverlay((Context) this, segmentUri,
									trackColoringMethod, mAverageSpeed, this.mMapView);
							overlays.add(segmentOverlay);
							mLastSegmentOverlay = segmentOverlay;
							if (segments.isFirst()) {
								segmentOverlay.addPlacement(SegmentOverlay.FIRST_SEGMENT);
							}
							if (segments.isLast()) {
								segmentOverlay.addPlacement(SegmentOverlay.LAST_SEGMENT);
								getLastTrackPoint();
							}
							if (trackId == lastTrackId) 
								mLastSegment = segmentsId;
						} while (segments.moveToNext());
					}

				}
				finally {
					if (segments != null) {
						segments.close();
					}
				}

				moveActiveViewWindow();
			}

		}

		Uri lastSegmentUri = Uri.withAppendedPath(Tracks.CONTENT_URI, lastTrackId + "/segments/"
				+ mLastSegment + "/waypoints");
		resolver.unregisterContentObserver(this.mSegmentWaypointsObserver);
		resolver.registerContentObserver(lastSegmentUri, false, this.mSegmentWaypointsObserver);
		Common.log(TAG, "LAST SEGMENT URI IS " + lastSegmentUri);

	}
/*
	private void createDataOverlays(long trackId) {
		Common.log(TAG, "CREATEDATAOVERLAYS");
		mLastSegmentOverlay = null;
		List<Overlay> overlays = this.mMapView.getOverlays();
		//ASLAI HERE
		Long lastTrackId = getLastTrackId();
		
		overlays.clear(); 
		overlays.add(mMylocation);

		ContentResolver resolver = this.getApplicationContext().getContentResolver();
		Cursor segments = null;
		int trackColoringMethod = SegmentOverlay.DRAW_MEASURED;// new Integer(
																// mSharedPreferences.getString(
																// Constants.TRACKCOLORING,
																// "2" )
																// ).intValue();
			Common.log(TAG, "CREATEDATAOVERLAYS FOR TRACK ID: " + trackId);
		try {

			Uri segmentsUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments");
			segments = resolver.query(segmentsUri, new String[] { Segments._ID }, null, null, null);
			if (segments != null && segments.moveToFirst()) {
				do {
					long segmentsId = segments.getLong(0);
					Uri segmentUri = ContentUris.withAppendedId(segmentsUri, segmentsId);
					SegmentOverlay segmentOverlay = new SegmentOverlay((Context) this, segmentUri,
							trackColoringMethod, mAverageSpeed, this.mMapView);
					overlays.add(segmentOverlay);
					mLastSegmentOverlay = segmentOverlay;
					if (segments.isFirst()) {
						segmentOverlay.addPlacement(SegmentOverlay.FIRST_SEGMENT);
					}
					if (segments.isLast()) {
						segmentOverlay.addPlacement(SegmentOverlay.LAST_SEGMENT);
						getLastTrackPoint();
					}
					if (trackId == lastTrackId) 
					    mLastSegment = segmentsId;
				} while (segments.moveToNext());
			}
			
		}
		finally {
			if (segments != null) {
				segments.close();
			}
		}

		moveActiveViewWindow();

		

		Uri lastSegmentUri = Uri.withAppendedPath(Tracks.CONTENT_URI, lastTrackId + "/segments/"
				+ mLastSegment + "/waypoints");
		resolver.unregisterContentObserver(this.mSegmentWaypointsObserver);
		resolver.registerContentObserver(lastSegmentUri, false, this.mSegmentWaypointsObserver);
		Common.log(TAG, "LAST SEGMENT URI IS " + lastSegmentUri);

	}
	
	*/
	private void updateDataOverlays() {
		boolean createOverlayExecuted = false;
		ContentResolver resolver = this.getApplicationContext().getContentResolver();
		for (Track track: mTrackIds) {
			if (track.visible) {
				long trackId = track.id;
				Common.log(TAG, "UPDATEDATAOVERLAYS FOR TRACK ID: " + trackId);

				Uri segmentsUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments");
				Cursor segmentsCursor = null;
				List<Overlay> overlays = this.mMapView.getOverlays();
				int segmentOverlaysCount = 0;

				for (Overlay overlay : overlays) {
					if (overlay instanceof SegmentOverlay) {
						segmentOverlaysCount++;
					}
				}
				try {
					segmentsCursor = resolver.query(segmentsUri, new String[] { Segments._ID }, null, null,
							null);
					if (segmentsCursor != null && segmentsCursor.getCount() == segmentOverlaysCount) {
						Common.log(TAG, "UPDATEDATAOVERLAYS SAME SEGMENT COUNT");
						// Common.log( TAG, "Alignment of segments" );
					}
					else {
						Common.log(TAG, "CREATEDATAOVERLAYS FROM UPDATEOVERLAYS");
						createDataOverlays();
						break;
					}
				}
				finally {
					if (segmentsCursor != null) {
						segmentsCursor.close();
					}
				}
				moveActiveViewWindow();
			}
		}
	}

	private void moveActiveViewWindow() {
		GeoPoint lastPoint = getLastTrackPoint();
		if (lastPoint != null && GPSLoggerServiceManager.getLoggingState() == Constants.LOGGING) {
			Point out = new Point();
			this.mMapView.getProjection().toPixels(lastPoint, out);
			int height = this.mMapView.getHeight();
			int width = this.mMapView.getWidth();
			if (out.x < 0 || out.y < 0 || out.y > height || out.x > width) {

				this.mMapView.clearAnimation();
				this.mMapView.getController().setCenter(lastPoint);
				// Common.log( TAG, "mMapView.setCenter()" );
			}
			else if (out.x < width / 4 || out.y < height / 4 || out.x > (width / 4) * 3
					|| out.y > (height / 4) * 3) {
				this.mMapView.clearAnimation();
				animateTo();
//				this.mMapView.getController().animateTo(lastPoint);
				// Common.log( TAG, "mMapView.animateTo()" );
			}
		}
	}

	/**
	 * @param avgSpeed
	 *            avgSpeed in m/s
	 */
	private void drawSpeedTexts(double avgSpeed) {
		avgSpeed = mUnits.conversionFromMetersPerSecond(avgSpeed);
		Common.log(TAG, "drawSpeedTexts(): avgSpeed = " + avgSpeed);
		
		mSpeedtexts[0].setVisibility(View.VISIBLE);
		mSpeedtexts[0].setText(String.format("%.0f %s", 0d, mUnits.getSpeedUnit()));
		mSpeedtexts[1].setVisibility(View.VISIBLE);
		mSpeedtexts[1].setText(String.format("%.0f %s", avgSpeed, mUnits.getSpeedUnit()));
		mSpeedtexts[2].setVisibility(View.VISIBLE);
		mSpeedtexts[2].setText(String.format("%.0f %s", avgSpeed*2, mUnits.getSpeedUnit()));
		/*
		for (int i = 0; i < mSpeedtexts.length; i++) {
			mSpeedtexts[i].setVisibility(View.VISIBLE);
			double speed = ((avgSpeed * 2d) / 5d) * i;
			String speedText = String.format("%.0f %s", speed, mUnits.getSpeedUnit());
			mSpeedtexts[i].setText(speedText);
		}*/
	}
    private void redrawOverlays() {
    	//ASLAI DIAGNOSTICS TILL HERE
    	Common.log(TAG, "ASLAI: REDRAW OVERLAYS");
		mMapView.getOverlays().clear();
		List<Overlay> overlays = this.mMapView.getOverlays();
		overlays.add(mMylocation);
		//ASLAI REMOVED HERE
		//ASLAI DIAGNOSTIC PROBLEM SHOULD BE HERE
		
//		GeoPoint lastPoint = getLastTrackPoint();
//	    mMapView.getController().animateTo(lastPoint);
		
		if (mTrackIds.size() > 0) {
			//ASLAI DIAGNOSTICS CHANGED HERE, There's still another place doing animate
			ArrayList<Track> tracksTmp = Track.shadowCopy(mTrackIds, false);
			for (Track track: tracksTmp) {
//				if (mTrackIds.get(mTrackIds.size() -1).visible)
				if (track.visible)
					moveToTrack(getLastTrackId(), false, false);
			//            moveToTrack(getLastTrackId(), true, true);
			}

		}    	
    }
// ASLAI
	private void clearOverlays() {	
		int state = GPSLoggerServiceManager.getLoggingState();
		Common.log("TAG", "ASLAI STATE IS: " + state);
		ArrayList<Track> tmpTrackIds = new ArrayList<Track>();
        if (state == Constants.LOGGING || state == Constants.PAUSED) {        	
            tmpTrackIds.add(mTrackIds.get(mTrackIds.size()-1));
        }
        mTrackIds = tmpTrackIds;
        Common.log(TAG, "ASLAI: TRACKIDSTOPREFERENCE IS: " + trackIdsToPreference());
		Editor editor = mSharedPreferences.edit();
		editor.putString("mTrackIds", trackIdsToPreference());
		editor.commit();        
		List<Overlay> overlays = this.mMapView.getOverlays();
		overlays.clear(); 
		redrawOverlays();
		overlays.add(mMylocation);
		updateTitleBar();
		mMapView.invalidate();
		//		createDataOverlays();
//		updateDataOverlays();
//		moveActiveViewWindow();


	}
	
	private void setTrackInvisible(int trackPosition) {	
		int state = GPSLoggerServiceManager.getLoggingState();
		Common.log("TAG", "ASLAI STATE IS: " + state);
        if (trackPosition == mTrackIds.size()-1 && (state == Constants.LOGGING || state == Constants.PAUSED)) {        	
        	return;
        }
        mTrackIds.get(trackPosition).visible = false;
        Common.log(TAG, "ASLAI: TRACKIDSTOPREFERENCE IS: " + trackIdsToPreference());
		Editor editor = mSharedPreferences.edit();
		editor.putString("mTrackIds", trackIdsToPreference());
		editor.commit();        
		List<Overlay> overlays = this.mMapView.getOverlays();
		overlays.clear(); 
		redrawOverlays();
		overlays.add(mMylocation);
		updateTitleBar();		
		mMapView.invalidate();
		//		createDataOverlays();
//		updateDataOverlays();
//		moveActiveViewWindow();


	}	
	private void setAllTracksInvisible(ArrayList<Track> tracks) {
		Track.setAllInvisible(tracks);
        Common.log(TAG, "ASLAI: TRACKIDSTOPREFERENCE IS: " + trackIdsToPreference());
		Editor editor = mSharedPreferences.edit();
		editor.putString("mTrackIds", trackIdsToPreference());
		editor.commit();        
		List<Overlay> overlays = this.mMapView.getOverlays();
		overlays.clear(); 
		redrawOverlays();
		overlays.add(mMylocation);
		updateTitleBar();
		mMapView.invalidate();		
	}
	/**
	 * Alter this to set a new track as current.
	 * 
	 * @param trackId
	 * @param center
	 *            center on the end of the track
	 */
	private void moveToTrack(long trackId, boolean center, boolean startLogging) {
		Common.log(TAG, "ASLAI: MOVE TO TRACK: " + trackId);
		Cursor track = null;
		try {
			ContentResolver resolver = this.getApplicationContext().getContentResolver();
			Uri trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId);
			Uri mediaUri = ContentUris.withAppendedId(Media.CONTENT_URI, this.getLastTrackId());
			track = resolver.query(trackUri, new String[] { Tracks.NAME, Tracks.DURATION,
					Tracks.DISTANCE }, null, null, null);
			addTrackIds(trackId, startLogging);
			if (track != null && track.moveToFirst()) {
//				this.mTrackId = trackId;
				mLastSegment = -1;
				mLastWaypoint = -1;
				// ASLAI PROBLEM HERE
				if (trackId == this.getLastTrackId() && track.getInt(1) != 0 && track.getDouble(2) != 0)
					statisticsPresent = true;
				else
					statisticsPresent = false;
				resolver.unregisterContentObserver(this.mTrackSegmentsObserver);
				resolver.unregisterContentObserver(this.mTrackMediasObserver);
				Uri tracksegmentsUri = Uri.withAppendedPath(Tracks.CONTENT_URI, getLastTrackId()
						+ "/segments");
				resolver.registerContentObserver(tracksegmentsUri, false,
						this.mTrackSegmentsObserver);
				resolver.registerContentObserver(mediaUri, false, this.mTrackMediasObserver);
//ASLAI CHANGED
				this.mMapView.getOverlays().clear();




			}

		}
		finally {
			if (track != null) {
				track.close();
			}
		}
		/*
		if (center) {
			GeoPoint lastPoint = getLastTrackPoint(trackId);
//ASLAI CHANGED			GeoPoint lastPoint = getLastTrackPoint();
			this.mMapView.getController().animateTo(lastPoint);
		}	
		*/
		updateTitleBar();
		updateDataOverlays();
		updateSpeedbarVisibility();					
		if (center) {
			Common.log(TAG, "ASLAI: Animating to track: " + trackId);
			GeoPoint lastPoint = getLastTrackPoint(trackId);
//ASLAI CHANGED			GeoPoint lastPoint = getLastTrackPoint();
			animateTo();
//			this.mMapView.getController().animateTo(lastPoint);
			Common.log(TAG, "ASLAI: Animating to :" + lastPoint.getLatitudeE6() + " " + lastPoint.getLongitudeE6());
			//ASLAI ADDED BELOW
			this.mMapView.invalidate();
		}	
	}
	/*
	//ASLAI
	private void moveToLoggingTrack(long trackId) {
		Cursor track = null;
		try {
			ContentResolver resolver = this.getApplicationContext().getContentResolver();
			Uri trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId);
			Uri mediaUri = ContentUris.withAppendedId(Media.CONTENT_URI, trackId);
			track = resolver.query(trackUri, new String[] { Tracks.NAME, Tracks.DURATION,
					Tracks.DISTANCE }, null, null, null);
			if (track != null && track.moveToFirst()) {
				addTrackIds(trackId, true);
//				this.mTrackId = trackId;
				mLastSegment = -1;
				mLastWaypoint = -1;
				if (track.getInt(1) != 0 && track.getDouble(2) != 0)
					statisticsPresent = true;
				else
					statisticsPresent = false;
				resolver.unregisterContentObserver(this.mTrackSegmentsObserver);
				resolver.unregisterContentObserver(this.mTrackMediasObserver);
				Uri tracksegmentsUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId
						+ "/segments");
				resolver.registerContentObserver(tracksegmentsUri, false,
						this.mTrackSegmentsObserver);
				resolver.registerContentObserver(mediaUri, false, this.mTrackMediasObserver);
//ASLAI CHANGED
//				this.mMapView.getOverlays().clear();

				updateTitleBar();
				updateDataOverlays();
				updateSpeedbarVisibility();

				if (true) {
					GeoPoint lastPoint = getLastTrackPoint();
					this.mMapView.getController().animateTo(lastPoint);
				}
			}
		}
		finally {
			if (track != null) {
				track.close();
			}
		}
	}

*/
	/**
	 * Get the last know position from the GPS provider and return that
	 * information wrapped in a GeoPoint to which the Map can navigate.
	 * 
	 * @see GeoPoint
	 * @return
	 */
	private GeoPoint getLastKnowGeopointLocation() {
		int microLatitude = 0;
		int microLongitude = 0;
		LocationManager locationManager = (LocationManager) this.getApplication().getSystemService(
				Context.LOCATION_SERVICE);
		Location locationFine = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (locationFine != null) {
			microLatitude = (int) (locationFine.getLatitude() * 1E6d);
			microLongitude = (int) (locationFine.getLongitude() * 1E6d);
		}
		if (locationFine == null || microLatitude == 0 || microLongitude == 0) {
			Location locationCoarse = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (locationCoarse != null) {
				microLatitude = (int) (locationCoarse.getLatitude() * 1E6d);
				microLongitude = (int) (locationCoarse.getLongitude() * 1E6d);
			}
			if (locationCoarse == null || microLatitude == 0 || microLongitude == 0) {
				//default location to Stanford, CA, USA
				microLatitude = 37424166;
				microLongitude = -122165000;
			}
		}
		GeoPoint geoPoint = new GeoPoint(microLatitude, microLongitude);
		return geoPoint;
	}

	/**
	 * Retrieve the last point of the current track
	 * 
	 * @param context
	 */
	private GeoPoint getLastTrackPoint() {
		Cursor waypoint = null;
		GeoPoint lastPoint = null;
		try {
			ContentResolver resolver = this.getContentResolver();
			waypoint = resolver.query(Uri.withAppendedPath(Tracks.CONTENT_URI, getLastTrackId()
					+ "/waypoints"), new String[] { Waypoints.LATITUDE, Waypoints.LONGITUDE,
					"max(" + Waypoints.TABLE + "." + Waypoints._ID + ")" }, null, null, null);
			if (waypoint != null && waypoint.moveToLast()) {
				int microLatitude = (int) (waypoint.getDouble(0) * 1E6d);
				int microLongitude = (int) (waypoint.getDouble(1) * 1E6d);
				lastPoint = new GeoPoint(microLatitude, microLongitude);
			}
			if (lastPoint == null || lastPoint.getLatitudeE6() == 0
					|| lastPoint.getLongitudeE6() == 0) {
				lastPoint = getLastKnowGeopointLocation();
			}
			else {
				mLastWaypoint = waypoint.getLong(2);
			}
		}
		finally {
			if (waypoint != null) {
				waypoint.close();
			}
		}
		return lastPoint;
	}
	//ASLAI Added
	private GeoPoint getFirstTrackPoint() {
		long waypointId = 0;
		Cursor minwaypoint = null;
		Cursor waypoint = null;
		GeoPoint lastPoint = null;
		ContentResolver resolver = this.getContentResolver();
		
		try {
			waypoint = resolver.query(Uri.withAppendedPath(Tracks.CONTENT_URI, getLastTrackId()
					+ "/waypoints"), new String[] { "max(" + Waypoints.TABLE + "." + Waypoints._ID + ")" }, null, null, null);

			if (waypoint != null && waypoint.moveToLast()) {
				mLastWaypoint = waypoint.getLong(0);
			}
		}
		finally {
			if (waypoint != null) {
				waypoint.close();
			}
		}

		try {
			minwaypoint = resolver.query(Uri.withAppendedPath(Tracks.CONTENT_URI, getLastTrackId()
				+ "/waypoints"), new String[] { "min(" + Waypoints.TABLE + "." + Waypoints._ID + ")" }, null, null, null);			
			if (minwaypoint != null && minwaypoint.moveToLast()) {
				waypointId = minwaypoint.getLong(0);
				waypoint = resolver.query(Uri.withAppendedPath(Tracks.CONTENT_URI, getLastTrackId()
					+ "/waypoints"), new String[] { Waypoints.LATITUDE, Waypoints.LONGITUDE }, 
					Waypoints.TABLE + "." + Waypoints._ID + " = " + waypointId, null, null);

				if (waypoint != null && waypoint.moveToLast()) {
					int microLatitude = (int) (waypoint.getDouble(0) * 1E6d);
					int microLongitude = (int) (waypoint.getDouble(1) * 1E6d);
					Common.log(TAG, "FIRST TRACK POINT IS : " + waypoint.getDouble(0) + ", "
							+ waypoint.getDouble(1));
					lastPoint = new GeoPoint(microLatitude, microLongitude);
				}
				if (lastPoint == null || lastPoint.getLatitudeE6() == 0
						|| lastPoint.getLongitudeE6() == 0) {
					lastPoint = getLastKnowGeopointLocation();
				}
		/*
				else {
					mLastWaypoint = waypoint.getLong(2);
				}
				*/
			}
			}
			finally {
				if (minwaypoint != null) {
					minwaypoint.close();
				}
				if (waypoint != null) {
					waypoint.close();
				}
			}
			
			/*
		if (waypoint != null) {
			waypoint.moveToFirst();
			Common.log(TAG, "WAYPOINTS ARE: \n");
			while (!waypoint.isAfterLast()) {
				Common.log(TAG, waypoint.getDouble(0) + ", "
						+ waypoint.getDouble(1) + "\n");
				waypoint.moveToNext();

			}
		}
		*/


		return lastPoint;
	}	
	
	//ASLAI Added
	private GeoPoint getLastTrackPoint(long trackId) {
		Cursor waypoint = null;
		GeoPoint lastPoint = null;
		try {
			ContentResolver resolver = this.getContentResolver();
			waypoint = resolver.query(Uri.withAppendedPath(Tracks.CONTENT_URI, trackId
					+ "/waypoints"), new String[] { Waypoints.LATITUDE, Waypoints.LONGITUDE,
					"max(" + Waypoints.TABLE + "." + Waypoints._ID + ")" }, null, null, null);
			if (waypoint != null && waypoint.moveToLast()) {
				Common.log(TAG, "ASLAI HERE1");
				int microLatitude = (int) (waypoint.getDouble(0) * 1E6d);
				int microLongitude = (int) (waypoint.getDouble(1) * 1E6d);
				lastPoint = new GeoPoint(microLatitude, microLongitude);
			}
			if (lastPoint == null || lastPoint.getLatitudeE6() == 0
					|| lastPoint.getLongitudeE6() == 0) {
				Common.log(TAG, "ASLAI HERE2");
				
				lastPoint = getLastKnowGeopointLocation();
			}
			else {
				Common.log(TAG, "ASLAI HERE3");
//ASLAI PROBLEM HERE
				mLastWaypoint = waypoint.getLong(2);
			}
		}
		finally {
			if (waypoint != null) {
				waypoint.close();
			}
		}
		return lastPoint;
	}

	private void moveToLastTrack() {
		Common.log(TAG, "ASLAI: MOVETOLASTTRACK");
		int trackId = -1;
		Cursor track = null;
		try {
			ContentResolver resolver = this.getApplicationContext().getContentResolver();
			track = resolver.query(Tracks.CONTENT_URI, new String[] { "max(" + Tracks._ID + ")",
					Tracks.NAME, }, null, null, null);
			if (track != null && track.moveToLast()) {
				trackId = track.getInt(0);
				moveToTrack(trackId, true, false);
			}
		}
		finally {
			if (track != null) {
				track.close();
			}
		}
	}
	
	private void syncGroupStats() {
		mStatsUpdating = true;
		if (!mTrackNameDialogShowing)
			mDialogUpdate = ProgressDialog.show(this, "", getString(R.string.dialog_updating_stats), true);
		int[] groupStatIds = mDbHelper.getGroupStatisticIds();
		if (groupStatIds == null || groupStatIds.length == 0) {
			updateUserStats();
		}
		else {
			try {
				mGameCon.getScoreBoards(GET_SBS_RID, mDbHelper.getGroupStatisticIds());
			} catch (RemoteException e) {}
		}
	}

	/**
	 * Calculates track duration, distance, etc. right after we stop tracking
	 */
	private boolean calculateTrackStatistics() {
		if (statisticsPresent)
			return false;
		Common.log(TAG, "calculateTrackStatistics()");
		long starttime = 0;
		double distanceTraveled = 0f;
		long duration = 0;

		Uri trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, this.getLastTrackId());
		Common.log(TAG, "trackUri = " + trackUri);
		ContentResolver resolver = this.getApplicationContext().getContentResolver();

		Cursor segments = null;
		Location lastLocation = null;
		Location currentLocation = null;
		try {
			Uri segmentsUri = Uri.withAppendedPath(trackUri, "segments");
			segments = resolver.query(segmentsUri, new String[] { Segments._ID }, null, null, null);
			if (segments.moveToFirst()) {
				do {
					long segmentsId = segments.getLong(0);
					Cursor waypoints = null;
					try {
						Uri waypointsUri = Uri.withAppendedPath(segmentsUri, segmentsId
								+ "/waypoints");
						waypoints = resolver.query(waypointsUri, new String[] { Waypoints._ID,
								Waypoints.TIME, Waypoints.LONGITUDE, Waypoints.LATITUDE }, null,
								null, null);
						if (waypoints.moveToFirst()) {
							do {
								if (starttime == 0) {
									starttime = waypoints.getLong(1);
								}
								currentLocation = new Location(this.getClass().getName());
								currentLocation.setTime(waypoints.getLong(1));
								currentLocation.setLongitude(waypoints.getDouble(2));
								currentLocation.setLatitude(waypoints.getDouble(3));
								if (lastLocation != null) {
									distanceTraveled += lastLocation.distanceTo(currentLocation);
									duration += currentLocation.getTime() - lastLocation.getTime();
								}
								lastLocation = currentLocation;

							} while (waypoints.moveToNext());
						}
					}
					finally {
						if (waypoints != null) {
							waypoints.close();
						}
					}
					lastLocation = null;
				} while (segments.moveToNext());
			}
		}
		finally {
			if (segments != null) {
				segments.close();
			}
		}

		ContentValues values = new ContentValues();
		values.put(Tracks.DURATION, new Long(duration));
		mCalculatedDuration = duration;
		values.put(Tracks.DISTANCE, new Double(distanceTraveled));
		mCalculatedDistance = distanceTraveled;
		if (mIsPartnerRun)
			values.put(Tracks.IS_PARTNER, 1);
		Common.log(TAG, "calculateTrackStatistics(): duration = " + duration
				+ "; distanceTraveled = " + distanceTraveled);
		resolver.update(trackUri, values, null, null);
		// resolver.notifyChange(trackUri, null);
		
		if (distanceTraveled > 0) {
			return true;
		}
		else {
			resolver.delete(trackUri, null, null);
			return false;
		}
	}
	
	private void updateUserStats() {
		double dist = mCalculatedDistance;
		long duration = mCalculatedDuration;
		Common.log(TAG, "updateUserStats(): dist = " + dist + "; duration = " + duration);
		int selfId = Common.getRegisteredUser(this).id;
		
		Editor editor = mSharedPreferences.edit();
		// Mark statistics as dirty
		editor.putBoolean(Constants.STATS_DIRTY_KEY, true);
		
		if (dist > 0 && duration > 0) {
			mDbHelper.increaseStatistic(Stats.DISTANCE_RAN_ID, -1, dist);
			mDbHelper.increaseStatistic(Stats.RUNNING_TIME_ID, -1, (double) duration);
			
			mDbHelper.updateDistanceRan(selfId);
			mDbHelper.updateRunningTime(selfId);
			
			float diffDistanceRan = mSharedPreferences.getFloat(Constants.DIFF_DISTANCE_RAN_KEY, 0f);
			float diffRunningTime = mSharedPreferences.getFloat(Constants.DIFF_RUNNING_TIME_KEY, 0f);
			
			editor.putFloat(Constants.DIFF_DISTANCE_RAN_KEY, diffDistanceRan + (float)dist);
			editor.putFloat(Constants.DIFF_DISTANCE_RAN_KEY, diffRunningTime + (float)duration);
		}
		
		if (mIsPartnerRun) {
			mDbHelper.increaseStatisticByOne(Stats.NUM_PARTNER_RUNS_ID, -1);
			mDbHelper.updateNumPartnerRuns(selfId);
			
			int diffPartnerRuns = mSharedPreferences.getInt(Constants.DIFF_NUM_PARTNER_RUNS_KEY, 0);
			editor.putInt(Constants.DIFF_NUM_PARTNER_RUNS_KEY, diffPartnerRuns + 1);
		}
		
		mDbHelper.increaseStatisticByOne(Stats.NUM_RUNS_ID, -1);
		
		mDbHelper.updateNumRuns(selfId);
		mDbHelper.updateAvgSpeed();
		//mDbHelper.updateMedDuration();
		//mDbHelper.updateMedDistance();
		
		int diffRuns = mSharedPreferences.getInt(Constants.DIFF_NUM_RUNS_KEY, 0);
		editor.putInt(Constants.DIFF_NUM_RUNS_KEY, diffRuns + 1);
		
		editor.commit();
		
		ScoreBoard[] scores = mDbHelper.getAllStatistics();
		try {
			mGameCon.updateScoreBoards(UPDATE_SBS_RID, scores);
		} catch (RemoteException e) {}
	}
	
	private void updateAchievements() {
		Cursor newAchCursor = mDbHelper.updateAchievements();
		if (newAchCursor.getCount() > 0) {			
			// Display a toast notification of the first achievement
			newAchCursor.moveToNext();
			View toastLayout = getLayoutInflater().inflate(R.layout.ach_toast, 
					(ViewGroup) findViewById(R.id.toast_layout_root));
			
			String title = newAchCursor.getString(8);
			Common.displayAchievementToast(title, newAchCursor.getInt(7),
					newAchCursor.getInt(4) == 0, getApplicationContext(), toastLayout);
			
			if (mSharedPreferences.getBoolean(Constants.POST_FB_KEY, false)) {
				Bundle params = new Bundle();
				params.putString("message", "I just ran " + Common.distanceString(this, mCalculatedDistance) + ". Got the \"" + title + "\" badge!");
				params.putString("link", Constants.SITE_URL);
				params.putString("name", Constants.SITE_TITLE);
				params.putString("caption", Constants.SITE_SLOGAN);
				params.putString("picture", Constants.SITE_LOGO);
				params.putString("privacy", "{\"value\": \"ALL_FRIENDS\"}");
				mAsyncRunner.request("me/feed", params, "POST", new WallPostListener());
			}
			
			//TODO:Snaptic integration
			if (mSharedPreferences.getBoolean(Constants.POST_CATCH_KEY, false)) {
				IntentIntegrator notesIntent = new IntentIntegrator(LoggerMap.this);
				notesIntent.createNote("I just ran " + Common.distanceString(this, mCalculatedDistance) + ". Got the \"" + title + "\" badge!\n\n#Moovy", true);
			}
		}
		else {
			if (mSharedPreferences.getBoolean(Constants.POST_FB_KEY, false)) {
				Bundle params = new Bundle();
				params.putString("message", "Just ran " + Common.distanceString(this, mCalculatedDistance) + "!");
				params.putString("link", Constants.SITE_URL);
				params.putString("name", Constants.SITE_TITLE);
				params.putString("caption", Constants.SITE_SLOGAN);
				params.putString("picture", Constants.SITE_LOGO);
				params.putString("privacy", "{\"value\": \"ALL_FRIENDS\"}");
				mAsyncRunner.request("me/feed", params, "POST", new WallPostListener());
			}
			
			if (mSharedPreferences.getBoolean(Constants.POST_CATCH_KEY, false)) {
				IntentIntegrator notesIntent = new IntentIntegrator(LoggerMap.this);
				notesIntent.createNote("Just ran " + Common.distanceString(this, mCalculatedDistance) + "!\n\n#Moovy", true);
			}
		}
		newAchCursor.close();
	}

	/***
	 * Collecting additional data
	 */
	private void addPicture() {
		Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ Constants.TMPICTUREFILE_PATH);
		// Common.log( TAG, "Picture requested at: " + file );
		i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		i.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(i, MENU_PICTURE);
	}

	/***
	 * Collecting additional data
	 */
	private void addVideo() {
		Intent i = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ Constants.TMPICTUREFILE_PATH);
		// Common.log( TAG, "Video requested at: " + file );
		i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		i.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(i, MENU_VIDEO);
	}

	private void addVoice() {
		Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
		startActivityForResult(intent, MENU_VOICE);
	}
	
	private class ScoreboardUpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Common.log(TAG, "onReceive()");
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Common.log(TAG, appResponse.toString());
					
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						final int requestId = appResponse.request_id;
						LoggerMap.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mDialogUpdate != null) mDialogUpdate.dismiss();
								Toast toast = Toast.makeText(LoggerMap.this, 
										R.string.connection_error_toast, Toast.LENGTH_SHORT);
								toast.show();
								
								if (requestId == GET_SBS_RID)
									//Continue calculating track stats even if we can't
									// sync group stats from server
									updateUserStats();
								else if (requestId == UPDATE_SBS_RID) {
									//Update achievements with local solo stats even if can't
									// do so for group stats
									mStatsUpdating = false;
									if (!mTrackNameDialogShowing) updateAchievements();
								}
							}
						});
						continue;
					}
					else if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_VERSION_ERROR)) {
						LoggerMap.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mDialogUpdate != null) mDialogUpdate.dismiss();
								Common.displayUpgradeDialog(LoggerMap.this);
							}
						});
						continue;
					}
					
					switch(appResponse.request_id) {
					case GET_SBS_RID:
						final ScoreBoard[] scores = (ScoreBoard[])(appResponse.object);
						LoggerMap.this.runOnUiThread(new Runnable() {
							public void run() {
								if (scores != null) {
									mDbHelper.updateScoreboards(scores);
									if (mSharedPreferences.getBoolean(Constants.STATS_DIRTY_KEY, false))
										mDbHelper.applyStatDiffs();
								}
								updateUserStats();
							}
						});
						break;
					case UPDATE_SBS_RID:
						LoggerMap.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mDialogUpdate != null) mDialogUpdate.dismiss();
								
								//Reset dirty bit and all diffs
								Editor editor = mSharedPreferences.edit();
								editor.putBoolean(Constants.STATS_DIRTY_KEY, false);
								editor.putFloat(Constants.DIFF_DISTANCE_RAN_KEY, 0f);
								editor.putFloat(Constants.DIFF_DISTANCE_RAN_KEY, 0f);
								editor.putInt(Constants.DIFF_NUM_RUNS_KEY, 0);
								editor.putInt(Constants.DIFF_NUM_PARTNER_RUNS_KEY, 0);
								editor.commit();
								
								mStatsUpdating = false;
								if (!mTrackNameDialogShowing) updateAchievements();
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
	
	private class WallPostListener implements RequestListener {

		public void onComplete(final String response) {
			// process the response here: executed in background thread
			Common.log(TAG, "Wall Post Response: " + response.toString());
		}

		public void onFacebookError(FacebookError e) {}

		public void onFileNotFoundException(FileNotFoundException e) {}

		public void onIOException(IOException e) {}

		public void onMalformedURLException(MalformedURLException e) {}
	}
}