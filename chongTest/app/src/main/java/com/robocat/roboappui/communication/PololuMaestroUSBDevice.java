package com.robocat.roboappui.communication;

import java.io.IOException;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class PololuMaestroUSBDevice implements PololuMaestroDevice {
	private static final String TAG = "PololuMaestroUSBDevice";
	private static final int TIMEOUT = 1000;
	private UsbManager usbManager;
	private UsbSerialDriver serialDevice;
	private SerialInputOutputManager serialIoManager;
	
	public PololuMaestroUSBDevice(UsbManager manager) {
		this.usbManager = manager;
	}
	
	public void close() {
		stopIoManager();
		if (serialDevice != null) {
			try {
				serialDevice.close();
			} catch (IOException e) {
				// Ignore.
			}
			serialDevice = null;
		}
	}
	
	private void stopIoManager() {
		if (serialIoManager != null) {
			Log.i(TAG, "Stopping io manager ..");
			serialIoManager.stop();
			serialIoManager = null;
		}
	}
	
	private void startIoManager() {
		if (serialDevice != null) {
			Log.i(TAG, "Starting io manager ..");
			serialIoManager = new SerialInputOutputManager(serialDevice, mListener);
		}
	}
	
	private void onDeviceStateChange() {
		stopIoManager();
		startIoManager();
	}
	
	@Override
	public int readInt() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void sendCommand(byte[] buffer, int length) {
		try {
			int outBytes = serialDevice.write(buffer, TIMEOUT);
			Log.d(TAG, "sendCommand(" + HexDump.toHexString(buffer, 0, length) + ", " + length + ") wrote " + outBytes);
		} catch (IOException e) {
			Log.wtf(TAG, e);
		}
	}
	
	@Override
	public void setDevice(UsbDevice device) {
		Log.d(TAG, "setDevice(" + device + ")");
		
		//serialDevice = UsbSerialProber.acquire(usbManager, device);
		
        final UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection != null) {
        	serialDevice = new CdcAcmSerialDriver(device, connection);
        }

		Log.d(TAG, "Resumed, serialDevice=" + serialDevice);
		
		if (serialDevice == null) {
			Log.e(TAG, "No serial device.");
		} else {
			try {
				serialDevice.open();
			} catch (IOException e) {
				Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
				try {
					serialDevice.close();
				} catch (IOException e2) {
					// Ignore.
				}
				serialDevice = null;
				return;
			}
		}
		onDeviceStateChange();
	}
	
	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
		
		@Override
		public void onRunError(Exception e) {
			Log.d(TAG, "Runner stopped.");
		}
		
		@Override
		public void onNewData(final byte[] data) {
			Log.d(TAG, "onNewData(" + data + ")");
		}
	};
}
