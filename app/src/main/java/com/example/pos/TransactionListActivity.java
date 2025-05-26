package com.example.pos;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.db.TransactionDao;
import com.example.pos.model.TransactionSummary;
import java.util.List;
import android.view.ViewGroup;
import android.widget.TextView;

public class TransactionListActivity extends AppCompatActivity {
    private RecyclerView rvTransactions;
    private TransactionDao transactionDao;
    private List<TransactionSummary> transactions;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        String userType = getIntent().getStringExtra("user_type");
        if (userType == null || !userType.equals("admin")) {
            Toast.makeText(this, "Access denied. Admins only.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        rvTransactions = findViewById(R.id.rvTransactions);
        transactionDao = new TransactionDao(this);
        transactions = transactionDao.getAllTransactionsSummary();
        adapter = new TransactionAdapter(transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
        private List<TransactionSummary> transactions;
        TransactionAdapter(List<TransactionSummary> transactions) { this.transactions = transactions; }
        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new TransactionViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            TransactionSummary t = transactions.get(position);
            holder.tv1.setText("Date: " + t.getDate() + " | Cashier: " + t.getCashierName());
            holder.tv2.setText("Total: ₱" + String.format("%.2f", t.getTotal()));
        }
        @Override
        public int getItemCount() { return transactions.size(); }
        static class TransactionViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;
            TransactionViewHolder(android.view.View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
} 