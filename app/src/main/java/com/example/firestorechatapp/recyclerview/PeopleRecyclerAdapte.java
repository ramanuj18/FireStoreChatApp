package com.example.firestorechatapp.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firestorechatapp.R;
import com.example.firestorechatapp.RoundedImageView;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.glide.GlideApp;
import com.example.firestorechatapp.model.PersonItem;
import com.example.firestorechatapp.model.User;
import com.example.firestorechatapp.util.StorageUtil;

import java.util.List;

public class PeopleRecyclerAdapte extends RecyclerView.Adapter<PeopleRecyclerAdapte.ViewHolder> {
    List<PersonItem> userList;
    FirebaseCallback listener;
    Context context;
    public PeopleRecyclerAdapte(List<PersonItem> userList,FirebaseCallback listener){
        this.listener=listener;
        this.userList=userList;
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
        final PersonItem personItem=userList.get(i);
        final User user=userList.get(i).getUser();
        String imagePath=user.getProfilePicturePath();
        String name=user.getName();
        String bio=user.getBio();

        viewHolder.tvName.setText(name);
        viewHolder.tvBio.setText(bio);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecyclerItemClick(personItem);
            }
        });
        if(!imagePath.equals("")){
            GlideApp.with(context)
                    .load(user.getProfilePicturePath())
                    .placeholder(R.drawable.ic_profile_green)
                    .into(viewHolder.personImage);
        }else {
            viewHolder.personImage.setImageResource(R.drawable.ic_profile_green);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
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
