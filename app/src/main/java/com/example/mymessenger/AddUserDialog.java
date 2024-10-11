package com.example.mymessenger;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddUserDialog extends DialogFragment {
    RecyclerViewAdapter adapterR;
    String MyUuid;
    RecyclerView recyclerView;
    ArrayList<String> users;
    AddUsersAdapter adapter;
    ViewModel viewModel;

    public AddUserDialog(ViewModel viewModel, String MyUuid, RecyclerViewAdapter adapter) {
        this.viewModel = viewModel;
        this.MyUuid = MyUuid;
        this.adapterR = adapter;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Добавить пользователя");
        View v = inflater.inflate(R.layout.adduser_fragment, null);
        recyclerView = v.findViewById(R.id.UsersScroll);
        try {
            users = new ArrayList<>(ServiceLocator.getNotificationService().net.graph.keySet());
            users.remove(MyUuid);
            ArrayList<String> chats = new ArrayList<>(viewModel.getChats(MyUuid).getValue());
            for (int i = 0; i < chats.size(); i++) {
                users.remove(chats.get(i));
            }
            Log.d("ADDUSER", chats+"");
        } catch (NullPointerException e) {
            Log.d("GRAPH DOESNT EXIST", "PUM PUM PUM");
            users = new ArrayList<>();
        }
        adapter = new AddUsersAdapter(viewModel, users, MyUuid, adapterR);

        //adapter.insertUsers();
        recyclerView.setAdapter(adapter);
        v.findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        v.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
