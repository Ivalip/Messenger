package com.example.mymessenger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_SENT = 0;
    public static final int VIEW_TYPE_RECEIVE = 1;
    public static final int VIEW_TYPE_SENT_GEO = 2;
    public static final int VIEW_TYPE_RECEIVE_GEO = 3;
    private Context context;
    String MyUuid;
    String tm;
    ViewModel viewModel = new ViewModel();
    private LayoutInflater mInflater;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    Activity activity;

    @Override
    public int getItemViewType (int position) {
        SharedPreferences sharedPref = context.getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        MyUuid = sharedPref.getString("uuid_key", "");
        //Log.d("UUID_M", chatMessages.get(position).sender);
        //Log.d("MyUuid", MyUuid);
        if (chatMessages.get(position).type.equals("GEO")) {
            if (chatMessages.get(position).sender.equals(MyUuid)) {
                return VIEW_TYPE_SENT_GEO;
            } else {
                return VIEW_TYPE_RECEIVE_GEO;
            }
        } else {
            if (chatMessages.get(position).sender.equals(MyUuid)) {
                return VIEW_TYPE_SENT;
            } else {
                return VIEW_TYPE_RECEIVE;
            }
        }
    }

    public ChatRecyclerViewAdapter(Context context, List<ChatMessage> messageList, ViewModel
                                   viewModel, Activity activity) {
        this.viewModel = viewModel;
        chatMessages = messageList;
        this.context = context;
        this.activity = activity;
    }
    public void newAddedData(List <ChatMessage> messages){
        chatMessages = messages;
        notifyDataSetChanged();
    }

    @Override
    public NewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        Log.d("ViewType", viewType+ "");
        View view;
        if (viewType == VIEW_TYPE_SENT || viewType == VIEW_TYPE_SENT_GEO) {
            this.mInflater = LayoutInflater.from(parent.getContext());
            view = mInflater.inflate(R.layout.message_item_1, parent, false);
            return new NewViewHolder(view);
        }
        else {
            this.mInflater = LayoutInflater.from(parent.getContext());
            view = mInflater.inflate(R.layout.message_item_2, parent, false);
            return new NewViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final NewViewHolder viewHolder = (NewViewHolder) holder;
        int type = getItemViewType(position);
        switch(type) {
            case VIEW_TYPE_SENT:
            case VIEW_TYPE_RECEIVE:
                viewHolder.content.setText(chatMessages.get(position).content);
                tm = chatMessages.get(position).time;
                viewHolder.time.setText(tm.substring(tm.indexOf(":") + 1, tm.length() - 3));
                viewHolder.deleteBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.deleteById(chatMessages.get(position).id);
                        removeAt(position);
                    }
                });
            break;
            case VIEW_TYPE_SENT_GEO:
            case VIEW_TYPE_RECEIVE_GEO:
                viewHolder.content.setText(chatMessages.get(position).content);
                tm = chatMessages.get(position).time;
                viewHolder.time.setText(tm.substring(tm.indexOf(":") + 1, tm.length() - 3));
                viewHolder.deleteBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.deleteById(chatMessages.get(position).id);
                        removeAt(position);
                    }
                });
                viewHolder.content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("DEST", viewHolder.content.getText().toString());
                        Compass compass = new Compass();
                        compass.setArguments(bundle);
                        FragmentTransaction mFragmentTransaction = ((AppCompatActivity) ((NewViewHolder) holder).content.getContext()).getSupportFragmentManager().beginTransaction();
                        mFragmentTransaction.replace(R.id.CompassContainer, compass);
                        mFragmentTransaction.addToBackStack(null).commit();
                        //open nav tab
                    }
                });
            break;
        }
    }
    public void removeAt(int position) {
        chatMessages.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public class NewViewHolder extends RecyclerView.ViewHolder {
        public TextView content;
        public TextView time;
        public TextView deleteBut;
        public NewViewHolder(View itemView) {
                super(itemView);
                deleteBut = itemView.findViewById(R.id.DeleteBut);
                content = itemView.findViewById(R.id.TEXT);
                time = itemView.findViewById(R.id.TIME);
            }
        }
        @Override
        public int getItemCount () {
            return chatMessages.size();
        }
}
