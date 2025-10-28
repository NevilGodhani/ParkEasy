package com.example.parkeasy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Set amount text and color based on type
        holder.tvAmount.setText("₹" + transaction.getAmount());

        if ("Credit".equalsIgnoreCase(transaction.getType())) {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }

        holder.tvPaymentID.setText("ID: " + transaction.getPaymentID());
        holder.tvTimestamp.setText("Date: " + transaction.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvPaymentID, tvTimestamp;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPaymentID = itemView.findViewById(R.id.tv_paymentID);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }

    // Method to filter list based on search
    public void filterList(List<Transaction> filteredList) {
        transactionList = filteredList;
        notifyDataSetChanged();
    }
}


