package com.example.firestorechatapp.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.firestorechatapp.NewGroupActivity;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.model.GroupModel;
import com.example.firestorechatapp.model.MessageCount;
import com.example.firestorechatapp.model.MessageItem;
import com.example.firestorechatapp.model.PersonItem;
import com.example.firestorechatapp.model.TextMessage;
import com.example.firestorechatapp.model.User;
import com.example.firestorechatapp.model.newGroupModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class FireStoreGroupChatUtil {
    static String groupIcon;
    static String aboutGroup;
    static String groupName;

    static FirebaseFirestore firestoreInstance=FirebaseFirestore.getInstance();

    static DocumentReference currentUserDocRef;
    static CollectionReference groupReference=firestoreInstance.collection("group");

    static CollectionReference chatChannelsCollectionRef=firestoreInstance.collection("groupChatChannels");

    public static void getUserList(FirebaseCallback firebaseCallback){
        firestoreInstance.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                final List<PersonItem> item=new ArrayList<>();
                    queryDocumentSnapshots.getDocuments().forEach(new Consumer<DocumentSnapshot>() {
                        @Override
                        public void accept(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null) {
                                if (!documentSnapshot.getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                    item.add(new PersonItem(documentSnapshot.toObject(User.class), documentSnapshot.getId()));
                                    /*item.add(documentSnapshot.toObject(User.class));*/
                                }
                            }
                        }
                    });
                firebaseCallback.getUserList(item);
            }
        });
    }
    public static void createNewGroup(String name,String aboutGroup,String profilePath,List<String> members,FirebaseCallback firebaseCallback){
      newGroupModel model=new newGroupModel(FirebaseAuth.getInstance().getCurrentUser().getUid(),Calendar.getInstance().getTime(),aboutGroup,profilePath,members,name);
        DocumentReference newGroup=groupReference.document();
        newGroup.set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               new NewGroupActivity().onGroupCreated();
            }
        });

      /*  Map map1=new HashMap();
        map1.put("name",name);
        firestoreInstance.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("engagedGroup").document(newGroup.getId()).set(map1);

        for(int i=0;i<members.size();i++){
            firestoreInstance.collection("users").document(members.get(i))
                    .collection("engagedGroup").document(newGroup.getId()).set(map1);
        }*/
    }

    public static ListenerRegistration getGroups(FirebaseCallback firebaseCallback){
       return firestoreInstance.collection("group").addSnapshotListener(new EventListener<QuerySnapshot>() {
           @Override
           public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
               if(e!=null){
                   return;
               }
               final List<GroupModel> item=new ArrayList<>();
                   queryDocumentSnapshots.getDocuments().forEach(new Consumer<DocumentSnapshot>() {
                       @Override
                       public void accept(DocumentSnapshot documentSnapshot) {
                           List<String> myId = (List<String>) documentSnapshot.get("members");
                           if (myId.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                               Log.d("contains", "myId" + myId.size());
                               item.add(new GroupModel(documentSnapshot.toObject(newGroupModel.class), documentSnapshot.getId()));
                           }
                       }
                   });
               firebaseCallback.getGroupList(item);
           }
       });
    }
    public static void getOrCreateChatChannel(List<String> otherUserIds,final String groupId, final FirebaseCallback firebaseCallback){
        Log.d("userId from utils",groupId);
        firestoreInstance.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("engagedGroup").document(groupId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    firebaseCallback.onComplete((String)documentSnapshot.get("channelId"));
                    return;
                }
                String currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference newChannel=chatChannelsCollectionRef.document();

                MessageCount messageCount=new MessageCount(false,0,false);
                otherUserIds.forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        newChannel.collection("status").document(s).set(messageCount);
                    }
                });
                Map map=new HashMap();
                map.put("channelId",newChannel.getId());
                    otherUserIds.forEach(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                                firestoreInstance.collection("users").document(s).collection("engagedGroup")
                                        .document(groupId)
                                        .set(map);
                        }
                    });
                firebaseCallback.onComplete(newChannel.getId());    //re-check
            }
        });
    }

    public static ListenerRegistration addGroupchatMessagesListener(String channelId,FirebaseCallback firebaseCallback){
        return chatChannelsCollectionRef.document(channelId).collection("messages")
                .orderBy("date")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e!=null){
                            return;
                        }
                        final List<MessageItem> items=new ArrayList<>();
                        queryDocumentSnapshots.getDocuments().forEach(new Consumer<DocumentSnapshot>() {
                            @Override
                            public void accept(DocumentSnapshot documentSnapshot) {
                                items.add(new MessageItem(documentSnapshot.toObject(TextMessage.class),documentSnapshot.getId()));
                                Log.d("messageId",documentSnapshot.getId());
                            }
                        });
                        firebaseCallback.onListen(items);
                    }
                });
    }
    public static void  sendMessage(TextMessage message,String channelId){
        chatChannelsCollectionRef.document(channelId).collection("messages").add(message);
    }

}
