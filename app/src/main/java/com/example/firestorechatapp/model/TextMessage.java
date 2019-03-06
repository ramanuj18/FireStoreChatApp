package com.example.firestorechatapp.model;

import java.util.Date;

public class TextMessage {
    String textMessage;
    String senderId;
    String type;
    Date date;
    boolean read;
    String recipientId;
    String senderName;
    public TextMessage(){

    }

    public TextMessage(String textMessage, String senderId,String type, Date date,boolean read,String recipientId,String senderName) {
        this.textMessage = textMessage;
        this.senderId = senderId;
        this.type=type;
        this.date = date;
        this.read=read;
        this.recipientId=recipientId;
        this.senderName=senderName;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public boolean isRead() {
        return read;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getSenderName() {
        return senderName;
    }
}
