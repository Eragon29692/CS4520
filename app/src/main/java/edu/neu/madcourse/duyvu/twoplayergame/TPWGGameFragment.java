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
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;
import edu.neu.madcourse.duyvu.twoplayergame.models.User;

import static edu.neu.madcourse.duyvu.wordgame.WGGameActivity.KEY_RESTORE;
import static edu.neu.madcourse.duyvu.wordgame.WGGameActivity.PREF_RESTORE;
import static edu.neu.madcourse.duyvu.wordgame.WGMainActivity.NORMAL_VOLUME;

public class TPWGGameFragment extends Fragment {
    private DatabaseReference mDatabase;
    private AlertDialog mDialog;
    private int maxInt = Integer.MAX_VALUE;
    SharedPreferences sharedPreferences;
    static private int mLargeIds[] = {R.id.tpwglarge1, R.id.tpwglarge2, R.id.tpwglarge3,
            R.id.tpwglarge4, R.id.tpwglarge5, R.id.tpwglarge6, R.id.tpwglarge7, R.id.tpwglarge8,
            R.id.tpwglarge9,};
    static private int mSmallIds[] = {R.id.tpwgsmall1, R.id.tpwgsmall2, R.id.tpwgsmall3,
            R.id.tpwgsmall4, R.id.tpwgsmall5, R.id.tpwgsmall6, R.id.tpwgsmall7, R.id.tpwgsmall8,
            R.id.tpwgsmall9,};
    private Handler mHandler = new Handler();
    private ValueEventListener connectionEventListener;
    private ValueEventListener userEventListener;
    private boolean connectionStatus = true;
    private TPWGTile mEntireBoard = new TPWGTile(this);
    private TPWGTile mLargeTiles[] = new TPWGTile[9];
    private TPWGTile mSmallTiles[][] = new TPWGTile[9][9];
    private TPWGTile.Owner mPlayer = TPWGTile.Owner.X;
    private TPWGTile.Owner myPlayer = TPWGTile.Owner.X;
    private Set<TPWGTile> mAvailable = new HashSet<TPWGTile>();
    private int mSoundX, mSoundO, mSoundMiss, mSoundRewind;
    private SoundPool mSoundPool;
    private float mVolume = 0f; //NORMAL_VOLUME;
    private int mLastLarge;
    private int mLastSmall;
    Globals dictionary;
    String opponentId = "unknown";
    //letter board
    private String[][] boardGame = new String[9][9];
    private Random randomGenerator = new Random();
    //flag for restoring letter orders
    private boolean restore = false;

    private ArrayList<String> usedWords = new ArrayList<>();

    private int scoreRatio = 1;

    public static int LONG_PRESS_TIME = 600; // Time in miliseconds
    public static int PRESS_TIME = 400; // Time in miliseconds

    private Handler longPressHandler = new Handler();
    private Handler pressHandler = new Handler();

    private String currentString = "";
    private int[] currentScore = new int[]{maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt};
    private int[] finishedBoard = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean touched = false;

    Runnable longPressed = new Runnable() {
        public void run() {
            onLongPressed();
            ((TPWGGameActivity) getActivity()).finishThinking();
        }
    };
    Runnable pressEvent = new Runnable() {
        public void run() {
            onPressed();
            ((TPWGGameActivity) getActivity()).startThinking();
            longPressHandler.postDelayed(longPressed, LONG_PRESS_TIME);
        }
    };

    boolean longPress = false;
    boolean singleTap = true;

    private Handler timeHandler = new Handler();
    private int currentTime = 0;
    private int phase1 = 120;
    private int phase2 = 60;

