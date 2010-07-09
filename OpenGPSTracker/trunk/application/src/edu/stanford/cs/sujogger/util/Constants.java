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
   
   public static final int SPEED_CONVERSION_RATIO = 3600000; //ms in 1hr
   
   public static final long UPDATE_INTERVAL = 900000; //ms in 15 minutes
   
   //Facebook integration
   public static final String GRAPH_BASE_URL = "http://graph.facebook.com/";
   public static final String FB_APP_ID = "127241693959042";
   public static final String[] FB_PERMISSIONS =
       new String[] {"read_friendlists", "email"};
   
   //First launch flags
   public static final String STATS_INITIALIZED = "stats_initialized";
   public static final String USER_REGISTERED = "user_registered";
   
   //User attribute keys (for SharedPreferences)
   public static final String USERREG_ID_KEY = "userreg_id";
   public static final String USERREG_FBID_KEY = "userreg_fbid";
   public static final String USERREG_EMAIL_KEY = "userreg_email";
   public static final String USERREG_FIRSTNAME_KEY = "userreg_firstname";
   public static final String USERREG_LASTNAME_KEY = "userreg_lastname";
   public static final String USERREG_PICTURE_KEY = "userreg_picture";
   
   public static final String LAST_CONCIERGE_ID_KEY = "last_concierge_id";
   
   //Last-updated keys (for Activities that request network updates
   public static final String BADGES_UPDATE_KEY = "badges_update_time";
   public static final String LB_USERSCORES_UPDATE_KEY = "userscores_update_time";
   public static final String LB_GROUPSCORES_UPDATE_KEY = "groupscores_update_time";
   public static final String GROUPS_UPDATE_KEY = "groups_update_time";
   public static final String FEED_UPDATE_KEY = "feed_update_time";
   
   //Gaming@Stanford stuff
   public static final int APP_ID = 1;
   public static final String APP_API_KEY = "";
}
