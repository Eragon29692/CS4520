package edu.neu.madcourse.duyvu.communication.GamePlayDemo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String score;
    public String playing;
    public String status;
    public String data;
    public String userId;


    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String userId){
        this.username = username;
        this.score = "0";
        //the opponent player's id
        this.playing = "";
        this.status = "Online";
        this.data = "";
        this.userId = userId;
    }

    @Override
    public String toString() {
        return username;
    }

}
