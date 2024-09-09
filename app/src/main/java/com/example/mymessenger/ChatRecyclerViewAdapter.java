package com.example.mymessenger;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_SENT = 0;
    public static final int VIEW_TYPE_RECEIVE = 1;
    private Context context;
    ViewModel viewModel = new ViewModel();
    private LayoutInflater mInflater;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    @Override
    public int getItemViewType (int position) {
        if (chatMessages.get(position).sender == "0") {
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVE;
        }
    }

    public ChatRecyclerViewAdapter(Context context, List<ChatMessage> messageList, ViewModel
                                   viewModel) {
        this.viewModel = viewModel;
        chatMessages = messageList;
        this.context = context;
    }
    public void newAddedData(List <ChatMessage> messages){
        chatMessages = messages;
        notifyDataSetChanged();
    }


    @Override
    public NewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            this.mInflater = LayoutInflater.from(parent.getContext());
            view = mInflater.inflate(R.layout.chat_sent_item, parent, false);
            return new NewViewHolder(view);
        }
        else {
            this.mInflater = LayoutInflater.from(parent.getContext());
            view = mInflater.inflate(R.layout.chat_receive_item, parent, false);
            return new NewViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final NewViewHolder viewHolder = (NewViewHolder) holder;
        viewHolder.content.setText(chatMessages.get(position).content);
        String tm = chatMessages.get(position).time;
        viewHolder.time.setText(tm.substring(tm.indexOf(":") + 1, tm.length()-3));
        viewHolder.deleteBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.deleteById(chatMessages.get(position).id);
                removeAt(position);
            }
        });
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
