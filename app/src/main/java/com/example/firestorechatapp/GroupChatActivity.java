package com.example.firestorechatapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.glide.GlideApp;
import com.example.firestorechatapp.model.MessageItem;
import com.example.firestorechatapp.model.TextMessage;
import com.example.firestorechatapp.model.User;
import com.example.firestorechatapp.recyclerview.ChatRecyclerView;
import com.example.firestorechatapp.recyclerview.GroupChatRecyclerView;
import com.example.firestorechatapp.util.FireStoreGroupChatUtil;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.example.firestorechatapp.util.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity implements FirebaseCallback {
        String groupName;
        List<String> otherUserIds;
        String groupId;
        TextView txtOnline,tootbarTitle;
        ImageView imageBack,imgCamera;
        String currentChannelId;
        ListenerRegistration messageListenerRegistration;
        FloatingActionButton fabSendMsg;
        TextInputEditText edtMessage;
        User currentUser;
        GroupChatRecyclerView adapter;
        RecyclerView recyclerMessage;
        byte[] selectedImageBytes;
        Dialog dialog;
       ProgressDialog progressDialog;
    private final int RC_SELECT_IMAGE=2;
    private final int RC_CAMERA=3;
        LinearLayoutManager layoutManager;
    String[] mimeTypes =
            {"image/png","image/jpeg","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                    "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                    "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                    "text/plain",
                    "application/pdf"};
    static final int MY_PERMISSIONS_REQUEST_CAMERA=101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        recyclerMessage=(RecyclerView)findViewById(R.id.recycler_view_messages);
        layoutManager=new LinearLayoutManager(this);
        recyclerMessage.setLayoutManager(layoutManager);
        txtOnline=(TextView)findViewById(R.id.txtOnlineStatus);
        tootbarTitle=(TextView)findViewById(R.id.toolbar_title);
        imgCamera=(ImageView)findViewById(R.id.imageViewCamera);
        imageBack=(ImageView)findViewById(R.id.imageBack);
        fabSendMsg=(FloatingActionButton) findViewById(R.id.fab_send_msg);
        edtMessage=(TextInputEditText)findViewById(R.id.editText_message);
        Intent intent=getIntent();
        groupName=intent.getStringExtra("groupName");
        otherUserIds=intent.getStringArrayListExtra("usrIds");
        groupId=intent.getStringExtra("groupId");


        tootbarTitle.setText(groupName);
        txtOnline.setVisibility(View.GONE);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        FirestoreUtil.getCurrentUser(this);
        FireStoreGroupChatUtil.getOrCreateChatChannel(otherUserIds,groupId,this);


        fabSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edtMessage.getText().toString().isEmpty()){
                    TextMessage textMessage=new TextMessage(edtMessage.getText().toString(),FirebaseAuth.getInstance()
                            .getUid(),"TEXT",Calendar.getInstance().getTime(),false,"",currentUser.getName());
                    FireStoreGroupChatUtil.sendMessage(textMessage,currentChannelId);
                    edtMessage.setText("");
                }
            }
        });
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectionDialog();
            }
        });
    }

    @Override
    public void onComplete(String channelId) {
        currentChannelId=channelId;
        Log.d("groupChannelId",channelId);
       messageListenerRegistration=FireStoreGroupChatUtil.addGroupchatMessagesListener(channelId,GroupChatActivity.this);

    }

    @Override
    public void getCurrentUser(User user) {
        currentUser=user;
    }

    @Override
    public void onListen(List<MessageItem> messageList) {
        adapter=new GroupChatRecyclerView(messageList,this,"",currentChannelId);
        recyclerMessage.setAdapter(adapter);
        recyclerMessage.scrollToPosition(recyclerMessage.getAdapter().getItemCount()-1);
    }

    private void showSelectionDialog(){
        dialog = new Dialog(GroupChatActivity.this, R.style.DialogSlideAnim);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.layout_choose);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER | Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.findViewById(R.id.tvFromCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions( GroupChatActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, RC_CAMERA);
                }
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.tvFromGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==RC_SELECT_IMAGE && resultCode==RESULT_OK && data!=null){
            if(data.getClipData()!=null){
                showProgressBar();
                ClipData mClipData=data.getClipData();
                for(int i=0;i<mClipData.getItemCount();i++){
                    ClipData.Item item=mClipData.getItemAt(i);
                    Uri uri=item.getUri();
                    StorageUtil.uploadMessageImageFile(uri,this);
                }
            }else if(data.getData()!=null){
                Uri selectedImagePath = data.getData();
                showProgressBar();
                StorageUtil.uploadMessageImageFile(selectedImagePath,this);
            }
        }
        /*if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {
                Uri selectedImagePath = data.getData();
                showProgressBar();
                StorageUtil.uploadMessageImageFile(selectedImagePath,this);
        }*/
        else if(requestCode==RC_CAMERA && resultCode==Activity.RESULT_OK && data!=null){
            Bitmap selectedImageBmp = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG,90,outputStream);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), selectedImageBmp, "Title", null);
            selectedImageBytes= outputStream.toByteArray();
            showProgressBar();
            StorageUtil.uploadMessageImage(selectedImageBytes,this);
        }
    }

    @Override
    public void getPath(String path,String type) {
        TextMessage messageTosend=new TextMessage(path,FirebaseAuth.getInstance().getUid(),type,Calendar.getInstance().getTime(),false,"",currentUser.getName());
        FireStoreGroupChatUtil.sendMessage(messageTosend,currentChannelId);
        progressDialog.dismiss();
    }

    private void showProgressBar(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Sending Image");
        progressDialog.show();
    }
    @Override
    public void onChatImageClick(String path) {
        showFullImageDialog(path);
    }

    private void showFullImageDialog(String imgPath){
        ImageView imageView;
        dialog = new Dialog(GroupChatActivity.this);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.full_screen_imageview);
      /*  Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);*/
        imageView=dialog.findViewById(R.id.fullScreenImage);
        GlideApp.with(this)
                .load(imgPath)
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(imageView);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
