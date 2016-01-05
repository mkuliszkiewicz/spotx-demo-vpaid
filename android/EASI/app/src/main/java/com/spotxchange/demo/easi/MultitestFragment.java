package com.spotxchange.demo.easi;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultitestFragment extends Fragment {
    private static String ARG_SCRIPTLET = "scriptlet";

    public MultitestFragment() {
        // Required empty public constructor
    }

    public static MultitestFragment newInstance(String scriptlet)
    {
        MultitestFragment f = new MultitestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCRIPTLET, scriptlet);

        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multitest, container, false);

        // Creates and shows an EASI interstitial immediately.
        launchVideoActivity(
            getArguments().getString(ARG_SCRIPTLET)
            );

        return view;
    }

    @Override
    public void onStart () {
        super.onStart();
    }

    private void launchVideoActivity(String scriptlet) {
        Intent adIntent = new Intent(getActivity(), VideoActivity.class);
        adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        adIntent.putExtra(VideoActivity.EXTRA_SCRIPTDATA, scriptlet);
        startActivity(adIntent);
    }

}
