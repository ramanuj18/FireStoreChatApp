package com.example.firestorechatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            startActivity(new Intent(SplashScreen.this,MainActivity.class));
        }else {
            startActivity(new Intent(SplashScreen.this, SignInActivity.class));
        }
        finish();
    }
}
