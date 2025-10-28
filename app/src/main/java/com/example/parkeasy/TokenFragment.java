package com.example.parkeasy;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;
public class TokenFragment extends Fragment {
    private TextView locationText, no,parkingNameText, finalPriceText, selectedSlotText, bookingDateText, vehicleRegText, startTimeText, endTimeText, durationText;
    private String nnn,ddd,sss,st,et;
    private ImageView qrCodeImage;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_token, container, false);
        // ✅ Play Success Sound
        playSuccessSound();
        // Automatically create PDF when the fragment loads
        ScrollView scrollView = view.findViewById(R.id.scrollView);
        qrCodeImage = view.findViewById(R.id.qrCodeImage);
        locationText=view.findViewById(R.id.neartexte);
        parkingNameText=view.findViewById(R.id.parkNamee);
        finalPriceText=view.findViewById(R.id.idfinal);
        selectedSlotText=view.findViewById(R.id.slot);
        bookingDateText=view.findViewById(R.id.date);
        vehicleRegText=view.findViewById(R.id.number);
        no=view.findViewById(R.id.type);
        startTimeText=view.findViewById(R.id.st);
        endTimeText=view.findViewById(R.id.et);
        durationText=view.findViewById(R.id.duration);
        if (getArguments() != null) {
            nnn = getArguments().getString("name");
            ddd = getArguments().getString("ddd");
            sss = getArguments().getString("sss");
        }
        DatabaseReference Reference = FirebaseDatabase.getInstance().getReference("Booking").child(ddd).child(nnn).child(sss);

        // Fetch Data from Firebase
         Reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve values from Firebase
                    String date = snapshot.child("date").getValue(String.class);
                    String duration = snapshot.child("duration").getValue(String.class);
                    String etime = snapshot.child("etime").getValue(String.class);
                    String near = snapshot.child("near").getValue(String.class);
                    String parkingName = snapshot.child("parkingName").getValue(String.class);
                    String slot = snapshot.child("slot").getValue(String.class);
                    String stime = snapshot.child("stime").getValue(String.class);
                    String totalPrice = snapshot.child("totalPrice").getValue(String.class);
                    String vehicleNo = snapshot.child("vehicleNo").getValue(String.class);
                    String vehicleType = snapshot.child("vehicleType").getValue(String.class);

                    // Set values to TextViews
                    bookingDateText.setText(date);
                    durationText.setText(duration+".00 Hour");
                    endTimeText.setText(etime+".00");
                    locationText.setText(parkingName);
                    parkingNameText.setText(near);
                    selectedSlotText.setText(slot);
                    startTimeText.setText(stime+".00");
                    finalPriceText.setText("₹ "+totalPrice+".00");
                    vehicleRegText.setText(vehicleNo);
                    no.setText(vehicleType);
                    // ✅ Create Dynamic JSON after data is loaded
                    Map<String, String> parkingData = new HashMap<>();
                    parkingData.put("name", nnn);
                    parkingData.put("date", ddd);
                    parkingData.put("slot", sss);
                    parkingData.put("stime", stime + ".00");
                    parkingData.put("etime", etime + ".00");
                    parkingData.put("duration", duration + ".00 Hour");
                    parkingData.put("total", "₹ " + totalPrice + ".00");
                    parkingData.put("vehivleNO", vehicleNo);
                    parkingData.put("vehivleType", vehicleType);

                    String jsonData = new Gson().toJson(parkingData);

                    // ✅ Generate QR Code with Dynamic Data
                    generateQRCode(jsonData);

                    // ✅ Ensure PDF is created after data is loaded
                    new Handler().postDelayed(() -> createPdf(scrollView), 500);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data", error.toException());
            }
        });


        // Wait for the ScrollView to be fully laid out
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Now the view is fully loaded, generate the PDF
                createPdf(scrollView);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), Mainscreen.class);
                startActivity(intent);
                requireActivity().finish();// Optional: If you want to close the current activity
            }
        }, 5000); // 3000 milliseconds = 3 seconds

        return view;
    }
    private void playSuccessSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.success);
        mediaPlayer.start();
    }
    private void generateQRCode(String text) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 150, 150);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void createPdf(ScrollView scrollView) {
        String directoryPath = getContext().getExternalFilesDir(null) + "/ParkingReceipts";
        File directory = new File(directoryPath);

        // Ensure the directory exists
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                Log.e("PDF Error", "Failed to create directory: " + directoryPath);
                return;
            }
        }

        String filePath = directoryPath + "/BookingReceipt.pdf";
        File file = new File(filePath);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            // Convert ScrollView to Bitmap
            Bitmap bitmap = getBitmapFromView(scrollView);
            if (bitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image image = Image.getInstance(stream.toByteArray());

                float documentWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
                float documentHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
                image.scaleToFit(documentWidth, documentHeight);
                document.add(image);
            } else {
                document.add(new Paragraph("Failed to generate image from ScrollView."));
            }

            document.close();
            fos.close();

            Log.d("PDF", "PDF successfully created: " + filePath);

            // 🔔 Show Notification
            showPdfSavedNotification(filePath);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PDF Error", "Error creating PDF: " + e.getMessage());
        }
    }


    // Convert ScrollView to Bitmap
    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    @SuppressLint({"MissingPermission", "NotificationPermission"})
    private void showPdfSavedNotification(String filePath) {
        Context context = getContext();
        if (context == null) return;

        String channelId = "pdf_channel";
        String channelName = "PDF Notifications";

        // 🔔 Create Notification Channel (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 🎯 Create Intent to Open PDF
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(filePath));
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔥 Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_book_24) // Replace with your app icon
                .setContentTitle("PDF Saved")
                .setContentText("Tap to open BookingReceipt.pdf")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // 🚀 Show Notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar title when the fragment is resumed
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Payment Invoice");
    }

}