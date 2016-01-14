package com.spotxchange.demo.easi;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotxchange.demo.easi.testcase.Testcase;
import com.spotxchange.demo.easi.testcase.TestcaseListFragment;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 SpotXchange
 */
public class MainActivity extends Activity implements TestcaseListFragment.OnTestsCompleteListener {
    private int NOTIFICATION_ID = 108;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if has intent (i.e. launched from command line or headless app tool

        Log.d("Main", "launching activity");
        if (getIntent().getExtras() != null) {
            Log.d("Main", String.format("got extras %1$s", getIntent().getExtras().getBoolean(getString(R.string.headless), false)));
            SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            if (getIntent().getExtras().getBoolean(getString(R.string.headless), false)) {
                Log.d("Main", "got headless");
                preferencesEditor
                        .putBoolean(getString(R.string.headless), true);
            }

            String webhook = getIntent().getExtras().getString(getString(R.string.results_webhook), "");
            if (!webhook.isEmpty()) {
                preferencesEditor
                        .putString(getString(R.string.results_webhook), webhook);
            }

            preferencesEditor.apply();
        }


        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction()
                //.replace(R.id.fragment_container, new UserInputFragment())
                .replace(R.id.fragment_container, new TestcaseListFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTestsComplete(final List<Testcase> results) {
        // TODO: Spawn notification to email / post results somewhere?
        Toast.makeText(this, "All test cases completed.", Toast.LENGTH_LONG).show();

        String postbackUrl = "http://irc.hq.booyahnetworks.com";

        StringRequest postResultsRequest = new StringRequest(Request.Method.POST, postbackUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("MainActivity", "Firing results notification for " + response);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response));
                        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this)
                                .setContentTitle("EASI Tests Completed")
                                .setContentText("Click to see results")
                                .setSmallIcon(R.drawable.notification_template_icon_bg)
                                .setContentIntent(contentIntent);

                        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(NOTIFICATION_ID, builder.build());
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity", String.format("Failure to post results: %1$s", error.toString()));
                    }
                })
        {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String resultsUrl = response.headers.get("Location");
                if (resultsUrl != null && !resultsUrl.isEmpty())
                {
                    return Response.success(resultsUrl, HttpHeaderParser.parseCacheHeaders(response));
                }

                return Response.error(new VolleyError(response));
            }

            @Override
            public void deliverError(VolleyError error) {
                String resultsUrl = error.networkResponse.headers.get("Location");
                if (resultsUrl != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_MOVED_TEMP)
                {
                    deliverResponse(resultsUrl);
                }
                else {
                    super.deliverError(error);
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("content", results.toString());
                params.put("lexer", "js");
                params.put("expire_options", "3600");

                params.put("title", String.format("EASI Android Automaton %1$s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")));
                //TODO: author from google id

                String encodedParams = "";
                for (Map.Entry<String, String> row : params.entrySet())
                {
                    encodedParams += String.format("%1$s=%2$s&", row.getKey(), row.getValue());
                }

                return encodedParams.getBytes();
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this, new HurlStack()
        {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpURLConnection conn = super.createConnection(url);
                conn.setInstanceFollowRedirects(false);
                return conn;
            }
        });

        queue.add(postResultsRequest);

        //Intent intent = new Intent(this, Uri.parse(resultsUrl));
    }
}
