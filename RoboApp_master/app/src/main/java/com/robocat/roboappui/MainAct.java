package com.robocat.roboappui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.analytics.GoogleAnalytics;
import com.robocat.roboappui.MultiTouchActivity.TypeOfAction;
import com.robocat.roboappui.commands.Control;
import com.robocat.roboappui.commands.FileIO;
import com.robocat.roboappui.dialog.RoboAppDialogFragment;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


//   https://code.google.com/p/android-file-dialog/
//   https://github.com/mburman/Android-File-Explore

/**
 * Class MainAct is the root activity that handles switching between fragments
 */
public class MainAct extends FragmentActivity implements ActionBar.TabListener, 
RoboAppDialogFragment.RoboAppDialogListener
{
    private static final String TAG = "MaestroSSCActivity";

    private static int numBars = 3;

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

    private static SeekBar seekBar[] = new SeekBar[numBars];
    private static TextView textView[] = new TextView[numBars];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String Language = preferences.getString("Lang","no_find");
        Locale mylocale = new Locale(Language);
        if(Language == "no_find")
            mylocale = Resources.getSystem().getConfiguration().locale;
        Resources res1 = getResources();
        Configuration conf = res1.getConfiguration();
        conf.locale = mylocale;
        DisplayMetrics dm = res1.getDisplayMetrics();
        res1.updateConfiguration(conf,dm);
        Locale.setDefault(mylocale);

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
        	control = new Control();
        	Control.initialize(commands);
        }
        
