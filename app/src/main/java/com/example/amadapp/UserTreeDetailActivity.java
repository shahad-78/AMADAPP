package com.example.amadapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.Tree;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserTreeDetailActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvName, tvScientificName, tvDate, tvLocation, tvDescription;
    private ImageButton btnClose;

    // Store lat/lng to use in the click listener
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tree_detail);

        // Init Views
        ivImage = findViewById(R.id.ivDetailTreeImage);
        tvName = findViewById(R.id.tvDetailTreeName);
        tvScientificName = findViewById(R.id.tvDetailScientificName);
        tvDate = findViewById(R.id.tvDetailDate);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvDescription = findViewById(R.id.tvDetailDescription);
        btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> finish());

        // Get Data passed from Adapter
        String treeID = getIntent().getStringExtra("treeID");
        String userImage = getIntent().getStringExtra("image");
        String date = getIntent().getStringExtra("date");
        latitude = getIntent().getStringExtra("lat");
        longitude = getIntent().getStringExtra("lng");

        // Set User Data
        tvDate.setText("Planted on: " + (date != null ? date : "Unknown Date"));

        if (latitude != null && longitude != null) {
            tvLocation.setText("Location: " + latitude + ", " + longitude);
            // Make location clickable
            tvLocation.setTextColor(getResources().getColor(R.color.green)); // Visual cue
            tvLocation.setOnClickListener(v -> openMap(latitude, longitude));
        } else {
            tvLocation.setText("Location: Unknown");
        }

        if (userImage != null && !userImage.isEmpty()) {
            Glide.with(this).load(userImage).centerCrop().into(ivImage);
        }

        // Fetch Species Data from 'Trees' node
        if (treeID != null) {
            fetchTreeDetails(treeID);
        } else {
            tvName.setText("Unknown Tree");
        }
    }

    private void openMap(String lat, String lng) {
        try {
            // Create a Uri from an intent string. Use the result to create an Intent.
            // geo:0,0?q=lat,lng(Label)
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(Planted Tree)");

            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps");

            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent);
        } catch (Exception e) {
            // Fallback: If Google Maps app isn't installed, open in browser
            try {
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Could not open map application.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchTreeDetails(String treeID) {
        DatabaseReference treeRef = FirebaseDatabase.getInstance().getReference("Trees").child(treeID);
        treeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Tree tree = snapshot.getValue(Tree.class);
                    if (tree != null) {
                        tvName.setText(tree.getName());
                        tvScientificName.setText(tree.getScientificName());
                        tvDescription.setText(tree.getDescription());
                    }
                } else {
                    tvName.setText("Tree details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserTreeDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}