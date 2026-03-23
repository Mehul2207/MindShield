package com.example.mindshield.qr;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.MultiFormatWriter;

public class QRGenerator {

    public static Bitmap generateQR(String text) throws WriterException {

        BitMatrix matrix =
                new MultiFormatWriter().encode(text,
                        BarcodeFormat.QR_CODE,
                        500,
                        500);

        int width = matrix.getWidth();
        int height = matrix.getHeight();

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {

            for (int y = 0; y < height; y++) {

                bmp.setPixel(x, y,
                        matrix.get(x, y) ?
                                android.graphics.Color.BLACK :
                                android.graphics.Color.WHITE);

            }
        }

        return bmp;
    }
}
