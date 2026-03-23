package com.example.mindshield.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.example.mindshield.R;

public class PressAnimationHelper {

    public static void applyPressAnimation(View view) {

        Animation scaleDown = android.view.animation.AnimationUtils
                .loadAnimation(view.getContext(), R.anim.scale_press);

        Animation scaleUp = android.view.animation.AnimationUtils
                .loadAnimation(view.getContext(), R.anim.scale_release);

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(scaleDown);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(scaleUp);
                    break;
            }
            return false;
        });
    }
}