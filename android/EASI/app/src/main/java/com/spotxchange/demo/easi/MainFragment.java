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
public class MainFragment extends Fragment {
    Button _showButton;
    View _view;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_main, container, false);

        // Creates and shows an EASI interstitial when clicked.
        _showButton = ((Button) _view.findViewById(R.id.show_button));
        _showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainFragment.this.launchVideoActivity();
            }
        });

        return _view;
    }


    private void launchVideoActivity() {
        _showButton.setEnabled(false);
        Intent adIntent = new Intent(getActivity(), VideoActivity.class);
        adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String input = ((EditText) _view.findViewById(R.id.target)).getText().toString();
        adIntent.putExtra(VideoActivity.EXTRA_SCRIPTDATA, input);
        startActivity(adIntent);
    }

}
