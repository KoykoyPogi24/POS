package com.example.pos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pos.db.UserDao;
import com.example.pos.model.User;

public class LoginScreen extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private RadioGroup rgUserType;
    private Button btnLogin;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgUserType = findViewById(R.id.rgUserType);
        btnLogin = findViewById(R.id.btnLogin);

        userDao = new UserDao(this);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            int selectedId = rgUserType.getCheckedRadioButtonId();
            if (username.isEmpty() || password.isEmpty() || selectedId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            String userType = ((RadioButton) findViewById(selectedId)).getText().toString().toLowerCase();

            User user = userDao.loginUser(username, password, userType);
            if (user != null) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                // Redirect based on user type
                if ("admin".equals(user.getUserType())) {
                    // TODO: Replace with your Admin dashboard activity
                    // startActivity(new Intent(this, AdminDashboardActivity.class));
                } else {
                    // TODO: Replace with your Cashier dashboard activity
                    // startActivity(new Intent(this, CashierDashboardActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials or user type.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}