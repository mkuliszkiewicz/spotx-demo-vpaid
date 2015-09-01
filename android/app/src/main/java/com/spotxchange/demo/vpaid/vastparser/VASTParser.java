package com.spotxchange.demo.vpaid.vastparser;

import android.util.Log;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.HashMap;
import java.util.Map;

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
     * IMPORTANT NOTE TO APP DEVELOPERS: Your vast response may have additional information not represented here,
     * such as impression beacons, companion banners, multiple ads (podded ads), etc.
     * @param xmlSource
     * @return
     * @throws XmlPullParserException
     */
    public static VPAIDResponse read(InputSource xmlSource)
    {
        XPath xpath = XPathFactory.newInstance().newXPath();

        String mediaUrl = null;
        String adParameters = null;

        try
        {
            Node linear = (Node) xpath.evaluate("/VAST/Ad/InLine/Creatives/Creative/Linear", xmlSource, XPathConstants.NODE);
            //mediaUrl = xpath.evaluate("/VAST/Ad[0]/InLine/Creatives/Creative[0]/Linear/MediaFiles/MediaFile[0]", xmlSource);
            mediaUrl = xpath.evaluate("MediaFiles/MediaFile", linear);
            adParameters = xpath.evaluate("AdParameters", linear);
        }
        catch(XPathExpressionException e) {
            Log.d(TAG, "Received invalid VAST response.");
            return null;
        }

        return new VPAIDResponse(mediaUrl, adParameters);
    }

    private static boolean isVpaidResponseValid(VPAIDResponse vpaid)
    {
        return vpaid.mediaUrl != null && vpaid.adParameters != null;
    }
}
