package com.example.ding.touchforcat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainTouch extends Activity implements View.OnClickListener {

    //declare two buttons for 2 types of movements
    private Button re_move_btn, fin_move_btn, back_btn, restore_btn;

    private String message;

    //Constructor
    //@param message to be printed
    public int add(int x,int y){
        return x+y;
    }//for Junit Test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_touch);


        //set two buttons' variables with xml files
        re_move_btn = (Button)findViewById(R.id.button1);
        re_move_btn.setOnClickListener(this);
        fin_move_btn = (Button)findViewById(R.id.button2);
        fin_move_btn.setOnClickListener(this);

    }

    public void onClick(View v) {
        if(v == re_move_btn) {
            //go to activity_record_touch.xml
            //setContentView(R.layout.activity_record_touch);
            startActivity(new Intent("com.example.ding.RECORDTOUCH"));
        }
        else if(v == fin_move_btn) {
            //go activity_finger_touch.xml
            //setContentView(R.layout.activity_finger_touch);
            startActivity(new Intent("com.example.ding.FINGERTOUCH"));
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_touch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}