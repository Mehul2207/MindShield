package com.example.mindshield;

import android.app.AppOpsManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mindshield.analytics.ProductivityCalculator;
import com.example.mindshield.analytics.UsageEventHelper;
import com.example.mindshield.health.GoogleFitManager;
import com.example.mindshield.health.StepSensorManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class DashboardFragment extends Fragment {

    private TextView scoreValue, scoreStatus, greetingText, aiInsight;
    private PieChart pieChart;
    private LinearLayout topAppsContainer;

    private TextView stepsText;
    private TextView heartText;
    private TextView usageText;
    private TextView sleepText;
    private ImageButton addSleepButton;

    private boolean isUsingFit = false;
    private StepSensorManager sensorManager;

    private GoogleFitManager googleFitManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d("DEBUG", "DashboardFragment Loaded");

        // UI binding
        scoreValue = view.findViewById(R.id.scoreValue);
        scoreStatus = view.findViewById(R.id.scoreStatus);
        greetingText = view.findViewById(R.id.greetingText);
        aiInsight = view.findViewById(R.id.aiInsight);
        pieChart = view.findViewById(R.id.pieChart);
        topAppsContainer = view.findViewById(R.id.topAppsContainer);

        stepsText = view.findViewById(R.id.stepsValue);
        heartText = view.findViewById(R.id.heartValue);
        usageText = view.findViewById(R.id.usageValue);
        sleepText = view.findViewById(R.id.sleepValue);
        addSleepButton = view.findViewById(R.id.addSleepButton);

        googleFitManager = new GoogleFitManager();

        TextView connectFit = view.findViewById(R.id.connectFitButton);

        connectFit.setOnClickListener(v -> {
            Log.d("DEBUG", "Manual Fit connect clicked");
            googleFitManager.requestPermissions(getActivity());
        });

        if (addSleepButton != null) {
            addSleepButton.setOnClickListener(v -> showSleepStartPicker());
        }

        setGreeting();
        setupChartUI();

        loadData(); // usage stats
        handleGoogleFit(); // health data

        return view;
    }

    private void showSleepStartPicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            Calendar sleepStart = Calendar.getInstance();
            sleepStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            sleepStart.set(Calendar.MINUTE, minute);
            showSleepEndPicker(sleepStart);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
        dialog.setTitle("When did you go to sleep?");
        dialog.show();
    }

    private void showSleepEndPicker(Calendar sleepStart) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            Calendar sleepEnd = Calendar.getInstance();
            sleepEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
            sleepEnd.set(Calendar.MINUTE, minute);

            if (sleepEnd.before(sleepStart)) {
                sleepEnd.add(Calendar.DATE, 1);
            }

            long diff = sleepEnd.getTimeInMillis() - sleepStart.getTimeInMillis();
            float hours = diff / (1000f * 60 * 60);

            if (isAdded()) {
                getActivity().runOnUiThread(() -> {
                    sleepText.setText(String.format(Locale.getDefault(), "%.1fh", hours));
                    Toast.makeText(getContext(), "Sleep recorded: " + String.format(Locale.getDefault(), "%.1f", hours) + " hours", Toast.LENGTH_SHORT).show();
                });
            }

        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
        dialog.setTitle("When did you wake up?");
        dialog.show();
    }

    // ---------------- GREETING ----------------
    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String message = (hour < 12) ? "Good Morning" :
                (hour < 18) ? "Good Afternoon" :
                        "Good Evening";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = "User";
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                name = user.getDisplayName();
            } else if (user.getEmail() != null) {
                name = user.getEmail().split("@")[0];
            }
        }

        greetingText.setText(message + ", " + name + " 👋");
    }

    // ---------------- GOOGLE FIT ----------------
    private void handleGoogleFit() {

        if (googleFitManager.hasPermissions(getActivity())) {

            Log.d("DEBUG", "Fit permission OK");
            loadHealthData();

        } else {

            Log.d("DEBUG", "Fit permission NOT granted");
            googleFitManager.requestPermissions(getActivity());

            stepsText.setText("Connect Fit");
            heartText.setText("--");
        }
    }

    private void loadHealthData() {

        googleFitManager.readSteps(getActivity(), steps -> {

            Log.d("DEBUG", "Fit steps: " + steps);

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {

                    if (steps >= 0) {
                        // ✅ Use ONLY Google Fit
                        isUsingFit = true;

                        if (sensorManager != null) {
                            sensorManager.stop(); // stop sensor if running
                        }

                        stepsText.setText(String.valueOf(steps));

                    } else {
                        Log.d("DEBUG", "Fit failed → using sensor");

                        isUsingFit = false;

                        if (sensorManager == null) {
                            sensorManager = new StepSensorManager(getContext(), sensorSteps -> {
                                if (isUsingFit) return; // 🚫 prevent override
                                if (isAdded() && getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        stepsText.setText(String.valueOf(sensorSteps));
                                    });
                                }
                            });
                            sensorManager.start();
                        }
                    }

                });
            }
        });

        googleFitManager.readHeartRate(getActivity(), hr -> {

            Log.d("DEBUG", "Heart rate: " + hr);

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (hr > 0) {
                        heartText.setText(hr + " bpm");
                    } else {
                        heartText.setText("--");
                    }
                });
            }
        });

        googleFitManager.readSleep(getActivity(), hours -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (sleepText.getText().toString().equals("--")) {
                        sleepText.setText(String.format(Locale.getDefault(), "%.1fh", hours));
                    }
                });
            }
        });
    }

    // ---------------- USAGE STATS ----------------
    private void loadData() {

        Log.d("DEBUG", "loadData running");

        if (!hasUsageStatsPermission()) {
            Log.d("DEBUG", "Usage permission NOT granted");
            requestPermission();
            return;
        }

        new Thread(() -> {

            try {
                Thread.sleep(1000); // give system time to update stats
            } catch (Exception e) {}

            Context appContext = requireContext().getApplicationContext();

            Map<String, Long> usageMap =
                    UsageEventHelper.getAccurateUsage(appContext);

            long totalTime = 0;
            for (long time : usageMap.values()) {
                totalTime += time;
            }

            final long fTotalTime = totalTime;

            List<Map.Entry<String, Long>> sorted =
                    new ArrayList<>(usageMap.entrySet());

            sorted.sort((a, b) ->
                    Long.compare(b.getValue(), a.getValue()));

            LinkedHashMap<String, Long> sortedMap = new LinkedHashMap<>();

            for (Map.Entry<String, Long> entry : sorted) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }

            double score =
                    ProductivityCalculator.calculateScore(sortedMap);

            if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {

                    scoreValue.setText(String.valueOf((int) score));

                    scoreStatus.setText(score > 75 ? "Excellent 🚀" :
                            score > 50 ? "Good 👍" :
                                    "Needs Improvement ⚠️");

                    aiInsight.setText(generateInsight(score));

                    long totalMinutes = fTotalTime / (1000 * 60);
                    long hours = totalMinutes / 60;
                    long minutes = totalMinutes % 60;

                    if (hours > 0) {
                        usageText.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));
                    } else {
                        usageText.setText(String.format(Locale.getDefault(), "%dm", minutes));
                    }

                    setupChart(sortedMap);
                    populateTopApps(sortedMap);
                });


        }).start();
    }

    private boolean hasUsageStatsPermission() {

        Context context = requireContext().getApplicationContext();

        AppOpsManager appOps =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        int mode;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    getContext().getPackageName()
            );
        } else {
            mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    getContext().getPackageName()
            );
        }

        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestPermission() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    // ---------------- AI INSIGHT ----------------
    private String generateInsight(double score) {
        if (score > 75) {
            return "You're in control today. Keep maintaining this balance.";
        } else if (score > 50) {
            return "Decent progress. Try reducing screen time slightly.";
        } else {
            return "High distraction detected. Consider a digital detox.";
        }
    }

    // ---------------- CHART ----------------
    private void setupChartUI() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setHoleRadius(55f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
    }

    private void setupChart(Map<String, Long> usageMap) {

        List<PieEntry> entries = new ArrayList<>();

        int count = 0;

        for (Map.Entry<String, Long> entry : usageMap.entrySet()) {

            if (count >= 5) break;

            float minutes = entry.getValue() / (1000f * 60f);

            entries.add(new PieEntry(minutes, entry.getKey()));
            count++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(
                Color.parseColor("#00E676"),
                Color.parseColor("#2979FF"),
                Color.parseColor("#00E5FF"),
                Color.parseColor("#FF4081"),
                Color.parseColor("#FFD600")
        );

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.animateY(900);
        pieChart.invalidate();
    }

    // ---------------- TOP APPS ----------------
    private void populateTopApps(Map<String, Long> usageMap) {

        topAppsContainer.removeAllViews();

        int count = 0;

        for (Map.Entry<String, Long> entry : usageMap.entrySet()) {

            if (count >= 5) break;

            float minutes = entry.getValue() / (1000f * 60f);

            Context context = requireContext(); // safe at this point

            TextView tv = new TextView(context);

            String appName = entry.getKey();

            try {
                appName = getContext()
                        .getPackageManager()
                        .getApplicationLabel(
                                getContext().getPackageManager()
                                        .getApplicationInfo(entry.getKey(), 0)
                        ).toString();
            } catch (Exception e) {
                // fallback to package name
            }

            tv.setText(appName + " • " + (int) minutes + " mins");
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(15f);

            tv.setPadding(20, 20, 20, 20);
            tv.setBackgroundResource(R.drawable.bg_glass_card);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(0, 8, 0, 8);
            tv.setLayoutParams(params);

            topAppsContainer.addView(tv);

            count++;
        }
    }
}