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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.actions.Statistics;
import edu.stanford.cs.sujogger.db.GPStracking.Tracks;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

/**
 * Show a list view of all tracks, also doubles for showing search results
 * 
 * @version $Id: TrackList.java 468 2010-03-28 13:47:13Z rcgroot $
 * @author rene (c) Jan 11, 2009, Sogeti B.V.
 */
public class TrackList extends ListActivity
{
   private static final String TAG = "OGT.TrackList";
   private static final int MENU_DETELE = 0;
   private static final int MENU_SHARE = 1;
   private static final int MENU_RENAME = 2;
   private static final int MENU_STATS = 3;

   public static final int DIALOG_FILENAME = 0;
   private static final int DIALOG_RENAME = 23;
   private static final int DIALOG_DELETE = 24;
   private static final int MENU_SEARCH = 0;
   
   public static final int TRACKSTATUS_IDLE=10;
   public static final int TRACKSTATUS_TRACKING=11;
   
   private EditText mTrackNameView;
   private Uri mDialogUri;
   private String mDialogCurrentName = "";
   
   private List<Map<String,?>> actions;
   private SimpleCursorAdapter trackAdapter;

   private OnClickListener mDeleteOnClickListener = new DialogInterface.OnClickListener()
      {
         public void onClick( DialogInterface dialog, int which )
         {
            getContentResolver().delete( mDialogUri, null, null );
            getListView().invalidateViews();
         }
      };
   private OnClickListener mRenameOnClickListener = new DialogInterface.OnClickListener()
      {
         public void onClick( DialogInterface dialog, int which )
         {
            //         Log.d( TAG, "Context item selected: "+mDialogUri+" with name "+mDialogCurrentName );

            String trackName = mTrackNameView.getText().toString();
            ContentValues values = new ContentValues();
            values.put( Tracks.NAME, trackName );
            TrackList.this.getContentResolver().update( mDialogUri, values, null, null );
         }
      };

   @Override
   protected void onCreate( Bundle savedInstanceState )
   {
      super.onCreate( savedInstanceState );
      Log.d(TAG, "onCreate()");
      this.setContentView( R.layout.tracklist );
      
      actions = new LinkedList<Map<String,?>>();
	  actions.add(createItem("New Track"));
	  actions.add(createItem("Statistics"));
	  
      displayIntent( getIntent() );

      // Add the context menu (the long press thing)
      registerForContextMenu( getListView() );
   }

   @Override
   public void onNewIntent( Intent newIntent )
   {
	   Log.d(TAG, "onNewIntent()");
	   displayIntent( newIntent );
   }
   
   @Override
   protected void onRestart() {
	   Log.d(TAG, "onRestart()");
	   
	   trackAdapter.notifyDataSetChanged();
	   //trackAdapter.notifyDataSetInvalidated();
	   getListView().invalidate();
	   getListView().invalidateViews();
	   //displayIntent( getIntent() );
	   super.onRestart();
   }
   
   @Override
   protected void onResume() {
	   trackAdapter.notifyDataSetChanged();
	   getListView().invalidate();
	   getListView().invalidateViews();
	   super.onResume();
   }
   /*
    * (non-Javadoc)
    * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
    */
   @Override
   protected void onRestoreInstanceState( Bundle state )
   {
	  Log.v("TrackList", "onRestoreInstanceState");
	  mDialogUri = state.getParcelable( "URI" );
      mDialogCurrentName = state.getString( "NAME" );
      super.onRestoreInstanceState( state );
   }
   
   /*
    * (non-Javadoc)
    * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
    */
   @Override
   protected void onSaveInstanceState( Bundle outState )
   {
      outState.putParcelable( "URI", mDialogUri );
      outState.putString( "NAME", mDialogCurrentName );
      super.onSaveInstanceState( outState );
   }

   @Override
   public boolean onCreateOptionsMenu( Menu menu )
   {
      boolean result = super.onCreateOptionsMenu( menu );

      menu.add( ContextMenu.NONE, MENU_SEARCH, ContextMenu.NONE, android.R.string.search_go ).setIcon( android.R.drawable.ic_search_category_default ).setAlphabeticShortcut( SearchManager.MENU_KEY );
      return result;
   }

   @Override
   public boolean onOptionsItemSelected( MenuItem item )
   {
      boolean handled = false;
      switch( item.getItemId() )
      {
         case MENU_SEARCH:
            onSearchRequested();
            handled = true;
            break;
         default:
            handled = super.onOptionsItemSelected( item );
      }
      return handled;
   }

