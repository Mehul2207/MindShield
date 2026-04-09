package com.example.mindshield.health;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.SharedPreferences;

public class StepSensorManager implements SensorEventListener {

    public interface StepCallback {
        void onSteps(int steps);
    }

    private SensorManager sensorManager;
    private Context context;
    private Sensor stepSensor;
    private StepCallback callback;

    private int initialSteps = -1;

    public StepSensorManager(Context context, StepCallback callback) {
        this.context = context;
        this.callback = callback;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    public void start() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int steps = (int) event.values[0];

        if (initialSteps == -1 || steps < initialSteps) {
            // ✅ handle reboot/reset case
            initialSteps = steps;
        }

        int currentSteps = steps - initialSteps;

        if (callback != null) {
            int finalSteps = Math.max(currentSteps, 0);

// Save steps
            SharedPreferences prefs = context.getSharedPreferences("health_data", Context.MODE_PRIVATE);

            prefs.edit().putInt("today_steps", finalSteps).apply();

            if (callback != null) {
                callback.onSteps(finalSteps);
            }
        }
    }

    public static int getTodaySteps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("health_data", Context.MODE_PRIVATE);
        return prefs.getInt("today_steps", 0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}