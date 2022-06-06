package com.alphawallet.app.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import com.alphawallet.app.R;

public class QRUtils {
    public static Bitmap createQRImage(Context context, String address, int imageSize) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    address,
                    BarcodeFormat.QR_CODE,
                    imageSize,
                    imageSize,
                    null);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            int[] allpixels = new int[bitmap.getHeight() * bitmap.getWidth()];
            bitmap.getPixels(allpixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            for (int i = 6; i < 9; i++) {
                allpixels[i] = context.getColor(R.color.ethernity_blue); // your rgb color
            }
            int qrCodeDimension = dpToPx(imageSize);
            bitmap.setPixels(allpixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            return Bitmap.createScaledBitmap(bitmap, qrCodeDimension, qrCodeDimension, false);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.error_fail_generate_qr), Toast.LENGTH_SHORT)
                    .show();
        }
        return null;
    }

    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