   @Override
   protected void onListItemClick( ListView l, View v, int position, long id )
   {
      super.onListItemClick( l, v, position, id );
      
      Log.v("TrackList", "position = " + position + "; id = " + id);
      if (position < actions.size() + 1) {
    	  if (position == 1) {
    		  Log.v("TrackList", "creating new track");
    		  Intent intent = new Intent();
    		  intent.setClass( this, LoggerMap.class );
    		  startActivity(intent);
    	  }
    	  else if (position == 2) {
    		  Log.v("TrackList", "pulling up stats");
    	  }
      }
      else if (position > actions.size() + 1){
	      Intent intent = new Intent();
	      intent.setData( ContentUris.withAppendedId( Tracks.CONTENT_URI, getTrackIdFromRowPosition(id)) );
	      
	      //TODO: eliminate the if statement (no one starts a TrackList activity anymore)
	      ComponentName caller = this.getCallingActivity();
	      if( caller != null )
	      {
	         setResult( RESULT_OK, intent );
	         finish();
	      }
	      else
	      {
	         intent.setClass( this, LoggerMap.class );
	         startActivity( intent );
	      }
      }
   }

   @Override
   public void onCreateContextMenu( ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo )
   {
      if( menuInfo instanceof AdapterView.AdapterContextMenuInfo )
      {
         AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
         if (itemInfo.position < actions.size() + 1) return;
         TextView textView = (TextView) itemInfo.targetView.findViewById( android.R.id.text1 );
         if( textView != null )
         {
            menu.setHeaderTitle( textView.getText() );
         }
      }
      menu.add( 0, MENU_STATS, 0, R.string.menu_statistics );
      menu.add( 0, MENU_SHARE, 0, R.string.menu_shareTrack );
      menu.add( 0, MENU_RENAME, 0, R.string.menu_renameTrack );
      menu.add( 0, MENU_DETELE, 0, R.string.menu_deleteTrack );
   }

   @Override
   public boolean onContextItemSelected( MenuItem item )
   {
      boolean handled = false;
      AdapterView.AdapterContextMenuInfo info;
      try
      {
         info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      }
      catch( ClassCastException e )
      {
         Log.e( TAG, "Bad menuInfo", e );
         return handled;
      }
      
      //TODO: cursor is obtained incorrectly
      long trackId = getTrackIdFromRowPosition(info.position);
      
      Uri trackUri = ContentUris.withAppendedId( Tracks.CONTENT_URI, trackId);
      Log.d(TAG, "onContextItemSelected(): trackUri=" + trackUri);
      ContentResolver resolver = this.getApplicationContext().getContentResolver();
      Cursor trackCursor = null;
      try
      {
         trackCursor = resolver.query( trackUri, new String[] { Tracks.NAME }, null, null, null );
         if( trackCursor != null && trackCursor.moveToLast() )
         {
            String trackName = trackCursor.getString( 0 );
            this.setTitle( this.getString( R.string.app_name ) + ": " + trackName );
         }
         
       //Cursor cursor = (Cursor) getListAdapter().getItem(  );
         mDialogUri = trackUri;
         mDialogCurrentName = trackCursor.getString(0);
         switch( item.getItemId() )
         {
            case MENU_DETELE:
            {
               showDialog( DIALOG_DELETE );
               handled = true;
               break;
            }
            case MENU_SHARE:
            {
               Intent actionIntent = new Intent( Intent.ACTION_RUN );
               actionIntent.setDataAndType( mDialogUri, Tracks.CONTENT_ITEM_TYPE );
               actionIntent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
               startActivity( Intent.createChooser( actionIntent, getString( R.string.chooser_title ) ) );
               handled = true;
               break;
            }
            case MENU_RENAME:
            {
               showDialog( DIALOG_RENAME );
               handled = true;
               break;
            }
            case MENU_STATS:
            {
               Intent actionIntent = new Intent( this, Statistics.class );
               actionIntent.setData( mDialogUri );
               startActivity( actionIntent );
               handled = true;
               break;
            }
            default:
               handled = super.onContextItemSelected( item );
               break;
         }
      }
      finally
      {
         if( trackCursor != null )
         {
            trackCursor.close();
         }
      }
      
      return handled;
   }

   /*
    * (non-Javadoc)
    * @see android.app.Activity#onCreateDialog(int)
    */
   @Override
   protected Dialog onCreateDialog( int id )
   {
      Dialog dialog = null;
      Builder builder = null;
      switch( id )
      {
         case DIALOG_RENAME:
            LayoutInflater factory = LayoutInflater.from( this );
            View view = factory.inflate( R.layout.namedialog, null );
            mTrackNameView = (EditText) view.findViewById( R.id.nameField );

            builder = new AlertDialog.Builder( this ).setTitle( R.string.dialog_routename_title ).setMessage( R.string.dialog_routename_message ).setIcon( android.R.drawable.ic_dialog_alert )
                  .setPositiveButton( R.string.btn_okay, mRenameOnClickListener ).setNegativeButton( R.string.btn_cancel, null ).setView( view );
            dialog = builder.create();
            return dialog;
         case DIALOG_DELETE:
            builder = new AlertDialog.Builder( TrackList.this ).setTitle( R.string.dialog_deletetitle ).setIcon( android.R.drawable.ic_dialog_alert ).setNegativeButton( android.R.string.cancel, null )
                  .setPositiveButton( android.R.string.ok, mDeleteOnClickListener );
            dialog = builder.create();
            String messageFormat = this.getResources().getString( R.string.dialog_deleteconfirmation );
            String message = String.format( messageFormat, "" );
            ( (AlertDialog) dialog ).setMessage( message );
            return dialog;
         default:
            return super.onCreateDialog( id );
      }
   }

