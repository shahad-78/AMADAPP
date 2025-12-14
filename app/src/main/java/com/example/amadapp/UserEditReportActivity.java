package com.example.amadapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserEditReportActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvChangeHint, tvStatus;
    private EditText etAddress, etDescription;
    private Button btnUpdate;
    private ProgressBar progressBar;

    private String reportId, originalImageUrl, status;
    private Bitmap newImageBitmap; // If user takes a new photo
    private boolean isImageChanged = false;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_report);

        // Init Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("DegradedAreas");
        mStorage = FirebaseStorage.getInstance().getReference("AreaImages");

        // Init Views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivImage = findViewById(R.id.ivReportImage);
        tvChangeHint = findViewById(R.id.tvChangeImageHint);
        tvStatus = findViewById(R.id.tvStatusBanner);
        etAddress = findViewById(R.id.etAddress);
        etDescription = findViewById(R.id.etDescription);
        btnUpdate = findViewById(R.id.btnUpdateReport);
        progressBar = findViewById(R.id.progressBar);

        // Get Data
        Intent intent = getIntent();
        reportId = intent.getStringExtra("reportId");
        originalImageUrl = intent.getStringExtra("image");
        status = intent.getStringExtra("status");

        etAddress.setText(intent.getStringExtra("address"));
        etDescription.setText(intent.getStringExtra("description"));
        tvStatus.setText("Status: " + status);

        if (originalImageUrl != null && !originalImageUrl.isEmpty()) {
            Glide.with(this).load(originalImageUrl).centerCrop().into(ivImage);
        }

        // Check Status: Allow Edit ONLY if "Pending"
        if (!"Pending".equalsIgnoreCase(status)) {
            disableEditing();
        } else {
            // Enable Image Click for updates
            ivImage.setOnClickListener(v -> checkCameraPermission());
            btnUpdate.setOnClickListener(v -> startUpdateProcess());
        }
    }

    private void disableEditing() {
        etAddress.setEnabled(false);
        etDescription.setEnabled(false);
        btnUpdate.setVisibility(View.GONE);
        tvChangeHint.setVisibility(View.GONE);
        ivImage.setOnClickListener(null); // Disable image click

        // Visual cues
        etAddress.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        etDescription.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        Toast.makeText(this, "This report cannot be edited as it is " + status, Toast.LENGTH_LONG).show();
    }

    // --- Image Update Logic ---
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    newImageBitmap = (Bitmap) extras.get("data");
                    ivImage.setImageBitmap(newImageBitmap);
                    isImageChanged = true;
                }
            });

    // --- Update Process ---
    private void startUpdateProcess() {
        String newAddress = etAddress.getText().toString().trim();
        String newDesc = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(newAddress) || TextUtils.isEmpty(newDesc)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        if (isImageChanged && newImageBitmap != null) {
            uploadNewImage(newAddress, newDesc);
        } else {
            updateDatabase(newAddress, newDesc, originalImageUrl);
        }
    }

    private void uploadNewImage(String address, String description) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = mStorage.child(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        fileRef.putBytes(data).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                        updateDatabase(address, description, uri.toString())
                )
        ).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnUpdate.setEnabled(true);
            Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateDatabase(String address, String description, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("address", address);
        updates.put("description", description);
        updates.put("image", imageUrl);

        mDatabase.child(reportId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Report Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}