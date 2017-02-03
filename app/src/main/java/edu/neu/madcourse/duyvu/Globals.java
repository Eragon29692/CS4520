package edu.neu.madcourse.duyvu;

import android.app.Application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Created by duyvu on 2/3/2017.
 */

public class Globals extends Application{
    private HashSet<String> dictionary = new HashSet<String>();

    public void setDictionary() {
        InputStream fis = null;
        try {
            fis = new FileInputStream("raw/wordlist.txt");
            BufferedReader r = new BufferedReader( new InputStreamReader(fis));
            String line;

            while ((line = r.readLine()) != null) {
                dictionary.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDictionary(String word) {
        return dictionary.contains(word);
    }

}
