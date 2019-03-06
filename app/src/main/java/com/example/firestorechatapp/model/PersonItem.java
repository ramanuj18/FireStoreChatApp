package com.example.firestorechatapp.model;

public class PersonItem {
    User user;
    String id;
    public PersonItem(User user,String id){
        this.user=user;
        this.id=id;
    }

    public User getUser() {
        return user;
    }

    public String getId() {
        return id;
    }
}
