package edu.stanford.cs.sujogger.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.stanford.cs.gaming.sdk.model.Group;
import edu.stanford.cs.sujogger.R;

public class GroupListArrayAdapter extends ArrayAdapter<Group> {
	public GroupListArrayAdapter(Context context, Group[] groups) {
		super(context, 0, groups);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null)
			view = LayoutInflater.from(getContext()).inflate(R.layout.grp_list_item, parent, false);

		Group group = getItem(position);
		if (group != null) {
			TextView grpTitle = (TextView) view.findViewById(R.id.grp_name);
			grpTitle.setText(group.name);

			TextView grpReadCount = (TextView) view.findViewById(R.id.grp_indicator);
			grpReadCount.setText(String.valueOf(group.users.length));
		}

		return view;
	}
}
