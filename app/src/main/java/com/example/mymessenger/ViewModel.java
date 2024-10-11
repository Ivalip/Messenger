package com.example.mymessenger;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.mymessenger.Database.Entity.ChatMessage;
import java.util.List;

public class ViewModel extends androidx.lifecycle.ViewModel {
    Repository repository;
    MutableLiveData<List<ChatMessage>> mutableLiveData = new MutableLiveData<>();
    MutableLiveData<List<String>> users = new MutableLiveData<>();
//    public void create_for_last(Context context) {
//        repository = new Repository(context);
//        getLast();
//    }
    public void createByID(Context context, String receiver, String MyUuid) {
        repository = new Repository(context);
        mutableLiveData = getById(receiver, MyUuid);
    }
    public void createChats (Context context, String Myuuid) {
        repository = new Repository(context);
        users = getChats(Myuuid);
    }
    public MutableLiveData<List<ChatMessage>> getAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
               mutableLiveData.postValue(repository.getAll());
            }
        }).start();
        return mutableLiveData;
    }
    public LiveData<List<ChatMessage>> getLast(String user) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                repository.getLast();
//            }
//        }).start();
        return repository.getLast(user);
    }
    public MutableLiveData<List<ChatMessage>> getById(String chatID, String MyUuid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mutableLiveData.postValue(repository.getById(chatID, MyUuid));
            }
        }).start();
        return mutableLiveData;
    }

    public MutableLiveData<List<String>> getChats(String userID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                users.postValue(repository.getChats(userID));
            }
        }).start();
        return users;
    }

    public void insert(ChatMessage message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.insert(message);
            };
        }).start();
    }
    public void deleteById (Long id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.deleteById(id);
            };
        }).start();
    }
    public void update(ChatMessage message) {
        repository.update(message);
    }

}
