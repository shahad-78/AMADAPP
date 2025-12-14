package com.example.amadapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.amadapp.Model.DegradedArea;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ReportAreaActivity extends AppCompatActivity {

    private ImageView ivAreaImage;
    private Button btnCamera, btnGallery, btnSubmitReport;
    private EditText etAddress, etDescription;
    private ProgressBar progressBar;

    private Uri photoURI;
    private String latitude, longitude;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_area);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("DegradedAreas");
        mStorage = FirebaseStorage.getInstance().getReference("AreaImages");

        ivAreaImage = findViewById(R.id.ivAreaImage);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        etAddress = findViewById(R.id.etAddress);
        etDescription = findViewById(R.id.etDescription);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getStringExtra("lat");
            longitude = intent.getStringExtra("lng");
            etAddress.setText("Lat: " + latitude + ", Lng: " + longitude);
        }

        btnCamera.setOnClickListener(v -> checkCameraPermission());
        btnGallery.setOnClickListener(v -> openGallery());
        btnSubmitReport.setOnClickListener(v -> validateAndSubmit());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            try {
                photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    ivAreaImage.setImageURI(photoURI);
                }
            });

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    photoURI = result.getData().getData();
                    ivAreaImage.setImageURI(photoURI);
                }
            });

    private void validateAndSubmit() {
        String address = etAddress.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (photoURI == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- AUTHENTICITY CHECK ---
        ImageValidator.ValidationResult result = ImageValidator.verifyImageAuthenticity(this, photoURI);
        if (!result.isValid) {
            Toast.makeText(this, "Verification Failed: " + result.message, Toast.LENGTH_LONG).show();
            return;
        }

        uploadImageToFirebase(address, description);
    }

    private void uploadImageToFirebase(String address, String description) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmitReport.setEnabled(false);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "Anonymous";
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = mStorage.child(userId).child(fileName);

        fileRef.putFile(photoURI).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                        saveDataToRealtimeDB(uri.toString(), address, description)
                )
        ).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnSubmitReport.setEnabled(true);
            Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveDataToRealtimeDB(String imageUrl, String address, String description) {
        String userID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "Anonymous";
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DegradedArea degradedArea = new DegradedArea(userID, imageUrl, currentDate, description, address, latitude, longitude, "Pending");

        mDatabase.push().setValue(degradedArea).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Report Submitted Successfully!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}