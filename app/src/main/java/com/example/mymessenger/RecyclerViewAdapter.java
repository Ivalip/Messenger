package com.example.mymessenger;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    ViewModel viewModel;
    String lastMessage;
    List<ChatMessage> lastMessages;
    private String[] mData;
    private LayoutInflater mInflater;
    Context context;
    FragmentActivity fragmentActivity;

    public void newAddedData(List <ChatMessage> lastMessages){
        this.lastMessages = lastMessages;
        notifyDataSetChanged();
    }

    RecyclerViewAdapter(String[] data, ViewModel viewModel, FragmentActivity activity) {
        this.mData = data;
        this.viewModel = viewModel;
        this.fragmentActivity = activity;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        this.mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.chatitem, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) { // 0, 1, 2
        holder.title.setText(mData[position]+ " channel");
        try {
            holder.lastMessage.setText(lastMessages.get(position+1).content);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        holder.myTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("Number", String.valueOf(position));
                startActivity(context, intent, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView myTextView;
        TextView lastMessage;
        TextView timeView;
        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            lastMessage = itemView.findViewById(R.id.last_message);
            myTextView = itemView.findViewById(R.id.NumberChat);
            timeView = itemView.findViewById(R.id.message_time);
        }
    }

}