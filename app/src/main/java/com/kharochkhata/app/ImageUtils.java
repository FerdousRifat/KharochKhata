package com.kharochkhata.app;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers to shrink a photo and convert it to text (Base64) so it can be saved
 * inside the Realtime Database, and to turn that text back into a picture.
 *
 * Why: Firebase Storage (the normal place for photos) needs a paid plan, so this
 * project keeps a small receipt photo (max 480 px) directly in the database.
 */
public final class ImageUtils {

    private static final int MAX_SIZE = 480;   // longest side in pixels
    private static final int JPEG_QUALITY = 70;

    private ImageUtils() {
    }

    /** Reads a picture chosen from the gallery, loading it at a reduced size to save memory. */
    public static Bitmap decodeUri(ContentResolver resolver, Uri uri) throws IOException {
        // Step 1: read only the size of the picture.
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }

        // Step 2: choose how much to shrink while loading.
        int sample = 1;
        while (bounds.outWidth / sample > MAX_SIZE * 2 || bounds.outHeight / sample > MAX_SIZE * 2) {
            sample *= 2;
        }

        // Step 3: load the picture for real.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        try (InputStream in = resolver.openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            if (bitmap == null) {
                throw new IOException("Could not decode image");
            }
            return bitmap;
        }
    }

    /** Bitmap -> small JPEG -> Base64 text. */
    public static String toBase64(Bitmap bitmap) {
        Bitmap small = scaleDown(bitmap);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        small.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    /** Base64 text -> Bitmap. Returns null if the text is empty or broken. */
    public static Bitmap fromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Bitmap scaleDown(Bitmap source) {
        int longest = Math.max(source.getWidth(), source.getHeight());
        if (longest <= MAX_SIZE) {
            return source;
        }
        float ratio = (float) MAX_SIZE / longest;
        int width = Math.round(source.getWidth() * ratio);
        int height = Math.round(source.getHeight() * ratio);
        return Bitmap.createScaledBitmap(source, width, height, true);
    }
}
