package com.example.parkeasy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
public class TicketFragment extends Fragment {
    private String StringFinalPrice,StringPricePerHour,StringAdminCharge,parkingNameee, userStartTime, userEndTime, duration, parkingLocation, finalPrice, selectedSlot, bookingDate, vehicleNumber, totalAmount, endTime, startTime, bookingId, vehicleType, adminFee, twoWheelerPrice, fourWheelerPrice;
    private static String pp;
   private String intermediateKey;
    private TextView wallet,locationText, subTotalText, parkingNameText, finalPriceText, selectedSlotText, adminFeeText, bookingDateText, vehicleTypeText, vehicleRegText, startTimeText, endTimeText, durationText, slotPriceText;
    private Button payNowButton;
    private EditText vehicleNumberInput;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=auth.getCurrentUser();
    String UID= currentUser.getUid();
    private double adminfeefor,ownerfeefor;
    private int pricePerHour, adminCharge, totalCost, startHour, endHour, hoursBooked;
    private DatabaseReference walletRef;
    public static double userBalance;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket, container, false);

        if (getArguments() != null) {
            parkingNameee = getArguments().getString("name");
            bookingDate = getArguments().getString("date");
            selectedSlot = getArguments().getString("slot");
            userStartTime = getArguments().getString("ust");
            userEndTime = getArguments().getString("uet");
        }

        // Initialize UI components
        parkingNameText = view.findViewById(R.id.parkName);
        locationText = view.findViewById(R.id.neartext);
        wallet=view.findViewById(R.id.wallet);
        bookingDateText = view.findViewById(R.id.date);
        selectedSlotText = view.findViewById(R.id.slot);
        vehicleTypeText = view.findViewById(R.id.type);
        startTimeText = view.findViewById(R.id.st);
        endTimeText = view.findViewById(R.id.et);
        durationText = view.findViewById(R.id.duration);
        slotPriceText = view.findViewById(R.id.slotPrice);
        subTotalText = view.findViewById(R.id.total);
        adminFeeText = view.findViewById(R.id.admin);
        vehicleNumberInput = view.findViewById(R.id.vehicle);
        finalPriceText = view.findViewById(R.id.finalPrice);
        payNowButton = view.findViewById(R.id.btnBookNow);

        // Get parking prices from Firebase and update UI after data retrieval
        getParkingPrices(new ParkingDataCallback() {
            @Override
            public void onDataRetrieved(String price2, String price4, String location) {
                twoWheelerPrice = price2;
                fourWheelerPrice = price4;
                parkingLocation = location;

                updateUI();
            }
        });
        fetchWalletBalance();
        updateWalletBalanceDisplay();
        payNowButton.setOnClickListener(v -> {
            showDeductMoneyDialog(totalCost);
        });

        return view;
    }
    private void showDeductMoneyDialog(int amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Deduction");
        builder.setMessage("Are you sure you want to deduct ₹" + amount + " from your wallet?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vehicleNumber = vehicleNumberInput.getText().toString();
                if (vehicleNumber.isEmpty()) {
                    Toast.makeText(getContext(), "Vehicle number is required", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference BReference = FirebaseDatabase.getInstance().getReference("Booking");
                    DatabaseReference Reference = FirebaseDatabase.getInstance().getReference("History");
                    DatabaseReference SReference = FirebaseDatabase.getInstance().getReference("Slot");
                    String amountText = totalAmount;
                    double amount = Double.parseDouble(amountText);
                    if (amount > userBalance) {
                        Toast.makeText(getContext(), "Insufficient balance!", Toast.LENGTH_SHORT).show();
                    }else{
                        double updatedBalance = userBalance - amount;
                        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallet").child("Customer").child(UID);
                        addBalanceToAdminWallet("Nevil",adminfeefor);
                        addBalanceToOwnerWallet(ownerfeefor);
                        addBalanceToWalletOwner(ownerfeefor,"Debit","Slot Booking "+selectedSlot);
                        addTransactionEntryAdmin(adminfeefor,"Debit","Slot Booking "+selectedSlot);
                        walletRef.child("balance").setValue(updatedBalance).addOnSuccessListener(aVoid -> {

                            addTransactionEntry(amount, "Debit", "Slot Booking"+selectedSlot);// Clear input
                            fetchWalletBalance();  // Refresh balance
                        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to withdraw", Toast.LENGTH_SHORT).show());
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = auth.getCurrentUser();
                        String randomKey = UUID.randomUUID().toString();
                        String no=vehicleNumberInput.getText().toString();
                        String userUid = currentUser.getUid();
                        Ticket ticket=new Ticket(bookingDate,duration,userEndTime,randomKey,parkingNameee,parkingLocation,selectedSlot,userStartTime,totalAmount,no,vehicleType);
                        SReference.child(parkingNameee).child(selectedSlot).setValue("booked");
                        BReference.child(bookingDate).child(parkingNameee).child(selectedSlot).setValue(ticket);
                        Reference.child(userUid).child(parkingNameee).child(randomKey).setValue(ticket).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                //
                            }else{
                                Toast.makeText(getContext(), "no maja avi", Toast.LENGTH_SHORT).show();
                            }
                        });

                        Intent intent=new Intent(getContext(),MainActivity2.class);
                        intent.putExtra("token","token");
                        intent.putExtra("date",bookingDate);
                        intent.putExtra("name",parkingNameee);
                        intent.putExtra("slot",selectedSlot);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                    //Toast.makeText(getContext(), "Processing Payment", Toast.LENGTH_SHORT).show();
                }
//                deductMoney(amount);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
//    private void deductMoney(int amount) {
//        // Fetch current wallet balance (Assume you store it in shared preferences or Firebase)
//        int currentBalance = getCurrentWalletBalance();
//
//        if (currentBalance >= amount) {
//            int newBalance = currentBalance - amount;
//            updateWalletBalance(newBalance);
//            Toast.makeText(getActivity(), "₹" + amount + " deducted successfully!", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getActivity(), "Insufficient balance!", Toast.LENGTH_SHORT).show();
//        }
//    }




    private void getParkingPrices(ParkingDataCallback callback) {
        DatabaseReference parkingRef = FirebaseDatabase.getInstance().getReference("Parkings");

        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getActivity(), "No parking data available", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean found = false;
                String price2 = "0", price4 = "0", location = "Unknown Location";

                for (DataSnapshot ownerSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot locationSnapshot : ownerSnapshot.getChildren()) {
                        for (DataSnapshot parkingSnapshot : locationSnapshot.getChildren()) {
                            if (parkingSnapshot.getKey().equals(parkingNameee)) {
                                price2 = parkingSnapshot.child("price2").getValue(String.class);
                                price4 = parkingSnapshot.child("price4").getValue(String.class);
                                location = parkingSnapshot.child("near").getValue(String.class);

                                if (price2 == null) price2 = "0";
                                if (price4 == null) price4 = "0";
                                if (location == null) location = "Unknown Location";

                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                    if (found) break;
                }

                if (!found) {
                    Toast.makeText(getActivity(), "Parking not found", Toast.LENGTH_SHORT).show();
                }

                // Pass the retrieved data to the callback function
                callback.onDataRetrieved(price2, price4, location);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface ParkingDataCallback {
        void onDataRetrieved(String twoWheelerPrice, String fourWheelerPrice, String parkingLocation);
    }
    private void updateUI() {
        // Determine vehicle type and final price
        if (selectedSlot.startsWith("B")) {
            adminFee = "10";
            vehicleType = "2 Wheeler";
            finalPrice = twoWheelerPrice;
        } else {
            adminFee = "20";
            vehicleType = "4 Wheeler";
            finalPrice = fourWheelerPrice;
        }

        // Handle null or empty values
        if (finalPrice != null && !finalPrice.isEmpty()) {
            try {
                pricePerHour = Integer.parseInt(finalPrice);
            } catch (NumberFormatException e) {
                Log.e("ParseError", "Invalid number format for finalPrice: " + finalPrice, e);
                pricePerHour = 0;
            }
        } else {
            Log.e("ParseError", "finalPrice is null or empty");
            pricePerHour = 0;
        }

        adminCharge = Integer.parseInt(adminFee);
        startHour = Integer.parseInt(userStartTime);
        endHour = Integer.parseInt(userEndTime);
        hoursBooked = endHour - startHour;
        duration = String.valueOf(hoursBooked);
        pricePerHour *= hoursBooked;

        totalCost = pricePerHour + adminCharge;
        adminfeefor=Double.parseDouble(adminFee);
        ownerfeefor=Double.parseDouble(String.valueOf(pricePerHour));
        StringFinalPrice = String.valueOf(finalPrice);
        StringPricePerHour = String.valueOf(pricePerHour);
        StringAdminCharge = String.valueOf(adminCharge);
        totalAmount = String.valueOf(totalCost);

        // Update UI
        parkingNameText.setText(parkingNameee);
        locationText.setText(parkingLocation);
        bookingDateText.setText(bookingDate);
        selectedSlotText.setText(selectedSlot);
        vehicleTypeText.setText(vehicleType);
        startTimeText.setText(userStartTime+".00");
        endTimeText.setText(userEndTime+".00");
        durationText.setText(duration+".00");
        slotPriceText.setText("₹ " + StringFinalPrice +".00 / Hr");
        subTotalText.setText("₹ " + StringPricePerHour+".00");
        adminFeeText.setText("₹ " + StringAdminCharge+".00");
        finalPriceText.setText("₹ " + totalAmount+".00");


    }
    private void fetchWalletBalance() {
        if (currentUser != null) {
            String userUid = currentUser.getUid();
            walletRef = FirebaseDatabase.getInstance().getReference("Wallet").child("Customer").child(userUid);
            walletRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Wallet wallet = snapshot.getValue(Wallet.class);
                        if (wallet != null) {
                            userBalance = wallet.getBalance();
                        } else {
                            userBalance = 0;
                        }
                    } else {
                        userBalance = 0;
                    }
                    // Update balance after retrieving data
                    updateWalletBalanceDisplay();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Balance", "Failed to retrieve balance: " + error.getMessage());
                }
            });
        }
    }
    private void updateWalletBalanceDisplay() {
        wallet.setText("Wallet Balance: ₹" + userBalance);;
    }
    private void addTransactionEntry(double amount, String type, String paymentID) {
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("Transaction").child("Customer").child(UID);
        String transactionID = transactionRef.push().getKey();
// 🔹 Get current date and time in "yyyy-MM-dd HH:mm" format (24-hour format)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", String.valueOf(amount));
        transaction.put("type", type);  // "Credit" for add, "Debit" for withdraw
        transaction.put("timestamp", currentDateTime);
        transaction.put("paymentID", String.valueOf(paymentID));

        if (transactionID != null) {
            transactionRef.child(transactionID).setValue(transaction);
        }
    }
    private void addTransactionEntryAdmin(double amount, String type, String paymentID) {
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("Transaction").child("Admin").child("Nevil");
        String transactionID = transactionRef.push().getKey();
// 🔹 Get current date and time in "yyyy-MM-dd HH:mm" format (24-hour format)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", String.valueOf(amount));
        transaction.put("type", type);  // "Credit" for add, "Debit" for withdraw
        transaction.put("timestamp", currentDateTime);
        transaction.put("paymentID", String.valueOf(paymentID));

        if (transactionID != null) {
            transactionRef.child(transactionID).setValue(transaction);
        }
    }
    private void addTransactionEntryOwner(double amount, String type, String paymentID,String uidd) {
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("Transaction").child("Owner").child(uidd);
        String transactionID = transactionRef.push().getKey();
// 🔹 Get current date and time in "yyyy-MM-dd HH:mm" format (24-hour format)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", String.valueOf(amount));
        transaction.put("type", type);  // "Credit" for add, "Debit" for withdraw
        transaction.put("timestamp", currentDateTime);
        transaction.put("paymentID", String.valueOf(paymentID));

        if (transactionID != null) {
            transactionRef.child(transactionID).setValue(transaction);
        }
    }
    private void addBalanceToAdminWallet(String userUid, double balance) {
        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallet").child("Admin").child(userUid);
        walletRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    // Get the existing wallet and balance
                    Wallet wallet = snapshot.getValue(Wallet.class);
                    if (wallet != null) {
                        double existingBalance = wallet.getBalance(); // Assuming getBalance() method exists in the Wallet class
                        double updatedBalance = existingBalance + balance;

                        // Update the wallet with the new balance
                        wallet.setBalance(updatedBalance);
                        walletRef.setValue(wallet);
                                 }
                } else {
                    // If the wallet doesn't exist, create a new one
                    Wallet wallet = new Wallet(userUid, balance);
                    walletRef.setValue(wallet);
                                       }
            } else {
                Toast.makeText(getContext(), "Failed to fetch balance", Toast.LENGTH_SHORT).show();
            }
        });
        // Store the balance in the wallet
    }
    private void addBalanceToOwnerWallet(double balance) {
        DatabaseReference parkingsRef = FirebaseDatabase.getInstance().getReference("Parkings");
        String targetLocation = parkingLocation; // Location to search

        parkingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot intermediateSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot locationSnapshot : intermediateSnapshot.getChildren()) {
                        String locationName = locationSnapshot.getKey();
                        if (locationName != null && locationName.equals(targetLocation)) {
                            String intermediateKey = intermediateSnapshot.getKey();
                            Log.d("FirebaseKey", "Intermediate Key: " + intermediateKey);

                            // Now proceed with wallet update inside this block
                            updateOwnerWallet(intermediateKey, balance);
                            return; // Exit loop once found
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error: " + databaseError.getMessage());
            }
        });
    }
    private void addBalanceToWalletOwner(double balance, String type, String paymentID) {
        DatabaseReference parkingsRef = FirebaseDatabase.getInstance().getReference("Parkings");
        String targetLocation = parkingLocation; // Location to search

        parkingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot intermediateSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot locationSnapshot : intermediateSnapshot.getChildren()) {
                        String locationName = locationSnapshot.getKey();
                        if (locationName != null && locationName.equals(targetLocation)) {
                            String intermediateKey = intermediateSnapshot.getKey();
                            Log.d("FirebaseKey", "Intermediate Key: " + intermediateKey);

                            // Now proceed with wallet update inside this block

                            addTransactionEntryOwner(balance,type,paymentID,intermediateKey);
                            return; // Exit loop once found
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error: " + databaseError.getMessage());
            }
        });
    }

    // Separate method to update wallet after getting the key
    private void updateOwnerWallet(String intermediateKey, double balance) {
        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallet").child("Owner").child(intermediateKey);

        walletRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    Wallet wallet = snapshot.getValue(Wallet.class);
                    if (wallet != null) {
                        double existingBalance = wallet.getBalance(); // Assuming getBalance() method exists
                        double updatedBalance = existingBalance + balance;
                        wallet.setBalance(updatedBalance);
                        walletRef.setValue(wallet);
                    }
                } else {
                    // If the wallet doesn't exist, create a new one
                    Wallet wallet = new Wallet(intermediateKey, balance);
                    walletRef.setValue(wallet);
                }
            } else {
                Toast.makeText(getContext(), "Failed to fetch balance", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Booking Confirmation");
    }

}
