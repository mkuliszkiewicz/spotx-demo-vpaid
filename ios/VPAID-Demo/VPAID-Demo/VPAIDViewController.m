//
//  Copyright (c) 2015 SpotX, Inc. All rights reserved.
//

#import "VPAIDViewController.h"
#import <AdSupport/AdSupport.h>
#import <JavaScriptCore/JavaScriptCore.h>

@interface VPAIDViewController () <UIWebViewDelegate>

@end

@implementation VPAIDViewController {
    UIWebView *_webview;
    BOOL _loaded;
    BOOL _started;
    BOOL _visible;
}

- (void)loadView
{
    _webview = [[UIWebView alloc] initWithFrame:[UIScreen mainScreen].bounds];
    _webview.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    _webview.translatesAutoresizingMaskIntoConstraints = YES;
    _webview.scalesPageToFit = NO;
    _webview.mediaPlaybackRequiresUserAction = NO;
    _webview.backgroundColor = [UIColor blackColor];
    _webview.scrollView.bounces = NO;
    _webview.delegate = self;
    self.view = _webview;

    UIButton *button = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    [button addTarget:self action:@selector(dismiss) forControlEvents:UIControlEventTouchUpInside];
    button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    [button setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [button setTitle:@"X - CLOSE" forState:UIControlStateNormal];
    button.frame = CGRectMake(16, 16, 100, 50);
    [_webview addSubview:button];
}

- (void)dismiss
{
    [self.presentingViewController dismissViewControllerAnimated:NO completion:nil];
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    id log = ^(JSValue *msg) {
        NSLog(@"%@", msg);
    };

    JSContext *ctx = [_webview valueForKeyPath:@"documentView.webView.mainFrame.javaScriptContext"];
    ctx[@"console"][@"log"] = log;
    ctx[@"console"][@"info"] = log;
    ctx[@"console"][@"debug"] = log;
    ctx[@"console"][@"error"] = log;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    _visible = YES;
    [self startAd];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    _visible = NO;
    [self stopAd];
}

- (void)startLoading
{
    [self view];

    NSString *advertiserID = [[[ASIdentifierManager sharedManager] advertisingIdentifier] UUIDString];
    NSString *bundleID = [[NSBundle mainBundle] bundleIdentifier];

    NSString *baseUrl = [@"http://nameless-tundra-9674.herokuapp.com/vpaid/" stringByAppendingPathComponent:_channelID];

    NSArray *query = @[
                       [@"app.domain=" stringByAppendingString:_domain],
                       [@"app.bundle=" stringByAppendingString:bundleID],
                       [@"device.idfa=" stringByAppendingString:advertiserID],
                       @"events=1",
                       @"autoplay=0"
                       ];

    NSURLComponents *url = [NSURLComponents componentsWithString:baseUrl];
    url.scheme = _secure ? @"https" : @"http";
    url.query = [query componentsJoinedByString:@"&"];

    NSURLRequest *request = [NSURLRequest requestWithURL:url.URL
                                             cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                         timeoutInterval:5.0];

    NSLog(@"Loading URL: %@", request.URL);
    [_webview loadRequest:request];
}

- (void)startAd
{
    if (_loaded && _visible) {
        if (_started) {
            [_webview stringByEvaluatingJavaScriptFromString:@"vpaid.resumeAd();"];
        }
        else {
            [_webview stringByEvaluatingJavaScriptFromString:@"vpaid.startAd();"];
            _started = true;
        }
    }
}

- (void)stopAd
{
    if (_loaded && _started) {
        [_webview stringByEvaluatingJavaScriptFromString:@"vpaid.pauseAd();"];
    }
}


- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    if ([request.URL.scheme isEqualToString:@"vpaid"]) {
        NSString *event = request.URL.host;
        [self performSelector:@selector(vpaidEvent:) withObject:event afterDelay:0.3];
        return NO;
    }

    return YES;
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{

}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    
}

- (void)vpaidEvent:(NSString *)event
{
    NSLog(@"EVENT: %@", event);
    
    if ([event isEqualToString:@"AdLoaded"]) {
        _loaded = YES;
        [self performSelector:@selector(startAd) withObject:nil afterDelay:0.5];
    }
    
    [_delegate vpaidViewController:self didReceiveEvent:event];
}

@end