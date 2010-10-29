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
package edu.stanford.cs.sujogger.viewer;

import java.util.Vector;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.location.Location;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.GPStracking;
import edu.stanford.cs.sujogger.db.GPStracking.Media;
import edu.stanford.cs.sujogger.db.GPStracking.Waypoints;
import edu.stanford.cs.sujogger.util.Common;

/**
 * Creates an overlay that can draw a single segment of connected waypoints
 * 
 * @version $Id: SegmentOverlay.java 467 2010-03-28 10:59:56Z rcgroot $
 * @author rene (c) Jan 11, 2009, Sogeti B.V.
 */
public class SegmentOverlay extends Overlay
{

   public static final int MIDDLE_SEGMENT = 0;
   public static final int FIRST_SEGMENT = 1;
   public static final int LAST_SEGMENT = 2;
   public static final String TAG = "OGT.SegmentOverlay";

   public static final int DRAW_GREEN = 0;
   public static final int DRAW_RED = 1;
   public static final int DRAW_MEASURED = 2;
   public static final int DRAW_CALCULATED = 3;
   public static final int DRAW_DOTS = 4;
   private static final float MINIMUM_RL_DISTANCE = 25;
   private static final float MINIMUM_RL_TIME = 5;
   private static final float MINIMUM_PX_DISTANCE = 15;
   private int mTrackColoringMethod = DRAW_CALCULATED;

   private ContentResolver mResolver;
   private Context mContext;
   private Projection mProjection;

   private int mPlacement = SegmentOverlay.MIDDLE_SEGMENT;
   private Uri mWaypointsUri;
   private Uri mMediaUri;
   private double mAvgSpeed;
   private GeoPoint mTopLeft;
   private GeoPoint mBottumRight;

   private Vector<DotVO> mDotPath;
   private Vector<MediaVO> mMediaPath;
   private Path mPath;
   private Shader mShader;

   private GeoPoint mStartPoint;
   private GeoPoint mEndPoint;
   private int mCalculatedPoints;
   private Point mPrevScreenPoint;
   private Point mScreenPoint;
   private boolean mLastOnscreen;
   private int mStepSize = 1;
   private int mStep = 0;
   private MapView mMapView;
   private Location location;
   private Location prevLocation;
   private Cursor mWaypointsCursor;
   private Uri mSegmentUri;
   private int mWaypointCount;

   /**
    * Constructor: create a new TrackingOverlay.
    * 
    * @param cxt
    * @param segmentUri
    * @param color
    * @param avgSpeed
    * @param mapView
    */
   public SegmentOverlay(Context cxt, Uri segmentUri, int color, double avgSpeed, MapView mapView)
   {
      super();
      this.mContext = cxt;
      this.mMapView = mapView;
      this.mTrackColoringMethod = color;
      this.mAvgSpeed = avgSpeed;
      this.mResolver = mContext.getApplicationContext().getContentResolver();
      this.mSegmentUri = segmentUri;
      this.mMediaUri = Uri.withAppendedPath( mSegmentUri, "media" );
      this.mWaypointsUri = Uri.withAppendedPath( mSegmentUri, "waypoints" );

      Cursor waypointsCursor = null;
      try
      {
         waypointsCursor = this.mResolver.query( this.mWaypointsUri, new String[] { Waypoints._ID }, null, null, null );
         mWaypointCount = waypointsCursor.getCount();
      }
      finally
      {
         waypointsCursor.close();
      }
   }

