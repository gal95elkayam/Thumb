package com.example.thumb;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Date;

public class FriendlyMessage implements Serializable{

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public long getMessageTime() {
        return new Date().getTime();
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    private String text;
    private String name;
    private String timeStamp;
    private String fromUserId;
    private long messageTime;

    public FriendlyMessage() {

    }

    public FriendlyMessage(String text, String name, String fromUserId, String timeStamp) {
        this.text = text;
        this.name = name;
        this.fromUserId = fromUserId;
        this.timeStamp = timeStamp;
        this.messageTime = new Date().getTime();
    }
}