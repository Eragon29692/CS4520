package edu.neu.madcourse.duyvu.communication.GamePlayDemo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String score;
    public String playing;



    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String score){
        this.username = username;
        this.score = score;
        //the opponent player's id
        this.playing = null;
    }

}