   @Override
   public void draw( Canvas canvas, MapView mapView, boolean shadow )
   {
      super.draw( canvas, mapView, shadow );
      if( shadow )
      {
         //         Common.log( TAG, "No shadows to draw" );
      }
      else
      {
         mProjection = mapView.getProjection();
         GeoPoint oldTopLeft = mTopLeft;
         GeoPoint oldBottumRight = mBottumRight;
         mTopLeft = mProjection.fromPixels( 0, 0 );
         mBottumRight = mProjection.fromPixels( canvas.getWidth(), canvas.getHeight() );

         if( oldTopLeft == null || oldBottumRight == null || mTopLeft.getLatitudeE6() / 100 != oldTopLeft.getLatitudeE6() / 100 || mTopLeft.getLongitudeE6() / 100 != oldTopLeft.getLongitudeE6() / 100
               || mBottumRight.getLatitudeE6() / 100 != oldBottumRight.getLatitudeE6() / 100 || mBottumRight.getLongitudeE6() / 100 != oldBottumRight.getLongitudeE6() / 100 )
         {
            this.mScreenPoint = new Point();
            this.mPrevScreenPoint = new Point();
            calculateTrack();
         }
         switch( mTrackColoringMethod )
         {
            case ( DRAW_CALCULATED ):
            case ( DRAW_MEASURED ):
            case ( DRAW_RED ):
            case ( DRAW_GREEN ):
               if( mPath == null )
               {
                  calculatePath();
               }
               drawPath( canvas );
               break;
            case ( DRAW_DOTS ):
               if( mDotPath == null )
               {
                  calculateDots();
               }
               drawDots( canvas );
               break;
         }
         drawStartStopCircles( canvas );
         if( mMediaPath == null )
         {
            calculateMedia();
         }
         drawMedia( canvas );
      }
   }

   public void calculateTrack()
   {
      switch( mTrackColoringMethod )
      {
         case ( DRAW_CALCULATED ):
         case ( DRAW_MEASURED ):
         case ( DRAW_RED ):
         case ( DRAW_GREEN ):
            calculatePath();
            break;
         case ( DRAW_DOTS ):
            calculateDots();
            break;
      }
   }

   /**
    * @param canvas
    * @param mapView
    * @param shadow
    * @see SegmentOverlay#draw(Canvas, MapView, boolean)
    */
   private synchronized void calculateDots()
   {
      mPath = null;
      if( mDotPath == null )
      {
         mDotPath = new Vector<DotVO>();
      }
      else
      {
         mDotPath.clear();
      }
      mCalculatedPoints = 0;
      mStep = 0;

      try
      {
         mWaypointsCursor = this.mResolver.query( this.mWaypointsUri, new String[] { Waypoints.LATITUDE, Waypoints.LONGITUDE, Waypoints.ACCURACY }, null, null, null );
         if( mProjection != null && mWaypointsCursor.moveToFirst() )
         {
            GeoPoint geoPoint;
            do
            {
               geoPoint = extractGeoPoint();
               setScreenPoint( geoPoint );

               float distance = (float) distanceInPoints( this.mPrevScreenPoint, this.mScreenPoint );
               if( distance > MINIMUM_PX_DISTANCE )
               {
                  DotVO dotVO = new DotVO();
                  dotVO.x = this.mScreenPoint.x;
                  dotVO.y = this.mScreenPoint.y;
                  dotVO.radius = mProjection.metersToEquatorPixels( mWaypointsCursor.getFloat( 2 ) );
                  mDotPath.add( dotVO );

                  this.mPrevScreenPoint.x = this.mScreenPoint.x;
                  this.mPrevScreenPoint.y = this.mScreenPoint.y;
               }
            }
            while( moveToNextWayPoint() );

            this.mEndPoint = extractGeoPoint();
            DotVO pointVO = new DotVO();
            pointVO.x = this.mScreenPoint.x;
            pointVO.y = this.mScreenPoint.y;
            pointVO.radius = mProjection.metersToEquatorPixels( mWaypointsCursor.getFloat( 2 ) );
            mDotPath.add( pointVO );
         }
      }
      finally
      {
         if( mWaypointsCursor != null )
         {
            mWaypointsCursor.close();
         }
      }
   }

   private synchronized void drawDots( Canvas canvas )
   {
      Paint dotpaint = new Paint();
      Paint radiusPaint = new Paint();
      radiusPaint.setColor( Color.YELLOW );
      radiusPaint.setAlpha( 100 );

      for( DotVO dotVO : mDotPath )
      {
         Bitmap bitmap = BitmapFactory.decodeResource( this.mContext.getResources(), R.drawable.stip2 );
         canvas.drawBitmap( bitmap, dotVO.x - 8, dotVO.y - 8, dotpaint );
         if( dotVO.radius > 8f )
         {
            canvas.drawCircle( dotVO.x, dotVO.y, dotVO.radius, radiusPaint );
         }
      }
   }

