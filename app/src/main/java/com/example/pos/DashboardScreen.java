package com.example.pos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;
import android.widget.ImageButton;

public class DashboardScreen extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_screen);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        // Hamburger menu: open drawer (assume you have a menu icon somewhere, e.g. with id menuIcon)
        View menuIcon = findViewById(R.id.menuIcon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.openDrawer(navView);
                }
            });
        }

        // Close button in drawer header
        View header = navView.getHeaderView(0);
        ImageButton btnCloseDrawer = header.findViewById(R.id.btnCloseDrawer);
        btnCloseDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(navView);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    drawerLayout.closeDrawer(navView);
                    return true;
                } else if (id == R.id.nav_inventory) {
                    startActivity(new Intent(DashboardScreen.this, InventoryScreen.class));
                } else if (id == R.id.nav_transactions) {
                    startActivity(new Intent(DashboardScreen.this, TransactionScreen.class));
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(DashboardScreen.this, ReportScreen.class));
                }
                drawerLayout.closeDrawer(navView);
                return true;
            }
        });

        Button btnTopSellingProducts = findViewById(R.id.btnTopSellingProducts);
        Button btnRecentTransactions = findViewById(R.id.btnRecentTransactions);

        btnTopSellingProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardScreen.this, InventoryScreen.class);
                startActivity(intent);
            }
        });

        btnRecentTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardScreen.this, TransactionScreen.class);
                startActivity(intent);
            }
        });
    }
}