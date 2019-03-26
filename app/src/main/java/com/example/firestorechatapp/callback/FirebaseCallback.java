package com.example.firestorechatapp.callback;

import android.net.Uri;
import android.util.Log;

import com.example.firestorechatapp.model.GroupModel;
import com.example.firestorechatapp.model.MessageItem;
import com.example.firestorechatapp.model.PersonItem;
import com.example.firestorechatapp.model.TextMessage;
import com.example.firestorechatapp.model.User;
import com.example.firestorechatapp.model.newGroupModel;

import java.net.URL;
import java.util.Date;
import java.util.List;

public interface FirebaseCallback {
    default void getCurrentUser(User user){

    }
    default void onComplete(String channelId){

    }
    default void onListen(List<MessageItem> messageList){

   }
    default void getPath(String path,String type){

    }
    default void onRecyclerItemClick(PersonItem user){

    }
    default void onStartIntent(){

    }
    default void getUserList(List<PersonItem> userList){

    }
    default void onChatImageClick(String path){

   }
   default void setOnlineStatus(boolean isOnline,Date laseSeen){

   }
   default void onCompleteFCM(List<String> tokens){

   }
   default void onTyping(boolean typing){

   }
   default void getChatChannelStatus(boolean active){

   }
   default void onCheckChange(boolean checked,PersonItem personItem){

   }
   default void getGroupList(List<GroupModel> groupModel){

   }
   default void onGroupItemClick(GroupModel groupModel){

   }
   default void showToast(){

   }
   default void onGroupCreated(){

   }
}
