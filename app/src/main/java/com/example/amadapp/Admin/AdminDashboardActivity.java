package com.example.amadapp.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.amadapp.MainActivity;
import com.example.amadapp.Model.Challenge;
import com.example.amadapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private ImageView btnLogout;

    // Cards
    private MaterialCardView cardUsers, cardChallenges, cardTrees, cardEducation, cardReports, cardUpdateSystem;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        // Init Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        btnLogout = findViewById(R.id.btnLogout);

        cardUsers = findViewById(R.id.cardManageUsers);
        cardChallenges = findViewById(R.id.cardManageChallenges);
        cardTrees = findViewById(R.id.cardManageTrees);
        cardEducation = findViewById(R.id.cardManageEducation);
        cardReports = findViewById(R.id.cardManageReports);
        cardUpdateSystem = findViewById(R.id.cardUpdateSystem); // NEW

        // Set Click Listeners
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(AdminDashboardActivity.this, MainActivity.class));
            finish();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        });

        cardUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageUsersActivity.class);
            startActivity(intent);
        });

        cardChallenges.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageChallengesActivity.class);
            startActivity(intent);
        });

        cardTrees.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageTreesActivity.class);
            startActivity(intent);
        });

        cardEducation.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageEducationActivity.class);
            startActivity(intent);
        });

        cardReports.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageReportsActivity.class);
            startActivity(intent);
        });

        // --- NEW: Update System Listener ---
        cardUpdateSystem.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("System Update")
                    .setMessage("This will scan all challenges and delete any that have passed their end date. Continue?")
                    .setPositiveButton("Yes", (dialog, which) -> performSystemUpdate())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void performSystemUpdate() {
        Toast.makeText(this, "Checking for expired challenges...", Toast.LENGTH_SHORT).show();

        mDatabase.child("Challenges").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int deletedCount = 0;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date currentDate = new Date();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Challenge challenge = data.getValue(Challenge.class);
                    if (challenge != null && challenge.getEnd_date() != null) {
                        try {
                            Date endDate = sdf.parse(challenge.getEnd_date());

                            // Check if endDate is before today (meaning it expired yesterday or earlier)
                            // We normalize current date to start of day if strict day comparison is needed,
                            // but usually direct comparison works: if endDate was yesterday, it's expired.
                            if (endDate != null && endDate.before(currentDate)) {
                                // Double check: if end date is TODAY, it is NOT expired yet.
                                // endDate.before(currentDate) returns true if endDate (00:00) is before now (e.g. 10:00)
                                // So we need to check if the day is strictly before today.

                                long diff = currentDate.getTime() - endDate.getTime();
                                long daysDiff = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);

                                // If 1 or more days passed since end date
                                if (daysDiff >= 1) {
                                    data.getRef().removeValue();
                                    deletedCount++;
                                }
                            }
                        } catch (ParseException e) {
                            Log.e("AdminUpdate", "Date parse error: " + e.getMessage());
                        }
                    }
                }

                String resultMsg = (deletedCount > 0)
                        ? "System Updated. Deleted " + deletedCount + " expired challenges."
                        : "System is up to date. No expired challenges found.";

                new AlertDialog.Builder(AdminDashboardActivity.this)
                        .setTitle("Update Complete")
                        .setMessage(resultMsg)
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Update Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}