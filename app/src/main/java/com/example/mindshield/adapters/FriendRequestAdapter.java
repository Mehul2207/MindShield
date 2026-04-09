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

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    public interface AcceptListener {
        void onAccept(User user);
    }

    public interface RejectListener {
        void onReject(User user);
    }

    private ArrayList<User> requestList;
    private AcceptListener acceptListener;
    private RejectListener rejectListener;

    public FriendRequestAdapter(ArrayList<User> requestList,
                                AcceptListener acceptListener,
                                RejectListener rejectListener) {
        this.requestList = requestList;
        this.acceptListener = acceptListener;
        this.rejectListener = rejectListener;
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

        User user = requestList.get(position);

        holder.name.setText(user.name);
        holder.email.setText(user.email);

        holder.acceptBtn.setOnClickListener(v -> acceptListener.onAccept(user));
        holder.rejectBtn.setOnClickListener(v -> rejectListener.onReject(user));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, email;
        Button acceptBtn, rejectBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.requestName);
            email = itemView.findViewById(R.id.requestEmail);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
        }
    }
}