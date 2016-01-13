package com.spotxchange.demo.easi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.spotxchange.demo.easi.testcase.TestcaseListFragment;

/**
 * Copyright (C) 2015 SpotXchange
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if has intent (i.e. launched from command line or headless app tool

        Log.d("Main", "launching activity");
        if (getIntent().getExtras() != null) {
            Log.d("Main", String.format("got extras %1$s", getIntent().getExtras().getBoolean(getString(R.string.headless), false)));
            SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            if (getIntent().getExtras().getBoolean(getString(R.string.headless), false)) {
                Log.d("Main", "got headless");
                preferencesEditor
                        .putBoolean(getString(R.string.headless), true);
            }

            String webhook = getIntent().getExtras().getString(getString(R.string.results_webhook), "");
            if (!webhook.isEmpty()) {
                preferencesEditor
                        .putString(getString(R.string.results_webhook), webhook);
            }

            preferencesEditor.apply();
        }


        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction()
                //.replace(R.id.fragment_container, new UserInputFragment())
                .replace(R.id.fragment_container, new TestcaseListFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
        }
        return super.onOptionsItemSelected(item);
    }
}
