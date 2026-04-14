package com.example.mindshield.analytics;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UsageStatsHelper {

    private static final String TAG = "UsageStatsHelper";

    public static Map<String, Long> getAppUsage(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, Long> usageMap = new HashMap<>();
        
        if (usm == null) {
            return usageMap;
        }

        // Calculate midnight today
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        // queryAndAggregateUsageStats aggregates all stats for the given range
        Map<String, UsageStats> stats = usm.queryAndAggregateUsageStats(startTime, endTime);

        if (stats != null && !stats.isEmpty()) {
            for (Map.Entry<String, UsageStats> entry : stats.entrySet()) {
                long time = entry.getValue().getTotalTimeInForeground();
                if (time > 0) {
                    usageMap.put(entry.getKey(), time);
                }
            }
        } else {
            Log.d(TAG, "No stats found via queryAndAggregateUsageStats");
        }

        return usageMap;
    }
}