    Runnable timerPhase = new Runnable() {
        public void run() {
            if (phase1 != -1) {
                ((TPWGGameActivity) getActivity()).displayTime(phase1, (mPlayer == myPlayer ? "You" : "Opponent"), connectionStatus);
                if (phase1 % 10 == 0) {
                    if ((phase1 / 10) % 2 == 1) {
                        mPlayer = TPWGTile.Owner.X;
                    } else {
                        mPlayer = TPWGTile.Owner.O;
                    }
                    pressHandler.removeCallbacks(pressEvent);
                    longPressHandler.removeCallbacks(longPressed);
                    ((TPWGGameActivity) getActivity()).stopThinking();
                    ((TPWGGameActivity) getActivity()).stopThinking();
                }
            } else {
                ((TPWGGameActivity) getActivity()).displayTime(phase2, "You", connectionStatus);
            }
            if (phase1 == 10) {
                ((TPWGGameActivity) getActivity()).animationForTimer(9);
            }
            if (phase1 > 0) {
                phase1--;
                timeHandler.postDelayed(this, 1000);
            }
            if (phase1 == -1 && phase2 > 0) {
                phase2--;
                timeHandler.postDelayed(this, 1000);
            }
            if (phase1 == 0 || checkFinishedPhase1() && phase1 != -1) {
                phase1 = -1;
                flipBoardForPhase2();
                timeHandler.postDelayed(this, 1000);
            }
            if (phase2 == 10) {
                ((TPWGGameActivity) getActivity()).animationForTimer(9);
            }
            if (phase1 == -1 && phase2 == 0) {
                mDatabase.child("users").child(opponentId).child("score").setValue(calculateAndDisplayTotalScore() == 0 ? "-1" : Integer.toString(calculateAndDisplayTotalScore()));
                /*
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("TIME'S UP. YOUR SCORE IS " + calculateAndDisplayTotalScore());
                builder.setCancelable(false);

                builder.setPositiveButton(R.string.ok_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                restartNoTimerAndSound();
                                getActivity().finish();
                            }
                        });
                mDialog = builder.show();
                */
            }
        }
    };

    private boolean checkFinishedPhase1() {
        boolean finished = true;
        for (int i = 0; i < 9; i++) {
            if (finishedBoard[i] == 0) {
                finished = false;
            }
        }
        //if (finished) {
        //    timeHandler.removeCallbacks(timerPhase);
        //}
        return finished;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        //makeLetterBoard();
        connectionEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    connectionStatus = true;
                    Log.d("mytag", "connected");
                } else {
                    connectionStatus = false;
                    Log.d("mytag", "disconnected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("mytag", "listener canceled");
            }
        };

        userEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null && !user.data.equals("") && user.data.substring(0, user.data.indexOf(",")).equals(opponentId)) {
                    updateState(user.data);
                    calculateAndDisplayTotalScore();
                }
                if (user != null && !user.score.equals("0")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(calculateAndDisplayTotalScore() > Integer.parseInt(user.score) ? "YOU HAVE WON" :
                            (calculateAndDisplayTotalScore() == Integer.parseInt(user.score) ? "YOU HAVE TIED" : "YOU HAVE LOST"));
                    mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("score").setValue("0");
                    builder.setCancelable(false);

                    builder.setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    restartNoTimerAndSound();
                                    getActivity().finish();
                                }
                            });
                    mDialog = builder.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        connectionEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    connectionStatus = true;
                    Log.d("mytag","connected");
                } else {
                    connectionStatus = false;
                    Log.d("mytag","disconnected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("mytag","listener canceled");
            }
        };
    }

    @Override
    public void onStart() {
        longPress = false;
        singleTap = true;
        calculateAndDisplayTotalScore();
        mVolume = ((TPWGGameActivity) getActivity()).getVolume();
        mSoundPool.setVolume(mSoundO, mVolume, mVolume);
        mSoundPool.setVolume(mSoundX, mVolume, mVolume);
        mSoundPool.setVolume(mSoundRewind, mVolume, mVolume);
        if (restore) {
            restore = false;
            super.onStart();
            return;
        }
        currentString = "";
        //makeLetterBoard();
        updateAllTiles();
        //timeHandler.postDelayed(timerPhase, 1000);
        super.onStart();
    }

    private TPWGTile.Owner getOpponent(TPWGTile.Owner player) {
        if (player == TPWGTile.Owner.X) {
            return TPWGTile.Owner.O;
        } else {
            return TPWGTile.Owner.X;
        }
    }
    private int calculateAndDisplayTotalScore() {
        int total = 0;
        for (int i = 0; i < 9; i++) {
            if (currentScore[i] != maxInt && (mLargeTiles[i].getOwner() == myPlayer || mLargeTiles[i].getOwner() == TPWGTile.Owner.NEITHER)) {
                total += currentScore[i] * scoreRatio;
            }
        }
        ((TPWGGameActivity) getActivity()).displayScore(Integer.toString(total));
        return total;
    }

    private void addScore(int add) {
        if (currentScore[mLastLarge] == maxInt) {
            currentScore[mLastLarge] = add;
        } else {
            currentScore[mLastLarge] += add;
        }
    }

    private void flipBoardForPhase2() {
        for (int i = 0; i < 9; i++) {
            //mLargeTiles[i].setOwner(TPWGTile.Owner.NEITHER);
            if (mLargeTiles[i].getOwner() == TPWGTile.Owner.NEITHER || finishedBoard[i] == 0) {
                mLargeTiles[i].setOwner(TPWGTile.Owner.UNAVAIL);
            } else {
                mLargeTiles[i].setOwner(TPWGTile.Owner.NEITHER);
            }
            for (int k = 0; k < 9; k++) {
                if (mSmallTiles[i][k].getOwner() == TPWGTile.Owner.NEITHER || finishedBoard[i] == 0) {
                    mSmallTiles[i][k].setOwner(TPWGTile.Owner.UNAVAIL);
                } else {
                    mSmallTiles[i][k].setOwner(TPWGTile.Owner.NEITHER);
                }
                mSmallTiles[i][k].setAvailable(true);
            }
        }
        timeHandler.removeCallbacks(timerPhase);
        //mLastLarge = -1;
        //mLastSmall = -1;
        currentString = "";
        mAvailable.clear();
        calculateAndDisplayTotalScore();
        setAllAvailable();
        updateAllTiles();
    }

    private void onLongPressed() {
        longPress = true;
        singleTap = false;
        Boolean checkWord = ((TPWGGameActivity) getActivity()).dictionary.checkDictionary(currentString);
        if (checkWord && !usedWords.contains(currentString)) {
            if (phase1 != -1) {
                finishedBoard[mLastLarge] = (myPlayer == TPWGTile.Owner.X ? 1 : 2);
                ((TPWGGameActivity) getActivity()).displayWord(currentString);
                for (int i = 0; i < 9; i++) {
                    mSmallTiles[mLastLarge][i].setAvailable(false);
                    mLargeTiles[mLastLarge].setOwner(myPlayer);
                }
                //set all tiles to one player
                for (int i = 0; i < 9; i++) {
                    if (mSmallTiles[mLastLarge][i].getOwner() != myPlayer) {
                        if (mSmallTiles[mLastLarge][i].getOwner() == TPWGTile.Owner.X) {
                            mSmallTiles[mLastLarge][i].setOwner(TPWGTile.Owner.O);
                        }
                        else if (mSmallTiles[mLastLarge][i].getOwner() == TPWGTile.Owner.O) {
                            mSmallTiles[mLastLarge][i].setOwner(TPWGTile.Owner.X);
                        } else {
                            //do nothing bad design :|
                        }
                    }
                }
            } else {
                ((TPWGGameActivity) getActivity()).displayWord(currentString);
                for (int i = 0; i < 9; i++) {
                    mSmallTiles[mLastLarge][i].setAvailable(true);
                }
            }
            addScore(currentString.length() * scoreRatio);
            usedWords.add(currentString);
        } else {
            if (phase1 != -1) {
                if (usedWords.contains(currentString)) {
                    ((TPWGGameActivity) getActivity()).displayWord("ALREADY USED!!!");
                } else {
                    ((TPWGGameActivity) getActivity()).displayWord("WRONG!!!");
                }
                for (int i = 0; i < 9; i++) {
                    mSmallTiles[mLastLarge][i].setAvailable(true);
                    mSmallTiles[mLastLarge][i].setOwner(TPWGTile.Owner.NEITHER);
                }
            } else {
                if (usedWords.contains(currentString)) {
                    ((TPWGGameActivity) getActivity()).displayWord("ALREADY USED!!!");
                } else {
                    ((TPWGGameActivity) getActivity()).displayWord("WRONG!!!");
                }
                for (int i = 0; i < 9; i++) {
                    mSmallTiles[mLastLarge][i].setAvailable(true);
                }
            }
            addScore(-currentString.length() * scoreRatio);
        }
        updateOnLongPress();
    }

    private void updateOnLongPress() {
        mLastLarge = -1;
        mLastSmall = -1;
        currentString = "";
        mAvailable.clear();
        calculateAndDisplayTotalScore();
        setAllAvailable();
        updateAllTiles();
        if (phase1 > 0) {
            mDatabase.child("users").child(opponentId).child("data").setValue(getState());
        }
    }

    private void onPressed() {
        longPress = false;
        singleTap = false;
    }

    private void makeLetterBoard() {
        touched = false;
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

    private int setNextAvailableFromLastMove(int fLarge, int fSmall) {
        int availableInSmall = 0;
        if (fLarge != -1 || fSmall != -1) {
            //making other unvavalable
            int[] availPos = getAdjacent(fSmall);
            //set all unval
            for (int i = 0; i < 9; i++) {
                mSmallTiles[fLarge][i].setAvailable(false);
            }
            //set the avail ones
            for (int i = 0; i < availPos.length; i++) {
                if (mSmallTiles[fLarge][availPos[i]].getOwner() == TPWGTile.Owner.NEITHER) {
                    mSmallTiles[fLarge][availPos[i]].setAvailable(true);
                    availableInSmall++;
                }
            }
        }
        return availableInSmall;
    }

    private int[] getAdjacent(int pos) {
        int[] positions;
        switch (pos) {
            case 1:
                positions = new int[]{0, 3, 4, 5, 2};
                break;
            case 2:
                positions = new int[]{1, 4, 5};
                break;
            case 3:
                positions = new int[]{0, 1, 4, 7, 6};
                break;
            case 4:
                positions = new int[]{0, 1, 2, 5, 8, 7, 6, 3};
                break;
            case 5:
                positions = new int[]{2, 1, 4, 7, 8};
                break;
            case 6:
                positions = new int[]{3, 4, 7};
                break;
            case 7:
                positions = new int[]{6, 3, 4, 5, 8};
                break;
            case 8:
                positions = new int[]{7, 4, 5};
                break;
            default:
                positions = new int[]{1, 3, 4};
        }
        return positions;
    }

    private int[] setWordOrder() {
        int start = randomGenerator.nextInt(9);
        int[] order;
        switch (start) {
            case 1:
                order = new int[]{1, 2, 4, 5, 8, 7, 6, 3, 0};
                break;
            case 2:
                order = new int[]{2, 4, 6, 7, 8, 5, 1, 0, 3};
                break;
            case 3:
                order = new int[]{3, 0, 1, 2, 5, 8, 4, 7, 6};
                break;
            case 4:
                order = new int[]{4, 8, 5, 7, 6, 3, 0, 1, 2};
                break;
            case 5:
                order = new int[]{5, 8, 7, 6, 4, 2, 1, 3, 0};
                break;
            case 6:
                order = new int[]{6, 4, 2, 5, 1, 0, 3, 7, 8};
                break;
            case 7:
                order = new int[]{7, 4, 6, 3, 0, 1, 2, 5, 8};
                break;
            case 8:
                order = new int[]{8, 5, 2, 1, 0, 4, 6, 7, 3};
                break;
            default:
                order = new int[]{0, 1, 2, 4, 3, 6, 7, 5, 8};
        }
        return order;
    }

    private void clearAvailable() {
        mAvailable.clear();
    }

    private void addAvailable(TPWGTile tile) {
        if (tile.getAvailable()) {
            tile.animate();
            mAvailable.add(tile);
        }
    }

    public boolean isAvailable(TPWGTile tile) {
        return mAvailable.contains(tile);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.tpwglarge_board, container, false);
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
                final TPWGTile smallTile = mSmallTiles[large][small];
                smallTile.setView(inner);
                // ...
                inner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (mPlayer == myPlayer && connectionStatus) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    if (mLastLarge != -1 && mLastLarge == fLarge && mSmallTiles[fLarge][fSmall].getOwner() != TPWGTile.Owner.NEITHER
                                            && mSmallTiles[fLarge][fSmall].getOwner() != TPWGTile.Owner.UNAVAIL
                                            || phase1 == -1 && mLastLarge != -1 && mLastLarge == fLarge && mSmallTiles[fLarge][fSmall].getOwner() == TPWGTile.Owner.NEITHER
                                            && mSmallTiles[fLarge][fSmall].getOwner() != TPWGTile.Owner.UNAVAIL) {
                                        pressHandler.postDelayed(pressEvent, PRESS_TIME);
                                    }
                                    break;
                                //case MotionEvent.ACTION_MOVE:
                                //    pressHandler.removeCallbacks(pressEvent);
                                //    longPressHandler.removeCallbacks(longPressed);
                                //    ((TPWGGameActivity) getActivity()).stopThinking();
                                //    break;
                                case MotionEvent.ACTION_UP:
                                    pressHandler.removeCallbacks(pressEvent);
                                    longPressHandler.removeCallbacks(longPressed);
                                    ((TPWGGameActivity) getActivity()).stopThinking();

                                    if (!longPress && singleTap) {
                                        //..........................................comment out animation for now
                                        smallTile.animate();
                                        // ...
                                        if (isAvailable(smallTile) && mSmallTiles[fLarge][fSmall].getAvailable()) {
                                            //.....................................comment out thinking for now
                                            //making other unvavalable
                                            currentString += smallTile.getLetter();
                                            int nomove = setNextAvailableFromLastMove(fLarge, fSmall);
                                            //do the click
                                            mSoundPool.play(mSoundX, mVolume / 2, mVolume / 2, 1, 0, 1f);
                                            if (phase1 != -1) {
                                                makeMove(fLarge, fSmall);
                                            } else {
                                                mLastLarge = fLarge;
                                                mLastSmall = fSmall;
                                                mSmallTiles[fLarge][fSmall].setAvailable(false);
                                                setAvailableFromLastMove(fLarge);
                                                updateAllTiles();
                                            }
                                            //.................comment think out for part one of this assignment 5
                                            //think();
                                        }
                                    }
                                    longPress = false;
                                    singleTap = true;
                                    break;
                            }
                        }
                        return true;
                    }
                });

                // ...
            }
        }
        //adding letter to the board
        makeLetterBoard();

        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String token = FirebaseInstanceId.getInstance().getToken();
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    opponentId = user.playing;
                    if (FirebaseInstanceId.getInstance().getToken().compareTo(opponentId) > 0) {
                        myPlayer = TPWGTile.Owner.X;
                        //mDatabase.child("users").child(token).child("data").setValue(getState());
                        mDatabase.child("users").child(opponentId).child("data").setValue(getState());
                    } else {
                        myPlayer = TPWGTile.Owner.O;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    private void think() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                if (mEntireBoard.getOwner() == TPWGTile.Owner.NEITHER) {
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
                ((TPWGGameActivity) getActivity()).stopThinking();
            }
        }, 1000);
    }

    private void pickMove(int move[]) {
        TPWGTile.Owner opponent = mPlayer == TPWGTile.Owner.X ? TPWGTile.Owner.O : TPWGTile
                .Owner.X;
        int bestLarge = -1;
        int bestSmall = -1;
        int bestValue = Integer.MAX_VALUE;
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                TPWGTile smallTile = mSmallTiles[large][small];
                if (isAvailable(smallTile)) {
                    // Try the move and get the score
                    TPWGTile newBoard = mEntireBoard.deepCopy();
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
        mPlayer = mPlayer == TPWGTile.Owner.X ? TPWGTile.Owner.O : TPWGTile
                .Owner.X;
    }

    private void makeMove(int large, int small) {
        if (!touched) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_RESTORE, true).commit();
            touched = true;
        }

        mLastLarge = large;
        mLastSmall = small;

        TPWGTile smallTile = mSmallTiles[large][small];
        TPWGTile largeTile = mLargeTiles[large];
        smallTile.setOwner(mPlayer);
        //put large for better gameplay
        setAvailableFromLastMove(large);
