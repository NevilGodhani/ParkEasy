package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class SupportFragment extends Fragment {

    private TextView tvSupportEmail;
    private Button btnContactSupport;

    public SupportFragment() {
        // Required empty public constructor
    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_support, container, false);
        // Initialize Contact Us button
        btnContactSupport = view.findViewById(R.id.btnContactSupport);
        tvSupportEmail = view.findViewById(R.id.tvSupportEmail);
        tvSupportEmail.setOnClickListener(v -> openEmail());

        // Set up click listener for Contact Us button
        btnContactSupport.setOnClickListener(v -> openEmail());
        return view;
    }
    private void openEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:parkeasy@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - ParkEasy");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, I need assistance with...");

        // Check if an email app is available
        if (emailIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(emailIntent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Help & Support");
    }
}