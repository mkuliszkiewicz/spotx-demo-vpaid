# SpotX VPAID Demo APP
Testing app for SpotX's VPAID for in app UIWebViews for iOS.

## Set up
```
pod install
```
then open up the ```VPAID-Demo.xcworkspace```


## How to use SpotX VPAID 2.0 inside a UIWebView
Create a UIWebView and attach a UIWebView delegate to handle VPAID events:
```swift
var vpaidWebView: UIWebView = UIWebView(frame: CGRectMake(0, 0, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
vpaidWebView.allowsInlineMediaPlayback = true
vpaidWebView.delegate = vpaidWebViewDelegate
```

Set up the UIWebViewDelegate to listen for the VPAID events
```swift
class VpaidWebViewDelegate: NSObject, UIWebViewDelegate {
    // our custom prefix to listen for in URLs
    let vpaidPrefix = "vpaid2://"

    func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        // get the URL of the request
        let url = request.URL?.absoluteString
        // check if the URL matches our prefix
        if(url?.rangeOfString(vpaidPrefix) != nil){
            // get the VPAID event out of the URL
            let event = url?.stringByReplacingOccurrencesOfString(vpaidPrefix, withString: "", options: NSStringCompareOptions.LiteralSearch, range: nil)
            // process it
            self.processVpaidEvent(event!)
            return false
        }
        //return (url == "about:blank") // uncomment to stop click-thrus
        return true
    }

    private func processVpaidEvent(event: String){
        switch event {
            case "ad_loaded":
                // let's start the ad
                vpaidWebView.stringByEvaluatingJavaScriptFromString("window.oAdOS.startAd();")
            case "ad_started":
                // ...
                break
            case "ad_error":
                // ...
                break
            case "ad_paused":
                // ...
                break
            case "ad_stopped":
                // ...
                break
            case "ad_clicked":
                // ...
                break
            default:
                // ...
                break
        }
    }
}
```

Then fetch the VAST response and parse out the media file and ad parameters:
```swift
let vurl = "https://your-vast-endpoint.com"
let task = NSURLSession.sharedSession().dataTaskWithURL(vurl!) {(data, response, error) in
    // parse the response xml
    let xml = SWXMLHash.parse(data!)

    // get the media framework
    let mediaFramework = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["MediaFiles"]["MediaFile"][0].element?.attributes["apiFramework"]
    // get the media source file
    let mediaUrl = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["MediaFiles"]["MediaFile"][0].element?.text
    // get the required parameters to play the ad
    let adParams = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["AdParameters"].element?.text

    // check to make sure the media framework is VPAID and the media and parameters are not empty
    if(mediaFramework != nil && mediaFramework! == "VPAID" && mediaUrl != nil && adParams != nil){
        // shove it in, load it up
        self.loadVpaidWebView(adParams!, mediaUrl: mediaUrl!)
    }
    else{
        // alert the user the VAST wasn't valid
        let refreshAlert = UIAlertController(title: "Invalid VAST response", message: "empty vast or media is not vpaid", preferredStyle: UIAlertControllerStyle.Alert)
        refreshAlert.addAction(UIAlertAction(title: "OK", style: .Default, handler: {
            (action: UIAlertAction!) in
                self.setupWebView()
            }
        ))
        dispatch_async(dispatch_get_main_queue(), {
            self.presentViewController(refreshAlert, animated: true, completion: nil)
        })
    }
}
task.resume()
```

Then load your javascript into the UIWebView with the parameters pulled from the VAST response:
```swift
/**
*   Loads the web view with our dynamically created HTML and given javascript variables
*/
private func loadVpaidWebView(adParams: String, mediaUrl: String){
    vpaidWebView.loadHTMLString(createHtml(adParams, mediaUrl: mediaUrl), baseURL: nil)
    self.view.addSubview(vpaidWebView)
}

/**
*   Returns a simple HTML page (as a String) embedded with js script tags populated with the given media url and ad parameters
*/
private func createHtml(adParams: String, mediaUrl: String) -> String{
    return String(format:
        "<!DOCTYPE html><html><head> \n" +
        "<script src=\"%@\" type=\"text/javascript\"></script> \n" +
        "<script type=\"text/javascript\"> \n" +
            self.getVpaidJS() +
        "</script> \n" +
        "</head><body></body></html>", mediaUrl, adParams)
}

/**
*   Loads our custom javascript from the file
*/
private func getVpaidJS() -> String{
    do{
        let path = NSBundle.mainBundle().pathForResource("ios-vpaid", ofType: "js")
        let js = try String(contentsOfFile: path!, encoding: NSUTF8StringEncoding)
        return js
    }
    catch{
        return "";
    }
}
```

ios-vpaid.js
```javascript
var oAdOS, iContentWidth = window.innerWidth || 320, iContentHeight = window.innerHeight || 240, strViewMode = "normal", oEnvVars = { "in-app":true };
var oCreativeData = %@;

function attachIframe(srcName){
    var iframe = document.createElement('iframe');
    iframe.id = "ios-vpaid-event-iframe-" + srcName;
    iframe.style.display = "none";
    iframe.src = "vpaid2://" + srcName;
    document.body.appendChild(iframe);
}

if(document.readyState == "complete"){
    window.oAdOS = getVPAIDAd();
    oAdOS.subscribe(function(){ attachIframe("ad_loaded"); },  "AdLoaded", null);
    oAdOS.subscribe(function(){ attachIframe("ad_started"); }, "AdStarted", null);
    oAdOS.subscribe(function(){ attachIframe("ad_paused"); },  "AdPaused", null);
    oAdOS.subscribe(function(){ attachIframe("ad_stopped"); }, "AdStopped", null);
    oAdOS.subscribe(function(){ attachIframe("ad_error"); },   "AdError", null);
    oAdOS.subscribe(function(){ attachIframe("ad_clicked"); }, "AdClickThru", null);
    window.oAdOS.initAd(iContentWidth, iContentHeight, oEnvVars.media_transcoding, 0, JSON.stringify(oCreativeData), oEnvVars);
}
else{
    window.onload = function() {
        window.oAdOS = getVPAIDAd();
        oAdOS.subscribe(function(){ attachIframe("ad_loaded");  },  "AdLoaded", null);
        oAdOS.subscribe(function(){ attachIframe("ad_started"); }, "AdStarted", null);
        oAdOS.subscribe(function(){ attachIframe("ad_paused"); },  "AdPaused", null);
        oAdOS.subscribe(function(){ attachIframe("ad_stopped"); }, "AdStopped", null);
        oAdOS.subscribe(function(){ attachIframe("ad_error"); },   "AdError", null);
        oAdOS.subscribe(function(){ attachIframe("ad_clicked"); }, "AdClickThru", null);
        window.oAdOS.initAd(iContentWidth, iContentHeight, oEnvVars.media_transcoding, 0, JSON.stringify(oCreativeData), oEnvVars);
    }
}
```

Then pat yourself on the back, you can now play SpotX VPAID 2.0 ads.