   /*
    * (non-Javadoc)
    * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
    */
   @Override
   protected void onPrepareDialog( int id, Dialog dialog )
   {
      super.onPrepareDialog( id, dialog );
      switch( id )
      {
         case DIALOG_RENAME:
        	if (mDialogCurrentName == null) {
        		mTrackNameView.setText( "" );
        		mTrackNameView.setSelection( 0, 0 );
        	}
        	else {
        		mTrackNameView.setText( mDialogCurrentName );
        		mTrackNameView.setSelection( 0, mDialogCurrentName.length() );
        	}
            break;
         case DIALOG_DELETE:
            AlertDialog alert = (AlertDialog) dialog;
            String messageFormat = this.getResources().getString( R.string.dialog_deleteconfirmation );
            String message = String.format( messageFormat, mDialogCurrentName );
            alert.setMessage( message );
            break;
      }
   }
   
   private void displayIntent( Intent intent )
   {
      Log.d(TAG, "displayIntent()");
	   final String queryAction = intent.getAction();
      Cursor tracksCursor = null;
      if( Intent.ACTION_SEARCH.equals( queryAction ) )
      {
         // Got to SEARCH a query for tracks, make a list
         tracksCursor = doSearchWithIntent( intent );
         displayCursor( tracksCursor, false );
      }
      else if( Intent.ACTION_VIEW.equals( queryAction ) )
      {
         // Got to VIEW a single track, instead had it of to the LoggerMap
         Intent notificationIntent = new Intent( this, LoggerMap.class );
         notificationIntent.setData( intent.getData() );
         startActivity( notificationIntent );
      }
      else
      {
         // Got to nothing, make a list of everything
         tracksCursor = managedQuery( Tracks.CONTENT_URI, new String[] { Tracks._ID, Tracks.NAME, Tracks.CREATION_TIME, Tracks.DURATION, Tracks.DISTANCE }, null, null, null );
         Log.d(TAG, "displayIntent(): displaying all tracks. count = " + tracksCursor.getCount());
         displayCursor( tracksCursor, true );
      }
      
   }

   private void displayCursor( Cursor tracksCursor, boolean showGlobal )
   {
      Log.d(TAG, "displayCursor(): " + DatabaseUtils.dumpCursorToString(tracksCursor));
	   // Create an array to specify the fields we want to display in the list (only TITLE)
      // and an array of the fields we want to bind those fields to (in this case just text1)
      String[] fromColumns = new String[] { Tracks.NAME, Tracks.CREATION_TIME, Tracks.DURATION, Tracks.DISTANCE };
      int[] toItems = new int[] { R.id.listitem_name, R.id.listitem_from, R.id.listitem_duration, R.id.listitem_distance };
      // Now create a simple cursor adapter and set it to display
      trackAdapter = new SimpleCursorAdapter( this, 
    		  R.layout.trackitem, tracksCursor, fromColumns, toItems );
      //showGlobal = false;
      if (!showGlobal) {
    	  setListAdapter( trackAdapter );
      }
      else {
    	  SeparatedListAdapter groupedAdapter = new SeparatedListAdapter(this);
    	  groupedAdapter.addSection("", new SimpleAdapter(this, actions, R.layout.list_item_simple,
    			  new String[] {ITEM_TITLE}, 
    			  new int[] {R.id.list_simple_title}));
    	  
    	  groupedAdapter.addSection("My Tracks", trackAdapter);
    	  
    	  setListAdapter( groupedAdapter );
      }
   }
   
   private final static String ITEM_TITLE = "title";
   private final static String ITEM_CAPTION = "caption";
   
   private Map<String,?> createItem(String title) {
		Map<String,String> item = new HashMap<String,String>();
		item.put(ITEM_TITLE, title);
		return item;
	}
   
   private long getTrackIdFromRowPosition(long pos) {
	   pos = pos - (actions.size() + 1);
	   Cursor tracksCursor = managedQuery( Tracks.CONTENT_URI, new String[] { Tracks._ID }, null, null, null );
	   pos = tracksCursor.getCount() - pos + 1;
	   return pos;
   }
   
   private Cursor doSearchWithIntent( final Intent queryIntent )
   {
      final String queryString = queryIntent.getStringExtra( SearchManager.QUERY );
      Cursor cursor = managedQuery( Tracks.CONTENT_URI, new String[] { Tracks._ID, Tracks.NAME, Tracks.CREATION_TIME, Tracks.DURATION, Tracks.DISTANCE }, "name LIKE ?", new String[] { "%" + queryString + "%" }, null );
      return cursor;
   }
}
