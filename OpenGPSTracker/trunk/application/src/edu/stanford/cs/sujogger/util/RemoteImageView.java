package edu.stanford.cs.sujogger.util;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import edu.stanford.cs.sujogger.R;

public class RemoteImageView extends ImageView {
	private String mLocal;
	private String mRemote;
	private HTTPThread mThread = null;

	public RemoteImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setLocalURI(String local) {
		mLocal = local;
	}

	public void setRemoteURI(String uri) {
		if (uri == null) mRemote = null;
		else if (uri.startsWith("http")) {
			mRemote = uri;
		}
	}

	public void loadImage() {
		if (mRemote != null) {
			if (mLocal == null) {
				mLocal = Common.getCacheFileName(mRemote);
				//Environment.getExternalStorageDirectory() + "/.remote-image-view-cache/" + mRemote.hashCode() + ".jpg";
			}
			//Log.d("OGT.RemoteImageView", "loadImage(): mLocal = " + mLocal + "; mRemote = " + mRemote);
			// check for the local file here instead of in the thread because
			// otherwise previously-cached files wouldn't be loaded until after
			// the remote ones have been downloaded.
			File local = new File(mLocal);
			if (local.exists()) {
				setFromLocal();
			} else {
				// we already have the local reference, so just make the parent
				// directories here instead of in the thread.
				local.getParentFile().mkdirs();
				queue();
			}
		}
		else
			setImageResource(R.drawable.icon);
	}

	@Override
	public void finalize() {
		if (mThread != null) {
			HTTPQueue queue = HTTPQueue.getInstance();
			queue.dequeue(mThread);
		}
	}

	private void queue() {
		if (mThread == null) {
			mThread = new HTTPThread(mRemote, mLocal, mHandler);
			HTTPQueue queue = HTTPQueue.getInstance();
			queue.enqueue(mThread, HTTPQueue.PRIORITY_HIGH);
		}
		setImageResource(R.drawable.icon);
	}

	private void setFromLocal() {
		mThread = null;
		Drawable d = Drawable.createFromPath(mLocal);
		if (d != null) {
			setImageDrawable(d);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setFromLocal();
		}
	};
}
