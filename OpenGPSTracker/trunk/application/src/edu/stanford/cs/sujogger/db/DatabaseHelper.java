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
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import edu.stanford.cs.sujogger.db.GPStracking.Achievements;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.GroupsUsers;
import edu.stanford.cs.sujogger.db.GPStracking.Media;
import edu.stanford.cs.sujogger.db.GPStracking.MediaColumns;
import edu.stanford.cs.sujogger.db.GPStracking.Segments;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.db.GPStracking.TracksColumns;
import edu.stanford.cs.sujogger.db.GPStracking.Users;
import edu.stanford.cs.sujogger.db.GPStracking.Waypoints;
import edu.stanford.cs.sujogger.db.GPStracking.WaypointsColumns;

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
	
	public long getStatisticLong(long statisticId) {
		return (long) getStatisticReal(statisticId);
	}
	
	public double getStatisticReal(long statisticId) {
		double statVal = 0;
		Cursor cursor = mDb.query(true, Stats.TABLE, new String[] {Stats.VALUE}, 
				Stats._ID + "=" + statisticId, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
			statVal = cursor.getDouble(0);
		
		cursor.close();
		return statVal;
	}
	
	public boolean setStatistic(long statisticId, double val) {		
		ContentValues args = new ContentValues();
		args.put(Stats.VALUE, val);
		
		return mDb.update(Stats.TABLE, args, Stats._ID + "=" + statisticId, null) > 0;
	}
	
	public void increaseStatistic(long statisticId, double val) {		
		ContentValues args = new ContentValues();
		args.put(Stats.VALUE, val);

		mDb.execSQL("UPDATE " + Stats.TABLE + " SET " + Stats.VALUE + " = " + Stats.VALUE + 
				" + ? WHERE " + Stats._ID + " = ?", 
				new Object[] {new Double(val), new Long(statisticId)});
	}
	
	public void increaseStatisticByOne(long statisticId) {
		increaseStatistic(statisticId, 1);
	}
	
	// Updates distances run in the past week / month
	public void updateDistanceRan() {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DISTANCE + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.DISTANCE_RAN_WEEK_ID, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DISTANCE + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.DISTANCE_RAN_MONTH_ID, cursor.getDouble(0));
		cursor.close();
	}
	
	// Updates total running time in the past week / month
	public void updateRunningTime() {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DURATION + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.RUNNING_TIME_WEEK_ID, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"sum(" + Tracks.DURATION + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.RUNNING_TIME_MONTH_ID, cursor.getDouble(0));
		cursor.close();
	}
	
	// Updates number of runs in the past week / month
	public void updateNumRuns() {
		long timeWeekThreshold = System.currentTimeMillis() - Stats.WEEK_INTERVAL;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {"count(" + Tracks._ID + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeWeekThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.NUM_RUNS_WEEK_ID, cursor.getDouble(0));
		cursor.close();
		
		long timeMonthThreshold = System.currentTimeMillis() - Stats.MONTH_INTERVAL;
		cursor = mDb.query(Tracks.TABLE, new String[] {"count(" + Tracks._ID + ")"}, 
				Tracks.CREATION_TIME + ">=" + timeMonthThreshold, null, null, null, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst())
			setStatistic(Stats.NUM_RUNS_MONTH_ID, cursor.getDouble(0));
		cursor.close();
	}
	
	public void updateAvgSpeed() {
		double totalDist = getStatisticReal(Stats.DISTANCE_RAN_ID);
		double totalTime = getStatisticReal(Stats.RUNNING_TIME_ID);
		if (totalTime != 0)
			setStatistic(Stats.AVG_SPEED_ID, totalDist / totalTime);
		
		double totalDistWeek = getStatisticReal(Stats.DISTANCE_RAN_WEEK_ID);
		double totalTimeWeek = getStatisticReal(Stats.RUNNING_TIME_WEEK_ID);
		if (totalTime != 0)
			setStatistic(Stats.AVG_SPEED_WEEK_ID, totalDistWeek / totalTimeWeek);
		
		double totalDistMonth = getStatisticReal(Stats.DISTANCE_RAN_MONTH_ID);
		double totalTimeMonth = getStatisticReal(Stats.RUNNING_TIME_MONTH_ID);
		if (totalTime != 0)
			setStatistic(Stats.AVG_SPEED_MONTH_ID, totalDistMonth / totalTimeMonth);
	}
	
	public void updateMedDuration() {
		long numTracks = getStatisticLong(Stats.NUM_RUNS_ID);
		long queryLimit = numTracks > 4 ? numTracks / 4 : numTracks;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {Tracks.DURATION}, 
				null, null, null, null, Tracks.DURATION, String.valueOf(queryLimit));
		
		int queryCount = cursor.getCount();
		if (queryCount > 0) {
			cursor.moveToPosition(queryCount / 2);
			setStatistic(Stats.MED_DURATION_ID, cursor.getLong(0));
		}
		
		cursor.close();
	}
	
	public void updateMedDistance() {
		long numTracks = getStatisticLong(Stats.NUM_RUNS_ID);
		long queryLimit = numTracks > 4 ? numTracks / 4 : numTracks;
		Cursor cursor = mDb.query(Tracks.TABLE, new String[] {Tracks.DISTANCE}, 
				null, null, null, null, Tracks.DISTANCE, String.valueOf(queryLimit));
		
		int queryCount = cursor.getCount();
		if (queryCount > 0) {
			cursor.moveToPosition(queryCount / 2);
			setStatistic(Stats.MED_DURATION_ID, cursor.getDouble(0));
		}
		
		cursor.close();
	}
	
	/**
	 * Achievements Methods
	 */
	
	public Cursor updateAchievements() {
		Cursor cursor = null;
		
		/**
		 * SELECT achievements._id, achievements.completed FROM achievements, statistics 
		 * WHERE achievements.statistic_id=statistics._id
		 * AND ((statistics.value >= achievements.condition AND achievements.completed = 0)
		 * OR (statistics.value <= achievements.condition AND achievements.completed = 1))
		 */
		
		String selectClause = Achievements.TABLE + "." + Achievements._ID;
		String tables = Achievements.TABLE + ", " + Stats.TABLE;
		String whereClause = Achievements.TABLE + "." + Achievements.STATISTIC_ID + "=" + 
			Stats.TABLE + "." + Stats._ID + " AND " + 
			"((" + Stats.TABLE + "." + Stats.VALUE + ">=" + 
			Achievements.TABLE + "." + Achievements.CONDITION + " AND " +
			Achievements.TABLE + "." + Achievements.COMPLETED + "=0" + ") OR (" +
			Stats.TABLE + "." + Stats.VALUE + "<=" + 
			Achievements.TABLE + "." + Achievements.CONDITION + " AND " +
			Achievements.TABLE + "." + Achievements.COMPLETED + "=1" + "))";
		
		cursor = mDb.rawQuery("SELECT " + selectClause + " FROM " + tables + " WHERE " + 
				whereClause, null);
		
		/**
		 * UPDATE achievements SET completed=1, updated_at=current_time WHERE
		 * _id = 1 OR _id...
		 */
		int i = 0;
		String updateWhereClause = "";
		while (cursor.moveToNext()) {
			if (i == 0)
				updateWhereClause += Achievements._ID + "=" + cursor.getString(0);
			else
				updateWhereClause += " OR " + Achievements._ID + "=" + cursor.getString(0);
			i++;
		}
		
		if (updateWhereClause != "") {
			Log.d(TAG, "whereClause = " + updateWhereClause);
			ContentValues updateValues = new ContentValues();
			updateValues.put(Achievements.COMPLETED, 1);
			updateValues.put(Achievements.UPDATED_AT, System.currentTimeMillis());
			
			mDb.update(Achievements.TABLE, updateValues, updateWhereClause, null);
		}
		
		DatabaseUtils.dumpCursor(cursor);
		cursor.moveToPosition(-1);
		return cursor;
	}
	
	private Cursor getRecentAchievements(int conditionVal) {
		long timeThreshold = System.currentTimeMillis() - Achievements.RECENT_INTERVAL;
		Cursor cursor = mDb.query(Achievements.TABLE, new String[] {Achievements._ID, Achievements.GROUP_ID, Achievements.COMPLETED, Achievements.CATEGORY, Achievements.ICON_RESOURCE}, 
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
		Cursor cursor = mDb.query(Achievements.TABLE, 
				new String[] {Achievements._ID, Achievements.GROUP_ID, Achievements.COMPLETED, Achievements.CATEGORY, Achievements.ICON_RESOURCE}, 
				Achievements.CATEGORY + "&" + bitmask + "=" + cat, null, null, null, null);
		
		return cursor;
	}
	
	public int getAchievementCountForCat(int cat, int bitmask) {
		Cursor cursor = mDb.query(Achievements.TABLE, 
				new String[] {Achievements._ID, Achievements.GROUP_ID, Achievements.COMPLETED, Achievements.CATEGORY}, 
				Achievements.CATEGORY + "&" + bitmask + "=" + cat, null, null, null, null);
		
		int count = cursor.getCount();
		cursor.close();
		
		return count;
	}
	
	public int getCompletedAchievementCountForCat(int cat, int bitmask) {
		Cursor cursor = mDb.query(Achievements.TABLE, 
				new String[] {Achievements._ID, Achievements.GROUP_ID, Achievements.COMPLETED, Achievements.CATEGORY}, 
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
	
	public Cursor getGroups() {
		/**
		 * SELECT groups._id, groups.group_id, groups.name, ifnull(subtotal.count,0) 
		 * FROM groups LEFT JOIN 
		 * (SELECT groups.*, count(groups_users._id) AS count FROM groups, groups_users 
		 * WHERE groups._id=groups_users.group_id) subtotal ON groups._id=subtotal._id;		
		 */
		
		String selectClause = 
			Groups.TABLE + "." + Groups._ID + " AS _id, " + 
			Groups.TABLE + "." + Groups.GROUP_ID + ", " + 
			Groups.TABLE + "." + Groups.NAME + ", " + 
			"ifnull(subtotal.count,0)";
		String outerCondition = Groups.TABLE + "." + Groups._ID + "=subtotal._id";
		
		String innerSelectClause = Groups.TABLE + 
			".*, count(" + GroupsUsers.TABLE + "." + GroupsUsers._ID + ") AS count";
		String innerTables = Groups.TABLE + ", " + GroupsUsers.TABLE;
		String innerWhereClause = Groups.TABLE + "." + Groups._ID + "=" + 
			GroupsUsers.TABLE + "." + GroupsUsers.GROUP_ID;
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
		return mDb.insert(Groups.TABLE, null, values) >= 0;
	}
	
	public Cursor getGroupWithId(long groupId) {
		Cursor cursor = mDb.query(Groups.TABLE, 
				new String[] {Groups._ID, Groups.GROUP_ID, Groups.NAME, Groups.IS_OWNER}, 
				Groups._ID + "=" + groupId, null, null, null, null);
		return cursor;
	}
	
	public Cursor getUsersForGroup(long groupId) {
		/**
		 * SELECT users.* FROM users, groups_users WHERE users._id=groups_users.user_id
		 * AND groups_users.group_id=groupId
		 */
		String tables = Users.TABLE + ", " + GroupsUsers.TABLE;
		String whereClause = Users.TABLE + "." + Users._ID + "=" +
			GroupsUsers.TABLE + "." + GroupsUsers.USER_ID + 
			" AND " + GroupsUsers.TABLE + "." + GroupsUsers.GROUP_ID + "=" + groupId;
		Cursor cursor = mDb.rawQuery("SELECT " + Users.TABLE + ".* FROM " + tables + 
				" WHERE " + whereClause, null);
		return cursor;
	}
	
	public Cursor getAllUsersExcludingGroup(long groupId) {
		/**
		 * SELECT DISTINCT users.* FROM users, groups_users WHERE users._id=groups_users.user_id
		 * AND users._id NOT IN 
		 * (SELECT groups_users.user_id FROM groups_users WHERE groups_users.group_id=groupId)
		 */
		String subQuery = "(SELECT " + GroupsUsers.TABLE + "." + GroupsUsers.USER_ID + " FROM " + 
			GroupsUsers.TABLE + " WHERE " + 
			GroupsUsers.TABLE + "." + GroupsUsers.GROUP_ID + "=" + groupId + ")";
		
		String tables = Users.TABLE + ", " + GroupsUsers.TABLE;
		String whereClause = Users.TABLE + "." + Users._ID + "=" +
			GroupsUsers.TABLE + "." + GroupsUsers.USER_ID + 
			" AND " + Users.TABLE + "." + Users._ID + " NOT IN " + subQuery;
		Cursor cursor = mDb.rawQuery("SELECT DISTINCT " + Users.TABLE + ".* FROM " + tables + 
				" WHERE " + whereClause, null);
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
}
