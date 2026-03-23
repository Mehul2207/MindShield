package com.example.mindshield.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {

    private static final DatabaseReference db =
            FirebaseDatabase.getInstance().getReference();

    public static DatabaseReference getChatRoom(String user1, String user2) {

        String roomId;

        if (user1.compareTo(user2) < 0) {
            roomId = user1 + "_" + user2;
        } else {
            roomId = user2 + "_" + user1;
        }

        return db.child("chats").child(roomId);
    }
}