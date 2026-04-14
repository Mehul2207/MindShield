package com.example.mindshield;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import androidx.fragment.app.Fragment;

import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.models.User;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanFragment extends Fragment {

    private LinearLayout scanBtn;

    public ScanFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        scanBtn = view.findViewById(R.id.btn_scan_qr);

        scanBtn.setOnClickListener(v -> startQRScanner());
    }

    private void startQRScanner() {

        IntentIntegrator integrator =
                IntentIntegrator.forSupportFragment(this);

        integrator.setPrompt("Scan friend's QR");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);

        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 android.content.Intent data) {

        IntentResult result =
                IntentIntegrator.parseActivityResult(
                        requestCode,
                        resultCode,
                        data
                );

        if (result != null) {

            if (result.getContents() != null) {
                processScannedUid(result.getContents());
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processScannedUid(String scannedUid) {

        FirebaseManager.getUser(scannedUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    User user = snapshot.getValue(User.class);

                    if (user != null) {

                        View dialogView = LayoutInflater.from(getContext())
                                .inflate(R.layout.dialog_user_profile, null);

                        ImageView profileImage =
                                dialogView.findViewById(R.id.dialogProfileImage);

                        TextView name =
                                dialogView.findViewById(R.id.dialogName);

                        TextView email =
                                dialogView.findViewById(R.id.dialogEmail);

                        name.setText(user.name);
                        email.setText(user.email);

                        // Optional profile image loading
                        if (user.profilePic != null && !user.profilePic.isEmpty()) {
                            com.bumptech.glide.Glide
                                    .with(requireContext())
                                    .load(user.profilePic)
                                    .into(profileImage);
                        }

                        new AlertDialog.Builder(requireContext())
                                .setView(dialogView)
                                .setPositiveButton("Add Friend", (d, w) -> {

                                    FirebaseManager.sendFriendRequest(user.userId);

                                    Toast.makeText(
                                            getContext(),
                                            "Friend request sent",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();

                    } else {

                        Toast.makeText(
                                getContext(),
                                "User not found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}