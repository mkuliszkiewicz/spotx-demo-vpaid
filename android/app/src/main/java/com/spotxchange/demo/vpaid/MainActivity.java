package com.spotxchange.demo.vpaid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static final String TOAST_TEXT = "Testing.";

    private Button _showButton;
    private WebView _adWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the next level button, which tries to show an interstitial when clicked.
        _showButton = ((Button) findViewById(R.id.show_button));
        _showButton.setEnabled(false);
        _showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInterstitial();
            }
        });

        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        _adWebView = new WebView(this);
        loadInterstitial();

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if(_adWebView != null) {
            _adWebView.setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            closeAd();
        }
    }

    private void loadInterstitial() {
        _showButton.setEnabled(false);
        _adWebView.loadUrl("http://local.spotxcdn.com/adcallback");
        _adWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                _showButton.setEnabled(true);
            }
        });
    }

    private void closeAd() {
        loadInterstitial();
    }
}
