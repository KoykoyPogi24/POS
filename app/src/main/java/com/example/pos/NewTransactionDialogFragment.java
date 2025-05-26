package com.example.pos;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class NewTransactionDialogFragment extends DialogFragment {

    public NewTransactionDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_new_transaction, container, false);

        // Close button
        ImageButton btnClose = view.findViewById(R.id.btnCloseDialog);
        btnClose.setOnClickListener(v -> dismiss());

        // Cancel button
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> dismiss());

        // Complete Transaction button
        Button btnComplete = view.findViewById(R.id.btnComplete);
        btnComplete.setOnClickListener(v -> {
            // TODO: Handle transaction completion logic here
            dismiss();
        });

        // TODO: Add logic for product search, add, and payment method selection

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
} 