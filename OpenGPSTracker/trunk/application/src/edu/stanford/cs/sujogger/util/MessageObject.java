package edu.stanford.cs.sujogger.util;

public class MessageObject {
	public long mOrigSendTime;
	public long mProposedTime;
	public String mSubject;
	public String mBody;
	
	public MessageObject() {}
	
	public MessageObject(long origSendTime, long proposedTime, String subject, String body) {
		mOrigSendTime = origSendTime;
		mProposedTime = proposedTime;
		mSubject = subject;
		mBody = body;
	}

}
