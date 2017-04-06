package edu.neu.madcourse.duyvu.twoplayergame.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User implements Comparable<User> {

    public String username;
    public String score;
    public String playing;
    public String status;
    public String data;
    public String userId;
    public String rankScore;


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
        this.rankScore = "0";
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public int compareTo(User userB) {
        if (Integer.parseInt(rankScore) > Integer.parseInt(userB.rankScore))
            return -1;
        else
            return 1;
    }
}
