package com.example.parkeasy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment {
    private EditText emailEditText, passwordEditText, confirmPasswordEditText, fname, lname;
    private Button registerButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;
    private boolean CisPasswordVisible = false;
    private ImageView passwordToggle,CpasswordToggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_register, container, false);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password);
        registerButton = view.findViewById(R.id.registerBtn);
        progressBar = view.findViewById(R.id.progressBar);
        fname=view.findViewById(R.id.first_name);
        passwordToggle = view.findViewById(R.id.password_toggle);
        CpasswordToggle = view.findViewById(R.id.Cpassword_toggle);
        lname=view.findViewById(R.id.last_name);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        // Toggle Password Visibility
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
        // Toggle Password Visibility
        CpasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CtogglePasswordVisibility();
            }
        });
        return view;
    }
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String first= fname.getText().toString().trim();
        String last=lname.getText().toString().trim();
        if (TextUtils.isEmpty(first)) {
            Toast.makeText(getContext(), "Enter First Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(last)) {
            Toast.makeText(getContext(), "Enter Last Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "Confirm Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.GONE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        registerButton.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String key=user.getUid();
//                            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Check");
//                            Check check = new Check(email,password);
//                            userReference.child(password).setValue(check);
                            DatabaseReference customer=FirebaseDatabase.getInstance().getReference("Customer").child(key);
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User").child("customer");
                            User user1 = new User(first,last,email,password,key);
                            customer.setValue(user1);
                            reference.child(key).setValue(user1).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(getContext(),Mainscreen.class);
                                    startActivity(intent);
                                    requireActivity().finish();
                                }else {
                                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(getContext(), "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24); // Replace with your closed-eye icon
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.baseline_visibility_24); // Replace with your open-eye icon
        }
        isPasswordVisible = !isPasswordVisible;
        passwordEditText.setSelection(passwordEditText.getText().length()); // Move cursor to end
    }
    private void CtogglePasswordVisibility() {
        if (CisPasswordVisible) {
            confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            CpasswordToggle.setImageResource(R.drawable.baseline_visibility_off_24); // Replace with your closed-eye icon
        } else {
            confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            CpasswordToggle.setImageResource(R.drawable.baseline_visibility_24); // Replace with your open-eye icon
        }
        CisPasswordVisible = !CisPasswordVisible;
        confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length()); // Move cursor to end
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Create Account");

    }
}