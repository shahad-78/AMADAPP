package com.example.amadapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.DegradedArea;

import java.util.List;

public class UserReportsAdapter extends RecyclerView.Adapter<UserReportsAdapter.ViewHolder> {

    // Helper class to hold ID + Data
    public static class ReportItem {
        public String id;
        public DegradedArea report;

        public ReportItem(String id, DegradedArea report) {
            this.id = id;
            this.report = report;
        }
    }

    private List<ReportItem> reportList;
    private Context context;

    public UserReportsAdapter(List<ReportItem> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportItem item = reportList.get(position);
        DegradedArea report = item.report;

        holder.tvDate.setText(report.getDate());
        holder.tvDesc.setText(report.getDescription());
        holder.tvStatus.setText(report.getStatus());

        // Status Coloring
        if ("Confirmed".equalsIgnoreCase(report.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if ("Rejected".equalsIgnoreCase(report.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange (Pending)
        }

        if (report.getImage() != null && !report.getImage().isEmpty()) {
            Glide.with(context)
                    .load(report.getImage())
                    .centerCrop()
                    .placeholder(R.drawable.ic_report_area)
                    .into(holder.ivImage);
            holder.ivImage.setPadding(0, 0, 0, 0);
        }

        // --- Handle Item Click ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserEditReportActivity.class);
            intent.putExtra("reportId", item.id);
            intent.putExtra("description", report.getDescription());
            intent.putExtra("address", report.getAddress());
            intent.putExtra("image", report.getImage());
            intent.putExtra("status", report.getStatus());
            intent.putExtra("lat", report.getLat());
            intent.putExtra("lng", report.getLng());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvDate, tvDesc, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivReportImage);
            tvDate = itemView.findViewById(R.id.tvReportDate);
            tvDesc = itemView.findViewById(R.id.tvReportDesc);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
        }
    }
}