   private synchronized void calculatePath()
   {
	  // Common.log(TAG, "CALCULATING PATH");
      mDotPath = null;
      if( this.mPath == null )
      {
         this.mPath = new Path();
      }
      else
      {
         this.mPath.rewind();
      }
      this.mShader = null;

      GeoPoint geoPoint;
      mCalculatedPoints = 0;
      mStep = 0;
      this.prevLocation = null;
      int moves = 0;
      calculateStepSize();

      try
      {
         mWaypointsCursor = this.mResolver.query( this.mWaypointsUri, new String[] { Waypoints.LATITUDE, Waypoints.LONGITUDE, Waypoints.SPEED, Waypoints.TIME }, null, null, null );
         if( mProjection != null && mWaypointsCursor.moveToFirst() )
         {
            // Start point of the segments, possible a dot
            this.mStartPoint = extractGeoPoint();
            this.location = new Location( this.getClass().getName() );
            this.location.setLatitude( mWaypointsCursor.getDouble( 0 ) );
            this.location.setLongitude( mWaypointsCursor.getDouble( 1 ) );
            this.location.setTime( mWaypointsCursor.getLong( 3 ) );
            moveToGeoPoint( this.mStartPoint );

            do
            {
               //               Common.log(TAG, "Moving the loop of: moveToNextWayPoint() at cursor position: "+trackCursor.getPosition() ) ;
               geoPoint = extractGeoPoint();
               double speed = -1d;
               switch( mTrackColoringMethod )
               {
                  case DRAW_GREEN:
                  case DRAW_RED:
                     lineToGeoPoint( geoPoint, speed );
                     break;
                  case DRAW_MEASURED:
                     lineToGeoPoint( geoPoint, mWaypointsCursor.getDouble( 2 ) );
                     break;
                  case DRAW_CALCULATED:
                     this.location = new Location( this.getClass().getName() );
                     this.location.setLatitude( mWaypointsCursor.getDouble( 0 ) );
                     this.location.setLongitude( mWaypointsCursor.getDouble( 1 ) );
                     this.location.setTime( mWaypointsCursor.getLong( 3 ) );
                     if( ( this.prevLocation.distanceTo( this.location ) > MINIMUM_RL_DISTANCE 
                           && this.location.getTime() - this.prevLocation.getTime() > MINIMUM_RL_TIME )
                           || mWaypointsCursor.isLast() )
                     {
                        speed = calculateSpeedBetweenLocations( this.prevLocation, this.location );
                        lineToGeoPoint( geoPoint, speed );
                     }
                     else
                     {
                        lineToGeoPoint( geoPoint, -1d );
                     }
                     break;
                  default:
                     lineToGeoPoint( geoPoint, speed );
                     break;
               }
               moves++;
            }
            while( moveToNextWayPoint() );

            // End point of the segments, possible a dot
            this.mEndPoint = extractGeoPoint();

         }
      }
      finally
      {
         if( mWaypointsCursor != null )
         {
            mWaypointsCursor.close();
         }
      }
      //      Common.log( TAG, "transformSegmentToPath stop: points "+mCalculatedPoints+" from "+moves+" moves" );
	   //Common.log(TAG, "CALCULATING PATH DONE");
 
   }

   /**
    * @param canvas
    * @param mapView
    * @param shadow
    * @see SegmentOverlay#draw(Canvas, MapView, boolean)
    */
   private synchronized void drawPath( Canvas canvas )
   {
      Paint routePaint = new Paint();
      routePaint.setPathEffect( new CornerPathEffect( 10 ) );
      switch( mTrackColoringMethod )
      {
         case ( DRAW_CALCULATED ):
         case ( DRAW_MEASURED ):
            routePaint.setShader( this.mShader );
            break;
         case ( DRAW_RED ):
            routePaint.setColor( Color.RED );
            break;
         case ( DRAW_GREEN ):
            routePaint.setColor( Color.GREEN );
            break;
         default:
            routePaint.setColor( Color.YELLOW );
            break;
      }
      routePaint.setStyle( Paint.Style.STROKE );
      routePaint.setStrokeWidth( 6 );
      routePaint.setAntiAlias( true );
      canvas.drawPath( this.mPath, routePaint );
   }

