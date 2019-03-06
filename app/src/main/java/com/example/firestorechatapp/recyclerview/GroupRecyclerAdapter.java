package com.example.firestorechatapp.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firestorechatapp.R;
import com.example.firestorechatapp.RoundedImageView;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.glide.GlideApp;
import com.example.firestorechatapp.model.GroupModel;
import com.example.firestorechatapp.model.newGroupModel;
import com.example.firestorechatapp.util.StorageUtil;

import java.util.List;
import java.util.stream.Stream;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {
        Context context;
        List<GroupModel> groupList;
        FirebaseCallback firebaseCallback;

        public GroupRecyclerAdapter(List<GroupModel> list, FirebaseCallback firebaseCallback){
            this.groupList=list;
            this.firebaseCallback=firebaseCallback;
        }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_people_item,viewGroup,false);
        context=viewGroup.getContext();
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        GroupModel groupModel=groupList.get(i);
       String icoPath=groupModel.getGroupModel().getGroupIconPath();

        viewHolder.tvName.setText(groupModel.getGroupModel().getName());
        viewHolder.tvBio.setText(groupModel.getGroupModel().getGroupBio());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            firebaseCallback.onGroupItemClick(groupModel);
            }
        });

        if(!icoPath.equals("")){
            GlideApp.with(context)
                    .load(groupModel.getGroupModel().getGroupIconPath())
                    .placeholder(R.drawable.ic_profile_green)
                    .into(viewHolder.personImage);
        }else {
            viewHolder.personImage.setImageResource(R.drawable.ic_profile_green);
        }
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView personImage;
        TextView tvName,tvBio;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            personImage=itemView.findViewById(R.id.imageView_profile_picture);
            tvName=itemView.findViewById(R.id.textView_name);
            tvBio=itemView.findViewById(R.id.textView_bio);
        }
    }
}
