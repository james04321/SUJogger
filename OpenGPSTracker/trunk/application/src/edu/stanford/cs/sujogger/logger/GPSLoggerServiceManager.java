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

import edu.stanford.cs.sujogger.util.Constants;

import edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Class to interact with the service that tracks and logs the locations
 * 
 * @version $Id: GPSLoggerServiceManager.java 455 2010-03-14 08:16:44Z rcgroot $
 * @author rene (c) Jan 18, 2009, Sogeti B.V.
 */
public class GPSLoggerServiceManager
{
   private static final String TAG = "OGT.GPSLoggerServiceManager";
   private static final String REMOTE_EXCEPTION = "REMOTE_EXCEPTION";
   private static Context mCtx;
   private static IGPSLoggerServiceRemote mGPSLoggerRemote;
   private static final Object mStartLock = new Object();
   private static boolean mStarted = false;

   /**
    * Class for interacting with the main interface of the service.
    */
   private static ServiceConnection mServiceConnection = null;
/*
   public GPSLoggerServiceManager(Context ctx)
   {
      this.mCtx = ctx;
   }
   */
   public static void setContext(Context ctx) {
	   mCtx = ctx;
   }

   public static int getLoggingState()
   {
      synchronized (mStartLock)
      {
         int logging = Constants.UNKNOWN;
         try
         {
            if( mGPSLoggerRemote != null )
            {
               logging = mGPSLoggerRemote.loggingState();
               //               Log.d( TAG, "mGPSLoggerRemote tells state to be "+logging );
            }
            else
            {
               Log.w( TAG, "Remote interface to logging service not found. Started: " + mStarted );
            }
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could stat GPSLoggerService.", e );
         }
         return logging;
      }
   }

   public static long isLogging()
   {
      synchronized (mStartLock)
      {
         try
         {
            if( mGPSLoggerRemote != null )
            {
               return mGPSLoggerRemote.isLogging();
               //               Log.d( TAG, "mGPSLoggerRemote tells state to be "+logging );
            }
            else
            {
               Log.w( TAG, "Remote interface to logging service not found. Started: " + mStarted );
            }
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could stat GPSLoggerService.", e );
         }
         return -1;
      }
   }   
   
   public static boolean isMediaPrepared()
   {
      synchronized (mStartLock)
      {
         boolean prepared = false;
         try
         {
            if( mGPSLoggerRemote != null )
            {
               prepared = mGPSLoggerRemote.isMediaPrepared();
            }
            else
            {
               Log.w( TAG, "Remote interface to logging service not found. Started: " + mStarted );
            }
         }
         catch (RemoteException e)
         {
            Log.e( TAG, "Could stat GPSLoggerService.", e );
         }
         return prepared;
      }
   }

   public static long startGPSLogging( String name )
   {
      synchronized (mStartLock)
      {
         if( mStarted )
         {
            try
            {
               if( mGPSLoggerRemote != null )
               {
                  return mGPSLoggerRemote.startLogging();
               }
            }
            catch (RemoteException e)
            {
               Log.e( TAG, "Could not start GPSLoggerService.", e );
            }
         }
         return -1;
      }
   }

   public static void pauseGPSLogging()
   {
      synchronized (mStartLock)
      {
         if( mStarted )
         {
            try
            {
               if( mGPSLoggerRemote != null )
               {
                  mGPSLoggerRemote.pauseLogging();
               }
            }
            catch (RemoteException e)
            {
               Log.e( TAG, "Could not start GPSLoggerService.", e );
            }
         }
      }
   }

   public static long resumeGPSLogging()
   {
      synchronized (mStartLock)
      {
         if( mStarted )
         {
            try
            {
               if( mGPSLoggerRemote != null )
               {
                  return mGPSLoggerRemote.resumeLogging();
               }
            }
            catch (RemoteException e)
            {
               Log.e( TAG, "Could not start GPSLoggerService.", e );
            }
         }
         return -1;
      }
   }

   public static void stopGPSLogging()
   {
	   Log.d(TAG, "mstarted is: " + mStarted);
      synchronized (mStartLock)
      {
         if( mStarted )
         {
            try
            {
         	   Log.d(TAG, "mGPSLoggerRemote is: " + mGPSLoggerRemote);

               if( mGPSLoggerRemote != null )
               {
             	   Log.d(TAG, "stopLogging");
            	   
                  mGPSLoggerRemote.stopLogging();
               }
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
   }

   public static void storeMediaUri( Uri mediaUri )
   {
      synchronized (mStartLock)
      {
         if( mStarted )
         {
            try
            {
               if( mGPSLoggerRemote != null )
               {
                  mGPSLoggerRemote.storeMediaUri( mediaUri );
               }
            }
            catch (RemoteException e)
            {
               Log.e( GPSLoggerServiceManager.REMOTE_EXCEPTION, "Could not send media to GPSLoggerService.", e );
            }
         }
         else
         {
            Log.e( TAG, "No GPSLoggerRemote service connected to this manager" );
         }
      }
   }

   /**
    * Means by which an Activity lifecycle aware object hints about binding and unbinding
    */
   public static void startup()
   {
       Log.d( TAG, "connectToGPSLoggerService()" );
      if( !mStarted)
      {
         mServiceConnection = new ServiceConnection()
            {
               public void onServiceConnected( ComponentName className, IBinder service )
               {
                  synchronized (mStartLock)
                  {
                     Log.d( TAG, "onServiceConnected()" );
                     mGPSLoggerRemote = IGPSLoggerServiceRemote.Stub.asInterface( service );
                     mStarted = true;
                  }
               }

               public void onServiceDisconnected( ComponentName className )
               {
                  synchronized (mStartLock)
                  {
                     Log.d( TAG, "onServiceDisconnected()" );
                     mGPSLoggerRemote = null;
                     mStarted = false;
                  }
               }
            };
         mCtx.bindService( new Intent( Constants.SERVICENAME ), mServiceConnection, Context.BIND_AUTO_CREATE );
      }
      else
      {
         Log.w( TAG, "Attempting to connect whilst connected" );
      }
   }

   /**
    * Means by which an Activity lifecycle aware object hints about binding and unbinding
    */
   public static void shutdown()
   {
      Log.d( TAG, "disconnectFromGPSLoggerService()" );
      try
      {
         mCtx.unbindService( mServiceConnection );
      }
      catch (IllegalArgumentException e)
      {
         Log.e( TAG, "Failed to unbind a service, prehaps the service disapearded?", e );
      }
   }
}