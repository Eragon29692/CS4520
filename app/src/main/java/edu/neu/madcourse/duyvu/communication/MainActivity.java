package edu.neu.madcourse.duyvu.communication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.neu.madcourse.duyvu.R;
import edu.neu.madcourse.duyvu.communication.fcm.FCMActivity;
import edu.neu.madcourse.duyvu.communication.realtimedatabase.RealtimeDatabaseActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_main);
    }

    public void openFCMActivity(View view) {
        startActivity(new Intent(MainActivity.this, FCMActivity.class));
    }

    public void openDBActivity(View view) {
        startActivity(new Intent(MainActivity.this, RealtimeDatabaseActivity.class));
    }
}
