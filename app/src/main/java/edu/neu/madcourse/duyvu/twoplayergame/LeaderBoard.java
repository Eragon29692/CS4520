package edu.neu.madcourse.duyvu.twoplayergame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Collections;
import java.util.Comparator;

import edu.neu.madcourse.duyvu.R;
import edu.neu.madcourse.duyvu.twoplayergame.models.User;

import static edu.neu.madcourse.duyvu.communication.MainActivity.USER_NAME;

public class LeaderBoard extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ArrayList<User> listItems = new ArrayList<User>();
    private ArrayList<User> listItemsTemp = new ArrayList<User>();
    private ListView listView;

    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tpwg_leaderboard);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        listView = (ListView) findViewById(R.id.tpwgPlayerList);
        listView.setAdapter(new ArrayAdapter<User>(this, R.layout.dictionary_listview_text, listItems));


        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);

                listItems.add(user);

                Collections.sort(listItems);
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousID) {
                User user = dataSnapshot.getValue(User.class);
                String token = FirebaseInstanceId.getInstance().getToken();

                for (int i = 0; i < listItems.size(); i++) {
                    if (listItems.get(i).userId.equals(user.userId)) {
                        listItems.remove(i);
                    }
                }
                listItems.add(user);
                Collections.sort(listItems);
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

    }

    @Override
    public void onResume() {
        super.onResume();
        mDatabase.child("users").addChildEventListener(childEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        mDatabase.child("users").removeEventListener(childEventListener);
    }

    @Override
    public void onBackPressed() {
        String token = FirebaseInstanceId.getInstance().getToken();
        mDatabase.child("users").removeEventListener(childEventListener);
        this.finish();
    }
}

