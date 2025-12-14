package com.example.amadapp;

import android.content.Context;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ImageValidator {

    public static class ValidationResult {
        public boolean isValid;
        public String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }

    // This is the method matching (Context, Uri)
    public static ValidationResult verifyImageAuthenticity(Context context, Uri imageUri) {
        InputStream inputStream = null;
        try {
            // Open the stream from the URI (works for FileProvider and Gallery)
            inputStream = context.getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                return new ValidationResult(false, "Cannot read image file.");
            }

            ExifInterface exif = new ExifInterface(inputStream);

            // 1. Check for Camera Maker/Model
            String make = exif.getAttribute(ExifInterface.TAG_MAKE);
            String model = exif.getAttribute(ExifInterface.TAG_MODEL);

            if (make == null || model == null) {
                // Note: Some legit Android phones might strip this in certain modes,
                // but usually it's present for camera photos.
                return new ValidationResult(false, "Metadata missing. Please take a photo directly with the camera.");
            }

            // 2. Check for Editing Software
            String software = exif.getAttribute(ExifInterface.TAG_SOFTWARE);
            if (software != null) {
                String lower = software.toLowerCase();
                if (lower.contains("photoshop") || lower.contains("editor") || lower.contains("gimp")) {
                    return new ValidationResult(false, "Editing software detected: " + software);
                }
            }

            // 3. Check Date Original (Prevent old photos)
            String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            if (dateString != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                try {
                    Date imageDate = sdf.parse(dateString);
                    Date now = new Date();
                    long diff = now.getTime() - imageDate.getTime();
                    long days = TimeUnit.MILLISECONDS.toDays(diff);

                    if (days > 1) { // Reject photos older than 1 day
                        return new ValidationResult(false, "Photo is too old (" + days + " days ago). Please take a new one.");
                    }
                } catch (Exception e) {
                    // Date format mismatch or error, proceed with caution
                }
            } else {
                return new ValidationResult(false, "Timestamp missing. Authenticity cannot be verified.");
            }

            return new ValidationResult(true, "Verified.");

        } catch (IOException e) {
            return new ValidationResult(false, "Error reading metadata: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException ignored) {}
            }
        }
    }
}