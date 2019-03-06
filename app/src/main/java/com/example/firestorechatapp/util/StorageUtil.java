package com.example.firestorechatapp.util;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.firestorechatapp.callback.FirebaseCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import java.util.Calendar;
import java.util.Objects;

public class StorageUtil {

   public static FirebaseStorage storageInstance=FirebaseStorage.getInstance();

   public static   StorageReference currentUserRef;

    public static void uploadProfilePhoto(Uri profile, final FirebaseCallback firebaseCallback){
        currentUserRef=storageInstance.getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        final StorageReference ref=currentUserRef.child("profilePictures/"+Calendar.getInstance().getTimeInMillis());
        ref.putFile(profile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                firebaseCallback.getPath(uri.toString(),taskSnapshot.getMetadata().getContentType());
                            }
                        });
            }
        });
    }

    public static StorageReference pathToReference(String path){
        return storageInstance.getReference(path);}

        public  static void uploadMessageImage(byte[] imageByte,final FirebaseCallback firebaseCallback){
            currentUserRef=storageInstance.getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        final StorageReference ref=currentUserRef.child("messages/"+Calendar.getInstance().getTimeInMillis());
        ref.putBytes(imageByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                firebaseCallback.getPath(uri.toString(),taskSnapshot.getMetadata().getContentType());
                            }
                        });

            }
        });
        }
            public static void uploadMessageImageFile(Uri data, final FirebaseCallback firebaseCallback){
                currentUserRef=storageInstance.getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                final StorageReference ref=currentUserRef.child("messages/"+Calendar.getInstance().getTimeInMillis());
                ref.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        firebaseCallback.getPath(uri.toString(),taskSnapshot.getMetadata().getContentType());
                                    }
                                });

                    }
                });

            }

            public static void downLoadFile(String url,String extension,FirebaseCallback firebaseCallback){
                File location=new File(Environment.getExternalStorageDirectory()+ "/"+"fireStorechatApp"+"/");
                if(!location.exists()){
                    location.mkdir();//If there is no folder it will be created.
                }
                String ext =null;
                File tempFile = null;
                if(extension!=null) {
                   ext= MimeTypeMap.getSingleton().getExtensionFromMimeType(extension);
                }
                   tempFile=new File(location,Calendar.getInstance().getTimeInMillis()+"."+ext);
                StorageReference httpsReference=storageInstance.getReferenceFromUrl(url);
                httpsReference.getFile(tempFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.d("downloading","completed");
                                firebaseCallback.showToast();
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                   /*     double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d("progress----->",""+progress);*/
                    }
                });
            }
}
