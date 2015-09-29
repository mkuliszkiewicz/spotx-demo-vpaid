package com.spotxchange.demo.vpaid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Copyright (C) 2015 SpotXchange
 */
public class VASTDownloadTask extends AsyncTask<String, Integer, Integer> {

    private VASTResponseHandler _handler;

    public VASTDownloadTask(VASTResponseHandler handler) {
        _handler = handler;
    }

    @Override
    protected Integer doInBackground(String... urls ) {
        StringBuilder responseBuilder = new StringBuilder();

        try {
            URL url = new URL(urls[0]);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() >= 400) {
                _handler.onFailure(connection.getResponseMessage());
                return connection.getResponseCode();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            if (responseBuilder.length() > 0) {
                _handler.onSuccessResponse(responseBuilder.toString());
                return connection.getResponseCode();
            }
            else {
                _handler.onEmptyResponse();
                return connection.getResponseCode();
            }
        }
        catch(Exception e) {
            Log.e(VASTDownloadTask.class.getSimpleName(), e.toString());
            _handler.onFailure(e.toString());
            return -1;
        }
    }

    public interface VASTResponseHandler {
        void onSuccessResponse(String response);

        void onEmptyResponse();

        void onFailure(String response);
    }
}
