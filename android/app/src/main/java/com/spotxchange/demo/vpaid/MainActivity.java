package com.spotxchange.demo.vpaid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotxchange.demo.vpaid.vastparser.VASTParser;
import com.spotxchange.demo.vpaid.vastparser.VPAIDResponse;

import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import java.io.StringReader;


public class MainActivity extends Activity {
    private Button _loadButton;
    private Button _showButton;
    private WebView _adWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private void loadVASTResponse() {
        TextView target = ((TextView) findViewById(R.id.target));
        String targetUrl =  target.getText().toString();

        Toast.makeText(this, "Loading " + targetUrl, Toast.LENGTH_SHORT).show();

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
                        loadInterstitial(vpaidResponse);
                    }
                });
            }

            @Override
            public void onEmptyResponse() {
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if(_adWebView != null) {
            _adWebView.setVisibility(View.VISIBLE);
            _adWebView.evaluateJavascript(
                    getString(R.string.jsStartAd),
                    null
            );
            Toast.makeText(MainActivity.this, "start", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            closeAd();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadInterstitial(final VPAIDResponse vpaidResponse) {
        _showButton.setEnabled(false);

        WebView.setWebContentsDebuggingEnabled(true);
        _adWebView.setVisibility(View.INVISIBLE);
        _adWebView.getSettings().setJavaScriptEnabled(true);
        _adWebView.addJavascriptInterface(new VpaidAdInterface(this, vpaidResponse), "NativeInterface");

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

    private void evaluateJavascript(final String javascript)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _adWebView.evaluateJavascript(javascript, null);
            }
        });
    }

    private void closeAd() {
        //loadInterstitial();
    }

    private class VpaidAdInterface {
        Context _context;
        VPAIDResponse _response;

        VpaidAdInterface(Context context, VPAIDResponse vpaidResponse)
        {
            _context = context;
            _response = vpaidResponse;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @JavascriptInterface
        public void onPageLoaded() {
            evaluateJavascript(
                getString(R.string.jsEnvironment)
            );

            evaluateJavascript(
                String.format(
                    getString(R.string.jsAdParameters),
                    _response.adParameters
                )
            );

            evaluateJavascript(
                    getString(R.string.jsGetVPAIDAd)
            );
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @JavascriptInterface
        public void onGetVpaidAd() {
            evaluateJavascript(
                    getString(R.string.jsInitAd)
            );
        }

        @JavascriptInterface
        public void onAdLoaded() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _showButton.setEnabled(true);
                }
            });
        }
    }
}
