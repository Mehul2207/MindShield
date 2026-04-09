package com.example.mindshield.firebase;

import com.example.mindshield.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {

    private static final DatabaseReference db =
            FirebaseDatabase.getInstance(
                    "https://mindshield-a2b70-default-rtdb.firebaseio.com"
            ).getReference();

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static DatabaseReference getUsersRef() {
        return db.child("users");
    }

    public static DatabaseReference getUser(String userId) {
        return db.child("users").child(userId);
    }

    public static DatabaseReference getFriends(String uid) {
        return db.child("friends").child(uid);
    }

    public static DatabaseReference getFriendRequests(String uid) {
        return db.child("friend_requests").child(uid);
    }

    public static void saveUserIfNotExists() {

        FirebaseUser firebaseUser = getCurrentUser();

        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();

        String photo = firebaseUser.getPhotoUrl() != null
                ? firebaseUser.getPhotoUrl().toString()
                : "";

        User user = new User(
                uid,
                firebaseUser.getDisplayName(),
                firebaseUser.getEmail(),
                photo
        );

        getUser(uid).setValue(user);
    }

    public static void sendFriendRequest(String receiverUid) {

        String senderUid = getUserId();

        if (senderUid == null) return;

        if (senderUid.equals(receiverUid)) return; // self prevention

        getFriends(senderUid).child(receiverUid)
                .get()
                .addOnSuccessListener(friendSnapshot -> {

                    if (friendSnapshot.exists()) {
                        return; // already friend
                    }

                    getFriendRequests(receiverUid)
                            .child(senderUid)
                            .get()
                            .addOnSuccessListener(requestSnapshot -> {

                                if (!requestSnapshot.exists()) {
                                    getFriendRequests(receiverUid)
                                            .child(senderUid)
                                            .setValue(true);
                                }
                            });
                });
    }

    public static void acceptFriend(String otherUid) {

        String myUid = getUserId();

        if (myUid == null) return;

        getFriends(myUid).child(otherUid).setValue(true);
        getFriends(otherUid).child(myUid).setValue(true);

        getFriendRequests(myUid).child(otherUid).removeValue();
    }

    public static DatabaseReference getMessages(String user1, String user2) {

        String roomId = generateChatRoom(user1, user2);

        return db.child("messages").child(roomId);
    }

    private static String generateChatRoom(String a, String b) {

        return a.compareTo(b) < 0
                ? a + "_" + b
                : b + "_" + a;
    }
}