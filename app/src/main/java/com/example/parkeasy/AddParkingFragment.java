package com.example.parkeasy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddParkingFragment extends Fragment {

    EditText email,pass;
    Button add;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_add_parking, container, false);
        email=view.findViewById(R.id.email);
        pass=view.findViewById(R.id.password);
        add=view.findViewById(R.id.add);
        passwordToggle = view.findViewById(R.id.password_toggle);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null){
            String uid=currentUser.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User").child("customer").child(uid);
            reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        DataSnapshot snapshot = task.getResult();
                        if(snapshot.exists()){
                            Check user=snapshot.getValue(Check.class);
                            if (user != null) {
                                String e = user.getEmail() != null ? user.getEmail() : "";
                                String p = user.getPass() != null ? user.getPass() : "";
                                email.setText(e);
                                pass.setText(p);
                            }
                        }
                    }
                }
            });
        }
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUser!=null){
                    String userUid = currentUser.getUid();
                    String e=email.getText().toString();
                    String p=pass.getText().toString();
                    DatabaseReference addReference = FirebaseDatabase.getInstance().getReference("User").child("owner").child(userUid);
                    addReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(getContext(), "You are already eligible", Toast.LENGTH_SHORT).show();
                            } else {
                                // UID does not exist
                                Check check= new Check(e,p,userUid);
                                addReference.setValue(check).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "You are now eligible to log in to our Authority app to add your own parking space", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Please Try Again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        return view;
    }
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24); // Replace with your closed-eye icon
        } else {
            pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.baseline_visibility_24); // Replace with your open-eye icon
        }
        isPasswordVisible = !isPasswordVisible;
        pass.setSelection(pass.getText().length()); // Move cursor to end
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Owner");
    }
}