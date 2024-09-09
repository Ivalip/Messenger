package com.example.mymessenger;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class AddUserDialog extends DialogFragment implements View.OnClickListener {

    private EditText getEditText;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Добавить пользователя");
        View v = inflater.inflate(R.layout.adduser_fragment, null);
        v.findViewById(R.id.btnAdd).setOnClickListener(this);
        v.findViewById(R.id.btnBack).setOnClickListener(this);
        return v;
    }

    public void onClick(View v) {
        dismiss();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
