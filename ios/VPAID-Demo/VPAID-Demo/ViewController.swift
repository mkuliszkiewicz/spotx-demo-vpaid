//
//  ViewController.swift
//  VPAID-Demo
//
//  Created by sjulian on 8/8/15.
//  Copyright (c) 2015 spotx. All rights reserved.
//

import UIKit
import Foundation
import SWXMLHash

let bundleUrlParam = "&app%5Bbundle%5D=com.spotxchange.android-vpaid-demo"
let defaultVastURL = "http://search.spotxchange.com/vast/2.00/85394?VPAID=js&app[domain]=com.spotxchange.vpaid"
let adBrokerUrl = "http://aka.spotxcdn.com/media/videos/js/ad/InstreamAdBroker_2015-07-07_14-37.debug.js"

var vpaidWebView: UIWebView = UIWebView(frame: CGRectMake(0, 50, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
var vpaidWebViewDelegate: VpaidWebViewDelegate = VpaidWebViewDelegate()

class ViewController: UIViewController {

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
        //self.setupWebView()
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
        vpaidWebView = UIWebView(frame: CGRectMake(0, 68, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
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
                self.presentViewController(refreshAlert, animated: true, completion: nil)
            }
        }
        task.resume()
    }

    private func loadVpaidWebView(adParams: String, mediaUrl: String){
        var html = self.createHtml(adParams, mediaUrl: mediaUrl)
        vpaidWebView.loadHTMLString(createHtml(adParams, mediaUrl: mediaUrl), baseURL: nil)
        self.view.addSubview(vpaidWebView)

        //
        // TODO: listen for VPAID events (but I don't want to, so here are some dispatches)
        //

        // AdOS.initAd()
        var delay = 2 * Double(NSEC_PER_SEC)
        var time = dispatch_time(DISPATCH_TIME_NOW, Int64(delay))
        dispatch_after(time, dispatch_get_main_queue()){
            vpaidWebView.stringByEvaluatingJavaScriptFromString("window.oAdOS.initAd(iContentWidth, iContentHeight, oEnvVars.media_transcoding, 0, JSON.stringify(oCreativeData), oEnvVars);");
        }

        // AdOS.startAd()
        delay = 4 * Double(NSEC_PER_SEC)
        time = dispatch_time(DISPATCH_TIME_NOW, Int64(delay))
        dispatch_after(time, dispatch_get_main_queue()){
            vpaidWebView.stringByEvaluatingJavaScriptFromString("window.oAdOS.startAd();");
        }
    }

    private func createHtml(adParams: String, mediaUrl: String) -> String{
        return String(format:
            "<html><head> \n" +
            "<script src=\"%@\" type=\"text/javascript\"></script> \n" +
            "<script type=\"text/javascript\"> \n" +
                "\t var oAdOS, iContentWidth = window.innerWidth || 320, iContentHeight = window.innerHeight || 240, strViewMode = \"normal\", oEnvVars = {  }; \n" +
                "\t var oCreativeData = %@; \n" +
                "\t if(document.readyState == \"complete\"){ window.oAdOS = getVPAIDAd(); } else { window.onload = function() { window.oAdOS = getVPAIDAd(); }}; \n" +
            "</script> \n" +
            "</head><body></body></html>", mediaUrl, adParams)
    }

}

class VpaidWebViewDelegate: NSObject, UIWebViewDelegate {
    func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        var u = request.URL?.absoluteString
        //println(u!)
        //return (u == "about:blank")
        return true
    }
}


