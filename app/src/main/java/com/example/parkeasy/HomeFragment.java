package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {
    private HomeAdapter adapter;
    private List<Home> home;
    Context context=getContext();
    ImageView like;
    Button filter,onn;
    EditText sea;
    private boolean isFilterApplied = false; // Track filter state

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_home, container, false);
        like=view.findViewById(R.id.menu_button);
        sea=view.findViewById(R.id.search_bar);
        filter=view.findViewById(R.id.button_filter);
        onn=view.findViewById(R.id.button_extra);
        home = new ArrayList<>();
        adapter = new HomeAdapter(getContext(), home);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Parkings");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                home.clear();
                for (DataSnapshot parkingSnapshot : dataSnapshot.getChildren()) { // Loop through 'Parkings'
                    for (DataSnapshot areaSnapshot : parkingSnapshot.getChildren()) { // Loop through areas (e.g., Katargam)
                        for (DataSnapshot locationSnapshot : areaSnapshot.getChildren()) { // Loop through locations (e.g., Shiv Temple)
                            Home home1 = locationSnapshot.getValue(Home.class);
                            home.add(home1);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),MainActivity2.class);
                intent.putExtra("like","like");
                startActivity(intent);
            }
        });
        sea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterBottomSheet();
            }
        });
        onn.setOnClickListener(v -> {
            if (isFilterApplied) {
                // Remove filter and show all data
                adapter.filterList(home);
                isFilterApplied = false;
                onn.setText("Show ON Parkings"); // Update button text
            } else {
                // Apply filter to show ON parkings
                filterOnParkings();
                isFilterApplied = true;
                onn.setText("Show All Parkings"); // Update button text
            }
        });


        return view;
    }
    private void filterList(String query) {
        List<Home> filteredList = new ArrayList<>();
        for (Home home : home) {
            if (home.getParkingName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(home);
            }
        }
        adapter.filterList(filteredList);  // Update adapter with filtered data
    }
    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        RadioGroup areaFilterGroup = sheetView.findViewById(R.id.area_filter_group);
        Button applyFilterButton = sheetView.findViewById(R.id.button_apply_area_filter);

        applyFilterButton.setOnClickListener(v -> {
            int selectedId = areaFilterGroup.getCheckedRadioButtonId();
            String selectedArea = "All"; // Default is all areas

            if (selectedId == R.id.area_katargam) {
                selectedArea = "Katargam";
            } else if (selectedId == R.id.area_mota) {
                selectedArea = "Mota Varachha";
            } else if (selectedId == R.id.area_varachha) {
                selectedArea = "Varachha";
            } else if (selectedId == R.id.area_nana) {
                selectedArea = "Nana Varachha";
            }

            filterParkingByArea(selectedArea);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
    private void filterParkingByArea(String area) {
        List<Home> filteredList = new ArrayList<>();

        if (area.equals("All")) {
            filteredList.addAll(home); // Show all parkings
        } else {
            for (Home home : home) {
                if (home.getNear().equalsIgnoreCase(area)) {
                    filteredList.add(home);
                }
            }
        }

        adapter.filterList(filteredList);
    }
    private void filterOnParkings() {
        List<Home> filteredList = new ArrayList<>();

        // Get current time in 24-hour format
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // Gets the hour (0-23)

        for (Home home : home) {
            if (home == null) {
                Log.e("FilterError", "Home object is null");
                continue;
            }

            String stimeStr = home.getStime(); // Example: "03:00"
            String etimeStr = home.getEtime(); // Example: "15:00"

            if (stimeStr == null || etimeStr == null || stimeStr.isEmpty() || etimeStr.isEmpty()) {
                Log.e("FilterError", "Missing time data for parking: " + home.getParkingName());
                continue;
            }

            try {
                int startTime = Integer.parseInt(stimeStr.split(":")[0]); // Extract only hour
                int endTime = Integer.parseInt(etimeStr.split(":")[0]);   // Extract only hour

                if (currentHour >= startTime && currentHour < endTime) {
                    filteredList.add(home); // Parking is ON, add to the list
                }
            } catch (NumberFormatException e) {
                Log.e("FilterError", "Invalid time format for: " + home.getParkingName(), e);
            }
        }


        // Update the adapter
        adapter.filterList(filteredList);
    }

}