package edu.neu.madcourse.duyvu.hellomad;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import edu.neu.madcourse.duyvu.R;

public class AboutMe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        TextView textViewTitle = (TextView) findViewById(R.id.about_me_imei);
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        textViewTitle.setText("IMEI: " + telephonyManager.getDeviceId());

    }
}
