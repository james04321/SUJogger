package edu.stanford.cs.sujogger.viewer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.actions.PublishGPX;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking;
import edu.stanford.cs.sujogger.db.GPStracking.Stats;
import edu.stanford.cs.sujogger.util.Common;

import edu.stanford.cs.sujogger.util.Statistic;


import android.app.ListActivity;



public class StatisticsView extends ListActivity {

	ArrayList<Statistic> mStats;
	DatabaseHelper mDb;
	long mGroupId;
	public static final String TAG = "StatisticsView";



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      mGroupId = getIntent().getExtras().getLong("group_id");
      Log.d(TAG, "mGroupId is "+ mGroupId);
      mDb = new DatabaseHelper(this);
      mStats = mDb.getStatistics(mGroupId);     
      setContentView(R.layout.list_simple);

  	StatsListAdapter adapter = new StatsListAdapter(this, R.layout.ach_line, mStats);
    setListAdapter(adapter);

        //View view = (View) findViewById(R.layout.ach_list);
        //Log.d(TAG, "View is " + view);

        //setContentView(view);
//        View view = this.findViewById(R.id.AvgSpeedTitle);
//        View
//        Log.d(TAG, "VIEW TAG IS "+ view.getTag());
//        view.findViewWithTag(tag)
 //       view.find
       

    }
    

    public void setStatistics() {
        mDb = new DatabaseHelper(this);
        mDb.getStatistics(mGroupId);    	
    }

    
	   class StatsListAdapter extends ArrayAdapter<Statistic> {
		   private ArrayList<Statistic> items;
		   Context context;
	        public StatsListAdapter(Context context, int textViewResourceId, ArrayList<Statistic> items) {
                super(context, textViewResourceId, items);
                this.context = context;
                this.items = items;
        }

	        public View getView(int position, View convertView, ViewGroup parent) {
	                View v = convertView;
	                if (v == null) {
	                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                    v = vi.inflate(R.layout.ach_line, null);
	                }
	                Statistic o = items.get(position);
	                if (o != null) {
	            		TextView titleView = (TextView) v.findViewById(R.id.lb_name);
	            		Log.d(TAG, "O.STAT_ID IS " + o.stat_id);
	            		titleView.setText(GPStracking.Stats.ALL_STAT_TITLES[o.stat_id]);
	            		double value = o.value;
	            		TextView valueText = (TextView) v.findViewById(R.id.lb_value);
	            		Context context = StatisticsView.this;
	            		switch(o.stat_id) {
	            		case Stats.DISTANCE_RAN_ID:
	            		case Stats.DISTANCE_RAN_WEEK_ID:
	            		case Stats.DISTANCE_RAN_MONTH_ID:
	            			valueText.setText(Common.distanceString(context, value));
	            			break;
	            		case Stats.RUNNING_TIME_ID:
	            		case Stats.RUNNING_TIME_WEEK_ID:
	            		case Stats.RUNNING_TIME_MONTH_ID:
	            			valueText.setText(Common.durationString(context, (long) value));
	            			break;
	            		case Stats.AVG_SPEED_ID:
	            		case Stats.AVG_SPEED_WEEK_ID:
	            		case Stats.AVG_SPEED_MONTH_ID:
	            			valueText.setText(Common.speedString(context, value));
	            			break;
	            		case Stats.NUM_RUNS_ID:
	            		case Stats.NUM_RUNS_WEEK_ID:
	            		case Stats.NUM_RUNS_MONTH_ID:
	            		case Stats.NUM_PARTNER_RUNS_ID:
	            		case Stats.NUM_PARTNER_RUNS_WEEK_ID:
	            		case Stats.NUM_PARTNER_RUNS_MONTH_ID:
	            			valueText.setText(String.valueOf(value));
	            			break;
	            		} 
	            		
	            		titleView.setText(GPStracking.Stats.ALL_STAT_TITLES[o.stat_id]);
	            		
	                }
	                return v;
	        }
	   
	   }
}
 