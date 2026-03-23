package com.example.mindshield;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindshield.adapters.ChatAdapter;
import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.models.Message;
import com.example.mindshield.utils.PressAnimationHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private Button sendButton;

    private ArrayList<Message> messageList;
    private ChatAdapter adapter;

    private final String CURRENT_USER = "USER001";
    private final String FRIEND_USER = "USER002";

    private ValueEventListener messageListener;

    public ChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.chatRecycler);
        inputMessage = view.findViewById(R.id.inputMessage);
        sendButton = view.findViewById(R.id.sendButton);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, CURRENT_USER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 🔥 Animate send button
        PressAnimationHelper.applyPressAnimation(sendButton);

        // 🔥 Animate input container safely
        ViewParent parent = inputMessage.getParent();
        if (parent instanceof View) {
            PressAnimationHelper.applyPressAnimation((View) parent);
        }

        // 🔹 Send message
        sendButton.setOnClickListener(v -> {

            String text = inputMessage.getText().toString().trim();

            if (!TextUtils.isEmpty(text)) {

                Message msg = new Message(
                        CURRENT_USER,
                        FRIEND_USER,
                        text,
                        System.currentTimeMillis()
                );

                FirebaseManager.getChatRoom(CURRENT_USER, FRIEND_USER)
                        .push()
                        .setValue(msg);

                inputMessage.setText("");
            }
        });

        // 🔹 Receive messages
        messageListener = FirebaseManager.getChatRoom(CURRENT_USER, FRIEND_USER)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        messageList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            Message msg = data.getValue(Message.class);

                            if (msg != null) {
                                messageList.add(msg);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (!messageList.isEmpty()) {
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (messageListener != null) {
            FirebaseManager.getChatRoom(CURRENT_USER, FRIEND_USER)
                    .removeEventListener(messageListener);
        }
    }
}