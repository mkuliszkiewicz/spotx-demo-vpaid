//
//  Copyright (c) 2015 SpotX. All rights reserved.
//

import UIKit
import Foundation
import SWXMLHash

let defaultVastURL = "http://search.spotxchange.com/vast/2.00/85394?VPAID=js&app[domain]=com.spotxchange.vpaid"
let bundleUrlParam = "&app%5Bbundle%5D=com.spotxchange.android-vpaid-demo"

var vpaidWebView: UIWebView = UIWebView(frame: CGRectMake(0, 50, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
var vpaidWebViewDelegate: VpaidWebViewDelegate = VpaidWebViewDelegate()

class NativeVpaidViewController: UIViewController {

    // MARK: Set Up

    @IBOutlet weak var textFieldVastUrl: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()
        setupWebView()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    // MARK: Button Actions

    @IBAction func btnLaunchWebView(sender: AnyObject) {
        let vastUrl = self.textFieldVastUrl.text!
        // display a loading message in the web view while we wait
        vpaidWebView.loadHTMLString("<html><head></head><body><center>loading...</center></body></html>", baseURL: nil)
        self.view.addSubview(vpaidWebView)
        //  make the VAST call
        self.fetchVastThenLoadVpaidWebView(vastUrl)
    }

    @IBAction func btnCloseWebView(sender: AnyObject) {
        setupWebView()
    }

    // MARK: Private WebView Functions

    /**
    *   Sets up an empty WebView that allows media inline playback and is attached to our delegate
    */
    private func setupWebView(){
        vpaidWebView.removeFromSuperview()
        vpaidWebView = UIWebView(frame: CGRectMake(0, 68, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height - 49))
        vpaidWebView.allowsInlineMediaPlayback = true
        vpaidWebView.mediaPlaybackRequiresUserAction = false
        vpaidWebView.delegate = vpaidWebViewDelegate
    }

    /**
    *   Makes a network request to the VAST endpoint and parses out the XML data and loads up the web view
    */
    private func fetchVastThenLoadVpaidWebView(vastUrl: String){
        let vurl = (vastUrl.isEmpty) ? NSURL(string: defaultVastURL) : NSURL(string: vastUrl + bundleUrlParam)
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
    }


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

}

// MARK: WebViewDelegate

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


