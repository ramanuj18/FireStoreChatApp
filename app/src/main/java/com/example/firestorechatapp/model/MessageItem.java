package com.example.firestorechatapp.model;

public class MessageItem {
    TextMessage textMessage;
    String messageId;

    public MessageItem(TextMessage textMessage, String messageId) {
        this.textMessage = textMessage;
        this.messageId = messageId;
    }

    public TextMessage getTextMessage() {
        return textMessage;
    }

    public String getMessageId() {
        return messageId;
    }
}
