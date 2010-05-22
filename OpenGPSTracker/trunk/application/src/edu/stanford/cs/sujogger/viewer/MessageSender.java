package edu.stanford.cs.sujogger.viewer;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
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
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.Messages;

public class MessageSender extends Activity {
	private static final String TAG = "OGT.MessageSender";
	private long mGroupId;
	private DatabaseHelper mDbHelper;

	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;

	// Date and Time
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;

	// Views
	private View datetimeLayout;
	private Button addButton, dateButton, timeButton, sendButton;
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

		Log.d(TAG, "onCreate(): groupId = " + mGroupId);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();

		setTitle("New Message");

		

		msgSpinner = (Spinner) findViewById(R.id.msg_type_spinner);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Messages.types);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		msgSpinner.setAdapter(spinnerAdapter);
		msgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Log.d(TAG, "spinner position = " + pos);
				switch(pos) {
				case Messages.TYPE_GENERIC: 
					datetimeLayout.setVisibility(View.GONE);
					subjectText.setEnabled(true);
					subjectText.setHint("Subject");
					subjectText.requestFocus();
					break;
				case Messages.TYPE_INVITE:
					datetimeLayout.setVisibility(View.VISIBLE);
					subjectText.setEnabled(false);
					subjectText.setHint("Someone invites you to a run");
					bodyText.requestFocus();
					break;
				case Messages.TYPE_CHALLENGE: 
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
		addButton = (Button) findViewById(R.id.msg_to_add);

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
				// TODO Auto-generated method stub
				
			}
		});
		
		msgRecipientText = (TextView)findViewById(R.id.msg_to);
		if (mGroupId > 0) {
			Cursor groupInfo = mDbHelper.getGroupWithId(mGroupId);
			String groupName = null;
			if (groupInfo.moveToFirst())
				groupName = groupInfo.getString(2);
			groupInfo.close();
			
			msgRecipientText.setText("To: " + groupName);
			addButton.setVisibility(View.GONE);
		}
		
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
	
	private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
