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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import edu.stanford.cs.sujogger.db.GPStracking.Media;
import edu.stanford.cs.sujogger.db.GPStracking.MediaColumns;
import edu.stanford.cs.sujogger.db.GPStracking.Segments;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.db.GPStracking.TracksColumns;
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
class DatabaseHelper extends SQLiteOpenHelper {
	private static String DB_PATH = "/data/data/edu.stanford.cs.sujogger/databases/";
	private static String DB_NAME = "SUJogger.sqlite";
	private static int DB_VERSION = 1;
	
	private Context mContext;
	private final static String TAG = "OGT.DatabaseHelper";

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
	/*
	public void openDatabase() throws SQLException {
		mDb = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
	}
	
	@Override
	public synchronized void close() {
		if (mDb != null) mDb.close();
		super.close();
	}
*/
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

		SQLiteDatabase sqldb = getWritableDatabase();

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

		long waypointId = sqldb.insert(Waypoints.TABLE, null, args);

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
		SQLiteDatabase sqldb = getWritableDatabase();

		ContentValues args = new ContentValues();
		args.put(MediaColumns.TRACK, trackId);
		args.put(MediaColumns.SEGMENT, segmentId);
		args.put(MediaColumns.WAYPOINT, waypointId);
		args.put(MediaColumns.URI, mediaUri);

		// Log.d( TAG, "Media stored in the datebase: "+mediaUri );

		long mediaId = sqldb.insert(Media.TABLE, null, args);

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
		SQLiteDatabase sqldb = getWritableDatabase();
		int affected = 0;
		Cursor cursor = null;
		long segmentId = -1;

		try {
			cursor = sqldb.query(Segments.TABLE, new String[] { Segments._ID }, Segments.TRACK
					+ "= ?", new String[] { String.valueOf(trackId) }, null, null, null, null);
			if (cursor.moveToFirst()) {
				segmentId = cursor.getLong(0);
				affected += deleteSegment(sqldb, trackId, segmentId);
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

		affected += sqldb.delete(Tracks.TABLE, Tracks._ID + "= ?", new String[] { String
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
		SQLiteDatabase sqldb = getWritableDatabase();

		Cursor cursor = null;
		long trackId = -1;
		long segmentId = -1;
		long waypointId = -1;
		try {
			cursor = sqldb.query(Media.TABLE, new String[] { Media.TRACK, Media.SEGMENT,
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

		int affected = sqldb.delete(Media.TABLE, Media._ID + "= ?", new String[] { String
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

		SQLiteDatabase sqldb = getWritableDatabase();
		long trackId = sqldb.insert(Tracks.TABLE, null, args);

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
		SQLiteDatabase sqldb = getWritableDatabase();

		ContentValues args = new ContentValues();
		args.put(Segments.TRACK, trackId);
		long segmentId = sqldb.insert(Segments.TABLE, null, args);

		ContentResolver resolver = this.mContext.getContentResolver();
		resolver.notifyChange(Uri.withAppendedPath(Tracks.CONTENT_URI, trackId + "/segments"), null);

		return segmentId;
	}
	
	public int getStatisticInt(long statisticId) {
		return (int) getStatisticReal(statisticId);
	}
	
	public double getStatisticReal(long statisticId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(true, Stats.TABLE, new String[] {Stats.VALUE}, 
				Stats._ID + "=" + statisticId, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getDouble(0);
		}
		else return 0;
	}
	
	public boolean setStatistic(long statisticId, double val) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues args = new ContentValues();
		args.put(Stats.VALUE, val);
		
		return db.update(Stats.TABLE, args, Stats._ID + "=" + statisticId, null) > 0;
	}
	
	public void increaseStatistic(long statisticId, double val) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues args = new ContentValues();
		args.put(Stats.VALUE, val);
		
		db.execSQL("UPDATE ? Set ? = ? + ? WHERE ? = ?", 
				new Object[] {Stats.TABLE, Stats.VALUE, Stats.VALUE, 
				new Double(val), Stats._ID, new Long(statisticId)});
	}
	
	public void increaseStatisticByOne(long statisticId) {
		increaseStatistic(statisticId, 1);
	}
}
