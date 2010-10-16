package edu.stanford.cs.sujogger.actions.utils;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Obj;
import edu.stanford.cs.gaming.sdk.model.ObjProperty;
import edu.stanford.cs.gaming.sdk.model.User;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.db.GPStracking.Waypoints;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;

public class TrackCreator {
	private GamingServiceConnection mGamingServiceConn;
	private TrackCreatorReceiver mReceiver;
	public static final String TAG = "TrackCreator";
	private Activity activity;
	private Context mContext;
	private long mTrackId = -1;
	private long mSegmentId = -1;
	private boolean mStartNextSegment;
	private long mWaypointId = -1;

	private Location mPreviousLocation;
	private DatabaseHelper mDbHelper;

	private ProgressDialog mProgressDialog;
	
	private static final int GET_OBJ_RID = 110;

	public TrackCreator(Activity activity) {
		this.activity = activity;
		this.mContext = activity.getApplicationContext();
		mReceiver = new TrackCreatorReceiver();
		mGamingServiceConn = new GamingServiceConnection(activity, mReceiver, Constants.APP_ID,
				Constants.APP_API_KEY, this.getClass().getName());
		mGamingServiceConn.bind();
		User user = Common.getRegisteredUser(activity);
		mGamingServiceConn.setUserId(user.id, user.fb_id, user.fb_token);

	}

	public void downloadTrack(int trackId, String name) {
		try {
			// trackId = 21; //ASLAI: HERE
			mProgressDialog = ProgressDialog.show(activity, "", activity
					.getString(R.string.dialog_download_track), true);

			mGamingServiceConn.getObj(GET_OBJ_RID, trackId);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	class TrackCreatorReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGamingServiceConn.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					Log.d(TAG, "TrackCreatorReceiver: Response received with request id:"
							+ appResponse.request_id);
					
					if (appResponse.result_code.equals(GamingServiceConnection.RESULT_CODE_ERROR)) {
						activity.runOnUiThread(new Runnable() {
							public void run() {
								if (mProgressDialog != null) mProgressDialog.dismiss();
								Toast toast = Toast.makeText(activity, 
										R.string.connection_error_toast, Toast.LENGTH_SHORT);
								toast.show();
							}
						});
						continue;
					}
					
					switch (appResponse.request_id) {
					case GET_OBJ_RID:
						Log.d(TAG, "OBJECT IS: " + appResponse.object.getClass().getName());
						final Obj obj = (Obj) appResponse.object;
						final ObjProperty[] objProp = ((Obj) appResponse.object).object_properties;
						
						activity.runOnUiThread(new Runnable() {
							public void run() {
								try {
									SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
									String name = "";
									int duration = 0;
									double distance = 0;
									String trackGPX = "";
									int userId = 0;
									long creationTime = 0;
									for (int i = 0; i < objProp.length; i++) {
										if ("gpx".equals(objProp[i].name)) {
											trackGPX = objProp[i].string_val;
											Log.d(TAG, "TrackGPX is: " + trackGPX);
										}
										else if ("duration".equals(objProp[i].name)) {
											duration = objProp[i].int_val;
										}
										else if ("distance".equals(objProp[i].name)) {
											distance = objProp[i].float_val;
										}
										else if ("name".equals(objProp[i].name)) {
											name = objProp[i].string_val;
										}
										else if ("creation_time".equals(objProp[i].name)) {
											creationTime = (long) objProp[i].float_val;
										}
										else if ("user_id".equals(objProp[i].name)) {
											userId = objProp[i].int_val;
										}
			
									}
									startNewTrack(obj.id, name, duration, distance, userId, creationTime);
									startNewSegment();
									
									
										saxParser.parse(new InputSource(new StringReader(trackGPX)),
											new LocationHandler(TrackCreatorReceiver.this));
									
									
									TrackCreator.this.mGamingServiceConn.unbind();
									mProgressDialog.cancel();
									Toast toast = Toast.makeText(activity, "Track " + name
											+ " is downloaded successfully", Toast.LENGTH_SHORT);
									toast.show();
									Log.d(TAG, "USER_ID IS " + userId);
								} catch (Exception e) {}
							}
						});
						break;

					default:
						break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void startNewTrack(int trackId, String name, int duration, double distance,
				int userId, long creationTime) {
			ContentValues values = new ContentValues();
			Log.d(TAG, "STARTNEWTRACK DURATION IS " + duration);
			Log.d(TAG, "STARTNEWTRACK DISTANCE IS " + distance);
			values.put(Tracks.DURATION, duration);
			values.put(Tracks.DISTANCE, distance);
			values.put(Tracks.NAME, name);
			values.put(Tracks.TRACK_ID, trackId);
			values.put(Tracks.USER_ID, userId);
			values.put(Tracks.CREATION_TIME, creationTime);
			mDbHelper = new DatabaseHelper(activity);
			mDbHelper.openAndGetDb();
			mTrackId = mDbHelper.createTrack(values);
			mDbHelper.close();

			// Uri newTrack = mContext.getContentResolver().insert(
			// Tracks.CONTENT_URI, values);
			// mTrackId = new Long( newTrack.getLastPathSegment() ).longValue();
		}

		public void stopLogging() {
			// doing nothing for now
		}

		private void startNewSegment() {
			mPreviousLocation = null;
			Uri newSegment = mContext.getContentResolver().insert(
					Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId + "/segments"), null);
			mSegmentId = new Long(newSegment.getLastPathSegment()).longValue();
		}

	}

	public class LocationHandler extends DefaultHandler {

		// ===========================================================
		// Fields
		// ===========================================================

		private boolean mInEleTag = false;
		private boolean mInTimeTag = false;
		private Location mLocation;
		private TrackCreatorReceiver mTcr;
		private StringBuilder mStringBuilder;

		// ===========================================================
		// Getter & Setter
		// ===========================================================

		public LocationHandler(TrackCreatorReceiver tcr) {
			super();
			this.mTcr = tcr;
		}

		private void startNewSegment() {
			mPreviousLocation = null;
			Uri newSegment = mContext.getContentResolver().insert(
					Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId + "/segments"), null);
			mSegmentId = new Long(newSegment.getLastPathSegment()).longValue();
		}

		public void storeLocation(Location location) {

			mPreviousLocation = location;
			ContentValues args = new ContentValues();

			args.put(Waypoints.LATITUDE, new Double(location.lat));
			args.put(Waypoints.LONGITUDE, new Double(location.lng));
			// args.put( Waypoints.SPEED, new Float( location.getSpeed() ) );
			// args.put( Waypoints.TIME, new Long( System.currentTimeMillis() )
			// );
			// Log.d( TAG, "Location based time sent to ContentProvider"+
			// DateFormat.getInstance().format(new Date( args.getAsLong(
			// Waypoints.TIME ) ) ) );
			// if( location.hasAccuracy() )
			// {
			// args.put( Waypoints.ACCURACY, new Float( location.getAccuracy() )
			// );
			// }
			if (location.hasAltitude) {
				args.put(Waypoints.ALTITUDE, new Double(location.ele));

			}
			/*
			 * if( location.hasBearing() ) { args.put( Waypoints.BEARING, new
			 * Float( location.getBearing() ) ); }
			 */
			Uri waypointInsertUri = Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId
					+ "/segments/" + mSegmentId + "/waypoints");
			Uri inserted = mContext.getContentResolver().insert(waypointInsertUri, args);
			mWaypointId = Long.parseLong(inserted.getLastPathSegment());
		}

