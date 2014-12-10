package com.robocat.roboappui.commands;

/**
 * This class is used to parse commands from Strings. The name of the command will be set
 * equal to the string passed in to parse.  The command data will be found by searching for
 * the substring within parentheses and the using the Byte.decode() function to decode the
 * substring to a byte.  Example: the string "abcde (0x4F) ghijk" will create a command
 * with the name "abcde (0x4F) ghijk" and the data sent for that command will be 0x04.
 * @author Joey Phelps
 *
 */
public class CommandParser {
	private Command command;
	
	private String strToParse;
	
	/**
	 * Default constructor
	 */
	public CommandParser() {
		this("");
	}
	
	/**
	 * Constructor that sets the string to parse to str
	 * @param str - string to parse
	 */
	public CommandParser(String str) {
		command = new Command();
		strToParse = new String(str);
	}
	
	/**
	 * Sets the string to parse to str
	 * @param str - string to parse
	 */
	public void setStringToParse(String str) {
		strToParse = new String(str);
	}
	
	/**
	 * Gets the command after it has been parsed
	 * @return the command
	 */
	public Command getCommand() {
		return new Command(command);
	}
	
	/**
	 * Parses the string to parse and creates the command
	 * @throws NumberFormatException
	 */
	public void parse() throws NumberFormatException {
		
		command.setName(strToParse);
		int lindex = strToParse.indexOf("(");
		int rindex = strToParse.indexOf(")");
		if (lindex >= 0 && rindex > lindex) {
			command.setData(strToParse.substring(lindex+1, rindex));
		} else {
			command.setName("parse_failed");
		}
	}
	
	/**
	 * Parses the string str and returns the command
	 * @param str - string to parse
	 * @return parsed command
	 */
	public Command parse(String str) {
		setStringToParse(str);
		parse();
		return getCommand();
	}
	
	/**
	 * Parses an array of strings and returns an array of commands
	 * @param str - string to parse
	 * @return commands parsed
	 */
	public Command[] parseArray(String[] str) {
		Command[] results = new Command[str.length];
		
		for (int i = 0; i < str.length; i++) {
			results[i] = parse(str[i]);
		}
		
		return results;
	}
}
