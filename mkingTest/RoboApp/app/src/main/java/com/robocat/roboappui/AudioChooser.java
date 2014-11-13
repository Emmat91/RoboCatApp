package com.robocat.roboappui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
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

import com.robocat.roboappui.commands.FileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Audio Chooser is an activity that displays a list of
 * audio files to choose from to play. This list lives in R.array.audio_files
 * @author Tim Grannen
 */
public class AudioChooser extends Activity {

    /** Variable to represent the audio file list */
    private ListView mainListView ;

    /** Variable to be able to pass a string array to the ListView */
    private ArrayAdapter<String> listAdapter ;

    /** String to hold the selected audio file name */
    private String SelectedText;

    /** TextView to hold the selected audio file name */
    private TextView SelectedTextView;

    /** TextView to hold the selected audio file name */
    private TextView PastTextView;


    public static final String AUDIO_DIRECTORY = "/Android/data/com.robocat.roboapp/audio/";


    /**
     * Creating the instance for the AudioChooser
     * Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_activity);
        Button back = (Button) findViewById(R.id.AudioExit);
        Button play = (Button) findViewById(R.id.AudioSelect);
        SelectedTextView = (TextView) findViewById(R.id.SelectedText);
        SelectedTextView.setText("");
        SelectedText = "";
        mainListView = (ListView) findViewById( android.R.id.list );
        String[] Audio_files,Audio_files_ext,both;
        final ArrayList<String> AudioList = new ArrayList<String>();

        back.setOnClickListener(BackButton);
        play.setOnClickListener(PlayButton);

        try{
            Audio_files = getResources().getStringArray(R.array.audio_files);
            Audio_files_ext = FileIO.getFilesNamesInDirectory(AUDIO_DIRECTORY);
            both = ConCat(Audio_files,Audio_files_ext);
            AudioList.addAll(Arrays.asList(both));
            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, AudioList);
            mainListView.setAdapter(listAdapter);
        }
        catch (Exception e){
            try{
                String mes = e.getMessage();
                Log.d("Audio Chooser",mes);
            }
            catch (Exception ex){
                Log.d("Audio Chooser","couldn't get message");
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

//        Uncomment to enable long press on items to delete.

//        mainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
//                boolean test = false;
//                try {
//                    String LongValue = AudioList.get(position);
//                    test = DeleteFile(LongValue);
//                    SelectedText.setText(R.string.Select);
//                    return test;
//                } catch (Exception e) {
//                    return false;
//                }
//            }
//        });

    }

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
        if(value.equals(Sel)){
            makeToast("No file selected!");
            return false;
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_DIRECTORY + value);
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

    /**
     * OnClickListener for the play button.
     * Makes a toast of the selected audio file
     */
    View.OnClickListener PlayButton = new View.OnClickListener() {
        public void onClick(View v) {
            Play_audio(v);
        }
    };



    public void Play_audio(View v){
        Context context = v.getContext();
        String AudioFile = SelectedText;
        Play_audio(context, AudioFile);
    }

    public void Play_audio(Context c, String s){
        String [] aud = getResources().getStringArray(R.array.audio_files);
        try{
            Play_audio(c,s,aud);
        }
        catch (FileNotFoundException f){
            makeToast(f.getMessage());
        }
    }


    public static void Play_audio(Context c, String s, String [] aud) throws FileNotFoundException{
        int AFile = FindStandardAudioFile(s, aud);
        if(AFile>0){
            MediaPlayer mp = MediaPlayer.create(c,AFile);
            mp.start();
        }
        else{
            //playing external recorded audio
            Play_external(c,s);
        }
    }

    private static void Play_external(Context c,String s) throws FileNotFoundException{
        try{
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_DIRECTORY + s;
            Log.d("External",path);
            File f = new File(path);
            Uri uri = Uri.fromFile(f);
            MediaPlayer mp = MediaPlayer.create(c,uri);
            mp.start();
        }
        catch (Exception e){
            Log.e("Couldn't play External",s);
            throw new FileNotFoundException("Couldn't play external file: "+s);
        }
    }

    private static int FindStandardAudioFile(String file,String []aud) {
        int ret = 0;
        try{
            if(file.equals(aud[0]))
                ret = R.raw.cat_meow_2_cat_stevens_2034822903;
            else if(file.equals(aud[1]))
                ret = R.raw.cat_scream_soundbible;
            else if(file.equals(aud[2]))
                ret = R.raw.roaring_lion_soundbible;
            else{
                ret=-1;
            }
        }
        catch (Exception e){
            ret=-2;
        }
        return ret;
    }


    public static String [] ConCat(String[] One, String[] Two){
        String [] res = new String[One.length+Two.length];
        System.arraycopy(One,0,res,0,One.length);
        System.arraycopy(Two,0,res,One.length,Two.length);
        return res;
    }

    /**
     * Displays a toast with the value passed in s
     * @param s String value to display in the toast
     */
    public void makeToast(String s){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, s, Toast.LENGTH_LONG);
        toast.show();
    }
}
