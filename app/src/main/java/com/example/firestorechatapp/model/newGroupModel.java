package com.example.firestorechatapp.model;

import java.util.Date;
import java.util.List;

public class newGroupModel {

   String admin;
   Date createDate;
   String groupBio;
   String groupIconPath;
   List<String> members;
   String name;
   public newGroupModel(){

   }

    public newGroupModel(String admin, Date createDate, String groupBio, String groupIconPath, List<String> members, String name) {
        this.admin = admin;
        this.createDate = createDate;
        this.groupBio = groupBio;
        this.groupIconPath = groupIconPath;
        this.members = members;
        this.name = name;
    }

    public String getAdmin() {
        return admin;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getGroupBio() {
        return groupBio;
    }

    public String getGroupIconPath() {
        return groupIconPath;
    }

    public List<String> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }
}
