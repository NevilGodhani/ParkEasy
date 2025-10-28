package com.example.parkeasy;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class BookingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private EditText searchDate;
    private List<HistoryItem> home;
    private DatabaseReference historyRef;
    private String uid;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchDate = view.findViewById(R.id.searchDate);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        home = new ArrayList<>();
        historyAdapter = new HistoryAdapter(home, getContext());

        recyclerView.setAdapter(historyAdapter);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        loadHistory();

        searchDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterList(s.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        return view;
    }
    private void loadHistory() {
        historyRef = FirebaseDatabase.getInstance().getReference("History").child(uid);
        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                home.clear();
                for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot historySnapshot : locationSnapshot.getChildren()) {
                        HistoryItem item = historySnapshot.getValue(HistoryItem.class);
                        home.add(item);
                    }
                }

                // Update the adapter's dataset properly
                historyAdapter.updateFullList(home);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void filterList(String query) {
        historyAdapter.filterList(query);  // Directly use adapter's method
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Bookings");
    }
}
