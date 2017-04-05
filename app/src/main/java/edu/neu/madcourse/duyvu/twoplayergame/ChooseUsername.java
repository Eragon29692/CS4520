package edu.neu.madcourse.duyvu.twoplayergame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

import static edu.neu.madcourse.duyvu.communication.MainActivity.USER_NAME;

public class ChooseUsername extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_choose_username);
        Globals dictionary = (Globals) getApplication();
        dictionary.setDictionary();
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
