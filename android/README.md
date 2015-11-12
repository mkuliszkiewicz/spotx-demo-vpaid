# SpotX VPAID Demo APP
Testing app for SpotX's VPAID for in app WebView for Android.

## How to use SpotX VPAID 2.0 inside a WebView
First fetch the VAST response from your endpoint
```java
public class VASTDownloadTask extends AsyncTask<String, Integer, Integer> {
    private VASTResponseHandler _handler;

    public VASTDownloadTask(VASTResponseHandler handler) {
        _handler = handler;
    }

    @Override
    protected Integer doInBackground(String... urls ) {
        StringBuilder responseBuilder = new StringBuilder();
        try {
            URL url = new URL(urls[0]); // https://your-vast-endpoint.com
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() >= 400) {
                _handler.onFailure(connection.getResponseMessage());
                return connection.getResponseCode();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            if (responseBuilder.length() > 0) {
                _handler.onSuccessResponse(responseBuilder.toString());
                return connection.getResponseCode();
            }
            else {
                _handler.onEmptyResponse();
                return connection.getResponseCode();
            }
        }
        catch(Exception e) {
            Log.e(VASTDownloadTask.class.getSimpleName(), e.toString());
            _handler.onFailure(e.toString());
            return -1;
        }
    }

    public interface VASTResponseHandler {
        void onSuccessResponse(String response);
        void onEmptyResponse();
        void onFailure(String response);
    }
}
```

And in your Activity download and handle the VAST response by parsing it
```java
// helper class to parse the VAST XML
public class VASTParser {
    public static VPAIDResponse read(InputSource xmlSource) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String mediaUrl = null;
        String adParameters = null;
        try {
            // find the linear root
            Node linear = (Node) xpath.evaluate("/VAST/Ad/InLine/Creatives/Creative/Linear", xmlSource, XPathConstants.NODE);
            // get the media source file
            mediaUrl = xpath.evaluate("MediaFiles/MediaFile", linear);
            // get the ad aparamets
            adParameters = xpath.evaluate("AdParameters", linear);
        }
        catch(XPathExpressionException e) {
            // oops, bad VAST, XML, or whatever
            return null;
        }
        return new VPAIDResponse(mediaUrl, adParameters);
    }
}

// helper class for storing the necessary variables for the VPAID response
public class VPAIDResponse {
    public final String mediaUrl;
    public final String adParameters;
    public VPAIDResponse (String mediaUrl, String adParameters) {
        this.mediaUrl = mediaUrl;
        this.adParameters = adParameters;
    }

```

Once you have successfully parsed your VAST XML, queue the VAST response up for playing in a new activity. 
```java
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
                        readyVpaidResponse(vpaidResponse);
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
```

Then, when you're ready to show an add, launch a new activity with the VPAID response into a WebView with a custom Javascript interface we will write to listen for our VPAID events
```java

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
        // Ad has successfully loaded, lets' start playing it
        _adWebView.evaluateJavascript(getString(R.string.jsStartAd),null);
    }

    @JavascriptInterface
    public void onAdStarted() {
        // Ad has started playing
    }
}
```
And our javascript.xml that holds all of our Javascript strings.
File: strings.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="jsEnvironment"><![CDATA[
    var oAdOS,
        iContentWidth = window.innerWidth || 320,
        iContentHeight = window.innerHeight || 240,
        strViewMode = \'normal\',
        oEnvVars = {
        };
    ]]></string>
    <string name="jsAdParameters">
        "var oCreativeData = %s;"
    </string>
    <string name="htmlAdBrokerScript"><![CDATA[
        <!DOCTYPE html>
        <head>
            <script src="%s" type="text/javascript"></script>
            <script>
                var pollingId,
                    nativeCallback = function() {
                        if (window.NativeInterface && window.NativeInterface.onPageLoaded)
                        {
                            NativeInterface.onPageLoaded();
                            window.clearInterval(pollingId);
                        }
                    };
                pollingId = setInterval(
                    nativeCallback,
                    50
                    );
            </script>
        </head>
        <body/>
    ]]></string>
    <string name="jsGetVPAIDAd"><![CDATA[
    if(document.readyState == \'complete\')
    {
        window.oAdOS = getVPAIDAd();
        NativeInterface.onGetVpaidAd();
    }
    else
    {
        window.onload = function()
        {
            window.oAdOS = getVPAIDAd();
            NativeInterface.onGetVpaidAd();
        }
    };
    ]]></string>
    <!-- When calling native interface functions, we wrap them in an anonymous function to prevent
    "npmethod called on non-npobject" errors that happen when attempting to bind external interfaces
    to variables in javascript. -->
    <string name="jsInitAd"><![CDATA[
    window.oAdOS.subscribe(function() {NativeInterface.onAdLoaded();}, \"AdLoaded\");
    window.oAdOS.subscribe(function() {NativeInterface.onAdStarted();}, \"AdStarted\");

    window.oAdOS.initAd(
        iContentWidth,
        iContentHeight,
        oEnvVars.media_transcoding,
        0,
        JSON.stringify(oCreativeData),
        oEnvVars
        );
    ]]></string>
    <string name="jsStartAd"><![CDATA[
    window.oAdOS.startAd();
    ]]></string>
    <string name="jsInsertVideoSlot"><![CDATA[
    var videoSlot = window.document.createElement(\"video\");
    oEnvVars[\"videoSlot\"] = videoSlot;
    ]]></string>
</resources>
```


Then pat yourself on the back because you have now parsed a VAST response and loaded your VPAID ad in-app into your WebView!
