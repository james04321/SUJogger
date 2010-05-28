package edu.stanford.cs.sujogger.util;

import edu.stanford.cs.sujogger.db.GPStracking;

import android.net.Uri;


/**
 * Various application wide constants
 * 
 * @version $Id: Constants.java 468 2010-03-28 13:47:13Z rcgroot $
 * @author rene (c) Mar 22, 2009, Sogeti B.V.
 */
public class Constants
{
   public static final String DISABLEBLANKING = "disableblanking";
   public static final String SATELLITE = "SATELLITE";
   public static final String TRAFFIC = "TRAFFIC";
   public static final String SPEED = "showspeed";
   public static final String COMPASS = "COMPASS";
   public static final String LOCATION = "LOCATION";
   public static final String TRACKCOLORING = "trackcoloring";
   public static final int UNKNOWN = -1;
   public static final int LOGGING = 1;
   public static final int PAUSED = 2;
   public static final int STOPPED = 3;
   public static final String SPEEDSANITYCHECK = "speedsanitycheck";
   public static final String PRECISION = "precision";
   public static final String LOGATSTARTUP = "logatstartup";
   public static final String SERVICENAME = "edu.stanford.cs.sujogger.intent.action.GPSLoggerService";
   public static final String UNITS = "units";
   public static final int UNITS_DEFAULT = 0;
   public static final int UNITS_IMPERIAL = 1;
   public static final int UNITS_METRIC = 2;
   public static final String EXTERNAL_DIR = "/OpenGPSTracker/";
   public static final String TMPICTUREFILE_PATH = EXTERNAL_DIR+"media_tmp";
   public static final Uri NAME_URI = Uri.parse( "content://" + GPStracking.AUTHORITY+".string" );
   
   //Facebook integration
   public static final String GRAPH_BASE_URL = "http://graph.facebook.com/";
   public static final String FB_APP_ID = "127241693959042";
   public static final String[] FB_PERMISSIONS =
       new String[] {"read_friendlists", "offline_access"};
   
   //First launch flags
   public static final String STATS_INITIALIZED = "stats_initialized";
   public static final String USER_REGISTERED = "user_registered";
   
   //Gaming@Stanford stuff
   public static final int APP_ID = 1;
   public static final String APP_API_KEY = "";
}
