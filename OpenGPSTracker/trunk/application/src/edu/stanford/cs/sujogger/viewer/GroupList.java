package edu.stanford.cs.sujogger.viewer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.stanford.cs.gaming.sdk.model.AppResponse;
import edu.stanford.cs.gaming.sdk.model.Group;
import edu.stanford.cs.gaming.sdk.service.GamingServiceConnection;
import edu.stanford.cs.sujogger.R;
import edu.stanford.cs.sujogger.db.DatabaseHelper;
import edu.stanford.cs.sujogger.db.GPStracking.Groups;
import edu.stanford.cs.sujogger.util.Common;
import edu.stanford.cs.sujogger.util.Constants;
import edu.stanford.cs.sujogger.util.GroupListAdapter;
import edu.stanford.cs.sujogger.util.SeparatedListAdapter;

public class GroupList extends ListActivity {
	private static final String TAG = "OGT.GroupList";
	
	private static final int DIALOG_GRPNAME = 1;
	
	private DatabaseHelper mDbHelper;
	private Cursor mGroupsCursor;
	private SeparatedListAdapter mGroupAdapter;
	private List<Map<String,?>> actions;
	
	private GamingServiceConnection mGameCon;
	private GroupListReceiver mReceiver;
	
	//Request IDs
	private static final int GRP_CREATE_RID = 1;
	
	//Views
	private EditText mGroupNameView;
	
	//Listeners
	private final DialogInterface.OnClickListener mGroupNameDialogListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			String groupName = mGroupNameView.getText().toString();
			Log.d(TAG, "mGroupNameDialogListener: " + groupName);
			GroupList.this.toggleNewGroupItemState();
			
			try {
				mGameCon.createGroup(GRP_CREATE_RID, new Group(groupName));
			}
			catch (RemoteException e) {
				
			}
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.list_simple);

		mDbHelper = new DatabaseHelper(this);
		mDbHelper.openAndGetDb();
		
		mReceiver = new GroupListReceiver(); 
		mGameCon = new GamingServiceConnection(this.getParent(), mReceiver, 
				Constants.APP_ID, Constants.APP_API_KEY, GroupList.class.toString());
		mGameCon.bind();

		// Create cursors
		mGroupsCursor = mDbHelper.getGroups();
		startManagingCursor(mGroupsCursor);
		
		actions = new LinkedList<Map<String,?>>();
		actions.add(Common.createItem("New group"));

		fillData();
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		mDbHelper.openAndGetDb();
		super.onRestart();
		mGroupAdapter.notifyDataSetChanged();
		getListView().invalidateViews();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		Log.d(TAG, "onResume(): dumping groups cursor");
		DatabaseUtils.dumpCursor(mGroupsCursor);
	}
	
	@Override
	protected void onStop() {
		
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mGameCon.unbind();
		super.onDestroy();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "position = " + position + "; id = " + id);
		
		if (position == 1) {
			Log.d(TAG, "pulling up new group dialog");
			showDialog(DIALOG_GRPNAME);
		}
		else {
			Object item = mGroupAdapter.getItem(position);
			if (item.getClass() == Integer.class) {
				Log.d(TAG, "starting GroupDetail for group_id = " + (Integer)item);
				long groupId = ((Integer)item).longValue();
				startGroupDetail(groupId);
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		LayoutInflater factory = null;
		View view = null;
		Builder builder = null;
		
		switch(id) {
		case DIALOG_GRPNAME:
			builder = new AlertDialog.Builder(this);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.namedialog, null);
			mGroupNameView = (EditText) view.findViewById(R.id.nameField);
			mGroupNameView.setHint(R.string.dialog_newgrpname_hint);
			builder.setTitle(R.string.dialog_newgrpname_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.btn_okay, mGroupNameDialogListener)
				.setNegativeButton(R.string.btn_cancel, null)
				.setView(view);
			dialog = builder.create();
			return dialog;
		default: return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_GRPNAME:
			mGroupNameView.setText("");
			break;
		default:break;
		}
	}
	
	private void startGroupDetail(long groupId) {
		Intent i = new Intent(this, GroupDetail.class);
		i.putExtra(Groups.TABLE, groupId);
		startActivity(i);
	}
	
	private void toggleNewGroupItemState() {
		View newGroupItem = getListView().getChildAt(1);
		TextView title = (TextView)newGroupItem.findViewById(R.id.newtrack_title);
		View progressBar = newGroupItem.findViewById(R.id.newtrack_progress);
		
		if (progressBar.getVisibility() == View.INVISIBLE) {
			title.setText("Creating new group...");
			progressBar.setVisibility(View.VISIBLE);
		}
		else {
			title.setText("New group");
			progressBar.setVisibility(View.INVISIBLE);
		}
	}
	
	private void fillData() {
		mGroupAdapter = new SeparatedListAdapter(this);
		
		mGroupAdapter.addSection("", new SimpleAdapter(this, actions, R.layout.new_track_item,
  			  new String[] {Common.ITEM_TITLE}, new int[] {R.id.newtrack_title}));
		
		mGroupAdapter.addSection("My Groups", new GroupListAdapter(this, mGroupsCursor, true));
		
		setListAdapter(mGroupAdapter);
	}
	
	class GroupListReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			try {
				AppResponse appResponse = null;
				while ((appResponse = mGameCon.getNextPendingNotification()) != null) {
					Log.d(TAG, appResponse.toString());
					switch(appResponse.request_id) {
					case GRP_CREATE_RID:
						GroupList.this.toggleNewGroupItemState();
						Integer groupId = (Integer)(appResponse.object);
						Group newGroup = (Group)(appResponse.appRequest.object);
						Log.d(TAG, "onReceive(): groupId = " + groupId + "; groupName = " + newGroup.name);
						GroupList.this.mDbHelper.addGroup(groupId.longValue(), newGroup.name, 1);
						GroupList.this.mGroupsCursor.requery();
						GroupList.this.mGroupAdapter.notifyDataSetChanged();
						GroupList.this.getListView().invalidateViews();
						break;
					default: break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
