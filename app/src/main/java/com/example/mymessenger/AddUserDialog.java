package com.example.mymessenger;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

public class AddUserDialog extends DialogFragment {
    RecyclerView recyclerView;
    AddUsersAdapter adapter;
    ViewModel viewModel;

    public AddUserDialog(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Добавить пользователя");
        View v = inflater.inflate(R.layout.adduser_fragment, null);
        recyclerView = v.findViewById(R.id.UsersScroll);
        adapter = new AddUsersAdapter(viewModel);
        adapter.insertUsers();
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
