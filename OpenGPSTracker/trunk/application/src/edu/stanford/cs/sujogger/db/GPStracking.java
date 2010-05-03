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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The GPStracking provider stores all static information about GPStracking.
 * 
 * @version $Id: GPStracking.java 459 2010-03-19 14:11:12Z rcgroot $
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 */
public final class GPStracking
{
   /** The authority of this provider */
   public static final String AUTHORITY = "edu.stanford.cs.sujogger";
   /** The content:// style URL for this provider */
   public static final Uri CONTENT_URI = Uri.parse( "content://" + GPStracking.AUTHORITY );
   /** The name of the database file */
   //static final String DATABASE_NAME = "GPSLOG.db";
   /** The version of the database schema */
   //static final int DATABASE_VERSION = 9;
   
   public static final class Stats implements android.provider.BaseColumns {
		//Statistics ID constants
		public static final int DISTANCE_RAN_ID = 1;
		public static final int RUNNING_TIME_ID = 2;
		public static final int NUM_RUNS_ID = 3;
		public static final int AVG_SPEED_ID = 4;
		public static final int MED_DURATION_ID = 5;
		public static final int MED_DISTANCE_ID = 6;
		
		public static final String VALUE = "value";
		
		static final String VALUE_TYPE = "REAL NOT NULL";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		static final String TABLE = "statistics";
		static final String CREATE_STATEMENT = 
			"CREATE TABLE" + Stats.TABLE + 
			"( " + Stats._ID + " " + Stats._ID_TYPE +
			", " + Stats.VALUE + " " + Stats.VALUE_TYPE +
			");";
	}
	
	public static final class Achievements implements android.provider.BaseColumns {
		public static final String STATISTIC_ID = "statistic_id";
		public static final String ACHIEVEMENTCATEGORY_ID = "achievementcategory_id";
		public static final String GROUP_ID = "group_id";
		public static final String CONDITION = "condition";
		public static final String COMPLETED = "completed";
		
		static final String STATISTIC_ID_TYPE = "INTEGER NOT NULL";
		static final String ACHIEVEMENTCATEGORY_ID_TYPE = "INTEGER NOT NULL";
		static final String GROUP_ID_TYPE = "INTEGER NOT NULL";
		static final String CONDITION_TYPE = "REAL NOT NULL";
		static final String COMPLETED_TYPE = "INTEGER NOT NULL";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		static final String TABLE = "achievements";
		static final String CREATE_STATEMENT =
			"CREATE TABLE" + Achievements.TABLE +
			"( " + Achievements._ID + " " + Achievements._ID_TYPE +
			", " + Achievements.STATISTIC_ID + " " + Achievements.STATISTIC_ID_TYPE +
			", " + Achievements.ACHIEVEMENTCATEGORY_ID + " " + Achievements.ACHIEVEMENTCATEGORY_ID_TYPE +
			", " + Achievements.GROUP_ID + " " + Achievements.GROUP_ID_TYPE +
			", " + Achievements.CONDITION + " " + Achievements.CONDITION_TYPE +
			", " + Achievements.COMPLETED + " " + Achievements.COMPLETED_TYPE +
			");";
	}
	
	public static final class AchievementCategories implements android.provider.BaseColumns {
		public static final String ACHIEVEMENT_ID = "achievement_id";
		public static final String CATEGORY_ID = "category_id";
		
		static final String ACHIEVEMENT_ID_TYPE = "INTEGER NOT NULL";
		static final String CATEGORY_ID_TYPE = "INTEGER NOT NULL";
		
		static final String TABLE = "achievementcategories";
	}
   
   /**
    * This table contains tracks.
    * 
    * @author rene
    */
   public static final class Tracks extends TracksColumns implements android.provider.BaseColumns
   {
      /** The MIME type of a CONTENT_URI subdirectory of a single track. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.edu.stanford.cs.track";
      /** The MIME type of CONTENT_URI providing a directory of tracks. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.edu.stanford.cs.track";
      /** The content:// style URL for this provider */
      public static final Uri CONTENT_URI = Uri.parse( "content://" + GPStracking.AUTHORITY + "/" + Tracks.TABLE );

