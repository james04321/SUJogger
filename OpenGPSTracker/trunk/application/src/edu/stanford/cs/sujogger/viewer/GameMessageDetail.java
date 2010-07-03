package edu.stanford.cs.sujogger.viewer;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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
	private int mDidStart;
	
	private Button mStartButton;
	private Button mReplyButton;

	public GameMessageDetail() {}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.gamemessagedetail);
		
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
		
		mPeople.moveToPosition(-1);
		ArrayList<Long> recipients = new ArrayList<Long>(mPeople.getCount());
		while(mPeople.moveToNext())
			if (mPeople.getLong(1) != Common.getRegisteredUser(this).id)
				recipients.add(mPeople.getLong(1));
		
		mReplyRecipientIds = new long[recipients.size()];
		for (int i = 0; i < mReplyRecipientIds.length; i++)
			mReplyRecipientIds[i] = recipients.get(i);
		
		//Log.d(TAG, Arrays.toString(mReplyRecipientIds));
		
		mPeople.moveToFirst();
		
		mStartButton = (Button)findViewById(R.id.startbutton);
		mStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startTrack();
			}
		});
		mStartButton.setVisibility(View.GONE);
		
		mReplyButton = (Button)findViewById(R.id.replybutton);
		mReplyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startMessageSender();
			}
		});
		
		if (!(mPeople.getCount() == 1 && mPeople.getInt(1) == Common.getRegisteredUser(this).id)) {
			if ((mMessageType == GameMessages.TYPE_INVITE || mMessageType == GameMessages.TYPE_CHALLENGE) &&
					mDidStart == 0)
				mStartButton.setVisibility(View.VISIBLE);
			if (!(mPeople.getCount() > 1))
				mReplyButton.setText("Reply");
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
		
		Log.d(TAG, "onListItemClick(): user id = " + (Integer)mGroupAdapter.getItem(position));
	}
	
	private void startTrack() {
		Log.d(TAG, "startTrack()");
		mDbHelper.setMessageToStarted(mMessageId);
		
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
		if (mPeople != null)
			mGroupAdapter.addSection("People", new UserListAdapter(this, mPeople, false, null));
		
		setListAdapter(mGroupAdapter);
	}
}
