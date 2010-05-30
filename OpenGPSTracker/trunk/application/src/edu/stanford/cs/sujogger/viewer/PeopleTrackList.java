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
import java.util.List;

import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Obj;
import edu.stanford.cs.gaming.sdk.model.ObjProperty;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;

import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.actions.PublishGPX;
import edu.stanford.cs.sujogger.actions.Statistics;
import edu.stanford.cs.sujogger.actions.utils.TrackCreator;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.DateView;
import edu.stanford.cs.sujogger.util.DistanceView;
import edu.stanford.cs.sujogger.util.DurationView;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
 
public class PeopleTrackList extends ListActivity {
	class Track {
		public int id;
		public String name;
		public int duration;
		public double distance;
		
	}
	   private static final int MENU_DOWNLOAD_TRACK = 0;
	
	public static final String TAG = "PeopleTrackList";
	   private PeopleTrackListReceiver mReceiver;
	   private GamingServiceConnection mGamingServiceConn;
	   private ArrayList<Track> trackList;
	   private Hashtable<Integer, Track> trackHash;

	private ProgressDialog mProgressDialog;
	class PeopleTrackListReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGamingServiceConn.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					Log.d(TAG, "PUBLISHGPXReceiver: Response received with request id:" + appResponse.request_id);
					
