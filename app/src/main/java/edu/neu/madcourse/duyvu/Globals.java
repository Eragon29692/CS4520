package edu.neu.madcourse.duyvu;

import android.app.Application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;


public class Globals extends Application{
    private HashMap<String, Integer> dictionary = new HashMap<>();
    private ArrayList<String> arrayDictionary = new ArrayList<>();
    int loadState = 0;
    public void setDictionary() {
        try {
            InputStream fis;
            if (loadState == 0) {
                fis = getResources().openRawResource(R.raw.wordlist_150_1);
                loadState = 1;
            }
            else if (loadState == 1) {
                fis = getResources().openRawResource(R.raw.wordlist_150_2);
                loadState = 2;
            } else {
                return;
            }
            BufferedReader r = new BufferedReader( new InputStreamReader(fis));
            String line;


            while ((line = r.readLine()) != null) {

                String[] lineProcess = line.split(",");
                for (int i = 0; i < 150 && i < lineProcess.length; i++) {
                    arrayDictionary.add(lineProcess[i]);
                }
                //for (int i = 0; i < 80 && i < lineProcess.length; i++) {
                //    dictionary.put(lineProcess[i], 0);
                //}
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDictionary(String word) {
        return arrayDictionary.contains(word);
    }

}
