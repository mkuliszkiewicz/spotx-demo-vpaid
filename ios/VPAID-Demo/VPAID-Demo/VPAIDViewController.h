//
//  Copyright (c) 2015 SpotX, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol VPAIDViewControllerDelegate;

@interface VPAIDViewController : UIViewController


@property (nonatomic, weak) id<VPAIDViewControllerDelegate> delegate;

@property (nonatomic, copy) NSString *channelID;

@property (nonatomic, copy) NSString *domain;

@property (nonatomic, assign) BOOL secure;


- (void)startLoading;

- (void)startAd;

- (void)stopAd;

@end



@protocol VPAIDViewControllerDelegate <NSObject>

- (void)vpaidViewController:(VPAIDViewController *)viewController didReceiveEvent:(NSString *)event;

@end