/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/eband4 for more book information.
 ***/
package edu.neu.madcourse.duyvu.wordgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

import static edu.neu.madcourse.duyvu.wordgame.WGMainActivity.NORMAL_VOLUME;

public class WGGameActivity extends AppCompatActivity {
    public static final String KEY_RESTORE = "key_restore";
    public static final String SOUND_VOLUME = "sound_volume";
    public static final String PREF_RESTORE = "pref_restore";
    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private WGGameFragment mGameFragment;
    private int scoreDisplay = 0;
    public Globals dictionary;
    SharedPreferences sharedPreferences;
    private float soundVolume = NORMAL_VOLUME;
    private Handler animationHandler = new Handler();

    Runnable animationTimer = new Runnable() {
        public void run() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wgactivity_game);
        dictionary = (Globals) getApplication();
        mGameFragment = (WGGameFragment) getFragmentManager()
                .findFragmentById(R.id.wgfragment_game);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        soundVolume = getIntent().getFloatExtra(SOUND_VOLUME, NORMAL_VOLUME);
        if (restore) {
            String gameData = sharedPreferences.getString(WGGameActivity.PREF_RESTORE, null);
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
        TextView scorePanel = (TextView) findViewById(R.id.wgactivity_game_score);
        scorePanel.setText("Score: " + score);
    }

    public void displayTime(int time) {
        TextView timePanel = (TextView) findViewById(R.id.wgactivity_timer);
        timePanel.setText("Time: " + Integer.toString(time));
    }

    public void displayWord(String word) {
        TextView scorePanel = (TextView) findViewById(R.id.wgactivity_current_word);
        scorePanel.setText(word);
    }

    public void onClickPausedButton(View view)
    {
        Intent intent = new Intent(WGGameActivity.this, WGPausedActivity.class);
        intent.putExtra(SOUND_VOLUME, soundVolume);
        finish();
        startActivity(intent);
    }

    public void animationForTimer(int time) {
        TextView timerPanel = (TextView) findViewById(R.id.wgactivity_timer);
        TimerRunnable timerRunnable = new TimerRunnable(time, timerPanel, animationHandler);
        animationHandler.postDelayed(timerRunnable, 1000);
    }

    public void restartGame() {
        mGameFragment.restartGame();
    }

    public void reportWinner(final WGTile.Owner winner) {
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
                mMediaPlayer = MediaPlayer.create(WGGameActivity.this,
                        winner == WGTile.Owner.X ? R.raw.oldedgar_winner
                                : winner == WGTile.Owner.O ? R.raw.notr_loser
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
        View thinkView = findViewById(R.id.wgthinking);
        View thinkViewBar = findViewById(R.id.wgthinkingProgressBar);
        thinkViewBar.setBackgroundResource(R.drawable.thinking_background_progress);
        thinkView.setVisibility(View.VISIBLE);
    }

    public void finishThinking() {
        View thinkView = findViewById(R.id.wgthinking);
        View thinkViewBar = findViewById(R.id.wgthinkingProgressBar);
        thinkViewBar.setBackgroundResource(R.drawable.thinking_background_finished);
        thinkView.setVisibility(View.VISIBLE);
    }

    public void stopThinking() {
        View thinkView = findViewById(R.id.wgthinking);
        View thinkViewBar = findViewById(R.id.wgthinkingProgressBar);
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