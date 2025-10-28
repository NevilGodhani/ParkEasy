package com.example.parkeasy;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class BookFragment extends Fragment {
    private GridLayout bikeSlotsGrid, carSlotsGrid;
    private DatabaseReference databaseReference;
    private String selectedSlot = ""; // Stores selected slot address
    TextView date,parkingName,car,bike;// Track ImageViews
    String nameee,sprice2,sprice4,fprice,bust,buet;
    private TextView selectedSlotText;
    private View selectedView = null;
    private Button payNowButton;
    private CardView paymentLayout;
    private int biuet,biust;
    private String currentDate;
    private Handler timeChangeHandler = new Handler();
    private Handler EtimeChangeHandler = new Handler();
    private Runnable timeValidationRunnable,EtimeValidationRunnable;
    private TimePicker StimePicker,EtimePicker;
//    private Button SbtnSetTime,EbtnSetTime;
    private int SparkingStartHour,EparkingStartHour;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_book, container, false);

        if (getArguments() != null) {
            nameee = getArguments().getString("name");
        }
        bikeSlotsGrid = view.findViewById(R.id.bikeSlotsGrid);
        carSlotsGrid = view.findViewById(R.id.carSlotsGrid);
        date=view.findViewById(R.id.date);
        car=view.findViewById(R.id.car);
        bike=view.findViewById(R.id.bike);
        parkingName=view.findViewById(R.id.parkingName);
        selectedSlotText = view.findViewById(R.id.selectedSlotText);
        payNowButton = view.findViewById(R.id.payNowButton);
        paymentLayout = view.findViewById(R.id.paymentLayout);
        StimePicker = view.findViewById(R.id.StimePicker);
//        SbtnSetTime = view.findViewById(R.id.SbtnSetTime);
        EtimePicker = view.findViewById(R.id.EtimePicker);
//        EbtnSetTime = view.findViewById(R.id.EbtnSetTime);

        StimePicker.setIs24HourView(true);
        EtimePicker.setIs24HourView(true);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        currentDate = sdf.format(new Date());
        date.setText(currentDate);
        parkingName.setText(nameee);
        Visible();
        // Set TimePicker to show hours only
        StimePicker.setIs24HourView(true);
        StimePicker.setMinute(0);
        EtimePicker.setIs24HourView(true);
        EtimePicker.setMinute(0);
        disableMinuteSelection();
        EdisableMinuteSelection();
//

        // Fetch Parking Start Time
        getParkingTime();
        EgetParkingTime();
        StimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (timeValidationRunnable != null) {
                    timeChangeHandler.removeCallbacks(timeValidationRunnable); // Cancel previous validation
                }

                timeValidationRunnable = () ->{
                    validateAndSetTime(hourOfDay);
                    refreshSlots();
                } ;
                timeChangeHandler.postDelayed(timeValidationRunnable, 1500);// Delay execution by 1 second

            }
        });
        EtimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (EtimeValidationRunnable != null) {
                    EtimeChangeHandler.removeCallbacks(EtimeValidationRunnable); // Cancel previous validation
                }

                EtimeValidationRunnable = () -> {
                    EvalidateAndSetTime(hourOfDay);
                    refreshSlots();
                };
                EtimeChangeHandler.postDelayed(EtimeValidationRunnable, 1500); // Delay execution by 1 second
            }
        });
       // SbtnSetTime.setOnClickListener(v -> validateAndSetTime());
