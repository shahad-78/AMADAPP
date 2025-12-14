package com.example.amadapp.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.DegradedArea;
import com.example.amadapp.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminReportsAdapter adapter;
    private List<AdminReportsAdapter.AdminReportItem> allReports; // Master list
    private List<AdminReportsAdapter.AdminReportItem> filteredReports; // Display list
    private DatabaseReference mDatabase;

    // UI Stats
    private TextView tvPending, tvConfirmed, tvRejected;
    private ChipGroup chipGroupFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_reports);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Reports");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mDatabase = FirebaseDatabase.getInstance().getReference("DegradedAreas");

        tvPending = findViewById(R.id.tvCountPending);
        tvConfirmed = findViewById(R.id.tvCountConfirmed);
        tvRejected = findViewById(R.id.tvCountRejected);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        recyclerView = findViewById(R.id.rvReports);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allReports = new ArrayList<>();
        filteredReports = new ArrayList<>();

        adapter = new AdminReportsAdapter(filteredReports, new AdminReportsAdapter.OnReportActionListener() {
            @Override
            public void onReject(AdminReportsAdapter.AdminReportItem item) {
                confirmReject(item);
            }

            @Override
            public void onForward(AdminReportsAdapter.AdminReportItem item) {
                forwardReport(item);
            }

            @Override
            public void onDelete(AdminReportsAdapter.AdminReportItem item) {
                // Permanently delete from database
                mDatabase.child(item.id).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(AdminManageReportsActivity.this, "Report Deleted Permanently", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(AdminManageReportsActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show());
            }
        });
        recyclerView.setAdapter(adapter);

        // Chip Filter Listener
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> filterReports(checkedId));

        fetchReports();
    }

    private void fetchReports() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allReports.clear();
                long pending = 0, confirmed = 0, rejected = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    DegradedArea report = data.getValue(DegradedArea.class);
                    if (report != null) {
                        allReports.add(new AdminReportsAdapter.AdminReportItem(data.getKey(), report));

                        // Count Statuses
                        if ("Pending".equalsIgnoreCase(report.getStatus())) pending++;
                        else if ("Confirmed".equalsIgnoreCase(report.getStatus())) confirmed++;
                        else if ("Rejected".equalsIgnoreCase(report.getStatus())) rejected++;
                    }
                }

                // Update Stats UI
                tvPending.setText(String.valueOf(pending));
                tvConfirmed.setText(String.valueOf(confirmed));
                tvRejected.setText(String.valueOf(rejected));

                // Refresh current filter
                filterReports(chipGroupFilter.getCheckedChipId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminManageReportsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterReports(int checkedId) {
        filteredReports.clear();
        String statusFilter = "All";

        if (checkedId == R.id.chipPending) statusFilter = "Pending";
        else if (checkedId == R.id.chipConfirmed) statusFilter = "Confirmed";
        else if (checkedId == R.id.chipRejected) statusFilter = "Rejected";

        for (AdminReportsAdapter.AdminReportItem item : allReports) {
            if (statusFilter.equals("All") || statusFilter.equalsIgnoreCase(item.report.getStatus())) {
                filteredReports.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmReject(AdminReportsAdapter.AdminReportItem item) {
        // Update Status to Rejected instead of deleting
        mDatabase.child(item.id).child("status").setValue("Rejected")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Report Rejected", Toast.LENGTH_SHORT).show());
    }

    private void forwardReport(AdminReportsAdapter.AdminReportItem item) {
        DegradedArea report = item.report;

        String subject = "Report of Degraded Area: " + report.getDate();
        String body = "Hello,\n\n" +
                "We received a report about a degraded environmental area that requires attention.\n\n" +
                "Details:\n" +
                "- Date: " + report.getDate() + "\n" +
                "- Location: " + report.getAddress() + " (" + report.getLat() + ", " + report.getLng() + ")\n" +
                "- Description: " + report.getDescription() + "\n\n" +
                "Image Evidence: " + report.getImage() + "\n\n" +
                "Submitted via AMAD App.";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Report via..."));

            // Auto-Confirm on Forward
            mDatabase.child(item.id).child("status").setValue("Confirmed");

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}