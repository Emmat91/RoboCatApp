package com.example.ding.touchforcat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class RecordTouch extends Activity implements View.OnClickListener {

    //declare two buttons for 2 types of movements
    private Button back_btn, restore_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_touch);


        //set two buttons' variables with xml files

        back_btn = (Button)findViewById(R.id.btn_back);
        back_btn.setOnClickListener(this);



        restore_btn = (Button)findViewById(R.id.btn_restore);
        restore_btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == back_btn) {
            finish();
        //    setContentView(R.layout.activity_main_touch);
        //    startActivity(new Intent("android.intent.action.MAIN"));
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