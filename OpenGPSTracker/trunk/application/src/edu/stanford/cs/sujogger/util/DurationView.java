package edu.stanford.cs.sujogger.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class DurationView extends TextView {
	private long mDurationSec;
	public DurationView(Context context) {
		super(context);
	}

	public DurationView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DurationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void setText( CharSequence charSeq, BufferType type ) {  
		long longVal;
		if( charSeq.length() == 0 ) {
			longVal = 0l ;
		}
		else {
			try {
				longVal = Long.parseLong(charSeq.toString()) ;
			}
			catch(NumberFormatException e) {
				longVal = 0l;
			}
		}
		this.mDurationSec = longVal / 1000;
		
		long hours, minutes, seconds;
		hours = mDurationSec / 3600;
		longVal = mDurationSec - (hours * 3600);
		minutes = mDurationSec / 60;
		mDurationSec = mDurationSec - (minutes * 60);
		seconds = mDurationSec;
		
		String text = "";
		text += String.format("%02d", hours) + ":";
		text += String.format("%02d", minutes) + ":";
		text += String.format("%02d", seconds);
		super.setText( text, type );
   }
}
