package com.robocat.roboappui.communication;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is a way to safely send messages between the Gui thread and the thread used
 * for USB communication.
 * @author Joey Phelps
 *
 */
public class InterThreadCommunication {
	protected static ReentrantLock lock = new ReentrantLock();
	
	protected static ArrayList<CommandMessage> gToUBytes = new ArrayList<CommandMessage>();
	
	protected static ArrayList<CommandMessage> uToGBytes = new ArrayList<CommandMessage>();
	
	public static final int GUI_THREAD = 0;
	public static final int USB_THREAD = 1;
	
	/**
	 * This is used to communicate the status of read and write operations.
	 * SUCCESS means the operation was successful, ERROR means there was an error,
	 * and NO_DATA means that there is currently no data to read.
	 *
	 */
	public enum Status {
		NO_DATA, SUCCESS, ERROR
	}
	
	/**
	 * This constructor does not do anything.
	 */
	public InterThreadCommunication() {
	}
	
	/**
	 * Sends a message to the other thread
	 * @param m - the message to send
	 * @param thread - the thread that is calling this function
	 * @return the status of the write operation
	 */
	public Status write(CommandMessage m, int thread) {
		if (thread == GUI_THREAD) {
			return add(gToUBytes, new CommandMessage(m));
		}
		if (thread == USB_THREAD) {
			return add(uToGBytes, new CommandMessage(m));
		}
		return Status.ERROR;
	}
	
	/**
	 * Reads a message sent from the other thread and and removes the message from the queue
	 * @param m - will be the message received from the other thread 
	 * @param thread - the thread calling this function
	 * @return the status of the read operation
	 */
	public Status readAndDelete(CommandMessage m, int thread) {
		Status status = Status.ERROR;
		if (thread == GUI_THREAD) {
			status = pop(uToGBytes, m);
		} else if (thread == USB_THREAD) {
			status = pop(gToUBytes, m);
		}
		return status;
	}
	
	/**
	 * Checks to see if there is a message from the other thread waiting to be read
	 * @param thread - the thread calling this function
	 * @return true if a read is waiting or false if not.
	 */
	public boolean readWaiting(int thread) {
		int size = 0;
		if (thread == GUI_THREAD) {
			size = size(uToGBytes);
		} else if (thread == USB_THREAD) {
			size = size(gToUBytes);
		}
		if (size > 0)
			return true;
		return false;
	}
	
	private int size(ArrayList<CommandMessage> arr) {
		int size;
		lock.lock();
		try {
			size = arr.size();
		} finally {
			lock.unlock();
		}
		return size;
	}
	
	private Status add(ArrayList<CommandMessage> arr, CommandMessage m) {
		Status status = Status.ERROR;
		lock.lock();
		try {
			arr.add(m);
			status = Status.SUCCESS;
		} finally {
			lock.unlock();
		}
		return status;
	}
	
	private Status pop(ArrayList<CommandMessage> arr, CommandMessage m) {
		Status status = Status.ERROR;
		lock.lock();
		try {
			m.setMessage(arr.get(0));
			arr.remove(0);
			status = Status.SUCCESS;
		} catch (IndexOutOfBoundsException e) {
			status = Status.NO_DATA;
		} finally {
			lock.unlock();
		}
		return status;
	}
	
	public static class CommandMessage {
		private Byte data;
		private String str;
		
		public CommandMessage() {
			this(new Byte((byte) 0x00));
		}
		
		public CommandMessage(Byte b) {
			this(b, "");
		}
		
		public CommandMessage(Byte b, String s) {
			data = b;
			str = s;
		}
		
		public CommandMessage(CommandMessage m) {
			this(m.getData(), m.getString());
		}
		
		public Byte getData() {
			return data;
		}
		
		public String getString() {
			return str;
		}
		
		public void setString(String s) {
			str = s;
		}
		
		public void setData(Byte b) {
			data = b;
		}
		
		public void setMessage(CommandMessage m) {
			data = m.getData();
			str = m.getString();
		}
		
		public boolean equalTo(CommandMessage m) {
			return data.compareTo(m.getData()) == 0 && str == m.getString();
		}
	}
	
}
