package edu.stanford.cs.sujogger.util;

import edu.stanford.cs.sujogger.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class GroupListAdapter extends CursorAdapter {
	private Cursor mCursor;
	
	public GroupListAdapter(Context context, Cursor c) {
		super(context, c);
		mCursor = c;
	}

	public GroupListAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mCursor = c;
	}
	
	public Object getItem(int position) {
		mCursor.moveToPosition(position);
		return mCursor.getInt(1);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String name = cursor.getString(2);
		
		TextView grpTitle = (TextView)view.findViewById(R.id.grp_name);
		grpTitle.setText(name);
		
		TextView grpReadCount = (TextView)view.findViewById(R.id.grp_indicator);
		grpReadCount.setText(String.valueOf(cursor.getInt(3)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = LayoutInflater.from(context).inflate(R.layout.grp_list_item, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
