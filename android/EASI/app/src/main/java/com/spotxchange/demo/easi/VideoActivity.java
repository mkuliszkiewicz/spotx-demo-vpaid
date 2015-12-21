package com.spotxchange.demo.easi;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.Toast;

public class VideoActivity extends AppCompatActivity {
    public final static String EXTRA_SCRIPTDATA = "SCRIPTDATA";

    // Current view.
    private WebView _view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        _view = loadInterstitial();
    }

    @Override
    protected void onPause () {
        super.onPause();
        if (null != _view) {
            /*
                From: http://developer.android.com/reference/android/webkit/WebView.html#onPause()

                Pauses any extra processing associated with this WebView and its associated DOM,
                plugins, JavaScript etc. For example, if this WebView is taken offscreen,
                this could be called to reduce unnecessary CPU or network traffic.
                When this WebView is again "active", call onResume().
             */
            _view.onPause();
            _view.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != _view) {
            /*
                Resumes a WebView after a previous call to onPause().
             */
            _view.onResume();
            _view.resumeTimers();
        }
    }


    /**
     * Loads the EASI tag using the user input
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private WebView loadInterstitial() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String scriptTag = preferences.getString(
            getString(R.string.tag_url),
            getString(R.string.default_easi_url)
            );

        //String scriptTag = "http://search.spotxchange.com/js/spotx.js";

        // load EASI with the script values given
        String scriptData = getIntent().getStringExtra(VideoActivity.EXTRA_SCRIPTDATA);
        if (scriptData == null || scriptData.isEmpty()) {
            scriptData = getString(R.string.default_target);
        }

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

        if (preferences.getBoolean( getString(R.string.lock_orientation), true)) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            else
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        // For debugging, compare with HTML EASI demo page
        //newView.loadUrl("http://search.spotxchange.com/test/ad/js/easi/EASI.html");

        Toast.makeText(VideoActivity.this, "Constructing EASI ad...", Toast.LENGTH_SHORT).show();

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
