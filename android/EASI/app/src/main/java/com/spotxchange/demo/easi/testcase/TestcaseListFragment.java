package com.spotxchange.demo.easi.testcase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TestcaseListFragment extends Fragment implements OnListFragmentInteractionListener {
    private int TEST_REQUEST_CODE = 1300;
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private TestcaseRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TestcaseListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
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
            adapter = new TestcaseRecyclerViewAdapter(mListener);
            recyclerView.setAdapter(adapter);

            executeTestcaseRequest(adapter);
        }
        return view;
    }

    public void executeTestcaseRequest(final TestcaseRecyclerViewAdapter adapter) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String testcaseUrl = preferences.getString(
            getString(R.string.testcase_url),
            getString(R.string.default_testcase_url)
        );

        StringRequest stringRequest = new StringRequest(testcaseUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        adapter.addTestcases(
                                Testcase.parseTestcasesFromPictOutput(response)
                        );
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Failed to retrieve test conditions.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(stringRequest);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (this instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) this;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(int position, Testcase item) {
        Intent adIntent = new Intent(getActivity(), VideoActivity.class);

        // Not flagging as new task because of a bug with onActivityResult getting called immediately:
        // http://stackoverflow.com/questions/15687322/onactivityresult-being-called-instantly
        //adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        adIntent.putExtra(VideoActivity.EXTRA_SCRIPTDATA, item.scriptlet);
        adIntent.putExtra(VideoActivity.EXTRA_TESTID, position);
        this.startActivityForResult(adIntent, TEST_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TEST_REQUEST_CODE) {
            adapter.markTestCaseResult(data.getIntExtra(VideoActivity.EXTRA_TESTID, -1), resultCode);
        }
    }
}


