package edu.stanford.cs.sujogger.viewer;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Categories;
import edu.stanford.cs.sujogger.util.AchListAdapter;

public class AchievementList extends ListActivity {
	private static final String TAG = "OGT.AchievementList";
	
	private int mCat;
	private DatabaseHelper mDbHelper;
	private Cursor mAchCursor;

	public AchievementList() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_simple);

		mCat = savedInstanceState != null ? savedInstanceState.getInt(Categories.TABLE) : 0;

		if (mCat == 0) {
			Bundle extras = getIntent().getExtras();
			mCat = extras != null ? extras.getInt(Categories.TABLE) : 0;
		}
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mAchCursor = mDbHelper.getAchievementsInCat(mCat, Categories.getMaskForSingleCat(mCat));
		startManagingCursor(mAchCursor);
		
		this.setTitle(Categories.getNameForCat(mCat) + " Achievements");
		
		fillData();
	}
	
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(Categories.TABLE, mCat);
	}
	
	private void fillData() {
		AchListAdapter achAdapter = new AchListAdapter(this, mAchCursor, true);
		setListAdapter(achAdapter);
	}

}
