package com.example.parkeasy;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
public class WalletActivity extends AppCompatActivity implements PaymentResultListener {
    private EditText etAmount;
    private Button btnAddMoney, btnWithdrawMoney;
    TextView balance;
    Toolbar toolbar;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private DatabaseReference transactionRef;
    private EditText etSearchDate;
    private boolean isWithdrawal = false;

    public static double userBalance;
    private DatabaseReference walletRef;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=auth.getCurrentUser();
    String UID= currentUser.getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet);
        View decoreview= getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left=insets.getSystemWindowInsetLeft();
                int top=insets.getSystemWindowInsetTop();
                int right=insets.getSystemWindowInsetRight();
                int bottom=insets.getSystemWindowInsetBottom();
                v.setPadding(left,top,right,bottom);
                return insets.consumeSystemWindowInsets();
            }
        });
        toolbar=findViewById(R.id.toolbar_drawer);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        etAmount = findViewById(R.id.et_amount);
        btnWithdrawMoney = findViewById(R.id.btn_withdraw_money);
        btnAddMoney = findViewById(R.id.btn_add_money);
        balance=findViewById(R.id.balanceTextView);
        fetchWalletBalance();

        Checkout.preload(this); // Move here for early initialization
        btnAddMoney.setOnClickListener(v -> startPayment());
        btnWithdrawMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withdrawMoney();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etSearchDate = findViewById(R.id.et_search_date);

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList);
        recyclerView.setAdapter(adapter);

        loadTransactions();


        // Search by Date
        etSearchDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByDate(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }
    private void startPayment() {
        isWithdrawal = false;
        String amountText = etAmount.getText().toString().trim();
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountText) * 100; // Convert to paisa
            Checkout checkout = new Checkout();
            checkout.setKeyID("rzp_test_0I2MKmxoOaGd7C");

            JSONObject options = new JSONObject();
            options.put("name", "Parking App");
            options.put("description", "Add Money to Wallet");
            options.put("currency", "INR");
            options.put("amount", amount);

            this.runOnUiThread(() -> {
                try {
                    checkout.open(this, options);
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        if (razorpayPaymentID != null && !razorpayPaymentID.isEmpty()) {
            double amount = Double.parseDouble(etAmount.getText().toString());
            if (isWithdrawal) {
                // 🛑 Withdraw money (Deduct from wallet)
                if (amount > userBalance) {
                    Toast.makeText(this, "Insufficient balance!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double updatedBalance = userBalance - amount;
                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallet").child("Customer").child(UID);
                walletRef.child("balance").setValue(updatedBalance).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Withdrawal Successful!", Toast.LENGTH_SHORT).show();
                    addTransactionEntry(amount, "Debit", razorpayPaymentID);
                    etAmount.setText("");  // Clear input
                    fetchWalletBalance();  // Refresh balance
                    loadTransactions();
                    playSuccessSound();
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to withdraw", Toast.LENGTH_SHORT).show());
            }else {
                addBalanceToWallet(UID, amount);  // Update Firebase wallet balance

                DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Transaction").child("Customer").child(UID);
                String transactionID = walletRef.push().getKey();
                // 🔹 Get current date and time in "yyyy-MM-dd HH:mm" format (24-hour format)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String currentDateTime = sdf.format(new Date());
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("amount", String.valueOf(amount));
                transaction.put("type", "Credit");
                transaction.put("timestamp", currentDateTime);
                transaction.put("paymentID",razorpayPaymentID);

                // Store in Firebase
                if (transactionID != null) {
                    walletRef.child(transactionID).setValue(transaction).addOnCompleteListener(task -> {
                        etAmount.setText("");  // Clear EditText after transaction is stored
                        fetchWalletBalance(); // Fetch updated balance immediately
                    });
                }
                loadTransactions();
                playSuccessSound();
                Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(this, "Payment ID is null!", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onPaymentError(int code, String response) {
        String errorMessage = (response != null && !response.isEmpty()) ? response : "Payment Error: No response received!";
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
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
        balance.setText("Wallet Balance: ₹" + userBalance);;
    }
    private void addBalanceToWallet(String userUid, double balance) {
        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("Wallet").child("Customer").child(userUid);
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
                        walletRef.setValue(wallet)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Balance updated successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update balance", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // If the wallet doesn't exist, create a new one
                    Wallet wallet = new Wallet(userUid, balance);
                    walletRef.setValue(wallet)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Balance added successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add balance", Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(this, "Failed to fetch balance", Toast.LENGTH_SHORT).show();
            }
        });
        // Store the balance in the wallet
    }
    private void withdrawMoney() {
        String amountText = etAmount.getText().toString().trim();
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountText);

        if (amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > userBalance) {
            Toast.makeText(this, "Insufficient balance!", Toast.LENGTH_SHORT).show();
            return;
        }

        startRazorpayWithdrawal(amount);
    }
    private void startRazorpayWithdrawal(double amount) {
        isWithdrawal = true;
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_0I2MKmxoOaGd7C");  // Replace with your Razorpay test key

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Parking App");
            options.put("description", "Withdraw Money");
            options.put("currency", "INR");
            options.put("amount", amount * 100);  // Convert to paisa

            this.runOnUiThread(() -> {
                try {
                    checkout.open(this, options);
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
    private void loadTransactions() {
        transactionRef = FirebaseDatabase.getInstance().getReference("Transaction").child("Customer").child(UID);
        transactionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Transaction transaction = dataSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        transactionList.add(transaction);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WalletActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Filter by amount
    private void filterByAmount(String amountText) {
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            if (transaction.getAmount().equalsIgnoreCase(amountText)) {
                filteredList.add(transaction);
            }
        }
        adapter.filterList(filteredList);
    }
    // Filter by date
    private void filterByDate(String dateText) {
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            if (transaction.getTimestamp().contains(dateText)) {
                filteredList.add(transaction);
            }
        }
        adapter.filterList(filteredList);
    }
    private void playSuccessSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(WalletActivity.this, R.raw.success);
        mediaPlayer.start();
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity)this).getSupportActionBar().setTitle("Wallet");
    }
}