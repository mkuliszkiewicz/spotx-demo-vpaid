//
//  Copyright (c) 2015 SpotX. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "VPAIDViewController.h"

@interface ServerVpaidViewController : UIViewController <VPAIDViewControllerDelegate>

@property (nonatomic, weak) IBOutlet UITextField *channel;

@property (nonatomic, weak) IBOutlet UISwitch *secure;

- (IBAction)playAd:(id)sender;

@end