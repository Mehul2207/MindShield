package com.example.mindshield;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mindshield.adapters.PostAdapter;
import com.example.mindshield.analytics.ProductivityCalculator;
import com.example.mindshield.analytics.UsageEventHelper;
import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.health.GoogleFitManager;
import com.example.mindshield.models.Post;
import com.example.mindshield.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;

import java.util.*;

public class ForumFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private FloatingActionButton addPostFab;
    private DatabaseReference postsRef;
    private GoogleFitManager googleFitManager;

    public ForumFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.forumRecycler);
        addPostFab = view.findViewById(R.id.addPostFab);
        
        postsRef = FirebaseDatabase.getInstance("https://mindshield-a2b70-default-rtdb.firebaseio.com")
                .getReference("posts");
        
        googleFitManager = new GoogleFitManager();

        postList = new ArrayList<>();
        adapter = new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        addPostFab.setOnClickListener(v -> showAddPostDialog());

        loadPosts();
    }

    private void showAddPostDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_post, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme)
                .setView(dialogView)
                .create();

        TextInputEditText contentEdit = dialogView.findViewById(R.id.postContentEdit);
        TextView scorePreview = dialogView.findViewById(R.id.postScorePreview);
        CheckBox checkSteps = dialogView.findViewById(R.id.checkSteps);
        CheckBox checkHR = dialogView.findViewById(R.id.checkHR);
        RadioGroup visibilityGroup = dialogView.findViewById(R.id.visibilityGroup);
        Button btnPost = dialogView.findViewById(R.id.btnPost);

        // Auto-fetch score
        int currentScore = calculateCurrentScore();
        scorePreview.setText("Your Productivity Score: " + currentScore);

        btnPost.setOnClickListener(v -> {
            String content = contentEdit.getText().toString().trim();
            if (content.isEmpty()) return;

            String visibility = visibilityGroup.getCheckedRadioButtonId() == R.id.radioPublic ? "public" : "friends";
            
            fetchHealthDataAndPost(content, currentScore, checkSteps.isChecked(), checkHR.isChecked(), visibility, dialog);
        });

        dialog.show();
    }

    private int calculateCurrentScore() {
        Map<String, Long> usageMap = UsageEventHelper.getAccurateUsage(requireContext().getApplicationContext());
        return (int) ProductivityCalculator.calculateScore(usageMap);
    }

    private void fetchHealthDataAndPost(String content, int score, boolean includeSteps, boolean includeHR, String visibility, AlertDialog dialog) {
        String myUid = FirebaseManager.getUserId();
        
        FirebaseManager.getUser(myUid).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user == null) return;

            String postId = postsRef.push().getKey();
            long timestamp = System.currentTimeMillis();

            // Handle Health Data fetches asymmetrically or wait (simplified here)
            googleFitManager.readSteps(getActivity(), steps -> {
                googleFitManager.readHeartRate(getActivity(), hr -> {
                    
                    String stepsStr = includeSteps ? String.valueOf(steps) : null;
                    String hrStr = includeHR ? String.valueOf(hr) : null;

                    Post post = new Post(postId, myUid, user.name, user.profilePic, 
                                       content, score, stepsStr, hrStr, visibility, timestamp);
                    
                    if (postId != null) {
                        postsRef.child(postId).setValue(post).addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Posted!", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });
        });
    }

    private void loadPosts() {
        String myUid = FirebaseManager.getUserId();
        
        postsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Post post = data.getValue(Post.class);
                    if (post != null) {
                        if (post.visibility.equals("public") || post.userId.equals(myUid)) {
                            postList.add(0, post); // Add to top
                        } else if (post.visibility.equals("friends")) {
                            // Friend check logic would go here (omitted for brevity)
                            postList.add(0, post);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}