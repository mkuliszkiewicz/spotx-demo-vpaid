//
//  Copyright © 2015 spotx. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

  var easiViewController : UIViewController = UIViewController()
  var easiWebView : UIWebView = UIWebView()

  @IBOutlet weak var scriptDataTextField: UITextView!
  @IBOutlet weak var buttonShowAd: UIButton!
  @IBOutlet weak var urlTextField: UITextField!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    self.title = "EASI Tester"
    self.navigationItem.backBarButtonItem = UIBarButtonItem(title: "Back", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }

  @IBAction func showAdButtonPressed(sender: AnyObject) {
    easiWebView.removeFromSuperview()
    easiWebView = UIWebView(frame: CGRectMake(0, 0, UIScreen.mainScreen().bounds.width, UIScreen.mainScreen().bounds.height))
    easiWebView.allowsInlineMediaPlayback = true
    easiWebView.mediaPlaybackRequiresUserAction = false

    let html = createHtml(urlTextField.text!, scriptData: scriptDataTextField.text)
    easiWebView.loadHTMLString(html, baseURL: NSURL(string: "http://search.spotxchange.com"))

    easiViewController = UIViewController()
    easiViewController.view = easiWebView
    easiViewController.title = "EASI Ad View"
    self.navigationController?.pushViewController(easiViewController, animated: true)
  }

  private func createHtml(scriptUrl: String, scriptData: String) -> String{
    return String(format:
      "<!DOCTYPE html><head></head><body> \n" +
      "<div id=\"player\" /> \n" +
        "<script src=\"%@\" type=\"text/javascript\" %@ ></script>" +
      "</div></body></html>", scriptUrl, scriptData)
  }

  @IBAction func settingsPressed(sender: AnyObject) {
    // to do?
  }
}

