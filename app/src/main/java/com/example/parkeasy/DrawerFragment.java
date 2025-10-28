package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DrawerFragment extends Fragment {
Context context=getActivity();
     private String Namee;
CardView addParking,edit;
TextView setting,payment,language,booking,fullname,emaildrawer,help;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_drawer, container, false);
        fullname=view.findViewById(R.id.fullname);
        emaildrawer=view.findViewById(R.id.emaildrawer);
        help=view.findViewById(R.id.help);
        retrieveEmail();
        retrieveF();
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(),MainActivity2.class);
                i.putExtra("helpp","helpp");
                startActivity(i);
            }
        });
        edit=view.findViewById(R.id.btn_edit_profile);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getActivity(),MainActivity2.class);
                ii.putExtra("edite","edite");
                startActivity(ii);
            }
        });
        payment=view.findViewById(R.id.payment);
        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(),WalletActivity.class);
                startActivity(i);
            }
        });
        setting=view.findViewById(R.id.logout);
        setting.setOnClickListener(v -> logoutBottomSheet());
        addParking=view.findViewById(R.id.addParking);
        addParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), MainActivity2.class);
                intent.putExtra("add","add");
                startActivity(intent);
            }
        });
        language=view.findViewById(R.id.language);
        language.setOnClickListener(v -> languageBottomSheet());
        booking=view.findViewById(R.id.booking);
        booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),MainActivity2.class);
                intent.putExtra("bookingg","bookingg");
                startActivity(intent);
            }
        });
        return view;
    }
    private void languageBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.language_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);
        Button applyFilterButton = sheetView.findViewById(R.id.btn_save_language);
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Toast.makeText(getContext(), "English is already chosen", Toast.LENGTH_SHORT).show();
            }
        });
        bottomSheetDialog.show();
    }
    private void logoutBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.logout_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);
        Button applyFilterButton = sheetView.findViewById(R.id.btn_save_logout);
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth=FirebaseAuth.getInstance();
                auth.signOut(); // Firebase Logout
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });
        bottomSheetDialog.show();
    }
    private void retrieveEmail() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("email");

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email1 = snapshot.getValue(String.class);
                    emaildrawer.setText(email1); // Set retrieved email
                } else {
                    emaildrawer.setText("Not Available"); // Default text if no email is found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching email: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void retrieveF() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("first");

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phone1 = snapshot.getValue(String.class);
                    String NAm=phone1;
                    retrieveL(NAm);
                } else {
                    // Namee="Not Available";
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching phone: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void retrieveL(String Namee1) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("last");

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phone1 = snapshot.getValue(String.class);
                    Namee=Namee1+" "+phone1;
                    fullname.setText(Namee);
                } else {
                    fullname.setText("Not Available");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching phone: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Menu");
    }
}