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

import edu.stanford.cs.sujogger.R;
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
   /** The version of the database schema */
   //static final int DATABASE_VERSION = 9;
   
   public static final class Stats implements android.provider.BaseColumns {
		//Statistics ID constants
		public static final int DISTANCE_RAN_ID = 1;
		public static final int RUNNING_TIME_ID = 2;
		public static final int NUM_RUNS_ID = 3;
		public static final int NUM_PARTNER_RUNS_ID = 4;
		public static final int AVG_SPEED_ID = 5;
		//public static final int MED_DURATION_ID = 6;
		//public static final int MED_DISTANCE_ID = 7;
		
		public static final int DISTANCE_RAN_WEEK_ID = 11;
		public static final int RUNNING_TIME_WEEK_ID = 12;
		public static final int NUM_RUNS_WEEK_ID = 13;
		public static final int NUM_PARTNER_RUNS_WEEK_ID = 14;
		public static final int AVG_SPEED_WEEK_ID = 15;
		
		public static final int DISTANCE_RAN_MONTH_ID = 21;
		public static final int RUNNING_TIME_MONTH_ID = 22;
		public static final int NUM_RUNS_MONTH_ID = 23;
		public static final int NUM_PARTNER_RUNS_MONTH_ID = 24;
		public static final int AVG_SPEED_MONTH_ID = 25;
		
		public static final int[] ALL_STAT_IDS = new int[] {
			DISTANCE_RAN_ID, RUNNING_TIME_ID, NUM_RUNS_ID, NUM_PARTNER_RUNS_ID, AVG_SPEED_ID,
			DISTANCE_RAN_WEEK_ID, RUNNING_TIME_WEEK_ID,
			NUM_RUNS_WEEK_ID, NUM_PARTNER_RUNS_WEEK_ID, AVG_SPEED_WEEK_ID, DISTANCE_RAN_MONTH_ID,
			RUNNING_TIME_MONTH_ID, NUM_RUNS_MONTH_ID, NUM_PARTNER_RUNS_MONTH_ID, AVG_SPEED_MONTH_ID};
		
		public static final int[] GROUP_STAT_IDS = new int[] {
			DISTANCE_RAN_ID, RUNNING_TIME_ID, NUM_RUNS_ID, NUM_PARTNER_RUNS_ID};
		
		public static final String[] STAT_TYPES_SOLO = new String[] {
			"Distance ran", "Running time", "Runs", "Partner runs", 
			"Avg speed"};
		public static final String[] ALL_STAT_TITLES = new String[] { 
			"", "Distance ran", "Running time", "Runs", "Partner runs", "Avg speed", "", "","","",
			"", "Distance ran (week)", "Running time (week)", "Runs (week)", "Partner runs (week)", "Avg speed (week)", "", "","","",
			"", "Distance ran (month)", "Running time (month)", "Runs (month)", "Partner runs (month)", "Avg speed (month)", "", "","",""
			};		
		
		public static final String[] STAT_TYPES_GROUP = new String[] {
			"Distance ran", "Running time", "Runs", "Partner runs"};
		
		public static final String[] TIME_TYPES = new String[] {
			"All time", "Past week", "Past month"};
		
		public static final long WEEK_INTERVAL = 604800000; // milliseconds in a week
		public static final long MONTH_INTERVAL = 2629743830L; // approx. milliseconds in a month
		
		// Table attributes
		public static final String STATISTIC_ID = "statistic_id";
		public static final String SCOREBOARD_ID = "scoreboard_id";
		public static final String GROUP_ID = "group_id"; //group_id_server; 0 if self
		public static final String VALUE = "value";
		
		static final String STATISTIC_ID_TYPE = "INTEGER NOT NULL";
		static final String SCOREBOARD_ID_TYPE = "INTEGER NOT NULL";
		static final String GROUP_ID_TYPE = "INTEGER NOT NULL DEFAULT 0";
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
		public static final long RECENT_INTERVAL = 259200000; //3 days
		
		public static final String STATISTIC_ID = "statistic_id";
		public static final String IS_GROUP = "is_group";
		public static final String CONDITION = "condition";
		public static final String COMPLETED = "completed";
		public static final String UPDATED_AT = "updated_at";
		public static final String CATEGORY = "category";
		public static final String ICON_RESOURCE = "icon_resource";
		public static final String TITLE = "title";
		public static final String DESCRIPTION = "description";
		
		static final String STATISTIC_ID_TYPE = "INTEGER NOT NULL";
		static final String IS_GROUP_TYPE = "INTEGER NOT NULL";
		static final String CONDITION_TYPE = "REAL NOT NULL";
		static final String COMPLETED_TYPE = "INTEGER NOT NULL";
		static final String UPDATED_AT_TYPE = "INTEGER NOT NULL";
		static final String CATEGORY_TYPE = "INTEGER NOT NULL DEFAULT 1";
		static final String ICON_RESOURCE_TYPE = "INTEGER NOT NULL DEFAULT 2130837504";
		static final String TITLE_TYPE = "TEXT NOT NULL";
		static final String DESCRIPTION_TYPE = "TEXT NOT NULL";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		static final String TABLE = "achievements";
		
		public static final String[] COLUMNS = new String[] {_ID,
			STATISTIC_ID, IS_GROUP, CONDITION, COMPLETED, UPDATED_AT, CATEGORY, ICON_RESOURCE,
			TITLE, DESCRIPTION};
	}
	
	public static final class Categories {
		public static final int NUM_DIFF_CAT = 3;
		public static final int NUM_TYPE_CAT = 4;
		
		public static final int DIFF_EASY = 0x1;
		public static final int DIFF_MED = 0x2;
		public static final int DIFF_HARD = 0x3;
		
		public static final int EXT_5K = 0x7;
		public static final int EXT_HALFMARATHON = 0x8;
		public static final int EXT_MARATHON = 0x9;
		
		public static final int TYPE_SPEED = 0x10;
		public static final int TYPE_ENDURANCE = 0x20;
		public static final int TYPE_DETERMINATION = 0x30;
		public static final int TYPE_CHARISMA = 0x40;
		public static final int TYPE_EXT = 0x50;
		
		public static final int SOLO = 0x100;
		public static final int TEAM = 0x200;
		
		public static int DIFF_MASK = 0xF;
		public static int TYPE_MASK = 0xF0;
		
		public static String TABLE = "categories";
		
		public static int difficulty(int cat) {
			return 0x000F & cat;
		}
		
		public static int type(int cat) {
			return 0x00F0 & cat;
		}
		
		public static int team(int cat) {
			return 0x0F00 & cat;
		}
		
		public static int getMaskForSingleCat(int cat) {
			if (difficulty(cat) != 0)
				return DIFF_MASK;
			if (type(cat) != 0)
				return TYPE_MASK;
			
			return TYPE_MASK;
		}
		
		public static String getNameForCat(int cat) {
			switch(cat) {
			case DIFF_EASY: return "Easy";
			case DIFF_MED: return "Medium";
			case DIFF_HARD: return "Hard";
			case TYPE_SPEED: return "Speed";
			case TYPE_ENDURANCE: return "Endurance";
			case TYPE_DETERMINATION: return "Determination";
			case TYPE_CHARISMA: return "Charisma";
			default: return "";
			}
		}
	}
	
	public static final class Users implements android.provider.BaseColumns {
		public static final String USER_ID = "user_id";
		public static final String FB_ID = "fb_id";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String IMG_URL = "img_url";
		public static final String IS_FRIEND = "is_friend";
		
		static final String USER_ID_TYPE = "INTEGER NOT NULL UNIQUE";
		static final String FB_ID_TYPE = "INTEGER NOT NULL";
		static final String FIRST_NAME_TYPE = "TEXT";
		static final String LAST_NAME_TYPE = "TEXT";
		static final String IMG_URL_TYPE = "TEXT";
		static final String IS_FRIEND_TYPE = "INTEGER NOT NULL DEFAULT 0";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		public static final String TABLE = "users";
	}
	
	public static final class Groups implements android.provider.BaseColumns {
		public static final String GROUP_ID = "group_id";
		public static final String NAME = "name";
		public static final String IS_OWNER = "is_owner";
		
		static final String GROUP_ID_TYPE = "INTEGER NOT NULL UNIQUE";
		static final String NAME_TYPE = "TEXT NOT NULL";
		static final String IS_OWNER_TYPE = "INTEGER NOT NULL";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		public static final String TABLE = "groups";
	}
	
	//Table to store all groups for leaderboards
	public static final class GroupsTemp implements android.provider.BaseColumns {
		public static final String GROUP_ID = "group_id";
		public static final String NAME = "name";
		
		static final String GROUP_ID_TYPE = "INTEGER NOT NULL UNIQUE";
		static final String NAME_TYPE = "TEXT NOT NULL";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		public static final String TABLE = "groups_temp";
	}
	
	public static final class GroupsUsers implements android.provider.BaseColumns {
		public static final String GROUP_ID = "group_id";
		public static final String USER_ID = "user_id";
		
		static final String GROUP_ID_TYPE = "INTEGER NOT NULL";
		static final String USER_ID_TYPE = "INTEGER NOT NULL";
		
		public static final String TABLE = "groups_users";
	}
	
	public static final class GameMessages implements android.provider.BaseColumns {
		public static final String FROM_USER = "from_user"; //user_id_server
		public static final String TYPE = "type";
		public static final String SEND_TIME = "send_time";
		public static final String PROPOSED_TIME = "proposed_time";
		public static final String ORIG_SEND_TIME = "orig_send_time";
		public static final String SUBJECT = "subject";
		public static final String BODY = "body";
		public static final String IS_BCAST = "is_bcast";
		public static final String DID_START = "did_start";
		
		static final String FROM_USER_TYPE = "INTEGER NOT NULL";
		static final String TYPE_TYPE = "INTEGER NOT NULL";
		static final String SEND_TIME_TYPE = "INTEGER NOT NULL";
		static final String PROPOSED_TIME_TYPE = "INTEGER";
		static final String ORIG_SEND_TIME_TYPE = "INTEGER NOT NULL";
		static final String SUBJECT_TYPE = "TEXT NOT NULL";
		static final String BODY_TYPE = "TEXT";
		static final String IS_BCAST_TYPE = "INTEGER NOT NULL DEFAULT 0";
		static final String DID_START_TYPE = "INTEGER NOT NULL DEFAULT 0";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		public static final String TABLE = "game_messages";
		
		//GameMessage types
		public static final int TYPE_INVITE = 0;
		public static final int TYPE_CHALLENGE = 1;
		public static final int TYPE_GENERIC = 2;
		
		public static String[] types = {"Invite", "Challenge", "Message"};
	}
	
	public static final class GMRecipients implements android.provider.BaseColumns {
		public static final String GM_ID = "gm_id";
		public static final String USER_ID = "user_id"; //user_id_server
		
		static final String GM_ID_TYPE = "INTEGER NOT NULL";
		static final String USER_ID_TYPE = "INTEGER NOT NULL";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		public static final String TABLE = "gm_recipients";
	}
	
	public static class Messages {
		
		//Types
		public static final int TYPE_GM = 0;
		public static final int TYPE_NOTIFICATION = 1;
	}
	
	public static final class ScoreboardTemp implements android.provider.BaseColumns {
		public static final String USER_ID = "user_id";
		public static final String GROUP_ID = "group_id";
		public static final String VALUE = "value";
		public static final String TYPE = "type";
		
		static final String USER_ID_TYPE = "INTEGER";
		static final String GROUP_ID_TYPE = "INTEGER";
		static final String VALUE_TYPE = "INTEGER NOT NULL";
		static final String TYPE_TYPE = "INTEGER NOT NULL";
		static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
		
		public static final String TABLE = "scoreboard";
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
      public static final String TRACK_ID      = "track_id";
      public static final String USER_ID       = "user_id";
      
      static final String NAME_TYPE            = "TEXT";
      static final String CREATION_TIME_TYPE   = "INTEGER NOT NULL";
      static final String DURATION_TYPE        = "INTEGER";
      static final String DISTANCE_TYPE        = "REAL";
      static final String TRACK_ID_TYPE        = "INTEGER";
      static final String USER_ID_TYPE         = "INTEGER NOT NULL DEFAULT 0";
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
