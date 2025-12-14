package com.example.amadapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.RecentActivity;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private List<RecentActivity> activityList;

    public RecentActivityAdapter(List<RecentActivity> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the dedicated layout 'item_activity.xml'
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivity activity = activityList.get(position);

        holder.description.setText(activity.getTitle());
        holder.time.setText(activity.getDate());

        // Optional: Set icon based on type (tree vs report)
        // You can uncomment and ensure these drawables exist in your project
        /*
        if ("tree".equals(activity.getType())) {
             holder.icon.setImageResource(R.drawable.tree_img);
        } else if ("report".equals(activity.getType())) {
             holder.icon.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        */
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView description, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Map views from item_activity.xml
            icon = itemView.findViewById(R.id.activity_icon);
            description = itemView.findViewById(R.id.activity_description);
            time = itemView.findViewById(R.id.activity_time);
        }
    }
}