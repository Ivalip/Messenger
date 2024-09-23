package com.example.mymessenger;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.mymessenger.Database.Entity.ChatMessage;
import java.util.List;

public class ViewModel extends androidx.lifecycle.ViewModel {
    Repository repository;
    MutableLiveData<List<ChatMessage>> mutableLiveData = new MutableLiveData<>();
    public void create(Context context) {
        repository = new Repository(context);
        getAll();
    }
    public void create_for_last(Context context) {
        repository = new Repository(context);
        getLast();
    }
    public void createByID(Context context, String receiver) {
        repository = new Repository(context);
        mutableLiveData = getById(receiver);
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
    public LiveData<List<ChatMessage>> getLast() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                repository.getLast();
//            }
//        }).start();
        return repository.getLast();
    }
    public MutableLiveData<List<ChatMessage>> getById(String number) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mutableLiveData.postValue(repository.getById(number));
            }
        }).start();
        return mutableLiveData;
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
