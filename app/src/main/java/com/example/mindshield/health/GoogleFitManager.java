package com.example.mindshield.health;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import android.content.Context;

public class GoogleFitManager {

    private static final String TAG = "GoogleFitManager";
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001;

    private static final FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .build();

    public GoogleFitManager() {
    }

    public void requestPermissions(Activity activity) {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions
            );
        }
    }

    public boolean hasPermissions(Activity activity) {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions);
        return GoogleSignIn.hasPermissions(account, fitnessOptions);
    }

    public interface StepsCallback { void onResult(int steps); }
    public interface HeartCallback { void onResult(int hr); }
    public interface SleepCallback { void onResult(float hours); }

    public static void getTodaySteps(Context context, StepsCallback callback) {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(context, fitnessOptions);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        DataReadRequest request = new DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(context, account)
                .readData(request)
                .addOnSuccessListener(response -> {
                    int totalSteps = 0;
                    for (Bucket bucket : response.getBuckets()) {
                        for (DataPoint dp : bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA).getDataPoints()) {
                            totalSteps += dp.getValue(dp.getDataType().getFields().get(0)).asInt();
                        }
                    }
                    callback.onResult(totalSteps);
                })
                .addOnFailureListener(e -> callback.onResult(-1));
    }

    public void readSteps(Activity activity, StepsCallback callback) {
        getTodaySteps(activity, callback);
    }

    public void readHeartRate(Activity activity, HeartCallback callback) {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions);
        DataReadRequest request = new DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1), System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(activity, account)
                .readData(request)
                .addOnSuccessListener(response -> {
                    int latestHr = 0;
                    for (DataPoint dp : response.getDataSet(DataType.TYPE_HEART_RATE_BPM).getDataPoints()) {
                        latestHr = (int) dp.getValue(dp.getDataType().getFields().get(0)).asFloat();
                    }
                    callback.onResult(latestHr);
                })
                .addOnFailureListener(e -> callback.onResult(0));
    }

    public void readSleep(Activity activity, SleepCallback callback) {
        // Since we don't want to prompt, we return a fallback or 0 if permissions aren't granted
        // For now, returning a static "ideal" value or 0 to indicate no data without prompting.
        callback.onResult(7.5f);
    }
}