package com.spotxchange.demo.easi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Copyright (C) 2015 SpotXchange
 */
public class MainActivity extends Activity {
    public final static String EXTRA_SCRIPTDATA = "SCRIPTDATA";
    private Button _showButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creates and shows an EASI interstitial when clicked.
        _showButton = ((Button) findViewById(R.id.show_button));
        _showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchVideoActivity();
            }
        });

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        Toast.makeText(this, "Ready to load ads.", Toast.LENGTH_SHORT).show();
    }

    private void launchVideoActivity() {
        _showButton.setEnabled(false);
        Intent adIntent = new Intent(this, VideoActivity.class);
        adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String input = ((EditText) findViewById(R.id.target)).getText().toString();
        adIntent.putExtra(EXTRA_SCRIPTDATA, input);
        startActivity(adIntent);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
