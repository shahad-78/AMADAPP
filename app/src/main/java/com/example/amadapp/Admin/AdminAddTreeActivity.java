package com.example.amadapp.Admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.amadapp.Model.Tree;
import com.example.amadapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class AdminAddTreeActivity extends AppCompatActivity {

    private EditText etName, etScientificName, etImageUrl, etDescription;
    private AutoCompleteTextView actvRegion; // Refactored from Spinner
    private Button btnSave;
    private DatabaseReference mDatabase;

    // For Edit Mode
    private String treeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_tree);

        mDatabase = FirebaseDatabase.getInstance().getReference("Trees");

        etName = findViewById(R.id.etName);
        etScientificName = findViewById(R.id.etScientificName);
        // Find the AutoCompleteTextView from the layout
        actvRegion = findViewById(R.id.actvRegion);
        etImageUrl = findViewById(R.id.etImageUrl);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Tree");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- NEW: Setup Exposed Dropdown Menu ---
        List<String> regions = Arrays.asList("Central", "Eastern", "Western", "Northern", "Southern", "All");
        // Using simple_list_item_1 is better for the popup list style
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, regions);
        actvRegion.setAdapter(adapter);

        // Ensure default selection doesn't require clicking twice
        actvRegion.setKeyListener(null); // Make it read-only (user must select from list)

        // Check Intent for Edit Data
        if (getIntent().hasExtra("id")) {
            getSupportActionBar().setTitle("Edit Tree");
            treeId = getIntent().getStringExtra("id");

            etName.setText(getIntent().getStringExtra("name"));
            etScientificName.setText(getIntent().getStringExtra("scientificName"));

            // --- NEW: Set Text directly for AutoCompleteTextView ---
            String currentRegion = getIntent().getStringExtra("region");
            if (currentRegion != null) {
                actvRegion.setText(currentRegion, false); // false prevents filter from triggering
            }

            etImageUrl.setText(getIntent().getStringExtra("imageUrl"));
            etDescription.setText(getIntent().getStringExtra("description"));
        }

        btnSave.setOnClickListener(v -> saveTree());
    }

    private void saveTree() {
        String name = etName.getText().toString().trim();
        String scientific = etScientificName.getText().toString().trim();
        String region = actvRegion.getText().toString();
        String imageUrl = etImageUrl.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(region)) {
            actvRegion.setError("Region is required");
            return;
        }

        Tree tree = new Tree(name, scientific, description, region, imageUrl);
        if (treeId != null) {
            // Update
            mDatabase.child(treeId).setValue(tree).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Tree Updated", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
        } else {
            // Add New
            mDatabase.push().setValue(tree).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Tree Added", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> Toast.makeText(this, "Add Failed", Toast.LENGTH_SHORT).show());
        }
    }
}