package com.spotxchange.demo.easi;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserInputFragment extends Fragment {
    Button _showButton;

    public UserInputFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_input, container, false);

        // Creates and shows an EASI interstitial when clicked.
        _showButton = ((Button) view.findViewById(R.id.show_button));
        _showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInputFragment.this.launchVideoActivity(
                    ((EditText) view.findViewById(R.id.target)).getText().toString()
                    );
            }
        });

        return view;
    }

    @Override
    public void onStart () {
        super.onStart();

        // always enable the show button when the fragment initially becomes visible
        _showButton.setEnabled(true);
    }
    
    private void launchVideoActivity(String scriptlet) {
        _showButton.setEnabled(false);
        Intent adIntent = new Intent(getActivity(), VideoActivity.class);
        adIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        adIntent.putExtra(VideoActivity.EXTRA_SCRIPTDATA, scriptlet);
        startActivity(adIntent);
    }

}