      /** The name of this table */
      static final String TABLE = "tracks";
      static final String CREATE_STATEMENT = 
         "CREATE TABLE " + Tracks.TABLE + "(" + " " + Tracks._ID           + " " + Tracks._ID_TYPE + 
                                          "," + " " + Tracks.NAME          + " " + Tracks.NAME_TYPE + 
                                          "," + " " + Tracks.CREATION_TIME + " " + Tracks.CREATION_TIME_TYPE + 
                                          "," + " " + Tracks.DURATION + " " + Tracks.DURATION_TYPE +
                                          "," + " " + Tracks.DISTANCE + " " + Tracks.DISTANCE_TYPE +
                                          ");";
   }
   
   /**
    * This table contains segments.
    * 
    * @author rene
    */
   public static final class Segments extends SegmentsColumns implements android.provider.BaseColumns
   {

      /** The MIME type of a CONTENT_URI subdirectory of a single segment. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.edu.stanford.cs.segment";
      /** The MIME type of CONTENT_URI providing a directory of segments. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.edu.stanford.cs.segment";

      /** The name of this table */
      static final String TABLE = "segments";
      static final String CREATE_STATMENT = 
         "CREATE TABLE " + Segments.TABLE + "(" + " " + Segments._ID   + " " + Segments._ID_TYPE + 
                                            "," + " " + Segments.TRACK + " " + Segments.TRACK_TYPE + 
                                            ");";
   }

   /**
    * This table contains waypoints.
    * 
    * @author rene
    */
   public static final class Waypoints extends WaypointsColumns implements android.provider.BaseColumns
   {

      /** The MIME type of a CONTENT_URI subdirectory of a single waypoint. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.edu.stanford.cs.waypoint";
      /** The MIME type of CONTENT_URI providing a directory of waypoints. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.edu.stanford.cs.waypoint";
      
      /** The name of this table */
      public static final String TABLE = "waypoints";
      static final String CREATE_STATEMENT = "CREATE TABLE " + Waypoints.TABLE + 
      "(" + " " + BaseColumns._ID + " " + WaypointsColumns._ID_TYPE + 
      "," + " " + WaypointsColumns.LATITUDE  + " " + WaypointsColumns.LATITUDE_TYPE + 
      "," + " " + WaypointsColumns.LONGITUDE + " " + WaypointsColumns.LONGITUDE_TYPE + 
      "," + " " + WaypointsColumns.TIME      + " " + WaypointsColumns.TIME_TYPE + 
      "," + " " + WaypointsColumns.SPEED     + " " + WaypointsColumns.SPEED + 
      "," + " " + WaypointsColumns.SEGMENT   + " " + WaypointsColumns.SEGMENT_TYPE + 
      "," + " " + WaypointsColumns.ACCURACY  + " " + WaypointsColumns.ACCURACY_TYPE + 
      "," + " " + WaypointsColumns.ALTITUDE  + " " + WaypointsColumns.ALTITUDE_TYPE + 
      "," + " " + WaypointsColumns.BEARING   + " " + WaypointsColumns.BEARING_TYPE + 
      ");";
      
      //TODO: don't need upgrade statement anymore
      static final String[] UPGRADE_STATEMENT_7_TO_8 = 
         {
            "ALTER TABLE " + Waypoints.TABLE + " ADD COLUMN " + WaypointsColumns.ACCURACY + " " + WaypointsColumns.ACCURACY_TYPE +";",
            "ALTER TABLE " + Waypoints.TABLE + " ADD COLUMN " + WaypointsColumns.ALTITUDE + " " + WaypointsColumns.ALTITUDE_TYPE +";",
            "ALTER TABLE " + Waypoints.TABLE + " ADD COLUMN " + WaypointsColumns.BEARING  + " " + WaypointsColumns.BEARING_TYPE +";"
         };
   }
   
   /**
    * This table contains media URI's.
    * 
    * @author rene
    */
   public static final class Media extends MediaColumns implements android.provider.BaseColumns
   {

