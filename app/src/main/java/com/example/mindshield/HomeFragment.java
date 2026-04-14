package com.example.mindshield;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mindshield.analytics.UsageEventHelper;
import com.example.mindshield.health.GoogleFitManager;
import com.example.mindshield.health.StepSensorManager;
import com.example.mindshield.utils.PressAnimationHelper;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private GoogleFitManager googleFitManager;
    private StepSensorManager stepSensorManager;
    private TextView usageText;
    private TextView stepsText;
    private TextView heartText;
    private TextView sleepText;
    private ImageButton addSleepButton;

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
        Log.d("DEBUG", "HomeFragment Loaded");

        stepsText = view.findViewById(R.id.stepsValue);
        heartText = view.findViewById(R.id.heartValue);
        usageText = view.findViewById(R.id.usageValue);
        sleepText = view.findViewById(R.id.sleepValue);
        addSleepButton = view.findViewById(R.id.addSleepButton);

        googleFitManager = new GoogleFitManager();

        if (googleFitManager.hasPermissions(requireActivity())) {
            Log.d("DEBUG", "Permissions already granted → loading data");
            loadData();
            handler.postDelayed(refreshRunnable, 10000);
        } else {
            Log.d("DEBUG", "Requesting Google Fit permissions");
            googleFitManager.requestPermissions(requireActivity());
        }

        if (addSleepButton != null) {
            addSleepButton.setOnClickListener(v -> showSleepStartPicker());
        }

        View scoreCard = view.findViewById(R.id.scoreCard);
        if (scoreCard != null) {
            PressAnimationHelper.applyPressAnimation(scoreCard);
        }

        ViewGroup root = (ViewGroup) view;
        applyAnimationToMetrics(root);
    }

    private void showSleepStartPicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            Calendar sleepStart = Calendar.getInstance();
            sleepStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            sleepStart.set(Calendar.MINUTE, minute);
            showSleepEndPicker(sleepStart);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
        picker.setTitle("When did you go to sleep?");
        picker.show();
    }

    private void showSleepEndPicker(Calendar sleepStart) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            Calendar sleepEnd = Calendar.getInstance();
            sleepEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
            sleepEnd.set(Calendar.MINUTE, minute);

            if (sleepEnd.before(sleepStart)) {
                sleepEnd.add(Calendar.DATE, 1);
            }

            long diff = sleepEnd.getTimeInMillis() - sleepStart.getTimeInMillis();
            float hours = diff / (1000f * 60 * 60);

            requireActivity().runOnUiThread(() -> {
                sleepText.setText(String.format(Locale.getDefault(), "%.1fh", hours));
                Toast.makeText(getContext(), "Sleep recorded: " + String.format(Locale.getDefault(), "%.1f", hours) + " hours", Toast.LENGTH_SHORT).show();
            });

        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
        picker.setTitle("When did you wake up?");
        picker.show();
    }

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadData();
            handler.postDelayed(this, 10000);
        }
    };

    private void loadData() {
        Log.d("DEBUG", "Fetching Health Data...");

        // 👣 STEPS
        googleFitManager.readSteps(requireActivity(), steps -> {
            if (steps >= 0) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            stepsText.setText(String.valueOf(steps))
                    );
                }
            } else {
                Log.d("DEBUG", "Google Fit steps failed → using sensor");
                if (stepSensorManager == null) {
                    stepSensorManager = new StepSensorManager(getContext(), sensorSteps -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    stepsText.setText(String.valueOf(sensorSteps))
                            );
                        }
                    });
                    stepSensorManager.start();
                }
            }
        });

        // ❤️ HEART RATE
        googleFitManager.readHeartRate(requireActivity(), hr -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() ->
                        heartText.setText(hr > 0 ? hr + " bpm" : "--")
                );
            }
        });

        // 💤 SLEEP (Initial load)
        googleFitManager.readSleep(requireActivity(), hours -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (sleepText.getText().toString().equals("--")) {
                        sleepText.setText(String.format(Locale.getDefault(), "%.1fh", hours));
                    }
                });
            }
        });

        // 📱 USAGE STATS
        loadUsageStats();
    }

    private void loadUsageStats() {
        Map<String, Long> usage = UsageEventHelper.getAccurateUsage(getContext());
        long totalTime = 0;
        for (long time : usage.values()) {
            totalTime += time;
        }

        long totalMinutes = totalTime / (1000 * 60);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                if (hours > 0) {
                    usageText.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));
                } else {
                    usageText.setText(String.format(Locale.getDefault(), "%dm", minutes));
                }
            });
        }
    }

    private void applyAnimationToMetrics(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyAnimationToMetrics((ViewGroup) child);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
    }
}