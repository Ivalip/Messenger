package com.example.mymessenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_SENT = 0;
    public static final int VIEW_TYPE_RECEIVE = 1;
    private Context context;
    String MyUuid;
    ViewModel viewModel = new ViewModel();
    private LayoutInflater mInflater;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    @Override
    public int getItemViewType (int position) {
        SharedPreferences sharedPref = context.getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        MyUuid = sharedPref.getString("uuid_key", "");
        Log.d("UUID_M", chatMessages.get(position).sender);
        Log.d("MyUuid", MyUuid);
        if (chatMessages.get(position).sender.equals(MyUuid)) {
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
        Log.d("ViewType", viewType+"");
        View view;
        if (viewType == VIEW_TYPE_SENT) {
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