//        launchAboutActivity();
    }

    @Override
    /**
     *  This function checks for which intent started the app and checks whether 
     *  a usb device is attached or not
     */
    public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		Log.d(TAG, "onResume(" + intent + ")");
		String action = intent.getAction();
		deviceConnected(false);
		if (action.equals("android.intent.action.MAIN")) {
            //Toast.makeText(getApplicationContext(), 
                   // "Reconnect the USB devices.", Toast.LENGTH_LONG).show();
            //finish();
		}

		if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")|action.equals("android.intent.action.MAIN")) {
			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				Log.d(TAG, "INTENT DEVICE ATTACHED=" + device.toString());
				RoboCatActivity.device = device;
              deviceConnected(true);

			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.d(TAG, "INTENT DEVICE DETACHED=" + device.toString());
				RoboCatActivity.device = device;
              deviceConnected(false);
			} else {
				Log.d(TAG, "Unexpected Action=" + action.toString());

			}
		}
		else
		{
            //Toast.makeText(getApplicationContext(), 
                    //"Invalid USB device or USB device not found.", Toast.LENGTH_LONG).show();
			
		}
        Log.d("About to initialize", "Initializing " + RoboCatActivity.deviceConnected);
		RoboCatActivity.initializeMaestro((UsbManager) getSystemService(USB_SERVICE));

	}
    
    private void deviceConnected(boolean connected) {
    	RoboCatActivity.deviceConnected = connected;
    	TextView v = (TextView) findViewById(R.id.deviceAttached);
    	if (v == null)
    		return;
    	if (connected)
    		v.setText(getApplicationContext().getString(R.string.connectedMessage));
    	else
    		v.setText(getApplicationContext().getString(R.string.errorMessage));
    		
    }
    
    @Override
    /**
     * This function is used to save the state of the control class
     * when the user leaves the Main Activity
     * @return the Control Class
     */
    public Object onRetainCustomNonConfigurationInstance() {
    	return control;
    }
    
    /**
     * Sends an intent to start StartActivity.
     */
    public void launchLanguageActivity() {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivity(intent);
    }
    public void launchAboutActivity() {
    	Intent intent = new Intent(this, StartActivity.class);
    	startActivity(intent);
    }
    public void launchHelpActivity() {
        Intent intent_help = new Intent(this, HelpActivity.class);
        startActivity(intent_help);
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
        if(item.getItemId()==R.id.language){
            launchLanguageActivity();
            return true;
        }
        else if(item.getItemId()==R.id.about){
            launchAboutActivity();
            return true;
        }
        else if(item.getItemId()==R.id.help){
            launchHelpActivity();
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
    		Control.sendManualCommand(d.getData());
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
    
    /**
     * This function opens the servo control page
     * @param v
     */
    public void launchRoboCatActivity(View v) {
    	Intent i = new Intent(this, RoboCatActivity.class);
    	startActivity(i);
    }
    
    /**
     * This function tells the Control class which multi-touch action 
     * the selected file will be mapped to will be mapped
     * @param v - the text view of the action to be mapped
     */
    public void selectFileToBeMapped(View v) {
    	TextView t = (TextView) v;
    	if (t.getId() == R.id.one_down_select) {
    		Control.actionToBeMapped = TypeOfAction.OneDown;
    	} else if (t.getId() == R.id.one_left_select) {
    		Control.actionToBeMapped = TypeOfAction.OneLeft;
    	} else if (t.getId() == R.id.one_right_select) {
    		Control.actionToBeMapped = TypeOfAction.OneRight;
    	} else if (t.getId() == R.id.one_up_select) {
    		Control.actionToBeMapped = TypeOfAction.OneUp;
    	} else if (t.getId() == R.id.two_up_select) {
            Control.actionToBeMapped = TypeOfAction.TwoUp;
        } else if (t.getId() == R.id.two_down_select) {
            Control.actionToBeMapped = TypeOfAction.TwoDown;
        } else if (t.getId() == R.id.pinch_select) {
            Control.actionToBeMapped = TypeOfAction.TwoPinch;
        } else if (t.getId() == R.id.expand_select) {
            Control.actionToBeMapped = TypeOfAction.TwoExpand;
        } else {
    		return;
    	}
    	Intent i = new Intent(this, FileChooser.class);
    	startActivity(i);
    }
    
    
    
    /**
     * This function clears the Command Display and the command history.
     * 
     * @param v - the view of the widget calling this function
     */
    public void resetDisplay(View v) {
    	RoboCatActivity.clearGaitButton();
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
                    fragment = new HWSectionFragment();
                    /*Bundle args = new Bundle();
                    String s = "";//fill();
                    args.putString(HWSectionFragment.ARG_COMMAND_STRING,s);
                    fragment.setArguments(args);*/
                    break;
                case 1:
                    fragment = new EndSectionFragment();
                    break;
                case 2:
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
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
    

    /**
     * This class controls the user interface for the hardware developer interface.
     * @author Joey Phelps
     *
     */
    public static class HWSectionFragment extends Fragment implements AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {
        
        private static View rootView;

        public HWSectionFragment() {
        }
        
        /**
         * This function is called when the command select slider is moved
         */
         
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getMax() == 9) {
                textView[0].setText(rootView.getContext().getResources().getString(R.string.iterationText) + " " + Integer.toString(progress + 1));
                RoboCatActivity.iterations = progress + 1;
            }
            else if (seekBar.getMax() == 39) {
                double time = (progress+1) / 10.0;
                textView[1].setText(rootView.getContext().getResources().getString(R.string.timeText) + " " + String.format("%.1f", time));
                RoboCatActivity.time = progress + 1;
            }
            else {
                RoboCatActivity.acceleration = progress + 1;
                textView[2].setText(rootView.getContext().getResources().getString(R.string.accelerationText) + " " + Integer.toString(progress + 1));
            }

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
        		Control.sendCommand(command);
        		updateOutput();
        	} 
        		
        	parent.setSelection(0);
        	
        }
        
        /**
         * This function updates the command display.
         */
        public static void updateOutput() {
            try{
                TextView t = (TextView) rootView.findViewById(R.id.Commands);
                t.setText(Control.getDisplayableText(rootView.getContext()));
            }
            catch (Exception e){
                Log.e(TAG,"Exception caught in updateOutput");
            }
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
            rootView = inflater.inflate(R.layout.fragment_hw_layout, container, false); //change


            //mainAct = (MainAct) this.getActivity();
            
            /*Spinner commandSelect = (Spinner) rootView.findViewById(R.id.command_select);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity().getBaseContext(),
            		R.array.command_select_options, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            
            commandSelect.setAdapter(adapter);
            commandSelect.setOnItemSelectedListener(this);
            
            SeekBar commandSlider = (SeekBar) rootView.findViewById(R.id.command_slider);
            commandSlider.setMax(255);
            commandSlider.setOnSeekBarChangeListener(this);*/


            for (int i = 0; i < numBars; i++) {
                seekBar[i] = new SeekBar(rootView.getContext());
                textView[i] = new TextView(rootView.getContext());
            }

            seekBar[0] = (SeekBar) rootView.findViewById(R.id.iterations);
            seekBar[1] = (SeekBar) rootView.findViewById(R.id.time);
            seekBar[2] = (SeekBar) rootView.findViewById(R.id.acceleration);

            textView[0] = (TextView) rootView.findViewById(R.id.iterationText);
            textView[1] = (TextView) rootView.findViewById(R.id.timeText);
            textView[2] = (TextView) rootView.findViewById(R.id.accelerationText);

            textView[0].setText(rootView.getContext().getResources().getString(R.string.iterationText) + " 1");
            textView[1].setText(rootView.getContext().getResources().getString(R.string.timeText) + " 0.5");
            textView[2].setText(rootView.getContext().getResources().getString(R.string.accelerationText) + " 3");

            for (int i = 0; i < numBars; i++) {
                seekBar[i].setOnSeekBarChangeListener(this);
            }


            updateOutput();
            
            return rootView;
        }
        
        public void onResume() {
        	super.onResume();
        	updateOutput();
        	TextView v = (TextView) rootView.findViewById(R.id.deviceAttached);
        	if (v == null)
        		return;
        	if (RoboCatActivity.deviceConnected)
        		v.setText(rootView.getContext().getString(R.string.errorMessage));
        	else
        		v.setText(rootView.getContext().getString(R.string.errorMessage));
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
    	private static View rootView;

        public MultiSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_multi_layout, container, false);
            return rootView;
        }
        
        public void onResume() {
        	super.onResume();
        	updateFileNames();

        }
        
        /**
         * Updates the text fields that display which file is mapped to each action
         */
        public void updateFileNames() {
        	TextView t = (TextView) rootView.findViewById(R.id.one_down_file);
        	String str = Control.getFileMappedToTouch(TypeOfAction.OneDown);
        	updateTextView(t, str);
        	t = (TextView) rootView.findViewById(R.id.one_left_file);
        	str = Control.getFileMappedToTouch(TypeOfAction.OneLeft);
        	updateTextView(t, str);
        	t = (TextView) rootView.findViewById(R.id.one_right_file);
        	str = Control.getFileMappedToTouch(TypeOfAction.OneRight);
        	updateTextView(t, str);
        	t = (TextView) rootView.findViewById(R.id.one_up_file);
            str = Control.getFileMappedToTouch(TypeOfAction.OneUp);
            updateTextView(t, str);
            t = (TextView) rootView.findViewById(R.id.two_up_file);
            str = Control.getFileMappedToTouch(TypeOfAction.TwoUp);
            updateTextView(t, str);
            t = (TextView) rootView.findViewById(R.id.two_down_file);
            str = Control.getFileMappedToTouch(TypeOfAction.TwoDown);
            updateTextView(t, str);
            t = (TextView) rootView.findViewById(R.id.pinch_file);
            str = Control.getFileMappedToTouch(TypeOfAction.TwoPinch);
            updateTextView(t, str);
            t = (TextView) rootView.findViewById(R.id.expand_file);
            str = Control.getFileMappedToTouch(TypeOfAction.TwoExpand);
            updateTextView(t, str);
        }
        
        /**
         * This function is used by the updateFileNames() function
         * @param t - the TextView to update
         * @param str - the String to set the text to
         * @see updateFileNames()
         */
        private static void updateTextView(TextView t, String str) {
        	if (str == null) {
        		t.setText(Control.NONE_SELECTED);
        	} else {
        		t.setText(FileIO.removeExtension(str, FileIO.ROBOCATMESSAGE_EXTENSION));
        	}
        }
    }

}
