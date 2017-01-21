package edu.neu.madcourse.duyvu.hellomad;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.neu.madcourse.duyvu.R;
import edu.neu.madcourse.duyvu.tictactoe.MainActivity;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void onClickAbout(View view)
    {
        Intent intent = new Intent(MainMenu.this, AboutMe.class);
        startActivity(intent);
    }

    public void onClickTicTacToe(View view)
    {
        Intent intent = new Intent(MainMenu.this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickQuit(View view)
    {
        finish();
        System.exit(0);
    }

    public void onGenerateErrorQuit(View view)
    {
        throw new RuntimeException("Happy Crashing!!!");
    }




}
