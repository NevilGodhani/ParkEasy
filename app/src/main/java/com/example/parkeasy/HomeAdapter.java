package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder>{
    private final Context context;
    private List<Home> home;


    public HomeAdapter(Context context, List<Home> home) {
        this.context = context;
        this.home = home;
    }
    @NonNull
    @Override
    public HomeAdapter.HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_home, parent, false);
        return new HomeAdapter.HomeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.HomeViewHolder holder, int position) {
        Home home1 = home.get(position);

        // ✅ Prevent crashes due to null values
        if (home1 == null || home1.getParkingName() == null) {
            return;
        }

        holder.name.setText(home1.getParkingName());
        holder.near.setText(home1.getNear());
        holder.s.setText(home1.getStime());
        holder.e.setText(home1.getEtime());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userUid = currentUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Like")
                    .child(userUid)
                    .child(home1.getParkingName());

            // ✅ Check if parking is already liked
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.like.setImageResource(R.drawable.baseline_favorite_red_24); // Liked
                    } else {
                        holder.like.setImageResource(R.drawable.baseline_favorite_border_24); // Not liked
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.like.setImageResource(R.drawable.baseline_favorite_border_24); // Default to unliked
        }

        // ✅ Handle view details click
        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity2.class);
            intent.putExtra("name", home1.getParkingName());
            context.startActivity(intent);
        });

        // ✅ Prevent Picasso from crashing when image URL is null
        Picasso.get()
                .load(home1.getImageurl())
                .placeholder(R.drawable.ic_launcher_background) // Show placeholder if null
                .error(R.drawable.baseline_person_24) // Show error image if invalid
                .fit()
                .centerCrop()
                .into(holder.imageView);

        // ✅ Handle like button click
        holder.like.setOnClickListener(v -> {
            if (currentUser != null) {
                String userUid = currentUser.getUid();
                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Like").child(userUid);
                DatabaseReference parkingRef = itemRef.child(home1.getParkingName());

                parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // ✅ If already liked, remove from database
                            parkingRef.removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    holder.like.setImageResource(R.drawable.baseline_favorite_border_24);
                                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // ✅ If not liked, add to database
                            parkingRef.setValue(true).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    holder.like.setImageResource(R.drawable.baseline_favorite_red_24);
                                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return home.size();
    }
    public void filterList(List<Home> filteredList) {
       home=filteredList;
        notifyDataSetChanged();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder{
        public TextView name,near,s,e;
        public ImageView imageView,like;
        CardView view;
        @SuppressLint("WrongViewCast")
        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.parkingName);
            near=itemView.findViewById(R.id.parkingAddress);
            imageView = itemView.findViewById(R.id.parkingImage);
            s=itemView.findViewById(R.id.startTimeText);
            e=itemView.findViewById(R.id.endTimeText);
            like=itemView.findViewById(R.id.likeButton);
            view=itemView.findViewById(R.id.viewDetailsCard);
        }
    }
}
