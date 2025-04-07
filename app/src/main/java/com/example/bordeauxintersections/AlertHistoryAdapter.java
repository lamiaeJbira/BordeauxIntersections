

package com.example.bordeauxintersections;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertHistoryAdapter extends RecyclerView.Adapter<AlertHistoryAdapter.ViewHolder> {
    private List<AlertHistoryEntry> alertHistoryList;

    public AlertHistoryAdapter(List<AlertHistoryEntry> alertHistoryList) {
        this.alertHistoryList = alertHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertHistoryEntry entry = alertHistoryList.get(position);
        holder.intersectionName.setText(entry.getIntersectionName());
        holder.distance.setText(String.format("%.2f m", entry.getDistance()));
        // Vous pouvez également formatter le timestamp si besoin
    }

    @Override
    public int getItemCount() {
        return alertHistoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView intersectionName;
        TextView distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            intersectionName = itemView.findViewById(R.id.text_intersection);
            distance = itemView.findViewById(R.id.text_distance);
        }
    }
}
