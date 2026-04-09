package com.example.mindshield;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindshield.adapters.FriendRequestAdapter;
import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.models.User;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class FriendRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private ArrayList<User> requestList;

    public FriendRequestsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.requestsRecycler);

        requestList = new ArrayList<>();

        adapter = new FriendRequestAdapter(
                requestList,
                user -> {
                    FirebaseManager.acceptFriend(user.userId);
                    requestList.remove(user);
                    adapter.notifyDataSetChanged();
                },
                user -> {
                    FirebaseManager.getFriendRequests(FirebaseManager.getUserId())
                            .child(user.userId)
                            .removeValue();

                    requestList.remove(user);
                    adapter.notifyDataSetChanged();
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {

        String myUid = FirebaseManager.getUserId();

        FirebaseManager.getFriendRequests(myUid)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        requestList.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {

                            String senderUid = child.getKey();

                            FirebaseManager.getUser(senderUid)
                                    .get()
                                    .addOnSuccessListener(data -> {

                                        User user = data.getValue(User.class);

                                        if (user != null) {
                                            requestList.add(user);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }
}