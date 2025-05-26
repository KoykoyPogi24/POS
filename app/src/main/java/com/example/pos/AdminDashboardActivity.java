package com.example.pos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        Button btnViewTransactions = findViewById(R.id.btnViewTransactions);
        Button btnManageProducts = findViewById(R.id.btnManageProducts);

        btnManageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, UserManagementActivity.class));
            }
        });
        btnViewTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, TransactionListActivity.class));
            }
        });
        btnManageProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, ProductManagementActivity.class));
            }
        });
    }
} 