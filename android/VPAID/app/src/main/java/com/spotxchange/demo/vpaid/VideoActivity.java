package com.spotxchange.demo.vpaid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.spotxchange.demo.vpaid.vastparser.VPAIDResponse;

import java.util.LinkedList;
import java.util.Queue;

public class VideoActivity extends AppCompatActivity {
    // Pool of available VAST responses to play.
    public static Queue<VPAIDResponse> pool = new LinkedList<>();

    // Current view.
    private WebView _view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        VPAIDResponse vpaidResponse = pool.poll();
        if (vpaidResponse != null) {
            _view = loadInterstitial(vpaidResponse);
            //setContentView(_view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // On KitKat and earlier, we cannot load video in the background without causing a rendering failure (video without audio)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                _view.setVisibility(View.VISIBLE);
            }
        }
        else {
            finish();
        }


    }

    /**
     * Loads the VPAID ad parsed from the VAST response into the VPAID WebView
     * @param vpaidResponse
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private WebView loadInterstitial(final VPAIDResponse vpaidResponse) {
        WebView newView = ((WebView) findViewById(R.id.interstitial));
        // Setup our WebView with a javascript interface
        WebView.setWebContentsDebuggingEnabled(true);
        //_view.setVisibility(View.INVISIBLE);
        newView.getSettings().setJavaScriptEnabled(true);
        newView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        // load our VAID JS interface
        newView.addJavascriptInterface(new VpaidAdInterface(this, vpaidResponse), "NativeInterface");

        // load the AdBroker with the given media source parsed from the VAST resposne
        newView.loadDataWithBaseURL(
                "http://search.spotxchange.com",
                String.format(
                        getString(R.string.htmlAdBrokerScript),
                        vpaidResponse.mediaUrl
                ),
                "text/html",
                "utf8",
                null
        );

        Toast.makeText(VideoActivity.this, "Constructing VPAID ad...", Toast.LENGTH_SHORT).show();

        return newView;
    }


    /**
     * Starts the VPAID ad that has been loaded, otherwise notify user tha ad did not load
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if(_view != null) {
            _view.evaluateJavascript(
                    getString(R.string.jsStartAd),
                    null
            );
            _view.setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Evals the Javascript inside the VPAID WebView
     * @param javascript
     */
    private void evaluateJavascript(final String javascript) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _view.evaluateJavascript(javascript, null);
            }
        });
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
            // Ad has successfully loaded, let's play it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInterstitial();
                    Toast.makeText(VideoActivity.this, "Ad loaded.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void onAdStarted() {
            // Ad has started playing, let's notify the user
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoActivity.this, "Ad started.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
