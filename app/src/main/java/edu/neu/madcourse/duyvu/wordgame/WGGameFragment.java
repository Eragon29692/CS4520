/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/eband4 for more book information.
 ***/
package edu.neu.madcourse.duyvu.wordgame;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

public class WGGameFragment extends Fragment {
    static private int mLargeIds[] = {R.id.wglarge1, R.id.wglarge2, R.id.wglarge3,
            R.id.wglarge4, R.id.wglarge5, R.id.wglarge6, R.id.wglarge7, R.id.wglarge8,
            R.id.wglarge9,};
    static private int mSmallIds[] = {R.id.wgsmall1, R.id.wgsmall2, R.id.wgsmall3,
            R.id.wgsmall4, R.id.wgsmall5, R.id.wgsmall6, R.id.wgsmall7, R.id.wgsmall8,
            R.id.wgsmall9,};
    private Handler mHandler = new Handler();
    private WGTile mEntireBoard = new WGTile(this);
    private WGTile mLargeTiles[] = new WGTile[9];
    private WGTile mSmallTiles[][] = new WGTile[9][9];
    private WGTile.Owner mPlayer = WGTile.Owner.X;
    private Set<WGTile> mAvailable = new HashSet<WGTile>();
    private int mSoundX, mSoundO, mSoundMiss, mSoundRewind;
    private SoundPool mSoundPool;
    private float mVolume = 1f;
    private int mLastLarge;
    private int mLastSmall;
    Globals dictionary;
    //letter board
    private String[][] boardGame = new String[9][9];
    private Random randomGenerator = new Random();
    //flag for restoring letter orders
    private boolean restore = false;

    public static int LONG_PRESS_TIME = 800; // Time in miliseconds
    public static int PRESS_TIME = 400; // Time in miliseconds

    private Handler longPressHandler = new Handler();
    private Handler pressHandler = new Handler();

    Runnable longPressed = new Runnable() {
        public void run() {
            longPress = true;
            singleTap = false;
            ((WGGameActivity) getActivity()).finishThinking();
            Log.i("info", "LongPress");
        }
    };
    Runnable pressEvent = new Runnable() {
        public void run() {
            longPress = false;
            singleTap = false;
            ((WGGameActivity) getActivity()).startThinking();
            longPressHandler.postDelayed(longPressed, LONG_PRESS_TIME);
        }
    };
    boolean longPress = false;
    boolean singleTap = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        initGame();
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        mSoundX = mSoundPool.load(getActivity(), R.raw.sergenious_movex, 1);
        mSoundO = mSoundPool.load(getActivity(), R.raw.sergenious_moveo, 1);
        mSoundMiss = mSoundPool.load(getActivity(), R.raw.erkanozan_miss, 1);
        mSoundRewind = mSoundPool.load(getActivity(), R.raw.joanne_rewind, 1);
        dictionary = (Globals) this.getActivity().getApplication();

