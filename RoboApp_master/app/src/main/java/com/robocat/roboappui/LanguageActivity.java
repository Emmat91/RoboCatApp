package com.robocat.roboappui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

/**
 * Created by Ding on 02/16/2015.
 */
public class LanguageActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        Button zh_btn = (Button)findViewById(R.id.zh);
        zh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Locale locale = new Locale("zh_CN");
                Locale.setDefault(locale);

                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                onConfigurationChanged(config);
                restartActivity();
                }
            });
        Button en_btn = (Button)findViewById(R.id.en);
        en_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Locale locale_en = new Locale("en_US");
                Locale.setDefault(locale_en);
                Configuration config = new Configuration();
                config.locale = locale_en;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                onConfigurationChanged(config);
                restartActivity();
            }
        });
    }
    //@Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        // refresh your views here
//        super.onConfigurationChanged(newConfig);
//        setContentView(R.menu.main);
//    }
    private void restartActivity() {
        //Intent intent = getIntent();
        finish();
    }

    public void start(View view) {finish();}
}
