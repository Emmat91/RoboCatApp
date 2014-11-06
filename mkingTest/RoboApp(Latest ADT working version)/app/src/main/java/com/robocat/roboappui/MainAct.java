package com.robocat.roboappui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.robocat.roboappui.R;
import com.robocat.roboappui.communication.PololuMaestroUSBCommandProcess;
import com.robocat.roboappui.commands.Control;
import com.robocat.roboappui.dialog.RoboAppDialogFragment;

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
import java.util.Locale;



//   https://code.google.com/p/android-file-dialog/
//   https://github.com/mburman/Android-File-Explore

/**
 * Class MainAct is the root activity that handles switching between fragments
 */
public class MainAct extends FragmentActivity implements ActionBar.TabListener, 
RoboAppDialogFragment.RoboAppDialogListener  {

	private static final String TAG = "MaestroSSCActivity";
    private static boolean deviceConnected;
    private static PololuMaestroUSBCommandProcess maestroSSC;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    
    private static Control control;
    
    /**
     * mRecorder - to record audio
     */
    private static MediaRecorder mRecorder = null;
    
    public static final String AUDIO_DIRECTORY = "/Android/data/com.robocat.roboapp/audio";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        
        //initializes the control object which serves as the interface between the gui and
        //the usb communication
        control = (Control) getLastCustomNonConfigurationInstance();
        if(control == null) {
        	Resources res = getResources();
        	String[] commands = res.getStringArray(R.array.command_select_options);
        	UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        	Intent intent = getIntent();
        	UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        	control = new Control();
        	control.initialize(commands, manager, device);
        }
        
//        launchAboutActivity();
    }
    
    @Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		Log.d(TAG, "onResume(" + intent + ")");
		String action = intent.getAction();
		deviceConnected = false;
		if (action.equals("android.intent.action.MAIN")) {
            Toast.makeText(getApplicationContext(), 
                    "Reconnect the USB devices.", Toast.LENGTH_LONG).show();
            //finish();
		}

		if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {//|action.equals("android.intent.action.MAIN")) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				    Log.d(TAG, "INTENT DEVICE ATTACHED=" + device.toString());
                    maestroSSC.setDevice(device);
                    deviceConnected = true;
                   Toast.makeText(getApplicationContext(),
                         "USB device connected.", Toast.LENGTH_LONG).show();

			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.d(TAG, "INTENT DEVICE DETACHED=" + device.toString());
              Toast.makeText(getApplicationContext(), 
                          "USB device disconnected.", Toast.LENGTH_LONG).show();
			} else {
				Log.d(TAG, "Unexpected Action=" + action.toString());

			}
		}
		else
		{
            Toast.makeText(getApplicationContext(), 
                    "Invalid USB device or USB device not found.", Toast.LENGTH_LONG).show();
			
		}
	}

    public Context getContext() {
        return getApplicationContext();
    }
    
    protected void onRestart() {
    	super.onRestart();
    	HWSectionFragment.updateOutput();
    }
    
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
    	return control;
    }
    
    /**
     * Sends an intent to start StartActivity.
     */
    public void launchAboutActivity() {
    	Intent intent = new Intent(this, StartActivity.class);
    	startActivity(intent);
    }

    /**
     *  Creates a menu for Audio Chooser
     * @param menu
     * @return true if menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.about){
            launchAboutActivity();
            return true;
        }
        return false;
    }



    
    /**
     * Sets the current page of the mViewPager from a click on an ActionBar tab
     * @param tab
     * @param fragmentTransaction
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    
    /*Functions For Hardware Developer Interface
     * 
     * 
     */
    
    
  //Command select functions
    
    /**
     * This function is called when the user clicks submit on a RoboAppDialogFragment.
     * It sends the command entered in the dialog to the RoboCat and updates the command
     * display.
     * 
     * @param dialog - the DialogFragment the user interacted with.
     */
    public void onCommandSubmitClick(DialogFragment dialog) {
    	boolean success = true;
    	RoboAppDialogFragment d = (RoboAppDialogFragment) dialog;
    	try {
    		control.sendManualCommand(d.getData());
    	} catch (NumberFormatException e) {
    		success = false;
    	}
    	if (success)
    		HWSectionFragment.updateOutput();
    }
    
    /**
     * This function is called when the user clicks cancel on a RoboAppDialogFragment.
     * It currently does not do anything.
     * 
     * @param dialog - the DialogFragment
     */
    public void onCancelClick(DialogFragment dialog) {
    	
    }
    
    /**
     * This function saves the command history to internal storage.
     * 
     * @param v - the view of the widget calling this function
     */
    public void save(View v) {
    	Intent i = new Intent(this,SaveFileChooser.class);
        startActivity(i);
    }
    
    /**
     * This function loads the command history from internal storage and adds it to the display.
     * 
     * @param v - the view of the widget calling this function
     */
    public void load(View v) {
    	Intent i = new Intent(this,LoadFileChooser.class);
        startActivity(i);
    }
    
    public void launchRoboCatActivity(View v) {
    	Intent i = new Intent(this, RoboCatActivity.class);
    	startActivity(i);
    }
    
    /**
     * This function is called when the user presses the send but next to the slider in the Hardware
     * Developer Interface.  This function creates and sends a command with the value specified
     * by the slider
     * @param v - the view of the widget calling this function
     */
    public void sliderSubmit(View v) {
    	SeekBar s = (SeekBar) findViewById(R.id.command_slider);
    	//Command c = new Command();
    	//c.setData(s.getProgress());
    	control.sendManualCommand("0x" + Integer.toHexString(s.getProgress()));
    	HWSectionFragment.updateOutput();
    }
    
    /**
     * This function clears the Command Display and the command history.
     * 
     * @param v - the view of the widget calling this function
     */
    public void resetDisplay(View v) {
    	control.clearCommandDisplay();
    	HWSectionFragment.updateOutput();
//      Examples of audio playback in different section of the app

//      -----From a static context----
//      String [] aud = getResources().getStringArray(R.array.audio_files);
//      try{
//          AudioChooser.Play_audio(v.getContext(),"Scream",aud);
//      }
//      catch (FileNotFoundException f){
//
//      }

//      -----From a non-static context----
//      AudioChooser.Play_audio(v.getContext(),"Scream");

    }
    
    /**
     * Returns to the previous activity
     * @param v
     */
    public void End_User_Exit(View v){
        finish();
    }

    /**
     * Starts up the Multi-Touch Activity from a button click
     * @param view
     */
    public void Start_multi(View view){
        Intent myIntent=new Intent(this,MultiTouchActivity.class);
        startActivity(myIntent);
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    
    public void Start_camera(View view){
        if(checkCameraHardware(getApplicationContext())){
            Intent myIntent=new Intent(this,CameraActivity.class);
            startActivity(myIntent);
        }
        else{
            MakeToast("No camera Available");
        }
    }

    private void MakeToast(String s) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, s, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
         * Starts up the Audio Chooser Activity from a button click
         * @param view
         */
    public void Play_audio_file(View view){
        Intent i = new Intent(this,AudioChooser.class);
        startActivity(i);
    }

    /**
     * Called when recording is started. Will save to the top level of the SD card.
     */
    private static void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        Date d = new Date();
        CharSequence s  = DateFormat.format("hh_mm_ss_aa_MM_d_yyyy", d.getTime());
        String audio_file = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_DIRECTORY + "/RoboApp_recording("+s.toString()+").mp4";
        mRecorder.setOutputFile(audio_file);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        Log.d("Audio","Recording!!!!!");
        try {
            mRecorder.prepare();
            mRecorder.start();

        }
        catch (IOException e) {
            Log.e("Record", "prepare() failed");

        }
    }
    /**
     * Called when recording is to be stopped
     */
    private static void stopRecording() {
        Log.d("Audio","Stopped Recording!!!!!");
        try{
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        catch (Exception e)
        {
            Log.d("Audio","Error in stoprecording");
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.


            Fragment fragment;

            switch (position) {
                case 0:
                    fragment = new ShortcutSectionFragment();
                    break;
                case 1:
                    fragment = new HWSectionFragment();
                    /*Bundle args = new Bundle();
                    String s = "";//fill();
                    args.putString(HWSectionFragment.ARG_COMMAND_STRING,s);
                    fragment.setArguments(args);*/
                    break;
                case 2:
                    fragment = new EndSectionFragment();
                    break;
                case 3:
                    fragment = new MultiSectionFragment();
                    break;
                default:
                    fragment = new HWSectionFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Shortcuts".toUpperCase(l);
                case 1:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     *  This class controls the user interface for the main shortcut interface.
     *  @author Matt King
     *
     */
    public static class ShortcutSectionFragment extends Fragment {

        public ShortcutSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_screen, container, false); //change
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.authorText);      //change
            dummyTextView.setText("Updating of previous app done by: Matthew King, Pinfang Ding, Matthew Hutton, and Liu Chong");
            return rootView;
        }
    }


    /**
     * This class controls the user interface for the hardware developer interface.
     * @author Joey Phelps
     *
     */
    public static class HWSectionFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        View.OnClickListener,
    	SeekBar.OnSeekBarChangeListener{

        private Button homeButton, recordButton, clearGaitButton, runGaitButton;
        //private SeekBar channel1PositionBar, channel2PositionBar, channel3PositionBar, channel4PositionBar, channel5PositionBar, channel6PositionBar,  channel7PositionBar, channel8PositionBar, channel9PositionBar, channel10PositionBar, channel11PositionBar, channel12PositionBar;
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
        int[] channelNoMapArray=new int[]{1,2,3,5,6,7,12,13,14,16,17,18};

        String gaitDefaultFileName = "GaitShared.txt";
        private static View rootView;

        public HWSectionFragment() {
        }
        
        /**
         * This function is called when the command select slider is moved
         * @param seekBar - the seek bar widget
         * @param progress - the position of the seek bar which is a value between 0 and 255
         * @param fromUser - is true if the change was made by the user
         */
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        	TextView t = (TextView) rootView.findViewById(R.id.slider_display);
        	t.setText("0x" + Integer.toHexString(progress));
        }
        
        public void onStartTrackingTouch(SeekBar seekBar) {
        	
        }
        
        public void onStopTrackingTouch(SeekBar seekBar) {
        	
        }
        
        //Command select functions
        /**
         * This function is called when nothing in the command dropdown is selected.
         * @param parent - the AdapterView of the command dropdown.
         */
        public void onNothingSelected(AdapterView<?> parent) {
        }
        
        /**
         * This function is called when an item in the command dropdown is selected.
         * If the Manual command is selected this function opens up a dialog for the user to
         * enter the values to send to the cat.  Other wise it sends the command to the cat
         * and updates the command display.
         * 
         * @param parent - the AdapterView of the command dropdown.
         * @param view - the View of the spinner widget.
         * @param pos - the position in the array of selectable items that was accepted.
         * @param id - the id of the command dropdown
         */
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        	String command = parent.getItemAtPosition(pos).toString();
        	
        	
        	if (command.equals("Manual")) {
        		enterManualCommand();
        	} else if (!command.equals("Select Command")) {
        		control.sendCommand(command);
        		updateOutput();
        	} 
        		
        	parent.setSelection(0);
        	
        }
        
        /**
         * This function updates the command display.
         */
        public static void updateOutput() {
        	TextView t = (TextView) rootView.findViewById(R.id.Commands);
        	t.setText(control.getDisplayableText());
        }
        
        /**
         * This function launches a dialog so that the user can enter values which will
         * then be sent to the RoboCat
         */
        public void enterManualCommand() {
        	DialogFragment popup = new RoboAppDialogFragment();
            popup.show(this.getActivity().getSupportFragmentManager(),"RoboAppDialogFragment");
        }
        

        /**
         * This funciton sets up the command select dropdown updates the output.
         */
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.robocat_layout, container, false); //change
            //mainAct = (MainAct) this.getActivity();


            // initialize the gait array value to -1. declare in the beginning, but have to initialize here
            Arrays.fill(arrayGaitChannelProgress,-1);

            homeButton = (Button) rootView.findViewById(R.id.homeButtonid);
            homeButton.setOnClickListener(this);
            recordButton = (Button) rootView.findViewById(R.id.buttonpololurecordid);
            recordButton.setOnClickListener(this);
            clearGaitButton = (Button) rootView.findViewById(R.id.clearGaitButtonid);
            clearGaitButton.setOnClickListener(this);
            runGaitButton = (Button) rootView.findViewById(R.id.runGaitButtonid);
            runGaitButton.setOnClickListener(this);


            // Initialize the textView and seekBar
            /*for(int i = 0; i < channelCount; i++) {
                textViewChannelPositionArray[i] = new TextView(this);
                channelPositionBarArray[i] = new SeekBar(this);
            }*/


            channelPositionBarArray[0] = (SeekBar) rootView.findViewById(R.id.channel1PositionBar);
            //channel1PositionBar = (SeekBar) rootView.findViewById(R.id.channel1PositionBar);
            //channel1PositionBar.setOnSeekBarChangeListener(this);
            channelPositionBarArray[1] = (SeekBar) rootView.findViewById(R.id.channel2PositionBar);
            channelPositionBarArray[2] = (SeekBar) rootView.findViewById(R.id.channel3PositionBar);
            channelPositionBarArray[3] = (SeekBar) rootView.findViewById(R.id.channel4PositionBar);
            channelPositionBarArray[4] = (SeekBar) rootView.findViewById(R.id.channel5PositionBar);
            channelPositionBarArray[5] = (SeekBar) rootView.findViewById(R.id.channel6PositionBar);
            channelPositionBarArray[6] = (SeekBar) rootView.findViewById(R.id.channel7PositionBar);
            channelPositionBarArray[7] = (SeekBar) rootView.findViewById(R.id.channel8PositionBar);
            channelPositionBarArray[8] = (SeekBar) rootView.findViewById(R.id.channel9PositionBar);
            channelPositionBarArray[9] = (SeekBar) rootView.findViewById(R.id.channel10PositionBar);
            channelPositionBarArray[10] = (SeekBar) rootView.findViewById(R.id.channel11PositionBar);
            channelPositionBarArray[11] = (SeekBar) rootView.findViewById(R.id.channel12PositionBar);
            // set listeners for channelPositionBar
            for(int i = 0; i < channelCount; i++) {
                channelPositionBarArray[i].setOnSeekBarChangeListener(this);
            }

            //errorsTextView = (TextView) rootView.findViewById(R.id.errorsTextView);


            textViewChannelPositionArray[0] = (TextView) rootView.findViewById (R.id.channel1positionvalue);
            textViewChannelPositionArray[1] = (TextView) rootView.findViewById (R.id.channel2positionvalue);
            textViewChannelPositionArray[2] = (TextView) rootView.findViewById (R.id.channel3positionvalue);
            textViewChannelPositionArray[3] = (TextView) rootView.findViewById (R.id.channel4positionvalue);
            textViewChannelPositionArray[4] = (TextView) rootView.findViewById (R.id.channel5positionvalue);
            textViewChannelPositionArray[5] = (TextView) rootView.findViewById (R.id.channel6positionvalue);
            textViewChannelPositionArray[6] = (TextView) rootView.findViewById (R.id.channel7positionvalue);
            textViewChannelPositionArray[7] = (TextView) rootView.findViewById (R.id.channel8positionvalue);
            textViewChannelPositionArray[8] = (TextView) rootView.findViewById (R.id.channel9positionvalue);
            textViewChannelPositionArray[9] = (TextView) rootView.findViewById (R.id.channel10positionvalue);
            textViewChannelPositionArray[10] = (TextView) rootView.findViewById (R.id.channel11positionvalue);
            textViewChannelPositionArray[11] = (TextView) rootView.findViewById (R.id.channel12positionvalue);



            UsbManager manager = (UsbManager) rootView.getContext().getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            maestroSSC = new PololuMaestroUSBCommandProcess(manager);


            updateOutput();
            
            return rootView;
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


        public void clearGaitButton()
        {

            // test: delete the Gait.txt file

            File root = new File(Environment.getExternalStorageDirectory(), "Gait");
            if (!root.exists()) {
                root.mkdirs();
            }
            //File gpxfile = new File(root, "Gait"+currentDateandTime+".txt");
            File gpxfile = new File(root, gaitDefaultFileName);

            boolean deleted = gpxfile.delete();


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
                        File root = new File(Environment.getExternalStorageDirectory(), "Gait");
                        if (!root.exists())
                        {
                            root.mkdirs();
                        }
                        File gpxfile = new File(root, gaitDefaultFileName);

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
                }
            };

            Handler h = new Handler();
            h.postDelayed(r, 1000); // <-- the "1000" is the delay time in miliseconds.
