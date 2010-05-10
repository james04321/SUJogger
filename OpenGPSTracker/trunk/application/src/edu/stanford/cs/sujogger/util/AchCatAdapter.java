package edu.stanford.cs.sujogger.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Categories;

public class AchCatAdapter extends BaseAdapter {
	private Context mContext;
	private DatabaseHelper mDbHelper;
	private int mSection;
	
	public AchCatAdapter(Context context, DatabaseHelper dbHelper, int section) {
		mContext = context;
		mDbHelper = dbHelper;
		mSection = section;
	}

	public int getCount() {
		switch(mSection) {
		case 0: return Categories.NUM_DIFF_CAT;
		case 1: return Categories.NUM_TYPE_CAT;
		default: return 0;
		}
	}

	public Object getItem(int position) {
		if (mSection == 0) {
			switch(position) {
			case 0: return Categories.DIFF_EASY;
			case 1: return Categories.DIFF_MED;
			case 2: return Categories.DIFF_HARD;
			default: break;
			}
		}
		else if (mSection == 1) {
			switch(position) {
			case 0: return Categories.TYPE_SPEED;
			case 1: return Categories.TYPE_ENDURANCE;
			case 2: return Categories.TYPE_DETERMINATION;
			case 3: return Categories.TYPE_CHARISMA;
			default: return null;
			}
		}
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = LayoutInflater.from(mContext).inflate(R.layout.achievementcatitem, parent, false);
		
		TextView title = (TextView)convertView.findViewById(R.id.achievementcat_name);
		if (mSection == 0) {
			switch(position) {
			case 0: title.setText("Easy"); break;
			case 1: title.setText("Medium"); break;
			case 2: title.setText("Hard"); break;
			default: break;
			}
		}
		else if (mSection == 1) {
			switch(position) {
			case 0: title.setText("Speed"); break;
			case 1: title.setText("Endurance"); break;
			case 2: title.setText("Determination"); break;
			case 3: title.setText("Charisma"); break;
			default: break;
			}
		}
		
		TextView fraction = (TextView)convertView.findViewById(R.id.achievementcat_fraction);
		if (mSection == 0) {
			switch(position) {
			case 0: fraction.setText(
					mDbHelper.getCompletedAchievementCountForCat(Categories.DIFF_EASY, Categories.DIFF_MASK) + "/" + 
					mDbHelper.getAchievementCountForCat(Categories.DIFF_EASY, Categories.DIFF_MASK)); break;
			case 1: fraction.setText(
					mDbHelper.getCompletedAchievementCountForCat(Categories.DIFF_MED, Categories.DIFF_MASK) + "/" + 
					mDbHelper.getAchievementCountForCat(Categories.DIFF_MED, Categories.DIFF_MASK)); break;
			case 2: fraction.setText(
					mDbHelper.getCompletedAchievementCountForCat(Categories.DIFF_HARD, Categories.DIFF_MASK) + "/" + 
					mDbHelper.getAchievementCountForCat(Categories.DIFF_HARD, Categories.DIFF_MASK)); break;
			default: break;
			}
		}
		else if (mSection == 1) {
			switch(position) {
			case 0: fraction.setText(
					mDbHelper.getCompletedAchievementCountForCat(Categories.TYPE_SPEED, Categories.TYPE_MASK) + "/" + 
					mDbHelper.getAchievementCountForCat(Categories.TYPE_SPEED, Categories.TYPE_MASK)); break;
			case 1: fraction.setText(
					mDbHelper.getCompletedAchievementCountForCat(Categories.TYPE_ENDURANCE, Categories.TYPE_MASK) + "/" + 
					mDbHelper.getAchievementCountForCat(Categories.TYPE_ENDURANCE, Categories.TYPE_MASK)); break;
			case 2: fraction.setText(
					mDbHelper.getCompletedAchievementCountForCat(Categories.TYPE_DETERMINATION, Categories.TYPE_MASK) + "/" + 
					mDbHelper.getAchievementCountForCat(Categories.TYPE_DETERMINATION, Categories.TYPE_MASK)); break;
			case 3: fraction.setText(
					mDbHelper.getCompletedAchievementCountForCat(Categories.TYPE_CHARISMA, Categories.TYPE_MASK) + "/" + 
					mDbHelper.getAchievementCountForCat(Categories.TYPE_CHARISMA, Categories.TYPE_MASK)); break;
			default: break;
			}
		}
		
		return convertView;
	}

}
