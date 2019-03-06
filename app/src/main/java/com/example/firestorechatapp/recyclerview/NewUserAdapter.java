package com.example.firestorechatapp.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.firestorechatapp.R;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.model.PersonItem;

import java.util.List;

public class NewUserAdapter extends RecyclerView.Adapter<NewUserAdapter.ViewHolder> {
    List<PersonItem> userList;
    Context context;
    FirebaseCallback callback;

    public NewUserAdapter(List<PersonItem> userList, FirebaseCallback firebaseCallback){
        this.userList=userList;
        this.callback=firebaseCallback;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_list_item,viewGroup,false);
        context=viewGroup.getContext();
        NewUserAdapter.ViewHolder viewHolder=new NewUserAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        viewHolder.checkBox.setText(userList.get(i).getUser().getName());
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                callback.onCheckChange(b,userList.get(i));
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox=itemView.findViewById(R.id.checkbox);
        }
    }
}
