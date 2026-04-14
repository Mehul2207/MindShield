package com.example.mindshield.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mindshield.R;
import com.example.mindshield.models.Post;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.userName.setText(post.userName);
        holder.content.setText(post.content);
        holder.score.setText("Score: " + post.score);
        holder.visibility.setText(post.visibility.toUpperCase());

        String timeAgo = DateUtils.getRelativeTimeSpanString(post.timestamp, 
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        holder.time.setText(timeAgo);

        // Load Avatar
        if (post.userProfilePic != null && !post.userProfilePic.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.userProfilePic)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Health Data
        if (post.steps != null) {
            holder.stepsContainer.setVisibility(View.VISIBLE);
            holder.stepsText.setText("👣 " + post.steps);
        } else {
            holder.stepsContainer.setVisibility(View.GONE);
        }

        if (post.heartRate != null) {
            holder.hrContainer.setVisibility(View.VISIBLE);
            holder.hrText.setText("❤️ " + post.heartRate);
        } else {
            holder.hrContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, content, score, time, visibility, stepsText, hrText;
        ShapeableImageView avatar;
        LinearLayout stepsContainer, hrContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.postUserName);
            content = itemView.findViewById(R.id.postContent);
            score = itemView.findViewById(R.id.postScore);
            time = itemView.findViewById(R.id.postTime);
            visibility = itemView.findViewById(R.id.postVisibility);
            avatar = itemView.findViewById(R.id.postUserAvatar);
            stepsText = itemView.findViewById(R.id.postSteps);
            hrText = itemView.findViewById(R.id.postHR);
            stepsContainer = itemView.findViewById(R.id.postStepsContainer);
            hrContainer = itemView.findViewById(R.id.postHRContainer);
        }
    }
}