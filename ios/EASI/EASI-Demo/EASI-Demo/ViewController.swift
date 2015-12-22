//
//  Copyright © 2015 spotx. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

  var easiViewController : UIViewController = UIViewController()
  var easiWebView : UIWebView = UIWebView()
  var easiDelegate = EasiWebViewDelegate()

  var visibleWidth:CGFloat = 0
  var visibleHeight:CGFloat = 0

  @IBOutlet weak var scriptDataTextField: UITextView!
  @IBOutlet weak var buttonShowAd: UIButton!
  @IBOutlet weak var urlTextField: UITextField!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    self.title = "EASI Tester"
    self.navigationItem.backBarButtonItem = UIBarButtonItem(title: "Back", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)

    visibleWidth = UIScreen.mainScreen().bounds.width
    visibleHeight = UIScreen.mainScreen().bounds.height - (self.navigationController?.navigationBar.frame.size.height)! - UIApplication.sharedApplication().statusBarFrame.size.height

    self.scriptDataTextField.text = getDefaultScriptData()
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }

  @IBAction func showAdButtonPressed(sender: AnyObject) {
    easiWebView.removeFromSuperview()
    easiWebView = UIWebView(frame: CGRectMake(0, 0, visibleWidth, visibleHeight))
    easiWebView.allowsInlineMediaPlayback = true
    easiWebView.mediaPlaybackRequiresUserAction = false

    easiDelegate = EasiWebViewDelegate()
    easiWebView.delegate = easiDelegate

    let html = createHtml(urlTextField.text!, scriptData: scriptDataTextField.text)
    easiWebView.loadHTMLString(html, baseURL: NSURL(string: "http://search.spotxchange.com"))

    easiViewController = UIViewController()
    easiViewController.view = easiWebView
    easiViewController.title = "EASI Ad View"

    self.navigationController?.pushViewController(easiViewController, animated: true)
  }

  private func createHtml(scriptUrl: String, scriptData: String) -> String{
    return String(format:
      "<!DOCTYPE html><head></head><body style=\"margin:0; padding:0;\"> \n" +
      "<div id=\"player\" /> \n" +
        "<script src=\"%@\" type=\"text/javascript\" %@ ></script>" +
      "</div></body></html>", scriptUrl, scriptData)
  }

  private func getDefaultScriptData() -> String {
    let w = String(stringInterpolationSegment: visibleWidth)
    let h = String(stringInterpolationSegment: visibleHeight)
    return String(format: "data-spotx_channel_id=\"85394\" \n" +
      "data-spotx_content_width=\"%@\" \n" +
      "data-spotx_content_height=\"%@\" \n" +
      "data-spotx_ad_unit=\"incontent\" \n" +
      "data-spotx_content_type=\"game\" \n" +
      "data-spotx_content_page_url=\"http://spotx.ninja\" \n" +
      "data-spotx_app_bundle=\"com.spotx.ios.easi\" \n" +
      "data-spotx_device_ifa=\"unknown\" \n" +
      "data-spotx_autoplay=\"1\" \n" +
      "data-spotx_content_container_id=\"player\" \n" +
      "data-spotx_video_slot_can_autoplay=\"1\" \n", w, h)
  }

  @IBAction func settingsPressed(sender: AnyObject) {
    // to do?
  }

}


class EasiWebViewDelegate: NSObject, UIWebViewDelegate {

  var finishedLoading:Bool = false

  func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
    if(finishedLoading){
      UIApplication.sharedApplication().openURL(request.URL!)
      return false
    }
    return true
  }

  func webViewDidFinishLoad(webView: UIWebView) {
    finishedLoading = true
  }

  func webViewDidStartLoad(webView: UIWebView) {
    finishedLoading = false
  }

}
