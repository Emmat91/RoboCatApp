package com.robocat.roboappui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Ding on 02/05/2015.
 */
public class HelpActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);
    }

    public void start(View view) {
        finish();
    }
}
