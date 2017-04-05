package edu.neu.madcourse.duyvu.communication.GamePlayDemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import edu.neu.madcourse.duyvu.R;
import edu.neu.madcourse.duyvu.communication.GamePlayDemo.models.User;

import static edu.neu.madcourse.duyvu.communication.MainActivity.USER_NAME;

public class ChoosePlayer extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private boolean connectionStatus = true;
    private ArrayList<User> listItems = new ArrayList<User>();
    private ArrayList<User> listItemsTemp = new ArrayList<User>();
    private ListView listView;

    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_choose_player);

        final String username = getIntent().getStringExtra(USER_NAME);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        listView = (ListView) findViewById(R.id.communicationPlayerList);
        listView.setAdapter(new ArrayAdapter<User>(this, R.layout.dictionary_listview_text, listItems));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                String opponentId = ((User) adapter.getItemAtPosition(position)).userId;
                mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("playing").setValue(opponentId);
                mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("score").setValue("0");
                Intent intent = new Intent(ChoosePlayer.this, GameplayDemoActivity.class);
                startActivity(intent);
                mDatabase.child("users").removeEventListener(childEventListener);
                mDatabase.child(".info/connected").removeEventListener(valueEventListener);
                ChoosePlayer.this.finish();
                // assuming string and if you want to get the value on click of list item
                // do what you intend to do on click of listview row
            }
        });

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                if (user.playing != null && user.playing.equals("") && user.status.equals("Online") && !dataSnapshot.getKey().equals(FirebaseInstanceId.getInstance().getToken())) {
                    listItems.add(user);
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousID) {
                User user = dataSnapshot.getValue(User.class);
                for (int i = 0; i < listItems.size(); i++) {
                    if (listItems.get(i).userId.equals(user.userId)) {
                        listItems.remove(i);
                    }
                }
                if (user.playing != null && user.playing.equals("") && user.status.equals("Online") && !dataSnapshot.getKey().equals(FirebaseInstanceId.getInstance().getToken())) {
                    listItems.add(user);
                }
                if (user.playing != null && user.playing.equals(FirebaseInstanceId.getInstance().getToken()) && user.status.equals("Online")) {
                    Intent intent = new Intent(ChoosePlayer.this, GameplayDemoActivity.class);
                    mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("playing").setValue(user.userId);
                    mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("score").setValue("0");
                    startActivity(intent);
                    ChoosePlayer.this.finish();
                }
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                listItems.remove(user);
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        //mDatabase.child("users").addChildEventListener(childEventListener);


        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                String token = FirebaseInstanceId.getInstance().getToken();
                if (connected) {
                    connectionStatus = true;
                    listItems.addAll(listItemsTemp);
                    listItemsTemp.clear();
                    mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String token = FirebaseInstanceId.getInstance().getToken();
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user.status != "Online")
                                    mDatabase.child("users").child(token).child("status").setValue("Online");
                                if (user.username != username)
                                    mDatabase.child("users").child(token).child("username").setValue(username);
                            } else {
                                User currentUser = new User(username, token);
                                mDatabase.child("users").child(token).setValue(currentUser);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                        }
                    });
                    mDatabase.child("users").child(token).child("status").onDisconnect().setValue("Offine");
                    mDatabase.child("users").child(token).child("playing").onDisconnect().setValue("");
                    mDatabase.child("users").child(token).child("score").onDisconnect().setValue("0");

                    Log.d("ChoosePlayer", "connected");
                } else {
                    connectionStatus = false;
                    listItemsTemp.addAll(listItems);
                    listItems.clear();
                    Log.d("ChoosePlayer", "disconnected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("mytag", "listener canceled");
            }
        };
        //mDatabase.child(".info/connected").addValueEventListener(valueEventListener);
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
    public void onBackPressed() {
        String token = FirebaseInstanceId.getInstance().getToken();
        mDatabase.child("users").removeEventListener(childEventListener);
        mDatabase.child(".info/connected").removeEventListener(valueEventListener);
        mDatabase.child("users").child(token).child("status").setValue("Offine");
        mDatabase.child("users").child(token).child("playing").setValue("");
        mDatabase.child("users").child(token).child("score").setValue("0");
        this.finish();
    }
}

