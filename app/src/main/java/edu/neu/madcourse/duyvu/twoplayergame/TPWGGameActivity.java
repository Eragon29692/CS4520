/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/eband4 for more book information.
 ***/
package edu.neu.madcourse.duyvu.twoplayergame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

import static edu.neu.madcourse.duyvu.wordgame.WGMainActivity.NORMAL_VOLUME;

public class TPWGGameActivity extends AppCompatActivity {
    public static final String KEY_RESTORE = "key_restore";
    public static final String SOUND_VOLUME = "sound_volume";
    public static final String PREF_RESTORE = "pref_restore";
    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private TPWGGameFragment mGameFragment;
    private int scoreDisplay = 0;
    public Globals dictionary;
    SharedPreferences sharedPreferences;
    private float soundVolume = NORMAL_VOLUME;
    private Handler animationHandler = new Handler();
    private DatabaseReference mDatabase;
    Runnable animationTimer = new Runnable() {
        public void run() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tpwgactivity_game);
        dictionary = (Globals) getApplication();
        mGameFragment = (TPWGGameFragment) getFragmentManager()
                .findFragmentById(R.id.tpwgfragment_game);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        soundVolume = getIntent().getFloatExtra(SOUND_VOLUME, NORMAL_VOLUME);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (restore) {
            String gameData = sharedPreferences.getString(TPWGGameActivity.PREF_RESTORE, null);
            if (gameData != null && !gameData.equals("") && restore) {
                mGameFragment.putState(gameData);
            }
        }
        Log.d("UT3", "restore = " + restore);
    }

    protected float getVolume() {
        return soundVolume;
    }

    public void displayScore(String score) {
        TextView scorePanel = (TextView) findViewById(R.id.tpwgactivity_game_score);
        scorePanel.setText("Score: " + score);
    }

    public void displayTime(int time) {
        TextView timePanel = (TextView) findViewById(R.id.tpwgactivity_timer);
        timePanel.setText("Time: " + Integer.toString(time));
    }

    public void displayWord(String word) {
        TextView scorePanel = (TextView) findViewById(R.id.tpwgactivity_current_word);
        scorePanel.setText(word);
    }

    public void onClickPausedButton(View view)
    {
        Intent intent = new Intent(TPWGGameActivity.this, TPWGPausedActivity.class);
        intent.putExtra(SOUND_VOLUME, soundVolume);
        finish();
        startActivity(intent);
    }

    public void animationForTimer(int time) {
        TextView timerPanel = (TextView) findViewById(R.id.tpwgactivity_timer);
        TimerRunnable timerRunnable = new TimerRunnable(time, timerPanel, animationHandler);
        animationHandler.postDelayed(timerRunnable, 1000);
    }

    public void restartGame() {
        mGameFragment.restartGame();
    }

    public void reportWinner(final TPWGTile.Owner winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
        builder.setMessage(getString(R.string.declare_winner, winner));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        final Dialog dialog = builder.create();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMediaPlayer = MediaPlayer.create(TPWGGameActivity.this,
                        winner == TPWGTile.Owner.X ? R.raw.oldedgar_winner
                                : winner == TPWGTile.Owner.O ? R.raw.notr_loser
                                : R.raw.department64_draw
                );
                mMediaPlayer.setVolume(soundVolume, soundVolume);
                mMediaPlayer.start();
                dialog.show();
            }
        }, 500);

        // Reset the board to the initial position
        mGameFragment.initGame();
    }

    public void startThinking() {
        View thinkView = findViewById(R.id.tpwgthinking);
        View thinkViewBar = findViewById(R.id.tpwgthinkingProgressBar);
        thinkViewBar.setBackgroundResource(R.drawable.thinking_background_progress);
        thinkView.setVisibility(View.VISIBLE);
    }

    public void finishThinking() {
        View thinkView = findViewById(R.id.tpwgthinking);
        View thinkViewBar = findViewById(R.id.tpwgthinkingProgressBar);
        thinkViewBar.setBackgroundResource(R.drawable.thinking_background_finished);
        thinkView.setVisibility(View.VISIBLE);
    }

    public void stopThinking() {
        View thinkView = findViewById(R.id.tpwgthinking);
        View thinkViewBar = findViewById(R.id.tpwgthinkingProgressBar);
        thinkViewBar.setBackgroundResource(R.drawable.thinking_background_progress);
        thinkView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayer = MediaPlayer.create(this, R.raw.canon_piano_best);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setVolume(soundVolume, soundVolume);
        mMediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(null);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        String gameData = mGameFragment.getState();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_RESTORE, gameData).commit();
        Log.d("UT3", "state = " + gameData);
    }

    @Override
    public void onBackPressed() {
        super.onStop();  // Always call the superclass method first
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("status").setValue("Offline");
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("playing").setValue("");
        finish();
    }
}

class TimerRunnable implements Runnable {
    private int time;
    private TextView timerPanel;
    private Handler animationHandler;
    public TimerRunnable(int time, TextView timerPanel, Handler animationHandler) {
        this.time = time;
        this.timerPanel = timerPanel;
        this.animationHandler = animationHandler;
    }

    @Override
    public void run() {
        if (time > 0) {
            if (time % 2 == 1) {
                timerPanel.setBackgroundResource(R.drawable.dwround_blue_button);
            } else {
                timerPanel.setBackgroundResource(R.drawable.dwround_white_textbox);
            }
            time--;
            animationHandler.postDelayed(this, 1000);
        } else {
            timerPanel.setBackgroundResource(R.drawable.dwround_white_textbox);
            animationHandler.removeCallbacks(this);
        }
    }
}