//        EbtnSetTime.setOnClickListener(v -> EvalidateAndSetTime());



        databaseReference = FirebaseDatabase.getInstance().getReference("Slot").child(nameee);
        loadSlots();
        // Hide payment layout initially
        payNowButton.setVisibility(View.GONE);
        payNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EvalidateAndSetTime(EtimePicker.getHour());
                biuet=EtimePicker.getHour();
                buet=String.valueOf(biuet);
                biust=StimePicker.getHour();
                bust=String.valueOf(biust);
                Intent intent=new Intent(getContext(),MainActivity2.class);
                intent.putExtra("ticket","ticket");
                intent.putExtra("slot",selectedSlot);
                intent.putExtra("ust",bust);
                intent.putExtra("uet",buet);
                intent.putExtra("pname",nameee);
                intent.putExtra("date",currentDate);
                startActivity(intent);
            }
        });
        return  view;
    }
    private void refreshSlots() {
        bikeSlotsGrid.removeAllViews();
        carSlotsGrid.removeAllViews();
        loadSlots(); // Reload slots dynamically
    }


    private void getParkingTime() {
        DatabaseReference parkingRef = FirebaseDatabase.getInstance().getReference("Parkings");
        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ownerSnapshot : snapshot.getChildren()) { // Iterate owner IDs
                        for (DataSnapshot locationSnapshot : ownerSnapshot.getChildren()) { // Iterate locations
                            for (DataSnapshot parkingSnapshot : locationSnapshot.getChildren()) { // Iterate parkings
                                if (parkingSnapshot.getKey().equals(nameee)) { // Find "VR MALL"
                                    String parkingTimeStr = parkingSnapshot.child("stime").getValue(String.class);
                                    String EparkingTimeStr = parkingSnapshot.child("etime").getValue(String.class);
                                    if (parkingTimeStr != null && EparkingTimeStr != null) {
                                        Log.d("FirebaseData", "Start Time: " + parkingTimeStr);
                                        SparkingStartHour = Integer.parseInt(parkingTimeStr.split(":")[0]); // Extract hour
                                        EparkingStartHour = Integer.parseInt(EparkingTimeStr.split(":")[0]); // Extract hour
                                        setInitialTime();
                                    } else {
                                        Toast.makeText(getActivity(), "Start time not found", Toast.LENGTH_SHORT).show();
                                    }
                                    return; // Exit loop after finding data
                                }
                            }
                        }
                    }
                    Toast.makeText(getActivity(), "Parking not found in database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "No parking data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setInitialTime() {
        int currentHour = getCurrentIndiaTime();

        // If parking time is greater than current time, use parking time; otherwise, use current time
        int initialHour = Math.max(SparkingStartHour, currentHour);
        StimePicker.setHour(initialHour);
        StimePicker.setMinute(0);
    }
    private int getCurrentIndiaTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    private void validateAndSetTime(int hourOfDay) {
//        int selectedHour = StimePicker.getHour(); // User-selected hour
        int selectedHour = hourOfDay;
        int currentHour = getCurrentIndiaTime(); // Get current time in India
        int validStartTime = Math.max(currentHour, SparkingStartHour); // Earliest valid start time
        int latestValidTime = EparkingStartHour - 1; // One hour before parking end time

        // Ensure selected time is within valid range
        if (selectedHour < validStartTime) {
            StimePicker.setHour(validStartTime);
            StimePicker.setMinute(0);
            Toast.makeText(getActivity(), "Start time adjusted to " + validStartTime + ":00", Toast.LENGTH_SHORT).show();
            return;  // Exit the method after setting the valid time
        }

        if (selectedHour >= EparkingStartHour) {
            StimePicker.setHour(latestValidTime);
            StimePicker.setMinute(0);
            Toast.makeText(getActivity(), "Start time cannot be after " + latestValidTime + ":00", Toast.LENGTH_SHORT).show();
            return;  // Exit the method after setting the valid time
        }

    }
    private void disableMinuteSelection() {
        // Try to find minute selection and disable it
        int minuteId = getResources().getIdentifier("minute", "id", "android");
        View minuteView = StimePicker.findViewById(minuteId);
        if (minuteView != null) {
            minuteView.setVisibility(View.GONE); // Hide minute spinner
        }
    }

    private void EgetParkingTime() {
        DatabaseReference EparkingRef = FirebaseDatabase.getInstance().getReference("Parkings");

        EparkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ownerSnapshot : snapshot.getChildren()) { // Iterate owner IDs
                        for (DataSnapshot locationSnapshot : ownerSnapshot.getChildren()) { // Iterate locations
                            for (DataSnapshot parkingSnapshot : locationSnapshot.getChildren()) { // Iterate parkings
                                if (parkingSnapshot.getKey().equals(nameee)) { // Find "VR MALL"
                                    String parkingTimeStr = parkingSnapshot.child("stime").getValue(String.class);
                                    String EparkingTimeStr = parkingSnapshot.child("etime").getValue(String.class);
                                    if (parkingTimeStr != null && EparkingTimeStr != null) {
                                        Log.d("FirebaseData", "Start Time: " + parkingTimeStr);
                                        SparkingStartHour = Integer.parseInt(parkingTimeStr.split(":")[0]); // Extract hour
                                        EparkingStartHour = Integer.parseInt(EparkingTimeStr.split(":")[0]); // Extract hour
                                        EsetInitialTime();
                                        Log.d("FirebaseData", "End Time: " + parkingTimeStr);
                                    } else {
                                        Toast.makeText(getActivity(), "End time not found", Toast.LENGTH_SHORT).show();
                                    }
                                    return; // Exit loop after finding "VR MALL"
                                }
                            }
                        }
                    }
                    Toast.makeText(getActivity(), "Parking not found in database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "No parking data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void EsetInitialTime() {
        int currentHour = EgetCurrentIndiaTime();

        // If parking time is greater than current time, use parking time; otherwise, use current time
        int initialHour = Math.min(EparkingStartHour, currentHour+1);
        EtimePicker.setHour(initialHour);
        EtimePicker.setMinute(0);
    }
    private int EgetCurrentIndiaTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    private void EvalidateAndSetTime(int hour) {
        int selectedHour = hour;
        int startHour = StimePicker.getHour();
        int currentHour = EgetCurrentIndiaTime(); // Get current time in India

        // Ensure end time is at least 1 hour after start time
        if (selectedHour <= startHour) {
            EtimePicker.setHour(startHour + 1);
            EtimePicker.setMinute(0);
            Toast.makeText(getActivity(), "End time must be after start time. Adjusted to " + (startHour + 1) + ":00", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure selected time is between the current time and parking end time
        int validMinTime = Math.max(currentHour, startHour + 1); // Either current time or start + 1 hour

        if (selectedHour < validMinTime) {
            EtimePicker.setHour(validMinTime);
            EtimePicker.setMinute(0);
            Toast.makeText(getActivity(), "End time cannot be before " + validMinTime + ":00. Adjusted.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure selected time is not after the parking's closing time
        if (selectedHour > EparkingStartHour) {
            EtimePicker.setHour(EparkingStartHour);
            EtimePicker.setMinute(0);
            Toast.makeText(getActivity(), "End time cannot exceed parking availability. Set to " + EparkingStartHour + ":00", Toast.LENGTH_SHORT).show();
            return;
        }

//Toast.makeText(getActivity(), "End Time set: " + selectedHour + ":00", Toast.LENGTH_SHORT).show();
    }
    private void EdisableMinuteSelection() {
        // Try to find minute selection and disable it
        int minuteId = getResources().getIdentifier("minute", "id", "android");
        View minuteView = EtimePicker.findViewById(minuteId);
        if (minuteView != null) {
            minuteView.setVisibility(View.GONE); // Hide minute spinner
        }
    }

    private void Visible() {
        if (nameee != null) {
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
                                        if (parkingSnapshot.getKey().equals(nameee)) { // Find "Shiv Plaza"
                                            Parking parking = parkingSnapshot.getValue(Parking.class);
                                            if (parking != null) {

                                                // Price Handling
                                                bike.setVisibility(parking.getPrice2().equals("0") ? View.GONE : View.VISIBLE);
                                                bikeSlotsGrid.setVisibility(parking.getPrice2().equals("0") ? View.GONE : View.VISIBLE);

                                                car.setVisibility(parking.getPrice4().equals("0") ? View.GONE : View.VISIBLE);
                                                carSlotsGrid.setVisibility(parking.getPrice4().equals("0") ? View.GONE : View.VISIBLE);
                                            }
                                            return; // Exit loop once found
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "No data available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to read value", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void loadSlots() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                    String slotId = slotSnapshot.getKey();
                    String status = slotSnapshot.getValue(String.class);

                    boolean isBikeSlot = slotId.startsWith("B");
                    GridLayout parentGrid = isBikeSlot ? bikeSlotsGrid : carSlotsGrid;

                    LinearLayout slotLayout = new LinearLayout(getActivity());
                    slotLayout.setOrientation(LinearLayout.VERTICAL);
                    slotLayout.setGravity(Gravity.CENTER);
                    slotLayout.setPadding(10, 10, 10, 10);

                    ImageView slotImage = new ImageView(getActivity());
                    slotImage.setLayoutParams(new ViewGroup.LayoutParams(150, 100));

                    Button slotButton = new Button(getActivity());
                    slotButton.setText(slotId);
                    slotButton.setTextSize(14);
                    slotButton.setPadding(10, 10, 10, 10);

                    if (status.equals("free")) {
                        slotButton.setBackgroundColor(Color.GREEN);
                        slotButton.setOnClickListener(v -> handleSlotSelection(slotButton, slotId));
                        slotLayout.addView(slotButton);
                    } else {
                        int selectedStartTime = StimePicker.getHour();
                        int selectedEndTime = EtimePicker.getHour();
                        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference("Booking").child(currentDate).child(nameee).child(slotId);
                        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                if (snapshot1.exists()) {
                                    {
                                        int bookedStart = Integer.parseInt(snapshot1.child("stime").getValue(String.class));
                                        int bookedEnd = Integer.parseInt(snapshot1.child("etime").getValue(String.class));

                                        if(( selectedStartTime > bookedStart && selectedStartTime < bookedEnd ) || ( selectedEndTime > bookedStart && selectedEndTime < bookedEnd ) || ( selectedStartTime <= bookedStart && selectedEndTime >= bookedEnd )) {
                                            slotImage.setImageResource(isBikeSlot ? R.drawable.bikee : R.drawable.carr);
                                            slotLayout.addView(slotImage);
                                        } else {
                                            slotButton.setBackgroundColor(Color.GREEN);
                                            slotButton.setOnClickListener(v -> handleSlotSelection(slotButton, slotId));
                                            slotLayout.addView(slotButton);
                                        }

                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Failed to retrieve bookings", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                    param.setMargins(8, 8, 8, 8);
                    param.width = 200;
                    param.height = 150;
                    slotLayout.setLayoutParams(param);

                    parentGrid.addView(slotLayout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load slots", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleSlotSelection(Button slotButton, String slotId) {
        if (selectedSlot.equals(slotId)) {
            // If the selected slot is tapped again, unselect it
            slotButton.setBackgroundColor(Color.GREEN); // Reset to available color
            selectedSlot = "";  // Clear selected slot
            selectedView = null;

            // Hide Pay Now button and Payment Section
            selectedSlotText.setText("Selected Slot: None");
            paymentLayout.setVisibility(View.GONE);
            payNowButton.setVisibility(View.GONE);
        } else {
            // If selecting a new slot
            if (selectedView != null) {
                selectedView.setBackgroundColor(Color.GREEN); // Reset previous selection
            }

            selectedSlot = slotId;  // Store selected slot ID
            selectedView = slotButton;
            slotButton.setBackgroundColor(Color.YELLOW); // Highlight selected slot

            // Show selected slot in TextView
            selectedSlotText.setText("Selected Slot: " + selectedSlot);
            selectedSlotText.setVisibility(View.VISIBLE);

            // Show Payment Section
            paymentLayout.setVisibility(View.VISIBLE);
            payNowButton.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Select Parking Slot");

    }
}