   public synchronized void calculateMedia()
   {
      if( mMediaPath == null )
      {
         mMediaPath = new Vector<MediaVO>();
      }
      else
      {
         mMediaPath.clear();
      }
      Cursor mediaCursor = null;
      try
      {
         //         Common.log( TAG, "Searching for media on " + this.mMediaUri );
         mediaCursor = this.mResolver.query( this.mMediaUri, new String[] { Media.WAYPOINT, Media.URI }, null, null, null );
         if( mProjection != null && mediaCursor.moveToFirst() )
         {
            do
            {
               MediaVO mediaVO = new MediaVO();
               mediaVO.waypointId = mediaCursor.getLong( 0 );
               mediaVO.uri = Uri.parse( mediaCursor.getString( 1 ) );

//               Common.log( TAG, mediaVO.uri.toString() );
               
               Uri mediaWaypoint = ContentUris.withAppendedId( mWaypointsUri, mediaVO.waypointId );
               Cursor waypointCursor = null;
               try
               {
                  waypointCursor = this.mResolver.query( mediaWaypoint, new String[] { Waypoints.LATITUDE, Waypoints.LONGITUDE }, null, null, null );
                  if( waypointCursor != null && waypointCursor.moveToFirst() )
                  {
                     int microLatitude = (int) ( waypointCursor.getDouble( 0 ) * 1E6d );
                     int microLongitude = (int) ( waypointCursor.getDouble( 1 ) * 1E6d );
                     mediaVO.geopoint = new GeoPoint( microLatitude, microLongitude );
                  }
               }
               finally
               {
                  if( waypointCursor != null )
                  {
                     waypointCursor.close();
                  }
               }
               mMediaPath.add( mediaVO );
            }
            while( mediaCursor.moveToNext() );
         }
      }
      finally
      {
         if( mediaCursor != null )
         {
            mediaCursor.close();
         }
      }
   }

   private synchronized void drawMedia( Canvas canvas )
   {
      for( MediaVO mediaVO : mMediaPath )
      {
         if( isOnScreen( mediaVO.geopoint ) )
         {
            setScreenPoint( mediaVO.geopoint );
            int drawable = 0;
            if( mediaVO.uri.getScheme().equals( "file" ) )
            {
               if( mediaVO.uri.getLastPathSegment().endsWith( "3gp" ) )
               {
                  drawable = R.drawable.media_film;
               }
               else if( mediaVO.uri.getLastPathSegment().endsWith( "jpg" ) )
               {
                  drawable = R.drawable.media_camera;
               }
               else if( mediaVO.uri.getLastPathSegment().endsWith( "txt" ) )
               {
                  drawable = R.drawable.media_notepad;
               }
            }
            else if( mediaVO.uri.getScheme().equals( "content" ) )
            {
               if( mediaVO.uri.getAuthority().equals( GPStracking.AUTHORITY + ".string" ) )
               {
                  drawable = R.drawable.media_mark;
               }
               else if( mediaVO.uri.getAuthority().equals( "media" ) )
               {
                  drawable = R.drawable.media_speech;
               }
            }
            Bitmap bitmap = BitmapFactory.decodeResource( this.mContext.getResources(), drawable );
            int left = ( bitmap.getWidth() * 3 ) / 7;
            int up = ( bitmap.getHeight() * 6 ) / 7;
            canvas.drawBitmap( bitmap, mScreenPoint.x - left, mScreenPoint.y - up, new Paint() );
         }
      }
   }

   private void drawStartStopCircles( Canvas canvas )
   {
      Bitmap bitmap;
      if( ( this.mPlacement == FIRST_SEGMENT || this.mPlacement == FIRST_SEGMENT + LAST_SEGMENT ) && this.mStartPoint != null )
      {
         setScreenPoint( this.mStartPoint );
         bitmap = BitmapFactory.decodeResource( this.mContext.getResources(), R.drawable.stip2 );
         canvas.drawBitmap( bitmap, mScreenPoint.x - 8, mScreenPoint.y - 8, new Paint() );
      }
      if( ( this.mPlacement == LAST_SEGMENT || this.mPlacement == FIRST_SEGMENT + LAST_SEGMENT ) && this.mEndPoint != null )
      {
         setScreenPoint( this.mEndPoint );
         bitmap = BitmapFactory.decodeResource( this.mContext.getResources(), R.drawable.stip );
         canvas.drawBitmap( bitmap, mScreenPoint.x - 5, mScreenPoint.y - 5, new Paint() );
      }
   }

