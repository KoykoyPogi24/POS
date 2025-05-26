package com.example.pos;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        BottomNavigationView nav = findViewById(R.id.adminBottomNav);
        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_admin_users:
                        // Already here
                        return true;
                    case R.id.menu_admin_transactions:
                        startActivity(new android.content.Intent(UserManagementActivity.this, TransactionListActivity.class));
                        finish();
                        return true;
                    case R.id.menu_admin_products:
                        startActivity(new android.content.Intent(UserManagementActivity.this, ProductManagementActivity.class));
                        finish();
                        return true;
                }
                return false;
            }
        });
    }
} 