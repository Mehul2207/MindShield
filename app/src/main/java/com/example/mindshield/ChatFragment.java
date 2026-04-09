package com.example.mindshield;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.mindshield.ai.OpenRouterService;
import com.example.mindshield.ai.ContextBuilder;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindshield.adapters.ChatAdapter;
import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.models.Message;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private Button sendButton;

    private ArrayList<Message> messageList;
    private ChatAdapter adapter;

    private String currentUserId;
    private String friendUserId;

    private DatabaseReference messagesRef;
    private ChildEventListener messageListener;
    private OpenRouterService aiService = new OpenRouterService();

    private static final String AI_USER_ID = "AI_BOT";

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

        currentUserId = FirebaseManager.getUserId();

        Bundle args = getArguments();
        if (args != null) {
            friendUserId = args.getString("friendUserId");
        }

        if (currentUserId == null || friendUserId == null) {
            return;
        }

        messagesRef = FirebaseManager.getMessages(currentUserId, friendUserId);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnMessageLongClickListener((message, position) -> {
            showDeleteDialog(message, position);
        });

        // Friend verification first
        FirebaseManager.getFriends(currentUserId)
                .child(friendUserId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists() || friendUserId.equals(AI_USER_ID)) {

                        sendButton.setOnClickListener(v -> sendMessage());

                        attachMessageListener();

                    } else {

                        sendButton.setEnabled(false);

                        Toast.makeText(
                                getContext(),
                                "Only friends can chat",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void sendMessage() {

        String text = inputMessage.getText().toString().trim();

        if (TextUtils.isEmpty(text)) return;

        String messageId = messagesRef.push().getKey();

        if (messageId == null) return;

        Message message = new Message(
                messageId,
                currentUserId,
                friendUserId,
                text,
                System.currentTimeMillis(),
                "text"
        );

        messagesRef.child(messageId).setValue(message);

        updateUserChats(text);

        if (friendUserId.equals(AI_USER_ID)) {

            ContextBuilder.buildContext(getContext(), context -> {

                aiService.getChatResponse(text, context, new OpenRouterService.AIResponseCallback() {
                    @Override
                    public void onResponse(String response) {

                        String aiMessageId = messagesRef.push().getKey();
                        if (aiMessageId == null) return;

                        Message aiMessage = new Message(
                                aiMessageId,
                                "AI_BOT",
                                currentUserId,
                                response,
                                System.currentTimeMillis(),
                                "text"
                        );

                        requireActivity().runOnUiThread(() -> {
                            messagesRef.child(aiMessageId).setValue(aiMessage);
                            updateUserChats(response);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "AI Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });

            });
        }

        inputMessage.setText("");
    }

    private void showDeleteDialog(Message message, int position) {

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    deleteMessageFromFirebase(message);
                    adapter.removeMessage(position);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMessageFromFirebase(Message message) {

        if (message == null || message.messageId == null) return;

        messagesRef.child(message.messageId).removeValue();
    }
    private void attachMessageListener() {

        messageListener = messagesRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String prevChildKey) {

                Message msg = snapshot.getValue(Message.class);

                if (msg != null) {
                    messageList.add(msg);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                // Not needed now, but MUST exist
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

                Message deletedMsg = snapshot.getValue(Message.class);

                if (deletedMsg == null) return;

                for (int i = 0; i < messageList.size(); i++) {
                    if (messageList.get(i).messageId.equals(deletedMsg.messageId)) {
                        messageList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String s) {
                // Not used
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Optional: log error
            }
        });
    }

    private void updateUserChats(String lastMessage) {

        DatabaseReference userChatsRef = FirebaseDatabase
                .getInstance("https://mindshield-a2b70-default-rtdb.firebaseio.com")
                .getReference("userChats");

        long time = System.currentTimeMillis();

        userChatsRef.child(currentUserId)
                .child(friendUserId)
                .child("lastMessage")
                .setValue(lastMessage);

        userChatsRef.child(currentUserId)
                .child(friendUserId)
                .child("timestamp")
                .setValue(time);

        userChatsRef.child(friendUserId)
                .child(currentUserId)
                .child("lastMessage")
                .setValue(lastMessage);

        userChatsRef.child(friendUserId)
                .child(currentUserId)
                .child("timestamp")
                .setValue(time);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (messageListener != null && messagesRef != null) {
            messagesRef.removeEventListener(messageListener);
        }
    }
}