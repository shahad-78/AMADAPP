package com.example.amadapp.Admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.amadapp.Model.EducationContent;
import com.example.amadapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminAddContentActivity extends AppCompatActivity {

    private EditText etTitle, etContent, etImageUrl, etExternalUrl;
    private Button btnSave;
    private DatabaseReference mDatabase;

    // Variables for Editing mode
    private String contentId;
    // private String existingDate; // Removed as we will always use current date on save

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_content);

        // Init Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("EducationalContents");

        // Init Views
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        etImageUrl = findViewById(R.id.etImageUrl);
        etExternalUrl = findViewById(R.id.etExternalUrl);
        btnSave = findViewById(R.id.btnSave);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Content");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Check for Intent Extras (Edit Mode)
        if (getIntent().hasExtra("id")) {
            getSupportActionBar().setTitle("Edit Content");
            contentId = getIntent().getStringExtra("id");
            // existingDate = getIntent().getStringExtra("date"); // No longer needed

            etTitle.setText(getIntent().getStringExtra("title"));
            etContent.setText(getIntent().getStringExtra("content"));
            etImageUrl.setText(getIntent().getStringExtra("image"));
            etExternalUrl.setText(getIntent().getStringExtra("url"));
        }

        btnSave.setOnClickListener(v -> saveContent());
    }

    private void saveContent() {
        String title = etTitle.getText().toString().trim();
        String desc = etContent.getText().toString().trim();
        String imgUrl = etImageUrl.getText().toString().trim();
        String extUrl = etExternalUrl.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "Title and Content are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- UPDATED: Always use current date for both Add and Edit ---
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        EducationContent newContent = new EducationContent(title, desc, date, extUrl, imgUrl);

        if (contentId != null) {
            // Update
            mDatabase.child(contentId).setValue(newContent)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Content Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
        } else {
            // Add New
            mDatabase.push().setValue(newContent)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Content Added", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Add Failed", Toast.LENGTH_SHORT).show());
        }
    }
}