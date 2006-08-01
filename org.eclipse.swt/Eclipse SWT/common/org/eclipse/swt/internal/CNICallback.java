package org.eclipse.swt.internal;

public class CNICallback {
  
  final CNIDispatcher dispatcher;
  final int method;
  final int argCount;
  final int /*long*/ address;
  final int /*long*/ errorResult;
  
  public CNICallback(CNIDispatcher dispatcher, int method, int argCount) {
    this(dispatcher, method, argCount, 0);
  }

  public CNICallback(CNIDispatcher dispatcher, int method, int argCount,
                     int /*long*/ errorResult)
  {
    if (dispatcher == null) throw new NullPointerException();

    this.dispatcher = dispatcher;
    this.method = method;
    this.argCount = argCount;
    this.errorResult = errorResult;
    this.address = bind(this);
  }

  private static native synchronized int /*long*/ bind(CNICallback callback);

  private static native synchronized int /*long*/ unbind(CNICallback callback);
  
  public static final native synchronized void setEnabled(boolean enable);

  public static final native synchronized boolean getEnabled();

  public int /*long*/ getAddress() {
    return address;
  }

  public void dispose() {
    unbind(this);
  }

}
