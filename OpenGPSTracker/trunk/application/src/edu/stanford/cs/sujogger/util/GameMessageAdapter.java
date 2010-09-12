package edu.stanford.cs.sujogger.util;

import java.util.Calendar;

import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.GPStracking.GameMessages;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class GameMessageAdapter extends CursorAdapter {
	private static final String TAG = "OGT.GameMessageAdapter";
	private Cursor mCursor;
	private boolean mFlinging = false;
	private boolean mFull;

	public GameMessageAdapter(Context context, Cursor c, boolean full) {
		this(context, c, true, full);
	}

	public GameMessageAdapter(Context context, Cursor c, boolean autoRequery, boolean full) {
		super(context, c, autoRequery);
		mCursor = c;
		mFull = full;
	}
	
	@Override
	public void changeCursor(Cursor cursor) {
		mCursor = cursor;
		super.changeCursor(cursor);
	}
	
	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			Log.d(TAG, "getItem(): id = " + mCursor.getLong(0));
			return mCursor.getLong(0);
		}
		else return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View ret = super.getView(position, convertView, parent);
		//TODO: Facebook
		
		if (ret != null) {
			RemoteImageView image = (RemoteImageView) ret.findViewById(R.id.msg_item_image);
			if (image != null && !mFlinging) {
				Log.d(TAG, "getView(): trying to fetch image");
				image.loadImage();
			}
		}
		return ret;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int type = cursor.getInt(2);
		long sendTime = cursor.getLong(3);
		String subject = cursor.getString(6);
		String firstName = cursor.getString(13);
		String lastName = cursor.getString(14);
		String imgUrl = cursor.getString(15);
		
		if (type == GameMessages.TYPE_INVITE)
			view.setBackgroundColor(Color.rgb(0, 40, 0));
		else if (type == GameMessages.TYPE_CHALLENGE)
			view.setBackgroundColor(Color.rgb(40, 0, 0));
		else if (type == GameMessages.TYPE_GENERIC)
			view.setBackgroundColor(Color.rgb(0, 0, 0));
		
		//TODO: Facebook
		RemoteImageView image = (RemoteImageView)view.findViewById(R.id.msg_item_image);
		if (imgUrl != null) {
			image.setLocalURI(Common.getCacheFileName(imgUrl));
			image.setRemoteURI(imgUrl);
		}
		//image.setVisibility(View.GONE);
		
		TextView nameText = (TextView)view.findViewById(R.id.msg_item_name);
		nameText.setText(firstName + " " + lastName);
		
		TextView subjectText = (TextView)view.findViewById(R.id.msg_item_subject);
		subjectText.setText(subject);
		
		TextView timeText = (TextView)view.findViewById(R.id.msg_item_time);
		Calendar c = Calendar.getInstance();
		int yearNow = c.get(Calendar.YEAR);
		int monthNow = c.get(Calendar.MONTH) + 1;
		int dayNow = c.get(Calendar.DAY_OF_MONTH);
		c.setTimeInMillis(sendTime);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		if (year == yearNow && month == monthNow && day == dayNow) {
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			timeText.setText(Common.timeString(hour, minute));
		}
		else {
			timeText.setText(month + "/" + day + "/" + year);
		}
		
		if (mFull) {
			String body = cursor.getString(7);
			TextView bodyText = (TextView)view.findViewById(R.id.msg_item_body);
			if (body == null) {
				bodyText.setVisibility(View.GONE);
				bodyText.setText("");
			}
			else {
				bodyText.setVisibility(View.VISIBLE);
				bodyText.setText(body);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		if (mFull) {
			final View v = LayoutInflater.from(context).inflate(R.layout.message_item_full, parent, false);
			bindView(v, context, cursor);
			return v;
		}
		else {
			final View v = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
			bindView(v, context, cursor);
			return v;
		}
	}

}
