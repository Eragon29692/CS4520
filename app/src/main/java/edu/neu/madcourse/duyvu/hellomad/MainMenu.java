package edu.neu.madcourse.duyvu.hellomad;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;
import edu.neu.madcourse.duyvu.dictionary.DictionaryMainActivity;
import edu.neu.madcourse.duyvu.tictactoe.MainActivity;

public class MainMenu extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Globals dictionary = (Globals)getApplication();
        dictionary.setDictionary();
    }

    public void onClickAbout(View view)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            Intent intent = new Intent(MainMenu.this, AboutMe.class);
            startActivity(intent);
        }
    }

    public void onClickTicTacToe(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickDictionary(View view)
    {
        Intent intent = new Intent(this, DictionaryMainActivity.class);
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



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Intent intent = new Intent(MainMenu.this, AboutMe.class);
                    startActivity(intent);

                } else {
                    finish();
                    System.exit(0);
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