   /**
    * Set the mPlace to the specified value.
    * 
    * @see SegmentOverlay.FIRST
    * @see SegmentOverlay.MIDDLE
    * @see SegmentOverlay.LAST
    * @param place The placement of this segment in the line.
    */
   public void addPlacement( int place )
   {
      this.mPlacement += place;
   }

   public boolean isLast()
   {
      return ( mPlacement >= LAST_SEGMENT );
   }

   public long getSegmentId()
   {
      return Long.parseLong( mSegmentUri.getLastPathSegment() );
   }

   private void moveToGeoPoint( GeoPoint geoPoint )
   {
      setScreenPoint( geoPoint );

      if( this.mPath != null )
      {
         this.mPath.moveTo( this.mScreenPoint.x, this.mScreenPoint.y );
      }
      this.prevLocation = this.location;
      this.mPrevScreenPoint.x = this.mScreenPoint.x;
      this.mPrevScreenPoint.y = this.mScreenPoint.y;
   }

   private void lineToGeoPoint( GeoPoint geoPoint, double speed )
   {

      //      Common.log( TAG, "Drawing line to " + geoPoint + " with speed " + speed );
      setScreenPoint( geoPoint );

      //      Bitmap bitmap = BitmapFactory.decodeResource( this.mContext.getResources(), R.drawable.stip2 );
      //      this.mCanvas.drawBitmap( bitmap, this.mScreenPoint.x - 8, this.mScreenPoint.y - 8, new Paint() );
      Common.log(TAG, "speed = " + speed);
      if( speed >= 0.0 ) {
         int greenfactor = (int) Math.min( ( 127 * speed ) / mAvgSpeed, 255 );
         int redfactor = 255 - greenfactor;
         int currentColor = Color.rgb( redfactor, greenfactor, 0 );
         float distance = (float) distanceInPoints( this.mPrevScreenPoint, this.mScreenPoint );
         if( distance > MINIMUM_PX_DISTANCE )
         {
            int x_circle = ( this.mPrevScreenPoint.x + this.mScreenPoint.x ) / 2;
            int y_circle = ( this.mPrevScreenPoint.y + this.mScreenPoint.y ) / 2;
            float radius_factor = 0.4f;
            Shader lastShader = new RadialGradient( x_circle, y_circle, distance, new int[] { currentColor, currentColor, Color.TRANSPARENT }, new float[] { 0, radius_factor, 1 }, TileMode.CLAMP );
            //            Paint foo = new Paint();
            //            foo.setStyle( Paint.Style.STROKE );
            //            this.mCanvas.drawCircle(
            //                  x_circle,
            //                  y_circle, 
            //                  distance*radius_factor, 
            //                  foo );
            //            Common.log( TAG, "mPrevScreenPoint"+ mPrevScreenPoint );
            //            Common.log( TAG, "mScreenPoint"+ mScreenPoint );
            //            Common.log( TAG, "Created shader for speed " + speed + " on "+x_circle+","+y_circle);
            if( this.mShader != null )
            {
               this.mShader = new ComposeShader( this.mShader, lastShader, Mode.SRC_OVER );
            }
            else
            {
               this.mShader = lastShader;
            }
            this.prevLocation = this.location;
            this.mPrevScreenPoint.x = this.mScreenPoint.x;
            this.mPrevScreenPoint.y = this.mScreenPoint.y;
         }
      }
      else {
    	  // Draw tracks w/o speed in a light-blue color
          int currentColor = Color.rgb(107, 190, 222);
          float distance = (float) distanceInPoints( this.mPrevScreenPoint, this.mScreenPoint );
          if( distance > MINIMUM_PX_DISTANCE ) {
             int x_circle = ( this.mPrevScreenPoint.x + this.mScreenPoint.x ) / 2;
             int y_circle = ( this.mPrevScreenPoint.y + this.mScreenPoint.y ) / 2;
             float radius_factor = 0.4f;
             Shader lastShader = new RadialGradient( x_circle, y_circle, distance, new int[] { currentColor, currentColor, Color.TRANSPARENT }, new float[] { 0, radius_factor, 1 }, TileMode.CLAMP );
             if( this.mShader != null )
                this.mShader = new ComposeShader( this.mShader, lastShader, Mode.SRC_OVER );
             else
                this.mShader = lastShader;
          }
      }

      this.mPath.lineTo( this.mScreenPoint.x, this.mScreenPoint.y );
   }

