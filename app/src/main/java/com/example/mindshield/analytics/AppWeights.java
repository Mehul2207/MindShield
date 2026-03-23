package com.example.mindshield.analytics;

import java.util.HashMap;
import java.util.Map;

public class AppWeights {

    private static final Map<String, Integer> appWeights = new HashMap<>();

    static {
        // Social
        appWeights.put("com.instagram.android", 10);
        appWeights.put("com.snapchat.android", 10);

        // Entertainment
        appWeights.put("com.google.android.youtube", 40);
        appWeights.put("com.netflix.mediaclient", 30);

        // Learning
        appWeights.put("org.coursera.android", 100);
        appWeights.put("com.udemy.android", 90);

        // Tools
        appWeights.put("com.google.android.apps.docs", 85);
    }

    public static int getWeight(String packageName) {
        return appWeights.getOrDefault(packageName, 50);
    }
}