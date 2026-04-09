package com.example.mindshield;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindshield.adapters.UserAdapter;
import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.models.User;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class UserListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ArrayList<User> userList;

    public UserListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.userListRecycler);

        userList = new ArrayList<>();

        adapter = new UserAdapter(userList, user -> checkBeforeSending(user));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {

        String myUid = FirebaseManager.getUserId();

        FirebaseManager.getUsersRef()
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        userList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            User user = data.getValue(User.class);

                            if (user != null &&
                                    !user.userId.equals(myUid)) {

                                FirebaseManager.getFriends(myUid)
                                        .child(user.userId)
                                        .get()
                                        .addOnSuccessListener(friendSnapshot -> {

                                            if (!friendSnapshot.exists()) {
                                                userList.add(user);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void checkBeforeSending(User user) {

        String myUid = FirebaseManager.getUserId();

        FirebaseManager.getFriendRequests(user.userId)
                .child(myUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        Toast.makeText(
                                getContext(),
                                "Request already sent",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        FirebaseManager.sendFriendRequest(user.userId);

                        Toast.makeText(
                                getContext(),
                                "Friend request sent",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}