   private void setScreenPoint( GeoPoint geoPoint )
   {
      this.mProjection.toPixels( geoPoint, this.mScreenPoint );
      mCalculatedPoints++;
   }

   private boolean moveToNextWayPoint()
   {
      if( mWaypointsCursor.isLast() )
      {
         return false;
      }
      //boolean onScreen = isOnScreen( extractGeoPoint( trackCursor ) );
      if( mLastOnscreen )
      {
         return moveToNextOnScreenWaypoint();
      }
      else
      {
         mLastOnscreen = false;
         return moveToNextOffScreenWaypoint();
      }
   }

   /**
    * Move the cursor to the next waypoint based on the stepsize
    * 
    * @param trackCursor
    * @return
    */
   private boolean moveToNextOnScreenWaypoint()
   {
      GeoPoint evalPoint;
      while( mWaypointsCursor.moveToNext() )
      {
         mStep++;

         //         evalPoint = extractGeoPoint( trackCursor );
         //         if( !isOnScreen( evalPoint ) )
         //         {
         //            //               Common.log(TAG, "first out screen "+trackCursor.getPosition() );
         //            return true;
         //         }

         if( isFullStepTaken() )
         {
            return true;
         }
      }
      // No full step can be taken, the last waypoint of the segment might be on screen.
      mWaypointsCursor.moveToLast();
      evalPoint = extractGeoPoint();
      return isOnScreen( evalPoint );
   }

   private boolean moveToNextOffScreenWaypoint()
   {
      GeoPoint lastPoint = extractGeoPoint();
      while( mWaypointsCursor.move( mStepSize * 2 ) )
      {
         if( mWaypointsCursor.isLast() )
         {
            //               Common.log(TAG, "last off screen "+trackCursor.getPosition() );
            return true;
         }

         GeoPoint evalPoint = extractGeoPoint();
         if( isOnScreen( evalPoint ) )
         {
            mLastOnscreen = true;
            moveToGeoPoint( lastPoint );
            //               Common.log(TAG, "first in screen "+trackCursor.getPosition() );
            return true;
         }
         lastPoint = evalPoint;
      }
      return mWaypointsCursor.moveToLast();
   }

   private boolean isFullStepTaken()
   {
      return mStep % mStepSize == 0;
   }

   /**
    * If a segment contains more then 500 waypoints and is zoomed out more then twice then some waypoints will not be used to render the line, this speeding things along.
    */
   private void calculateStepSize()
   {
      if( mWaypointCount < 100 )
      {
         mStepSize = 1;
      }
      else
      {
         int zoomLevel = mMapView.getZoomLevel();
         int maxZoomLevel = mMapView.getMaxZoomLevel();
         if( zoomLevel >= maxZoomLevel - 2 )
         {
            mStepSize = 1;
         }
         else
         {
            mStepSize = 2 * ( maxZoomLevel - zoomLevel );
         }
      }
   }

   /**
    * Is a given GeoPoint in the current projection of the map.
    * 
    * @param eval
    * @return
    */
   private boolean isOnScreen( GeoPoint eval )
   {
      boolean under = this.mTopLeft.getLatitudeE6() > eval.getLatitudeE6();
      boolean above = this.mBottumRight.getLatitudeE6() < eval.getLatitudeE6();
      boolean right = this.mTopLeft.getLongitudeE6() < eval.getLongitudeE6();
      boolean left = this.mBottumRight.getLongitudeE6() > eval.getLongitudeE6();
      return under && above && right && left;
   }

   public void setTrackColoringMethod( int coloring, double avgspeed )
   {
      this.mTrackColoringMethod = coloring;
      this.mAvgSpeed = avgspeed;
      calculatePath();
   }

   /**
    * For the current waypoint cursor returns the GeoPoint
    * 
    * @return
    */
   private GeoPoint extractGeoPoint()
   {
      int microLatitude = (int) ( mWaypointsCursor.getDouble( 0 ) * 1E6d );
      int microLongitude = (int) ( mWaypointsCursor.getDouble( 1 ) * 1E6d );
      return new GeoPoint( microLatitude, microLongitude );
   }

