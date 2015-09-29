package com.spotxchange.demo.vpaid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
    private WebView _adWebView;

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
                showInterstitial();
            }
        });

        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        _adWebView = ((WebView) findViewById(R.id.interstitial));

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
                Log.d("VASTResponseHandler", vpaidResponse.mediaUrl);
                Log.d("VASTResponseHandler", vpaidResponse.adParameters);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // yay, successful VAST response, now load it
                        loadInterstitial(vpaidResponse);
                    }
                });
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

    /**
     * Starts the VPAID ad that has been loaded, otherwise notify user tha ad did not load
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if(_adWebView != null) {
            _adWebView.evaluateJavascript(
                getString(R.string.jsStartAd),
                null
            );
        }
        else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            closeAd();
        }
    }

    /**
     * Loads the VPAID ad parsed from the VAST response into the VPAID WebView
     * @param vpaidResponse
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadInterstitial(final VPAIDResponse vpaidResponse) {
        _showButton.setEnabled(false);

        // Setup our WebView with a javascript interface
        WebView.setWebContentsDebuggingEnabled(true);
        _adWebView.setVisibility(View.INVISIBLE);
        _adWebView.getSettings().setJavaScriptEnabled(true);
        // load our VAID JS interface
        _adWebView.addJavascriptInterface(new VpaidAdInterface(this, vpaidResponse), "NativeInterface");

        // load the AdBroker with the given media source parsed from the VAST resposne
        _adWebView.loadDataWithBaseURL(
                "http://search.spotxchange.com",
                String.format(
                        getString(R.string.htmlAdBrokerScript),
                        vpaidResponse.mediaUrl
                ),
                "text/html",
                "utf8",
                null
        );

        Toast.makeText(MainActivity.this, "Constructing VPAID ad...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Evals the Javascript inside the VPAID WebView
     * @param javascript
     */
    private void evaluateJavascript(final String javascript) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _adWebView.evaluateJavascript(javascript, null);
            }
        });
    }

    private void closeAd() {
        // ...
    }


    /**
     * Handles the VPAID events and fires off the appropriate Javascript into the VPAID WebView
     */
    private class VpaidAdInterface {
        Context _context;
        VPAIDResponse _response;

        VpaidAdInterface(Context context, VPAIDResponse vpaidResponse) {
            _context = context;
            _response = vpaidResponse;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @JavascriptInterface
        public void onPageLoaded() {
            // load the environment  variables
            evaluateJavascript(getString(R.string.jsEnvironment));

            // load the JS ad parameters parsed from the VAST response
            evaluateJavascript(String.format(getString(R.string.jsAdParameters),_response.adParameters));

            // load the getVPAIDAd JS
            evaluateJavascript(getString(R.string.jsGetVPAIDAd));
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @JavascriptInterface
        public void onGetVpaidAd() {
            // VPAID is loaded, now let's init the ad
            evaluateJavascript(getString(R.string.jsInitAd));
        }

        @JavascriptInterface
        public void onAdLoaded() {
            // Ad has successfully loaded, let's notify the user
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _showButton.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Ad loaded.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void onAdStarted() {
            // Ad has started playing, let's notify the user
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _adWebView.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Ad started.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
