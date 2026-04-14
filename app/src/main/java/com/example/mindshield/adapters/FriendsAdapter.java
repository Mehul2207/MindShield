package com.example.mindshield.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mindshield.R;
import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.models.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    public interface FriendClickListener {
        void onClick(User user);
    }

    private ArrayList<User> friendList;
    private FriendClickListener listener;

    public FriendsAdapter(ArrayList<User> friendList,
                          FriendClickListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        User user = friendList.get(position);

        holder.name.setText(user.name);
        
        // Load Avatar
        if (user.profilePic != null && !user.profilePic.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.profilePic)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Fetch Last Message
        fetchLastMessage(user.userId, holder.lastMessage, holder.time);

        holder.itemView.setOnClickListener(v -> listener.onClick(user));
    }

    private void fetchLastMessage(String friendId, TextView lastMsgText, TextView timeText) {
        String myUid = FirebaseManager.getUserId();
        if (myUid == null) return;

        FirebaseDatabase.getInstance("https://mindshield-a2b70-default-rtdb.firebaseio.com")
                .getReference("userChats")
                .child(myUid)
                .child(friendId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String msg = snapshot.child("lastMessage").getValue(String.class);
                            Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                            lastMsgText.setText(msg != null ? msg : "No messages yet");
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                timeText.setText(sdf.format(new Date(timestamp)));
                            } else {
                                timeText.setText("");
                            }
                        } else {
                            lastMsgText.setText("Tap to start chatting");
                            timeText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, lastMessage, time;
        ShapeableImageView avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.chatTime);
            avatar = itemView.findViewById(R.id.chatAvatar);
        }
    }
}