/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")savedInstanceState;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.stanford.cs.sujogger.viewer;

import java.util.ArrayList;
import java.util.Hashtable;

import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Obj;
import edu.stanford.cs.gaming.sdk.model.ObjProperty;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;

import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.actions.utils.TrackCreator;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.DistanceView;
import edu.stanford.cs.sujogger.util.DurationView;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PeopleTrackList extends ListActivity {
	class Track {
		public int id;
		public String name;
		public int duration;
		public double distance;

	}

	public static final String TAG = "PeopleTrackList";
	private PeopleTrackListReceiver mReceiver;
	private GamingServiceConnection mGamingServiceConn;
	private Handler mHandler = new Handler();
	private ArrayList<Track> trackList;
	private Hashtable<Integer, Track> trackHash;

	private ProgressDialog mProgressDialog;
	
	//Request IDs
	private static final int OBJ_PROPS_RID = 120;
	
	private Runnable mRefreshTask = new Runnable() {
		public void run() {
			mProgressDialog = ProgressDialog.show(PeopleTrackList.this, "",
					getString(R.string.dialog_download_track_list), true);
			int user_id = PeopleTrackList.this.getIntent().getExtras().getInt("userId");
			
			try {
				String[] names = new String[3];
				names[0] = "name";
				names[1] = "duration";
				names[2] = "distance";
				mGamingServiceConn.getObjProperties(OBJ_PROPS_RID, user_id, -1, "track", names);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	private DatabaseHelper mDbHelper;
	
	class PeopleTrackListReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGamingServiceConn.getNextPendingNotification()) != null) {
					Common.log(TAG, appResponse.toString());
					Common.log(TAG, "PUBLISHGPXReceiver: Response received with request id:"
							+ appResponse.request_id);
					
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						PeopleTrackList.this.runOnUiThread(new Runnable() {
							public void run() {
								if (mProgressDialog != null) mProgressDialog.dismiss();
								Toast toast = Toast.makeText(PeopleTrackList.this, 
										R.string.connection_error_toast, Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						continue;
					}
					
					switch (appResponse.request_id) {
					case OBJ_PROPS_RID:
						final PeopleTrackListAdapter adapter = createAdapter((ObjProperty[]) appResponse.object);
						PeopleTrackList.this.runOnUiThread(new Runnable() {
							public void run() {
								setListAdapter(adapter);
								Common.log(TAG, "HERE 3");
								mProgressDialog.dismiss();
							}
						});
						break;
					case 101:
						Common.log(TAG, "PUBLISHGPXReceiver: Response received with request id: "
								+ appResponse.request_id);
						Common.log(TAG, "Response is: " + appResponse);
						Obj[] objArray = (Obj[]) appResponse.object;
						for (int i = 0; i < objArray.length; i++) {
							for (int j = 0; j < objArray[i].object_properties.length; j++) {
								Common.log(TAG, "STRING_VAL IS: "
										+ objArray[i].object_properties[j].string_val);
							}
						}
					default:
						break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Common.log(TAG, "HERE1");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.list_simple);
		
		TextView emptyView = (TextView)getListView().getEmptyView();
		emptyView.setText(R.string.no_tracks);
		
		mReceiver = new PeopleTrackListReceiver();
		mGamingServiceConn = new GamingServiceConnection(this, mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, this.getClass().getName());
		mGamingServiceConn.bind();
		User user = Common.getRegisteredUser(this);
		mGamingServiceConn.setUserId(user.id, user.fb_id, user.fb_token);
		
		trackList = new ArrayList<Track>();
		trackHash = new Hashtable<Integer, Track>();
		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		mHandler.postDelayed(mRefreshTask, 100);
	}

	public void finalize() {
		mDbHelper.close();
		mGamingServiceConn.unbind();
		finish();
		onDestroy();
	}

	public void onDestroy() {
		mDbHelper.close();
		mGamingServiceConn.unbind();
		super.onDestroy();
	}

	protected PeopleTrackListAdapter createAdapter(ObjProperty[] objProp) {
		Common.log("TAG", "HERE5");
		// String[] testValues = new String[0];
		if (objProp != null && objProp.length > 0) {
			// testValues = new String[objProp.length];
			for (int i = 0; i < objProp.length; i++) {
				if (trackHash.get(objProp[i].obj_id) == null) {
					Track track = new Track();
					track.id = objProp[i].obj_id;
					trackHash.put(track.id, track);
				}
				Track track = trackHash.get(objProp[i].obj_id);
				Common.log(TAG, "ASLAI OBJPROP[i].NAME is " + objProp[i].name);
				if ("name".equals(objProp[i].name)) {
					trackList.add(0, track);
					track.name = objProp[i].string_val;
					// testValues[i] = objProp[i].string_val;
				}
				else if ("duration".equals(objProp[i].name)) {
					track.duration = objProp[i].int_val;
				}
				else if ("distance".equals(objProp[i].name)) {
					track.distance = objProp[i].float_val;
				}
			}
		}
		Common.log("TAG", "HERE6");

		/*
		 * // Create some mock data String[] testValues = new String[] {
		 * "Test1", "Test2", "Test3" };
		 */

		// Create a simple array adapter (of type string) with the test values
		PeopleTrackListAdapter adapter = new PeopleTrackListAdapter(PeopleTrackList.this,
				R.layout.trackitem, trackList);

		return adapter;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);

		int objId = trackList.get(position).id;
		String name = trackHash.get(objId).name;
		Common.log(TAG, "objId = " + objId);
		if (mDbHelper.getIdFromTrackId(objId) == -1) {
		TrackCreator trackCreator = new TrackCreator(this);
		trackCreator.downloadTrack(objId, name);
		} else
			Toast.makeText(this, "The track is already downloaded", Toast.LENGTH_LONG).show();
	}
	
	class PeopleTrackListAdapter extends ArrayAdapter<Track> {
		private ArrayList<Track> items;

		public PeopleTrackListAdapter(Context context, int textViewResourceId,
				ArrayList<PeopleTrackList.Track> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.trackitem, null);
			}
			Track o = items.get(position);
			if (o != null) {
				TextView titleView = (TextView) v.findViewById(R.id.listitem_name);
				titleView.setText(o.name);
				
				DistanceView distanceView = (DistanceView) v.findViewById(R.id.listitem_distance);
				distanceView.setText("" + o.distance);

				DurationView durationView = (DurationView) v.findViewById(R.id.listitem_duration);
				durationView.setText("" + o.duration);
				
				ImageView iconView = (ImageView) v.findViewById(R.id.listitem_icon);
				iconView.setVisibility(View.GONE);
			}
			return v;
		}

	}

}
