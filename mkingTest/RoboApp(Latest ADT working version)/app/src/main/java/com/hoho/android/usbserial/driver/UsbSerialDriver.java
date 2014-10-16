package com.hoho.android.usbserial.driver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import java.io.IOException;

public abstract class UsbSerialDriver
{
  public static final int DEFAULT_READ_BUFFER_SIZE = 16384;
  public static final int DEFAULT_WRITE_BUFFER_SIZE = 16384;
  protected final UsbDeviceConnection mConnection;
  protected final UsbDevice mDevice;
  protected byte[] mReadBuffer;
  protected final Object mReadBufferLock = new Object();
  protected byte[] mWriteBuffer;
  protected final Object mWriteBufferLock = new Object();
  
  public UsbSerialDriver(UsbDevice paramUsbDevice, UsbDeviceConnection paramUsbDeviceConnection)
  {
    this.mDevice = paramUsbDevice;
    this.mConnection = paramUsbDeviceConnection;
    this.mReadBuffer = new byte[16384];
    this.mWriteBuffer = new byte[16384];
  }
  
  public abstract void close()
    throws IOException;
  
  public final UsbDevice getDevice()
  {
    return this.mDevice;
  }
  
  public abstract void open()
    throws IOException;
  
  public abstract int read(byte[] paramArrayOfByte, int paramInt)
    throws IOException;
  
  public abstract int setBaudRate(int paramInt)
    throws IOException;
  
  public final void setReadBufferSize(int paramInt)
  {
    synchronized (this.mReadBufferLock)
    {
      if (paramInt == this.mReadBuffer.length) {
        return;
      }
      this.mReadBuffer = new byte[paramInt];
      return;
    }
  }
  
  public final void setWriteBufferSize(int paramInt)
  {
    synchronized (this.mWriteBufferLock)
    {
      if (paramInt == this.mWriteBuffer.length) {
        return;
      }
      this.mWriteBuffer = new byte[paramInt];
      return;
    }
  }
  
  public abstract int write(byte[] paramArrayOfByte, int paramInt)
    throws IOException;
}


/* Location:           C:\Users\MattKing\Desktop\Robocat\RoboCat_1412286494623\classes_dex2jar.jar
 * Qualified Name:     com.hoho.android.usbserial.driver.UsbSerialDriver
 * JD-Core Version:    0.7.0.1
 */