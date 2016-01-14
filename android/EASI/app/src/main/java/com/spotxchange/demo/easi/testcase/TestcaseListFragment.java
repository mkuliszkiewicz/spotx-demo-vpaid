package com.spotxchange.demo.easi.testcase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotxchange.demo.easi.R;
import com.spotxchange.demo.easi.VideoActivity;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TestcaseListFragment extends Fragment implements OnListFragmentInteractionListener {
    private int TEST_REQUEST_CODE = 1300;
    private int mColumnCount = 1;
    private TestcaseRecyclerViewAdapter adapter;
    private OnTestsCompleteListener _completeListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TestcaseListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_testcase_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new TestcaseRecyclerViewAdapter(this);
            recyclerView.setAdapter(adapter);

            executeTestcaseRequest(adapter);
        }
        return view;
    }

    public void executeTestcaseRequest(final TestcaseRecyclerViewAdapter adapter) {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String testcaseUrl = preferences.getString(
            getString(R.string.testcase_url),
            getString(R.string.default_testcase_url)
        );

        StringRequest stringRequest = new StringRequest(testcaseUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            adapter.addTestcases(
                                    Testcase.parseTestcasesFromPictOutput(response)
                            );

                            if (preferences.getBoolean(getString(R.string.headless), true)) {
                                // Automatically run first test
                                spawnVideoActivity(0, adapter.getItem(0));
                            }
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            Toast.makeText(getActivity(), "Failed to retrieve test conditions.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(stringRequest);
    }

    private void spawnVideoActivity(int testcaseId, Testcase item)
    {
        Intent adIntent = new Intent(getActivity(), VideoActivity.class);

        // Not flagging as new task because of a bug with onActivityResult getting called immediately:
        // http://stackoverflow.com/questions/15687322/onactivityresult-being-called-instantly
        //adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        adIntent.putExtra(VideoActivity.EXTRA_SCRIPTDATA, item.scriptlet);
        adIntent.putExtra(VideoActivity.EXTRA_TESTID, testcaseId);
        this.startActivityForResult(adIntent, TEST_REQUEST_CODE);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            _completeListener = (OnTestsCompleteListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListFragmentInteraction(int position, Testcase item) {
        spawnVideoActivity(position, item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TEST_REQUEST_CODE && (resultCode == Testcase.FAILED || resultCode == Testcase.PASSED))
        {
            final int position = data.getIntExtra(VideoActivity.EXTRA_TESTID, -1);
            adapter.markTestCaseResult(position, resultCode);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


            if (preferences.getBoolean(getString(R.string.headless), true))
            {
                // Automatically run next test (if the previous test completed)
                if (position + 1 < adapter.getItemCount()) {
                    //spawnVideoActivity(position + 1, adapter.getItem(position));

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            spawnVideoActivity(position + 1, adapter.getItem(position));
                        }
                    }, 200);
                }
                else
                {
                    _completeListener.onTestsComplete(adapter.getItems());
                }
            }
        }
    }

    public interface OnTestsCompleteListener {
        public void onTestsComplete(List<Testcase> results);
    }
}


