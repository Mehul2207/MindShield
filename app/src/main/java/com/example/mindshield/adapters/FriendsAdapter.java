package com.example.mindshield.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindshield.R;
import com.example.mindshield.models.User;

import java.util.ArrayList;

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
                .inflate(R.layout.item_friend_request, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        User user = friendList.get(position);

        holder.name.setText(user.name);
        holder.email.setText(user.email);

        holder.chatBtn.setText("Chat");

        holder.chatBtn.setOnClickListener(v -> listener.onClick(user));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, email;
        Button chatBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.requestName);
            email = itemView.findViewById(R.id.requestEmail);
            chatBtn = itemView.findViewById(R.id.acceptBtn);

            itemView.findViewById(R.id.rejectBtn).setVisibility(View.GONE);
        }
    }
}