					switch(appResponse.request_id) {
					case 120:
//				        ListAdapter adapter = createAdapter((ObjProperty[]) appResponse.object);
						PeopleTrackListAdapter adapter = createAdapter((ObjProperty[]) appResponse.object);
				        setListAdapter(adapter);
				        Log.d(TAG, "HERE 3");
				        mProgressDialog.cancel();
				        /*
						int trackId = ((Obj) appResponse.object).id;
						Log.d(TAG, "APPRESPONSE OBJECT IS: " + appResponse.object.getClass().getName());
	//					int trackId = (Integer) appResponse.object;
						Obj obj = (Obj) appResponse.appRequest.object;
						int _id = obj.object_properties[0].int_val;
					//	Uri uri = new Uri(obj.object_properties[3].string_val);
						ContentResolver resolver = context.getContentResolver();
						ContentValues values = new ContentValues();
						values.put(Tracks.TRACK_ID, trackId);
						resolver.update(PeopleTrackList.this.getIntent().getData() , values, Tracks._ID + "=" + _id, null);
						
						//						mGamingServiceConn.getObjs(101, "track", Common.getRegisteredUser().id, -1, false);
						TrackCreator trackCreator = new TrackCreator(PeopleTrackList.this);
						trackCreator.downloadTrack(21);
						*/
						/*
						GroupList.this.toggleNewGroupItemState();
						Integer groupId = (Integer)(appResponse.object);
						Group newGroup = (Group)(appResponse.appRequest.object);
						Log.d(TAG, "onReceive(): groupId = " + groupId + "; groupName = " + newGroup.name);
						GroupList.this.mDbHelper.addGroup(groupId.longValue(), newGroup.name, 1);
						GroupList.this.mGroupsCursor.requery();
						GroupList.this.mGroupAdapter.notifyDataSetChanged();
						GroupList.this.getListView().invalidateViews();
						*/
						break;
					case 101:
						Log.d(TAG, "PUBLISHGPXReceiver: Response received with request id: " + appResponse.request_id);
						Log.d(TAG, "Response is: " + appResponse);
						Obj[] objArray = (Obj[]) appResponse.object;
						for (int i =0; i < objArray.length; i++) {
							for (int j=0; j < objArray[i].object_properties.length; j++) {
							Log.d(TAG, "STRING_VAL IS: " + objArray[i].object_properties[j].string_val);
							}
						}
					default: break;
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
    	Log.d(TAG, "HERE1");
        super.onCreate(savedInstanceState);
    	  mReceiver = new PeopleTrackListReceiver(); 
    	  mGamingServiceConn = new GamingServiceConnection(this, mReceiver, 
    				Constants.APP_ID, Constants.APP_API_KEY, this.getClass().getName());
    	  mGamingServiceConn.bind();
    	  mGamingServiceConn.setUserId(Common.getRegisteredUser().id);
          registerForContextMenu( getListView() );
    	  mProgressDialog = ProgressDialog.show(this, "", getString( R.string.dialog_download_track_list), true);
    	  trackList = new ArrayList<Track>();
    	  trackHash = new Hashtable<Integer, Track>();
    	  Log.d(TAG, "HERE2");

    	  try {
    		  String[] names = new String[3];
    		  names[0] = "name";
    		  names[1] = "duration";
    		  names[2] = "distance";
			mGamingServiceConn.getObjProperties(120, Common.getRegisteredUser().id, -1, "track", names);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 


    }
    
    public void finalize() {
  	   mGamingServiceConn.unbind();
       finish();    	
 	   onDestroy();
    }
    
    public void onDestroy() {
 	   mGamingServiceConn.unbind();
 	   super.onDestroy();
    }

    protected PeopleTrackListAdapter createAdapter(ObjProperty[] objProp)
    { 
    	Log.d("TAG", "HERE5");
//    	String[] testValues = new String[0];
    	if (objProp != null && objProp.length > 0) {
//    	 testValues = new String[objProp.length];
    	for (int i=0; i< objProp.length; i++) {
    		if (trackHash.get(objProp[i].obj_id) == null) {
    			Track track = new Track();
    			track.id = objProp[i].obj_id;
    			trackHash.put(track.id, track);
    		}
    		Track track = trackHash.get(objProp[i].obj_id);
    		Log.d(TAG, "ASLAI OBJPROP[i].NAME is " + objProp[i].name);
    		if ("name".equals(objProp[i].name)) {
    			trackList.add(track);
    			track.name = objProp[i].string_val;
//        		testValues[i] = objProp[i].string_val;
    		} else if ("duration".equals(objProp[i].name)) {
    			track.duration = objProp[i].int_val;
    		} else if ("distance".equals(objProp[i].name)) {
    			track.distance = objProp[i].float_val;
    		}
    	}
    	}
    	Log.d("TAG", "HERE6");

    	/*
    	// Create some mock data
    	String[] testValues = new String[] {
    			"Test1",
    			"Test2",
    			"Test3"
    	};
    	*/
 
    	// Create a simple array adapter (of type string) with the test values
    	PeopleTrackListAdapter adapter = new PeopleTrackListAdapter(PeopleTrackList.this, R.layout.trackitem, trackList);
 
    	return adapter;
    }
 
    
    /**
     * Creates and returns a list adapter for the current list activity
     * @return
     */
    /*
    protected ListAdapter createAdapter(ObjProperty[] objProp)
    { 
    	Log.d("TAG", "HERE5");
    	String[] testValues = new String[0];
    	if (objProp != null && objProp.length > 0) {
    	 testValues = new String[objProp.length];
    	for (int i=0; i< objProp.length; i++) {
    		if (trackHash.get(objProp[i].obj_id) == null) {
    			Track track = new Track();
    			track.id = objProp[i].obj_id;
    			trackHash.put(track.id, track);
    		}
    		Track track = trackHash.get(objProp[i].obj_id);
    		if ("name".equals(objProp[i].name)) {
    			trackList.add(track.id);
    			track.name = objProp[i].string_val;
        		testValues[i] = objProp[i].string_val;
    		} else if ("duration".equals(objProp[i].name)) {
    			track.duration = objProp[i].int_val;
    		} else if ("distance".equals(objProp[i].name)) {
    			track.distance = objProp[i].float_val;
    		}
    	}
    	}
    	Log.d("TAG", "HERE6");
    		*/
    	/*
    	// Create some mock data
    	String[] testValues = new String[] {
    			"Test1",
    			"Test2",
    			"Test3"
    	};
    	*/
 /*
    	// Create a simple array adapter (of type string) with the test values
    	ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testValues);
 
    	return adapter;
    }
*/
    /*
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		

		int objId = tracks[position].id;
		String name = tracks[position].name;
		Log.d(TAG, "objId = " + objId);
		TrackCreator trackCreator = new TrackCreator(this);
		trackCreator.downloadTrack(objId, name);

		return;
	}
*/
	   public void onCreateContextMenu( ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo )
	   {
		   /*
	      if( menuInfo instanceof AdapterView.AdapterContextMenuInfo )
	      {
	         AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
	         if (itemInfo.position < actions.size() + 1) return;
	         TextView textView = (TextView) itemInfo.targetView.findViewById( android.R.id.text1 );
	         if( textView != null )
	         {
	            menu.setHeaderTitle( textView.getText() );
	         }
	      }
	      */
	      menu.add( 0, MENU_DOWNLOAD_TRACK, 0, R.string.menu_downloadTrack );

	   }
	   public boolean onContextItemSelected( MenuItem item )
	   {
	      boolean handled = false;
	      AdapterView.AdapterContextMenuInfo info;
	      try
	      {
	         info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	      }
	      catch( ClassCastException e )
	      {
	         Log.e( TAG, "Bad menuInfo", e );
	         return handled;
	      }
	      

	      
			Log.v(TAG, "ASLAI HERE: position = " + info.position + "; id = " + trackList.get(info.position));
			

			int objId = trackList.get(info.position).id;
			String name = trackHash.get(objId).name;
			Log.d(TAG, "objId = " + objId);
			TrackCreator trackCreator = new TrackCreator(this);
			trackCreator.downloadTrack(objId, name);	      
			handled = true;
	      return handled;
	   }

	   
	   
	   class PeopleTrackListAdapter extends ArrayAdapter<Track> {
		   private ArrayList<Track> items;
	        public PeopleTrackListAdapter(Context context, int textViewResourceId, ArrayList<PeopleTrackList.Track> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

	        public View getView(int position, View convertView, ViewGroup parent) {
	                View v = convertView;
	                if (v == null) {
	                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                    v = vi.inflate(R.layout.trackitem, null);
	                }
	                Track o = items.get(position);
	                if (o != null) {
	            		TextView titleView = (TextView) v.findViewById(R.id.listitem_name);
	            		titleView.setText(o.name);
	            		
	            		DateView creationTimeView = (DateView)v.findViewById(R.id.listitem_from);
//	            		creationTimeView.setText(creationTime);
	            		creationTimeView.setVisibility(View.INVISIBLE);
	            		DistanceView distanceView = (DistanceView)v.findViewById(R.id.listitem_distance);
	            		distanceView.setText("" + o.distance);
	            		
	            		DurationView durationView = (DurationView)v.findViewById(R.id.listitem_duration);
	            		durationView.setText("" + o.duration);
	            			            		ImageView iconView = (ImageView)v.findViewById(R.id.listitem_icon);
	            		//if (trackId != null)
	            			iconView.setVisibility(View.GONE);
	                	/*
	                        TextView tt = (TextView) v.findViewById(R.id.toptext);
	                        TextView bt = (TextView) v.findViewById(R.id.bottomtext);
	                        if (tt != null) {
	                              tt.setText("Name: "+o.getOrderName());                            }
	                        if(bt != null){
	                              bt.setText("Status: "+ o.getOrderStatus());
	                        }
	                        */
	                }
	                return v;
	        }
	   
	   }
	   
}
