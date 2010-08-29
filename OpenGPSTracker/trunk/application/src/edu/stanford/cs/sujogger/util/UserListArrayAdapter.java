package edu.stanford.cs.sujogger.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.sujogger.R;

public class UserListArrayAdapter extends ArrayAdapter<User>{
	public UserListArrayAdapter(Context context, User[] users) {
		super(context, 0, users);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null)
			view = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);

		User user = getItem(position);
		if (user != null) {
			TextView grpTitle = (TextView) view.findViewById(R.id.user_name);
			grpTitle.setText(user.first_name + " " + user.last_name);
		}
		
		//TODO: Facebook
		RemoteImageView image = (RemoteImageView)view.findViewById(R.id.user_image);
		//if (imgUrl != null) {
		//	image.setLocalURI(Common.getCacheFileName(imgUrl));
		//	image.setRemoteURI(imgUrl);
		//}
		image.setVisibility(View.GONE);

		return view;
	}
}
