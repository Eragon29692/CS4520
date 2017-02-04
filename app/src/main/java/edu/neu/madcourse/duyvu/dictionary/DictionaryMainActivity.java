package edu.neu.madcourse.duyvu.dictionary;

import android.content.Intent;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import edu.neu.madcourse.duyvu.Globals;
import edu.neu.madcourse.duyvu.R;

public class DictionaryMainActivity extends AppCompatActivity {
    private ArrayList<String> listItems = new ArrayList<String>();
    private ListView listView;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_main);
        EditText editText = (EditText) findViewById(R.id.dictionaryEditText);
        editText.addTextChangedListener(onTextChange);
        listView = (ListView) findViewById(R.id.dictionaryListView);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.dictionary_listview_text, listItems));
        mMediaPlayer = MediaPlayer.create(this, R.raw.beep_sound);
        mMediaPlayer.setVolume(1f, 1f);
    }

    public void onClickClear(View view)
    {
        listItems.clear();
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    public void onClickReturn(View view)
    {
        finish();
    }

    public void onClickAcknowledgement(View view)
    {
        Intent intent = new Intent(DictionaryMainActivity.this, DictionaryAcknowledgeActivity.class);
        startActivity(intent);
    }

    private TextWatcher onTextChange = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            Globals dictionary = (Globals)getApplication();
            if (s.length() >=3 && dictionary.checkDictionary(s.toString().toLowerCase())) {
                mMediaPlayer.start();
                if (!listItems.contains(s.toString())) {
                    listItems.add(s.toString().toLowerCase());
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    //listView.smoothScrollToPosition(listItems.size());
                    listView.post(new Runnable(){
                        public void run() {
                            listView.setSelection(listView.getCount() - 1);
                        }});
                }
            }
        }
    };


}
