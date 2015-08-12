package com.spotxchange.demo.vpaid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static final String TOAST_TEXT = "Loading channel 68801...";

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
        _adWebView = ((WebView) findViewById(R.id.interstitial));
        loadInterstitial();

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_SHORT).show();
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadInterstitial() {
        _showButton.setEnabled(false);

        WebView.setWebContentsDebuggingEnabled(true);
        _adWebView.setVisibility(View.INVISIBLE);
        _adWebView.getSettings().setJavaScriptEnabled(true);
        //_adWebView.loadUrl("http://local.spotxcdn.com/adcallback");
        _adWebView.loadDataWithBaseURL(
                "http://search.spotxchange.com",
                getString(R.string.PLACEHOLDER_htmlAdBrokerScript),
                "text/html",
                "utf8",
                null
        );

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                _showButton.setEnabled(true);

                _adWebView.evaluateJavascript(
                        getString(R.string.jsEnvironment),
                        null
                );
                _adWebView.evaluateJavascript(
                        getString(R.string.PLACEHOLDER_jsAdParameters),
                        null
                );

                _adWebView.evaluateJavascript(
                        getString(R.string.jsGetVPAIDAd),
                        null
                );
                Toast.makeText(MainActivity.this, "ctor", Toast.LENGTH_SHORT).show();
            }
        }, 5000);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                _adWebView.evaluateJavascript(
                    getString(R.string.jsInitAd),
                    null
                );
                Toast.makeText(MainActivity.this, "init", Toast.LENGTH_SHORT).show();
            }
        }, 7000);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                _adWebView.evaluateJavascript(
                    getString(R.string.jsStartAd),
                    null
                );
                Toast.makeText(MainActivity.this, "start", Toast.LENGTH_SHORT).show();
            }
        }, 10000);
    }

    private void closeAd() {
        loadInterstitial();
    }
}
