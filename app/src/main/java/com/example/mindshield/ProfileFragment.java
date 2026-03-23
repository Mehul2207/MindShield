package com.example.mindshield;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.mindshield.qr.QRGenerator;
import com.example.mindshield.utils.PressAnimationHelper;
import com.google.zxing.WriterException;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView qrImage = view.findViewById(R.id.qrImage);

        try {
            Bitmap qr = QRGenerator.generateQR("USER:MindShield001");
            qrImage.setImageBitmap(qr);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // 🔥 Safe animation on parent container
        ViewParent parent = qrImage.getParent();
        if (parent instanceof View) {
            PressAnimationHelper.applyPressAnimation((View) parent);
        }
    }
}