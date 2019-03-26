package com.example.firestorechatapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.glide.GlideApp;
import com.example.firestorechatapp.model.MessageItem;
import com.example.firestorechatapp.model.TextMessage;
import com.example.firestorechatapp.model.User;
import com.example.firestorechatapp.recyclerview.ChatRecyclerView;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.example.firestorechatapp.util.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity implements FirebaseCallback {
    String userName;
    String otherUserId;
    String currentChannelId=null;
    ListenerRegistration messagesListenerRegistration;
    TextInputEditText edtMessage;
    ImageView imgGallery,imgCamera,imageBack;
    RecyclerView recyclerMessage;
    LinearLayoutManager layoutManager;
    FloatingActionButton fabSendMsg;
    byte[] selectedImageBytes;
    Dialog dialog;
    ChatRecyclerView adapter;
    TextView txtOnline,tootbarTitle;
    ListenerRegistration listenerRegistration;
    List<MessageItem> list=new ArrayList<>();
   ProgressDialog progressDialog;
   String onlineStatus="";
   ListenerRegistration typingListener;
   User currentUser;
   RoundedImageView imageProfile;
    public boolean wait=false;
   static final int MY_PERMISSIONS_REQUEST_CAMERA=101;
    public static final List<String> types = Collections
            .unmodifiableList(new ArrayList<String>() {
                {
                    add("image/jpeg");
                    add("image/png");
                }
            });
    String[] mimeTypes =
            {"image/png","image/jpeg","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                    "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                    "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                    "text/plain",
                    "application/pdf"};
    private final int RC_SELECT_IMAGE=2;
    private final int RC_CAMERA=3;

    long delay = 1500; // 1 seconds after user stops typing
    long last_text_edit = 0;
    Handler handler = new Handler();

    Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                Log.d("status","typing stop");
                FirestoreUtil.updateTypingStatus(false,currentChannelId);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      // getSupportActionBar().hide();
        setContentView(R.layout.activity_chat_layout);
        recyclerMessage=(RecyclerView)findViewById(R.id.recycler_view_messages);
        layoutManager=new LinearLayoutManager(this);
        recyclerMessage.setLayoutManager(layoutManager);
        edtMessage=(TextInputEditText)findViewById(R.id.editText_message);
        imgGallery=(ImageView)findViewById(R.id.imageViewGallery);
        fabSendMsg=(FloatingActionButton) findViewById(R.id.fab_send_msg);
        imgCamera=(ImageView)findViewById(R.id.imageViewCamera);
        txtOnline=(TextView)findViewById(R.id.txtOnlineStatus);
        tootbarTitle=(TextView)findViewById(R.id.toolbar_title);
        imageBack=(ImageView)findViewById(R.id.imageBack);
        imageProfile=(RoundedImageView)findViewById(R.id.imageProfile);

        Intent intent=getIntent();
        userName= intent.getStringExtra(AppConstants.USER_NAME);
        Log.d("userName",intent.getStringExtra(AppConstants.USER_NAME));
        otherUserId= intent.getStringExtra(AppConstants.USER_ID);
        Log.d("userId",intent.getStringExtra(AppConstants.USER_ID));

        tootbarTitle.setText(userName);

        FirestoreUtil.getCurrentUser(this);

        FirestoreUtil.getOrCreateChatChannel(otherUserId,this);
     //  Toast.makeText(this, ""+userName+userId, Toast.LENGTH_SHORT).show();

        fabSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edtMessage.getText().toString().isEmpty()){
                    TextMessage textMessage=new TextMessage(edtMessage.getText().toString(),FirebaseAuth.getInstance()
                    .getUid(),"TEXT",Calendar.getInstance().getTime(),false,otherUserId,currentUser.getName());
                    FirestoreUtil.sendMessage(textMessage,currentChannelId);
                    edtMessage.setText("");
                    FirestoreUtil.addReadMessageCounter(currentChannelId,otherUserId);
                }
            }
        });
        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES,types.toArray());
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE);*/
            }
        });
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, RC_CAMERA);*/
               showSelectionDialog();
            }
        });
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    handler.removeCallbacks(input_finish_checker);
                FirestoreUtil.updateTypingStatus(true,currentChannelId);
            }

            @Override
            public void afterTextChanged(Editable editable) {
             //   if (editable.length() > 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
              //  }
            }
        });
    }

    @Override
    public void onComplete(String channelId) {
       // Toast.makeText(ChatActivity.this, "complete"+channelId, Toast.LENGTH_SHORT).show();
        currentChannelId=channelId;
        messagesListenerRegistration=FirestoreUtil.addChatMessagesListener(currentChannelId,this);
        typingListener=FirestoreUtil.getTypingStatus(channelId,otherUserId,this);
        FirestoreUtil.setUserActiveStatus(true,currentChannelId);
    }
    @Override
    public void onListen(List<MessageItem> messageList) {
        adapter=new ChatRecyclerView(messageList,this,otherUserId,currentChannelId);
        recyclerMessage.setAdapter(adapter);
      recyclerMessage.scrollToPosition(recyclerMessage.getAdapter().getItemCount()-1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==RC_SELECT_IMAGE && resultCode==Activity.RESULT_OK && data!=null){
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

      /*  if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
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
       TextMessage messageTosend=new TextMessage(path,FirebaseAuth.getInstance().getUid(),type,Calendar.getInstance().getTime(),false,otherUserId,currentUser.getName());
       FirestoreUtil.sendMessage(messageTosend,currentChannelId);
       progressDialog.dismiss();
    }

    @Override
    public void onTyping(boolean typing) {
        if(typing){
            txtOnline.setText("typing...");
        }else {
            txtOnline.setText(onlineStatus);
        }
    }

    @Override
    public void onChatImageClick(String path) {
      //  Toast.makeText(this, ""+path, Toast.LENGTH_SHORT).show();
        showFullImageDialog(path);
    }
    private void showFullImageDialog(String imgPath){
        ImageView imageView;
        dialog = new Dialog(ChatActivity.this);
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
    private void showSelectionDialog(){
        dialog = new Dialog(ChatActivity.this, R.style.DialogSlideAnim);
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
                if (ContextCompat.checkSelfPermission(ChatActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions( ChatActivity.this,
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
               // intent.setType("image/*");
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
    public void setOnlineStatus(boolean isOnline,Date lastSeen) {
       // setOnLineStatusInTextView(isOnline,lastSeen);
        if(isOnline){
            txtOnline.setText("Online");
            onlineStatus="Online";
        }else {
            String str=getOnlineStatus(lastSeen);
            txtOnline.setText(str);
            onlineStatus=str;
        }
    }
    private void setOnLineStatusInTextView(boolean isOnline,Date lastSeen){
        if(!wait){
            if(isOnline){
                txtOnline.setText("Online");
                wait=true;
            }else {
              txtOnline.setText(getOnlineStatus(lastSeen));
                wait=true;
            }
        }else {
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isOnline){
                                txtOnline.setText("Online");
                                wait=false;
                            }else {
                              txtOnline.setText(getOnlineStatus(lastSeen));
                            }
                            wait=false;
                        }
                    });
                }
            }, 10000);
        }
    }

    private String getOnlineStatus(Date lastSeen){
        SimpleDateFormat timeFormat= (SimpleDateFormat) SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        String time=timeFormat.format(lastSeen);

        SimpleDateFormat dateFormat= (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
        String date=dateFormat.format(lastSeen);

        String currentDate=dateFormat.format(Calendar.getInstance().getTime());
        if(date.equals(currentDate)) {
         return  "seen today" + " at " + time;
        }
        else{  return "seen "+parseDateToddMMyyyy(date)+" at "+time;}
    }

    @Override
    protected void onStart() {
        super.onStart();
       listenerRegistration= FirestoreUtil.getOnlineStatus(otherUserId,this);
       if(currentChannelId!=null){
           FirestoreUtil.setUserActiveStatus(true,currentChannelId);
       }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirestoreUtil.removeListener(listenerRegistration);
    }
    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "MM/dd/yy";
        String outputPattern = "dd MMM";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
    private void showProgressBar(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Sending Image");
        progressDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode){
           case MY_PERMISSIONS_REQUEST_CAMERA:{
               if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                   startActivityForResult(cameraIntent, RC_CAMERA);
               }
           }
       }
    }
    @Override
    protected void onStop() {
        super.onStop();
        FirestoreUtil.setUserActiveStatus(false,currentChannelId);
        FirestoreUtil.removeListener(typingListener);
    }

    @Override
    public void getCurrentUser(User user) {
        currentUser=user;
    }

}
