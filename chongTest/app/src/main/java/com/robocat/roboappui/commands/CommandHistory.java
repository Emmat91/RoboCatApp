package com.robocat.roboappui.commands;

import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;

/**
 * This class keeps track of the commands that have been sent to the Robo Cat and can save
 * and load the history to and from a file in internal storage.
 * @author Joey Phelps
 *
 */
public class CommandHistory {
	protected ArrayList<Command> commands;
	
	protected FileIO fio;
	
	/**
	 * Creates a new history.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CommandHistory() {
		commands = new ArrayList();
		fio = new FileIO();
	}
	
	/**
	 * Adds the command c to the history.
	 * @param c - the command.
	 */
	public void addCommand(Command c) {
		commands.add(new Command(c));
	}
	
	/**
	 * Returns the command history as a byte array which can be output to a file.
	 * @return the command history as a byte array.
	 */
	public byte[] getBytes() {
		int size = commands.size();
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++) {
			bytes[i] = commands.get(i).getData();
		}
		return bytes;
	}
	
	/**
	 * Deletes all entries in the command history.
	 */
	public void clear() {
		commands.clear();
	}
	
	/**
	 * Saves the command history to a file in external storage.
	 * @param filename - the output file
	 * @param context - the context the save function is being called from
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void save(String filename, Context context) throws FileNotFoundException, IOException {
		fio.saveExternal(filename, createSaveText(), context);
	}
	
	/**
	 * Loads a command history from a file in external storage.
	 * @param filename - the input file in external storage
	 * @param context - the context the load function is being called from
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void load(String filename) throws FileNotFoundException, IOException {
		clear();
		String buffer[] = fio.loadExternal(filename);
		CommandParser parser = new CommandParser();
		Command[] cbuf = parser.parseArray(buffer);
		for (int i = 0; i < cbuf.length; i++) {
			addCommand(cbuf[i]);
		}
	}
	
	/**
	 * Returns a String of all the commands in the command history
	 * @return the command history as a String.
	 */
	public String createDisplayableText() {
		int size = commands.size();
		String text = "";
		
		for (int i = 0; i < size; i++) {
			text += commands.get(i).getOutput() + "\n";
		}
		
		return text;
	}
	
	/**
	 * Returns a String array of all the commands in the command history.  This String[] can be
	 * written to a file.
	 * @return the String representation of the commands in the command history
	 */
	public String[] createSaveText() {
		int size = commands.size();
		String[] saveText = new String[size];
		
		for (int i = 0; i < size; i++) {
			saveText[i] = commands.get(i).getOutput(Command.DisplayMode.DISPLAY_NAME);
		}
		
		return saveText;
	}
	
	/*private long sumOfBytes() {
		
	}*/
	
}
