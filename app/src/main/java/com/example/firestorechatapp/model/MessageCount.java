package com.example.firestorechatapp.model;

public class MessageCount {
    boolean active;
    int count;
    boolean typing;

    public MessageCount(boolean active, int count, boolean typing) {
        this.active = active;
        this.count = count;
        this.typing = typing;
    }

    public boolean isActive() {
        return active;
    }

    public int getCount() {
        return count;
    }

    public boolean isTyping() {
        return typing;
    }
}
