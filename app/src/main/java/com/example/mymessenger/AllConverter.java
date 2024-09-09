package com.example.mymessenger;

import androidx.room.TypeConverter;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.util.List;
public class AllConverter {
    @TypeConverter
    public List<ChatMessage> fromMessages(List<ChatMessage> chatMessages) {
        return chatMessages;
    }
}
