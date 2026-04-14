package com.example.mindshield.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mindshield.R;
import com.example.mindshield.models.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final ArrayList<User> userList;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(ArrayList<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Name display with fallback
        holder.userName.setText(user.name != null && !user.name.isEmpty() ? user.name : "Anonymous");

        // Fixed Email Fetch: Handle potential null values from Firebase
        // If user.email is null, it shows a futuristic placeholder
        if (user.email != null && !user.email.isEmpty()) {
            holder.userEmail.setText(user.email);
        } else {
            holder.userEmail.setText("Identity Protected");
        }

        // Load profile image using Glide (matches the futuristic neon UI)
        if (user.profilePic != null && !user.profilePic.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.profilePic)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(holder.userAvatar);
        } else {
            holder.userAvatar.setImageResource(R.drawable.ic_profile_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail;
        ImageView userAvatar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userAvatar = itemView.findViewById(R.id.userAvatar);
        }
    }
}