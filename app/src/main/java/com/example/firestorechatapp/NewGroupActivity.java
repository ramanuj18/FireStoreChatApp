package com.example.firestorechatapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.glide.GlideApp;
import com.example.firestorechatapp.model.PersonItem;
import com.example.firestorechatapp.recyclerview.NewUserAdapter;
import com.example.firestorechatapp.util.FireStoreGroupChatUtil;
import com.example.firestorechatapp.util.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NewGroupActivity extends AppCompatActivity implements FirebaseCallback {
    RecyclerView recyclerNewGroup;
    List<String> userIds=new ArrayList<>();
    EditText edtGroupSubject,edtAboutGroup;
    Button btnCreateNewGroup;
    String aboutGroup;
    String groupSubject;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        this.setTitle("New Group");
        recyclerNewGroup=findViewById(R.id.recycleNewGroup);
        edtGroupSubject=findViewById(R.id.edtGroupSubject);
        btnCreateNewGroup=findViewById(R.id.btnCreateNewGroup);
        edtAboutGroup=findViewById(R.id.edtAboutGroup);
        userIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerNewGroup.setLayoutManager(linearLayoutManager);

        FireStoreGroupChatUtil.getUserList(this);


        btnCreateNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupSubject=edtGroupSubject.getText().toString();
                aboutGroup=edtAboutGroup.getText().toString();
                //create new group
                if(!TextUtils.isEmpty(groupSubject)&&groupSubject.trim().length()>4){
                    if(!TextUtils.isEmpty(aboutGroup)&&aboutGroup.trim().length()>4){
                        if(userIds.size()>1){
                            FireStoreGroupChatUtil.createNewGroup(groupSubject,aboutGroup,"",userIds,NewGroupActivity.this);
                        }else {
                            Toast.makeText(NewGroupActivity.this, "please select a user", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        edtAboutGroup.setError("about group have min 5 char");
                    }
                }else {
                    edtGroupSubject.setError("group subject have min 5 char");
                }
            }
        });
    }

    @Override
    public void getUserList(List<PersonItem> userList) {
        Log.d("listSize",""+userList.size());
        NewUserAdapter newUserAdapter=new NewUserAdapter(userList,NewGroupActivity.this);
        recyclerNewGroup.setAdapter(newUserAdapter);
    }

    @Override
    public void onCheckChange(boolean checked, PersonItem personItem) {
        if(checked){
            if(!userIds.contains(personItem.getId())) {
                userIds.add(personItem.getId());
            }
        }else {
            if(userIds.contains(personItem.getId())){
                userIds.remove(personItem.getId());
            }
        }
        Log.d("checked",""+userIds.size());
    }

    @Override
    public void onGroupCreated() {
        Toast.makeText(this, "new group created", Toast.LENGTH_SHORT).show();
        finish();
    }
}
