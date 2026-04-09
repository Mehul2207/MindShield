package com.example.mindshield;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.mindshield.health.StepSensorManager;
import com.example.mindshield.analytics.UsageStatsHelper;
import android.provider.Settings;
import android.content.Intent;
import java.util.Map;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mindshield.health.GoogleFitManager;
import com.example.mindshield.utils.PressAnimationHelper;

public class HomeFragment extends Fragment {

    private GoogleFitManager googleFitManager;

    private StepSensorManager stepSensorManager;
    private TextView usageText;

    private TextView stepsText;
    private TextView heartText;

    private final Handler handler = new Handler();

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("DEBUG", "HomeFragment Loaded"); // ADD THIS

        stepsText = view.findViewById(R.id.stepsValue);
        heartText = view.findViewById(R.id.heartValue);
        usageText = view.findViewById(R.id.usageValue); // add in XML

        googleFitManager = new GoogleFitManager();

        // 🔥 FIXED FLOW
        if (googleFitManager.hasPermissions(requireActivity())) {

            Log.d("DEBUG", "Permissions already granted → loading data");

            loadData();

            // Optional refresh every 10s
            handler.postDelayed(refreshRunnable, 10000);

        } else {

            Log.d("DEBUG", "Requesting Google Fit permissions");

            googleFitManager.requestPermissions(requireActivity());
        }

        View scoreCard = view.findViewById(R.id.scoreCard);
        if (scoreCard != null) {
            PressAnimationHelper.applyPressAnimation(scoreCard);
        }

        ViewGroup root = (ViewGroup) view;
        applyAnimationToMetrics(root);
    }

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadData();
            handler.postDelayed(this, 10000);
        }
    };

    private void loadData() {

        Log.d("DEBUG", "Fetching Google Fit...");

        googleFitManager.readSteps(requireActivity(), steps -> {

            if (steps > 0) {
                // ✅ GOOGLE FIT SUCCESS
                requireActivity().runOnUiThread(() ->
                        stepsText.setText(String.valueOf(steps))
                );

            } else {
                // 🔥 FALLBACK → PHONE SENSOR
                Log.d("DEBUG", "Google Fit failed → using sensor");

                if (stepSensorManager == null) {
                    stepSensorManager = new StepSensorManager(getContext(), sensorSteps -> {
                        requireActivity().runOnUiThread(() ->
                                stepsText.setText(String.valueOf(sensorSteps))
                        );
                    });
                    stepSensorManager.start();
                }
            }
        });

        // ❤️ HEART RATE (keep as is)
        googleFitManager.readHeartRate(requireActivity(), hr -> {
            requireActivity().runOnUiThread(() ->
                    heartText.setText(hr > 0 ? hr + " bpm" : "--")
            );
        });

        // 📱 USAGE STATS
        loadUsageStats();
    }

    private void loadUsageStats() {

        if (!hasUsagePermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }

        Map<String, Long> usage = UsageStatsHelper.getAppUsage(getContext());

        long totalTime = 0;

        for (long time : usage.values()) {
            totalTime += time;
        }

        long minutes = totalTime / (1000 * 60);

        requireActivity().runOnUiThread(() ->
                usageText.setText(minutes + " min")
        );
    }

    private boolean hasUsagePermission() {

        try {
            UsageStatsHelper.getAppUsage(getContext());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void applyAnimationToMetrics(ViewGroup parent) {

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);

            if (child instanceof ViewGroup) {
                applyAnimationToMetrics((ViewGroup) child);
            }

            if (child.getId() == R.id.metricCard) {
                PressAnimationHelper.applyPressAnimation(child);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
    }
}