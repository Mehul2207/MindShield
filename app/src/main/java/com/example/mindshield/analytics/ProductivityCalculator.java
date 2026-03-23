package com.example.mindshield.analytics;

import java.util.Map;

public class ProductivityCalculator {

    public static double calculateScore(Map<String, Long> usageMap) {

        double totalWeightedTime = 0;
        double totalTime = 0;

        for (Map.Entry<String, Long> entry : usageMap.entrySet()) {

            String packageName = entry.getKey();
            long time = entry.getValue();

            int weight = AppWeights.getWeight(packageName);

            totalWeightedTime += time * weight;
            totalTime += time;
        }

        if (totalTime == 0) return 0;

        return totalWeightedTime / totalTime;
    }
}