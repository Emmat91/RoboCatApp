package com.hoho.android.usbserial.util;

import android.util.Log;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SerialInputOutputManager
  implements Runnable
{
  private static final int BUFSIZ = 4096;
  private static final boolean DEBUG = true;
  private static final int READ_WAIT_MILLIS = 200;
  private static final String TAG = SerialInputOutputManager.class.getSimpleName();
  private final UsbSerialDriver mDriver;
  private Listener mListener;
  private final ByteBuffer mReadBuffer = ByteBuffer.allocate(4096);
  private State mState = State.STOPPED;
  private final ByteBuffer mWriteBuffer = ByteBuffer.allocate(4096);
  
  public SerialInputOutputManager(UsbSerialDriver paramUsbSerialDriver)
  {
    this(paramUsbSerialDriver, null);
  }
  
  public SerialInputOutputManager(UsbSerialDriver paramUsbSerialDriver, Listener paramListener)
  {
    this.mDriver = paramUsbSerialDriver;
    this.mListener = paramListener;
  }
  
  private State getState()
  {
    try
    {
      State localState = this.mState;
      return localState;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }
  
  private void step()
    throws IOException
  {
    int i = this.mDriver.read(this.mReadBuffer.array(), 200);
    if (i > 0)
    {
      Log.d(TAG, "Read data len=" + i);
      Listener localListener = getListener();
      if (localListener != null)
      {
        byte[] arrayOfByte2 = new byte[i];
        this.mReadBuffer.get(arrayOfByte2, 0, i);
        localListener.onNewData(arrayOfByte2);
      }
      this.mReadBuffer.clear();
    }
    synchronized (this.mWriteBuffer)
    {
      int j = this.mWriteBuffer.position();
      byte[] arrayOfByte1 = null;
      if (j > 0)
      {
        i = this.mWriteBuffer.position();
        arrayOfByte1 = new byte[i];
        this.mWriteBuffer.get(arrayOfByte1, 0, i);
        this.mWriteBuffer.clear();
      }
      if (arrayOfByte1 != null)
      {
        Log.d(TAG, "Writing data len=" + i);
        this.mDriver.write(arrayOfByte1, 200);
      }
      return;
    }
  }
  
  public Listener getListener()
  {
    try
    {
      Listener localListener = this.mListener;
      return localListener;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }
  
  /* Error */
  public void run()
  {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: invokespecial 130	com/hoho/android/usbserial/util/SerialInputOutputManager:getState	()Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   6: getstatic 57	com/hoho/android/usbserial/util/SerialInputOutputManager$State:STOPPED	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   9: if_acmpeq +18 -> 27
    //   12: new 132	java/lang/IllegalStateException
    //   15: dup
    //   16: ldc 134
    //   18: invokespecial 135	java/lang/IllegalStateException:<init>	(Ljava/lang/String;)V
    //   21: athrow
    //   22: astore_1
    //   23: aload_0
    //   24: monitorexit
    //   25: aload_1
    //   26: athrow
    //   27: aload_0
    //   28: getstatic 138	com/hoho/android/usbserial/util/SerialInputOutputManager$State:RUNNING	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   31: putfield 59	com/hoho/android/usbserial/util/SerialInputOutputManager:mState	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   34: aload_0
    //   35: monitorexit
    //   36: getstatic 35	com/hoho/android/usbserial/util/SerialInputOutputManager:TAG	Ljava/lang/String;
    //   39: ldc 140
    //   41: invokestatic 143	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   44: pop
    //   45: aload_0
    //   46: invokespecial 130	com/hoho/android/usbserial/util/SerialInputOutputManager:getState	()Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   49: getstatic 138	com/hoho/android/usbserial/util/SerialInputOutputManager$State:RUNNING	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   52: if_acmpeq +50 -> 102
    //   55: getstatic 35	com/hoho/android/usbserial/util/SerialInputOutputManager:TAG	Ljava/lang/String;
    //   58: new 80	java/lang/StringBuilder
    //   61: dup
    //   62: ldc 145
    //   64: invokespecial 85	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   67: aload_0
    //   68: invokespecial 130	com/hoho/android/usbserial/util/SerialInputOutputManager:getState	()Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   71: invokevirtual 148	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   74: invokevirtual 92	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   77: invokestatic 143	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   80: pop
    //   81: aload_0
    //   82: monitorenter
    //   83: aload_0
    //   84: getstatic 57	com/hoho/android/usbserial/util/SerialInputOutputManager$State:STOPPED	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   87: putfield 59	com/hoho/android/usbserial/util/SerialInputOutputManager:mState	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   90: getstatic 35	com/hoho/android/usbserial/util/SerialInputOutputManager:TAG	Ljava/lang/String;
    //   93: ldc 150
    //   95: invokestatic 143	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   98: pop
    //   99: aload_0
    //   100: monitorexit
    //   101: return
    //   102: aload_0
    //   103: invokespecial 152	com/hoho/android/usbserial/util/SerialInputOutputManager:step	()V
    //   106: goto -61 -> 45
    //   109: astore 6
    //   111: getstatic 35	com/hoho/android/usbserial/util/SerialInputOutputManager:TAG	Ljava/lang/String;
    //   114: new 80	java/lang/StringBuilder
    //   117: dup
    //   118: ldc 154
    //   120: invokespecial 85	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   123: aload 6
    //   125: invokevirtual 157	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   128: invokevirtual 160	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   131: invokevirtual 92	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   134: aload 6
    //   136: invokestatic 164	android/util/Log:w	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   139: pop
    //   140: aload_0
    //   141: invokevirtual 102	com/hoho/android/usbserial/util/SerialInputOutputManager:getListener	()Lcom/hoho/android/usbserial/util/SerialInputOutputManager$Listener;
    //   144: astore 8
    //   146: aload 8
    //   148: ifnull +12 -> 160
    //   151: aload 8
    //   153: aload 6
    //   155: invokeinterface 168 2 0
    //   160: aload_0
    //   161: monitorenter
    //   162: aload_0
    //   163: getstatic 57	com/hoho/android/usbserial/util/SerialInputOutputManager$State:STOPPED	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   166: putfield 59	com/hoho/android/usbserial/util/SerialInputOutputManager:mState	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   169: getstatic 35	com/hoho/android/usbserial/util/SerialInputOutputManager:TAG	Ljava/lang/String;
    //   172: ldc 150
    //   174: invokestatic 143	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   177: pop
    //   178: aload_0
    //   179: monitorexit
    //   180: return
    //   181: astore 9
    //   183: aload_0
    //   184: monitorexit
    //   185: aload 9
    //   187: athrow
    //   188: astore_3
    //   189: aload_0
    //   190: monitorenter
    //   191: aload_0
    //   192: getstatic 57	com/hoho/android/usbserial/util/SerialInputOutputManager$State:STOPPED	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   195: putfield 59	com/hoho/android/usbserial/util/SerialInputOutputManager:mState	Lcom/hoho/android/usbserial/util/SerialInputOutputManager$State;
    //   198: getstatic 35	com/hoho/android/usbserial/util/SerialInputOutputManager:TAG	Ljava/lang/String;
    //   201: ldc 150
    //   203: invokestatic 143	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   206: pop
    //   207: aload_0
    //   208: monitorexit
    //   209: aload_3
    //   210: athrow
    //   211: astore 4
    //   213: aload_0
    //   214: monitorexit
    //   215: aload 4
    //   217: athrow
    //   218: astore 12
    //   220: aload_0
    //   221: monitorexit
    //   222: aload 12
    //   224: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	225	0	this	SerialInputOutputManager
    //   22	4	1	localObject1	Object
    //   188	22	3	localObject2	Object
    //   211	5	4	localObject3	Object
    //   109	45	6	localException	Exception
    //   144	8	8	localListener	Listener
    //   181	5	9	localObject4	Object
    //   218	5	12	localObject5	Object
    // Exception table:
    //   from	to	target	type
    //   2	22	22	finally
    //   23	25	22	finally
    //   27	36	22	finally
    //   45	81	109	java/lang/Exception
    //   102	106	109	java/lang/Exception
    //   162	180	181	finally
    //   183	185	181	finally
    //   45	81	188	finally
    //   102	106	188	finally
    //   111	146	188	finally
    //   151	160	188	finally
    //   191	209	211	finally
    //   213	215	211	finally
    //   83	101	218	finally
    //   220	222	218	finally
  }
  
  public void setListener(Listener paramListener)
  {
    try
    {
      this.mListener = paramListener;
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }
  
  public void stop()
  {
    try
    {
      if (getState() == State.RUNNING)
      {
        Log.i(TAG, "Stop requested");
        this.mState = State.STOPPING;
      }
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }
  
  public void writeAsync(byte[] paramArrayOfByte)
  {
    synchronized (this.mWriteBuffer)
    {
      this.mWriteBuffer.put(paramArrayOfByte);
      return;
    }
  }
  
  public static abstract interface Listener
  {
    public abstract void onNewData(byte[] paramArrayOfByte);
    
    public abstract void onRunError(Exception paramException);
  }
  
  private static enum State
  {
    STOPPING,  RUNNING,  STOPPED;
  }
}


/* Location:           C:\Users\MattKing\Desktop\Robocat\RoboCat_1412286494623\classes_dex2jar.jar
 * Qualified Name:     com.hoho.android.usbserial.util.SerialInputOutputManager
 * JD-Core Version:    0.7.0.1
 */