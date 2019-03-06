package com.example.firestorechatapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firestorechatapp.GroupChatActivity;
import com.example.firestorechatapp.R;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.model.GroupModel;
import com.example.firestorechatapp.model.newGroupModel;
import com.example.firestorechatapp.recyclerview.GroupRecyclerAdapter;
import com.example.firestorechatapp.util.FireStoreGroupChatUtil;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment implements FirebaseCallback {
    ListenerRegistration listenerRegistration;
    RecyclerView recycler_people;
    Context context;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
                // Inflate the layout for this fragment
            View view=inflater.inflate(R.layout.fragment_people, container, false);
            recycler_people=view.findViewById(R.id.recycler_people);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(context);
        recycler_people.setLayoutManager(linearLayoutManager);
                return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Override
    public void onStart() {
        super.onStart();
      listenerRegistration= FireStoreGroupChatUtil.getGroups(this);
       // listenerRegistration=FireStoreGroupChatUtil.getGroupList(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirestoreUtil.removeListener(listenerRegistration);
    }

    @Override
    public void getGroupList(List<GroupModel> groupModel) {
        Log.d("groupList",""+groupModel.size());
        GroupRecyclerAdapter adapter=new GroupRecyclerAdapter(groupModel,GroupFragment.this);
       recycler_people.setAdapter(adapter);
    }

    @Override
    public void onGroupItemClick(GroupModel groupModel) {
        List<String> otherUserIds=groupModel.getGroupModel().getMembers();
        String groupName=groupModel.getGroupModel().getName();
        String groupId=groupModel.getGroupId();
        Intent groupChatIntent=new Intent(getContext(),GroupChatActivity.class);
        groupChatIntent.putStringArrayListExtra("usrIds",(ArrayList<String>)otherUserIds);
        groupChatIntent.putExtra("groupName",groupName);
        groupChatIntent.putExtra("groupId",groupId);
        startActivity(groupChatIntent);
    }
}
