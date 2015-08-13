//
//  ViewController.swift
//  VPAID-Demo
//
//  Created by sjulian on 8/8/15.
//  Copyright (c) 2015 spotx. All rights reserved.
//

import UIKit
import SWXMLHash

let vastURL = "http://search.spotxchange.com/vast/2.00/85394?VPAID=js&prefetch=0&autoinit=1&autoplay=1&cb=1438140245814&mutable=0&app%5Bsdkversion%5D=2.1%20(2015072821)&device%5Bdpidsha1%5D=79aae48c-19c6-4ca0-a4a1-adbf160d9684&app%5Bname%5D=SpotX%20SDK%20Demo&app%5Bversion%5D=2.1&app%5Bbundle%5D=com.spotxchange.demo&app%5Bdomain%5D=com.spotxchange.demo&device%5Bconnectiontype%5D=2&device%5Bos%5D=Android&device%5Bosv%5D=5.0.2&device%5Bmake%5D=samsung&device%5Bmodel%5D=SM-G920T&device%5Bdevicetype%5D=1&device%5Bgeo%5D%5Blat%5D=0.0&device%5Bgeo%5D%5Blong%5D=0.0&device%5Bgeo%5D%5Bcountry%5D=USA&device%5Bip%5D=fe80%3A%3Ae850%3A8bff%3Afe32%3A43fb%25p2p0&device%5Bcarrier%5D=310260"

let adBrokerUrl = "http://aka.spotxcdn.com/media/videos/js/ad/InstreamAdBroker_2015-07-07_14-37.debug.js";
//let adBrokerUrl = "http://deals.lod.search.spotxchange.com/ad_player/listing_type/5.js"

var vpaidWebView: UIWebView = UIWebView(frame: CGRectMake(0, 50, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    @IBAction func btnLaunchWebView(sender: AnyObject) {
        // TODO: not hardcode VAST url
        let vurl = NSURL(string: vastURL)
        let task = NSURLSession.sharedSession().dataTaskWithURL(vurl!) {(data, response, error) in
            //println(NSString(data: data, encoding: NSUTF8StringEncoding))
            var xmlData = NSString(data: data, encoding: NSUTF8StringEncoding)
            let xml = SWXMLHash.parse(data)
            var mediaFramework = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["MediaFiles"]["MediaFile"][0].element?.attributes["apiFramework"]
            var mediaUrl = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["MediaFiles"]["MediaFile"][0].element?.text
            var adParams = xml["VAST"]["Ad"]["InLine"]["Creatives"]["Creative"]["Linear"]["AdParameters"].element?.text

            //TODO: check if mediaFramework == VPAID
            self.loadTheWebView(adParams!, mediaUrl: mediaUrl!)

        }
        task.resume()
        vpaidWebView.loadHTMLString("<html><head></head><body>loading...</body></html>", baseURL: nil)
        self.view.addSubview(vpaidWebView)
    }

    func loadTheWebView(adParams: String, mediaUrl: String){
        var html = self.createHtml(adParams, mediaUrl: mediaUrl)
        //println(html)
        vpaidWebView.loadHTMLString(createHtml(adParams, mediaUrl: mediaUrl), baseURL: nil)
        self.view.addSubview(vpaidWebView)

        let delay = 2 * Double(NSEC_PER_SEC)
        let delay2 = 4 * Double(NSEC_PER_SEC)
        var time = dispatch_time(DISPATCH_TIME_NOW, Int64(delay))
        dispatch_after(time, dispatch_get_main_queue()){
            self.initAd();
        }

        time = dispatch_time(DISPATCH_TIME_NOW, Int64(delay2))
        dispatch_after(time, dispatch_get_main_queue()){
            self.startAd();
        }

    }

    func initAd(){
        println("INITAD")
        vpaidWebView.stringByEvaluatingJavaScriptFromString("window.oAdOS.initAd(iContentWidth, iContentHeight, oEnvVars.media_transcoding, 0, JSON.stringify(oCreativeData), oEnvVars);");
    }

    func startAd(){
        println("STARTAD")
        vpaidWebView.stringByEvaluatingJavaScriptFromString("window.oAdOS.startAd();");
    }

    func createHtml(adParams: String, mediaUrl: String) -> String{
        return String(format:
            "<html><head> \n" +
            "<script src=\"%@\" type=\"text/javascript\"></script> \n" +
            "<script type=\"text/javascript\"> \n" +
                "var oAdOS, iContentWidth = window.innerWidth || 320, iContentHeight = window.innerHeight || 240, strViewMode = \"normal\", oEnvVars = {}; \n" +
                "var oCreativeData = %@; \n" +
                "if(document.readyState == \"complete\"){ window.oAdOS = getVPAIDAd(); } else { window.onload = function() { window.oAdOS = getVPAIDAd(); }}; \n" +
            "</script>" +
            "</head> " +
            "<body></body></html>", mediaUrl, adParams)
    }

}

