package edu.neu.madcourse.duyvu.communication.GamePlayDemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import edu.neu.madcourse.duyvu.R;
import edu.neu.madcourse.duyvu.communication.GamePlayDemo.models.User;


public class GameplayDemoActivity extends AppCompatActivity {

    private static final String TAG = GameplayDemoActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private TextView userName;
    private TextView score;
    private TextView userName2;
    private TextView score2;
    private RadioButton player1;
    private Button add5;
    private boolean connectionStatus = true;
    private String opponentToken = "";
    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_realtime_database);

        userName = (TextView) findViewById(R.id.username);
        score = (TextView) findViewById(R.id.score);
        userName2 = (TextView) findViewById(R.id.username2);
        score2 = (TextView) findViewById(R.id.score2);

        player1 = (RadioButton)findViewById(R.id.player1);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        add5 = (Button)findViewById(R.id.add5);
        add5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectionStatus) {
                    String token = FirebaseInstanceId.getInstance().getToken();
                    GameplayDemoActivity.this.onAddScore(mDatabase, player1.isChecked() ? token : opponentToken);
                }
            }
        });



        childEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        User user = dataSnapshot.getValue(User.class);

                        if (dataSnapshot.getKey().equalsIgnoreCase(FirebaseInstanceId.getInstance().getToken())) {
                            score.setText(user.score);
                            userName.setText(user.username);
                        } else {
                            score2.setText(String.valueOf(user.score));
                            userName2.setText(user.username);
                            opponentToken = user.userId;
                        }
                        Log.e(TAG, "onChildAdded: dataSnapshot = " + dataSnapshot.getValue());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        User user = dataSnapshot.getValue(User.class);

                        if (dataSnapshot.getKey().equalsIgnoreCase(FirebaseInstanceId.getInstance().getToken())) {
                            score.setText(user.score);
                            userName.setText(user.username);
                        } else {
                            score2.setText(String.valueOf(user.score));
                            userName2.setText(user.username);
                        }
                        Log.v(TAG, "onChildChanged: "+dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled:" + databaseError);
                    }
                };


        valueEventListener = new ValueEventListener() {
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
    public void onResume() {
        super.onResume();
        mDatabase.child("users").addChildEventListener(childEventListener);
        mDatabase.child(".info/connected").addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        mDatabase.child("users").removeEventListener(childEventListener);
        mDatabase.child(".info/connected").removeEventListener(valueEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("status").setValue("Offline");
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("score").setValue("0");
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("playing").setValue("");
    }

    @Override
    public void onBackPressed() {
        String token = FirebaseInstanceId.getInstance().getToken();
        mDatabase.child("users").removeEventListener(childEventListener);
        mDatabase.child(".info/connected").removeEventListener(valueEventListener);
        this.finish();
    }



    /**
     * Called on score add
     * @param postRef
     * @param user
     */
    private void onAddScore(DatabaseReference postRef, String user) {
        postRef
                .child("users")
                .child(user)
                .runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                User u = mutableData.getValue(User.class);
                if (u == null) {
                    return Transaction.success(mutableData);
                }
                if(connectionStatus) {
                    u.score = String.valueOf(Integer.valueOf(u.score) + 5);
                    mutableData.setValue(u);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
}
