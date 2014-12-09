package com.robocat.roboappui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    
    private String SelectedText="";

    /** Variable to hold the selected audio file name */
    private TextView SelectedTextView;
    
    private TextView PastTextView;

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
        SelectedTextView = (TextView) findViewById(R.id.SelectedText);
        SelectedTextView.setText("");
        mainListView = (ListView) findViewById( android.R.id.list );
        String[] Audio_files;
        final ArrayList<String> AudioList = new ArrayList<String>();

        back.setOnClickListener(BackButton);
        load.setOnClickListener(LoadButton);

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
        getMenuInflater().inflate(R.menu.audio_chooser, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.Delete){
            return DelSelect();
        }
        if(item.getItemId()==R.id.about){
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            return true;
        }
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
    
    public boolean DelSelect(){
        String value = SelectedText;
        return DeleteFile(value);
    }

    private boolean DeleteFile(String value) {
        for(String s :getResources().getStringArray(R.array.audio_files)){
            if(s.equals(value)){
                makeToast("Can't Delete " + value);
                return false;
            }
        }

        String Sel = getResources().getString(R.string.Select);
        if(value.equals(Sel) || value.equals("")){
            makeToast("No file selected!");
            return false;
        }
        
        File path = new File(Environment.getExternalStorageDirectory(), FileIO.EXTERNAL_STORAGE_DIRECTORY);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path, FileIO.addExtension(value, FileIO.ROBOCATMESSAGE_EXTENSION));
        if(file.delete()){
            makeToast(value + " was deleted!");
            listAdapter.remove(value);
            SelectedText = getResources().getString(R.string.Select);
            //SelectedTextView.setText(SelectedText);
            return true;
        }
        else {
            makeToast("delete failed with file: " + value);
            return false;
        }
    }

    /**
     * OnClickListener for the load button.
     * Makes a toast of the selected audio file
     */
    View.OnClickListener LoadButton = new View.OnClickListener() {
        public void onClick(View v) {
            String sel = SelectedText;
            makeToast("Running: "+sel);
            try {
				Control.execute(FileIO.addExtension(sel, FileIO.ROBOCATMESSAGE_EXTENSION));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
