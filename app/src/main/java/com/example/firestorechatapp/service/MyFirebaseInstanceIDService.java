package com.example.firestorechatapp.service;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.List;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService implements FirebaseCallback {
    public static String newRegistrationToken;

    @Override
    public void onTokenRefresh() {
        newRegistrationToken= FirebaseInstanceId.getInstance().getToken();
       if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            addTokenToFirestore(newRegistrationToken);
       }
    }

    public void addTokenToFirestore(String registrationToken){
        newRegistrationToken=registrationToken;
        try {
            if (newRegistrationToken == null){}
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        FirestoreUtil.getFCMRegistrationTokens(MyFirebaseInstanceIDService.this);
    }
    @Override
    public void onCompleteFCM(List<String> tokens) {
        if(tokens.contains(newRegistrationToken)){
            return;
        }
        tokens.add(newRegistrationToken);
        FirestoreUtil.setFCMRegistrationTokens(tokens);
    }
}
