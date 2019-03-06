package com.example.firestorechatapp.util;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.fragment.MyAccount;
import com.example.firestorechatapp.model.ChatChannel;
import com.example.firestorechatapp.model.MessageCount;
import com.example.firestorechatapp.model.MessageItem;
import com.example.firestorechatapp.model.PersonItem;
import com.example.firestorechatapp.model.TextMessage;
import com.example.firestorechatapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class FirestoreUtil {


        static FirebaseFirestore firestoreInstance=FirebaseFirestore.getInstance();

        static DocumentReference currentUserDocRef=firestoreInstance.document("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

        static CollectionReference chatChannelsCollectionRef=firestoreInstance.collection("chatChannels");

        //init user when user create account first time OR when user already registered. if user successfully signIn then call onStartIntent callback;
        public static void initCurrentUserIfFirstTime(final FirebaseCallback firebaseCallback){
            currentUserDocRef =firestoreInstance.document("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            currentUserDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(!documentSnapshot.exists()){
                        User newUser=new User(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(),"","",true,Calendar.getInstance().getTime(),new ArrayList<String>());
                        currentUserDocRef.set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseCallback.onStartIntent();
                            }
                        });
                    }else {
                        firebaseCallback.onStartIntent();
                    }
                }
            });
        }


        //getCurrentUser() get the information about a user that is currentally signed in. if success then call getCurrentUser() callback.
        public static void getCurrentUser(final FirebaseCallback firebaseCallback){
            currentUserDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                   firebaseCallback.getCurrentUser(documentSnapshot.toObject(User.class));
                }
            });
        }

        //its take three parameter and update user profile. if success call showToast callback.
        public static void updateCurrentUser(String name,String bio,String imagePath){
            Map map=new HashMap();
            if(!name.equals(""))
            map.put("name",name);
            if(!bio.equals(""))
            map.put("bio",bio);
            if(imagePath !=null){
                map.put("profilePicturePath",imagePath);
            }
           currentUserDocRef.update(map).addOnSuccessListener(new OnSuccessListener() {
               @Override
               public void onSuccess(Object o) {
                   MyAccount.showToast1();
               }
           });
        }

        //this method take two parameter and when app is on background or killed then update online status.
        public static  void updateOnlineStatus(boolean online, Date lastSeen){
            Map map=new HashMap();
           map.put("online",online);
           if(lastSeen!=null) {
               map.put("lastSeen", lastSeen);
           }
           currentUserDocRef.update(map);
        }

        //when receiver user read the message then update read status of particular message
        public static  void updateReadStatus(String channelId,String messageId){
            Map map=new HashMap();
            map.put("read",true);
            chatChannelsCollectionRef.document(channelId).collection("messages").document(messageId).update(map);
        }

        //in the firestore database if anything changed in user collection then listen the changes and call getUserList() callback.
        public static ListenerRegistration addUsersListener(final FirebaseCallback firebaseCallback){
            return firestoreInstance.collection("users")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(e!=null){
                                return;
                            }
                            final List<PersonItem> item=new ArrayList<>();
                                queryDocumentSnapshots.getDocuments().forEach(new Consumer<DocumentSnapshot>() {
                                    @Override
                                    public void accept(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot != null) {
                                            if (!documentSnapshot.getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                                item.add(new PersonItem(documentSnapshot.toObject(User.class), documentSnapshot.getId()));
                                                //item.add(documentSnapshot.toObject(User.class));
                                                Log.d("from---->","N");
                                            }
                                        }
                                    }
                                });
                            firebaseCallback.getUserList(item);
                        }
                    });
        }

        //removeListener() remove register listener.
        public static void removeListener(ListenerRegistration listenerRegistration){
            listenerRegistration.remove();
        }

        //when a user wants to chat with another user then this method create a chat channel and add extra thing if user chat first time. else if user already
        //chatted with that person so getChannel and call onComplete() callback.
        public static void getOrCreateChatChannel(final String otherUserId, final FirebaseCallback firebaseCallback){
            Log.d("userId from utils",otherUserId);
            firestoreInstance.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("engagedChatChannels").document(""+otherUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        firebaseCallback.onComplete(documentSnapshot.get("channelId").toString());
                        return;
                    }

                    String currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference newChannel=chatChannelsCollectionRef.document();

                    MessageCount messageCount=new MessageCount(false,0,false);

                    newChannel.collection("status").document(currentUserId).set(messageCount);
                    newChannel.collection("status").document(otherUserId).set(messageCount);

                    Map map=new HashMap();
                    map.put("channelId",newChannel.getId());
                   currentUserDocRef.collection("engagedChatChannels")
                           .document(otherUserId)
                           .set(map);
                   firestoreInstance.collection("users").document(otherUserId)
                           .collection("engagedChatChannels")
                           .document(currentUserId)
                           .set(map);
                   firebaseCallback.onComplete(newChannel.getId());    //re-check
                }
            });
        }


        //when a chad node is active then this method listen all the changes of particular chat channel and call onListen callback.
        public static ListenerRegistration addChatMessagesListener(String channelId, final FirebaseCallback firebaseCallback){
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
                                        items.add(new MessageItem(documentSnapshot.toObject(TextMessage.class), documentSnapshot.getId()));
                                        Log.d("messageId", documentSnapshot.getId());
                                    }
                                });
                            firebaseCallback.onListen(items);
                        }
                    });
        }

        //send message accept two parameter and add message into given channel Id.
        public static void  sendMessage(TextMessage message,String channelId){
            chatChannelsCollectionRef.document(channelId).collection("messages").add(message);
        }

        //getOnlineStatus() accept otherUserId as parameter and get online status of particular user. and call setOnlineStatus callback.
        public static ListenerRegistration getOnlineStatus(String otherUserId,FirebaseCallback firebaseCallback){
           return firestoreInstance.collection("users").document(otherUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
               @Override
               public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                   if(e!=null){
                       return;
                   }
                   boolean isOnline=(boolean)documentSnapshot.get("online");
                   Date lastSeen=(Date)documentSnapshot.get("lastSeen");
                   firebaseCallback.setOnlineStatus(isOnline,lastSeen);
               }
           });
        }

        public static void getFCMRegistrationTokens(FirebaseCallback firebaseCallback){
            currentUserDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user=documentSnapshot.toObject(User.class);
                    firebaseCallback.onCompleteFCM(user.getRegistrationTokens());
                }
            });
        }
        public static void setFCMRegistrationTokens(List<String> registrationTokens){
            Map map=new HashMap();
            map.put("registrationTokens",registrationTokens);
            currentUserDocRef.update(map);
        }

        public static ListenerRegistration getTypingStatus(String channelId,String otherUserId,FirebaseCallback firebaseCallback){
            return chatChannelsCollectionRef.document(channelId).collection("status").document(otherUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if(e!=null){
                                return;
                            }
                            boolean isTyping=(boolean)documentSnapshot.get("typing");
                            firebaseCallback.onTyping(isTyping);
                        }
                    });
        }

        public static void updateTypingStatus(boolean typing,String channelId){
           chatChannelsCollectionRef.document(channelId).collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                   .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
               @Override
               public void onSuccess(DocumentSnapshot documentSnapshot) {
                   Map map=new HashMap();
                   map.put("typing",typing);
                   chatChannelsCollectionRef.document(channelId).collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(map);
               }
           });
        }
        public static  void setUserActiveStatus(boolean activeStatus,String channelId){
            chatChannelsCollectionRef.document(channelId).collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Map map=new HashMap();
                    map.put("active",activeStatus);
                    map.put("count",0);
                    chatChannelsCollectionRef.document(channelId).collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(map);
                }
            });
        }
        public static void addReadMessageCounter(String channelId,String otherUserId){
         chatChannelsCollectionRef.document(channelId).collection("status")
                 .document(otherUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
             @Override
             public void onSuccess(DocumentSnapshot documentSnapshot) {
                 if(!(boolean)documentSnapshot.get("active")){
                     long count=(long)documentSnapshot.get("count");
                     Map map=new HashMap();
                     map.put("count",count+1);
                     chatChannelsCollectionRef.document(channelId).collection("status").document(otherUserId).update(map);
                 }
             }
         });
        }
        public static void getChatChannelActiveStatus(String channelId,FirebaseCallback firebaseCallback){
            chatChannelsCollectionRef.document(channelId).collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    boolean active=(boolean)documentSnapshot.get("active");
                    firebaseCallback.getChatChannelStatus(active);
                }
            });
        }
}

