package com.example.parkeasy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> historyList;
    private List<HistoryItem> filteredList;
    private Context context;

    public HistoryAdapter(List<HistoryItem> historyList, Context context) {
        this.historyList = new ArrayList<>(historyList); // Prevent reference issues
        this.filteredList = new ArrayList<>(historyList);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = filteredList.get(position);

        holder.txtDate.setText("Date: " + item.getDate());
        holder.txtParkingName.setText("Parking: " + item.getParkingName());
        holder.txtSlot.setText("Slot: " + item.getSlot());
        holder.txtVehicleType.setText("Vehicle: " + item.getVehicleType());
        holder.txtTime.setText("Start Time: " + item.getStime() + " | End Time: " + item.getEtime());
        holder.txtPrice.setText("Total Price: ₹" + item.getTotalPrice());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // Fix adapter reset issue
    public void updateFullList(List<HistoryItem> newList) {
        historyList.clear();
        historyList.addAll(newList);
        filteredList.clear();
        filteredList.addAll(newList);
        notifyDataSetChanged();
    }

    public void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(historyList);
        } else {
            for (HistoryItem item : historyList) {
                if (item.getDate().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtParkingName, txtSlot, txtVehicleType, txtTime, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtParkingName = itemView.findViewById(R.id.txtParkingName);
            txtSlot = itemView.findViewById(R.id.txtSlot);
            txtVehicleType = itemView.findViewById(R.id.txtVehicleType);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
