package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
       public TextView name,phone,email,gender;
       LinearLayout phoneL,nameL,genderL;
     private   String Namee;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_profile, container, false);
        name=view.findViewById(R.id.tv_name);
        phone=view.findViewById(R.id.tv_phone);
        email=view.findViewById(R.id.tv_email);
        gender=view.findViewById(R.id.tv_gender);
        phoneL=view.findViewById(R.id.phone_linear);
        nameL=view.findViewById(R.id.name_linear);
        genderL=view.findViewById(R.id.gender_Linear);
        retrieveP();
        retrieveF();
        retrieveGender();
        retrieveEmail();
        phoneL.setOnClickListener(v->phoneBottomSheet());
        nameL.setOnClickListener(v -> nameBottomSheet());
        genderL.setOnClickListener(v -> genderBottomSheet());

        return  view;
    }

    private void retrieveP() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("phone");

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phone1 = snapshot.getValue(String.class);
                    phone.setText("+91 "+phone1); // Set retrieved phone number
                } else {
                    phone.setText("Not Available"); // Default text if no phone is found
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching phone: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void phoneBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.phone_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TextView newEmail=sheetView.findViewById(R.id.edit_email);
        Button applyFilterButton = sheetView.findViewById(R.id.btn_save_email);
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newEmail.getText().toString().isEmpty() || newEmail.getText().toString().length() < 10) {
                    Toast.makeText(getActivity(), "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }else {

                    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("phone");
                    // First, check if phone data exists
                    customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                //Toast.makeText(getContext(), "Phone number not found. Adding new one...", Toast.LENGTH_SHORT).show();
                            }

                            // Update or add phone number
                            customerRef.setValue(newEmail.getText().toString().trim())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Phone updated successfully", Toast.LENGTH_SHORT).show();
                                        retrieveP(); // Call retrieve method to update the UI
                                        bottomSheetDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update phone", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            });
        bottomSheetDialog.show();
    }
    private void nameBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetVie = getLayoutInflater().inflate(R.layout.name_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetVie);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TextView newfname = sheetVie.findViewById(R.id.edit_fname);
        TextView newlname =sheetVie.findViewById(R.id.edit_lname);
        Button applyFilterButton = sheetVie.findViewById(R.id.btn_save_name);
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newfname.getText().toString().isEmpty() || newlname.getText().toString().isEmpty() ) {
                    Toast.makeText(getActivity(), "Enter a valid Name", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    DatabaseReference lcustomerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("last");
                    DatabaseReference fcustomerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("first");
                    // First, check if phone data exists
                    fcustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                //Toast.makeText(getContext(), "Phone number not found. Adding new one...", Toast.LENGTH_SHORT).show();
                            }
                            // Update or add phone number
                            fcustomerRef.setValue(newfname.getText().toString().trim());

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    lcustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                //Toast.makeText(getContext(), "Phone number not found. Adding new one...", Toast.LENGTH_SHORT).show();
                            }
                            // Update or add phone number
                            lcustomerRef.setValue(newlname.getText().toString().trim())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Name updated successfully", Toast.LENGTH_SHORT).show();
                                        retrieveF(); // Call retrieve method to update the UI
                                        bottomSheetDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update phone", Toast.LENGTH_SHORT).show());

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        bottomSheetDialog.show();
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
                    name.setText(Namee);
                } else {
                    name.setText("Not Available");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching phone: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void genderBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.gender_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        RadioGroup genderGroup = sheetView.findViewById(R.id.radioGroupGender);
        Button saveButton = sheetView.findViewById(R.id.btn_save_gender);

        saveButton.setOnClickListener(v -> {
            int selectedId = genderGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getActivity(), "Select a gender", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = sheetView.findViewById(selectedId);
            String selectedGender = selectedRadioButton.getText().toString();

            DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("gender");

            customerRef.setValue(selectedGender)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Gender updated successfully", Toast.LENGTH_SHORT).show();
                        retrieveGender();
                        bottomSheetDialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update gender", Toast.LENGTH_SHORT).show());
        });

        bottomSheetDialog.show();
    }
    private void retrieveGender() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("gender");

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String gender1 = snapshot.getValue(String.class);
                    gender.setText(gender1); // Set retrieved gender
                } else {
                    gender.setText("Not Available"); // Default text if no gender is found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching gender: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void retrieveEmail() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid).child("email");

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email1 = snapshot.getValue(String.class);
                    email.setText(email1); // Set retrieved email
                } else {
                    email.setText("Not Available"); // Default text if no email is found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching email: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");
    }

}