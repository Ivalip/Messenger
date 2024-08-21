package com.example.mymessenger.Database.DataDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.example.mymessenger.AllConverter;
import com.example.mymessenger.Database.Entity.ChatMessage;

import java.util.List;


@Dao
public interface DataDAO {
    @Query("SELECT * FROM messages")
    List<ChatMessage> getAll();

    @Query("SELECT * FROM messages WHERE receiver = :receiver ORDER BY time ASC")
    @TypeConverters({AllConverter.class})
    List <ChatMessage> getById(String receiver);

    @Insert
    void insert(ChatMessage message);

    @Update
    void update(ChatMessage message);

    @Delete
    void delete(ChatMessage message);

//    @Query("UPDATE messages SET content = :content, time = :time , " +
//            "sender = :sender, receiver = :receiver")
//    void updateChannelById(String content, String time, int sender, int receiver);

    @Query("DELETE FROM messages WHERE id = :id")
    void deleteById (Long id);

}
