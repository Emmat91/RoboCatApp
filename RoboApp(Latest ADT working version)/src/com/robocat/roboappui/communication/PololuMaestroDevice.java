package com.robocat.roboappui.communication;

import android.hardware.usb.UsbDevice;

/**
 * Interface for abstracting Device type (USB, Serial, etc.).
 *
 * 
 */
public interface PololuMaestroDevice {
	int readInt();
	void sendCommand(byte[] buffer, int length);
	public void setDevice(UsbDevice device);
}

