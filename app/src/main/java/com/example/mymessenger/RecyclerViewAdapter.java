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

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    ViewModel viewModel;
    private String[] mData;
    private LayoutInflater mInflater;
    Context context;
    FragmentActivity fragmentActivity;

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
        viewModel.createByID(context, position + "");
        List<ChatMessage> messageList = new ArrayList<>();
        //Observing our LiveData for showing it in recyclerView.
        viewModel.mutableLiveData.observe(fragmentActivity, (Observer<? super List<ChatMessage>>)
                            new Observer<List<ChatMessage>>() {
                        @Override
                        public void onChanged(List<ChatMessage> messages) {
                        Log.d("NEW_MESSAGE", " ArrayList: " + messageList);
                        if (messages.size() == 0) {
                            Log.d("EMPTY MESSAGES", "Empty list of messages");
                        } else {
                            messageList.clear();
                            for (int i = 0; i < messages.size(); i++) {
                                messageList.add(messages.get(i));
                            }
                            Log.d("LAST_MESSAGE", "Message: " + messages.get(messages.size() - 1).content + " Position: " + position);
                            holder.lastMessage.setText(messages.get(messages.size() - 1).content);
                            String time = messages.get(messages.size() - 1).time;
                            holder.timeView.setText(time.substring(time.indexOf(":")+1, time.indexOf(":")+6));
                        }
                    }
                });
        holder.title.setText(mData[position]+ " channel");
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