package com.example.mymessenger;

import android.content.Context;
import android.util.Log;


import com.example.mymessenger.Database.DataDAO.DataDAO;
import com.example.mymessenger.Database.Entity.ChatMessage;
import java.util.List;

public class Repository {

    AppDatabase db;
    DataDAO dataDAO;
    public Repository(Context context) {
        db = AppDatabase.getInstance(context);
        dataDAO = db.getDataDao();
    }
    public List<ChatMessage> getAll() {
        return dataDAO.getAll();
    }
    public List<ChatMessage> getById(String receiver) {
        Log.d("POLUCHENIYE PO ID", "NE POLUCHIL");
        return dataDAO.getById(receiver);
    }
    public void insert(ChatMessage message) {
        dataDAO.insert(message);
    }
    public void update(ChatMessage message) {
        dataDAO.update(message);
    }
    public void deleteById (Long id) {
        dataDAO.deleteById(id);
    }
}
