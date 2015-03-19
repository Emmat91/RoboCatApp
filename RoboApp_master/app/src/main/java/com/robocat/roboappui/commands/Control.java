package com.robocat.roboappui.commands;

import com.robocat.roboappui.AudioChooser;
import com.robocat.roboappui.MainAct;
import com.robocat.roboappui.MultiTouchActivity;
import com.robocat.roboappui.R;
import com.robocat.roboappui.MultiTouchActivity.TypeOfAction;
import com.robocat.roboappui.RoboCatActivity;
import com.robocat.roboappui.commands.CommandDisplay;
import com.robocat.roboappui.commands.CommandHistory;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.io.File;
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
	
	public static final String NONE_SELECTED = "None Selected";
	
	protected static CommandDisplay commandDisplay;
	
	protected static ArrayList<Command> knownCommands;
	
	protected static ArrayList<CommandHistory> histories;
	
	protected static EnumMap<MultiTouchActivity.TypeOfAction, Pair<String, Pair<Boolean, Boolean>>> touchToFileMap;
	
	public static TypeOfAction actionToBeMapped;
	
	public Control() {
		
	}
	
	/**
	 * Creates a new command display, parses the commands in commands and adds them to
	 * known commands.  This should only be called once in the application.
	 * @param commands - commands that will appear in the command select drop down
	 */
	public static void initialize(String[] commands) {
		histories = new ArrayList<CommandHistory>();
		commandDisplay = new CommandDisplay();
		
		knownCommands = new ArrayList<Command>();
		
		initiateKnownCommands(commands);
		touchToFileMap = new EnumMap<TypeOfAction, Pair<String, Pair<Boolean,Boolean>>>(TypeOfAction.class);
		for (TypeOfAction t : TypeOfAction.values()) {
			touchToFileMap.put(t, Pair.create(NONE_SELECTED, Pair.create(false, false)));
		}
	}
	
	/**
	 * This finds which operation is mapped to the given touch action and performs that operation
	 * @param action - the multi-touch operation that was performed
	 * @param context - the context of the activity that calls this function
	 * @return the filename of the file that was played if it was an audio file or 
	 * the filename of the command file sent to the cat
	 */
	public static String performTouchOperation(TypeOfAction action, Context context) {
		Pair<String, Pair<Boolean, Boolean>> p = touchToFileMap.get(action);
		if (p != null && p.second.second) {
			if(p.second.first) {  //is audio file
				try {
					AudioChooser.Play_audio(context, p.first, context.getResources().getStringArray(R.array.audio_files));
				} catch (FileNotFoundException e) {
					return "";
				}
				return "Playing: " + p.first;
			}
            else {  // is command
                try {
                    File root = new File("/storage/emulated/0/Android/data/com.robocat.roboapp/commands/" + p.first);

                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(root)));

                    int[] gaitLineVal= RoboCatActivity.parseGait(br);
                    RoboCatActivity.generateGaitOnSD("GaitShared.txt",gaitLineVal);
                    int variance[] = new int[RoboCatActivity.channelCount];
                    for (int i = 0; i < RoboCatActivity.channelCount; i++) {
                        variance[i] = (gaitLineVal[i] - RoboCatActivity.storedServo[i]) / RoboCatActivity.iterations;
                    }
                    long startTime;
                    long endTime;
                    for (int i = 0; i < RoboCatActivity.iterations; i++) {
                        startTime = System.nanoTime(); // testing time between break points
                        for (int j = 0; j < RoboCatActivity.channelCount; j++) {
                            RoboCatActivity.progressChangeAction(RoboCatActivity.channelNoMapArray[j], RoboCatActivity.storedServo[j] + variance[j], j);
                        }
                        endTime = System.nanoTime(); // testing time between break points
                        long timer = (endTime-startTime) / 1000000;
                        try {
                            if ((RoboCatActivity.time * 100 / RoboCatActivity.iterations) > timer) {
                                Thread.sleep(Long.valueOf((RoboCatActivity.time * 100 / RoboCatActivity.iterations) - timer));
                            }
                        }
                        catch (InterruptedException e){
                        }
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
			/*try {
				execute(p.first);
			} catch (IOException e) {
				return "";
			}*/
			return FileIO.removeExtension(p.first, FileIO.ROBOCATMESSAGE_EXTENSION);
		}
		return "";
	}
	
	/**
	 * Maps a file to a touch operation
	 * @param action - the multi-touch operation
	 * @param filename - the file to use when this touch operation is performed
	 */
	public static void mapTouchToFile(TypeOfAction action, String filename) {
		if (action == null) {
			action = actionToBeMapped;
		}
		if (action == null) {
			return;
		}
		
		boolean audioFile = true;
		if (filename.endsWith(FileIO.ROBOCATMESSAGE_EXTENSION)) {
			audioFile = false;
		}
		touchToFileMap.put(action, Pair.create(filename, Pair.create(audioFile, true)));
	}
	
	/**
	 * Finds the file that maps to a touch operation
	 * @param action - the multi touch operation performed
	 * @return the name of the file that action maps to
	 */
	public static String getFileMappedToTouch(TypeOfAction action) {
		Pair<String, Pair<Boolean, Boolean>> p = touchToFileMap.get(action);
		if (p != null && p.second.second) {
			return p.first;
		}
		return null;
	}
	
	/**
	 * This function copies the file to the RoboCatActivity's GAIT_DEFAULT_FILE_NAME. It then 
	 * tells the RoboCatActivity to send the data in this file to the cat
	 * @param filename - the name of the file to read from
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void execute(String filename) throws IOException, FileNotFoundException {
		File path = new File(Environment.getExternalStorageDirectory(), FileIO.EXTERNAL_STORAGE_DIRECTORY);
        if (!path.exists()) {
            path.mkdirs();
        }
        File source = new File(path, filename);
        if (!source.exists()) {
        	throw new FileNotFoundException();
        }
        RoboCatActivity.runGaitButtonActionStatic(FileIO.EXTERNAL_STORAGE_DIRECTORY, filename);
	}
	
	/**
	 * Copies the temporary file used by the RoboCatActivity to a file in external storage with the name
	 * filename in in the directory FileIO.EXTERNAL_STORAGE_DIRECTORY
	 * @param filename - the name of the file to save to.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void saveGaitFile(String filename) throws IOException, FileNotFoundException {
		File path = new File(Environment.getExternalStorageDirectory(), FileIO.EXTERNAL_STORAGE_DIRECTORY);
        if (!path.exists()) {
            path.mkdirs();
        }
        File dest = new File(path, filename);
        if (!dest.exists()) {
        	dest.createNewFile();
        }
        path = new File(Environment.getExternalStorageDirectory(), RoboCatActivity.EXTERNAL_STORAGE_DIRECTORY);
        if (!path.exists()) {
            path.mkdirs();
        }
        File source = new File(path, RoboCatActivity.GAIT_DEFAULT_FILE_NAME);
        if (!dest.exists()) {
        	throw new FileNotFoundException();
        }
        RoboCatActivity.copyFileUsingFileChannels(source, dest);
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
    	//toast.show();
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
		//  = Toast.makeText(context, "In saveCommandDisplay()", Toast.LENGTH_SHORT);
    	//toast.show();
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
