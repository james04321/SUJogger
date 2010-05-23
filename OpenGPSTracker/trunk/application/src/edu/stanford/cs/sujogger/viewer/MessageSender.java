package edu.stanford.cs.sujogger.viewer;

import java.util.Arrays;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.GameMessages;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.Messages;
import edu.stanford.cs.sujogger.db.GPStracking.Users;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.MessageObject;

public class MessageSender extends Activity {
	private static final String TAG = "OGT.MessageSender";
	private static final int MSG_SEND_RID = 1;
	
	private long mGroupId;
	private long[] mUserIds;
	private User[] mUsers;
	private DatabaseHelper mDbHelper;
	
	private GamingServiceConnection mGameCon;
	private MessageSenderReceiver mReceiver;
	
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	
	//Request codes
	private static final int ACTIVITY_GROUPPICKER = 0;
	private static final int ACTIVITY_FRIENDPICKER = 1;
	
	// Date and Time
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;

	// Views
	private View datetimeLayout;
	private Button allButton, groupButton, friendsButton, dateButton, timeButton, sendButton;
	private Spinner msgSpinner;
	private EditText subjectText, bodyText;
	private TextView msgRecipientText;

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDateTime();
		}
	};

	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateDateTime();
		}
	};

	public MessageSender() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messagesender);

		mGroupId = savedInstanceState != null ? savedInstanceState.getLong(Groups.TABLE) : 0;
		if (mGroupId == 0) {
			Bundle extras = getIntent().getExtras();
			mGroupId = extras != null ? extras.getLong(Groups.TABLE) : 0;
		}
		
		long userId = savedInstanceState != null ? savedInstanceState.getLong(Users.TABLE) : 0;
		if (userId == 0) {
			Bundle extras = getIntent().getExtras();
			userId = extras != null ? extras.getLong(Users.TABLE) : 0;
		}

		Log.d(TAG, "onCreate(): groupId = " + mGroupId);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mReceiver = new MessageSenderReceiver(); 
		mGameCon = new GamingServiceConnection(this, mReceiver, 
				Constants.APP_ID, Constants.APP_API_KEY, Feed.class.toString());
		mGameCon.bind();

		setTitle("New Message");
		
		if (userId == 0) {
			mUserIds = null;
			mUsers = null;
		}
		else {
			mUserIds = new long[] {userId};
			mUsers = mDbHelper.getUserArrayForUserIds(mUserIds);
		}

		msgSpinner = (Spinner) findViewById(R.id.msg_type_spinner);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, GameMessages.types);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		msgSpinner.setAdapter(spinnerAdapter);
		msgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Log.d(TAG, "spinner position = " + pos);
				switch(pos) {
				case GameMessages.TYPE_GENERIC: 
					datetimeLayout.setVisibility(View.GONE);
					subjectText.setEnabled(true);
					subjectText.setHint("Subject");
					subjectText.requestFocus();
					break;
				case GameMessages.TYPE_INVITE:
					datetimeLayout.setVisibility(View.VISIBLE);
					subjectText.setEnabled(false);
					subjectText.setHint("Someone invites you to a run");
					bodyText.requestFocus();
					break;
				case GameMessages.TYPE_CHALLENGE: 
					datetimeLayout.setVisibility(View.VISIBLE);
					subjectText.setEnabled(false);
					subjectText.setHint("Someone challenges you to a run");
					bodyText.requestFocus();
					break;
				default: break;
				}
			}
			
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		subjectText = (EditText) findViewById(R.id.msg_subject);
		bodyText = (EditText) findViewById(R.id.msg_body);
		
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
		
		datetimeLayout = findViewById(R.id.msg_datetime_layout);
		
		allButton = (Button) findViewById(R.id.msg_to_all);
		allButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mGroupId = 0;
				mUserIds = null;
				mUsers = null;
				updateRecipients();
			}
		});
		
		groupButton = (Button) findViewById(R.id.msg_to_group);
		groupButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				MessageSender.this.startActivityForResult(
						new Intent(MessageSender.this, GroupPicker.class), ACTIVITY_GROUPPICKER);
			}
		});

		friendsButton = (Button) findViewById(R.id.msg_to_friends);
		friendsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(MessageSender.this, FriendPicker.class);
				i.putExtra(FriendPicker.MODE_KEY, FriendPicker.MODE_RECIPIENT);
				i.putExtra(Users.TABLE, mUserIds);
				startActivityForResult(i, ACTIVITY_FRIENDPICKER);
			}
		});

		dateButton = (Button) findViewById(R.id.msg_date);
		dateButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		timeButton = (Button) findViewById(R.id.msg_time);
		timeButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});
		
		sendButton = (Button) findViewById(R.id.msg_send);
		sendButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				sendMessage();
			}
		});
		
		msgRecipientText = (TextView)findViewById(R.id.msg_to);
		updateRecipients();
		
		updateDateTime();
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
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mGameCon.unbind();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Groups.TABLE, mGroupId);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false);
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mHour, mMinute);
			break;
		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
			break;
		}
	}
	
	private void updateRecipients() {
		if (mGroupId == 0 && mUsers == null)
			msgRecipientText.setText("To: All");
		else if (mGroupId > 0) {
			Cursor groupInfo = mDbHelper.getGroupWithId(mGroupId);
			String groupName = null;
			if (groupInfo.moveToFirst())
				groupName = groupInfo.getString(2);
			groupInfo.close();
			
			msgRecipientText.setText("To: " + groupName);
		}
		else if (mUsers != null) {
			msgRecipientText.setText("To: " + Common.nameListForUsers(mUsers));
		}
	}

	private void updateDateTime() {
		dateButton.setText(new StringBuilder()
			.append(mMonth+1).append("-")
			.append(mDay).append("-")
			.append(mYear));
		
		int hour12 = mHour;
		String ampm = "AM";
		if (hour12 >= 12) {
			ampm = "PM";
			if (hour12 > 12)
				hour12 -= 12;
		}
		else if (hour12 == 0)
			hour12 = 12;
		timeButton.setText(new StringBuilder()
			.append(hour12).append(":")
			.append(pad(mMinute)).append(" ")
			.append(ampm));
	}
	
	private User[] getArrayOfRecipients() {
		if (mUsers == null) {
			
		}
		else return mUsers;
		return null;
	}
	
	private void sendMessage() {
		Calendar c = Calendar.getInstance();
		c.set(mYear, mMonth, mDay, mHour, mMinute);
		long now = System.currentTimeMillis();
		MessageObject msgObject= new MessageObject(now, c.getTimeInMillis(), 
				subjectText.getText().toString(), bodyText.getText().toString());
		try {
			mGameCon.sendMessage(MSG_SEND_RID, msgObject, Messages.TYPE_GM, 
					Common.getRegisteredUser(), null, mUsers, now);	
		} catch (RemoteException e) {}
	}
	
	private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent == null) return;
		Bundle extras = intent.getExtras();
		
		switch(requestCode) {
		case ACTIVITY_GROUPPICKER: 
			mGroupId = extras.getLong(Groups.TABLE);
			Log.d(TAG, "onActivityResult(): groupId = " + mGroupId);
			updateRecipients();
			break;
		case ACTIVITY_FRIENDPICKER: 
			mUserIds = extras.getLongArray(Users.TABLE);
			Log.d(TAG, "onActivityResult(): groupIds = " + Arrays.toString(mUserIds));
			mUsers = mDbHelper.getUserArrayForUserIds(mUserIds);
			updateRecipients();
			break;
		default: break;
		}
	}
	
	// Empty receiver
	class MessageSenderReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {}
	}
}
