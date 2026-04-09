package com.example.mindshield;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.mindshield.firebase.FirebaseManager;
import com.example.mindshield.utils.PressAnimationHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;

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

        generateQRCode(qrImage);

        // Press animation safely
        ViewParent parent = qrImage.getParent();
        if (parent instanceof View) {
            PressAnimationHelper.applyPressAnimation((View) parent);
        }
    }

    private void generateQRCode(ImageView qrImage) {

        try {
            String uid = FirebaseManager.getUserId();

            if (uid == null) return;

            QRCodeWriter writer = new QRCodeWriter();

            int size = 500;

            Bitmap bitmap = Bitmap.createBitmap(
                    size,
                    size,
                    Bitmap.Config.RGB_565
            );

            var bitMatrix = writer.encode(
                    uid,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {

                    bitmap.setPixel(
                            x,
                            y,
                            bitMatrix.get(x, y)
                                    ? Color.BLACK
                                    : Color.WHITE
                    );
                }
            }

            qrImage.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}