      /** The MIME type of a CONTENT_URI subdirectory of a single media entry. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.edu.stanford.cs.media";
      /** The MIME type of CONTENT_URI providing a directory of media entry. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.edu.stanford.cs.media";
      
      /** The name of this table */
      public static final String TABLE = "media";
      static final String CREATE_STATEMENT = "CREATE TABLE " + Media.TABLE + 
      "(" + " " + BaseColumns._ID       + " " + MediaColumns._ID_TYPE + 
      "," + " " + MediaColumns.TRACK    + " " + MediaColumns.TRACK_TYPE + 
      "," + " " + MediaColumns.SEGMENT  + " " + MediaColumns.SEGMENT_TYPE + 
      "," + " " + MediaColumns.WAYPOINT + " " + MediaColumns.WAYPOINT_TYPE + 
      "," + " " + MediaColumns.URI      + " " + MediaColumns.URI_TYPE + 
      ");";
      public static final Uri CONTENT_URI = Uri.parse( "content://" + GPStracking.AUTHORITY + "/" + Media.TABLE );
   }
   
   /**
    * Columns from the tracks table.
    * 
    * @author rene
    */
   public static class TracksColumns
   {
      /** The end time */
      public static final String NAME          = "name";
      public static final String CREATION_TIME = "creationtime";
      public static final String DURATION      = "duration";
      public static final String DISTANCE      = "distance";
      
      static final String NAME_TYPE            = "TEXT";
      static final String CREATION_TIME_TYPE   = "INTEGER NOT NULL";
      static final String DURATION_TYPE        = "INTEGER";
      static final String DISTANCE_TYPE        = "REAL";
      static final String _ID_TYPE             = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }
   
   /**
    * Columns from the segments table.
    * 
    * @author rene
    */
   public static class SegmentsColumns
   {
      /** The track _id to which this segment belongs */
      public static final String TRACK = "track";     
      static final String TRACK_TYPE   = "INTEGER NOT NULL";
      static final String _ID_TYPE     = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }

   /**
    * Columns from the waypoints table.
    * 
    * @author rene
    */
   public static class WaypointsColumns
   {

      /** The latitude */
      public static final String LATITUDE = "latitude";
      /** The longitude */
      public static final String LONGITUDE = "longitude";
      /** The recorded time */
      public static final String TIME = "time";
      /** The speed in meters per second */
      public static final String SPEED = "speed";
      /** The segment _id to which this segment belongs */
      public static final String SEGMENT = "tracksegment";
      /** The accuracy of the fix */
      public static final String ACCURACY = "accuracy";
      /** The altitude */
      public static final String ALTITUDE = "altitude";
      /** the bearing of the fix */
      public static final String BEARING = "bearing";

      static final String LATITUDE_TYPE  = "REAL NOT NULL";
      static final String LONGITUDE_TYPE = "REAL NOT NULL";
      static final String TIME_TYPE      = "INTEGER NOT NULL";
      static final String SPEED_TYPE     = "REAL NOT NULL";
      static final String SEGMENT_TYPE   = "INTEGER NOT NULL";
      static final String ACCURACY_TYPE  = "REAL";
      static final String ALTITUDE_TYPE  = "REAL";
      static final String BEARING_TYPE   = "REAL";
      static final String _ID_TYPE       = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }
   
   /**
    * Columns from the meia table.
    * 
    * @author rene
    */
   public static class MediaColumns
   {
      /** The track _id to which this segment belongs */
      public static final String TRACK    = "track";     
      static final String TRACK_TYPE      = "INTEGER NOT NULL";
      public static final String SEGMENT  = "segment";     
      static final String SEGMENT_TYPE    = "INTEGER NOT NULL";
      public static final String WAYPOINT = "waypoint";     
      static final String WAYPOINT_TYPE   = "INTEGER NOT NULL";
      public static final String URI      = "uri";     
      static final String URI_TYPE        = "TEXT";
      static final String _ID_TYPE        = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }
}
