package com.robocat.roboappui;

import com.robocat.roboappui.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_start);
	}
	
	public void start(View view) {
		finish();
	}

}
