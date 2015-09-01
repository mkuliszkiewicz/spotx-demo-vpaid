package com.spotxchange.demo.vpaid.vastparser;

/**
 * Copyright (C) 2015 SpotXchange
 */
public class VPAIDResponse {
    public final String mediaUrl;
    public final String adParameters;

    public VPAIDResponse (String mediaUrl, String adParameters)
    {
        this.mediaUrl = mediaUrl;
        this.adParameters = adParameters;
    }
}
