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
package edu.stanford.cs.sujogger.actions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Obj;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.actions.utils.GpxCreator;
import edu.stanford.cs.sujogger.actions.utils.XmlCreationProgressListener;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.viewer.FriendPicker;
import edu.stanford.cs.sujogger.viewer.LoggerMap;
import edu.stanford.cs.sujogger.viewer.TrackList;

/**
 * Store a GPX file to SDCard
 * 
 * @version $Id: PublishGPX.java 472 2010-04-08 09:50:01Z rcgroot $
 * @author rene (c) Mar 22, 2009, Sogeti B.V.
 */
public class PublishGPX extends Activity {
	public static final String TAG = "OGT.PublishGPX";

	private static final int DIALOG_FILENAME = 11;
	private static final int PROGRESS_STEPS = 10;

	private RemoteViews mContentView;
	private int barProgress = 0;
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private EditText mFileNameView;

	private PublishGPXReceiver mReceiver;
	private GamingServiceConnection mGamingServiceConn;
	private DatabaseHelper mDbHelper;
	
	private ProgressDialog mProgressDialog;

	class PublishGPXReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGamingServiceConn.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					Log.d(TAG, "PUBLISHGPXReceiver: Response received with request id:"
							+ appResponse.request_id);
					
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						PublishGPX.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mProgressDialog != null) mProgressDialog.dismiss();
								Toast toast = Toast.makeText(PublishGPX.this, 
										R.string.connection_error_toast, Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						continue;
					}
					
					switch (appResponse.request_id) {
					case TrackList.PUBLISH_TRACK:
						final int trackId = ((Obj) appResponse.object).id;
						final Obj obj = (Obj) appResponse.appRequest.object;
						final int _id = obj.object_properties[0].int_val;
						Log.d(TAG, "TRACKID IS " + trackId);
						Log.d(TAG, "APPRESPONSE OBJECT IS: "
								+ appResponse.object.getClass().getName());
						
						PublishGPX.this.runOnUiThread(new Runnable() {
							public void run() {
								ContentValues values = new ContentValues();
								Log.d(TAG, "TRACKID IS " + trackId);
								values.put(Tracks.TRACK_ID, trackId);
								Log.d(TAG, "URI IS " + PublishGPX.this.getIntent().getData());
								DatabaseHelper mDbHelper = new DatabaseHelper(PublishGPX.this);
								mDbHelper.openAndGetDb();
								mDbHelper.updateTrack(_id, values);
								mDbHelper.close();
								
								mProgressDialog.cancel();
								PublishGPX.this.finalize();
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

	private OnClickListener mOnClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case Dialog.BUTTON_POSITIVE:
				PublishGPX.this.exportGPX(mFileNameView.getText().toString());
				break;
			case Dialog.BUTTON_NEGATIVE:
				PublishGPX.this.finish();
				break;
			}
		}
	};

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setVisible(false);
		super.onCreate(savedInstanceState);
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		mReceiver = new PublishGPXReceiver();
		
		mGamingServiceConn = new GamingServiceConnection(this, mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, this.getClass().getName());
		mGamingServiceConn.bind();
		User user = Common.getRegisteredUser(this);
		mGamingServiceConn.setUserId(user.id, user.fb_id, user.fb_token);
		
		showDialog(DIALOG_FILENAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Builder builder;
		switch (id) {
		case DIALOG_FILENAME:
			LayoutInflater factory = LayoutInflater.from(this);
			View view = factory.inflate(R.layout.filenamedialog, null);
			EditText editText = (EditText) view.findViewById(R.id.fileNameField);
			if (this.getIntent() != null && this.getIntent().getExtras() != null) {
				Log.d(TAG, "FILENAME IS " + this.getIntent().getExtras().getString("name"));
				editText.setText(this.getIntent().getExtras().getString("name"));
			}
			else
				Log.d(TAG, "NO INTENT IN ONCREATEDIALOG");
			mFileNameView = (EditText) view.findViewById(R.id.fileNameField);
			builder = new AlertDialog.Builder(this).setTitle(R.string.dialog_track_title)
					.setMessage(R.string.dialog_filename_publish).setIcon(
							android.R.drawable.ic_dialog_alert).setView(view).setPositiveButton(
							R.string.btn_okay, mOnClickListener).setNegativeButton(
							R.string.btn_cancel, mOnClickListener);
			Dialog dialog = builder.create();
			dialog.setOwnerActivity(this);
			return dialog;
		default:
			return super.onCreateDialog(id);
		}
	}

	protected void exportGPX(String chosenBaseFileName) {
		GpxCreator mGpxCreator = new GpxCreator(this, getIntent(), chosenBaseFileName, null,
				mGamingServiceConn, true);
		mGpxCreator.start();
		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.ticker_publishing) + " "
				+ chosenBaseFileName + " to server", true);

		// this.finish();
	}

	public void finalize() {
		mGamingServiceConn.unbind();
		finish();
		onDestroy();
	}

	public void onDestroy() {
		mGamingServiceConn.unbind();
		super.onDestroy();

	}

	class ProgressListener implements XmlCreationProgressListener {
		public void startNotification(String fileName) {
			String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) PublishGPX.this.getSystemService(ns);
			int icon = android.R.drawable.ic_menu_save;
			CharSequence tickerText = getString(R.string.ticker_publishing) + " " + fileName
					+ " to server";

			mNotification = new Notification();
			PendingIntent contentIntent = PendingIntent.getActivity(PublishGPX.this, 0, new Intent(
					PublishGPX.this, LoggerMap.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
					PendingIntent.FLAG_UPDATE_CURRENT);

			mNotification.contentIntent = contentIntent;
			mNotification.tickerText = tickerText;
			mNotification.icon = icon;
			mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
			mContentView = new RemoteViews(getPackageName(), R.layout.savenotificationprogress);
			mContentView.setImageViewResource(R.id.icon, icon);
			mContentView.setTextViewText(R.id.progresstext, tickerText);

			mNotification.contentView = mContentView;
		}

		public void updateNotification(int progress, int goal) {
			// Log.d( TAG, "Progress " + progress + " of " + goal );
			if (progress > 0 && progress < goal) {
				if ((progress * PROGRESS_STEPS) / goal != barProgress) {
					barProgress = (progress * PROGRESS_STEPS) / goal;
					mContentView.setProgressBar(R.id.progress, goal, progress, false);
					mNotificationManager.notify(R.layout.savenotificationprogress, mNotification);
				}
			}
			else if (progress == 0) {
				mContentView.setProgressBar(R.id.progress, goal, progress, true);
				mNotificationManager.notify(R.layout.savenotificationprogress, mNotification);
			}
			else if (progress >= goal) {
				mContentView.setProgressBar(R.id.progress, goal, progress, false);
				mNotificationManager.notify(R.layout.savenotificationprogress, mNotification);
			}
		}

		public void endNotification(String filename) {
			mNotificationManager.cancel(R.layout.savenotificationprogress);
		}
	}
}