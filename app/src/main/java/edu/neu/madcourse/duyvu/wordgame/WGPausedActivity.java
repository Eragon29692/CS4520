package edu.neu.madcourse.duyvu.wordgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.neu.madcourse.duyvu.R;

import static edu.neu.madcourse.duyvu.wordgame.WGGameActivity.KEY_RESTORE;
import static edu.neu.madcourse.duyvu.wordgame.WGGameActivity.SOUND_VOLUME;
import static edu.neu.madcourse.duyvu.wordgame.WGMainActivity.NORMAL_VOLUME;

public class WGPausedActivity extends AppCompatActivity {
    private float soundVolume = NORMAL_VOLUME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wgativity_pause);
        soundVolume = getIntent().getFloatExtra(SOUND_VOLUME, NORMAL_VOLUME);
    }

    public void onClickPausedButtonPauseActivity(View view)
    {
        Intent intent = new Intent(WGPausedActivity.this, WGGameActivity.class);
        intent.putExtra(KEY_RESTORE, true);
        intent.putExtra(SOUND_VOLUME, soundVolume);
        finish();
        startActivity(intent);
    }
}
