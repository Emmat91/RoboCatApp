package com.example.ding.touchforcat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Ding on 10/21/2014.
 */
public class FingerTouch extends Activity implements View.OnClickListener{

    private Button back_btn;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_touch);

        back_btn = (Button)findViewById(R.id.button);
        back_btn.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(v == back_btn){
            finish();
        }
    }
}
