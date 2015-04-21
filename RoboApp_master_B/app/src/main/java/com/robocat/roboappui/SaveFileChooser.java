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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import com.robocat.roboappui.commands.Control;
import com.robocat.roboappui.commands.FileIO;
import com.robocat.roboappui.R;

/**
 * Audio Chooser is an activity that displays all audio files which can select to play.
 * This list lives in R.array.audio_files
 * @author Pinfang Ding
 * @version  %I%, %G%
 */
public class SaveFileChooser extends Activity {

    /** Variable to represent the list of all audio files */
    private ListView mainListView ;

    /** Variable to be able to pass a string array to the ListView */
    private ArrayAdapter<String> listAdapter ;

    /** Variable to hold the selected audio file name */
    //private TextView SelectedText;

    private EditText filename;

    /**
     * Creating the instance for the AudioChooser
     * Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_file_activity);

        Button back = (Button) findViewById(R.id.SaveFileExit);
        Button save = (Button) findViewById(R.id.SaveFileSelect);
        //SelectedText = (TextView) findViewById(R.id.SelectedText);
        mainListView = (ListView) findViewById( android.R.id.list );
        filename = (EditText) findViewById(R.id.save_filename);
        String[] Audio_files;
        final ArrayList<String> AudioList = new ArrayList<String>();

        back.setOnClickListener(BackButton);
        save.setOnClickListener(SaveButton);

        try{
            Audio_files = FileIO.getFileNamesInDirectoryByExtension(FileIO.ROBOCATMESSAGE_EXTENSION);
            for (int i = 0; i < Audio_files.length; i++) {
            	Audio_files[i] = FileIO.removeExtension(Audio_files[i], FileIO.ROBOCATMESSAGE_EXTENSION);
            }
            AudioList.addAll(Arrays.asList(Audio_files));

            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, AudioList);

            mainListView.setAdapter(listAdapter);

        }
        catch (Exception e){
            try{
                String mes = e.getMessage();
                Log.d("Save File Chooser",mes);
            }
            catch (Exception ex){
                Log.d("Save File Chooser","couldn't get message");
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
                    //SelectedText.setText(selectedValue);
                    filename.setText(selectedValue);
                }
                catch (Exception e){

                }
            }
        });


    }



    /**
     * OnClickListener for the back button.
     * Returns to the previous activity
     * @see android.view.View.OnClickListener
     */
    View.OnClickListener BackButton = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * OnClickListener for the play button.
     * Makes a toast of the selected audio file
     * @param sel String value of the filename
     */
    View.OnClickListener SaveButton = new View.OnClickListener() {
        public void onClick(View v) {
            String sel = filename.getText().toString();
            if(sel.equals(""))
            {
                makeToast("Enter a name");
                return;
            }
            try {
            	Control.saveGaitFile(FileIO.addExtension(sel, FileIO.ROBOCATMESSAGE_EXTENSION));
            } catch (Exception e) {
            	makeToast("File Copy Failed");
            	return;
            }
            makeToast("Selected: "+sel);
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
