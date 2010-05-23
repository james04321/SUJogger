/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Volumes/Data/aslai/dev/cs294s/SUJogger/OpenGPSTracker/trunk/application/src/edu/stanford/cs/sujogger/logger/IGPSLoggerServiceRemote.aidl
 */
package edu.stanford.cs.sujogger.logger;
public interface IGPSLoggerServiceRemote extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote
{
private static final java.lang.String DESCRIPTOR = "edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote interface,
 * generating a proxy if needed.
 */
public static edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote))) {
return ((edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote)iin);
}
return new edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_loggingState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.loggingState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_startLogging:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.startLogging();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_pauseLogging:
{
data.enforceInterface(DESCRIPTOR);
this.pauseLogging();
reply.writeNoException();
return true;
}
case TRANSACTION_resumeLogging:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.resumeLogging();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_stopLogging:
{
data.enforceInterface(DESCRIPTOR);
this.stopLogging();
reply.writeNoException();
return true;
}
case TRANSACTION_storeMediaUri:
{
data.enforceInterface(DESCRIPTOR);
android.net.Uri _arg0;
if ((0!=data.readInt())) {
_arg0 = android.net.Uri.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
android.net.Uri _result = this.storeMediaUri(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_isMediaPrepared:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isMediaPrepared();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements edu.stanford.cs.sujogger.logger.IGPSLoggerServiceRemote
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public int loggingState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_loggingState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public long startLogging() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startLogging, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void pauseLogging() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pauseLogging, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public long resumeLogging() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_resumeLogging, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void stopLogging() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopLogging, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public android.net.Uri storeMediaUri(android.net.Uri mediaUri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.net.Uri _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((mediaUri!=null)) {
_data.writeInt(1);
mediaUri.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_storeMediaUri, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.net.Uri.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean isMediaPrepared() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isMediaPrepared, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_loggingState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_startLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_pauseLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_resumeLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_stopLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_storeMediaUri = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_isMediaPrepared = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
public int loggingState() throws android.os.RemoteException;
public long startLogging() throws android.os.RemoteException;
public void pauseLogging() throws android.os.RemoteException;
public long resumeLogging() throws android.os.RemoteException;
public void stopLogging() throws android.os.RemoteException;
public android.net.Uri storeMediaUri(android.net.Uri mediaUri) throws android.os.RemoteException;
public boolean isMediaPrepared() throws android.os.RemoteException;
}