        //create board game
        makeLetterBoard();
    }

    @Override
    public void onStart() {
        longPress = false;
        singleTap = true;
        if (restore) {
            restore = false;
            super.onStart();
            return;
        }
        makeLetterBoard();
        updateAllTiles();
        super.onStart();
    }

    private void makeLetterBoard() {
        int[] order;
        String word;
        for (int m = 0; m < 9; m++) {
            order = setWordOrder();
            word = dictionary.get9WordString();
            Log.d("tag", word);
            for (int n = 0; n < 9; n++) {
                mSmallTiles[m][order[n]].setLetter(Character.toString(word.charAt(n)));
            }
        }
    }

    private int[] setWordOrder() {
        int start = randomGenerator.nextInt(9);
        int[] order;
        switch (start) {
            case 1:
                order = new int[]{1, 2, 4, 5, 8, 7, 6, 3, 0};
            case 2:
                order = new int[]{2, 4, 6, 7, 8, 5, 1, 0, 3};
            case 3:
                order = new int[]{3, 0, 1, 2, 5, 8, 4, 7, 6};
            case 4:
                order = new int[]{4, 8, 5, 7, 6, 3, 0, 1, 2};
            case 5:
                order = new int[]{5, 8, 7, 6, 4, 2, 1, 3, 0};
            case 6:
                order = new int[]{6, 4, 2, 5, 1, 0, 3, 7, 8};
            case 7:
                order = new int[]{7, 4, 6, 3, 0, 1, 2, 5, 8};
            case 8:
                order = new int[]{8, 5, 2, 1, 0, 4, 6, 7, 3};
            default:
                order = new int[]{0, 1, 2, 4, 3, 6, 7, 5, 8};
        }
        return order;
    }

    private void clearAvailable() {
        mAvailable.clear();
    }

    private void addAvailable(WGTile tile) {
        if (!tile.unavailable) {
            tile.animate();
            mAvailable.add(tile);
        }
    }

    public boolean isAvailable(WGTile tile) {
        return mAvailable.contains(tile);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.wglarge_board, container, false);
        initViews(rootView);
        updateAllTiles();
        return rootView;
    }

    private void initViews(View rootView) {
        mEntireBoard.setView(rootView);
        for (int large = 0; large < 9; large++) {
            View outer = rootView.findViewById(mLargeIds[large]);
            mLargeTiles[large].setView(outer);

            for (int small = 0; small < 9; small++) {
                Button inner = (Button) outer.findViewById
                        (mSmallIds[small]);
                final int fLarge = large;
                final int fSmall = small;
                final WGTile smallTile = mSmallTiles[large][small];
                smallTile.setView(inner);
                // ...
                inner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch(event.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                pressHandler.postDelayed(pressEvent, PRESS_TIME);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                pressHandler.removeCallbacks(pressEvent);
                                longPressHandler.removeCallbacks(longPressed);
                                ((WGGameActivity) getActivity()).stopThinking();
                                break;
                            case MotionEvent.ACTION_UP:
                                pressHandler.removeCallbacks(pressEvent);
                                longPressHandler.removeCallbacks(longPressed);
                                ((WGGameActivity) getActivity()).stopThinking();

                                if(!longPress && singleTap) {
                                    //..........................................comment out animation for now
                                    //smallTile.animate();
                                    // ...
                                    if (isAvailable(smallTile)) {
                                        //.....................................comment out thinking for now
                                        //((WGGameActivity) getActivity()).startThinking();
                                        mSoundPool.play(mSoundX, mVolume, mVolume, 1, 0, 1f);
                                        makeMove(fLarge, fSmall);
                                        //.................comment think out for part one of this assignment 5
                                        //think();
                                    }
                                }
                                longPress = false;
                                singleTap = true;
                                break;
                        }
                        return true;
                    }
                });

                // ...
            }
        }
    }

    private void setTileLetter() {
        for (int i = 0; i < 9; i++) {
            for (int k = 0; k < 9; k++) {
                boardGame[i][k] = "A";
            }
        }
    }

    private void think() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                if (mEntireBoard.getOwner() == WGTile.Owner.NEITHER) {
                    int move[] = new int[2];
                    pickMove(move);
                    if (move[0] != -1 && move[1] != -1) {
                        switchTurns();
                        mSoundPool.play(mSoundO, mVolume, mVolume,
                                1, 0, 1f);
                        makeMove(move[0], move[1]);
                        switchTurns();
                    }
                }
                ((WGGameActivity) getActivity()).stopThinking();
            }
        }, 1000);
    }

    private void pickMove(int move[]) {
        WGTile.Owner opponent = mPlayer == WGTile.Owner.X ? WGTile.Owner.O : WGTile
                .Owner.X;
        int bestLarge = -1;
        int bestSmall = -1;
        int bestValue = Integer.MAX_VALUE;
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                WGTile smallTile = mSmallTiles[large][small];
                if (isAvailable(smallTile)) {
                    // Try the move and get the score
                    WGTile newBoard = mEntireBoard.deepCopy();
                    newBoard.getSubTiles()[large].getSubTiles()[small]
                            .setOwner(opponent);
                    int value = newBoard.evaluate();
                    Log.d("UT3",
                            "Moving to " + large + ", " + small + " gives value " +
                                    "" + value
                    );
                    if (value < bestValue) {
                        bestLarge = large;
                        bestSmall = small;
                        bestValue = value;
                    }
                }
            }
        }
        move[0] = bestLarge;
        move[1] = bestSmall;
        Log.d("UT3", "Best move is " + bestLarge + ", " + bestSmall);
    }

    private void switchTurns() {
        mPlayer = mPlayer == WGTile.Owner.X ? WGTile.Owner.O : WGTile
                .Owner.X;
    }

    private void makeMove(int large, int small) {
        mLastLarge = large;
        mLastSmall = small;
        WGTile smallTile = mSmallTiles[large][small];
        WGTile largeTile = mLargeTiles[large];
        smallTile.setOwner(mPlayer);
        //put large for better gameplay
        setAvailableFromLastMove(large);
        WGTile.Owner oldWinner = largeTile.getOwner();
        WGTile.Owner winner = largeTile.findWinner();
        if (winner != oldWinner) {
            largeTile.animate();
            largeTile.setOwner(winner);
        }
        winner = mEntireBoard.findWinner();
        mEntireBoard.setOwner(winner);
        updateAllTiles();
        if (winner != WGTile.Owner.NEITHER) {
            ((WGGameActivity) getActivity()).reportWinner(winner);
        }
    }

    public void restartGame() {
        mSoundPool.play(mSoundRewind, mVolume, mVolume, 1, 0, 1f);
        // ...
        initGame();
        initViews(getView());
        makeLetterBoard();
        updateAllTiles();
    }

    public void initGame() {
        Log.d("UT3", "init game");
        mEntireBoard = new WGTile(this);
        // Create all the tiles
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large] = new WGTile(this);
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small] = new WGTile(this);
            }
            mLargeTiles[large].setSubTiles(mSmallTiles[large]);
        }
        mEntireBoard.setSubTiles(mLargeTiles);

        // If the player moves first, set which spots are available
        mLastSmall = -1;
        mLastLarge = -1;
        setAvailableFromLastMove(mLastSmall);
    }

    private void setAvailableFromLastMove(int small) {
        clearAvailable();
        // Make all the tiles at the destination available
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                WGTile tile = mSmallTiles[small][dest];
                if (tile.getOwner() == WGTile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
        // If there were none available, make all squares available
        if (mAvailable.isEmpty()) {
            setAllAvailable();
        }
    }

    private void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                WGTile tile = mSmallTiles[large][small];
                if (tile.getOwner() == WGTile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
    }

    private void updateAllTiles() {
        mEntireBoard.updateDrawableState();
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large].updateDrawableState();
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small].updateDrawableState();
            }
        }
    }

    /**
     * Create a string containing the state of the game.
     */
    public String getState() {
        StringBuilder builder = new StringBuilder();
        builder.append(mLastLarge);
        builder.append(',');
        builder.append(mLastSmall);
        builder.append(',');
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                builder.append(mSmallTiles[large][small].getOwner().name());
                builder.append(":" + mSmallTiles[large][small].getLetter());
                builder.append(',');
            }
        }
        return builder.toString();
    }

    /**
     * Restore the state of the game from the given string.
     */
    public void putState(String gameData) {
        restore = true;
        String[] fields = gameData.split(",");
        int index = 0;
        mLastLarge = Integer.parseInt(fields[index++]);
        mLastSmall = Integer.parseInt(fields[index++]);
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                String[] values = fields[index++].split(":");
                WGTile.Owner owner = WGTile.Owner.valueOf(values[0]);
                mSmallTiles[large][small].setOwner(owner);
                if (values.length > 1)
                    mSmallTiles[large][small].setLetter(values[1]);
            }
        }
        setAvailableFromLastMove(mLastLarge);
        updateAllTiles();
    }
}

