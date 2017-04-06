package edu.neu.madcourse.duyvu.twoplayergame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

import static edu.neu.madcourse.duyvu.communication.MainActivity.USER_NAME;

public class ChooseUsername extends AppCompatActivity {

    private Button acknowledgeButton;
    private Button quitButton;
    private Button leaderButton;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_choose_username);
        Globals dictionary = (Globals) getApplication();
        dictionary.setDictionary();

        acknowledgeButton = (Button) findViewById(R.id.communication_username_ack);
        acknowledgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChooseUsername.this);
                builder.setMessage("1) I have no new resouce file used in this assignment (canon_piano.mp3)\n\n2) No outside code\n\n3) No additional help aside from the slides and piazza");
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // nothing
                            }
                        });
                mDialog = builder.show();
            }
        });

        quitButton = (Button) findViewById(R.id.communication_username_quit);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseUsername.this.finish();
            }
        });

        leaderButton = (Button) findViewById(R.id.tpwgcommunication_username_leader);
        leaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseUsername.this, LeaderBoard.class);
                startActivity(intent);
            }
        });
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
