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

    let vpaidPrefix = "vpaid2://"

    func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        var url = request.URL?.absoluteString
        if(url?.rangeOfString(vpaidPrefix) != nil){
            var event = url?.stringByReplacingOccurrencesOfString(vpaidPrefix, withString: "", options: NSStringCompareOptions.LiteralSearch, range: nil)
            self.processVpaidEvent(event!)
            return false
        }
        return true
    }

    private func processVpaidEvent(event: String){
        switch event {
            case "ad_loaded":
                vpaidWebView.stringByEvaluatingJavaScriptFromString("window.oAdOS.startAd();")
            case "ad_started":
                break
            case "ad_error":
                break
            case "ad_paused":
                break
            case "ad_stopped":
                break
            case "ad_clicked":
                break
            default:
                break
        }
    }
}
```

Then fetch the VAST response and parse out the media file and ad parameters:
```swift
let vurl = "http://your-vast-endpoint.com"
let task = NSURLSession.sharedSession().dataTaskWithURL(vurl!){ (data, response, error) in

    var xmlData = NSString(data: data, encoding: NSUTF8StringEncoding)
    let xml = SWXMLHash.parse(data)

    var mediaUrl = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["MediaFiles"]["MediaFile"][0].element?.text
    var adParams = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["AdParameters"].element?.text

    self.loadVpaidWebView(adParams!, mediaUrl: mediaUrl!)
}
task.resume()
```

Then load your javascript into the UIWebView with the parameters pulled from the VAST response:
```swift
private func loadVpaidWebView(adParams: String, mediaUrl: String){
    var html = self.createHtml(adParams, mediaUrl: mediaUrl)
    vpaidWebView.loadHTMLString(createHtml(adParams, mediaUrl: mediaUrl), baseURL: nil)
    self.view.addSubview(vpaidWebView)
}

private func createHtml(adParams: String, mediaUrl: String) -> String{
    return String(format:
        "<html><head> \n" +
        "<script src=\"%@\" type=\"text/javascript\"></script> \n" +
        "<script type=\"text/javascript\"> \n" +
            self.getVpaidJS() +
        "</script> \n" +
        "</head><body></body></html>", mediaUrl, adParams)
}

private func getVpaidJS() -> String{
    let path = NSBundle.mainBundle().pathForResource("ios-vpaid", ofType: "js")
    return String(contentsOfFile: path!, encoding: NSUTF8StringEncoding, error: nil)!
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
