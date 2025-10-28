package com.example.parkeasy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ViewHolder> {
    private List<Like> parkingList1;
    private Context context;
    public ParkingAdapter(Context context, List<Like> parkingList1) {
        this.parkingList1 = parkingList1;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_like, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Like model = parkingList1.get(position);

        holder.parkingName.setText(model.getParkingName());
        holder.near.setText("Area : " + model.getNear());
        holder.stime.setText(model.getStime());
        holder.etime.setText(model.getEtime());
        // ✅ Handle view details click
        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity2.class);
            intent.putExtra("name", model.getParkingName());
            context.startActivity(intent);
        });
        // Check if image URL is null or empty
        if (model.getImageurl() != null && !model.getImageurl().isEmpty()) {
            Picasso.get()
                    .load(model.getImageurl())
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                    .error(R.drawable.baseline_person_24) // Error image
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.baseline_person_24); // Default image
        }
    }

    @Override
    public int getItemCount() {
        return parkingList1.size();
    }

    public void filterList(List<Like> filteredList) {
        parkingList1 =  filteredList;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView parkingName, near, stime, etime;
        ImageView imageView;
        CardView view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingName = itemView.findViewById(R.id.parkingName);
            near = itemView.findViewById(R.id.parkingAddress);
            stime = itemView.findViewById(R.id.startTimeText);
            etime = itemView.findViewById(R.id.endTimeText);
            imageView = itemView.findViewById(R.id.parkingImage);
            view=itemView.findViewById(R.id.viewDetailsCard);
        }
    }
}

