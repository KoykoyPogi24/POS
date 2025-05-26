package com.example.pos;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;
import android.widget.Toast;
import com.example.pos.db.TransactionDao;
import com.example.pos.model.TransactionSummary;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CashierDailySummaryActivity extends AppCompatActivity {
    private TextInputEditText etDate;
    private RecyclerView rvSummaryCards, rvTransactions;
    private BarChart barChart;
    private SummaryCardAdapter summaryCardAdapter;
    private TransactionAdapter transactionAdapter;
    private TransactionDao transactionDao;
    private int cashierId;
    private List<TransactionSummary> transactionList = new ArrayList<>();
    private List<SummaryCard> summaryCards = new ArrayList<>();
    private String selectedDate;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_summary);
        transactionDao = new TransactionDao(this);
        cashierId = getIntent().getIntExtra("cashier_id", -1);
        if (cashierId == -1) {
            Toast.makeText(this, "Cashier not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        setupFilters();
        setTodayAsDefaultDate();
        loadSummaryAndTransactions();
    }

    private void initViews() {
        etDate = findViewById(R.id.etDate);
        rvSummaryCards = findViewById(R.id.rvSummaryCards);
        rvTransactions = findViewById(R.id.rvTransactions);
        barChart = findViewById(R.id.barChart);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        summaryCardAdapter = new SummaryCardAdapter(summaryCards);
        rvSummaryCards.setAdapter(summaryCardAdapter);
        transactionAdapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupFilters() {
        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        // Show a date picker dialog and update selectedDate
        Calendar cal = Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            selectedDate = sdf.format(cal.getTime());
            etDate.setText(selectedDate);
            loadSummaryAndTransactions();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setTodayAsDefaultDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        selectedDate = sdf.format(new Date());
        etDate.setText(selectedDate);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvSummaryCards.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        rvTransactions.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        barChart.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        etDate.setEnabled(!loading);
    }

    private void loadSummaryAndTransactions() {
        setLoading(true);
        try {
            // Load summary cards
            summaryCards.clear();
            double totalSales = transactionDao.getTotalSalesByDateAndCashier(selectedDate, cashierId);
            int transactionCount = transactionDao.getTransactionsSummaryByDateAndCashier(selectedDate, cashierId).size();
            String bestSeller = transactionDao.getBestSellerByDateAndCashier(selectedDate, cashierId);
            summaryCards.add(new SummaryCard("Total Sales", String.format("₱%.2f", totalSales), 0));
            summaryCards.add(new SummaryCard("Transactions", String.valueOf(transactionCount), 0));
            summaryCards.add(new SummaryCard("Best Seller", bestSeller, 0));
            summaryCardAdapter.notifyDataSetChanged();

            // Load transactions
            transactionList.clear();
            transactionList.addAll(transactionDao.getTransactionsSummaryByDateAndCashier(selectedDate, cashierId));
            transactionAdapter.notifyDataSetChanged();

            // Empty state
            if (transactionList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvTransactions.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvTransactions.setVisibility(View.VISIBLE);
            }

            // Load bar chart data for sales analytics
            double[] hourlySales = transactionDao.getHourlySalesByDateAndCashier(selectedDate, cashierId);
            updateBarChart(hourlySales);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            setLoading(false);
        }
    }

    private void updateBarChart(double[] hourlySales) {
        List<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            entries.add(new com.github.mikephil.charting.data.BarEntry(i, (float) hourlySales[i]));
        }
        com.github.mikephil.charting.data.BarDataSet dataSet = new com.github.mikephil.charting.data.BarDataSet(entries, "Sales by Hour");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(dataSet);
        barData.setBarWidth(0.9f);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(24);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    // TODO: Implement date filter logic
    // TODO: Load summary cards (total sales, transactions, best-seller for this cashier)
    // TODO: Load bar chart data
    // TODO: Load transaction list
    // TODO: Handle loading, empty, and error states
} 