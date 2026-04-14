package com.example.mindshield;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mindshield.firebase.FirebaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.mindshield.ChatContainerFragment;
import com.example.mindshield.FriendRequestsFragment;
import com.example.mindshield.FriendsFragment;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

public class DashboardActivity extends AppCompatActivity {

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 AUTH CHECK (CRITICAL)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);
        requestActivityPermission();
        FirebaseManager.saveUserIfNotExists();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // ✅ Load fragment safely
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new DashboardFragment();

            } else if (id == R.id.nav_forum) {
                fragment = new ForumFragment();

            } else if (id == R.id.nav_scan) {
                fragment = new ScanFragment();

            } else if (id == R.id.nav_chat) {
                fragment = new ChatContainerFragment();

            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit(); // ❌ removed backstack (important)
    }

    private void requestActivityPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                        2001
                );

            } else {
                Log.d("DEBUG", "ACTIVITY_RECOGNITION already granted");
            }
        }
    }

    // Google Fit result handling
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("GoogleFit", "Permission granted");

                loadFragment(new DashboardFragment());

            } else {
                Log.e("GoogleFit", "Permission denied");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "ACTIVITY_RECOGNITION GRANTED");

                // Reload fragment so steps update
                loadFragment(new DashboardFragment());

            } else {
                Log.e("DEBUG", "ACTIVITY_RECOGNITION DENIED");
            }
        }
    }
}