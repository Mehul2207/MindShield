package com.example.mindshield;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.mindshield.utils.PressAnimationHelper;

public class HomeFragment extends Fragment {

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔹 Apply animation to score card
        View scoreCard = view.findViewById(R.id.scoreCard);
        if (scoreCard != null) {
            PressAnimationHelper.applyPressAnimation(scoreCard);
        }

        // 🔹 Apply animation to ALL metric cards
        // Since include duplicates IDs, we handle them like this:
        ViewGroup root = (ViewGroup) view;

        applyAnimationToMetrics(root);
    }

    private void applyAnimationToMetrics(ViewGroup parent) {

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);

            if (child instanceof ViewGroup) {
                applyAnimationToMetrics((ViewGroup) child);
            }

            if (child.getId() == R.id.metricCard) {
                PressAnimationHelper.applyPressAnimation(child);
            }
        }
    }
}