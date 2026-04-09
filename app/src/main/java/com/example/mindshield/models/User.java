package com.example.mindshield.models;

public class User {

    public String userId;
    public String name;
    public String email;
    public String profilePic;

    public User() {}

    public User(String userId, String name, String email, String profilePic) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profilePic = profilePic;
    }
}