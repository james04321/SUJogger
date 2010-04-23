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
package nl.sogeti.android.gpstracker.tests.userinterface;

import nl.sogeti.android.gpstracker.logger.GPSLoggerServiceManager;
import nl.sogeti.android.gpstracker.tests.R;
import nl.sogeti.android.gpstracker.tests.utils.MockGPSLoggerDriver;
import nl.sogeti.android.gpstracker.viewer.LoggerMap;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/** 
 * 
 * @version $Id: OpenGPSTrackerDemo.java 170 2009-11-14 23:07:02Z rcgroot@gmail.com $
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 */
public class OpenGPSTrackerDemo extends ActivityInstrumentationTestCase2<LoggerMap>
{

   private static final Class<LoggerMap> CLASS = LoggerMap.class;
   private static final String PACKAGE = "nl.sogeti.android.gpstracker";
   private LoggerMap mLoggermap;
   private GPSLoggerServiceManager mLoggerServiceManager;
   private MapView mMapView;


   public OpenGPSTrackerDemo()
   {
      super( PACKAGE, CLASS );
   }


   @Override
   protected void setUp() throws Exception 
   {
      super.setUp();
      this.mLoggermap = getActivity();
      this.mLoggerServiceManager = new GPSLoggerServiceManager(this.mLoggermap);
      this.mMapView = (MapView) this.mLoggermap.findViewById( nl.sogeti.android.gpstracker.R.id.myMapView );
   }  

   protected void tearDown() throws Exception
   {
      this.mLoggerServiceManager.disconnectFromGPSLoggerService();
      super.tearDown();
   }

   /**
    * Start tracking and allow it to go on for 30 seconds
    * @throws InterruptedException 
    * 
    */
   @LargeTest
   public void testTracking() throws InterruptedException 
   {
      // Our data feeder to the emulator
      MockGPSLoggerDriver service = new MockGPSLoggerDriver( getInstrumentation().getContext(), R.xml.denhaagdenbosch, 6000 );
      try
      {
         Thread.sleep( 1 * 1000 );
         // Browse the Utrecht map
         service.sendSMS("Selecting a previous recorded track");
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "MENU DPAD_RIGHT" );
         Thread.sleep( 2 * 1000 );
         this.sendKeys( "L" );
         Thread.sleep( 2 * 1000 );
         service.sendSMS("The walk around the \"singel\" in Utrecht");
         this.sendKeys( "DPAD_CENTER" );
         Thread.sleep( 2 * 1000 );
         
         service.sendSMS("Zooming");
         this.sendKeys( "T T T T T" );
         Thread.sleep( 2 * 1000 );
         service.sendSMS("Scrolling about");
         this.mMapView.getController().animateTo( new GeoPoint(52095829, 5118599) );
         Thread.sleep( 2 * 1000 );
         this.mMapView.getController().animateTo( new GeoPoint(52096778, 5125090) );
         Thread.sleep( 2 * 1000 );
         this.mMapView.getController().animateTo( new GeoPoint(52085117, 5128255) );
         Thread.sleep( 2 * 1000 );
         this.mMapView.getController().animateTo( new GeoPoint(52081517, 5121646) );
         Thread.sleep( 2 * 1000 );
         this.mMapView.getController().animateTo( new GeoPoint(52093535, 5116711) );
         Thread.sleep( 2 * 1000 );
         this.sendKeys( "G G" );
         Thread.sleep( 2 * 1000 );
         
         // Show of the statistics screen
         service.sendSMS("Lets look at some statistics");
         this.sendKeys( "MENU DPAD_RIGHT DPAD_RIGHT" );
         Thread.sleep( 2 * 1000 );
         this.sendKeys( "E�" );
         Thread.sleep( 2 * 1000 );
         service.sendSMS("Shows the basics about time, speed and distance");
         Thread.sleep( 5 * 1000 );
         this.sendKeys("BACK");

         // Start feeding the GPS API with location data
         new Thread( service ).start();
         service.sendSMS("Lets start a new route");
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "MENU DPAD_RIGHT" );
         Thread.sleep( 2 * 1000 );
         this.sendKeys( "T" );
         Thread.sleep( 1 * 1000 );

         this.sendKeys("D E M O SPACE R O U T E ENTER");
         Thread.sleep( 2 * 1000 );
         service.sendSMS("The GPS logger is already running as a background service");
         Thread.sleep( 3 * 1000 );
         this.sendKeys("ENTER");
         Thread.sleep( 2 * 1000 );
         
         this.mMapView.getController().setZoom( 11 );
         
         int seconds = 0 ;
         while( service.getPositions() > 3 )
         {
            // Track
            Thread.sleep( 1 * 1000 );
            seconds++;
         }
         
         this.sendKeys( "T T T" );
         Thread.sleep( 1 * 1000 );
         service.sendSMS("Parked and arrived");
         Thread.sleep( 1 * 1000 );

         
         // Stop tracking
         service.sendSMS("Stopping tracking");
         this.sendKeys( "MENU DPAD_RIGHT" );
         Thread.sleep( 2 * 1000 );
         this.sendKeys( "T" );
         Thread.sleep( 1 * 1000 );
         

         service.sendSMS("Is the track stored allright?");
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "MENU DPAD_RIGHT DPAD_RIGHT" );
         Thread.sleep( 2 * 1000 );
         this.sendKeys( "L" );
         this.sendKeys( "DPAD_DOWN DPAD_DOWN" );
         Thread.sleep( 2 * 1000 );
         service.sendSMS("Yes, it is");
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "DPAD_CENTER" );
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "G G" );
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "G G" );
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "G G" );
         Thread.sleep( 1 * 1000 );
         this.sendKeys( "G G" );
         service.sendSMS("Thank you for watching this demo.");
         Thread.sleep( 10 * 1000 );
      }
      finally
      {
         // Stop feeding the GPS API with location data
         service.stop();
      }

      Thread.sleep( 5 * 1000 );
   }
}
