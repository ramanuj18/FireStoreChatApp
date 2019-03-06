package com.example.firestorechatapp.model;

public class GroupModel {
    newGroupModel groupModel;
    String groupId;

    public GroupModel(newGroupModel groupModel, String groupId) {
        this.groupModel = groupModel;
        this.groupId = groupId;
    }

    public newGroupModel getGroupModel() {
        return groupModel;
    }

    public String getGroupId() {
        return groupId;
    }
}
