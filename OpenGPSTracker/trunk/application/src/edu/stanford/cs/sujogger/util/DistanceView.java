package edu.stanford.cs.sujogger.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class DistanceView extends TextView {
	private double mDistance;
	public DistanceView(Context context) {
		super(context);
	}

	public DistanceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DistanceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void setText( CharSequence charSeq, BufferType type ) {  
		double doubleVal;
		if( charSeq.length() == 0 ) {
			doubleVal = 0l ;
		}
		else {
			try {
				doubleVal = Double.parseDouble(charSeq.toString()) ;
			}
			catch(NumberFormatException e) {
				doubleVal = 0l;
			}
		}
		
		UnitsI18n mUnits = new UnitsI18n(getContext(), null);
		mDistance = mUnits.conversionFromMeter( doubleVal );
		String text = String.format( "%.2f %s", mDistance, mUnits.getDistanceUnit() );
		super.setText( text, type );
   }
}
