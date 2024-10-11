package com.example.mymessenger.Database.DataDAO;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    @Query("SELECT *, MAX(time) FROM messages WHERE receiver = :user OR (sender = :user and receiver != :reciever) GROUP BY receiver")
    LiveData<List<ChatMessage>> getLast(String user, String reciever);

    @Query("SELECT * FROM messages WHERE ((receiver = :receiver AND sender = :user) OR" +
            " (receiver = :user AND sender = :receiver) OR (receiver = :zero AND receiver = :receiver)) AND time != :zero ORDER BY time ASC")
    List <ChatMessage> getById(String receiver, String user, String zero);

    @Query("SELECT sender FROM messages WHERE receiver = :user_id GROUP BY sender ORDER BY MAX(time) DESC")
    List <String> getChats(String user_id);



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
