/* 
 * This is the main activity file for the RoboCat project. In this file, the following tasks can be completed:
 * i) 	detect the correct Pololu Maestro USB device;
 * ii) 	detect the correct Arduino Mega USB device;
 * iii) obtain the progress from the seek bar, and map the no. of the seek bar to the correct Pololu Maestro channel
 * iv)	complete the correct functionality for the RoboCat, e.g., sending command to the Pololu Maestro, record the gait values
 * 		for the RoboCat, reset the servos, ...
 * Author: Haiquan Zhang
 */

package com.robocat.roboappui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap; 
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.widget.Toast;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.content.Context;
import android.content.Intent;
//import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.robocat.roboappui.R;
import com.robocat.roboappui.communication.PololuMaestroUSBCommandProcess;


public class RoboCatActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
	private static final String TAG = "MaestroSSCActivity";
	private Button homeButton, recordButton, clearGaitButton, runGaitButton;
	//private SeekBar channel1PositionBar, channel2PositionBar, channel3PositionBar, channel4PositionBar, channel5PositionBar, channel6PositionBar,  channel7PositionBar, channel8PositionBar, channel9PositionBar, channel10PositionBar, channel11PositionBar, channel12PositionBar;
	private static PololuMaestroUSBCommandProcess maestroSSC;
	public static UsbDevice device;
	public static boolean deviceConnected = false;
	private static boolean maestroInitialized = false;
   // private Handler mHandler = new Handler();
    //TextView textViewChannel1Position, textViewChannel2Position, textViewChannel3Position, textViewChannel4Position, textViewChannel5Position, textViewChannel6Position, textViewChannel7Position, textViewChannel8Position, textViewChannel9Position, textViewChannel10Position, textViewChannel11Position, textViewChannel12Position;
	int channelCount = 12;
	// the seekBar range is [900, 1500], and there is no way to modify the beginning value of the seek bar. therefore, the actual progress value and the 
	// seekbar progress value are recorded separately
	int progressResetActual =1500;
	int progressOffset =900;
	// int array to record the current gait values
	int[] arrayGaitChannelProgress = new int[2*(channelCount+1)];
	//ArrayList<Integer> arrayGaitChannelProgress = new ArrayList<Integer>(Collections.nCopies(2*channelCount, -1));
	TextView[] textViewChannelPositionArray = new TextView[channelCount];
	private SeekBar[] channelPositionBarArray = new SeekBar[channelCount];
	//the map between the seekbar no. and the pololu maestro channel 
	int[] channelNoMapArray=new int[]{1,2,3,5,6,7,12,13,15,16,17,18};
	
	public static final String GAIT_DEFAULT_FILE_NAME = "GaitShared.txt";
	
	public static final String EXTERNAL_STORAGE_DIRECTORY = "Gait";
	
	//This was added to make sure two streams of data are not sent to the cat at the same time
	public static AtomicBoolean threadRunning = new AtomicBoolean(false);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = getLayoutInflater().inflate(R.layout.robocat_layout, null);
		setContentView(view);
		// initialize the gait array value to -1. declare in the beginning, but have to initialize here
		Arrays.fill(arrayGaitChannelProgress,-1);
		
		homeButton = (Button) view.findViewById(R.id.homeButtonid);
		homeButton.setOnClickListener(this);
		recordButton = (Button) view.findViewById(R.id.buttonpololurecordid);
		recordButton.setOnClickListener(this);
		clearGaitButton = (Button) view.findViewById(R.id.clearGaitButtonid);
		clearGaitButton.setOnClickListener(this);
		runGaitButton = (Button) view.findViewById(R.id.runGaitButtonid);
		runGaitButton.setOnClickListener(this);
		
		
		// Initialize the textView and seekBar
		for(int i = 0; i < channelCount; i++) {
			   textViewChannelPositionArray[i] = new TextView(this);
			   channelPositionBarArray[i] = new SeekBar(this);
			}

		
		channelPositionBarArray[0] = (SeekBar) view.findViewById(R.id.channel1PositionBar);
		//channel1PositionBar = (SeekBar) view.findViewById(R.id.channel1PositionBar);
		//channel1PositionBar.setOnSeekBarChangeListener(this);
		channelPositionBarArray[1] = (SeekBar) view.findViewById(R.id.channel2PositionBar);
		channelPositionBarArray[2] = (SeekBar) view.findViewById(R.id.channel3PositionBar);
		channelPositionBarArray[3] = (SeekBar) view.findViewById(R.id.channel4PositionBar);
		channelPositionBarArray[4] = (SeekBar) view.findViewById(R.id.channel5PositionBar);
		channelPositionBarArray[5] = (SeekBar) view.findViewById(R.id.channel6PositionBar);
		channelPositionBarArray[6] = (SeekBar) view.findViewById(R.id.channel7PositionBar);
		channelPositionBarArray[7] = (SeekBar) view.findViewById(R.id.channel8PositionBar);
		channelPositionBarArray[8] = (SeekBar) view.findViewById(R.id.channel9PositionBar);
		channelPositionBarArray[9] = (SeekBar) view.findViewById(R.id.channel10PositionBar);
		channelPositionBarArray[10] = (SeekBar) view.findViewById(R.id.channel11PositionBar);
		channelPositionBarArray[11] = (SeekBar) view.findViewById(R.id.channel12PositionBar);
		// set listeners for channelPositionBar
		for(int i = 0; i < channelCount; i++) {
			channelPositionBarArray[i].setOnSeekBarChangeListener(this);
		}
		
		//errorsTextView = (TextView) view.findViewById(R.id.errorsTextView);

		
		textViewChannelPositionArray[0] = (TextView) findViewById (R.id.channel1positionvalue);
		textViewChannelPositionArray[1] = (TextView) findViewById (R.id.channel2positionvalue);
		textViewChannelPositionArray[2] = (TextView) findViewById (R.id.channel3positionvalue);
		textViewChannelPositionArray[3] = (TextView) findViewById (R.id.channel4positionvalue);
		textViewChannelPositionArray[4] = (TextView) findViewById (R.id.channel5positionvalue);
		textViewChannelPositionArray[5] = (TextView) findViewById (R.id.channel6positionvalue);
		textViewChannelPositionArray[6] = (TextView) findViewById (R.id.channel7positionvalue);
		textViewChannelPositionArray[7] = (TextView) findViewById (R.id.channel8positionvalue);
		textViewChannelPositionArray[8] = (TextView) findViewById (R.id.channel9positionvalue);
		textViewChannelPositionArray[9] = (TextView) findViewById (R.id.channel10positionvalue);
		textViewChannelPositionArray[10] = (TextView) findViewById (R.id.channel11positionvalue);
		textViewChannelPositionArray[11] = (TextView) findViewById (R.id.channel12positionvalue);

		

		
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		initializeMaestro(manager);
		
		
	}
	
	public static void initializeMaestro(UsbManager manager) {
		if (!maestroInitialized) {
			maestroSSC = new PololuMaestroUSBCommandProcess(manager);
			maestroInitialized = true;
		}
	}
	
	
	public static void copyFileUsingFileChannels(File source, File dest)

		      throws IOException {

		  FileChannel inputChannel = null;

		  FileChannel outputChannel = null;

		  try {

		      inputChannel = new FileInputStream(source).getChannel();

		      outputChannel = new FileOutputStream(dest).getChannel();

		      outputChannel.transferFrom(inputChannel, 0, inputChannel.size());

		  } finally {

		      inputChannel.close();

		      outputChannel.close();

		  }

		}
	

	
	private void toastMessage(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), 
                string, Toast.LENGTH_LONG).show();
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.robo_cat, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (deviceConnected)
			maestroSSC.setDevice(device);
	}

	/**
	 * Handle button click
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG,"onClick(" + v.getId() + ")");
		// Get button id first
		final Button b = (Button) v;
	    String buttonText = b.getText().toString();


		if (v.equals(homeButton))
		{
			goHome();
			
		}
		else if (v.equals(recordButton))
		{
			recordGaitButtonAction();
		}
		else if (v.equals(runGaitButton))
		{
			try {
				runGaitButtonAction();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (v.equals(clearGaitButton))
		{
			clearGaitButton();
		}
	}

	
	public static void clearGaitButton()
	{
		
		// test: delete the Gait.txt file
		
		File root = new File(Environment.getExternalStorageDirectory(), "Gait");
        if (!root.exists()) {
            root.mkdirs();
        }
        //File gpxfile = new File(root, "Gait"+currentDateandTime+".txt");
        File gpxfile = new File(root, GAIT_DEFAULT_FILE_NAME);
        
		boolean deleted = gpxfile.delete();

		
	}
	
	//This static funciton was added in order to use this functionality from
	//a different activity
	public static void runGaitButtonActionStatic(final String directory, final String filename) throws IOException {
		// TODO Auto-generated method stub
		
///////////////
		Runnable r = new Runnable() {
			@Override
			public void run()
			{
				try 
				{
					File root = new File(Environment.getExternalStorageDirectory(), directory);
					if (!root.exists()) 
					{
						root.mkdirs();
					}
					File gpxfile = new File(root, filename);

					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gpxfile)));
					String line;
					while ((line = br.readLine()) != null) 
					{
						int[] gaitLineVal=parseGait(line);
						oneGaitActionStatic(gaitLineVal);
						for (int iloop =0; iloop<100000000; iloop++){}
					}

					br.close();

				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //<-- put your code in here.
				finally {
					threadRunning.set(false);
				}
			}
		};

		//This was added to make sure two streams of data are not sent to the cat at the same time
		if (threadRunning.get()) {
			return;
		}
		threadRunning.set(true);

		Handler h = new Handler();
		h.postDelayed(r, 1000); // <-- the "1000" is the delay time in miliseconds. 
	//////////////////////
	

	}
	
	public void runGaitButtonAction() throws IOException {
		// TODO Auto-generated method stub
		
///////////////
		Runnable r = new Runnable() {
			@Override
			public void run()
			{
				try 
				{
					File root = new File(Environment.getExternalStorageDirectory(), EXTERNAL_STORAGE_DIRECTORY);
					if (!root.exists()) 
					{
						root.mkdirs();
					}
					File gpxfile = new File(root, GAIT_DEFAULT_FILE_NAME);
	
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gpxfile)));
					String line;
					while ((line = br.readLine()) != null) 
					{
						int[] gaitLineVal=parseGait(line);
						oneGaitAction(gaitLineVal);
						for (int iloop =0; iloop<100000000; iloop++){}
					}
    
					br.close();

				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //<-- put your code in here.
				finally {
					threadRunning.set(false);
				}
			}
		};

		//This was added to make sure two streams of data are not sent to the cat at the same time
		if (threadRunning.get()) {
			return;
		}
		threadRunning.set(true);

		Handler h = new Handler();
		h.postDelayed(r, 1000); // <-- the "1000" is the delay time in miliseconds. 
		//////////////////////
		
		
}
	
	//This static funciton was added in order to use this functionality from
	//a different activity
	private static void oneGaitActionStatic(int[] gaitLineVal) {
		// TODO Auto-generated method stub
    	for (int iRunGait=0, lenRunGait = gaitLineVal.length/2; iRunGait <lenRunGait; iRunGait++)
    	{	
    		// double iRunGait to point to the right channel
    		int iRunGaitDouble = iRunGait*2;
    		if (gaitLineVal[iRunGaitDouble] == -1) continue;
    		if (gaitLineVal[iRunGaitDouble] <0 ) 
	    			{
    			break;
    			}
    		else
    		{
	    		final int channelNoMapped = gaitLineVal[iRunGaitDouble];
	    		final int progressActual = gaitLineVal[iRunGaitDouble+1];
	    		int channelNoSeekBar=iRunGaitDouble/2;
	    		progressChangeActionStatic(channelNoMapped, progressActual, channelNoSeekBar);


/*			    		new Timer().schedule(new TimerTask() {          
	    		    @Override
	    		    public void run() {
	    		        // this code will be executed after 2 seconds       
			    		progressChangeAction(channelNo, progressActual);
	    		    }
	    		}, 2000);
*/			    		
/*
		    	try {
		    	    Thread.sleep(1000);
		    	} catch(InterruptedException ex) {
		    	    Thread.currentThread().interrupt();
		    	}			    		
*/		    			
    		}
    		
    	}
	}
	
	private void oneGaitAction(int[] gaitLineVal) {
		// TODO Auto-generated method stub
    	for (int iRunGait=0, lenRunGait = gaitLineVal.length/2; iRunGait <lenRunGait; iRunGait++)
    	{	
    		// double iRunGait to point to the right channel
    		int iRunGaitDouble = iRunGait*2;
    		if (gaitLineVal[iRunGaitDouble] == -1) continue;
    		if (gaitLineVal[iRunGaitDouble] <0 ) 
	    			{
    			break;
    			}
    		else
    		{
	    		final int channelNoMapped = gaitLineVal[iRunGaitDouble];
	    		final int progressActual = gaitLineVal[iRunGaitDouble+1];
	    		int channelNoSeekBar=iRunGaitDouble/2;
	    		progressChangeAction(channelNoMapped, progressActual, channelNoSeekBar);


/*			    		new Timer().schedule(new TimerTask() {          
	    		    @Override
	    		    public void run() {
	    		        // this code will be executed after 2 seconds       
			    		progressChangeAction(channelNo, progressActual);
	    		    }
	    		}, 2000);
*/			    		
/*
		    	try {
		    	    Thread.sleep(1000);
		    	} catch(InterruptedException ex) {
		    	    Thread.currentThread().interrupt();
		    	}			    		
*/		    			
    		}
    		
    	}

	}

	//This static funciton was added in order to use the functionality of the
	//non-static version of this function from a different Activity
	private static void progressChangeActionStatic(int channelNoMapped, int progressActual, int channelNoSeekBar)
	{
		if (deviceConnected)
			maestroSSC.setServoPosition(channelNoMapped, progressActual);
	}

	private void progressChangeAction(int channelNoMapped, int progressActual, int channelNoSeekBar)
	{
		if (deviceConnected)
			maestroSSC.setServoPosition(channelNoMapped, progressActual);
		// modify the offset for the progress of the seek bar
		int progressSeekBar = progressActual - progressOffset;
		channelPositionBarArray[channelNoSeekBar].setProgress(progressSeekBar);
		textViewChannelPositionArray[channelNoSeekBar].setText(String.valueOf(progressActual));
	}

	public static int[] parseGait(String line) {
		// TODO Auto-generated method stub

		String[] byteValues = line.substring(0, line.length() -1).split(",");
		
		//String[] byteValues = arr.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

		int[] results = new int[byteValues.length];

		for (int i = 0; i < byteValues.length; i++) {
		    try {
		        results[i] = Integer.parseInt(byteValues[i]);
		    } catch (NumberFormatException nfe) {};
		}
		
//		byte[] bytes = new byte[byteValues.length];
//
	//	for (int i=0, len=bytes.length; i<len; i++) {
//			   bytes[i] = Byte.valueOf(byteValues[i].trim());     
		//	   bytes[i] = Byte.valueOf(byteValues[i]);     
		//}

		//String str = new String(bytes);
		return results;
	}


	private void recordGaitButtonAction() {
		// TODO Auto-generated method stub
		generateGaitOnSD(GAIT_DEFAULT_FILE_NAME, arrayGaitChannelProgress);
		
	}
	
	public void generateGaitOnSD(String sFileName, int[] arrayGaitChannelProgress){
	    try
	    {
	        File root = new File(Environment.getExternalStorageDirectory(), EXTERNAL_STORAGE_DIRECTORY);
	        if (!root.exists()) {
	            root.mkdirs();
	        }
	        File gpxfile = new File(root, sFileName);
	      //  FileWriter writer = new FileWriter(gpxfile);
	    //    writer.append(Integer.toString(gaitArray[1]));
	  //      writer.flush();
	//        writer.close();
	        //Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
	        BufferedWriter outputWriter = null;
	        if (gpxfile.exists())
	        {
	        	outputWriter = new BufferedWriter(new FileWriter(gpxfile,true));
		        	
	        }
	        else
	        {
	        	outputWriter = new BufferedWriter(new FileWriter(gpxfile));
	        }
	        
	        //outputWriter = new BufferedWriter(new FileWriter(gpxfile));
	        for (int i = 0; i < arrayGaitChannelProgress.length; i++) {
	          // Maybe:
//	        	if (i != arrayGaitChannelProgress.length-1)
	        	{outputWriter.append(arrayGaitChannelProgress[i]+",");}
//	        	else
//	        	{outputWriter.append(String.valueOf(arrayGaitChannelProgress[i]));}
	          // Or:
//	          outputWriter.write(Integer.toString(gaitArray[i]);
	        }
            outputWriter.newLine();
	        outputWriter.flush();  
	        outputWriter.close();  
	    }
	    catch(IOException e)
	    {
	         e.printStackTrace();
	//         importError = e.getMessage();
	  //       iError();
	    }
	   }  
	

	public void goHome() {
		//dataArray[0] = (byte) 0xAA;
		//dataArray[1] = (byte) deviceNumber;
		//dataArray[2] = (byte) CMD_GO_HOME;
		
		//sendCommand(dataArray, 3);
		for (int channelNoSeekBar = 0; channelNoSeekBar < channelCount; channelNoSeekBar++)
		{
			// obtain the mapped channelNo
			int channelNoMapped = channelNoMapArray[channelNoSeekBar];
				progressChangeAction(channelNoMapped, progressResetActual, channelNoSeekBar);
			}	

	}

	/**
	 * SeekBar Listener
	 */
	public void onProgressChanged (SeekBar seekBar, int progressSeekBar, boolean fromUser) {
		int progressActual = progressSeekBar + progressOffset;
		Log.d(TAG,"onProgressChanged(" + seekBar.getId() + ", " + progressActual + ")");
		for (int channelNoSeekBar = 0; channelNoSeekBar < channelCount; channelNoSeekBar++)
		{
			if (seekBar.equals(channelPositionBarArray[channelNoSeekBar])) {
				// save position
				int channelNoMapped;
				channelNoMapped = channelNoMapArray[channelNoSeekBar];
				arrayGaitChannelProgress[channelNoSeekBar*2]=channelNoMapped;
				arrayGaitChannelProgress[channelNoSeekBar*2+1]=progressActual;
				// pololu operation
				
				progressChangeAction(channelNoMapped, progressActual, channelNoSeekBar);
				break;
			}	
			
		}
	}

	/**
	 * SeekBar Listener
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * SeekBar Listener
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}	
}




