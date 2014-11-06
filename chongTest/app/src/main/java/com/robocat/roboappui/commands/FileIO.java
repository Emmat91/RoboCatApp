package com.robocat.roboappui.commands;

/**
 * This class handles writing and reading from files in internal storage
 */
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileIO {
	public static final String EXTERNAL_STORAGE_DIRECTORY = "Android/data/com.robocat.roboapp/commands";
	
	/**
	 * This is the extension currently used for servo control files
	 */
	public static final String ROBOCATMESSAGE_EXTENSION = ".rcm";
	
	public static final String ROBOCATCOMMAND_EXTENSION = ".rcc";
	
	protected boolean readableExternalStorage;
	protected boolean writeableExternalStorage;
	
	/**
	 * Constructor
	 */
	public FileIO() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    readableExternalStorage = writeableExternalStorage = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    readableExternalStorage = true;
		    writeableExternalStorage = false;
		} else {
		    readableExternalStorage = writeableExternalStorage = false;
		}
	}
	
	/**
	 * Saves the strings in buffer to the file output stream fos
	 * @param fos- file ouput stream
	 * @param buffer - Strings to write out
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void save(FileOutputStream fos, String[] buffer) throws FileNotFoundException, IOException {
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
		for (int i = 0; i < buffer.length; i++) {
			dos.writeUTF(buffer[i]);
		}
		
		dos.close();
	}

	/**
	 * Loads Strings from a file input stream and returns the Strings in a array
	 * @param fis - file input stream
	 * @return the Strings read in from file input stream
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected String[] load(FileInputStream fis) throws FileNotFoundException, IOException {
	
		ArrayList<String> strBuffer = new ArrayList();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
		
		try {
			while (true) {
				String str = dis.readUTF();
				strBuffer.add(str);
			}
		} catch (EOFException e) {
		}
		
		int size = strBuffer.size();
		String[] strArray = new String[size];
		for (int i = 0; i < size; i++) {
			strArray[i] = strBuffer.get(i);
		}
		
		return strArray;
	}
	
	/**
	 * Saves the strings in buffer to the file filename in internal storage
	 * @param filename - output file
	 * @param buffer - Strings to write out
	 * @param context - context this function was called from
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveInternal(String filename, String[] buffer, Context context) throws FileNotFoundException, IOException {
		FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
		save(fos, buffer);
	}

	/**
	 * Loads Strings from a file in internal storage and returns the Strings in a array
	 * @param filename - input file
	 * @param context - context this function was called from
	 * @return the Strings read in from filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String[] loadInternal(String filename, Context context) throws FileNotFoundException, IOException {
	
		ArrayList<String> strBuffer = new ArrayList();
	
		FileInputStream fis = context.openFileInput(filename);
		return load(fis);
	}
	
	/**
	 * Saves the strings in buffer to the file filename in external storage
	 * @param filename - output file
	 * @param buffer - Strings to write out
	 * @param context - context this function was called from
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveExternal(String filename, String[] buffer, Context context) throws FileNotFoundException, IOException {
		if (!writeableExternalStorage) {
			return;
		}
		
		File path = Environment.getExternalStorageDirectory();
		path = new File(path, EXTERNAL_STORAGE_DIRECTORY);
		path.mkdirs();
		
		File file = new File(path, filename);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		save(fos, buffer);
		// Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context,
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
            }
        });
	}
	
	/**
	 * Loads Strings from a file in external storage and returns the Strings in a array
	 * not yet completed
	 * @param filename - input file
	 * @param context - context this function was called from
	 * @return the Strings read in from filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String[] loadExternal(String filename) throws FileNotFoundException, IOException {
		if (!readableExternalStorage) {
			return null;
		}
		
		File path = Environment.getExternalStorageDirectory();
		path = new File(path, EXTERNAL_STORAGE_DIRECTORY);
		path.mkdirs();
		String[] str = path.list();
		
		File file = new File(path, filename);
		
		if (!file.isFile()) {
			return null;
		}
		
		FileInputStream fis = new FileInputStream(file);
		return load(fis);
	}
	
	/**
	 * gets the names of the files in the directory EXTERNAL_STORAGE_DIRECTORY with the extension ext
	 * @param ext - the file extension to look for
	 * @return an array of filenames of files with the extension ext.
	 */
	public static String[] getFileNamesInDirectoryByExtension(String ext) {
		File path = Environment.getExternalStorageDirectory();
		path = new File(path, EXTERNAL_STORAGE_DIRECTORY);
		path.mkdirs();
		String[] temp = path.list();
		boolean[] include = new boolean[temp.length];
		int count = 0;
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].endsWith(ext)) {
				include[i] = true;
				count++;
			} else {
				include[i] = false;
			}
		}
		String[] result = new String[count];
		int length = ext.length();
		for (int i = 0, j = 0; i < count && j < temp.length; j++) {
			if (include[j]) {
				result[i] = temp[j];
				i++;
			}
		}
		return result;
	}
	
	/**
	 * Gets the names of the files in the directory
	 * EXTERNAL_STORAGE_DIRECTORY
	 * @return an array of the files in EXTERNAL_STORAGE_DIRECTORY
	 * @see	EXTERNAL_STORAGE_DIRECTORY
	 */
	public static String[] getFilesNamesInDirectory() {
		File path = Environment.getExternalStorageDirectory();
		path = new File(path, EXTERNAL_STORAGE_DIRECTORY);
		path.mkdirs();
		return path.list();
	}


	/**
	 * Gets file names in a directory
	 * @param Dir - the directory to look for files in
	 * @return an array of the file names in Dir
	 */
    public static String[] getFilesNamesInDirectory(String Dir) {
        File path = Environment.getExternalStorageDirectory();
        path = new File(path, Dir);
        path.mkdirs();
        return path.list();
    }
    

	/**
	 * Removes ext from the end of str if str ends with ext
	 * @param str - the filename
	 * @param ext - the extension
	 * @return - the filename without the extension
	 */
	public static String removeExtension(String str, String ext) {
		if (str.endsWith(ext)) {
			str = str.substring(0, str.length() - ext.length());
		}
		return str;
	}
	
	/**
	 * Concatenates the two strings if str does not end with ext
	 * @param str - the filename
	 * @param ext - the extension
	 * @return str with ext appended to it
	 */
	public static String addExtension(String str, String ext) {
		if (!str.endsWith(ext)) {
			str = str + ext;
		}
		return str;
	}
}