   /**
    * @param startLocation
    * @param endLocation
    * @return speed in m/s between 2 locations
    */
   private static double calculateSpeedBetweenLocations( Location startLocation, Location endLocation )
   {
      double speed = -1d;
      if( startLocation != null && endLocation != null )
      {
         float distance = startLocation.distanceTo( endLocation );
         float seconds = ( endLocation.getTime() - startLocation.getTime() ) / 1000f;
         speed = distance / seconds;
         //         Common.log( TAG, "Found a speed of "+speed+ " over a distance of "+ distance+" in a time of "+seconds);
      }
      if( speed > 0 )
      {
         return speed;
      }
      else
      {
         return -1d;
      }
   }

   public static int extendPoint( int x1, int x2 )
   {
      int diff = x2 - x1;
      int next = x2 + diff;
      return next;
   }

   private static double distanceInPoints( Point start, Point end )
   {
      int x = Math.abs( end.x - start.x );
      int y = Math.abs( end.y - start.y );
      return (double) Math.sqrt( x * x + y * y );
   }

   private boolean handleMediaTap( Uri mediaUri )
   {
      if( mediaUri.getScheme().equals( "file" ) )
      {
         Intent intent = new Intent( android.content.Intent.ACTION_VIEW );
         if( mediaUri.getLastPathSegment().endsWith( "3gp" ) )
         {
            intent.setDataAndType( mediaUri, "video/3gpp" );
            mContext.startActivity( intent );
            return true;
         }
         else if( mediaUri.getLastPathSegment().endsWith( "jpg" ) )
         {
            intent.setDataAndType( mediaUri, "image/jpeg" );
            mContext.startActivity( intent );
            return true;
         }
         else if( mediaUri.getLastPathSegment().endsWith( "txt" ) )
         {
            intent.setDataAndType( mediaUri, "text/plain" );
            mContext.startActivity( intent );
            return true;
         }
      }
      else if( mediaUri.getScheme().equals( "content" ) )
      {
         if( mediaUri.getAuthority().equals( GPStracking.AUTHORITY + ".string" ) )
         {
            String text = mediaUri.getLastPathSegment();
            Toast toast = Toast.makeText( mContext.getApplicationContext(), text, Toast.LENGTH_LONG );
            toast.show();
            return true;
         }
         else if( mediaUri.getAuthority().equals( "media" ) )
         {
            mContext.startActivity( new Intent( Intent.ACTION_VIEW, mediaUri ) );
            return true;
         }
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * @see com.google.android.maps.Overlay#onTap(com.google.android.maps.GeoPoint, com.google.android.maps.MapView)
    */
   @Override
   public boolean onTap( GeoPoint geoPoint, MapView mapView )
   {
      MediaVO tappedMedia = null;
      float minScreendistance = Float.MAX_VALUE;
      
      float[] distance = new float[1];
      for(MediaVO media : mMediaPath )
      {
         double startLat = geoPoint.getLatitudeE6()/1E6d;
         double startLon = geoPoint.getLongitudeE6()/1E6d;
         double endLat = media.geopoint.getLatitudeE6()/1E6d;
         double endLon = media.geopoint.getLongitudeE6()/1E6d;
         Location.distanceBetween( startLat, startLon, endLat, endLon, distance  );
         float screendistance = mapView.getProjection().metersToEquatorPixels( distance[0] ) * Resources.getSystem().getDisplayMetrics().density;
         if( minScreendistance > screendistance )
         {
            minScreendistance = screendistance;
            tappedMedia = media;
         }

      }
      if(  minScreendistance < 15 )
      {
         Common.log( TAG, String.format( "Tapped at a distance of %f which is %f on screen", distance[0], minScreendistance ) );
         return handleMediaTap( tappedMedia.uri );
      }
      return super.onTap( geoPoint, mapView );
   }

   private static class MediaVO
   {
      public Uri uri;
      public GeoPoint geopoint;
      public long waypointId;
   }

   private static class DotVO
   {
      public int x;
      public int y;
      public float radius;
   }

}
