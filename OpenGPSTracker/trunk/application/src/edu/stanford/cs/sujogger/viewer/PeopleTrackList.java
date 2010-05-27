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

import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Obj;
import edu.stanford.cs.gaming.sdk.model.ObjProperty;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;

import edu.stanford.cs.sujogger.actions.PublishGPX;
import edu.stanford.cs.sujogger.actions.utils.TrackCreator;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
 
public class PeopleTrackList extends ListActivity {
	public static final String TAG = "PeopleTrackList";
	   private PeopleTrackListReceiver mReceiver;
	   private GamingServiceConnection mGamingServiceConn;
	   private int[] ids;
	class PeopleTrackListReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGamingServiceConn.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					Log.d(TAG, "PUBLISHGPXReceiver: Response received with request id:" + appResponse.request_id);
					
					switch(appResponse.request_id) {
					case 120:
				        ListAdapter adapter = createAdapter((ObjProperty[]) appResponse.object);
				        setListAdapter(adapter);
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
        super.onCreate(savedInstanceState);
    	  mReceiver = new PeopleTrackListReceiver(); 
    	  mGamingServiceConn = new GamingServiceConnection(this, mReceiver, 
    				Constants.APP_ID, Constants.APP_API_KEY, this.getClass().getName());
    	  mGamingServiceConn.bind();
    	  mGamingServiceConn.setUserId(Common.getRegisteredUser().id);
    	  try {
			mGamingServiceConn.getObjProperties(120, Common.getRegisteredUser().id, -1, "track", "name");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 


    }
 
    /**
     * Creates and returns a list adapter for the current list activity
     * @return
     */
    protected ListAdapter createAdapter(ObjProperty[] objProp)
    { 
    	String[] testValues = new String[0];
    	if (objProp != null && objProp.length > 0) {
    	 testValues = new String[objProp.length];
    	 ids = new int[objProp.length];
    	for (int i=0; i< objProp.length; i++) {
    		testValues[i] = objProp[i].string_val;
    		ids[i] = objProp[i].obj_id;
    	}
    	}
    		
    	/*
    	// Create some mock data
    	String[] testValues = new String[] {
    			"Test1",
    			"Test2",
    			"Test3"
    	};
    	*/
 
    	// Create a simple array adapter (of type string) with the test values
    	ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testValues);
 
    	return adapter;
    }

    
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		

		int objId = ids[position];
		Log.d(TAG, "objId = " + objId);
		TrackCreator trackCreator = new TrackCreator(this);
		trackCreator.downloadTrack(objId);
		/*
		Intent i = new Intent(this, PeopleTrackList.class);
		i.putExtra("userId",userId);
		startActivity(i);
		*/
		return;
	}
	    
}
