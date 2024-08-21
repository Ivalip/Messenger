package com.example.mymessenger.Database.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class ChatMessage {

    @ColumnInfo(name = "content")
    public String content;
    @ColumnInfo(name = "time")
    public String time;
    @ColumnInfo(name = "sender")
    public String sender;
    @ColumnInfo(name = "receiver")
    public String receiver;
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    public Long id;

    public ChatMessage(String content, String time, String sender, String receiver) {
        this.content = content;
        this.time = time;
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
