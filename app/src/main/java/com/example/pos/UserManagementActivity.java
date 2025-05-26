package com.example.pos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.db.UserDao;
import com.example.pos.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {
    private RecyclerView rvUsers;
    private Button btnAddUser;
    private UserDao userDao;
    private List<User> users = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        String userType = getIntent().getStringExtra("user_type");
        if (userType == null || !userType.equals("admin")) {
            Toast.makeText(this, "Access denied. Admins only.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        rvUsers = findViewById(R.id.rvUsers);
        btnAddUser = findViewById(R.id.btnAddUser);
        userDao = new UserDao(this);

        users = userDao.getAllUsers();
        adapter = new UserAdapter(users, this::editUser, this::deleteUser);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        btnAddUser.setOnClickListener(v -> showAddUserDialog());
    }

    private void showAddUserDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_user, null);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        RadioGroup rgUserType = dialogView.findViewById(R.id.rgUserType);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Add User")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(d -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(v -> {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Username and password cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int checkedId = rgUserType.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Toast.makeText(this, "Please select a user type.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userType = ((RadioButton) dialogView.findViewById(checkedId)).getText().toString().toLowerCase();
                userDao.registerUser(username, password, userType);
                refreshUsers();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void editUser(User user) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_user, null);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        RadioGroup rgUserType = dialogView.findViewById(R.id.rgUserType);
        etUsername.setText(user.getUsername());
        // Password not shown for security
        if (user.getUserType().equals("admin")) rgUserType.check(R.id.rbAdmin);
        else rgUserType.check(R.id.rbCashier);

        new AlertDialog.Builder(this)
            .setTitle("Edit User")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String newUsername = etUsername.getText().toString().trim();
                String newUserType = ((RadioButton) dialogView.findViewById(rgUserType.getCheckedRadioButtonId())).getText().toString().toLowerCase();
                userDao.updateUser(user.getId(), newUsername, newUserType);
                refreshUsers();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Delete", (dialog, which) -> {
                userDao.deleteUser(user.getId());
                refreshUsers();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void refreshUsers() {
        users.clear();
        users.addAll(userDao.getAllUsers());
        adapter.notifyDataSetChanged();
    }

    // --- Adapter ---
    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;
        private final OnUserEditListener editListener;
        private final OnUserDeleteListener deleteListener;
        interface OnUserEditListener { void onEdit(User user); }
        interface OnUserDeleteListener { void onDelete(User user); }
        UserAdapter(List<User> users, OnUserEditListener editListener, OnUserDeleteListener deleteListener) {
            this.users = users;
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }
        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new UserViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.tv1.setText(user.getUsername() + " (" + user.getUserType() + ")");
            holder.tv2.setText("ID: " + user.getId());
            holder.itemView.setOnClickListener(v -> editListener.onEdit(user));
            holder.itemView.setOnLongClickListener(v -> { deleteListener.onDelete(user); return true; });
        }
        @Override
        public int getItemCount() { return users.size(); }
        static class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;
            UserViewHolder(View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
} 