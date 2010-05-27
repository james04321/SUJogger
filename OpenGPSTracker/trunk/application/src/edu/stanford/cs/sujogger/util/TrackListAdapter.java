package edu.stanford.cs.sujogger.util;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.stanford.cs.sujogger.R;

public class TrackListAdapter extends CursorAdapter {

	public TrackListAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	public TrackListAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String title = cursor.getString(1);
		String creationTime = cursor.getString(2);
		String duration = cursor.getString(3);
		String distance = cursor.getString(4);
		//String trackId = cursor.getString(5);
		
		TextView titleView = (TextView)view.findViewById(R.id.listitem_name);
		titleView.setText(title);
		
		DateView creationTimeView = (DateView)view.findViewById(R.id.listitem_from);
		creationTimeView.setText(creationTime);
		
		DistanceView distanceView = (DistanceView)view.findViewById(R.id.listitem_distance);
		distanceView.setText(distance);
		
		DurationView durationView = (DurationView)view.findViewById(R.id.listitem_duration);
		durationView.setText(duration);
		
		ImageView iconView = (ImageView)view.findViewById(R.id.listitem_icon);
		//if (trackId != null)
			iconView.setVisibility(View.VISIBLE);
		//else
		//	iconView.setVisibility(View.GONE);
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = LayoutInflater.from(context).inflate(R.layout.trackitem, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
