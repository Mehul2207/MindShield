package com.example.mindshield.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mindshield.R;
import com.example.mindshield.models.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> messages;
    private String currentUserId;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(Message message, int position);
    }

    private OnMessageLongClickListener longClickListener;

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }

    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);

        if (msg.senderId != null && msg.senderId.equals(currentUserId)) {
            return 1; // sent
        } else {
            return 0; // received
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message msg = messages.get(position);

        // ✅ FIXED: use msg.text instead of msg.message
        if (msg.text != null && !msg.text.isEmpty()) {
            holder.messageText.setText(msg.text);
        } else {
            holder.messageText.setText("");
        }
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMessageLongClick(msg, position);
            }
            return true;
        });
    }

    public void removeMessage(int position) {
        messages.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}