package edu.stanford.cs.sujogger.util;

public class MessageObject {
	public int mType;
	public long mOrigSendTime;
	public long mProposedTime;
	public String mSubject;
	public String mBody;
	
	public MessageObject() {}
	
	public MessageObject(int type, long origSendTime, long proposedTime, String subject, String body) {
		mType = type;
		mOrigSendTime = origSendTime;
		mProposedTime = proposedTime;
		mSubject = subject;
		mBody = body;
	}

}
