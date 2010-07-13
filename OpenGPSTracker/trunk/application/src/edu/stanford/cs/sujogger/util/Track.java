package edu.stanford.cs.sujogger.util;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;

public class Track {
  public long id;
  public boolean visible;
  
  public Track(long id) {
	  this.id = id;
	  this.visible = true;
  }
  public Track(long id, boolean visible) {
	  this.id = id;
	  this.visible = visible;
  }
  public static CharSequence[] tracksToCharSequence(ArrayList<Track> tracks, Context context) {
	  if (tracks == null || tracks.size() == 0) {
		  return new CharSequence[0];
	  }
	  CharSequence[] charSequence  = new CharSequence[tracks.size()];
	  int i = 0;
	  for (Track track : tracks) {
		  charSequence[i++] = getName(context, track.id);

	  }
	  return charSequence;
  }
  
  public static boolean[] tracksToVisibleBoolArray(ArrayList<Track> tracks) {
	  if (tracks == null || tracks.size() == 0) {
		  return new boolean[0];
	  }
	  boolean[] boolArr  = new boolean[tracks.size()];
	  int i = 0;
	  for (Track track : tracks) {
		  boolArr[i++] = track.visible;
	  }
	  return boolArr;
  }  
	private static String getName(Context context, long trackId) {
		ContentResolver resolver = context.getApplicationContext().getContentResolver();
		Cursor trackCursor = null;
		try {
			trackCursor = resolver.query(ContentUris.withAppendedId(Tracks.CONTENT_URI,
					trackId), new String[] { Tracks.NAME }, null, null, null);
			if (trackCursor != null && trackCursor.moveToLast()) {
				return trackCursor.getString(0);
			}
		}
		finally {
			if (trackCursor != null) {
				trackCursor.close();
			}
		}
		return "";
	} 
	
	public static ArrayList<Track> shadowCopy(ArrayList<Track> tracks, boolean logging) {
		ArrayList<Track> copiedTrack = new ArrayList<Track>();
		for (Track track: tracks) 
			copiedTrack.add(track);
		if (logging) {
			copiedTrack.remove(copiedTrack.size()-1);
		}
		return copiedTrack;
	}
	public static Track findTrackById(ArrayList<Track>  tracks, long trackId) {
		for (Track track: tracks)
			if (trackId == track.id)
				return track;
		return null;
	}
	public static int findTrackPosById(ArrayList<Track>  tracks, long trackId) {
		int i =0;
		for (Track track: tracks) {
			if (trackId == track.id)
				return i;
		    i++;
		}
		return -1;
	}	
	public static void setAllInvisible(ArrayList<Track> tracks) {
		for (Track track: tracks) {
			track.visible = false;
		}
	}
	public static boolean visibleExists(ArrayList<Track> tracks) {
		for (Track track: tracks) {
			if (track.visible == true)
				return true;
		}
		return false;
	}
}
