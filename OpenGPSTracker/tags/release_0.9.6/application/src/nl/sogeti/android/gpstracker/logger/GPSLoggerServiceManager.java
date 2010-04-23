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
package nl.sogeti.android.gpstracker.logger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * 
 * Class to interact with the service that tracks and logs the locations
 *
 * @version $Id: GPSLoggerServiceManager.java 206 2010-01-03 18:30:35Z rcgroot $
 * @author rene (c) Jan 18, 2009, Sogeti B.V.
 */
public class GPSLoggerServiceManager
{
   private static final String TAG = "GPSLoggerServiceManager";
   private static final String REMOTE_EXCEPTION = "REMOTE_EXCEPTION";
   private Context mCtx;
   private IGPSLoggerServiceRemote mGPSLoggerRemote;
   /**
    * Class for interacting with the main interface of the service.
    */
   private ServiceConnection mServiceConnection = null;


   public GPSLoggerServiceManager(Context ctx)
   {
      this.mCtx = ctx;
      connectToGPSLoggerService();
   }

   public boolean isLogging()
   {
      boolean logging = false;
      if ( isConnected() ) 
      { 
         try
         {
            logging = this.mGPSLoggerRemote.isLogging();
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could stat GPSLoggerService.", e );
            logging = false;
         }
      }
      else 
      {
         Log.e( TAG, "No GPSLoggerRemote service connected to this manager on status" );
      }
      return logging;
   }

   public long startGPSLogging(String name)
   {
      if ( isConnected() ) 
      { 
         try
         {
            return this.mGPSLoggerRemote.startLogging();
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could not start GPSLoggerService.", e );
         }
      }
      return -1;
   }

   public void stopGPSLogging()
   {
      connectToGPSLoggerService();
      if ( isConnected() ) 
      { 
         try
         {
            this.mGPSLoggerRemote.stopLogging();
         }
         catch (RemoteException e)
         {
            Log.e( GPSLoggerServiceManager.REMOTE_EXCEPTION, "Could not stop GPSLoggerService.", e );
         }
      }
      else 
      {
         Log.e( TAG, "No GPSLoggerRemote service connected to this manager" );
      }
   }

   /**
    * Means by which an Activity lifecycle aware object hints about binding and unbinding
    */
   public void disconnectFromGPSLoggerService()
   {
      Log.d( TAG, "disconnectFromGPSLoggerService()" );
      if( isConnected() )
      {
         try
         {
            this.mCtx.unbindService( this.mServiceConnection );
            this.mServiceConnection = null;
         }
         catch (IllegalArgumentException e) 
         {
            Log.e( TAG, "Failed to unbind a service, prehaps the service disapearded?", e );
         }
      }
      else 
      {
         Log.w( TAG, "Attempting to disconnect whilst" );
      }
   }

   /**
    * Means by which an Activity lifecycle aware object hints about binding and unbinding
    */
   private void connectToGPSLoggerService()
   {
      Log.d( TAG, "connectToGPSLoggerService()" );
      if( ! isConnected() )
      {
         this.mServiceConnection = new GPSLoggerServiceConnection();
         this.mCtx.bindService( new Intent( GPSLoggerService.SERVICENAME ), this.mServiceConnection, Context.BIND_AUTO_CREATE );
      }
      else 
      {
         Log.w( TAG, "Attempting to connect whilst connected" );
      }
   }

   private boolean isConnected()
   {
      if( this.mGPSLoggerRemote == null )
      {
         return false;
      }
      else
      {
         try
         {
            return mGPSLoggerRemote.isAlive();
         }
         catch (RemoteException e)
         {
            Log.w( TAG, "Failure during isAlive() ping to Service", e );
            return false;
         }
      }
   }
   
   private class GPSLoggerServiceConnection implements ServiceConnection
   {
      public void onServiceConnected( ComponentName className, IBinder service )
      {
         Log.d( TAG, "onServiceConnected()" );
         GPSLoggerServiceManager.this.mGPSLoggerRemote = IGPSLoggerServiceRemote.Stub.asInterface( service );
      }
      public void onServiceDisconnected( ComponentName className )
      {
         Log.d( TAG, "onServiceDisconnected()" );
         GPSLoggerServiceManager.this.mGPSLoggerRemote = null;
      }
   };
}