package com.spotxchange.demo.vpaid.vastparser;

import android.util.Log;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * Copyright (C) 2015 SpotXchange
 */
public class VASTParser {
    private static final String TAG = VASTParser.class.getSimpleName();

    /**
     * Parses a VAST XML response into a VPAID object.
     *
     * **** IMPORTANT NOTE TO APP DEVELOPERS ****
     * Your vast response may have additional information not represented here,
     * such as impression beacons, companion banners, multiple ads (podded ads), etc.
     *
     * @param xmlSource
     * @return
     * @throws XmlPullParserException
     */
    public static VPAIDResponse read(InputSource xmlSource) {
        XPath xpath = XPathFactory.newInstance().newXPath();

        String mediaUrl = null;
        String adParameters = null;

        // try to pull put the media source file and the associated ad pararmeters
        try {
            // find the linear root
            Node linear = (Node) xpath.evaluate("/VAST/Ad/InLine/Creatives/Creative/Linear", xmlSource, XPathConstants.NODE);
            // get the media source file
            mediaUrl = xpath.evaluate("MediaFiles/MediaFile", linear);
            // get the ad aparamets
            adParameters = xpath.evaluate("AdParameters", linear);
        }
        catch(XPathExpressionException e) {
            // oops, bad VAST, XML, or whatever
            Log.d(TAG, "Received invalid VAST response.");
            return null;
        }

        return new VPAIDResponse(mediaUrl, adParameters);
    }

}
