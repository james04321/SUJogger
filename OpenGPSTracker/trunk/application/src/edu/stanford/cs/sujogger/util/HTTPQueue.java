package edu.stanford.cs.sujogger.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;
import android.os.Message;

public class HTTPQueue {
	public static final int PRIORITY_LOW = 0;
	public static final int PRIORITY_HIGH = 1;

	private volatile static HTTPQueue sInstance = null;

	private ArrayList<HTTPThread> mQueue = new ArrayList<HTTPThread>();
	private HashMap<Long, Boolean> mThreads = new HashMap<Long, Boolean>();
	private Handler mQueuedHandler = null;

	private HTTPQueue() {
	}

	public static HTTPQueue getInstance() {
		if (sInstance == null) {
			sInstance = new HTTPQueue();
		}
		return sInstance;
	}

	public void enqueue(HTTPThread task) {
		enqueue(task, PRIORITY_LOW);
	}

	public synchronized void enqueue(HTTPThread task, int priority) {
		Boolean exists = mThreads.get(task.getId());
		if (exists == null) {
			if (mQueue.size() == 0 || priority == PRIORITY_LOW) {
				mQueue.add(task);
			} else {
				mQueue.add(1, task);
			}
			mThreads.put(task.getId(), true);
		}
		runFirst();
	}

	public synchronized void dequeue(final HTTPThread task) {
		mThreads.remove(task.getId());
		mQueue.remove(task);
	}

	public synchronized void finished(int result) {
		if (mQueuedHandler != null) {
			mQueuedHandler.sendEmptyMessage(result);
		}
		runFirst();
	}

	private synchronized void runFirst() {
		if (mQueue.size() > 0) {
			HTTPThread task = mQueue.get(0);
			if (task.getStatus() == HTTPThread.STATUS_PENDING) {
				mQueuedHandler = task.getHandler();
				task.setHandler(mHandler);
				task.start();
			} else if (task.getStatus() == HTTPThread.STATUS_FINISHED) {
				HTTPThread thread = mQueue.remove(0);
				mThreads.remove(thread.getId());
				runFirst();
			}
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			finished(message.what);
		}
	};
}
