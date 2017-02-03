package edu.neu.madcourse.duyvu;

import android.app.Application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


public class Globals extends Application{
    private HashMap<String, Integer> dictionary = new HashMap<>();

    public void setDictionary() {
        try {
            InputStream fis = getResources().openRawResource(R.raw.wordlist_80);
            BufferedReader r = new BufferedReader( new InputStreamReader(fis));
            String line;

            while ((line = r.readLine()) != null) {

                String[] lineProcess = line.split(",");
                for (int i = 80; i < 100 && i < lineProcess.length; i++) {
                    dictionary.put(lineProcess[i], 0);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDictionary(String word) {
        return dictionary.containsKey(word);
    }

}
