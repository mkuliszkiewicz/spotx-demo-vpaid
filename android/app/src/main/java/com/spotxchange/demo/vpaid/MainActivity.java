package com.spotxchange.demo.vpaid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotxchange.demo.vpaid.vastparser.VASTParser;
import com.spotxchange.demo.vpaid.vastparser.VPAIDResponse;

import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * Copyright (C) 2015 SpotXchange
 */
public class MainActivity extends Activity {
    private Button _loadButton;
    private Button _showButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set our load ad button
        _loadButton = ((Button) findViewById(R.id.load_button));
        _loadButton.setEnabled(true);
        _loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadVASTResponse();
            }
        });

        // Create the next level button, which tries to show an interstitial when clicked.
        _showButton = ((Button) findViewById(R.id.show_button));
        _showButton.setEnabled(false);
        _showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchVideoActivity();
            }
        });

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        Toast.makeText(this, "Ready to load ads.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Fetches the VAST response from the given URL asynchronously, and handles the response
     */
    private void loadVASTResponse() {
        TextView target = ((TextView) findViewById(R.id.target));
        String targetUrl =  target.getText().toString();

        Toast.makeText(this, "Loading " + targetUrl, Toast.LENGTH_SHORT).show();

        // Download the VAST response
        VASTDownloadTask.VASTResponseHandler handler = new VASTDownloadTask.VASTResponseHandler() {
            @Override
            public void onSuccessResponse(String response) {
                Log.d("VASTResponseHandler", response);
                final VPAIDResponse vpaidResponse = VASTParser.read(new InputSource(new StringReader(response)));
                
                if(vpaidResponse == null){
                    // VAST was response was empty
                    Log.d("VASTResponseHandler", "Received invalid VAST or XML!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error: Invalid VAST or XML.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Log.d("VASTResponseHandler", vpaidResponse.mediaUrl);
                    Log.d("VASTResponseHandler", vpaidResponse.adParameters);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // yay, successful VAST response, now load it
                            readyVpaidResponse(vpaidResponse);
                        }
                    });
                }
            }

            @Override
            public void onEmptyResponse() {
                // VAST was response was empty
                Log.d("VASTResponseHandler", "Received empty VAST response.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Received empty response when loading VAST.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(final String response) {
                // Could not read VAST response
                Log.e("VASTResponseHandler", "Failure: " + response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Received failure message when loading VAST: " + response, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        new VASTDownloadTask(handler).execute(targetUrl);
    }

    private void readyVpaidResponse(VPAIDResponse vpaidResponse) {
        VideoActivity.pool.add(vpaidResponse);
        _showButton.setEnabled(true);
    }

    private void launchVideoActivity() {
        _showButton.setEnabled(false);
        Intent adIntent = new Intent(this, VideoActivity.class);
        adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
