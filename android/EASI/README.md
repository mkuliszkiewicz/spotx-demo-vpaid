# SpotX VPAID Demo APP
Testing app for SpotX's EASI integration in Android WebView.


## How to use SpotX EASI inside a WebView

Load the appropriately formatted `<script>` tag inside the `<body>` tag of your webview.

```html
<script src="%1$s" type="text/javascript"
        data-spotx_ad_unit="incontent" data-spotx_channel_id="85394" data-spotx_content_type="game"
        data-spotx_content_width="300" data-spotx_content_height="250"
        data-spotx_content_page_url="http://spotx.ninja"
        data-spotx_app_bundle="com.spotx.ninja.demoapp"
        data-spotx_device_ifa="unknown"
        data-spotx_autoplay="1"
        data-spotx_content_container_id="player"
        data-spotx_video_slot_can_autoplay="1"
        %2$s
        ></script>
```

See the documentation on EASI script formatting for details on what data is appropriate for your app.

## Resources

* [SpotX Tag Generator](https://www.spotxchange.com/tag-generator/)
