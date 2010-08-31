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
package edu.stanford.cs.sujogger.db;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.stanford.cs.gaming.sdk.model.Group;
import edu.stanford.cs.gaming.sdk.model.ScoreBoard;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.sujogger.db.GPStracking.Achievements;
import edu.stanford.cs.sujogger.db.GPStracking.GMRecipients;
import edu.stanford.cs.sujogger.db.GPStracking.GameMessages;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.GroupsTemp;
import edu.stanford.cs.sujogger.db.GPStracking.GroupsUsers;
import edu.stanford.cs.sujogger.db.GPStracking.Media;
import edu.stanford.cs.sujogger.db.GPStracking.MediaColumns;
import edu.stanford.cs.sujogger.db.GPStracking.ScoreboardTemp;
import edu.stanford.cs.sujogger.db.GPStracking.Segments;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.db.GPStracking.TracksColumns;
import edu.stanford.cs.sujogger.db.GPStracking.Users;
import edu.stanford.cs.sujogger.db.GPStracking.Waypoints;
import edu.stanford.cs.sujogger.db.GPStracking.WaypointsColumns;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.MessageObject;
import edu.stanford.cs.sujogger.util.Statistic;

/**
 * Class to hold bare-metal database operations exposed as functionality blocks
 * To be used by database adapters, like a content provider, that implement a
 * required functionality set
 * 
 * @version $Id: DatabaseHelper.java 461 2010-03-19 14:15:19Z rcgroot $
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private final static String TAG = "OGT.DatabaseHelper";
	
	private static String DB_PATH = "/data/data/edu.stanford.cs.sujogger/databases/";
	private static String DB_NAME = "SUJogger.sqlite";
	private static int DB_VERSION = 1;
	
	private Context mContext;
	private SQLiteDatabase mDb;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		/*
		 * db.execSQL( Waypoints.CREATE_STATEMENT ); db.execSQL(
		 * Segments.CREATE_STATMENT ); db.execSQL( Tracks.CREATE_STATEMENT );
		 * db.execSQL( Media.CREATE_STATEMENT );
		 */
	}
	
	public void createDatabase() throws IOException {
		boolean dbExist = checkDatabase();
		
		if (!dbExist) {
			Log.d(TAG, "createDatabase(): creating and copying database");
			this.getReadableDatabase();
			try {
				copyDatabase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}
	
	private boolean checkDatabase() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	}
		catch(SQLiteException e) {}
 
    	if(checkDB != null) checkDB.close();
    	return checkDB != null ? true : false;
	}
	
	private void copyDatabase() throws IOException {
		InputStream myInput = mContext.getAssets().open(DB_NAME);
		String outFileName = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);
		
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0) {
			myOutput.write(buffer, 0, length);
		}
		
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public SQLiteDatabase openAndGetDb() throws SQLException {
		if (mDb == null || !mDb.isOpen())
			mDb = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
		return mDb;
	}
	
	@Override
	public synchronized void close() {
		if (mDb != null) mDb.close();
		super.close();
	}
	
	/**
	 * 
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int current, int targetVersion) {

	}
	
	//ASLAI
	public ArrayList<Statistic> getStatistics(long group_id) {
		ArrayList<Statistic> stats = new ArrayList<Statistic>();
    	openAndGetDb();	
		Cursor cursor = mDb.query(Stats.TABLE, new String[] {"value", "statistic_id", "group_id"}, "group_id = " + ((group_id == -1)?0:group_id), null, null, null, "statistic_id");
	    if (cursor != null) {
	        int count = cursor.getCount();
	        if (count == 0)
	        	return stats;
	        cursor.moveToFirst();
	        Statistic stat = null;
	        for (int i = 0; i < count; i++) {
	        	stat = new Statistic(cursor.getDouble(0), cursor.getInt(1));
	        	Log.d("STATISTICS", "STAT IS " + cursor.getDouble(0) + " " + cursor.getInt(1) + " " + cursor.getInt(2)); 
	        	stats.add(stat);
	        	cursor.moveToNext();	  
	        }
	    } 
		cursor.close();
		close();	
		return stats;
	}
	
	public void updateTrackRemoteId(int _id, int track_id) {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DISTANCE + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.DISTANCE_RAN_WEEK_ID, 0, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DISTANCE + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.DISTANCE_RAN_MONTH_ID, 0, cursor.getDouble(0));
		cursor.close();
		/*
	
		public boolean setStatistic(long statisticId, double val) {		
			ContentValues args = new ContentValues();
			args.put(Stats.VALUE, val);
			
			return mDb.update(Stats.TABLE, args, Stats._ID + "=" + statisticId, null) > 0;
		}	
		*/	
	}

	/**
	 * Creates a waypoint under the current track segment with the current time
	 * on which the waypoint is reached
	 * 
	 * @param track
	 *            track
	 * @param segment
	 *            segment
	 * @param latitude
	 *            latitude
	 * @param longitude
	 *            longitude
	 * @param time
	 *            time
	 * @param speed
	 *            the measured speed
	 * @return
	 */
	long insertWaypoint(long trackId, long segmentId, Location location) {
		if (trackId < 0 || segmentId < 0) {
			throw new IllegalArgumentException("Track and segments may not the less then 0.");
		}

		ContentValues args = new ContentValues();
		args.put(WaypointsColumns.SEGMENT, segmentId);
		args.put(WaypointsColumns.TIME, location.getTime());
		args.put(WaypointsColumns.LATITUDE, location.getLatitude());
		args.put(WaypointsColumns.LONGITUDE, location.getLongitude());
		args.put(WaypointsColumns.SPEED, location.getSpeed());
		args.put(WaypointsColumns.ACCURACY, location.getAccuracy());
		args.put(WaypointsColumns.ALTITUDE, location.getAltitude());
		args.put(WaypointsColumns.BEARING, location.getBearing());

		// Log.d( TAG, "Waypoint time stored in the datebase"+
		// DateFormat.getInstance().format(new Date( args.getAsLong(
		// Waypoints.TIME ) ) ) );

		long waypointId = mDb.insert(Waypoints.TABLE, null, args);

		ContentResolver resolver = this.mContext.getContentResolver();
		resolver.notifyChange(Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments/"
				+ segmentId + "/waypoints"), null);

		return waypointId;
	}

	long insertMedia(long trackId, long segmentId, long waypointId, String mediaUri) {
		if (trackId < 0 || segmentId < 0 || waypointId < 0) {
			throw new IllegalArgumentException(
					"Track, segments and waypoint may not the less then 0.");
		}
		
		ContentValues args = new ContentValues();
		args.put(MediaColumns.TRACK, trackId);
		args.put(MediaColumns.SEGMENT, segmentId);
		args.put(MediaColumns.WAYPOINT, waypointId);
		args.put(MediaColumns.URI, mediaUri);

		// Log.d( TAG, "Media stored in the datebase: "+mediaUri );

		long mediaId = mDb.insert(Media.TABLE, null, args);

		ContentResolver resolver = this.mContext.getContentResolver();
		Uri notifyUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments/" + segmentId
				+ "/waypoints/" + waypointId + "/media");
		resolver.notifyChange(notifyUri, null);
		resolver.notifyChange(ContentUris.withAppendedId(Media.CONTENT_URI, trackId), null);
		Log.d(TAG, "Notify: " + ContentUris.withAppendedId(Media.CONTENT_URI, trackId).toString());

		return mediaId;
	}
	
	public long createTrack(ContentValues contentValues) {
//		contentValues.put(Tracks.CREATION_TIME, new Date().getTime());
		long id = mDb.insert(Tracks.TABLE, null, contentValues);
		return id;
	}

	public long updateTrack(long _id, ContentValues contentValues) {
//		contentValues.put(Tracks.CREATION_TIME, new Date().getTime());
		long id = mDb.update(Tracks.TABLE, contentValues, "_id = " + _id , null);
		return id;
	}
	
	/**
	 * Deletes a single track and all underlying segments and waypoints
	 * 
	 * @param trackId
	 * @return
	 */
	int deleteTrack(long trackId) {
		int affected = 0;
		Cursor cursor = null;
		long segmentId = -1;

		try {
			cursor = mDb.query(Segments.TABLE, new String[] { Segments._ID }, Segments.TRACK
					+ "= ?", new String[] { String.valueOf(trackId) }, null, null, null, null);
			if (cursor.moveToFirst()) {
				segmentId = cursor.getLong(0);
				affected += deleteSegment(mDb, trackId, segmentId);
			}
			else {
				Log.e(TAG, "Did not find the last active segment");
			}
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		affected += mDb.delete(Tracks.TABLE, Tracks._ID + "= ?", new String[] { String
				.valueOf(trackId) });

		ContentResolver resolver = this.mContext.getContentResolver();
		resolver.notifyChange(Tracks.CONTENT_URI, null);
		resolver.notifyChange(ContentUris.withAppendedId(Tracks.CONTENT_URI, trackId), null);

		return affected;
	}

	/**
	 * 
	 * TODO
	 * 
	 * @param mediaId
	 * @return
	 */
	int deleteMedia(long mediaId) {
		Cursor cursor = null;
		long trackId = -1;
		long segmentId = -1;
		long waypointId = -1;
		try {
			cursor = mDb.query(Media.TABLE, new String[] { Media.TRACK, Media.SEGMENT,
					Media.WAYPOINT }, Media._ID + "= ?", new String[] { String.valueOf(mediaId) },
					null, null, null, null);
			if (cursor.moveToFirst()) {
				trackId = cursor.getLong(0);
				segmentId = cursor.getLong(0);
				waypointId = cursor.getLong(0);
			}
			else {
				Log.e(TAG, "Did not find the last active segment");
			}
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		int affected = mDb.delete(Media.TABLE, Media._ID + "= ?", new String[] { String
				.valueOf(mediaId) });

		ContentResolver resolver = this.mContext.getContentResolver();
		Uri notifyUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments/" + segmentId
				+ "/waypoints/" + waypointId + "/media");
		resolver.notifyChange(notifyUri, null);
		notifyUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments/" + segmentId
				+ "/media");
		resolver.notifyChange(notifyUri, null);
		notifyUri = Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/media");
		resolver.notifyChange(notifyUri, null);
		resolver.notifyChange(ContentUris.withAppendedId(Media.CONTENT_URI, mediaId), null);

		return affected;
	}

	/**
	 * Delete a segment and all member waypoints
	 * 
	 * @param sqldb
	 *            The SQLiteDatabase in question
	 * @param trackId
	 *            The track id of this delete
	 * @param segmentId
	 *            The segment that needs deleting
	 * @return
	 */
	int deleteSegment(SQLiteDatabase sqldb, long trackId, long segmentId) {
		int affected = sqldb.delete(Segments.TABLE, Segments._ID + "= ?", new String[] { String
				.valueOf(segmentId) });

		// Delete all waypoints from segments
		affected += sqldb.delete(Waypoints.TABLE, Waypoints.SEGMENT + "= ?", new String[] { String
				.valueOf(segmentId) });
		// Delete all media from segment
		affected += sqldb.delete(Media.TABLE, Media.TRACK + "= ? AND " + Media.SEGMENT + "= ?",
				new String[] { String.valueOf(trackId), String.valueOf(segmentId) });

		ContentResolver resolver = this.mContext.getContentResolver();
		resolver.notifyChange(Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments/"
				+ segmentId), null);
		resolver.notifyChange(Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments"), null);

		return affected;
	}

	/**
	 * Move to a fresh track with a new first segment for this track
	 * 
	 * @return
	 */
	long toNextTrack(String name) {
		long currentTime = new Date().getTime();
		ContentValues args = new ContentValues();
		args.put(TracksColumns.NAME, name);
		args.put(TracksColumns.CREATION_TIME, currentTime);
		args.put(TracksColumns.USER_ID, Common.getRegisteredUser(mContext).id);
		
		long trackId = mDb.insert(Tracks.TABLE, null, args);

		ContentResolver resolver = this.mContext.getContentResolver();
		resolver.notifyChange(Tracks.CONTENT_URI, null);

		return trackId;
	}

	/**
	 * Moves to a fresh segment to which waypoints can be connected
	 * 
	 * @return
	 */
	long toNextSegment(long trackId) {
		ContentValues args = new ContentValues();
		args.put(Segments.TRACK, trackId);
		long segmentId = mDb.insert(Segments.TABLE, null, args);

		ContentResolver resolver = this.mContext.getContentResolver();
		resolver.notifyChange(Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments"), null);

		return segmentId;
	}
	
	/**
	 * Statistics Methods
	 */
	
	public long getStatisticLong(long statisticId, long groupId) {
		return (long) getStatisticReal(statisticId, groupId);
	}
	
	public double getStatisticReal(long statisticId, long groupId) {
		double statVal = 0;
		Cursor cursor = mDb.query(true, Stats.TABLE, new String[] {Stats.VALUE}, 
				Stats.STATISTIC_ID + "=" + statisticId + " AND " + 
				Stats.GROUP_ID + "=" + groupId, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
			statVal = cursor.getDouble(0);
		
		cursor.close();
		return statVal;
	}
	
	public ScoreBoard[] scoreBoardArrayFromScores(Cursor scores) {
		ScoreBoard score;
		ScoreBoard[] scoreArray = new ScoreBoard[scores.getCount()];
		int i = 0;
		while (scores.moveToNext()) {
			score = new ScoreBoard();
			score.id = scores.getInt(2);
			score.app_id = Constants.APP_ID;
			score.user_id = scores.getInt(3) > 0 ? 0 : 
				Common.getRegisteredUser(mContext).id;
			score.group_id = scores.getInt(3);
			score.value = (int)scores.getDouble(4);
			score.sb_type = scores.getString(1);
			scoreArray[i] = score;
			Log.d(TAG, "sb_type = " + score.sb_type + "; value = " + score.value);
			i++;
		}
		scores.close();
		return scoreArray;
	}
	
	public int[] scoreBoardIdArrayFromScores(Cursor scores) {
		int[] scoreArray = new int[scores.getCount()];
		int i = 0;
		while (scores.moveToNext()) {
			scoreArray[i] = scores.getInt(2);
			i++;
		}
		scores.close();
		return scoreArray;
	}
	
	public ScoreBoard[] getSoloStatistics() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM statistics WHERE " + 
				Stats.GROUP_ID + "=" + 0, null);
		return scoreBoardArrayFromScores(cursor);
	}
	
	public ScoreBoard[] getGroupStatistics() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM statistics WHERE " + 
				Stats.GROUP_ID + ">" + 0, null);
		return scoreBoardArrayFromScores(cursor);
	}
	
	public ScoreBoard[] getAllStatistics() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + Stats.TABLE, null);
		return scoreBoardArrayFromScores(cursor);
	}
	
	public int[] getGroupStatisticIds() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM statistics WHERE " + 
				Stats.GROUP_ID + ">" + 0, null);
		return scoreBoardIdArrayFromScores(cursor);
	}
	
	public boolean setStatistic(long statisticId, long groupId, double val) {		
		ContentValues args = new ContentValues();
		args.put(Stats.VALUE, val);
		
		return mDb.update(Stats.TABLE, args, 
				Stats.STATISTIC_ID + "=" + statisticId + " AND " + 
				Stats.GROUP_ID + "=" + groupId, null) > 0;
	}
	
	//If groupId == -1, update ALL statistics with the given statisticId
	//If groupId < -1, update group statistics with the given statisticId
	public void increaseStatistic(long statisticId, long groupId, double val) {
		ContentValues args = new ContentValues();
		args.put(Stats.VALUE, val);
		
		if (groupId >= 0)
			mDb.execSQL("UPDATE " + Stats.TABLE + " SET " + Stats.VALUE + " = " + Stats.VALUE + 
					" + ? WHERE " + Stats.STATISTIC_ID + " = ? AND " + Stats.GROUP_ID + " = ?", 
					new Object[] {new Double(val), new Long(statisticId), new Long(groupId)});
		else if (groupId == -1)
			mDb.execSQL("UPDATE " + Stats.TABLE + " SET " + Stats.VALUE + " = " + Stats.VALUE + 
					" + ? WHERE " + Stats.STATISTIC_ID + " = ?", 
					new Object[] {new Double(val), new Long(statisticId)});
		else
			mDb.execSQL("UPDATE " + Stats.TABLE + " SET " + Stats.VALUE + " = " + Stats.VALUE + 
					" + ? WHERE " + Stats.STATISTIC_ID + " = ? AND " + Stats.GROUP_ID + " > 0", 
					new Object[] {new Double(val), new Long(statisticId)});
			
	}
	
	public void increaseStatisticByOne(long statisticId, long groupId) {
		increaseStatistic(statisticId, groupId, 1);
	}
	
	// Updates distances run in the past week / month
	public void updateDistanceRan(int selfId) {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DISTANCE + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.DISTANCE_RAN_WEEK_ID, 0, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DISTANCE + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.DISTANCE_RAN_MONTH_ID, 0, cursor.getDouble(0));
		cursor.close();
	}
	
	// Updates total running time in the past week / month
	public void updateRunningTime(int selfId) {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DURATION + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.RUNNING_TIME_WEEK_ID, 0, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DURATION + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.RUNNING_TIME_MONTH_ID, 0, cursor.getDouble(0));
		cursor.close();
	}
	
	// Updates number of runs in the past week / month
	public void updateNumRuns(int selfId) {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"count(" + Tracks._ID + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.NUM_RUNS_WEEK_ID, 0, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"count(" + Tracks._ID + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.NUM_RUNS_MONTH_ID, 0, cursor.getDouble(0));
		cursor.close();
	}
	
	// Updates number of partner runs in the past week / month
	public void updateNumPartnerRuns(int selfId) {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"count(" + Tracks._ID + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId + " AND " + Tracks.IS_PARTNER + "=" + 1, 
				null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.NUM_PARTNER_RUNS_WEEK_ID, 0, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"count(" + Tracks._ID + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold + " AND " + 
				Tracks.USER_ID + "=" + selfId + " AND " + Tracks.IS_PARTNER + "=" + 1, 
				null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.NUM_PARTNER_RUNS_MONTH_ID, 0, cursor.getDouble(0));
		cursor.close();
	}
	
	public void updateAvgSpeed() {
		double totalDist = getStatisticReal(Stats.DISTANCE_RAN_ID, 0);
		double totalTime = getStatisticReal(Stats.RUNNING_TIME_ID, 0);
		if (totalTime != 0)
			setStatistic(Stats.AVG_SPEED_ID, 0, 
					totalDist / totalTime * Constants.SPEED_CONVERSION_RATIO);
		
		double totalDistWeek = getStatisticReal(Stats.DISTANCE_RAN_WEEK_ID, 0);
		double totalTimeWeek = getStatisticReal(Stats.RUNNING_TIME_WEEK_ID, 0);
		if (totalTime != 0)
			setStatistic(Stats.AVG_SPEED_WEEK_ID, 0, 
					totalDistWeek / totalTimeWeek * Constants.SPEED_CONVERSION_RATIO);
		
		double totalDistMonth = getStatisticReal(Stats.DISTANCE_RAN_MONTH_ID, 0);
		double totalTimeMonth = getStatisticReal(Stats.RUNNING_TIME_MONTH_ID, 0);
		if (totalTime != 0)
			setStatistic(Stats.AVG_SPEED_MONTH_ID, 0, 
					totalDistMonth / totalTimeMonth * Constants.SPEED_CONVERSION_RATIO);
	}
	/*
	public void updateMedDuration() {
		long numTracks = getStatisticLong(Stats.NUM_RUNS_ID, 0);
		long queryLimit = numTracks > 4 ? numTracks / 4 : numTracks;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {Tracks.DURATION}, 
				null, null, null, null, Tracks.DURATION, String.valueOf(queryLimit));
		
		int queryCount = cursor.getCount();
		if (queryCount > 0) {
			cursor.moveToPosition(queryCount / 2);
			setStatistic(Stats.MED_DURATION_ID, 0, cursor.getLong(0));
		}
		
		cursor.close();
	}
	
	public void updateMedDistance() {
		long numTracks = getStatisticLong(Stats.NUM_RUNS_ID, 0);
		long queryLimit = numTracks > 4 ? numTracks / 4 : numTracks;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {Tracks.DISTANCE}, 
				null, null, null, null, Tracks.DISTANCE, String.valueOf(queryLimit));
		
		int queryCount = cursor.getCount();
		if (queryCount > 0) {
			cursor.moveToPosition(queryCount / 2);
			setStatistic(Stats.MED_DURATION_ID, 0, cursor.getDouble(0));
		}
		
		cursor.close();
	}
	*/
	
	public void applyStatDiffs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Log.d(TAG, "applyStatDiffs(): num runs diff = " + prefs.getInt(Constants.DIFF_NUM_RUNS_KEY, 0));
		increaseStatistic(Stats.DISTANCE_RAN_ID, -2, 
				prefs.getFloat(Constants.DIFF_DISTANCE_RAN_KEY, 0f));
		increaseStatistic(Stats.RUNNING_TIME_ID, -2, 
				prefs.getFloat(Constants.DIFF_RUNNING_TIME_KEY, 0f));
		increaseStatistic(Stats.NUM_RUNS_ID, -2, 
				prefs.getInt(Constants.DIFF_NUM_RUNS_KEY, 0));
		increaseStatistic(Stats.NUM_PARTNER_RUNS_ID, -2, 
				prefs.getInt(Constants.DIFF_NUM_PARTNER_RUNS_KEY, 0));
	}
	
	// Updates solo statistics with ScoreBoards from the server
	public void updateSoloScoreboards(ScoreBoard[] scores) {
		boolean shouldUpdateAchievements = false;
		ContentValues values = new ContentValues();
		int[] allStats = Stats.ALL_STAT_IDS;
		for (int i = 0; i < scores.length; i++) {
			values.clear();
			values.put(Stats.SCOREBOARD_ID, scores[i].id);
			if (allStats[i] < 10) {//only retain previous "all time" statistics
				values.put(Stats.VALUE, scores[i].value);
				if (scores[i].value > 0)
					shouldUpdateAchievements = true;
			}
			mDb.update(Stats.TABLE, values, 
					Stats.GROUP_ID + "=" + 0 + " AND " +
					Stats.STATISTIC_ID + "=" + allStats[i], null);
		}
		
		if (shouldUpdateAchievements)
			updateAchievements().close();
	}
	
	// Updates solo statistics with scoreboard IDs from the server
	public void updateSoloScoreboardIds(Integer[] scoreIds) {
		ContentValues values = new ContentValues();
		int[] allStats = Stats.ALL_STAT_IDS;
		for (int i = 0; i < scoreIds.length; i++) {
			values.clear();
			values.put(Stats.SCOREBOARD_ID, scoreIds[i]);
			mDb.update(Stats.TABLE, values, 
					Stats.GROUP_ID + "=" + 0 + " AND " +
					Stats.STATISTIC_ID + "=" + allStats[i], null);
		}
	}
	
	public void insertScoreboards(ScoreBoard[] scores) {
		mDb.beginTransaction();
		try {
			for (int i = 0; i < scores.length; i++) {
				ContentValues values = new ContentValues();
				values.put(Stats.STATISTIC_ID, Integer.parseInt(scores[i].sb_type));
				values.put(Stats.SCOREBOARD_ID, scores[i].id);
				values.put(Stats.GROUP_ID, scores[i].group_id);
				values.put(Stats.VALUE, scores[i].value);
				mDb.insert(Stats.TABLE, null, values);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
		
		//Cursor cursor = mDb.rawQuery("SELECT * FROM statistics", null);
		//DatabaseUtils.dumpCursor(cursor);
		//cursor.close();
	}
	
	public void updateScoreboards(ScoreBoard[] scores) {
		mDb.beginTransaction();
		try {
			for (int i = 0; i < scores.length; i++) {
				ContentValues values = new ContentValues();
				values.put(Stats.VALUE, scores[i].value);
				mDb.update(Stats.TABLE, values, Stats.SCOREBOARD_ID + "=" + scores[i].id, null);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
		
		//Cursor cursor = mDb.rawQuery("SELECT * FROM statistics", null);
		//DatabaseUtils.dumpCursor(cursor);
		//cursor.close();
	}
	
	/**
	 * Achievements Methods
	 */
	
	public Cursor updateAchievements() {
		Cursor cursor = null;
		
		/**
		 * SELECT achievements.* FROM achievements, statistics 
		 * WHERE achievements.statistic_id=statistics.statistic_id
		 * AND statistics.group_id=0
		 * AND achievements.is_group=0;
		 * AND ((statistics.value >= achievements.condition AND achievements.completed = 0)
		 * OR (statistics.value < achievements.condition AND achievements.completed = 1))
		 * UNION
		 * SELECT achievements.* FROM achievements LEFT JOIN 
		 * (SELECT statistic_id, max(value) as max_value FROM statistics 
		 * WHERE group_id>0 GROUP BY statistic_id) group_stats 
		 * ON achievements.statistic_id=group_stats.statistic_id WHERE achievements.is_group=1 
		 * AND ((group_stats.max_value >= achievements.condition AND achievements.completed = 0)
		 * OR (group_stats.max_value < achievements.condition AND achievements.completed = 1))
		 */
		
		String selectClause = Achievements.TABLE + ".*";
		String tables = Achievements.TABLE + ", " + Stats.TABLE;
		String whereClause1 = 
			Achievements.TABLE + "." + Achievements.STATISTIC_ID + "=" + 
			Stats.TABLE + "." + Stats.STATISTIC_ID + " AND " +
			Stats.TABLE + "." + Stats.GROUP_ID + "=0 AND " +
			Achievements.TABLE + "." + Achievements.IS_GROUP + "=0 AND" +
			"((" + Stats.TABLE + "." + Stats.VALUE + ">=" + 
			Achievements.TABLE + "." + Achievements.CONDITION + " AND " +
			Achievements.TABLE + "." + Achievements.COMPLETED + "=0" + ") OR (" +
			Stats.TABLE + "." + Stats.VALUE + "<" + 
			Achievements.TABLE + "." + Achievements.CONDITION + " AND " +
			Achievements.TABLE + "." + Achievements.COMPLETED + "=1" + "))";
		
		String innerQuery = "SELECT " + Stats.STATISTIC_ID + 
			", max(" + Stats.VALUE + ") as max_value FROM " + Stats.TABLE + " WHERE " +
			Stats.GROUP_ID + ">0 GROUP BY " + Stats.STATISTIC_ID;
		String tables2 = Achievements.TABLE + " LEFT JOIN (" + innerQuery + ") group_stats";
		String joinCondition = Achievements.TABLE + "." + Achievements.STATISTIC_ID + "=" + 
			"group_stats." + Stats.STATISTIC_ID;
		String whereClause2 = Achievements.TABLE + "." + Achievements.IS_GROUP + "=1 AND" +
			"((group_stats.max_value>=" + Achievements.TABLE + "." + Achievements.CONDITION + " AND " +
			Achievements.TABLE + "." + Achievements.COMPLETED + "=0" + ") OR (" +
			"group_stats.max_value<" + Achievements.TABLE + "." + Achievements.CONDITION + " AND " +
			Achievements.TABLE + "." + Achievements.COMPLETED + "=1" + "))";
		
		cursor = mDb.rawQuery("SELECT " + selectClause + " FROM " + tables + " WHERE " + 
				whereClause1 + " UNION SELECT " + selectClause + " FROM " +
				tables2 + " ON " + joinCondition + " WHERE " + whereClause2, null);
		
		/**
		 * UPDATE achievements SET completed=1, updated_at=current_time WHERE
		 * _id in (1,2...)
		 */
		int i = 0;
		String updateEarnedWhereClause = "";
		while (cursor.moveToNext()) {
			if (cursor.getInt(4) == 1) continue;
			if (i == 0)
				updateEarnedWhereClause += cursor.getString(0);
			else
				updateEarnedWhereClause += "," + cursor.getString(0);
			i++;
		}
		
		if (updateEarnedWhereClause != "") {
			Log.d(TAG, "updateEarnedWhereClause = " + updateEarnedWhereClause);
			ContentValues updateValues = new ContentValues();
			updateValues.put(Achievements.COMPLETED, 1);
			updateValues.put(Achievements.UPDATED_AT, System.currentTimeMillis());
			int result = mDb.update(Achievements.TABLE, updateValues, 
					Achievements._ID + " IN (" + updateEarnedWhereClause + ")", null);
			Log.d(TAG, "num rows updated from updateEarned = " + result);
		}
		
		/**
		 * UPDATE achievements SET completed=0, updated_at=current_time WHERE
		 * _id in (1,2...)
		 */
		cursor.moveToPosition(-1);
		i = 0;
		String updateLostWhereClause = "";
		while (cursor.moveToNext()) {
			if (cursor.getInt(4) == 0) continue;
			if (i == 0)
				updateLostWhereClause += cursor.getString(0);
			else
				updateLostWhereClause += "," + cursor.getString(0);
			i++;
		}
		
		if (updateLostWhereClause != "") {
			Log.d(TAG, "updateLostWhereClause = " + updateLostWhereClause);
			ContentValues updateValues = new ContentValues();
			updateValues.put(Achievements.COMPLETED, 0);
			updateValues.put(Achievements.UPDATED_AT, System.currentTimeMillis());
			int result = mDb.update(Achievements.TABLE, updateValues, 
					Achievements._ID + " IN (" + updateLostWhereClause + ")", null);
			Log.d(TAG, "num rows updated from updateLost = " + result);
		}
		
		Log.d(TAG, "dumping new achievements");
		//DatabaseUtils.dumpCursor(cursor);
		cursor.moveToPosition(-1);
		return cursor;
	}
	
	private Cursor getRecentAchievements(int conditionVal) {
		long timeThreshold = System.currentTimeMillis() - Achievements.RECENT_INTERVAL;
		Cursor cursor = mDb.query(Achievements.TABLE, Achievements.COLUMNS, 
				Achievements.UPDATED_AT + ">=" + timeThreshold + " AND " + Achievements.COMPLETED + "=" + conditionVal, null, null, null, Achievements.UPDATED_AT + " desc");
		
		return cursor;
	}
	
	public Cursor getRecentAchievementsEarned() {
		return getRecentAchievements(1);
	}
	
	public Cursor getRecentAchievementsLost() {
		return getRecentAchievements(0);
	}
	
	public Cursor getAchievementsInCat(int cat, int bitmask) {
		Cursor cursor = mDb.query(Achievements.TABLE, Achievements.COLUMNS, 
				Achievements.CATEGORY + "&" + bitmask + "=" + cat, null, null, null, null);
		
		return cursor;
	}
	
	public int getAchievementCountForCat(int cat, int bitmask) {
		Cursor cursor = mDb.query(Achievements.TABLE, Achievements.COLUMNS, 
				Achievements.CATEGORY + "&" + bitmask + "=" + cat, null, null, null, null);
		
		int count = cursor.getCount();
		cursor.close();
		
		return count;
	}
	
	public int getCompletedAchievementCountForCat(int cat, int bitmask) {
		Cursor cursor = mDb.query(Achievements.TABLE, Achievements.COLUMNS, 
				Achievements.CATEGORY + "&" + bitmask + "=" + cat + " AND " + Achievements.COMPLETED + "=1", null, null, null, null);
		
		int count = cursor.getCount();
		cursor.close();
		
		return count;
	}
	
	/**
	 * SELECT category, count(_id) & 15 AS cat FROM achievements GROUP BY cat UNION
	 * SELECT category, count(_id) & 240 AS cat FROM achievements GROUP BY cat ORDER BY cat
	 */
	
	public Cursor getAchievementCounts() {
		Cursor cursor = mDb.rawQuery("SELECT " + Achievements.CATEGORY + ", " +
				"count(" + Achievements._ID + ") & 15 AS cat FROM " + Achievements.TABLE +
				" GROUP BY cat UNION SELECT " + Achievements.CATEGORY + ", " +
				"count(" + Achievements._ID + ") & 240 AS cat FROM " + Achievements.TABLE +
				" GROUP BY cat ORDER BY cat", null);
		return cursor;
	}
	
	/**
	 * SELECT category, count(_id) & 15 AS cat FROM achievements WHERE completed=0 GROUP BY cat UNION
	 * SELECT category, count(_id) & 240 AS cat FROM achievements WHERE completed=0 GROUP BY cat ORDER BY cat
	 */
	
	public Cursor getUncompletedAchievementCounts() {
		Cursor cursor = mDb.rawQuery("SELECT " + Achievements.CATEGORY + ", " +
				"count(" + Achievements._ID + ") & 15 AS cat FROM " + Achievements.TABLE +
				" WHERE " + Achievements.COMPLETED + "=0 GROUP BY cat UNION SELECT " + Achievements.CATEGORY + ", " +
				"count(" + Achievements._ID + ") & 240 AS cat FROM " + Achievements.TABLE +
				" WHERE " + Achievements.COMPLETED + "=0 GROUP BY cat ORDER BY cat", null);
		return cursor;
	}
	
	/**
	 * Groups Methods
	 */
	
	//Get all groups along with the # of members
	public Cursor getGroups() {
		/**
		 * SELECT groups._id, groups.group_id, groups.name, ifnull(subtotal.count,0) 
		 * FROM groups LEFT JOIN 
		 * (SELECT groups.*, count(groups_users._id) AS count FROM groups, groups_users 
		 * WHERE groups.group_id=groups_users.group_id GROUP BY groups.group_id) subtotal ON groups.group_id=subtotal.group_id;		
		 */
		
		String selectClause = 
			Groups.TABLE + "." + Groups._ID + " AS _id, " + 
			Groups.TABLE + "." + Groups.GROUP_ID + ", " + 
			Groups.TABLE + "." + Groups.NAME + ", " + 
			"ifnull(subtotal.count,0)";
		String outerCondition = Groups.TABLE + "." + Groups.GROUP_ID + "=subtotal.group_id";
		
		String innerSelectClause = Groups.TABLE + ".*, count(" + 
			GroupsUsers.TABLE + "." + GroupsUsers._ID + ") AS count";
		String innerTables = Groups.TABLE + ", " + GroupsUsers.TABLE;
		String innerWhereClause = Groups.TABLE + "." + Groups.GROUP_ID + "=" + 
			GroupsUsers.TABLE + "." + GroupsUsers.GROUP_ID + " GROUP BY " +
			Groups.TABLE + "." + Groups.GROUP_ID;
		String innerQuery = "SELECT " + innerSelectClause + " FROM " + innerTables + 
			" WHERE " + innerWhereClause;
		
		Cursor cursor = mDb.rawQuery("SELECT " + selectClause + " FROM " + Groups.TABLE + 
				" LEFT JOIN (" + innerQuery + ") subtotal ON " + outerCondition, null);
		return cursor;
	}
	
	public boolean addGroup(long groupIdServer, String groupName, long isOwner) {
		ContentValues values = new ContentValues();
		values.put(Groups.GROUP_ID, groupIdServer);
		values.put(Groups.NAME, groupName);
		values.put(Groups.IS_OWNER, isOwner);
		return mDb.insert(Groups.TABLE, null, values) > 0;
	}
	
	public Cursor getGroupWithId(long groupId) {
		Cursor cursor = mDb.query(Groups.TABLE, 
				new String[] {Groups._ID, Groups.GROUP_ID, Groups.NAME, Groups.IS_OWNER}, 
				Groups.GROUP_ID + "=" + groupId, null, null, null, null);
		return cursor;
	}
	
	public Cursor getUsersForGroup(long groupId) {
		/**
		 * SELECT users.* FROM users, groups_users WHERE users.user_id=groups_users.user_id
		 * AND groups_users.group_id=groupId ORDER BY users.last_name, users.first_name
		 */
		String tables = Users.TABLE + ", " + GroupsUsers.TABLE;
		String whereClause = Users.TABLE + "." + Users.USER_ID + "=" +
			GroupsUsers.TABLE + "." + GroupsUsers.USER_ID + 
			" AND " + GroupsUsers.TABLE + "." + GroupsUsers.GROUP_ID + "=" + groupId;
		Cursor cursor = mDb.rawQuery("SELECT " + Users.TABLE + ".* FROM " + tables + 
				" WHERE " + whereClause + " ORDER BY " + Users.TABLE + "." + Users.LAST_NAME + "," + 
				Users.TABLE + "." + Users.FIRST_NAME, null);
		return cursor;
	}
	
	public Cursor getUsersForUserIds(long[] userIds) {
		if (userIds == null) return null;
		/**
		 * SELECT users.* FROM users WHERE users.user_id IN (1,2,...)
		 */
		String idString = "";
		for (int i = 0; i < userIds.length; i++) {
			if (i == 0) 
				idString += userIds[i];
			else 
				idString += "," + userIds[i];
		}
		String whereClause = Users.TABLE + "." + Users.USER_ID + 
			" IN (" + idString + ")";
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + Users.TABLE + 
				" WHERE " + whereClause, null);
		return cursor;
	}
	
	public User[] getUserArrayForUserCursor(Cursor userCursor) {
		if (userCursor == null) return null;
		else {
			if (userCursor.getCount() == 0) {
				userCursor.close();
				return null;
			}
			else {
				User[] users = new User[userCursor.getCount()];
				int pos = 0;
				while(userCursor.moveToNext()) {
					User newUser = new User();
					newUser.id = userCursor.getInt(1);
					newUser.fb_id = userCursor.getInt(2);
					newUser.first_name = userCursor.getString(3);
					newUser.last_name = userCursor.getString(4);
					newUser.fb_photo = userCursor.getString(5);
					users[pos] = newUser;
					pos++;
				}
				userCursor.close();
				return users;
				
			}
		}
	}
	
	public User[] getUserArrayForUserIds(long[] userIds) {
		Cursor cursor = getUsersForUserIds(userIds);
		User[] userArray = getUserArrayForUserCursor(cursor);
		cursor.close();
		return userArray;
	}
	
	public Cursor getAllUsersExcludingGroup(long groupId, int selfId) {
		/**
		 * SELECT DISTINCT users.* FROM users, groups_users WHERE users.user_id=groups_users.user_id
		 * AND users.user_id NOT IN 
		 * (SELECT groups_users.user_id FROM groups_users WHERE groups_users.group_id=groupId)
		 * AND users.user_id <> selfId
		 * UNION
		 * SELECT users.* FROM users WHERE users.user_id NOT IN 
		 * (SELECT groups_users.user_id FROM groups_users)
		 * AND users.user_id <> selfId
		 * ORDER BY users.last_name, users.first_name
		 */
		String subQuery = "(SELECT " + GroupsUsers.TABLE + "." + GroupsUsers.USER_ID + " FROM " + 
			GroupsUsers.TABLE + " WHERE " + 
			GroupsUsers.TABLE + "." + GroupsUsers.GROUP_ID + "=" + groupId + ")";
		
		String tables = Users.TABLE + ", " + GroupsUsers.TABLE;
		String whereClause = Users.TABLE + "." + Users.USER_ID + "=" +
			GroupsUsers.TABLE + "." + GroupsUsers.USER_ID + " AND " + 
			Users.TABLE + "." + Users.USER_ID + " NOT IN " + subQuery + " AND " +
			Users.TABLE + "." + Users.USER_ID + "<>" + selfId;
		String subQuery2 = "(SELECT " + GroupsUsers.TABLE + "." + GroupsUsers.USER_ID + " FROM " + 
			GroupsUsers.TABLE + ")";
		String whereClause2 = Users.TABLE + "." + Users.USER_ID + " NOT IN " + subQuery2 + " AND " +
			Users.TABLE + "." + Users.USER_ID + "<>" + selfId;
		Cursor cursor = mDb.rawQuery("SELECT DISTINCT " + Users.TABLE + ".* FROM " + tables + 
				" WHERE " + whereClause + " UNION" +
				" SELECT " + Users.TABLE + ".* FROM " + Users.TABLE + " WHERE " + whereClause2 +
				" ORDER BY " + Users.TABLE + "." + Users.LAST_NAME + "," + 
				Users.TABLE + "." + Users.FIRST_NAME, null);
		return cursor;
	}
	
	public void addUsersToGroup(long groupId, long[] users) {
		mDb.beginTransaction();
		try {
			for (int i = 0; i < users.length; i++) {
				ContentValues values = new ContentValues();
				values.put(GroupsUsers.GROUP_ID, groupId);
				values.put(GroupsUsers.USER_ID, users[i]);
				mDb.insert(GroupsUsers.TABLE, null, values);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}
	
	//Insert all groups and group_users and let the DB constraints
	// filter out duplicates
	public ArrayList<Group> updateGroups(Group[] groups, int selfId) {
		if (groups == null || groups.length == 0) return null;
		
		ArrayList<Group> newGroups = new ArrayList<Group>();
		User[] users;
		mDb.beginTransaction();
		try {
			for (int i = 0; i < groups.length; i++) {
				long isOwner = groups[i].owner_id == selfId ? 1 : 0;
				if (addGroup(groups[i].id, groups[i].name, isOwner))
					newGroups.add(groups[i]);
				
				users = groups[i].users;
				long[] userIds = new long[users.length];
				for (int j = 0; j < users.length; j++)
					userIds[j] = users[j].id;
				addUsersToGroup(groups[i].id, userIds);
				addUsers(groups[i].users);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
		
		//Cursor cursor = mDb.rawQuery("SELECT * FROM groups_users", null);
		//DatabaseUtils.dumpCursor(cursor);
		//cursor.close();
		
		return newGroups;
	}
	
	/**
	 * GameMessage Methods
	 */
	
	public Cursor getMessageWithId(long gmId) {
		String tables = GameMessages.TABLE + ", " + Users.TABLE;
		String whereClause = GameMessages.TABLE + "." + GameMessages.FROM_USER + "=" +
			Users.TABLE + "." + Users.USER_ID + " AND " +
			GameMessages.TABLE + "." + GameMessages._ID + "=" + gmId;
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + tables + " WHERE " + whereClause, null);
		return cursor;
	}
	
	public Cursor getAllMessages() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + GameMessages.TABLE + 
				" ORDER BY " + GameMessages.SEND_TIME + " DESC", null);
		return cursor;
	}
	
	public Cursor getAllMessagesWithFromUsers() {
		String tables = GameMessages.TABLE + ", " + Users.TABLE;
		String whereClause = GameMessages.TABLE + "." + GameMessages.FROM_USER + "=" +
			Users.TABLE + "." + Users.USER_ID;
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + tables + " WHERE " + whereClause + 
				" ORDER BY " + GameMessages.TABLE + "." + GameMessages.SEND_TIME + " DESC", null);
		return cursor;
	}
	
	public Cursor getMessagesWithFromUsers(boolean isBcast) {
		String tables = GameMessages.TABLE + ", " + Users.TABLE;
		String whereClause = GameMessages.TABLE + "." + GameMessages.FROM_USER + "=" +
			Users.TABLE + "." + Users.USER_ID + " AND " +
			GameMessages.TABLE + "." + GameMessages.IS_BCAST + "=" + (isBcast ? 1 : 0);
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + tables + " WHERE " + whereClause + 
				" ORDER BY " + GameMessages.TABLE + "." + GameMessages.SEND_TIME + " DESC", null);
		return cursor;
	}
	/*
	public Cursor getMessagesWithFromUsers(int type) {
		String tables = GameMessages.TABLE + ", " + Users.TABLE;
		String whereClause = GameMessages.TABLE + "." + GameMessages.FROM_USER + "=" +
			Users.TABLE + "." + Users.USER_ID + " AND " + 
			GameMessages.TABLE + "." + GameMessages.TYPE + "=" + type;
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + tables + " WHERE " + whereClause + 
				" ORDER BY " + GameMessages.TABLE + "." + GameMessages.SEND_TIME + " DESC", null);
		return cursor;
	}
	*/
	public Cursor getUsersForMessage(long gmId, long senderId) {
		/**
		 * SELECT * FROM users WHERE user_id=senderId UNION
		 * SELECT users.* FROM gm_recipients, users WHERE 
		 * gm_recipients.user_id=users.user_id AND gm_recipients.gm_id=gmId
		 */
		String query1 = "SELECT * FROM " + Users.TABLE + " WHERE " + Users.USER_ID + "=" + senderId;
		
		String tables = GMRecipients.TABLE + "," + Users.TABLE;
		String whereClause = GMRecipients.TABLE + "." + GMRecipients.USER_ID + "=" + 
			Users.TABLE + "." + Users.USER_ID + " AND " +
			GMRecipients.TABLE + "." + GMRecipients.GM_ID + "=" + gmId;
		String query2 = "SELECT " + Users.TABLE + ".* FROM " + tables + " WHERE " + whereClause;
		
		Cursor cursor = mDb.rawQuery(query1 + " UNION " + query2, null);
		return cursor;
	}
	
	public void insertGameMessage(User fromUser, User[] recipients,	
			long sendTime, MessageObject msgObject) {
		//Insert unseen users
		addUser(fromUser);
		if (recipients != null) {
			mDb.beginTransaction();
			try {
				for (int i = 0; i < recipients.length; i++) {
					addUser(recipients[i]);
				}
				mDb.setTransactionSuccessful();
			} finally {
				mDb.endTransaction();
			}
		}
		
		//Insert GameMessage
		ContentValues msgValues = new ContentValues();
		msgValues.put(GameMessages.FROM_USER, fromUser.id);
		msgValues.put(GameMessages.TYPE, msgObject.mType);
		msgValues.put(GameMessages.SEND_TIME, sendTime);
		msgValues.put(GameMessages.ORIG_SEND_TIME, msgObject.mOrigSendTime);
		msgValues.put(GameMessages.SUBJECT, msgObject.mSubject);
		msgValues.put(GameMessages.BODY, msgObject.mBody);
		if (recipients != null)
			msgValues.put(GameMessages.IS_BCAST, 0);
		else
			msgValues.put(GameMessages.IS_BCAST, 1);
		long messageId = mDb.insert(GameMessages.TABLE, null, msgValues);
		
		if (messageId == -1) return;
		
		//Insert GMRecipients
		if (recipients != null) {
			mDb.beginTransaction();
			try {
				for (int i = 0; i < recipients.length; i++) {
					ContentValues rValues = new ContentValues();
					rValues.put(GMRecipients.GM_ID, messageId);
					rValues.put(GMRecipients.USER_ID, recipients[i].id);
					mDb.insert(GMRecipients.TABLE, null, rValues);
				}
				mDb.setTransactionSuccessful();
			} finally {
				mDb.endTransaction();
			}
		}
	}
	
	//Sets the did_start attribute to 1 for a specific message
	public boolean setMessageToStarted(long msgId) {
		ContentValues values = new ContentValues();
		values.put(GameMessages.DID_START, 1);
		return mDb.update(GameMessages.TABLE, values, GameMessages._ID + "=" + msgId, null) == 1;
	}
	
	public boolean addUser(User user) {
		ContentValues values = new ContentValues();
		values.put(Users.USER_ID, user.id);
		values.put(Users.FB_ID, user.fb_id);
		values.put(Users.FIRST_NAME, user.first_name);
		values.put(Users.LAST_NAME, user.last_name);
		values.put(Users.IMG_URL, user.fb_photo);
		return mDb.insert(Users.TABLE, null, values) > 0;
	}
	
	public void addFriend(User user) {
		ContentValues values = new ContentValues();
		values.put(Users.USER_ID, user.id);
		values.put(Users.FB_ID, user.fb_id);
		values.put(Users.FIRST_NAME, user.first_name);
		values.put(Users.LAST_NAME, user.last_name);
		values.put(Users.IMG_URL, user.fb_photo);
		values.put(Users.IS_FRIEND, 1);
		if (mDb.insert(Users.TABLE, null, values) == 0) {
			mDb.update(Users.TABLE, values, Users.USER_ID + "=" + user.id, null);
		}
	}
	
	//Add all users to user table and let table constraints handle duplicates
	public void addUsers(User[] users) {
		mDb.beginTransaction();
		try {
			for (int i = 0; i < users.length; i++)
				addUser(users[i]);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
		
		//Cursor cursor = mDb.rawQuery("SELECT * FROM users", null);
		//DatabaseUtils.dumpCursor(cursor);
		//cursor.close();
	}
	
	public void addFriends(User[] users) {
		mDb.beginTransaction();
		try {
			for (int i = 0; i < users.length; i++)
				addFriend(users[i]);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
		
		//Cursor cursor = mDb.rawQuery("SELECT * FROM users WHERE is_friend=1", null);
		//DatabaseUtils.dumpCursor(cursor);
		//cursor.close();
	}
	
	public Cursor getAllUsers() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + Users.TABLE + " WHERE " +
				Users.USER_ID + "<>" + Common.getRegisteredUser(mContext).id +
				" ORDER BY " + Users.TABLE + "." + Users.LAST_NAME + "," + 
				Users.TABLE + "." + Users.FIRST_NAME, null);
		return cursor;
	}
	
	public void addGroupsTemp(Group[] groups) {
		mDb.beginTransaction();
		try {
			for (int i = 0; i < groups.length; i++) {
				ContentValues values = new ContentValues();
				values.put(GroupsTemp.GROUP_ID, groups[i].id);
				values.put(GroupsTemp.NAME, groups[i].name);
				mDb.insert(GroupsTemp.TABLE, null, values);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}
	
	/**
	 * ScoreBoard Methods
	 */
	
	public void fillScoreBoardTemp(ScoreBoard[] scores, boolean groupFlag) {
		String whereClause;
		if (groupFlag)
			whereClause = ScoreboardTemp.USER_ID + "=0";
		else
			whereClause = ScoreboardTemp.GROUP_ID + "=0";
		mDb.delete(ScoreboardTemp.TABLE, whereClause, null);
		
		mDb.beginTransaction();
		try {
			for (int i = 0; i < scores.length; i++) {
				ContentValues values = new ContentValues();
				values.put(ScoreboardTemp.USER_ID, scores[i].user_id);
				values.put(ScoreboardTemp.GROUP_ID, scores[i].group_id);
				values.put(ScoreboardTemp.VALUE, scores[i].value);
				values.put(ScoreboardTemp.TYPE, scores[i].sb_type);
				mDb.insert(ScoreboardTemp.TABLE, null, values);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
		
		//Cursor cursor = mDb.rawQuery("SELECT * FROM scoreboard", null);
		//DatabaseUtils.dumpCursor(cursor);
		//cursor.close();
	}
	
	public Cursor getScoresWithUsers(int statisticId) {
		String tables = ScoreboardTemp.TABLE + "," + Users.TABLE;
		String whereClause = ScoreboardTemp.TABLE + "." + ScoreboardTemp.USER_ID + "=" +
			Users.TABLE + "." + Users.USER_ID + " AND " + 
			ScoreboardTemp.TABLE + "." + ScoreboardTemp.TYPE + "=" + statisticId + " AND " +
			ScoreboardTemp.TABLE + "." + ScoreboardTemp.VALUE + ">0";
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + tables + " WHERE " + whereClause +
				" ORDER BY " + ScoreboardTemp.TABLE + "." + ScoreboardTemp.VALUE + " DESC", null);
		return cursor;
	}
	
	public Cursor getScoresWithGroups(int statisticId) {
		String tables = ScoreboardTemp.TABLE + "," + GroupsTemp.TABLE;
		String whereClause = ScoreboardTemp.TABLE + "." + ScoreboardTemp.GROUP_ID + "=" +
			GroupsTemp.TABLE + "." + GroupsTemp.GROUP_ID + " AND " + 
			ScoreboardTemp.TABLE + "." + ScoreboardTemp.TYPE + "=" + statisticId + " AND " +
			ScoreboardTemp.TABLE + "." + ScoreboardTemp.VALUE + ">0";
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + tables + " WHERE " + whereClause +
				" ORDER BY " + ScoreboardTemp.TABLE + "." + ScoreboardTemp.VALUE + " DESC", null);
		return cursor;
	}
}
