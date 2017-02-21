/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/eband4 for more book information.
 ***/
package edu.neu.madcourse.duyvu.wordgame;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.neu.madcourse.duyvu.R;

import static android.content.Context.MODE_PRIVATE;
import static edu.neu.madcourse.duyvu.wordgame.WGGameActivity.KEY_RESTORE;
import static edu.neu.madcourse.duyvu.wordgame.WGGameActivity.PREF_RESTORE;
import static edu.neu.madcourse.duyvu.wordgame.WGGameActivity.SOUND_VOLUME;
import static edu.neu.madcourse.duyvu.wordgame.WGMainActivity.NORMAL_VOLUME;

public class WGMainFragment extends Fragment {

    private AlertDialog mDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.wgfragment_main, container, false);
        // Handle buttons here...
        View newButton = rootView.findViewById(R.id.wgnew_button);
        View continueButton = rootView.findViewById(R.id.wgcontinue_button);
        View aboutButton = rootView.findViewById(R.id.wgabout_button);
        final Button soundButton = (Button)rootView.findViewById(R.id.wgsound_button);
        View acknowledgeButton = rootView.findViewById(R.id.wgacknowledge_button);
        View quitButton = rootView.findViewById(R.id.wgquit_button);

        if (((WGMainActivity) getActivity()).getVolume() != 0) {
            soundButton.setText("SOUND: ON");
        } else {
            soundButton.setText("SOUND: OFF");
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String gameData = sharedPreferences.getString(PREF_RESTORE, null);

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WGGameActivity.class);
                intent.putExtra(SOUND_VOLUME, ((WGMainActivity) getActivity()).getVolume());
                getActivity().startActivity(intent);
            }
        });
        acknowledgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("1) I have not use any additional images/icons/graphics for this assignment\n\n2)  No outside code\n\n3)  No additional help from the slides and piazza");
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // nothing
                            }
                        });
                mDialog = builder.show();
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WGGameActivity.class);
                intent.putExtra(KEY_RESTORE, true);
                intent.putExtra(SOUND_VOLUME, ((WGMainActivity) getActivity()).getVolume());
                getActivity().startActivity(intent);
            }
        });
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float volume = ((WGMainActivity) getActivity()).getVolume();
                if (volume != 0) {
                    soundButton.setText("SOUND: OFF");
                    ((WGMainActivity) getActivity()).setVolume(0);
                } else {
                    soundButton.setText("SOUND: ON");
                    ((WGMainActivity) getActivity()).setVolume(NORMAL_VOLUME);
                }
            }
        });
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.wgabout_text);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // nothing
                            }
                        });
                mDialog = builder.show();
            }
        });
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Get rid of the about dialog if it's still up
        if (mDialog != null)
            mDialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean restore = sharedPreferences.getBoolean(KEY_RESTORE, false);
        Button continueButton = (Button)getView().findViewById(R.id.wgcontinue_button);
        if (!restore) {
            continueButton.setVisibility(View.GONE);
        } else {
            continueButton.setVisibility(View.VISIBLE);
        }
    }
}
