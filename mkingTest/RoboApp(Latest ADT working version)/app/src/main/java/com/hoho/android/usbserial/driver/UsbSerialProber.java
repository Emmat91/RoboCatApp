package com.hoho.android.usbserial.driver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public enum UsbSerialProber
{
  CDC_ACM_SERIAL,  FTDI_SERIAL;
  
  public static UsbSerialDriver acquire(UsbManager paramUsbManager)
  {
    Iterator localIterator = paramUsbManager.getDeviceList().values().iterator();
    UsbSerialDriver localUsbSerialDriver;
    do
    {
      if (!localIterator.hasNext()) {
        return null;
      }
      localUsbSerialDriver = acquire(paramUsbManager, (UsbDevice)localIterator.next());
    } while (localUsbSerialDriver == null);
    return localUsbSerialDriver;
  }
  
  public static UsbSerialDriver acquire(UsbManager paramUsbManager, UsbDevice paramUsbDevice)
  {
    UsbSerialProber[] arrayOfUsbSerialProber = values();
    int i = arrayOfUsbSerialProber.length;
    for (int j = 0;; j++)
    {
      UsbSerialDriver localUsbSerialDriver;
      if (j >= i) {
        localUsbSerialDriver = null;
      }
      do
      {
        return localUsbSerialDriver;
        localUsbSerialDriver = arrayOfUsbSerialProber[j].getDevice(paramUsbManager, paramUsbDevice);
      } while (localUsbSerialDriver != null);
    }
  }
  
  private static boolean testIfSupported(UsbDevice paramUsbDevice, Map<Integer, int[]> paramMap)
  {
    int[] arrayOfInt = (int[])paramMap.get(Integer.valueOf(paramUsbDevice.getVendorId()));
    if (arrayOfInt == null) {}
    for (;;)
    {
      return false;
      int i = paramUsbDevice.getProductId();
      int j = arrayOfInt.length;
      for (int k = 0; k < j; k++) {
        if (i == arrayOfInt[k]) {
          return true;
        }
      }
    }
  }
  
  public abstract UsbSerialDriver getDevice(UsbManager paramUsbManager, UsbDevice paramUsbDevice);
}


/* Location:           C:\Users\MattKing\Desktop\Robocat\RoboCat_1412286494623\classes_dex2jar.jar
 * Qualified Name:     com.hoho.android.usbserial.driver.UsbSerialProber
 * JD-Core Version:    0.7.0.1
 */