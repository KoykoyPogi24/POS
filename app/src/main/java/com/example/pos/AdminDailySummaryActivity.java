package com.example.pos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import android.widget.Toast;
import com.example.pos.db.TransactionDao;
import com.example.pos.db.UserDao;
import com.example.pos.model.User;
import com.example.pos.model.TransactionSummary;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.content.Intent;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminDailySummaryActivity extends AppCompatActivity {
    private TextInputEditText etDate, etCashier;
    private RecyclerView rvSummaryCards, rvTransactions;
    private BarChart barChart;
    private SummaryCardAdapter summaryCardAdapter;
    private TransactionAdapter transactionAdapter;
    private TransactionDao transactionDao;
    private UserDao userDao;
    private List<User> cashierList = new ArrayList<>();
    private List<TransactionSummary> transactionList = new ArrayList<>();
    private List<SummaryCard> summaryCards = new ArrayList<>();
    private String selectedDate;
    private int selectedCashierId = -1;
    private View progressBar;
    private TextView tvEmptyState;
    private MaterialButton btnExportCsv;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_summary);
        
        transactionDao = new TransactionDao(this);
        userDao = new UserDao(this);
        
        initViews();
        setupRecyclerViews();
        setupObservers();
        setupFilters();
        
        executorService.execute(this::loadCashiers);
        setTodayAsDefaultDate();
    }

    private void initViews() {
        etDate = findViewById(R.id.etDate);
        etCashier = findViewById(R.id.etCashier);
        rvSummaryCards = findViewById(R.id.rvSummaryCards);
        rvTransactions = findViewById(R.id.rvTransactions);
        barChart = findViewById(R.id.barChart);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        
        btnExportCsv.setOnClickListener(v -> executorService.execute(this::exportTransactionsToCsv));
    }

    private void setupRecyclerViews() {
        summaryCardAdapter = new SummaryCardAdapter(summaryCards);
        transactionAdapter = new TransactionAdapter(transactionList);
        
        rvSummaryCards.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSummaryCards.setAdapter(summaryCardAdapter);
        
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setHasFixedSize(true);
    }

    private void setupObservers() {
        isLoading.observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            rvSummaryCards.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
            rvTransactions.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
            barChart.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
            etDate.setEnabled(!loading);
            etCashier.setEnabled(!loading);
        });
    }

    private void setupFilters() {
        etDate.setOnClickListener(v -> showDatePicker());
        etCashier.setOnClickListener(v -> showCashierPicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            selectedDate = sdf.format(cal.getTime());
            etDate.setText(selectedDate);
            loadSummaryAndTransactions();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showCashierPicker() {
        ArrayList<String> names = new ArrayList<>();
        names.add("All Cashiers");
        for (User u : cashierList) names.add(u.getUsername());
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Select Cashier")
            .setItems(names.toArray(new String[0]), (dialog, which) -> {
                if (which == 0) {
                    selectedCashierId = -1;
                    etCashier.setText("All Cashiers");
                } else {
                    selectedCashierId = cashierList.get(which - 1).getId();
                    etCashier.setText(cashierList.get(which - 1).getUsername());
                }
                loadSummaryAndTransactions();
            }).show();
    }

    private void setTodayAsDefaultDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        selectedDate = sdf.format(new Date());
        etDate.setText(selectedDate);
        loadSummaryAndTransactions();
    }

    private void loadCashiers() {
        try {
            List<User> users = userDao.getAllCashiers();
            mainHandler.post(() -> {
                cashierList.clear();
                cashierList.addAll(users);
                etCashier.setText("All Cashiers");
                selectedCashierId = -1;
            });
        } catch (Exception e) {
            mainHandler.post(() -> 
                Toast.makeText(this, "Error loading cashiers: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void loadSummaryAndTransactions() {
        isLoading.postValue(true);
        executorService.execute(() -> {
            try {
                // Load data in parallel
                double totalSales = (selectedCashierId == -1)
                    ? transactionDao.getTotalSalesByDate(selectedDate)
                    : transactionDao.getTotalSalesByDateAndCashier(selectedDate, selectedCashierId);

                List<TransactionSummary> transactions = (selectedCashierId == -1)
                    ? transactionDao.getTransactionsSummaryByDate(selectedDate)
                    : transactionDao.getTransactionsSummaryByDateAndCashier(selectedDate, selectedCashierId);

                String bestSeller = (selectedCashierId == -1)
                    ? transactionDao.getBestSellerByDate(selectedDate)
                    : transactionDao.getBestSellerByDateAndCashier(selectedDate, selectedCashierId);

                double[] hourlySales = (selectedCashierId == -1)
                    ? transactionDao.getHourlySalesByDate(selectedDate)
                    : transactionDao.getHourlySalesByDateAndCashier(selectedDate, selectedCashierId);

                // Update UI on main thread
                mainHandler.post(() -> {
                    updateSummaryCards(totalSales, transactions.size(), bestSeller);
                    updateTransactionList(transactions);
                    updateBarChart(hourlySales);
                    isLoading.setValue(false);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    isLoading.setValue(false);
                });
            }
        });
    }

    private void updateSummaryCards(double totalSales, int transactionCount, String bestSeller) {
        summaryCards.clear();
        summaryCards.add(new SummaryCard("Total Sales", String.format("₱%.2f", totalSales), R.drawable.ic_baseline_payments_24));
        summaryCards.add(new SummaryCard("Transactions", String.valueOf(transactionCount), R.drawable.ic_baseline_receipt_24));
        summaryCards.add(new SummaryCard("Best Seller", bestSeller, R.drawable.ic_baseline_star_24));
        summaryCardAdapter.notifyDataSetChanged();
    }

    private void updateTransactionList(List<TransactionSummary> transactions) {
        transactionList.clear();
        transactionList.addAll(transactions);
        transactionAdapter.notifyDataSetChanged();
        
        if (transactions.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void updateBarChart(double[] hourlySales) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            entries.add(new BarEntry(i, (float) hourlySales[i]));
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Sales by Hour");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setValueTextSize(10f);
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%02d:00", (int) value);
            }
        });
        
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.animateY(500);
        barChart.invalidate();
    }

    private void exportTransactionsToCsv() {
        if (transactionList.isEmpty()) {
            mainHandler.post(() ->
                Toast.makeText(this, "No transactions to export.", Toast.LENGTH_SHORT).show()
            );
            return;
        }
        
        try {
            File csvFile = new File(getExternalFilesDir(null), 
                "BizCraft_Transactions_" + selectedDate + "_" + System.currentTimeMillis() + ".csv");
            
            try (FileWriter writer = new FileWriter(csvFile)) {
                writer.append("ID,Date,Amount,Cashier\n");
                for (TransactionSummary tx : transactionList) {
                    writer.append(String.format("%d,%s,%.2f,%s\n",
                        tx.getId(), tx.getDate(), tx.getTotal(), tx.getCashierName()));
                }
            }
            
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", csvFile);
            Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/csv")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_SUBJECT, "BizCraft Transactions - " + selectedDate)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            mainHandler.post(() ->
                startActivity(Intent.createChooser(intent, "Share CSV via"))
            );
        } catch (Exception e) {
            mainHandler.post(() ->
                Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 