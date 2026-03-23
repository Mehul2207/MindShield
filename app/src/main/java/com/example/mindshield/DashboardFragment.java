package com.example.mindshield;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;

import java.util.*;

import com.example.mindshield.analytics.UsageStatsHelper;
import com.example.mindshield.analytics.ProductivityCalculator;

public class DashboardFragment extends Fragment {

    private TextView scoreValue, scoreStatus, greetingText;
    private TextView aiInsight;
    private PieChart pieChart;
    private LinearLayout topAppsContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        scoreValue = view.findViewById(R.id.scoreValue);
        scoreStatus = view.findViewById(R.id.scoreStatus);
        greetingText = view.findViewById(R.id.greetingText);
        aiInsight = view.findViewById(R.id.aiInsight);
        pieChart = view.findViewById(R.id.pieChart);
        topAppsContainer = view.findViewById(R.id.topAppsContainer);

        setGreeting();
        loadData();

        return view;
    }


    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String message = (hour < 12) ? "Good Morning" :
                (hour < 18) ? "Good Afternoon" :
                        "Good Evening";

        greetingText.setText(message + ", Mehul");
    }

    private void loadData() {

        new Thread(() -> {

            Map<String, Long> usageMap =
                    UsageStatsHelper.getAppUsage(getContext());

            double score =
                    ProductivityCalculator.calculateScore(usageMap);

            getActivity().runOnUiThread(() -> {

                scoreValue.setText(String.valueOf((int) score));

                scoreStatus.setText(score > 75 ? "Excellent" :
                        score > 50 ? "Good" :
                                "Needs Improvement");

                setupChart(usageMap);
                populateTopApps(usageMap);

            });

        }).start();
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
                android.graphics.Color.GREEN,
                android.graphics.Color.BLUE,
                android.graphics.Color.CYAN,
                android.graphics.Color.MAGENTA,
                android.graphics.Color.YELLOW
        );

        PieData data = new PieData(dataSet);
        data.setValueTextColor(android.graphics.Color.WHITE);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void populateTopApps(Map<String, Long> usageMap) {

        topAppsContainer.removeAllViews();

        int count = 0;

        for (Map.Entry<String, Long> entry : usageMap.entrySet()) {

            if (count >= 5) break;

            TextView tv = new TextView(getContext());
            tv.setTextColor(getResources().getColor(android.R.color.white));

            float minutes = entry.getValue() / (1000f * 60f);

            tv.setText(entry.getKey() + " - " + (int) minutes + " mins");

            tv.setPadding(0, 8, 0, 8);

            topAppsContainer.addView(tv);

            count++;
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps =
                (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);

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
}
