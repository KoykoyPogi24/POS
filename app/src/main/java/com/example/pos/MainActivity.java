package com.example.pos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEnterDashboard = findViewById(R.id.btnEnterDashboard);
        Button btnProcessSale = findViewById(R.id.btnProcessSale);
        Button btnViewInventory = findViewById(R.id.btnViewInventory);
        Button btnViewTransactions = findViewById(R.id.btnViewTransactions);
        Button btnViewReports = findViewById(R.id.btnViewReports);

        btnEnterDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DashboardScreen.class);
                startActivity(intent);
            }
        });

        btnProcessSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionScreen.class);
                startActivity(intent);
            }
        });

        btnViewInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InventoryScreen.class);
                startActivity(intent);
            }
        });

        btnViewTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionScreen.class);
                startActivity(intent);
            }
        });

        btnViewReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportScreen.class);
                startActivity(intent);
            }
        });
    }
}