package com.spotxchange.demo.easi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.spotxchange.demo.easi.dummy.DummyContent;

/**
 * Copyright (C) 2015 SpotXchange
 */
public class MainActivity extends Activity implements TestcaseListFragment.OnListFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction()
                //.replace(R.id.fragment_container, new MainFragment())
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
    public void onListFragmentInteraction(DummyContent.Testcase item) {
        Intent adIntent = new Intent(this, VideoActivity.class);
        adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        adIntent.putExtra(VideoActivity.EXTRA_SCRIPTDATA, item.scriptlet);
        startActivity(adIntent);
    }
}