//////////////////////


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


        private void progressChangeAction(int channelNoMapped, int progressActual, int channelNoSeekBar)
        {
            if (deviceConnected)
                maestroSSC.setServoPosition(channelNoMapped, progressActual);

            // modify the offset for the progress of the seek bar
            int progressSeekBar = progressActual - progressOffset;
            channelPositionBarArray[channelNoSeekBar].setProgress(progressSeekBar);
            textViewChannelPositionArray[channelNoSeekBar].setText(String.valueOf(progressActual));
        }

        private int[] parseGait(String line) {
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
            generateGaitOnSD(gaitDefaultFileName, arrayGaitChannelProgress);

        }

        public void generateGaitOnSD(String sFileName, int[] arrayGaitChannelProgress){
            try
            {
                File root = new File(Environment.getExternalStorageDirectory(), "Gait");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File gpxfile = new File(root, sFileName);
                //  FileWriter writer = new FileWriter(gpxfile);
                //    writer.append(Integer.toString(gaitArray[1]));
                //      writer.flush();
                //        writer.close();
                Toast.makeText(rootView.getContext(), "Saved", Toast.LENGTH_SHORT).show();
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
    }



    /**
     * The fragment that contains functions for the End User interface
     */
    public static class EndSectionFragment extends Fragment {

        public EndSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_end_layout, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.END_Text);
            dummyTextView.setText("");//Camera video displayed here");

            Switch s = (Switch) rootView.findViewById(R.id.RecordAudioSwitch);

            CompoundButton.OnCheckedChangeListener Rec = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        startRecording();
                    } else {
                        stopRecording();
                    }
                }
            };
            try{
                s.setOnCheckedChangeListener(Rec);
            }
            catch (Exception e){
                Log.d("OnCheckListener","Error to set");
            }
            return rootView;
        }
    }

    /**
     * The fragment that contains functions for the Multi-Touch interface
     */
    public static class MultiSectionFragment extends Fragment {

        public MultiSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_multi_layout, container, false); //change
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);      //change
            dummyTextView.setText("Multi touch interface here");     //change
            return rootView;
        }
    }

}
