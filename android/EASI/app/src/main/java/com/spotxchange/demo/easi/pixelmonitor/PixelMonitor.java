package com.spotxchange.demo.easi.pixelmonitor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class PixelMonitor {
    private static String TAG = "PixelMonitor";
    private long _lastPixelChecksum = -1;
    private View _view;
    private int _changeCount = 0;
    private int _changeThreshold;
    private int _unchangedThreshold;
    private OnPixelChangedListener _listener;

    /**
     * Default PixelMonitor. Triggers on default threshold.
     *
     * @param view The view whose pixel color will be monitored
     */
    public PixelMonitor(View view, OnPixelChangedListener listener) {
        this(view, listener, 5, 15);
    }

    /**
     * PixelMonitor. Trigger onPixelChangedListener.OnPixelChanged when the pixels have changed
     * changeThreshold times.
     *
     * @param view
     * @param changeThreshold Succeed after this many rounds of pixels changing
     * @param unchangedThreshold Fail after this many rounds of pixels not changing
     */
    public PixelMonitor(View view, OnPixelChangedListener listener, int changeThreshold, int unchangedThreshold) {
        _view = view;
        _listener = listener;
        _changeThreshold = changeThreshold;
        _unchangedThreshold = unchangedThreshold;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hasPixelsChanged()) {
                    _changeThreshold--;
                }
                else {
                    _unchangedThreshold--;
                }

                if (0 >= _changeThreshold) {
                    _listener.onPixelChangedThreshold();
                } else if (0 >= _unchangedThreshold)
                {
                    _listener.onPixelUnchangedThreshold();
                }
                else if (_view.isShown()) {
                    handler.postDelayed(this, 1000);
                }
                // Else: Stop if detached.
            }
        }, 200);
    }

    /**
     * Sums up the color values for a set of pixels in the view.
     * If the sum of those values has changed, fire OnPixelChangedListener.OnPixelChanged
     */
    private boolean hasPixelsChanged() {
        long pixelChecksum = 0;
        int width = _view.getWidth();
        int height = _view.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(
                _view.getWidth(),
                _view.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        _view.draw(canvas);

        int[] pixels = new int[width * height];

        //TODO: Optimize this by only grabbing subregions of the view.
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int pixel : pixels) {
            pixelChecksum += pixel;
        }

        boolean hasChanged;
        if (_lastPixelChecksum != -1 && pixelChecksum != _lastPixelChecksum) {
            Log.d(PixelMonitor.TAG, "Pixel colors changed.");
            hasChanged = true;
        } else {
            Log.d(PixelMonitor.TAG, String.format("No change in pixel checksum (area checked: [0~%1$d]x[0~%2$d])", width, height));
            hasChanged = false;
        }

        _lastPixelChecksum = pixelChecksum;
        return hasChanged;
    }

    public interface OnPixelChangedListener {
        public void onPixelChangedThreshold();
        public void onPixelUnchangedThreshold();
    }
}
