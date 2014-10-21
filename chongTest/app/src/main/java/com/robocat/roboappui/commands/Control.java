package com.robocat.roboappui.commands;

import com.robocat.roboappui.commands.CommandDisplay;
import com.robocat.roboappui.commands.CommandHistory;
import com.robocat.roboappui.communication.RoboCatCommunication;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class serves as an interface between the GUI and some of the functionality
 * of the app including sending data to the cat and keeping track of commands sent.
 * @author Joey Phelps
 *
 */
public class Control {
	public static final String FILENAME = "test.rcc";
	
	protected static CommandDisplay commandDisplay;
	
	protected static ArrayList<Command> knownCommands;
	
	protected static ArrayList<CommandHistory> histories;
	
	protected static RoboCatCommunication robocat;
	
	public Control() {
		
	}
	
	/**
	 * Creates a new command display, parses the commands in commands and adds them to
	 * known commands.  This should only be called once in the application.
	 * @param commands - commands that will appear in the command select drop down
	 */
	public static void initialize(String[] commands, UsbManager manager, UsbDevice device) {
		histories = new ArrayList<CommandHistory>();
		commandDisplay = new CommandDisplay();
		
		knownCommands = new ArrayList<Command>();
		
		initiateKnownCommands(commands);
		robocat = new RoboCatCommunication(manager, device);
	}
	
	/**
	 * Creates a new command history to store sent commands
	 * @return a value that is used to identify the new command history
	 */
	public static int newCommandHistory() {
		histories.add(new CommandHistory());
		return histories.size() - 1;
	}
	
	/**
	 * Removes the commandHistory specified by commandHistory
	 * @param commandHistory - the CommandHistory to close
	 */
	public static void closeCommandHistory(int commandHistory) {
		if (commandHistory >= 0 && commandHistory < histories.size()) {
			histories.remove(commandHistory);
		}
	}
	
	/**
	 * Sends a command and stores it in the command history specified by commandHistory
	 * @param command - the command to send
	 * @param commandHistory - the command history to save the command to.
	 * @return true if the command was sent successfully, false otherwise
	 */
	public static boolean sendCommand(Command command, int commandHistory) {
		if (commandHistory < 0 || commandHistory >= histories.size())
			return false;
		boolean successful = sendCommandToCat(command);
		CommandHistory c = histories.get(commandHistory);
		if (successful) {
			c.addCommand(command);
		} else {
			c.addCommand(new Command("Error"));
		}
		return successful;
	}
	
	
	/**
	 * Sends a command to the cat
	 * @param command - command to send
	 */
	public static void sendCommand(Command command) {
		boolean successful = sendCommandToCat(command);
		if (successful) {
			//commandHistory.addCommand(command);
			commandDisplay.addCommand(command);
		} else {
			commandDisplay.addCommand(new Command("Error"));
		}
	}
	
	/**
	 * Searches for a command in the known commands where name == command.
	 * If it finds one it sends this command to the cat and saves it to the command history specified
	 * by commandHistory
	 * @param command - command name to search for.
	 */
	public static boolean sendCommand(String command, int commandHistory) {
		int index = findKnownCommand(command);
		if (index >= 0) {
			return sendCommand(knownCommands.get(index), commandHistory);
		} else {
			return false;
		}
	}
	
	/**
	 * Searches for a command in the known commands where name == command.
	 * If it finds one it sends this command to the cat.
	 * @param command - command name to search for.
	 */
	public static void sendCommand(String command) {
		int index = findKnownCommand(command);
		if (index >= 0) {
			sendCommand(knownCommands.get(index));
		} else {
			
		}
	}
	
	/**
	 * Clears the command history specified by commandHistory
	 * @param commandHistory - the command history to clear
	 */
	public static void clearCommandHistory(int commandHistory) {
		if (commandHistory >= 0 && commandHistory < histories.size())
			histories.get(commandHistory).clear();
	}
	
	/**
	 * Clears the command display
	 */
	public static void clearCommandDisplay() {
		commandDisplay.clear();
	}
	
	/**
	 * Saves the command history to a file in internal storage
	 * @param filename - the name of the file to save the commands to
	 * @param context - context this function was called from
	 * @param commandHistory - the command history to save
	 */
	public static void saveCommandHistory(String filename, Context context, int commandHistory) {
		if (filename == "" || commandHistory < 0 || commandHistory >= histories.size())
			return;
		Toast toast = Toast.makeText(context, "In saveCommandDisplay()", Toast.LENGTH_SHORT);
    	toast.show();
    	try {
    		histories.get(commandHistory).save(filename, context);
    	} catch (FileNotFoundException e) {
    		
    	} catch (IOException e) {
    		
    	}
    	
	}
	
	/**
	 * Saves the command display to a file in internal storage
	 * @param filename - the name of the file to save the commands to
	 * @param context - context this function was called from
	 */
	public static void saveCommandDisplay(String filename, Context context) {
		if (filename == "")
			return;
		Toast toast = Toast.makeText(context, "In saveCommandDisplay()", Toast.LENGTH_SHORT);
    	toast.show();
    	try {
    		commandDisplay.save(filename, context);
    	} catch (FileNotFoundException e) {
    		
    	} catch (IOException e) {
    		
    	}
    	
	}
	
	/**
	 * Loads the command display from a file
	 * @param filename - the file to load the commands from
	 */
	public static void loadCommandDisplay(String filename) {
		if (filename == "")
			return;
    	try {
    		commandDisplay.load(filename);
    	} catch (FileNotFoundException e) {
    		
    	} catch (IOException e) {
    		
    	}
	}
	
	/**
	 * Returns a String[] of the names of all the known commands which appear in the command select.
	 * @return The name of the known commands.
	 */
	public static String[] getKnownCommands() {
		int size = knownCommands.size();
		String[] str = new String[size];
		
		for (int i = 0; i < size; i++) {
			str[i] = knownCommands.get(i).getName();
		}
		
		return str;
	}
	
	/**
	 * Returns the text to display in the commands sent text field
	 * @return text to display in commands sent text field
	 */
	public static String getDisplayableText() {
		return commandDisplay.getDisplayableText();
	}
	
	/**
	 * Decodes data to a byte and send that byte to the Robo Cat
	 * @param data - byte to send to the cat
	 * @see Byte.decode(string)
	 */
	public static void sendManualCommand(String data) {
		Command c = new Command("Manual (" + data + ")", data);
		sendCommand(c);
	}
	
	/**
	 * Sends the command to the cat and returns true if it was successful
	 * @param command - command to send to the Robo Cat
	 * @return true if the command was sent successfully and false otherwise
	 */
	private static boolean sendCommandToCat(Command command) {
		return true;//robocat.sendCommand(command) >= 0;
	}
	
	/**
	 * Returns the index of a known command where name == command
	 * @param command - name of command
	 * @return the index of the command or -1 if no matching command is found
	 */
	private static int findKnownCommand(String command) {
		int index = -1;
		int size = knownCommands.size();
		for (int i = 0; i < size && index == -1; i++) {
			if (command.equals(knownCommands.get(i).getName())) {
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Parses the Strings in commands, creates new commands for each one, and
	 * adds them to known commands.
	 * @param commands - commands to parse
	 */
	private static void initiateKnownCommands(String[] commands) {
		CommandParser parser = new CommandParser();
		Command[] c = parser.parseArray(commands);
		
		for (int i = 0; i < c.length; i++) {
			if (!c[i].getName().equals("parse_failed")) {
				knownCommands.add(c[i]);
			}
		}
	}
}
