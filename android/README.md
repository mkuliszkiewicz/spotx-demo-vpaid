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
}


private void loadVASTResponse() {
    String targetUrl =  "https://your-vast-endpoint.com"
    // Download the VAST response
    VASTDownloadTask.VASTResponseHandler handler = new VASTDownloadTask.VASTResponseHandler() {
        @Override
        public void onSuccessResponse(String response) {
            final VPAIDResponse vpaidResponse = VASTParser.read(new InputSource(new StringReader(response)));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadInterstitial(vpaidResponse); // yay, successful VAST response, now load it
                }
            });
        }

        @Override
        public void onEmptyResponse() {
            // VAST was response was empty
        }

        @Override
        public void onFailure(final String response) {
            // Could not read VAST response
        }
    };
    new VASTDownloadTask(handler).execute(targetUrl);
}
```

Once you have successfully parsed your VAST XML, load it into a WebView with a custom Javascript interface we will write to listen for our VPAID events
```java
private void loadInterstitial(final VPAIDResponse vpaidResponse) {pt interface
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
}

private void evaluateJavascript(final String javascript) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            _adWebView.evaluateJavascript(javascript, null);
        }
    });
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
