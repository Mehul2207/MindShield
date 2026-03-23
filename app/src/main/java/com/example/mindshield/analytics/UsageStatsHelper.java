package com.example.mindshield.analytics;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageStatsHelper {

    public static Map<String, Long> getAppUsage(Context context) {

        UsageStatsManager usm =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000 * 60 * 60 * 24);

        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

        Map<String, Long> usageMap = new HashMap<>();

        if (stats != null) {
            for (UsageStats usageStats : stats) {

                long time = usageStats.getTotalTimeInForeground();

                if (time > 0) {
                    usageMap.put(
                            usageStats.getPackageName(),
                            time
                    );
                }
            }
        }

        return usageMap;
    }
}