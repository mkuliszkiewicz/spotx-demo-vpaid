//
//  Copyright (c) 2015 SpotX. All rights reserved.
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

    /**
    *   Setup a blank Web View that allows inline media playback
    */
    private func setupWebView(){
        html5VpaidWebView.removeFromSuperview()
        html5VpaidWebView = UIWebView(frame: CGRectMake(0, 24, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
        html5VpaidWebView.allowsInlineMediaPlayback = true
        html5VpaidWebView.mediaPlaybackRequiresUserAction = false
        html5VpaidWebView.autoresizesSubviews = true
        html5VpaidWebView.scalesPageToFit = true
    }

    /**
    *   Load the Web View with our created HTML string as a source
    */
    private func loadWebView() {
        html5VpaidWebView.loadHTMLString(getHtml(), baseURL: nil)
        self.view.addSubview(html5VpaidWebView)
    }

    /**
    *   Returs a String of our HTML file 'demo.html'
    */
    private func getHtml() -> String {
        let path = NSBundle.mainBundle().pathForResource("demo", ofType: "html")
        do{
            return try String(contentsOfFile: path!, encoding: NSUTF8StringEncoding)
        }
        catch{
            return "";
        }
    }



}