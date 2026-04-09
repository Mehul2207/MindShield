package com.example.mindshield.ai;

import android.content.Context;
import com.example.mindshield.analytics.ProductivityCalculator;
import com.example.mindshield.analytics.UsageStatsHelper;
import com.example.mindshield.health.GoogleFitManager;
import com.example.mindshield.health.StepSensorManager;

import java.util.Map;

public class ContextBuilder {

    public static void buildContext(Context context, ContextCallback callback) {

        Map<String, Long> usageData = UsageStatsHelper.getAppUsage(context);
        double productivity = ProductivityCalculator.calculateScore(usageData);

        String stressLevel;

        if (productivity < 30) stressLevel = "High";
        else if (productivity < 70) stressLevel = "Moderate";
        else stressLevel = "Low";

        GoogleFitManager.getTodaySteps(context, steps -> {

            String result = "User Stats:\n" +
                    "- Steps today: " + steps + "\n" +
                    "- Productivity score: " + productivity + "\n" +
                    "- Estimated stress level: " + stressLevel + "\n";

            callback.onContextReady(result);
        });

    }

    public interface ContextCallback {
        void onContextReady(String context);
    }

}