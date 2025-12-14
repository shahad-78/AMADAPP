package com.example.amadapp.Admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.DegradedArea;
import com.example.amadapp.R;

import java.util.List;

public class AdminReportsAdapter extends RecyclerView.Adapter<AdminReportsAdapter.ViewHolder> {

    public static class AdminReportItem {
        public String id;
        public DegradedArea report;

        public AdminReportItem(String id, DegradedArea report) {
            this.id = id;
            this.report = report;
        }
    }

    private List<AdminReportItem> reportList;
    private OnReportActionListener listener;
    private Context context;

    public interface OnReportActionListener {
        void onReject(AdminReportItem item); // Changed from onDelete

        void onForward(AdminReportItem item);

        void onDelete(AdminReportItem item);
    }

    public AdminReportsAdapter(List<AdminReportItem> reportList, OnReportActionListener listener) {
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminReportItem item = reportList.get(position);
        DegradedArea report = item.report;

        holder.tvDate.setText(report.getDate());
        holder.tvStatus.setText(report.getStatus());
        holder.tvDesc.setText(report.getDescription());
        holder.tvLocation.setText("Location: " + report.getLat() + ", " + report.getLng());

        // Visual cues for status
        if ("Confirmed".equalsIgnoreCase(report.getStatus())) {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.green));
        } else if ("Rejected".equalsIgnoreCase(report.getStatus())) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }

        if (report.getImage() != null && !report.getImage().isEmpty()) {
            Glide.with(context)
                    .load(report.getImage())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivImage);
        }

        holder.tvLocation.setOnClickListener(v -> {
            String lat = report.getLat();
            String lng = report.getLng();
            if (lat != null && !lat.isEmpty() && lng != null && !lng.isEmpty()) {
                try {
                    Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Report Location)");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(mapIntent);
                    } else {
                        Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                        context.startActivity(browserIntent);
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Could not open map.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Invalid coordinates.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnReject.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Reject Report")
                    .setMessage("Are you sure you want to reject this report?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (listener != null) {
                            listener.onReject(item);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        // --- NEW: Delete Report on Image Click ---
        holder.ivImage.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Report")
                    .setMessage("Do you want to permanently delete this report from the database?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (listener != null) {
                            listener.onDelete(item); // Call the delete listener
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // FORWARD LOGIC
        holder.btnForward.setOnClickListener(v -> listener.onForward(item));

        // Disable buttons if already processed to prevent re-doing
        if (!"Pending".equalsIgnoreCase(report.getStatus())) {
            holder.btnReject.setEnabled(false);
            holder.btnForward.setEnabled(false);
            holder.btnReject.setAlpha(0.5f);
            holder.btnForward.setAlpha(0.5f);
        } else {
            holder.btnReject.setEnabled(true);
            holder.btnForward.setEnabled(true);
            holder.btnReject.setAlpha(1.0f);
            holder.btnForward.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvDate, tvStatus, tvDesc, tvLocation;
        Button btnReject, btnForward; // Renamed btnReject

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivReportImage);
            tvDate = itemView.findViewById(R.id.tvReportDate);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
            tvDesc = itemView.findViewById(R.id.tvReportDescription);
            tvLocation = itemView.findViewById(R.id.tvReportLocation);
            btnReject = itemView.findViewById(R.id.btnRejectReport); // Renamed ID
            btnForward = itemView.findViewById(R.id.btnForwardReport);
        }
    }
}