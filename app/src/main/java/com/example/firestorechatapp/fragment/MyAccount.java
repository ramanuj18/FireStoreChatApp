package com.example.firestorechatapp.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firestorechatapp.R;
import com.example.firestorechatapp.RoundedImageView;
import com.example.firestorechatapp.SignInActivity;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.glide.GlideApp;
import com.example.firestorechatapp.model.User;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.example.firestorechatapp.util.StorageUtil;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccount extends Fragment implements FirebaseCallback {

    boolean pictureJustChanged=false;
    TextInputEditText edtName,edtBio;
    Button btnSave,btnSignout;
    RoundedImageView imageView;
    Uri profilePicture;
    static Context context;

    public static final List<String> types = Collections
            .unmodifiableList(new ArrayList<String>() {
                {
                    add("image/jpeg");
                    add("image/png");
                }
            });
    private int RC_SELECT_IMAGE=2;


    public MyAccount() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_my_account, container, false);
        findViews(view);
        context=getContext();
        return view;
    }

    private void findViews(View view){
        edtName=view.findViewById(R.id.editText_name);
        edtBio=view.findViewById(R.id.editText_bio);
        btnSave=view.findViewById(R.id.btn_save);
        btnSignout=view.findViewById(R.id.btn_sign_out);
        imageView=view.findViewById(R.id.imageView_profile_picture);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES,types.toArray());
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(profilePicture!=null){
                    StorageUtil.uploadProfilePhoto(profilePicture,MyAccount.this);
                }else {
                    FirestoreUtil.updateCurrentUser(edtName.getText().toString(),edtBio.getText().toString(),null);
                }
            }
        });
        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance().signOut(getContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirestoreUtil.updateOnlineStatus(false,Calendar.getInstance().getTime());
                                Intent intent=new Intent(getContext(),SignInActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {
            Uri selectedImagePath = data.getData();
            profilePicture=selectedImagePath;
            try {
                Bitmap selectedImageBmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImagePath);

                GlideApp.with(this).load(selectedImageBmp)
                        .into(imageView);
                pictureJustChanged=true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }

    @Override
    public void onStart() {
        super.onStart();
        FirestoreUtil.getCurrentUser(this);
    }

    @Override
    public void getCurrentUser(User user) {
        if(MyAccount.this.isVisible()){
            edtName.setText(user.getName());
            edtBio.setText(user.getBio());
            if(!pictureJustChanged && !user.getProfilePicturePath().equals("")){
                GlideApp.with(this)
                        .load(user.getProfilePicturePath())
                        .placeholder(R.drawable.ic_profile_green)
                        .into(imageView);
            }
        }
    }

    @Override
    public void getPath(String path,String type) {
            FirestoreUtil.updateCurrentUser(edtName.getText().toString(),edtBio.getText().toString(),path);
    }
    public static void showToast1(){
        Toast.makeText(context, "detail updated", Toast.LENGTH_SHORT).show();
    }
}

