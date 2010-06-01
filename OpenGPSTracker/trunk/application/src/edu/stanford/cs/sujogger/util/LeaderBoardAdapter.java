package edu.stanford.cs.sujogger.util;

import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class LeaderBoardAdapter extends CursorAdapter {
	private static final String TAG = "OGT.LeaderBoardAdapter";
	private boolean mFlinging = false;
	private Cursor mCursor;
	private int mStatisticId;
	private boolean mIsGroup;
	
	public LeaderBoardAdapter(Context context, Cursor c, int statisticId, boolean isGroup) {
		this(context, c, true, statisticId, isGroup);
	}

	public LeaderBoardAdapter(Context context, Cursor c, boolean autoRequery, int statisticId, boolean isGroup) {
		super(context, c, autoRequery);
		mCursor = c;
		mStatisticId = statisticId;
		mIsGroup = isGroup;
	}
	
	public void changeCursor(Cursor cursor, int statisticId, boolean isGroup) {
		mCursor = cursor;
		mStatisticId = statisticId;
		mIsGroup = isGroup;
		super.changeCursor(cursor);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= mCursor.getCount()) return null;
		View ret = super.getView(position, convertView, parent);
		if (ret != null && !mIsGroup) {
			RemoteImageView image = (RemoteImageView) ret.findViewById(R.id.user_image);
			if (image != null && !mFlinging) {
				Log.d(TAG, "getView(): trying to fetch image");
				image.loadImage();
			}
		}
		return ret;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int value = cursor.getInt(3);
		TextView name = (TextView)view.findViewById(R.id.lb_name);
		RemoteImageView image = (RemoteImageView)view.findViewById(R.id.user_image);
		TextView valueText = (TextView)view.findViewById(R.id.lb_value);
		int place = cursor.getPosition() + 1;
		
		if (mIsGroup) {
			String groupName = cursor.getString(7);
			name.setText(place + ". " + groupName);
			image.setVisibility(View.GONE);
		} 
		else {
			String firstName = cursor.getString(8);
			String lastName = cursor.getString(9);
			String imgUrl = cursor.getString(10);
			image.setVisibility(View.VISIBLE);
			
			name.setText(place + ". " + firstName + " " + lastName);
			if (imgUrl != null) {
				image.setLocalURI(Common.getCacheFileName(imgUrl));
				image.setRemoteURI(imgUrl);
			}
		}
		
		switch(mStatisticId) {
		case Stats.DISTANCE_RAN_ID:
		case Stats.DISTANCE_RAN_WEEK_ID:
		case Stats.DISTANCE_RAN_MONTH_ID:
			valueText.setText(Common.distanceString(context, value));
			break;
		case Stats.RUNNING_TIME_ID:
		case Stats.RUNNING_TIME_WEEK_ID:
		case Stats.RUNNING_TIME_MONTH_ID:
			valueText.setText(Common.durationString(context, value));
			break;
		case Stats.AVG_SPEED_ID:
		case Stats.AVG_SPEED_WEEK_ID:
		case Stats.AVG_SPEED_MONTH_ID:
			valueText.setText(Common.speedString(context, ((double)value)/((double)Constants.SPEED_CONVERSION_RATIO)));
			break;
		case Stats.NUM_RUNS_ID:
		case Stats.NUM_RUNS_WEEK_ID:
		case Stats.NUM_RUNS_MONTH_ID:
		case Stats.NUM_PARTNER_RUNS_ID:
		case Stats.NUM_PARTNER_RUNS_WEEK_ID:
		case Stats.NUM_PARTNER_RUNS_MONTH_ID:
			valueText.setText(String.valueOf(value));
			break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
