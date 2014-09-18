
/**
 * Main class for controlling servos. Currently implemented using Pololu
 * Protocol.
 * 
 * Android requires us to get user permission to access a USB device. Best
 * approach is to do that in the Activity and then pass the device into the
 * overloaded constructor.
 * 
 */
package com.robocat.roboappui.communication;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.Arrays;
public class PololuMaestroUSBCommandProcess {
	private static final String TAG = "PololuMaestro";
	
	// Pololu Protocol Commands
	public static final int CMD_SET_POSITION = 0x04;
	public static final int CMD_SET_SPEED = 0x07;
	public static final int CMD_SET_ACCELERATION = 0x09;
	public static final int CMD_SET_PWM = 0x0A;
	public static final int CMD_GET_POSITION = 0x10;
	public static final int CMD_GET_MOVING_STATE = 0x13;
	public static final int CMD_GET_ERRORS = 0xA1;
	public static final int CMD_GO_HOME = 0x22;
	
	private static final int SERVO_HOME_POSITION = 1500;
	
	// Default Maestro device number is 12 (0x0C)
	private int deviceNumber = 12;
	private PololuMaestroDevice maestroDevice;
	
	// TODO Move arrays to multi-dimensional arrays to capture values for
	// multiple device numbers.
	private int[] servoPosition = new int[24];
	private int[] servoSpeed = new int[24];
	private int[] servoAcceleration = new int[24];
	private int pwmOnTime = 0;
	private int pwmPeriod = 0;
	private byte[] dataArray = new byte[8];
	
	public enum DeviceType {
		DEVICE_TYPE_SERIAL, DEVICE_TYPE_USB
	};
	
	public PololuMaestroUSBCommandProcess(UsbManager usbManager) {
		this(usbManager, DeviceType.DEVICE_TYPE_SERIAL);
	}
	
	public PololuMaestroUSBCommandProcess(UsbManager usbManager, DeviceType type) {
		switch (type) {
			default:
			case DEVICE_TYPE_SERIAL:
				//maestroDevice = new PololuMaestroUSBDevice(usbManager);
				break;
		}
		
		Arrays.fill(servoPosition, SERVO_HOME_POSITION);
	}
	
	public void setDevice(UsbDevice device) {
		maestroDevice.setDevice(device);
	}
	
	/**
	 * getDeviceNumber() is a placeholder for when we implement the Pololu
	 * protocol.
	 * 
	 */
	public int getDeviceNumber() {
		return deviceNumber;
	}
	
	/**
	 * setDeviceNumber() is a placeholder for when we implement the Pololu
	 * protocol.
	 * 
	 */
	public void setDeviceNumber(int deviceNumber) {
		this.deviceNumber = deviceNumber;
	}
	
	public int getServoPosition(int servoNumber) {
		return servoPosition[servoNumber];
	}
	
	public void setServoPosition(int servoNumber, int position) {
		this.servoPosition[servoNumber] = position;
		
		// Pololu Protocol expects you to pass in how many QUARTER-seconds.
		position = position * 4;
		
		dataArray[0] = (byte) 0xAA;
		dataArray[1] = (byte) deviceNumber;
		dataArray[2] = (byte) CMD_SET_POSITION;
		dataArray[3] = (byte) servoNumber;
		dataArray[4] = (byte) (position & 0x7F);
		dataArray[5] = (byte) ((position >> 7) & 0x7F);
		
		sendCommand(dataArray, 4);
	}
	
	public int getServoAcceleration(int servoNumber) {
		return servoAcceleration[servoNumber];
	}
	
	public void setServoAcceleration(int servoNumber, int acceleration) {
		this.servoAcceleration[servoNumber] = acceleration;
		
		dataArray[0] = (byte) 0xAA;
		dataArray[1] = (byte) deviceNumber;
		dataArray[2] = (byte) CMD_SET_ACCELERATION;
		dataArray[3] = (byte) servoNumber;
		dataArray[4] = (byte) (acceleration & 0x7F);
		dataArray[5] = (byte) ((acceleration >> 7) & 0x7F);
		
		sendCommand(dataArray, 6);
	}
	
	public int getPWMOnTime() {
		return pwmOnTime;
	}
	
	public int getPWMPeriod() {
		return pwmPeriod;
	}
	
	public void setPWM(int onTime, int period) {
		this.pwmOnTime = onTime;
		this.pwmPeriod = period;
		
		dataArray[0] = (byte) 0xAA;
		dataArray[1] = (byte) deviceNumber;
		dataArray[2] = (byte) CMD_SET_PWM;
		dataArray[3] = (byte) (onTime & 0x7F);
		dataArray[4] = (byte) ((onTime >> 7) & 0x7F);
		dataArray[5] = (byte) (period & 0x7F);
		dataArray[6] = (byte) ((period >> 7) & 0x7F);
		
		sendCommand(dataArray, 7);
	}
	
	public boolean getMovingState() {
		dataArray[0] = (byte) 0xAA;
		dataArray[1] = (byte) deviceNumber;
		dataArray[2] = (byte) CMD_GET_MOVING_STATE;
		
		sendCommand(dataArray, 3);
		
		int rc = readInt();
		
		return (rc > 0 ? true : false);
	}
	
	public int getServoSpeed(int servoNumber) {
		return servoSpeed[servoNumber];
	}
	
	public void setServoSpeed(int servoNumber, int speed) {
		this.servoSpeed[servoNumber] = speed;
		
		dataArray[0] = (byte) 0xAA;
		dataArray[1] = (byte) deviceNumber;
		dataArray[2] = (byte) CMD_SET_SPEED;
		dataArray[3] = (byte) servoNumber;
		dataArray[4] = (byte) (speed & 0x7F);
		dataArray[5] = (byte) ((speed >> 7) & 0x7F);
		
		sendCommand(dataArray, 6);
	}
	
	
	public int readInt() {
		return (maestroDevice.readInt());
	}
	
	public void sendCommand(byte[] buffer, int length) {
		Log.d(TAG, "sendCommand(" + buffer[2] + ")");
		
		maestroDevice.sendCommand(buffer, length);
	}
}
