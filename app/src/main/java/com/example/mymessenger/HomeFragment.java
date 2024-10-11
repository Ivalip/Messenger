package com.example.mymessenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    AppCompatButton toolbar;
    ImageView addUserBtn;
    AppCompatButton butSendForAll;
    RecyclerViewAdapter adapter;
    List <String> chatsList;
    ViewModel viewModel;
    RecyclerView recyclerView;
    DialogFragment addUserDialog;
    String MyUUID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        Bundle bundle = this.getArguments();
        MyUUID = bundle.getString("MyUUID");
        Log.d("UUID", MyUUID);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        butSendForAll = view.findViewById(R.id.btnForAllSend);
        recyclerView = view.findViewById(R.id.Scroll);
        toolbar = view.findViewById(R.id.toolbar);
        addUserBtn = view.findViewById(R.id.AddChatBtn);

        viewModel = new ViewModel();
        chatsList = new ArrayList<>();
        addUserDialog = new AddUserDialog(viewModel, MyUUID);

        viewModel.createChats(getContext(), MyUUID);
        //Observing our LiveData for showing chats in recyclerView
        viewModel.getChats(MyUUID).observe(getViewLifecycleOwner(), (Observer<? super List<String>>)
                new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> chats) {
                Log.d("HOMEFRGMNT", "Chats changed: " + chats);
                if (chats.size() == 0) {
                    Log.d("EMPTY MESSAGES", "Empty list of messages");
                } else {
                    chatsList.clear();
                    for (int i = 0; i < chats.size(); i++) {
                        chatsList.add(chats.get(i));
                    }
                    adapter.newAddedChat(chatsList);
                }
            }
        });
        adapter = new RecyclerViewAdapter(chatsList, viewModel, getActivity());
        recyclerView.setAdapter(adapter);

        //viewModel.create_for_last(getContext());

        //Observing our LiveData for showing last messages in recyclerView
        viewModel.getLast(MyUUID).observe(getViewLifecycleOwner(), (Observer<? super List<ChatMessage>>)
                new Observer<List<ChatMessage>>() {
                    @Override
                    public void onChanged(List<ChatMessage> messages) {
                        Log.d("NEW_MESSAGE", " ArrayList: " + messages);
                        if (messages.size() == 0) {
                            Log.d("EMPTY MESSAGES", "Empty list of messages");
                        } else {
                            adapter.newAddedData(messages);
//                            messageList.clear();
//                            for (int i = 0; i < messages.size(); i++) {
//                                messageList.add(messages.get(i));
//                            }
                            for(int i = 0; i < messages.size(); ++i){
                                Log.d("LAST_MESSAGE", "Message: " + messages.get(i).content + " Position: " + i);

                            }
                        }
                    }
                });
        @SuppressLint("RestrictedApi")
        MenuBuilder menuBuilder = new MenuBuilder(getContext());
        MenuInflater menuInflater = new MenuInflater(getContext());
        menuInflater.inflate(R.menu.menu, menuBuilder);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                MenuPopupHelper optionsMenu = new MenuPopupHelper(getContext(),
                        menuBuilder, v);
                optionsMenu.setForceShowIcon(true);
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                        if (item.getItemId() == R.id.menu_open_about) {

                        }
                        if (item.getItemId() == R.id.menu_open_settings) {
                            PrefsFragment frag = new PrefsFragment();
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.container, frag);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.addToBackStack(null);
                            ft.commit();
                        }
                        return false;
                    }
                    @Override
                    public void onMenuModeChange(@NonNull MenuBuilder menu) {
                    }
                });
                optionsMenu.show();
            }
        });
        butSendForAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentForAllSend = new Intent(getContext(), ChatActivity.class);
                intentForAllSend.putExtra("ChatID", "0");
                startActivity(intentForAllSend);
            }
        });
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserDialog.show(getFragmentManager(), "adduser_fragment");
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}