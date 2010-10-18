package edu.stanford.cs.sujogger.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.sujogger.R;

public class UserListAdapter extends CursorAdapter {
	private static final String TAG = "OGT.UserListAdapter";
	private boolean mFlinging = false;
	private boolean[] checkmarks;
	private int numChecked;
	private Cursor mCursor;
	
	public UserListAdapter(Context context, Cursor c, boolean showCheckBoxes, long[] initialUserIds) {
		this(context, c, true, showCheckBoxes, initialUserIds);
	}

	public UserListAdapter(Context context, Cursor c, boolean autoRequery, boolean showCheckBoxes, long[] initialUserIds) {
		super(context, c, autoRequery);
		mCursor = c;
		if (showCheckBoxes) {
			checkmarks = new boolean[c.getCount()];
			
			if (initialUserIds == null)
				clearAllChecked();
			else {
				int initPos = 0, cursorPos = 0;
				mCursor.moveToPosition(-1);
				while(mCursor.moveToNext() && initPos < initialUserIds.length) {
					if (mCursor.getLong(1) != initialUserIds[initPos])
						checkmarks[cursorPos] = false;
					else {
						checkmarks[cursorPos] = true;
						initPos++;
					}
					cursorPos++;
				}
				numChecked = initialUserIds.length;
			}
		}
		else
			checkmarks = null;
	}
	
	public Object getItem(int position) {
		if (position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			int userId = mCursor.getInt(1);
			if (userId > 0)
				return userId;
			else
				return mCursor.getLong(2);
		}
		else return null;
	}
	
	public User getUser(int position) {
		if (position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			User user = new User();
			user.id = mCursor.getInt(1);
			user.fb_id = mCursor.getLong(2);
			user.first_name = mCursor.getString(3);
			user.last_name = mCursor.getString(4);
			return user;
		}
		else return null;
	}
	
	public int getNumChecked() {
		return numChecked;
	}
	
	public boolean isCheckedAtPosition(int position) {
		if (position >= 0 && position < checkmarks.length)
			return checkmarks[position];
		else return false;
	}
	
	public int toggleItemAtPosition(int position) {
		if (position >= 0 && position < checkmarks.length) {
			checkmarks[position] = !checkmarks[position];
			if (checkmarks[position])
				numChecked++;
			else
				numChecked--;
			return numChecked;
		}
		else return -1;
	}
	
	public void clearAllChecked() {
		for (int i = 0; i < checkmarks.length; i++)
			checkmarks[i] = false;
		numChecked = 0;
	}
	
	public long[] getCheckedUserIds() {
		long[] checkedUsers = new long[numChecked];
		int pos = 0;
		for (int i = 0; i < checkmarks.length; i++) {
			if (checkmarks[i]) {
				mCursor.moveToPosition(i);
				checkedUsers[pos] = mCursor.getLong(1);
				pos++;
			}
		}
		return checkedUsers;
	}
	
	public User[] getCheckedUsers() {
		User[] checkedUsers = new User[numChecked];
		int pos = 0;
		for (int i = 0; i < checkmarks.length; i++) {
			if (checkmarks[i]) {
				mCursor.moveToPosition(i);
				User newUser = new User();
				newUser.id = mCursor.getInt(1);
				newUser.fb_id = mCursor.getInt(2);
				newUser.first_name = mCursor.getString(3);
				newUser.last_name = mCursor.getString(4);
				checkedUsers[pos] = newUser;
				pos++;
			}
		}
		return checkedUsers;
	}
	
	@Override
	public void notifyDataSetChanged() {
		if (checkmarks != null) {
			checkmarks = new boolean[mCursor.getCount()];
			clearAllChecked();
		}
		super.notifyDataSetChanged();
	}
	
	/*
	public void setFlinging(boolean flinging) {
		mFlinging = flinging;
	}
	*/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View ret = super.getView(position, convertView, parent);
		if (ret != null) {
			//TODO: Facebook
			RemoteImageView image = (RemoteImageView) ret.findViewById(R.id.user_image);
			if (image != null && !mFlinging) {
				Log.d(TAG, "getView(): trying to fetch image");
				image.loadImage();
			}
			
			if (checkmarks != null && position >= 0 && position < checkmarks.length) {
				View check = ret.findViewById(R.id.user_check);
				if (checkmarks[position])
					check.setVisibility(View.VISIBLE);
				else
					check.setVisibility(View.INVISIBLE);
			}
		}
		return ret;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String firstName = cursor.getString(3);
		String lastName = cursor.getString(4);
		String imgUrl = cursor.getString(5);
		
		TextView name = (TextView)view.findViewById(R.id.user_name);
		if (firstName == null)
			name.setText(lastName);
		else
			name.setText(firstName + " " + lastName);
		
		//TODO: Facebook
		RemoteImageView image = (RemoteImageView)view.findViewById(R.id.user_image);
		image.setLocalURI(Common.getCacheFileName(imgUrl));
		image.setRemoteURI(imgUrl);
		
		//image.setVisibility(View.GONE);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
