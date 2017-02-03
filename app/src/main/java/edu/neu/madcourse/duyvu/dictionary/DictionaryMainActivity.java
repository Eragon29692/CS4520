package edu.neu.madcourse.duyvu.dictionary;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

public class DictionaryMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_main);
        Globals dictionary = (Globals)getApplication();
        if(dictionary.checkDictionary("electrotints")) {
            Log.d("tag","hhhhhhhhhhhhTrue");
        } else {
            Log.d("tag","hhhhhhhhhhhhfalse");
        }
    }

    public void onClickReturn(View view)
    {
        finish();
    }
}
