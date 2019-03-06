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

import com.example.firestorechatapp.AppConstants;
import com.example.firestorechatapp.ChatActivity;
import com.example.firestorechatapp.R;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.model.PersonItem;
import com.example.firestorechatapp.recyclerview.PeopleRecyclerAdapte;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class People extends Fragment implements FirebaseCallback {
    RecyclerView recycler_people;
    ListenerRegistration userListenerRegistration;
    Context context;

    public People() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_people, container, false);
        recycler_people=view.findViewById(R.id.recycler_people);

        //register listener for listen data when it changes.
        userListenerRegistration=FirestoreUtil.addUsersListener(this);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(context);
        recycler_people.setLayoutManager(linearLayoutManager);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Override
    public void getUserList(List<PersonItem> userList) {
      //  Toast.makeText(context, ""+userList.size(), Toast.LENGTH_SHORT).show();
        PeopleRecyclerAdapte adapter =new PeopleRecyclerAdapte(userList,this);
        recycler_people.setAdapter(adapter);
    }

    @Override
    public void onRecyclerItemClick(PersonItem personItem) {
        Intent intent=new Intent(context,ChatActivity.class);
        intent.putExtra(AppConstants.USER_NAME,personItem.getUser().getName());
        intent.putExtra(AppConstants.USER_ID,personItem.getId());
        startActivity(intent);
       // Toast.makeText(context, ""+AppConstants.USER_ID, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        //removeListener when fragment is onStop. means changes in data not listen.
        FirestoreUtil.removeListener(userListenerRegistration);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

}
