package edu.stanford.cs.sujogger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class StatisticsManager {
	public static final class Statistics implements android.provider.BaseColumns {
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
			"CREATE TABLE" + Statistics.TABLE + 
			"( " + Statistics._ID + " " + Statistics._ID_TYPE +
			", " + Statistics.VALUE + " " + Statistics.VALUE_TYPE +
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
	
	private Context mCtx;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	public StatisticsManager(Context context) {
		mCtx = context;
	}
	

	
	

}
