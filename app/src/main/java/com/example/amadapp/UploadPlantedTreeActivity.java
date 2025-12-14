package com.example.amadapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.amadapp.Model.UserTree;
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

public class UploadPlantedTreeActivity extends AppCompatActivity {

    private ImageView ivPlantedTree;
    private Button btnCamera, btnUpload;
    private ProgressBar progressBar;
    private String treeID;
    private Uri photoURI;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_planted_tree);

        treeID = getIntent().getStringExtra("treeID");
        if (treeID == null) finish();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("userTrees");
        mStorage = FirebaseStorage.getInstance().getReference("PlantedTrees");

        ivPlantedTree = findViewById(R.id.ivPlantedTree);
        btnCamera = findViewById(R.id.btnCamera);
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);

        btnCamera.setOnClickListener(v -> checkPermissions());
        btnUpload.setOnClickListener(v -> uploadImage());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure the file is created successfully
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }

        // If file was created, launch camera
        if (photoFile != null) {
            try {
                photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            } catch (Exception e) {
                // Catches ActivityNotFoundException or SecurityException
                Toast.makeText(this, "Unable to open camera app.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    ivPlantedTree.setImageURI(photoURI);
                    ivPlantedTree.setPadding(0, 0, 0, 0);
                }
            });

    private void uploadImage() {
        if (photoURI == null) {
            Toast.makeText(this, "Please take a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- AUTHENTICITY CHECK ---
        ImageValidator.ValidationResult result = ImageValidator.verifyImageAuthenticity(this, photoURI);
        if (!result.isValid) {
            Toast.makeText(this, "Verification Failed: " + result.message, Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        String userId = mAuth.getCurrentUser().getUid();
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = mStorage.child(userId).child(fileName);

        fileRef.putFile(photoURI).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                        getCurrentLocationAndSave(userId, uri.toString())
                )
        ).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void getCurrentLocationAndSave(String userId, String imageUrl) {
        String lat = "0.0";
        String lng = "0.0";
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    lat = String.valueOf(location.getLatitude());
                    lng = String.valueOf(location.getLongitude());
                }
            }
        } catch (Exception e) {}

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        UserTree userTree = new UserTree(lng, lat, treeID, imageUrl, currentDate);

        mDatabase.child(userId).push().setValue(userTree).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Tree Uploaded!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}