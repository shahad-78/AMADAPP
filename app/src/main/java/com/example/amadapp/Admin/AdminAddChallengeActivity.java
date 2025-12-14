package com.example.amadapp.Admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.amadapp.Model.Challenge;
import com.example.amadapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;

public class AdminAddChallengeActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etStartDate, etEndDate, etPoints, etImageUrl;
    private Button btnSave;
    private DatabaseReference mDatabase;

    // For Edit Mode
    private String challengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_challenge);

        mDatabase = FirebaseDatabase.getInstance().getReference("Challenges");

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etPoints = findViewById(R.id.etPoints);
        etImageUrl = findViewById(R.id.etImageUrl);
        btnSave = findViewById(R.id.btnSave);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Challenge");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- NEW: Configure Date Pickers ---
        setupDatePicker(etStartDate);
        setupDatePicker(etEndDate);

        // Check Intent for Edit Data
        if (getIntent().hasExtra("id")) {
            getSupportActionBar().setTitle("Edit Challenge");
            challengeId = getIntent().getStringExtra("id");

            etTitle.setText(getIntent().getStringExtra("title"));
            etDescription.setText(getIntent().getStringExtra("description"));
            etStartDate.setText(getIntent().getStringExtra("start_date"));
            etEndDate.setText(getIntent().getStringExtra("end_date"));
            etPoints.setText(String.valueOf(getIntent().getIntExtra("points", 0)));
            etImageUrl.setText(getIntent().getStringExtra("image_url"));
        }

        btnSave.setOnClickListener(v -> saveChallenge());
    }

    // --- NEW: Helper method to show DatePicker ---
    private void setupDatePicker(EditText editText) {
        // Disable manual typing
        editText.setFocusable(false);
        editText.setClickable(true);

        editText.setOnClickListener(v -> {
            // Get current date or parsed date from text
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            if (!TextUtils.isEmpty(editText.getText())) {
                try {
                    String[] parts = editText.getText().toString().split("-");
                    if (parts.length == 3) {
                        year = Integer.parseInt(parts[0]);
                        month = Integer.parseInt(parts[1]) - 1; // Month is 0-indexed in Calendar
                        day = Integer.parseInt(parts[2]);
                    }
                } catch (NumberFormatException e) {
                    // Ignore parse errors, fallback to current date
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format: YYYY-MM-DD (e.g., 2025-01-05)
                        String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editText.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void saveChallenge() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String start = etStartDate.getText().toString().trim();
        String end = etEndDate.getText().toString().trim();
        String pointsStr = etPoints.getText().toString().trim();
        String imgUrl = etImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(pointsStr)) {
            etPoints.setError("Points are required");
            return;
        }
        if (TextUtils.isEmpty(start)) {
            etStartDate.setError("Start Date is required");
            return;
        }
        if (TextUtils.isEmpty(end)) {
            etEndDate.setError("End Date is required");
            return;
        }

        int points = Integer.parseInt(pointsStr);

        // Challenge(title, description, start_date, end_date, points, image_url)
        Challenge challenge = new Challenge(title, desc, start, end, points, imgUrl);

        if (challengeId != null) {
            // Update
            mDatabase.child(challengeId).setValue(challenge).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Challenge Updated", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
        } else {
            // Add New
            mDatabase.push().setValue(challenge).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Challenge Added", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> Toast.makeText(this, "Add Failed", Toast.LENGTH_SHORT).show());
        }
    }
}