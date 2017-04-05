package edu.neu.madcourse.duyvu.communication.GamePlayDemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import edu.neu.madcourse.duyvu.R;

import static edu.neu.madcourse.duyvu.communication.MainActivity.USER_NAME;

public class ChooseUsername extends AppCompatActivity {
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_choose_username);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void onClickChooseUsername(View view) {
        TextView usernameTextView = (TextView) findViewById(R.id.communication_choose_username);
        String username = usernameTextView.getText().toString();
        if (username != null || username != "") {
            Intent intent = new Intent(this, ChoosePlayer.class);
            intent.putExtra(USER_NAME, username);
            startActivity(intent);
        }
    }

}
