package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.TimeZone;

public class ParkingFragment extends Fragment {
    public static String namee;
    Context context =getActivity();
    private ImageView parkingImage;
    private TextView parkingName, parkingAddress, parkingNear, parkingContact, facilityCharging, facilityAir, price2t, price4t;
    private Button bookParkingButton,close;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking, container, false);
        Bundle bundle = getArguments();
        parkingImage = view.findViewById(R.id.parking_image);
        parkingName = view.findViewById(R.id.parking_name);
        parkingAddress = view.findViewById(R.id.parking_address);
        parkingNear = view.findViewById(R.id.parking_near);
        parkingContact = view.findViewById(R.id.parking_contact);
        facilityCharging = view.findViewById(R.id.facility_charging);
        facilityAir = view.findViewById(R.id.facility_air);
        price2t = view.findViewById(R.id.price2);
        price4t = view.findViewById(R.id.price4);
        bookParkingButton = view.findViewById(R.id.book_parking_button);
        close=view.findViewById(R.id.close_parking);
        if (bundle != null) {
            namee = bundle.getString("name");
            parkingName.setText(namee);
        }
        checkParkingAvailability();
        Context context=getContext();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (namee != null) {
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Parkings");

            userReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            for (DataSnapshot ownerSnapshot : snapshot.getChildren()) { // Iterate owner IDs
                                for (DataSnapshot locationSnapshot : ownerSnapshot.getChildren()) { // Iterate locations
                                    for (DataSnapshot parkingSnapshot : locationSnapshot.getChildren()) { // Iterate parkings
                                        if (parkingSnapshot.getKey().equals(namee)) { // Find "Shiv Plaza"
                                            Parking parking = parkingSnapshot.getValue(Parking.class);
                                            if (parking != null) {
                                                // Extract and display data
                                                parkingName.setText(parking.getParkingName());
                                                parkingAddress.setText("\uD83D\uDCCD Address: " + parking.getParkingAddress());
                                                parkingNear.setText("\uD83D\uDCCC Near: " + parking.getNear());
                                                parkingContact.setText("\uD83D\uDCDE Contact: " + parking.getContact());

                                                // Load Image
                                                Picasso.get().load(parking.getImageurl()).fit().centerCrop().into(parkingImage);

                                                // Facility Handling
                                                facilityCharging.setVisibility(parking.getCharging().equals("false") ? View.GONE : View.VISIBLE);
                                                facilityAir.setVisibility(parking.getAir().equals("false") ? View.GONE : View.VISIBLE);

                                                // Price Handling
                                                price2t.setVisibility(parking.getPrice2().equals("0") ? View.GONE : View.VISIBLE);
                                                price2t.setText("\uD83C\uDFCD\uFE0F 2-Wheel: ₹" + parking.getPrice2() + "/h");

                                                price4t.setVisibility(parking.getPrice4().equals("0") ? View.GONE : View.VISIBLE);
                                                price4t.setText("\uD83D\uDE97 4-Wheel: ₹" + parking.getPrice4() + "/h");
                                            }
                                            return; // Exit loop once found
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "No data available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Failed to read value", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        bookParkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(), MainActivity2.class);
                i.putExtra("book","book");
                i.putExtra("name",parkingName.getText());
                startActivity(i);
            }
        });
        return view;
    }
    private void checkParkingAvailability() {
        DatabaseReference parkingRef = FirebaseDatabase.getInstance().getReference("Parkings");
        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getActivity(), "No parking data available", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean foundParking = false;
                for (DataSnapshot ownerSnapshot : snapshot.getChildren()) { // Iterate owner IDs
                    for (DataSnapshot locationSnapshot : ownerSnapshot.getChildren()) { // Iterate locations
                        for (DataSnapshot parkingSnapshot : locationSnapshot.getChildren()) { // Iterate parkings
                            if (parkingSnapshot.getKey().equals(namee)) {
                                foundParking = true;
                                String stimeStr = parkingSnapshot.child("stime").getValue(String.class);
                                String etimeStr = parkingSnapshot.child("etime").getValue(String.class);

                                if (stimeStr == null || etimeStr == null) {
                                    Toast.makeText(getActivity(), "Parking Time Not Set", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                try {
                                    int startHour = Integer.parseInt(stimeStr.split(":")[0]);
                                    int endHour = Integer.parseInt(etimeStr.split(":")[0]);

                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                                    if (currentHour >= startHour && currentHour < endHour) {
                                        if (bookParkingButton != null) bookParkingButton.setVisibility(View.VISIBLE);
                                        if (close != null) close.setVisibility(View.GONE);
                                    } else {
                                        if (bookParkingButton != null) bookParkingButton.setVisibility(View.GONE);
                                        if (close != null) close.setVisibility(View.VISIBLE);
                                    }
                                } catch (NumberFormatException e) {
                                    Toast.makeText(getActivity(), "Invalid time format", Toast.LENGTH_SHORT).show();
                                }
                                return; // Exit after finding the correct parking
                            }
                        }
                    }
                }

                if (!foundParking) {
                    Toast.makeText(getActivity(), "Parking not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Parking Details");
    }

}