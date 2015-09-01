//
//  NativeVastViewController.swift
//  VPAID-Demo
//
//  Created by scottjulian on 8/8/15.
//  Copyright (c) 2015 spotx. All rights reserved.
//

import UIKit
import Foundation
import SWXMLHash

let defaultVastURL = "http://search.spotxchange.com/vast/2.00/85394?VPAID=js&app[domain]=com.spotxchange.vpaid"
let bundleUrlParam = "&app%5Bbundle%5D=com.spotxchange.android-vpaid-demo"
let adBrokerUrl = "http://aka.spotxcdn.com/media/videos/js/ad/InstreamAdBroker_2015-07-07_14-37.debug.js"

var vpaidWebView: UIWebView = UIWebView(frame: CGRectMake(0, 50, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
var vpaidWebViewDelegate: VpaidWebViewDelegate = VpaidWebViewDelegate()

/*
var url = NSURL(string: "http://nameless-tundra-9674.herokuapp.com/vpaid/85394?app.domain=com.spotxchange.vpaid&autoplay=0&events=true")
vpaidWebView.loadRequest(NSURLRequest(URL: url!))
*/
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
        var vastUrl = self.textFieldVastUrl.text
        vpaidWebView.loadHTMLString("<html><head></head><body><center>loading...</center></body></html>", baseURL: nil)
        self.view.addSubview(vpaidWebView)
        self.fetchVastThenLoadVpaidWebView(vastUrl)
    }

    @IBAction func btnCloseWebView(sender: AnyObject) {
        setupWebView()
    }

    // MARK: Private WebView Functions

    private func setupWebView(){
        vpaidWebView.removeFromSuperview()
        vpaidWebView = UIWebView(frame: CGRectMake(0, 68, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height - 49))
        vpaidWebView.allowsInlineMediaPlayback = true
        vpaidWebView.mediaPlaybackRequiresUserAction = false
        vpaidWebView.delegate = vpaidWebViewDelegate
    }

    private func fetchVastThenLoadVpaidWebView(vastUrl: String){
        let vurl = (vastUrl.isEmpty) ? NSURL(string: defaultVastURL) : NSURL(string: vastUrl + bundleUrlParam)
        let task = NSURLSession.sharedSession().dataTaskWithURL(vurl!) {(data, response, error) in
            var xmlData = NSString(data: data, encoding: NSUTF8StringEncoding)
            let xml = SWXMLHash.parse(data)
            
            var mediaFramework = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["MediaFiles"]["MediaFile"][0].element?.attributes["apiFramework"]
            var mediaUrl = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["MediaFiles"]["MediaFile"][0].element?.text
            var adParams = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["AdParameters"].element?.text

            if(mediaFramework != nil && mediaFramework! == "VPAID" && mediaUrl != nil && adParams != nil){
                self.loadVpaidWebView(adParams!, mediaUrl: mediaUrl!)
            }
            else{
                var refreshAlert = UIAlertController(title: "Invalid VAST response", message: "empty vast or media is not vpaid", preferredStyle: UIAlertControllerStyle.Alert)
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

}

// MARK: WebViewDelegate

class VpaidWebViewDelegate: NSObject, UIWebViewDelegate {

    let vpaidPrefix = "vpaid2://"

    func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        var url = request.URL?.absoluteString
        if(url?.rangeOfString(vpaidPrefix) != nil){
            var event = url?.stringByReplacingOccurrencesOfString(vpaidPrefix, withString: "", options: NSStringCompareOptions.LiteralSearch, range: nil)
            self.processVpaidEvent(event!)
            return false
        }
        //return (url == "about:blank") // uncomment to stop click-thrus
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


