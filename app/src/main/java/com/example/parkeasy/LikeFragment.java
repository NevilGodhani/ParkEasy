package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LikeFragment extends Fragment {
    private RecyclerView recyclerView;
    private DatabaseReference likeRef, parkingRef;
    private ParkingAdapter adapter1;
    private List<Like> home1;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=auth.getCurrentUser();
    String UID=currentUser.getUid();

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view= inflater.inflate(R.layout.fragment_like, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        home1 = new ArrayList<>();
        adapter1 = new ParkingAdapter(getContext(), home1);
        recyclerView.setAdapter(adapter1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        EditText searchEditText = view.findViewById(R.id.et_search);

        likeRef = FirebaseDatabase.getInstance().getReference("Like").child(UID);
        parkingRef = FirebaseDatabase.getInstance().getReference("Parkings");

        fetchLikedParkings();
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString()); // Call filtering method
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        return view;
    }

    private void filterList(String query) {
        List<Like> filteredList = new ArrayList<>();
        for (Like parking : home1) {  // Make sure `parkingList` is initialized
            if (parking.getParkingName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(parking);
            }
        }
        adapter1.filterList(filteredList);  // Update adapter with filtered data
    }

    private void fetchLikedParkings() {
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                home1.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                        String parkingName = likeSnapshot.getKey(); // Get liked parking name

                        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean found = false;
                                for (DataSnapshot parkingSnapshot : dataSnapshot.getChildren()) {
                                    for (DataSnapshot areaSnapshot : parkingSnapshot.getChildren()) { // Kamrej, Pasodra
                                        for (DataSnapshot parkingEntry : areaSnapshot.getChildren()) { // Parking Names
                                            String name = parkingEntry.child("parkingName").getValue(String.class);
                                            if (name != null && name.equals(parkingName)) {
                                                Like home2 = parkingEntry.getValue(Like.class);
                                                home1.add(home2);
                                                Log.d("ParkingDebug", "Matched Parking: " + name);
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (found) break;
                                    }
                                }
                                adapter1.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.e("ParkingDebug", "Error: " + error.getMessage());
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "No liked parkings found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LikeDebug", "Error: " + error.getMessage());
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Favourites");
    }
}