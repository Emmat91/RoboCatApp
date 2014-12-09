package com.robocat.roboappui.commands;

/**
 * This class stores the name of a command and a byte of data that can be sent to the cat
 * @author Joey Phelps
 *
 */
public class Command {
	private String commandName;
	
	private byte dataToSend;
	
	/**
	 * The DisplayMode specifies the format for the function getOutput().
	 * DISPLAY_NAME specifies that only the name will be returned by getOutput.
	 * DISPLAY_DATA specifies that only the data will be returned by getOutput.
	 * DISPLAY_BOTH specifies that the name followed by the data will be returned by getOutput.
	 */
	public enum DisplayMode {
		DISPLAY_NAME, DISPLAY_DATA, DISPLAY_BOTH
	}
	
	private DisplayMode displayMode;
	
	/**
	 * This constructor initializes the command name to "" and the command data to 0x00.
	 */
	public Command() {
		this("", "0x00");
	}
	
	/**
	 * This constructor initializes the command name to name, the command data to 0x00, and
	 * the display mode to DISPLAY_NAME.
	 * @param name - the command name.
	 */
	public Command(String name) {
		this(name, "0x00");
	}
	
	/**
	 * This constructor initializes the command name to name, the command data to data, and
	 * the display mode to DISPLAY_NAME.
	 * @param name - the command name.
	 * @param data - the command data as a String.
	 * @see Byte.decode(string)
	 */
	public Command(String name, String data) {
		this(name, parseByte(data));
	}
	
	/**
	 * This constructor initializes the command name to name, the command data to data, and
	 * the display mode to DISPLAY_NAME.
	 * @param name - the command name.
	 * @param data - the command data.
	 */
	public Command(String name, byte data) {
		this(name, data, DisplayMode.DISPLAY_NAME);
	}
	
	/**
	 * This constructor initializes the command name to name, the command data to data, and
	 * the display mode to mode.
	 * @param name - the command name.
	 * @param data - the command data.
	 * @param mode - the display mode. 
	 */
	public Command(String name, byte data, DisplayMode mode) {
		setName(name);
		dataToSend = data;
		displayMode = mode;
	}
	
	/**
	 * This constructor initializes the values to the values in c
	 * @param c - the command to copy from.
	 */
	public Command(Command c) {
		this(c.getName(), c.getData(), c.getDisplayMode());
	}
	
	/**
	 * Returns the command name.
	 * @return the command name.
	 */
	public String getName() {
		return new String(commandName);
	}
	
	/**
	 * Sets the command name to s.  If s is an empty string it sets the display mode to DISPLAY_DATA.
	 * @param s - the command name
	 */
	public void setName(String s) {
		commandName = new String(s);
		if (s == "")
			displayMode = DisplayMode.DISPLAY_DATA;
	}
	
	/**
	 * Returns the command data as a byte.
	 * @return the data to send to the cat.
	 */
	public byte getData() {
		return dataToSend;
	}
	
	/**
	 * Decodes s to a byte and sets the command data equal to that byte.
	 * @param s - the command data as a string.
	 * @see Byte.decode(string)
	 */
	public void setData(String s) {
		dataToSend = parseByte(s);
	}
	
	/**
	 * Sets the command data to b
	 * @param b - the command data.
	 */
	public void setData(byte b) {
		dataToSend = b;
	}
	
	/**
	 * Sets the command data to i
	 * @param i - the command data.
	 */
	public void setData(int i) {
		setData("0x" + Integer.toHexString(i));
	}
	
	/**
	 * Returns the current display mode.
	 * @return the display mode.
	 */
	public DisplayMode getDisplayMode() {
		return displayMode;
	}
	
	/**
	 * Sets the display mode to m.
	 * @param m - the new display mode.
	 */
	public void setDisplayMode(DisplayMode m) {
		displayMode = m;
	}
	
	/**
	 * Gets the output to be displayed in the command display based on the current display mode.
	 * @return DISPLAY_NAME specifies that only the name will be returned.
	 * DISPLAY_DATA specifies that only the data will be returned.
	 * DISPLAY_BOTH specifies that the name followed by the data will be returned.
	 */
	public String getOutput() {
		return getOutput(displayMode);
	}
	
	/**
	 * Gets the output to be displayed in the command display based on the parameter mode.
	 * @param mode - the mode to display output in.
	 * @return DISPLAY_NAME specifies that only the name will be returned.
	 * DISPLAY_DATA specifies that only the data will be returned.
	 * DISPLAY_BOTH specifies that the name followed by the data will be returned.
	 */
	public String getOutput(DisplayMode mode) {
		if (mode == DisplayMode.DISPLAY_NAME) {
			return getName();
		} else if (mode == DisplayMode.DISPLAY_DATA) {
			return toHexString(dataToSend);
			//return Integer.toHexString(dataToSend);
		} else {
			return getName() + " " + toHexString(dataToSend);
		}
	}
	
	/**
	 * parses a hex string into a byte
	 * @param d - the hex string to parse
	 * @return the byte value of d
	 */
	public static byte parseByte(String d) {
		int i = d.indexOf("0x");
		int b = 0;
		if (i == -1)
			i = 0;
		else
			i += 2;
		byte mSNib = hexCharToByte(d.charAt(i));
		byte lSNib = mSNib;
		if (d.length() > i + 1) {
			lSNib = hexCharToByte(d.charAt(i+1));
			b = mSNib << 4;
		}
		b = b | lSNib;
		return (byte) b;
	}
	
	/**
	 * parses a hex character into a byte
	 * @param c - the hex character to parse
	 * @return the byte value of c
	 */
	public static byte hexCharToByte(char c) {
		return Byte.decode("0x" + c);
	}
	
	/**
	 * Converts a byte to a string
	 * @param b - the byte
	 * @return the String representation of b.
	 */
	public static String toHexString(Byte b) {
		
		String str = Integer.toHexString(b);
		if (str.length() > 2)
			str = str.substring(str.length() - 2);
		return "0x" + str;
	}
	
	/**
	 * Converts a non-negative integer less than 16 to a hex digit represented as a String
	 * @param digit - the digit to convert to a hex String
	 * @return the digit in hex as a String.
	 */
	private static String digitToHexString(int digit) {
		String str = "";
		if (digit < 10) {
			str += digit;
		} else {
			str += ('A' + (digit - 10));
		}
		return str;
	}
}
