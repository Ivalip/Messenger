package com.example.mymessenger;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.util.ArrayList;

public class AddUsersAdapter extends RecyclerView.Adapter<AddUsersAdapter.MyAddViewHolder>{
    ArrayList<String> users;
    Context context;
    LayoutInflater mInflater;
    String user;
    ViewModel viewModel;

    public AddUsersAdapter(ViewModel viewModel) {
        this.viewModel = viewModel;
    }
    public void insertUsers() {
        try {
            this.users = new ArrayList<>(ServiceLocator.getNotificationService().net.graph.keySet());
        } catch (NullPointerException e) {
            Log.d("GRAPH DOESNT EXIST", "PUM PUM PUM");
            this.users = new ArrayList<>();
        }
    }
    @Override
    public MyAddViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        this.mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.useritem, parent, false);
        return new MyAddViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAddViewHolder holder, int position) {
        Log.d("POS", position+"");
        holder.userIDTextView.setText(users.get(position));
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = holder.userIDTextView.getText().toString();
                viewModel.insert(new ChatMessage("",
                        "",
                        "", user, "USER"));
                removeAt(position);
            }
        });
    }
    public void removeAt(int position) {
        users.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    public class MyAddViewHolder extends RecyclerView.ViewHolder {
        TextView userIDTextView;
        TextView add;
        public MyAddViewHolder(@NonNull View itemView) {
            super(itemView);
            userIDTextView = itemView.findViewById(R.id.AddUserBut);
        }
    }
}
