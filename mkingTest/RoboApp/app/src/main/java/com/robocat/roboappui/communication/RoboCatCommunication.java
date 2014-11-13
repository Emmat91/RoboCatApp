package com.robocat.roboappui.communication;

	import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.robocat.roboappui.commands.Command;
import com.robocat.roboappui.communication.InterThreadCommunication.*;

public class RoboCatCommunication {
	private InterThreadCommunication pipe;
	
	private static final String ERROR_MESSAGE = "Error";
	
	private static final int GUI_THREAD = InterThreadCommunication.GUI_THREAD;
	
	public static final int NO_DEVICE_ATTACHED = 0;
	public static final int SUCCESS = 1;
	public static final int FAILED = -1;
	public static final int DO_NOT_HAVE_PERMISSION = -2;
	
	private PololuMaestroUSBCommandProcess maestroSSC;
	
	private UsbManager manager;
	
	private boolean deviceAttached;
	private boolean hasPermission;
	
	public RoboCatCommunication(UsbManager m, UsbDevice d) {
		pipe = new InterThreadCommunication();
		manager = m;
		UsbDevice device = d;
		if (device == null) {
			deviceAttached = false;
			hasPermission = false;
		} else {
			deviceAttached = true;
			if (manager.hasPermission(device)) {
				hasPermission = true;
			} else {
				hasPermission = false;
			}
		}
		new Thread(new RoboCatThread()).start();
	}
	
	public boolean hasPermission() {
		return hasPermission;
	}
	
	public boolean isDeviceAttached() {
		return deviceAttached;
	}
	
	public int sendCommand(Command c) {
		if (!deviceAttached)
			return NO_DEVICE_ATTACHED;
		if (!hasPermission)
			return DO_NOT_HAVE_PERMISSION;
		CommandMessage m = new CommandMessage(c.getData(), "Command");
		Status status = pipe.write(m, GUI_THREAD);
		if (status == Status.SUCCESS)
			return SUCCESS;
		return FAILED;
	}
	
	private class RoboCatThread implements Runnable  {
		private static final int THREAD = InterThreadCommunication.USB_THREAD;
		
		public void run() {
			maestroSSC = new PololuMaestroUSBCommandProcess(manager);
			CommandMessage m = new CommandMessage();
			Status status;
			while(true) {
				if (pipe.readWaiting(THREAD)) {
					status = pipe.readAndDelete(m, THREAD);
					if (status == Status.SUCCESS)
						maestroSSC.sendCommand(new byte[] {m.getData()}, 1);
					else 
						pipe.write(new CommandMessage(null, ERROR_MESSAGE), THREAD);
				}
			}
			
		}
	}
}
