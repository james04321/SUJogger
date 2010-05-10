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
import edu.stanford.cs.sujogger.db.GPStracking.Achievements;

public class AchListAdapter extends CursorAdapter {

	public AchListAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	public AchListAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int achId = cursor.getInt(0);
		
		TextView achTitle = (TextView)view.findViewById(R.id.ach_list_item_title);
		achTitle.setText(Achievements.getTitleForId(achId));
		
		ImageView achIcon = (ImageView)view.findViewById(R.id.ach_list_item_image);
		achIcon.setImageResource(Achievements.getImageForId(achId));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = LayoutInflater.from(context).inflate(R.layout.ach_list_item, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
