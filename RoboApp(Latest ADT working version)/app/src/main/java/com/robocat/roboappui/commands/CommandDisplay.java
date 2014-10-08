package com.robocat.roboappui.commands;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;

/**
 * This class inherits the command history class and keeps track of commands sent to the
 * Robo Cat while keeping track of the text to display in the hardware developer interface.
 * @author Joey Phelps
 *
 */
@SuppressWarnings("unused")
public class CommandDisplay extends CommandHistory {
	protected static final String TEXT_START = "Commands Entered:\n";
	
	protected String text;
	
	/**
	 * Initializes the command history and the displayable text.
	 */
	public CommandDisplay() {
		super();
		text = "";
	}
	
	
	/**
	 * Deletes all entries in the command history and clears the displayable text.
	 */
	public void clear() {
		text = "";
		super.clear();
	}
	
	/**
	 * Returns a String that can be used to display all of the commands in the command history.
	 * @return the String of all the commands.
	 */
	public String getDisplayableText() {
		return TEXT_START + text;
	}
	
	/**
	 * Adds the command c to the command history and adds it to the displayable text.
	 * @param c - the command to be added.
	 */
	public void addCommand(Command c) {
		text += c.getOutput() + "\n";
		super.addCommand(c);
	}
}
