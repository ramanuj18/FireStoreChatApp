package com.example.firestorechatapp.model;

import java.util.Date;
import java.util.List;

public class User {

    String name;
    String bio;
    String profilePicturePath;
    boolean online;
    Date lastSeen;
    List<String> registrationTokens;
    public User(){

    }
    public User(String name, String bio, String profilePicturePath,boolean online,Date lastSeen,List<String> registrationTokens) {
        this.name = name;
        this.bio = bio;
        this.profilePicturePath = profilePicturePath;
        this.online=online;
        this.lastSeen=lastSeen;
        this.registrationTokens=registrationTokens;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public boolean isOnline() {
        return online;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public List<String> getRegistrationTokens() {
        return registrationTokens;
    }
}
