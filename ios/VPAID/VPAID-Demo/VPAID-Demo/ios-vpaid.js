var oAdOS, iContentWidth = window.innerWidth || 320, iContentHeight = window.innerHeight || 240, strViewMode = "normal", oEnvVars = { "in-app":true };
var oCreativeData = %@;

function attachIframe(srcName){
    var iframe = document.createElement('iframe');
    iframe.id = "ios-vpaid-event-iframe-" + srcName;
    iframe.style.display = "none";
    iframe.src = "vpaid2://" + srcName;
    document.body.appendChild(iframe);
}

if(document.readyState == "complete"){
    window.oAdOS = getVPAIDAd();
    oAdOS.subscribe(function(){ attachIframe("ad_loaded"); },  "AdLoaded", null);
    oAdOS.subscribe(function(){ attachIframe("ad_started"); }, "AdStarted", null);
    oAdOS.subscribe(function(){ attachIframe("ad_paused"); },  "AdPaused", null);
    oAdOS.subscribe(function(){ attachIframe("ad_stopped"); }, "AdStopped", null);
    oAdOS.subscribe(function(){ attachIframe("ad_error"); },   "AdError", null);
    oAdOS.subscribe(function(){ attachIframe("ad_clicked"); }, "AdClickThru", null);
    window.oAdOS.initAd(iContentWidth, iContentHeight, oEnvVars.media_transcoding, 0, JSON.stringify(oCreativeData), oEnvVars);
}
else{
    window.onload = function() {
        window.oAdOS = getVPAIDAd();
        oAdOS.subscribe(function(){ attachIframe("ad_loaded");  },  "AdLoaded", null);
        oAdOS.subscribe(function(){ attachIframe("ad_started"); }, "AdStarted", null);
        oAdOS.subscribe(function(){ attachIframe("ad_paused"); },  "AdPaused", null);
        oAdOS.subscribe(function(){ attachIframe("ad_stopped"); }, "AdStopped", null);
        oAdOS.subscribe(function(){ attachIframe("ad_error"); },   "AdError", null);
        oAdOS.subscribe(function(){ attachIframe("ad_clicked"); }, "AdClickThru", null);
        window.oAdOS.initAd(iContentWidth, iContentHeight, oEnvVars.media_transcoding, 0, JSON.stringify(oCreativeData), oEnvVars);
    }
}
