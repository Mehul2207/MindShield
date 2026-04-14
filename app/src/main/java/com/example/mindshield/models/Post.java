package com.example.mindshield.models;

import java.util.HashMap;
import java.util.Map;

public class Post {
    public String postId;
    public String userId;
    public String userName;
    public String userProfilePic;
    public String content;
    public int score;
    public String steps;
    public String heartRate;
    public String visibility; // "public" or "friends"
    public long timestamp;

    public Post() {}

    public Post(String postId, String userId, String userName, String userProfilePic, 
                String content, int score, String steps, String heartRate, 
                String visibility, long timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userProfilePic = userProfilePic;
        this.content = content;
        this.score = score;
        this.steps = steps;
        this.heartRate = heartRate;
        this.visibility = visibility;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("postId", postId);
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("userProfilePic", userProfilePic);
        result.put("content", content);
        result.put("score", score);
        result.put("steps", steps);
        result.put("heartRate", heartRate);
        result.put("visibility", visibility);
        result.put("timestamp", timestamp);
        return result;
    }
}