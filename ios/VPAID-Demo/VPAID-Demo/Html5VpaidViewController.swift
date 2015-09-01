//
//  Html5VpaidViewController.swift
//  VPAID-Demo
//
//  Created by scottjulian on 9/1/15.
//  Copyright (c) 2015 spotxchange. All rights reserved.
//

import UIKit
import Foundation

var html5VpaidWebView: UIWebView = UIWebView(frame: CGRectMake(0, 24, UIScreen.mainScreen().bounds.width - 8, UIScreen.mainScreen().bounds.height))

class Html5VpaidViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        setupWebView()
        loadWebView()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    private func setupWebView(){
        html5VpaidWebView.removeFromSuperview()
        html5VpaidWebView = UIWebView(frame: CGRectMake(0, 24, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
        html5VpaidWebView.allowsInlineMediaPlayback = true
        html5VpaidWebView.mediaPlaybackRequiresUserAction = false
        html5VpaidWebView.autoresizesSubviews = true
        html5VpaidWebView.scalesPageToFit = true
    }

    private func loadWebView() {
        html5VpaidWebView.loadHTMLString(getHtml(), baseURL: nil)
        self.view.addSubview(html5VpaidWebView)
    }

    private func getHtml() -> String {
        let path = NSBundle.mainBundle().pathForResource("demo", ofType: "html")
        return String(contentsOfFile: path!, encoding: NSUTF8StringEncoding, error: nil)!
    }



}