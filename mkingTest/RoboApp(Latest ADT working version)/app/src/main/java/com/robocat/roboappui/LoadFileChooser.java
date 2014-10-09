package com.robocat.roboappui;

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

import java.util.ArrayList;
import java.util.Arrays;

import com.robocat.roboappui.commands.Control;
import com.robocat.roboappui.commands.FileIO;
import com.robocat.roboappui.R;

/**
 * Audio Chooser is an activity that displays a list of
 * audio files to choose from to play. This list lives in R.array.audio_files
 * @author Tim Grannen
 * @author Joey Phelps
 */
public class LoadFileChooser extends Activity {

    /** Variable to represent the audio file list */
    private ListView mainListView ;

    /** Variable to be able to pass a string array to the ListView */
    private ArrayAdapter<String> listAdapter ;

    /** Variable to hold the selected audio file name */
    private TextView SelectedText;

    /**
     * Creating the instance for the AudioChooser
     * Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_file_activity);

        Button back = (Button) findViewById(R.id.LoadFileExit);
        Button load = (Button) findViewById(R.id.LoadFileSelect);
        SelectedText = (TextView) findViewById(R.id.SelectedText);
        mainListView = (ListView) findViewById( android.R.id.list );
        String[] Audio_files;
        final ArrayList<String> AudioList = new ArrayList<String>();

        back.setOnClickListener(BackButton);
        load.setOnClickListener(LoadButton);

        try{
            Audio_files = FileIO.getFilesNamesInDirectory();
            AudioList.addAll(Arrays.asList(Audio_files));

            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, AudioList);

            mainListView.setAdapter(listAdapter);

        }
        catch (Exception e){
            try{
                String mes = e.getMessage();
                Log.d("Load File Chooser",mes);
            }
            catch (Exception ex){
                Log.d("Load File Chooser","couldn't get message");
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
                    String selectedValue = AudioList.get(position);
                    SelectedText.setText(selectedValue);
                }
                catch (Exception e){

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
        getMenuInflater().inflate(R.menu.audio_chooser, menu);
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
    View.OnClickListener LoadButton = new View.OnClickListener() {
        @SuppressWarnings("static-access")
		public void onClick(View v) {
            String sel = (String) SelectedText.getText();
            makeToast("Selected: "+sel);
            (new Control()).loadCommandDisplay(sel);
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
