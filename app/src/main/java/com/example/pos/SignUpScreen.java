package com.example.pos;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pos.db.UserDao;

public class SignUpScreen extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private RadioGroup rgUserType;
    private Button btnRegister;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgUserType = findViewById(R.id.rgUserType);
        btnRegister = findViewById(R.id.btnRegister);

        userDao = new UserDao(this);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            int selectedId = rgUserType.getCheckedRadioButtonId();
            if (username.isEmpty() || password.isEmpty() || selectedId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            String userType = ((RadioButton) findViewById(selectedId)).getText().toString().toLowerCase();

            boolean success = userDao.registerUser(username, password, userType);
            if (success) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Registration failed. Username may already exist.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}