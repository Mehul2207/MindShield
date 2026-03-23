package com.example.mindshield;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.mindshield.utils.PressAnimationHelper;

public class ScanFragment extends Fragment {

    public ScanFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnBluetooth = view.findViewById(R.id.btn_scan_bluetooth);
        Button btnQR = view.findViewById(R.id.btn_scan_qr);
        Button btnNFC = view.findViewById(R.id.btn_nfc_share);

        // 🔥 Animate buttons
        PressAnimationHelper.applyPressAnimation(btnBluetooth);
        PressAnimationHelper.applyPressAnimation(btnQR);
        PressAnimationHelper.applyPressAnimation(btnNFC);

        // 🔥 Animate list container safely
        View listView = view.findViewById(R.id.device_list);
        ViewParent parent = listView.getParent();

        if (parent instanceof View) {
            PressAnimationHelper.applyPressAnimation((View) parent);
        }
    }
}