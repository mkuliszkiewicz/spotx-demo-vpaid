package com.spotxchange.demo.easi;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.Toast;

public class VideoActivity extends AppCompatActivity {
    // Current view.
    private WebView _view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        _view = loadInterstitial();
    }

    /**
     * Loads the EASI tag using the user input
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private WebView loadInterstitial() {
        String scriptTag = "http://aka.spotxcdn.com/media/videos/js/easi/EASI_2015-11-10_20-56.debug.js";
        //String scriptTag = "http://search.spotxchange.com/js/spotx.js";

        String scriptData = "data-example='foobar'";
        // load EASI with the script values given
        // scriptData = getScript();

        // Setup our WebView with a javascript interface
        WebView newView = ((WebView) findViewById(R.id.interstitial));
        WebView.setWebContentsDebuggingEnabled(true);
        newView.getSettings().setJavaScriptEnabled(true);
        newView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        newView.loadDataWithBaseURL(
                "http://search.spotxchange.com",
                String.format(
                        getString(R.string.easiTemplate),
                        scriptTag,
                        scriptData
                ),
                "text/html",
                "utf8",
                null
        );

        // For debugging, compare with HTML EASI demo page
        //newView.loadUrl("http://search.spotxchange.com/test/ad/js/easi/EASI.html");

        Toast.makeText(VideoActivity.this, "Constructing VPAID ad...", Toast.LENGTH_SHORT).show();

        return newView;
    }

    /**
     * Evals the Javascript inside the WebView
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
}
