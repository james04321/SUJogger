package edu.stanford.cs.sujogger.viewer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.GameMessages;
import edu.stanford.cs.sujogger.db.GPStracking.Users;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.GameMessageAdapter;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;
import edu.stanford.cs.sujogger.util.UserListAdapter;

public class GameMessageDetail extends ListActivity {
	private static final String TAG = "OGT.GameMessageDetail";
	
	private long mMessageId;
	private int mMessageType;
	private DatabaseHelper mDbHelper;
	private Cursor mMessage;
	private long[] mReplyRecipientIds;
	private Cursor mPeople;
	private SeparatedListAdapter mGroupAdapter;
	private List<Map<String,?>> mActions;
	private int mDidStart;

	public GameMessageDetail() {}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.list_simple);
		
		mMessageId = savedInstanceState != null ? savedInstanceState.getLong(GameMessages.TABLE) : 0;
		if (mMessageId == 0) {
			Bundle extras = getIntent().getExtras();
			mMessageId = extras != null ? extras.getLong(GameMessages.TABLE) : 0;
		}
		
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mMessage = mDbHelper.getMessageWithId(mMessageId);
		startManagingCursor(mMessage);
		mMessage.moveToFirst();
		mPeople = mDbHelper.getUsersForMessage(mMessageId, mMessage.getLong(1));
		startManagingCursor(mPeople);
		
		mMessageType = mMessage.getInt(2);
		mDidStart = mMessage.getInt(9);
		
		
		mReplyRecipientIds = new long[mPeople.getCount()-1];
		int i = 0;
		mPeople.moveToPosition(-1);
		while(i < mReplyRecipientIds.length && mPeople.moveToNext()) {
			if (mPeople.getLong(1) != Common.getRegisteredUser(this).id) {
				mReplyRecipientIds[i] = mPeople.getLong(1);
				i++;
			}
		}
		
		Log.d(TAG, Arrays.toString(mReplyRecipientIds));
		
		mPeople.moveToFirst();
		
		mActions = new LinkedList<Map<String,?>>();
		
		if (!(mPeople.getCount() == 1 && mPeople.getInt(1) == Common.getRegisteredUser(this).id)) {
			if ((mMessageType == GameMessages.TYPE_INVITE || mMessageType == GameMessages.TYPE_CHALLENGE) &&
					mDidStart == 0)
				mActions.add(Common.createItem("Accept and start"));
			if (mPeople.getCount() > 1)
				mActions.add(Common.createItem("Reply all"));
			else
				mActions.add(Common.createItem("Reply"));
		}
		
		fillData();
	}
	
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		Log.d(TAG, "onResume(): mMessageId = " + mMessageId);
		DatabaseUtils.dumpCursor(mMessage);
	}
	
	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(GameMessages.TABLE, mMessageId);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		
		if (position < 3) return;
		
		if (mActions.size() > 0) {
			if ((mMessageType == GameMessages.TYPE_INVITE || mMessageType == GameMessages.TYPE_CHALLENGE) &&
					mDidStart == 0) {
				if (position == 3) {
					startTrack();
					return;
				}
				else if (position == 4) {
					startMessageSender();
					return;
				}
			}
			else {
				if (position == 3) {
					startMessageSender();
					return;
				}
			}
		}
		
		Log.d(TAG, "onListItemClick(): user id = " + (Integer)mGroupAdapter.getItem(position));
	}
	
	private void startTrack() {
		Log.d(TAG, "startTrack()");
		mDbHelper.setMessageToStarted(mMessage.getLong(0));
		
		Intent intent = new Intent(this, LoggerMap.class);
		intent.putExtra(LoggerMap.PARTNER_RUN_KEY, true);
		startActivity(intent);
	}
	
	private void startMessageSender() {
		Log.d(TAG, "startMessageSender()");
		
		Intent intent = new Intent(this, MessageSender.class);
		intent.putExtra(Users.TABLE, mReplyRecipientIds);
		if (mMessageType == GameMessages.TYPE_GENERIC)
			intent.putExtra(GameMessages.SUBJECT, "Re: " + mMessage.getString(6));
		startActivity(intent);
	}
	
	private void fillData() {
		mGroupAdapter = new SeparatedListAdapter(this);
		
		mGroupAdapter.addSection("", new GameMessageAdapter(this, mMessage, true));
		mGroupAdapter.addSection("Actions", new SimpleAdapter(this, mActions, R.layout.list_item_simple, 
				new String[] {Common.ITEM_TITLE}, new int[] {R.id.list_simple_title}));
		if (mPeople != null)
			mGroupAdapter.addSection("People", new UserListAdapter(this, mPeople, false, null));
		
		setListAdapter(mGroupAdapter);
	}
}
