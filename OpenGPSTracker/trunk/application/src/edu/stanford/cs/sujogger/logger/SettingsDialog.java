/*------------------------------------------------------------------------------
 **     Ident: Innovation en Inspiration > Google Android 
 **    Author: rene
 ** Copyright: (c) Jan 22, 2009 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced  
 ** Distributed Software Engineering |  or transmitted in any form or by any        
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the      
 ** 4131 NJ Vianen                   |  purpose, without the express written    
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edu.stanford.cs.sujogger.logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.catchnotes.integration.IntentIntegrator;

import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.util.Constants;

/**
 * Controller for the settings dialog
 *
 * @version $Id: SettingsDialog.java 468 2010-03-28 13:47:13Z rcgroot $
 * @author rene (c) Jan 18, 2009, Sogeti B.V.
 */
public class SettingsDialog extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

   @Override
   protected void onCreate( Bundle savedInstanceState ) {
       super.onCreate( savedInstanceState );
       addPreferencesFromResource( R.layout.settings );
       
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       prefs.registerOnSharedPreferenceChangeListener(this);
   }
   
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	  Log.d("SettingsDialog", "key= " + key);
	  if (key.equals(Constants.POST_CATCH_KEY)) {
		  if (sharedPreferences.getBoolean(key, false)) {
			  IntentIntegrator notesIntent = new IntentIntegrator(this);
			  notesIntent.isNotesInstalled();
		  }
	  }
  }
  
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	  String versionString = null;
	  if (preference.getTitle().equals("Feedback")) {
		  try {
			  PackageInfo pkgInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			  versionString = this.getString(R.string.app_name) + " " + pkgInfo.versionName;
		  } catch (NameNotFoundException e) {}
		  
		  Intent msg = new Intent(Intent.ACTION_SEND);
		  msg.putExtra(Intent.EXTRA_EMAIL, new String[] {Constants.SITE_EMAIL});
		  msg.putExtra(Intent.EXTRA_SUBJECT, "");
		  msg.putExtra(Intent.EXTRA_TEXT, "\n\n-------------\n" + 
				  "App version: " + versionString + "\n" + 
				  "OS: Android " + android.os.Build.VERSION.RELEASE + "\n" +
				  "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
		  msg.setType("message/rfc822");
		  startActivity(Intent.createChooser(msg, "Select email application"));
	  }
	  else if (preference.getTitle().equals("Website")) {
		  Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SITE_URL));
		  startActivity(myIntent);
	  }
	  
	  return super.onPreferenceTreeClick(preferenceScreen, preference);
  }
}
