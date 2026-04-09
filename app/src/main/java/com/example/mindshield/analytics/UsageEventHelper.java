package com.example.mindshield.analytics;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class UsageEventHelper {

    private static final String TAG = "UsageEventHelper";

    public static HashMap<String, Long> getAccurateUsage(Context context) {

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        HashMap<String, Long> usageMap = new HashMap<>();

        List<UsageStats> stats =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        if (stats == null) return usageMap;

        for (UsageStats stat : stats) {

            String pkg = stat.getPackageName();

            if (isSystemPackage(pkg)) continue;

            long foregroundTime = stat.getTotalTimeInForeground();

            if (foregroundTime < 2 * 60 * 1000) continue;

            usageMap.put(pkg, foregroundTime);
        }

        Log.d(TAG, "Apps counted = " + usageMap.size());

        return usageMap;
    }

    private static boolean isSystemPackage(String pkg) {

        return pkg.contains("launcher") ||
                pkg.contains("systemui") ||
                pkg.contains("inputmethod") ||
                pkg.contains("permissioncontroller") ||
                pkg.equals("android");
    }
}
