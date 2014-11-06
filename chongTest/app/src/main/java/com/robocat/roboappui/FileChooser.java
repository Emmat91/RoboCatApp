package com.robocat.roboappui;

import java.util.ArrayList;
import java.util.Arrays;

import com.robocat.roboappui.commands.Control;
import com.robocat.roboappui.commands.FileIO;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileChooser extends Activity {
	/** Variable to represent the audio file list */
    private ListView mainListView ;

    /** Variable to be able to pass a string array to the ListView */
    private ArrayAdapter<String> listAdapter ;

    private String SelectedText;

    /** Variable to hold the selected audio file name */
    private TextView SelectedTextView;
    
    private TextView PastTextView;
    
    private String [] Audio, Command;

    /**
     * Creating the instance for the FileChooser
     * Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_select_activity);

        Button back = (Button) findViewById(R.id.FileSelectExit);
        Button select = (Button) findViewById(R.id.FileSelect);
        SelectedTextView = (TextView) findViewById(R.id.SelectedText);
        SelectedTextView.setText("");
        mainListView = (ListView) findViewById( android.R.id.list );
        String[] Audio_files,Audio_files_ext,bothAudioFiles,commandFiles;
        final ArrayList<String> AudioList = new ArrayList<String>();

        back.setOnClickListener(BackButton);
        select.setOnClickListener(SelectButton);

        try{
            Audio_files = getResources().getStringArray(R.array.audio_files);
            Audio_files_ext = FileIO.getFilesNamesInDirectory(AudioChooser.AUDIO_DIRECTORY);
            bothAudioFiles = AudioChooser.ConCat(Audio_files,Audio_files_ext);
            commandFiles = FileIO.getFileNamesInDirectoryByExtension(FileIO.ROBOCATMESSAGE_EXTENSION);
            for (int i = 0; i < commandFiles.length; i++) {
            	commandFiles[i] = FileIO.removeExtension(commandFiles[i], FileIO.ROBOCATMESSAGE_EXTENSION);
            }
            Audio_files_ext = AudioChooser.ConCat(bothAudioFiles, commandFiles);
            Audio = bothAudioFiles;
            Command = commandFiles;
            AudioList.addAll(Arrays.asList(Audio_files_ext));

            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, AudioList);

            mainListView.setAdapter(listAdapter);

        }
        catch (Exception e){
            try{
                String mes = e.getMessage();
                Log.d("File Chooser",mes);
            }
            catch (Exception ex){
                Log.d("File Chooser","couldn't get message");
            }

        }


        /**
         * setOnItemClickListener to get the title of the audio
         * file that was selected from the list
         */
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
            	try{
                    //Get to stay highlighted
                    TextView t = (TextView) view, past = PastTextView;
                    String selectedValue="";

                    selectedValue = AudioList.get(position);

                    if(selectedValue.equals(SelectedText))
                        ;
                    else
                    {
                        //reset past selected
                        try{
                            past.setBackgroundColor(getResources().getColor(R.color.white));
                        }
                        catch (Exception e){
                            Log.e("Coloring Past","Failed to color");
                        }
                        SelectedText = selectedValue;
                        //SelectedTextView.setText(SelectedText);
                        t.setBackgroundColor(getResources().getColor(R.color.light_blue));
                        PastTextView = t;
                    }

                }
                catch (Exception e){
                    Log.e("Selecting Audio",e.getMessage());
                }
            }
        });


    }

    /**
     *  Creates a menu for Audio Chooser
     * @param menu
     * @return true if menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.audio_chooser, menu);
        return true;
    }


    /**
     * OnClickListener for the back button.
     * Returns to the previous activity
     */
    View.OnClickListener BackButton = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * OnClickListener for the load button.
     * Makes a toast of the selected audio file
     */
    View.OnClickListener SelectButton = new View.OnClickListener() {
        public void onClick(View v) {
            String sel = SelectedText;
            makeToast("Selected: "+sel);
            boolean inCommand = false;
            for (String s : Command) {
            	if (s == sel) {
            		inCommand = true;
            	}
            }
            if (inCommand) {
            	sel = FileIO.addExtension(sel, FileIO.ROBOCATMESSAGE_EXTENSION);
            }
            Control.mapTouchToFile(null, sel);
            finish();
        }
    };

    /**
     * Displays a toast with the value passed in s
     * @param s String value to display in the toast
     */
    public void makeToast(String s){
        Context context = getApplicationContext();
        int duration = s.length();

        Toast toast = Toast.makeText(context, s, duration);
        toast.show();
    }
}
