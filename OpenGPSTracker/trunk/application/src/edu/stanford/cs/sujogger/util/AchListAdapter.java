package edu.stanford.cs.sujogger.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.stanford.cs.sujogger.R;

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
		int completed = cursor.getInt(4);
		
		TextView achTitle = (TextView)view.findViewById(R.id.ach_list_item_title);
		achTitle.setText(cursor.getString(8));
		if (completed == 0)
			achTitle.setTextColor(Color.GRAY);
		else
			achTitle.setTextColor(Color.WHITE);
		
		TextView achDescription = (TextView)view.findViewById(R.id.ach_list_item_description);
		achDescription.setText(cursor.getString(9));
		
		ImageView achIcon = (ImageView)view.findViewById(R.id.ach_list_item_image);
		achIcon.setImageResource(cursor.getInt(7));
		//Log.d("AchListAdapter", "title = " + cursor.getString(8) + "; icon resource = " + Integer.toHexString(cursor.getInt(7)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = LayoutInflater.from(context).inflate(R.layout.ach_list_item, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
