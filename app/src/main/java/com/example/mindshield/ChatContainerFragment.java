package com.example.mindshield;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChatContainerFragment extends Fragment {

    private Button friendsBtn, requestsBtn;


    public ChatContainerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_container, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        friendsBtn = view.findViewById(R.id.friendsBtn);
        requestsBtn = view.findViewById(R.id.requestsBtn);

        loadFragment(new FriendsFragment());

        friendsBtn.setOnClickListener(v ->
                loadFragment(new FriendsFragment()));

        requestsBtn.setOnClickListener(v ->
                loadFragment(new FriendRequestsFragment()));

        FloatingActionButton scanFab = view.findViewById(R.id.scanFab);

        scanFab.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ScanFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_inner_container, fragment)
                .commit();
    }
}