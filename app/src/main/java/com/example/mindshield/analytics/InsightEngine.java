package com.example.mindshield.analytics;

public class InsightEngine {

    public static String generateInsight(double score) {

        if (score < 30) {
            return "High distraction detected. Reduce social media usage.";
        } else if (score < 60) {
            return "Moderate productivity. Try focusing more on learning apps.";
        } else {
            return "Great productivity levels. Keep it up!";
        }
    }
}