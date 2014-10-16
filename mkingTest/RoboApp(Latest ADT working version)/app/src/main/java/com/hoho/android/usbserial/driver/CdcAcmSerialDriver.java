package com.hoho.android.usbserial.driver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CdcAcmSerialDriver
  extends UsbSerialDriver
{
  private static final int SET_LINE_CODING = 32;
  private static final int USB_RECIP_INTERFACE = 1;
  private static final int USB_RT_ACM = 33;
  private final String TAG = CdcAcmSerialDriver.class.getSimpleName();
  private UsbEndpoint mControlEndpoint;
  private UsbInterface mControlInterface;
  private UsbInterface mDataInterface;
  private UsbEndpoint mReadEndpoint;
  private UsbEndpoint mWriteEndpoint;
  
  public CdcAcmSerialDriver(UsbDevice paramUsbDevice, UsbDeviceConnection paramUsbDeviceConnection)
  {
    super(paramUsbDevice, paramUsbDeviceConnection);
  }
  
  public static Map<Integer, int[]> getSupportedDevices()
  {
    LinkedHashMap localLinkedHashMap = new LinkedHashMap();
    localLinkedHashMap.put(Integer.valueOf(9025), new int[] { 1, 67, 16, 66, 59, 68, 63, 68, 32822 });
    localLinkedHashMap.put(Integer.valueOf(5824), new int[] { 1155 });
    return localLinkedHashMap;
  }
  
  private int sendAcmControlMessage(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
  {
    UsbDeviceConnection localUsbDeviceConnection = this.mConnection;
    if (paramArrayOfByte != null) {}
    for (int i = paramArrayOfByte.length;; i = 0) {
      return localUsbDeviceConnection.controlTransfer(33, paramInt1, paramInt2, 0, paramArrayOfByte, i, 5000);
    }
  }
  
  private int setAcmLineCoding(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    byte[] arrayOfByte = new byte[7];
    arrayOfByte[0] = ((byte)(paramInt1 & 0xFF));
    arrayOfByte[1] = ((byte)(0xFF & paramInt1 >> 8));
    arrayOfByte[2] = ((byte)(0xFF & paramInt1 >> 16));
    arrayOfByte[3] = ((byte)(0xFF & paramInt1 >> 24));
    arrayOfByte[4] = ((byte)paramInt2);
    arrayOfByte[5] = ((byte)paramInt3);
    arrayOfByte[6] = ((byte)paramInt4);
    return sendAcmControlMessage(32, 0, arrayOfByte);
  }
  
  public void close()
    throws IOException
  {
    this.mConnection.close();
  }
  
  public void open()
    throws IOException
  {
    Log.d(this.TAG, "claiming interfaces, count=" + this.mDevice.getInterfaceCount());
    Log.d(this.TAG, "Claiming control interface.");
    this.mControlInterface = this.mDevice.getInterface(0);
    Log.d(this.TAG, "Control iface=" + this.mControlInterface);
    if (!this.mConnection.claimInterface(this.mControlInterface, true)) {
      throw new IOException("Could not claim control interface.");
    }
    this.mControlEndpoint = this.mControlInterface.getEndpoint(0);
    Log.d(this.TAG, "Control endpoint direction: " + this.mControlEndpoint.getDirection());
    Log.d(this.TAG, "Claiming data interface.");
    this.mDataInterface = this.mDevice.getInterface(1);
    Log.d(this.TAG, "data iface=" + this.mDataInterface);
    if (!this.mConnection.claimInterface(this.mDataInterface, true)) {
      throw new IOException("Could not claim data interface.");
    }
    this.mReadEndpoint = this.mDataInterface.getEndpoint(1);
    Log.d(this.TAG, "Read endpoint direction: " + this.mReadEndpoint.getDirection());
    this.mWriteEndpoint = this.mDataInterface.getEndpoint(0);
    Log.d(this.TAG, "Write endpoint direction: " + this.mWriteEndpoint.getDirection());
    Log.d(this.TAG, "Setting line coding");
    setBaudRate(115200);
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt)
    throws IOException
  {
    synchronized (this.mReadBufferLock)
    {
      int i = Math.min(paramArrayOfByte.length, this.mReadBuffer.length);
      int j = this.mConnection.bulkTransfer(this.mReadEndpoint, this.mReadBuffer, i, paramInt);
      if (j < 0) {
        return 0;
      }
      System.arraycopy(this.mReadBuffer, 0, paramArrayOfByte, 0, j);
      return j;
    }
  }
  
  public int setBaudRate(int paramInt)
    throws IOException
  {
    setAcmLineCoding(paramInt, 0, 0, 8);
    return paramInt;
  }
  
  public int write(byte[] paramArrayOfByte, int paramInt)
    throws IOException
  {
    int i = 0;
    for (;;)
    {
      if (i >= paramArrayOfByte.length) {
        return i;
      }
      int j;
      int k;
      synchronized (this.mWriteBufferLock)
      {
        j = Math.min(paramArrayOfByte.length - i, this.mWriteBuffer.length);
        byte[] arrayOfByte;
        if (i == 0)
        {
          arrayOfByte = paramArrayOfByte;
          k = this.mConnection.bulkTransfer(this.mWriteEndpoint, arrayOfByte, j, paramInt);
          if (k <= 0) {
            throw new IOException("Error writing " + j + " bytes at offset " + i + " length=" + paramArrayOfByte.length);
          }
        }
        else
        {
          System.arraycopy(paramArrayOfByte, i, this.mWriteBuffer, 0, j);
          arrayOfByte = this.mWriteBuffer;
        }
      }
      Log.d(this.TAG, "Wrote amt=" + k + " attempted=" + j);
      i += k;
    }
  }
}


/* Location:           C:\Users\MattKing\Desktop\Robocat\RoboCat_1412286494623\classes_dex2jar.jar
 * Qualified Name:     com.hoho.android.usbserial.driver.CdcAcmSerialDriver
 * JD-Core Version:    0.7.0.1
 */