/*
        TPWGTile.Owner oldWinner = largeTile.getOwner();
        TPWGTile.Owner winner = largeTile.findWinner();
        if (winner != oldWinner) {
            largeTile.animate();
            largeTile.setOwner(winner);
        }
        //winner = mEntireBoard.findWinner();
        mEntireBoard.setOwner(winner);
        if (winner != TPWGTile.Owner.NEITHER) {
            ((TPWGGameActivity) getActivity()).reportWinner(winner);
        }
*/
        updateAllTiles();
        mDatabase.child("users").child(opponentId).child("data").setValue(getState());

    }

    public void restartGame() {
        mSoundPool.play(mSoundRewind, mVolume / 2, mVolume / 2, 1, 0, 1f);
        // ...
        phase1 = 180;
        phase2 = 60;
        timeHandler.removeCallbacks(timerPhase);
        timeHandler.postDelayed(timerPhase, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_RESTORE, "").commit();
        editor.putBoolean(KEY_RESTORE, false).commit();
        currentScore = new int[]{maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt};
        finishedBoard = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        currentString = "";
        ((TPWGGameActivity) getActivity()).displayWord(currentString);
        initGame();
        initViews(getView());
        //makeLetterBoard();
        calculateAndDisplayTotalScore();
        updateAllTiles();
    }

    public void restartNoTimerAndSound() {
        // ...
        phase1 = 180;
        phase2 = 60;
        //timeHandler.removeCallbacks(timerPhase);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_RESTORE, "").commit();
        editor.putBoolean(KEY_RESTORE, false).commit();
        currentScore = new int[]{maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt, maxInt};
        finishedBoard = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        currentString = "";
        //((TPWGGameActivity)getActivity()).displayWord(currentString);
        //initGame();
        //initViews(getView());
        //makeLetterBoard();
        //calculateAndDisplayTotalScore();
        //updateAllTiles();
    }

    public void initGame() {
        Log.d("UT3", "init game");
        mEntireBoard = new TPWGTile(this);
        // Create all the tiles
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large] = new TPWGTile(this);
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small] = new TPWGTile(this);
            }
            mLargeTiles[large].setSubTiles(mSmallTiles[large]);
        }
        mEntireBoard.setSubTiles(mLargeTiles);
        usedWords.clear();
        // If the player moves first, set which spots are available
        mLastSmall = -1;
        mLastLarge = -1;
        setAvailableFromLastMove(mLastSmall);
    }

    private void setAvailableFromLastMove(int small) {
        clearAvailable();
        // Make all the tiles at the destination available
        if (small != -1) {
            mAvailable.clear();
            for (int dest = 0; dest < 9; dest++) {
                TPWGTile tile = mSmallTiles[small][dest];
                if (tile.getOwner() == TPWGTile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
        // If there were none available, make all squares available
        if (mAvailable.isEmpty() && small == -1 || mAvailable.isEmpty() && currentScore[small] != maxInt && finishedBoard[small] != 0) {
            mAvailable.clear();
            setAllAvailable();
        }
    }

    private void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                TPWGTile tile = mSmallTiles[large][small];
                if (tile.getOwner() == TPWGTile.Owner.NEITHER)
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
        builder.append(FirebaseInstanceId.getInstance().getToken());
        builder.append(',');
        builder.append(phase1);
        builder.append(',');
        builder.append(phase2);
        builder.append(',');
        builder.append(mLastLarge);
        builder.append(',');
        builder.append(mLastSmall);
        builder.append(',');
        builder.append(currentString);
        builder.append(',');
        //appending score for each tile
        for (int i = 0; i < 9; i++) {
            builder.append(currentScore[i]);
            builder.append(",");
        }
        //make sure board is pressed or not
        for (int i = 0; i < 9; i++) {
            builder.append(finishedBoard[i]);
            builder.append(",");
        }
        //adding letter and value of each tiles
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                builder.append(mSmallTiles[large][small].getOwner().name());
                builder.append(mSmallTiles[large][small].getAvailable() ? ":1:" : ":0:");
                builder.append(mSmallTiles[large][small].getLetter());
                builder.append(',');
            }
        }
        //adding the used word list
        for (int i = 0; i < usedWords.size(); i++) {
            builder.append(usedWords.get(i));
            builder.append(',');
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
        index++; //skipping who sending this update
        phase1 = Integer.parseInt(fields[index++]);
        phase2 = Integer.parseInt(fields[index++]);
        mLastLarge = Integer.parseInt(fields[index++]);
        mLastSmall = Integer.parseInt(fields[index++]);
        currentString = fields[index++];
        //scores for large tiles
        for (int i = 0; i < 9; i++) {
            //index++;
            currentScore[i] = Integer.parseInt(fields[index++]);
        }
        //check for finished board
        for (int i = 0; i < 9; i++) {
            finishedBoard[i] = Integer.parseInt(fields[index++]);
        }
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                String[] values = fields[index++].split(":");
                TPWGTile.Owner owner = TPWGTile.Owner.valueOf(values[0]);
                mSmallTiles[large][small].setOwner(owner);
                if (values.length > 2)
                    mSmallTiles[large][small].setAvailable(values[1] == "1" ? true : false);
                    mSmallTiles[large][small].setLetter(values[2]);
            }
        }
        //putting the list of used words
        for (int i = index; i < fields.length; i++) {
            usedWords.add(fields[i]);
        }

        setUnavailableTiles(finishedBoard);
        setNextAvailableFromLastMove(mLastLarge, mLastSmall);
        setAvailableFromLastMove(mLastLarge);
        updateAllTiles();
    }


    public void updateState(String gameData) {
        String[] fields = gameData.split(",");
        int index = 0;
        index++; //skipping who sending this update
        phase1 = Integer.parseInt(fields[index++]);
        phase2 = Integer.parseInt(fields[index++]);
        mLastLarge = Integer.parseInt(fields[index++]);
        mLastSmall = Integer.parseInt(fields[index++]);
        currentString = fields[index++];
        //scores for large tiles
        for (int i = 0; i < 9; i++) {
            currentScore[i] = Integer.parseInt(fields[index++]);
        }
        //check for finished board
        for (int i = 0; i < 9; i++) {
            finishedBoard[i] = Integer.parseInt(fields[index++]);
        }
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                String[] values = fields[index++].split(":");
                TPWGTile.Owner owner = TPWGTile.Owner.valueOf(values[0]);
                mSmallTiles[large][small].setOwner(owner);
                if (values.length > 2)
                    mSmallTiles[large][small].setAvailable(values[1].equals("1"));
                mSmallTiles[large][small].setLetter(values[2]);
            }
        }
        //putting the list of used words
        for (int i = index; i < fields.length; i++) {
            usedWords.add(fields[i]);
        }

        setUnavailableTiles(finishedBoard);
        setNextAvailableFromLastMove(mLastLarge, mLastSmall);
        setAvailableFromLastMove(mLastLarge);
        updateAllTiles();
    }


    //tiles with out a score mean it has not yet touched
    //therefore set the scored tiles as unavailable
    private void setUnavailableTiles(int[] score) {
        if (phase1 != -1) {
            for (int i = 0; i < 9; i++) {
                if (score[i] != 0) {
                    //set X as ower for finished tiles
                    mLargeTiles[i].setOwner(score[i] == 1 ? TPWGTile.Owner.X : TPWGTile.Owner.O);
                    for (int k = 0; k < 9; k++) {
                        mSmallTiles[i][k].setAvailable(false);
                    }
                }
            }
        } else {
            for (int i = 0; i < 9; i++) {
                mLargeTiles[i].setOwner(TPWGTile.Owner.NEITHER);
                for (int k = 0; k < 9; k++) {
                    mSmallTiles[i][k].setAvailable(true);
                }
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timerPhase);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).removeEventListener(userEventListener);
        mDatabase.child(".info/connected").removeEventListener(connectionEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        timeHandler.postDelayed(timerPhase, 0);
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).addValueEventListener(userEventListener);
        mDatabase.child(".info/connected").addValueEventListener(connectionEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

}

