package com.example.mymessenger;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    private String[] mData;
    private LayoutInflater mInflater;
    Context context;

    RecyclerViewAdapter(String[] data) {
        this.mData = data;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        this.mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.chatitem, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.myTextView.setText(mData[position]+ " channel");
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
        TextView myTextView;
        MyViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.NumberChat);
        }
    }

}