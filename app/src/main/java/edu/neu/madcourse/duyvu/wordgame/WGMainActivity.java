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
import android.media.MediaPlayer;
import android.os.Bundle;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

public class WGMainActivity extends Activity {
   MediaPlayer mMediaPlayer;
   // ...

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.wgativity_main);
      Globals dictionary = (Globals)getApplication();
      dictionary.setDictionary();
   }

   @Override
   protected void onResume() {
      super.onResume();
      mMediaPlayer = MediaPlayer.create(this, R.raw.tictactoe_background);
      mMediaPlayer.setVolume(0.5f, 0.5f);
      mMediaPlayer.setLooping(true);
      mMediaPlayer.start();
   }

   @Override
   protected void onPause() {
      super.onPause();
      mMediaPlayer.stop();
      mMediaPlayer.reset();
      mMediaPlayer.release();
   }
}
