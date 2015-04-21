package com.robocat.roboappui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.acl.LastOwnerException;
import java.util.Locale;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Ding on 02/16/2015.
 */
public class LanguageActivity extends Activity{

    public void setLocale(String lang) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Lang",lang);
        editor.apply();

        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        DisplayMetrics dm = res.getDisplayMetrics();
        res.updateConfiguration(conf, dm);
        Locale.setDefault(myLocale);

        Toast.makeText(getApplicationContext(), R.string.language_setting_message, Toast.LENGTH_LONG).show();
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        Button zh_btn = (Button)findViewById(R.id.zh);
        zh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Locale locale = new Locale("zh_CN");
//                Locale.setDefault(locale);
//
//                Configuration config = new Configuration();
//                config.locale = locale;
//                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//                onConfigurationChanged(config);
//                restartActivity();
                setLocale("zh_CN");
            }
        });
        Button en_btn = (Button)findViewById(R.id.en);
        en_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Locale locale_en = new Locale("en_US");
//                Locale.setDefault(locale_en);
//                Configuration config = new Configuration();
//                config.locale = locale_en;
//                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//                onConfigurationChanged(config);
//                restartActivity();
                setLocale("en_US");
            }
        });
    }

    public void start(View view) {finish();}
}
