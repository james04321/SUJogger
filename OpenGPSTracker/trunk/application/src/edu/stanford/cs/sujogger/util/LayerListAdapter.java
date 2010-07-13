package edu.stanford.cs.sujogger.util;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LayerListAdapter extends ArrayAdapter<Layer> {
		Context context;
		ArrayList<Layer> locationEntries;
		public LayerListAdapter(Context context, int textViewResourceId,
				ArrayList<Layer> items) {
			super(context, textViewResourceId, items);
			this.locationEntries = items;
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			/*
			if (view == null) {
				LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.journal, null);
			}
			Layer Layer = locationEntries.get(position);
			if (view != null) {
				TextView titleView = (TextView)view.findViewById(R.id.listitem_name);
				titleView.setText(Layer.toString());
				
				DateView creationTimeView = (DateView)view.findViewById(R.id.listitem_from);
				creationTimeView.setText(new Long(Layer.created_at).toString());
		
			}
			*/
//			view.setId(Layer.id);
			return view;
		}


	}



