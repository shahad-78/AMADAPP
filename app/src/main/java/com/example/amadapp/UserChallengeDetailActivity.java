package com.example.amadapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.Challenge;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class UserChallengeDetailActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvTitle, tvPoints, tvStatus, tvProgressText, tvDescription;
    private ProgressBar pbProgress;
    private Button btnUploadProof;
    private ImageButton btnClose;

    private String challengeID, status;
    private int progress;
    private Uri photoURI;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_challenge_detail);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference("ChallengeProofs");
        mAuth = FirebaseAuth.getInstance();

        ivImage = findViewById(R.id.ivChallengeDetailImage);
        tvTitle = findViewById(R.id.tvChallengeDetailTitle);
        tvPoints = findViewById(R.id.tvChallengePoints);
        tvStatus = findViewById(R.id.tvChallengeStatus);
        tvProgressText = findViewById(R.id.tvChallengeProgress);
        tvDescription = findViewById(R.id.tvChallengeDescription);
        pbProgress = findViewById(R.id.pbChallengeProgress);
        btnUploadProof = findViewById(R.id.btnUploadProof);
        btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> finish());

        challengeID = getIntent().getStringExtra("challengeID");
        status = getIntent().getStringExtra("status");
        progress = getIntent().getIntExtra("progress", 0);

        tvStatus.setText("Status: " + (status != null ? status : "Unknown"));
        tvProgressText.setText("Progress: " + progress + "%");
        pbProgress.setProgress(progress);

        if (status != null && !status.equalsIgnoreCase("Joined")) {
            btnUploadProof.setEnabled(false);
            btnUploadProof.setText("Challenge " + status);
            btnUploadProof.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        if (challengeID != null) {
            mDatabase.child("Challenges").child(challengeID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Challenge c = snapshot.getValue(Challenge.class);
                    if (c != null) {
                        tvTitle.setText(c.getTitle());
                        tvDescription.setText(c.getDescription());
                        tvPoints.setText(c.getPoints() + " Pts");
                        Glide.with(UserChallengeDetailActivity.this).load(c.getImage_url()).centerCrop().into(ivImage);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }

        btnUploadProof.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            photoFile = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
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
                Toast.makeText(this, "Unable to open camera app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    uploadProofImage();
                }
            });

    private void uploadProofImage() {
        if (mAuth.getCurrentUser() == null || photoURI == null) return;

        // --- AUTHENTICITY CHECK ---
        ImageValidator.ValidationResult result = ImageValidator.verifyImageAuthenticity(this, photoURI);
        if (!result.isValid) {
            Toast.makeText(this, "Verification Failed: " + result.message, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Uploading proof...", Toast.LENGTH_SHORT).show();
        btnUploadProof.setEnabled(false);

        String userId = mAuth.getCurrentUser().getUid();
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = mStorage.child(userId).child(fileName);

        fileRef.putFile(photoURI).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> updateChallengeStatus(uri.toString()))
        ).addOnFailureListener(e -> {
            btnUploadProof.setEnabled(true);
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateChallengeStatus(String proofUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("usersChallenge").child(userId).orderByChild("challengID").equalTo(challengeID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            child.getRef().child("status").setValue("Pending");
                            child.getRef().child("proofImage").setValue(proofUrl);
                            child.getRef().child("progress").setValue(100);
                        }
                        Toast.makeText(UserChallengeDetailActivity.this, "Proof submitted!", Toast.LENGTH_LONG).show();
                        tvStatus.setText("Status: Pending");
                        tvProgressText.setText("Progress: 100%");
                        pbProgress.setProgress(100);
                        btnUploadProof.setText("Pending Approval");
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        btnUploadProof.setEnabled(true);
                    }
                });
    }
}