		/*
		 * public Location getParsedData() { return mlocation;
		 * 
		 * }
		 */
		// ===========================================================
		// Methods
		// ===========================================================
		@Override
		public void startDocument() throws SAXException {
			// this.myParsedExampleDataSet = new ParsedExampleDataSet();
		}

		@Override
		public void endDocument() throws SAXException {
			// Nothing to do
		}

		/**
		 * Gets be called on opening tags like: <tag> Can provide attribute(s),
		 * when xml was like: <tag attribute="attributeValue">
		 */
		public void startElement(String namespaceURI, String localName, String qName,
				Attributes atts) throws SAXException {
			Log.d(TAG, "startElement: " + localName);
			if (localName.equals("trkpt")) {
				mLocation = new Location();
				mLocation.lat = new Double(atts.getValue("lat"));
				mLocation.lng = new Double(atts.getValue("lon"));
			}
			else if (localName.equals("ele")) {
				// mInEleTag = true;
				mStringBuilder = new StringBuilder();
			}
			else if (localName.equals("time")) {
				// mInTimeTag = true;
				mStringBuilder = new StringBuilder();

			}
		}

		/**
		 * Gets be called on closing tags like: </tag>
		 */
		@Override
		public void endElement(String namespaceURI, String localName, String qName)
				throws SAXException {
			Log.d(TAG, "endElement: " + localName);

			if (localName.equals("trkpt")) {
				// startNewSegment();
				storeLocation(mLocation);
			}
			else if (localName.equals("ele")) {
				mLocation.hasAltitude = true;
				mLocation.ele = new Double(mStringBuilder.toString());
			}
			else if (localName.equals("time")) {
				if (mStringBuilder != null && mLocation != null)
					mLocation.timeStr = mStringBuilder.toString();
				else
					Log.d(TAG, "mStringBuilder is: " + mStringBuilder);
				Log.d(TAG, "mLocation is: " + mLocation);

			}
		}

		/**
		 * Gets be called on the following structure: <tag>characters</tag>
		 */
		@Override
		public void characters(char ch[], int start, int length) {
			Log.d(TAG, "characters: " + (new String(ch, start, length)));

			if (ch != null && mStringBuilder != null)
				mStringBuilder.append(ch, start, length);
		}
	}

	class Location {
		double lat;
		double lng;
		double ele;
		String timeStr;
		boolean hasAltitude;
	}

}
