package com.example.mindshield;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindshield.adapters.FriendsAdapter;
import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.models.User;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private ArrayList<User> friendList;

    private static final String AI_USER_ID = "AI_BOT";

    public FriendsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.friendsRecycler);

        friendList = new ArrayList<>();

        adapter = new FriendsAdapter(friendList, user -> {

            ChatFragment chatFragment = new ChatFragment();

            Bundle args = new Bundle();
            args.putString("friendUserId", user.userId);
            chatFragment.setArguments(args);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadFriends();
    }

    private void loadFriends() {

        String myUid = FirebaseManager.getUserId();

        FirebaseManager.getFriends(myUid)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        friendList.clear();

                        // 🔥 ADD AI FIRST
                        friendList.add(createAIUser());

                        for (DataSnapshot child : snapshot.getChildren()) {

                            String friendUid = child.getKey();

                            FirebaseManager.getUser(friendUid)
                                    .get()
                                    .addOnSuccessListener(data -> {

                                        User user = data.getValue(User.class);

                                        if (user != null) {
                                            friendList.add(user);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // 🔥 CREATE AI USER
    private User createAIUser() {

        User aiUser = new User();
        aiUser.userId = AI_USER_ID;
        aiUser.name = "MindShield AI";
        aiUser.email = "AI Assistant";

        return aiUser;
    }
}