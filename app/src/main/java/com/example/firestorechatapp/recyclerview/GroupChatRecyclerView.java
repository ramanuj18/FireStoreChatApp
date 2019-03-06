package com.example.firestorechatapp.recyclerview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorechatapp.R;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.glide.GlideApp;
import com.example.firestorechatapp.model.MessageItem;
import com.example.firestorechatapp.model.TextMessage;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.example.firestorechatapp.util.StorageUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GroupChatRecyclerView extends RecyclerView.Adapter<GroupChatRecyclerView.ViewHolder> implements FirebaseCallback {

    public static List<MessageItem> messageList;
    Context context;
    FirebaseCallback listener;
    String otherUserId;
    String channelId;
    int MY_PERMISSIONS_REQUEST_STORAGE=102;
    public GroupChatRecyclerView(List<MessageItem> messageList, FirebaseCallback listener,String otherUserId,String channelId){
        this.messageList=messageList;
        this.listener=listener;
        this.otherUserId=otherUserId;
        this.channelId=channelId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_group_message,viewGroup,false);
        context=viewGroup.getContext();
       ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i){
        TextMessage textMessage=messageList.get(i).getTextMessage();
        String message=messageList.get(i).getTextMessage().getTextMessage();
        String senderName=messageList.get(i).getTextMessage().getSenderName();
        String[] firstSenderName=senderName.split(" ",2);

        SimpleDateFormat timeFormat= (SimpleDateFormat) SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        String time=timeFormat.format(messageList.get(i).getTextMessage().getDate());

        SimpleDateFormat dateFormat= (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
        String date=dateFormat.format(messageList.get(i).getTextMessage().getDate());

        if(i>=1){
            String sender=messageList.get(i-1).getTextMessage().getSenderName();
            if(senderName.equals(sender)){
                viewHolder.txtSenderName.setVisibility(View.GONE);
            }else {
                viewHolder.txtSenderName.setVisibility(View.VISIBLE);
            }
        }else {
            viewHolder.txtSenderName.setVisibility(View.VISIBLE);
        }
        if(messageList.get(i).getTextMessage().getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            FrameLayout.LayoutParams layoutParams= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.END);
            viewHolder.messageNameRoot.setLayoutParams(layoutParams);
            viewHolder.messageRoot.setBackgroundResource(R.drawable.message_bg);
            viewHolder.txtSenderName.setVisibility(View.GONE);
        }else {
            FrameLayout.LayoutParams layoutParams= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.START);
            viewHolder.messageNameRoot.setLayoutParams(layoutParams);
            viewHolder.messageRoot.setBackgroundResource(R.drawable.message_bg_left);
        }

        viewHolder.textViewMessageTime.setText(time);
        viewHolder.tvDate.setText(parseDateToddMMyyyy(date));
        viewHolder.txtSenderName.setText(firstSenderName[0]);


        if(messageList.get(i).getTextMessage().getType().equals("TEXT")){
            viewHolder.textViewMessageText.setVisibility(View.VISIBLE);
            viewHolder.constraintImage.setVisibility(View.GONE);
            viewHolder.textViewMessageText.setText(message);
        }else if(messageList.get(i).getTextMessage().getType().startsWith("image")){
            viewHolder.textViewMessageText.setVisibility(View.GONE);
            viewHolder.constraintImage.setVisibility(View.VISIBLE);
            GlideApp.with(context)
                    .load(message)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(viewHolder.messageImage);
        }else {
            //TODO:if file type is pdf,doc,xl,txt etc
            viewHolder.textViewMessageText.setVisibility(View.GONE);
            viewHolder.constraintImage.setVisibility(View.VISIBLE);
            viewHolder.messageImage.setImageResource(R.drawable.ic_file);
        }
        viewHolder.imageDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions( (Activity) context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                }else {
                    downloadImage(message,messageList.get(i).getTextMessage().getType());
                }
            }
        });
        viewHolder.messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(messageList.get(i).getTextMessage().getType().startsWith("image")) {
                    listener.onChatImageClick(message);
                }
            }
        });


        if(i>=1){
            String prevDate=dateFormat.format(messageList.get(i-1).getTextMessage().getDate());
            if(date.equals(prevDate)){
                viewHolder.tvDate.setVisibility(View.GONE);
            }else {
                viewHolder.tvDate.setVisibility(View.VISIBLE);
            }
        }else {
            viewHolder.tvDate.setVisibility(View.VISIBLE);
        }
        //for set read mark when message is read
        if(textMessage.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            if(messageList.get(i).getTextMessage().isRead()){
                viewHolder.readMark.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.readMark.setVisibility(View.GONE);
            }
        }else {
            viewHolder.readMark.setVisibility(View.GONE);
        }

        //for update read status when message unread.
        if(textMessage.getSenderId().equals(otherUserId)){
            if(!textMessage.isRead()){
                //update read status to firebase.
                FirestoreUtil.updateReadStatus(channelId,messageList.get(i).getMessageId());
            }
        }
    }
    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView textViewMessageText,textViewMessageTime,tvDate,txtSenderName;
        LinearLayout messageRoot;
        ImageView messageImage,readMark,imageDownload;
        ConstraintLayout constraintImage;
        LinearLayout messageNameRoot;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessageText=itemView.findViewById(R.id.textView_message_text);
            textViewMessageTime=itemView.findViewById(R.id.textView_message_time);
            messageRoot=itemView.findViewById(R.id.message_root);
            tvDate=itemView.findViewById(R.id.tvDate);
            messageImage=itemView.findViewById(R.id.imageView_message_image);
            readMark=itemView.findViewById(R.id.readMark);
            constraintImage=itemView.findViewById(R.id.constraintImage);
            imageDownload=itemView.findViewById(R.id.imageDownload);
            messageNameRoot=itemView.findViewById(R.id.messageNameRoot);
            txtSenderName=itemView.findViewById(R.id.senderName);
        }
    }
    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "MM/dd/yy";
        String outputPattern = "dd MMMM yyyy";
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
    class DownloadFile extends AsyncTask<String,Integer,Long> {
        ProgressDialog mProgressDialog = new ProgressDialog(context);// Change Mainactivity.this with your activity name.
        String strFolderName;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Downloading");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.show();
        }
        @Override
        protected Long doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL((String) aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                String targetFileName=Calendar.getInstance().getTimeInMillis() +".jpeg";
                int lenghtOfFile = conexion.getContentLength();
                String PATH = Environment.getExternalStorageDirectory()+ "/"+"fireStorechatApp"+"/";
                File folder = new File(PATH);
                if(!folder.exists()){
                    folder.mkdir();//If there is no folder it will be created.
                }
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(PATH+targetFileName);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress ((int)(total*100/lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setProgress(progress[0]);
            if(mProgressDialog.getProgress()==mProgressDialog.getMax()){
                mProgressDialog.dismiss();
                Toast.makeText(context, "File Downloaded", Toast.LENGTH_SHORT).show();
            }
        }
        protected void onPostExecute(String result) {
        }
    }
    private void downloadImage(String message,String type){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setMessage("Do you want to download this image?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StorageUtil.downLoadFile(message,type,GroupChatRecyclerView.this);
               // new DownloadFile().execute(message);
            }
        }).setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    @Override
    public void showToast() {
        Toast.makeText(context, "file download", Toast.LENGTH_SHORT).show();
    }
}