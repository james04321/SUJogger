package edu.stanford.cs.sujogger.util;

import edu.stanford.cs.sujogger.R;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class SegmentedControl extends LinearLayout {
	private static final int BUTTON_HEIGHT = 34;
	private Button mLeftButton, mRightButton;
	private int mSelectedSegmentIndex;
	private String[] mItems;
	private SegmentedControlListener mListener;
	
	public SegmentedControl(Context context, String[] items, int initIdx, SegmentedControlListener listener) {
		super(context);
		mItems = items;
		mSelectedSegmentIndex = initIdx;
		mListener = listener;
		
		this.setOrientation(HORIZONTAL);
		final float scale = context.getResources().getDisplayMetrics().density;
		
		mLeftButton = new Button(context);
		mLeftButton.setText(items[0]);
		if (initIdx == 0) 
			mLeftButton.setBackgroundResource(R.drawable.btn_sc_left_selected);
		else
			mLeftButton.setBackgroundResource(R.drawable.btn_sc_left);
		mLeftButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mSelectedSegmentIndex != 0) {
					setSelectedSegmentIndex(0);
					mListener.onValueChanged(0);
				}
			}
		});
		addView(mLeftButton, 
				new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, (int)(BUTTON_HEIGHT * scale + 0.5f), 1));
		
		mRightButton = new Button(context);
		mRightButton.setText(items[1]);
		if (initIdx == 1)
			mRightButton.setBackgroundResource(R.drawable.btn_sc_right_selected);
		else
			mRightButton.setBackgroundResource(R.drawable.btn_sc_right);
		mRightButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mSelectedSegmentIndex != 1) {
					setSelectedSegmentIndex(1);
					mListener.onValueChanged(1);
				}
			}
		});
		addView(mRightButton, 
				new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, (int)(BUTTON_HEIGHT * scale + 0.5f), 1));
	}
	
	public int selectedSegmentIndex() {
		return mSelectedSegmentIndex;
	}
	
	public void setSelectedSegmentIndex(int idx) {
		if (idx > mItems.length-1)
			idx = mItems.length-1;
		if (idx < 0)
			idx = 0;
		mSelectedSegmentIndex = idx;
		
		switch(mSelectedSegmentIndex) {
		case 0: 
			mLeftButton.setBackgroundResource(R.drawable.btn_sc_left_selected);
			mRightButton.setBackgroundResource(R.drawable.btn_sc_right);
			break;
		case 1: 
			mLeftButton.setBackgroundResource(R.drawable.btn_sc_left);
			mRightButton.setBackgroundResource(R.drawable.btn_sc_right_selected);
			break;
		}
	}
	
	public static interface SegmentedControlListener {
		public void onValueChanged(int newValue);
	}
}
