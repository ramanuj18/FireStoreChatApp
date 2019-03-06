package com.example.firestorechatapp;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.service.MyFirebaseInstanceIDService;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity implements FirebaseCallback {

    private static final int RC_SIGN_IN =1;
    Button btnSignIn;
    List<AuthUI.IdpConfig> signInProvider= Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(true).setRequireName(true).build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        btnSignIn=(Button)findViewById(R.id.sign_in);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(signInProvider).build();
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
              FirestoreUtil.initCurrentUserIfFirstTime(this);
            /*  Intent intent=new Intent(SignInActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                FirestoreUtil.updateOnlineStatus(true,null);*/
            }
            else if(resultCode==RESULT_CANCELED){
                if(response==null) return;
                switch (response.getError().getErrorCode()){
                    case ErrorCodes.NO_NETWORK:{
                        Snackbar.make(findViewById(R.id.constraint_layout),"No Network",Snackbar.LENGTH_LONG).show();
                        break;
                    }
                    case ErrorCodes.UNKNOWN_ERROR:{
                        Snackbar.make(findViewById(R.id.constraint_layout),"Unknown Error",Snackbar.LENGTH_LONG).show();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onStartIntent() {
        String newRegistrationToken=FirebaseInstanceId.getInstance().getToken();
        new MyFirebaseInstanceIDService().addTokenToFirestore(newRegistrationToken);

        Intent intent=new Intent(SignInActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        FirestoreUtil.updateOnlineStatus(true,null);
    }
}
