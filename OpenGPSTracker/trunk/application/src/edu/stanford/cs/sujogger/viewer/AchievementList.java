package edu.stanford.cs.sujogger.viewer;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Categories;
import edu.stanford.cs.sujogger.util.AchListAdapter;
import edu.stanford.cs.sujogger.util.Constants;

public class AchievementList extends ListActivity {
	private static final String TAG = "OGT.AchievementList";
	
	private int mCat;
	private DatabaseHelper mDbHelper;
	private Cursor mAchCursor;

	public AchievementList() {}

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
		
		if (Constants.AD_TEST) AdManager.setTestDevices(new String[] { "3468678E351E95A5F7A64D2271BCB7BF" });
		AdView adView = (AdView)View.inflate(this, R.layout.adview, null);
		getListView().addHeaderView(adView);
		
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
