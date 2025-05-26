package com.example.pos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.model.Product;
import com.example.pos.model.CartItem;
import com.example.pos.db.ProductDao;
import com.example.pos.db.TransactionDao;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import com.google.android.material.snackbar.Snackbar;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TransactionScreen extends AppCompatActivity {
    private EditText etSearch, etDiscount;
    private ImageButton btnScanBarcode;
    private RecyclerView rvProducts, rvCart;
    private TextView tvTotal, tvTax, tvDiscount, tvGrandTotal;
    private RadioGroup rgPaymentType;
    private Button btnConfirm;

    private List<Product> allProducts = new ArrayList<>();
    private List<CartItem> cartItems = new ArrayList<>();
    private double total = 0, tax = 0, discount = 0, grandTotal = 0;

    private ProductDao productDao;
    private TransactionDao transactionDao;
    private int cashierId = 2; // TODO: Set this dynamically based on logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_screen);

        etSearch = findViewById(R.id.etSearch);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        rvProducts = findViewById(R.id.rvProducts);
        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotal);
        tvTax = findViewById(R.id.tvTax);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        rgPaymentType = findViewById(R.id.rgPaymentType);
        btnConfirm = findViewById(R.id.btnConfirm);
        etDiscount = findViewById(R.id.etDiscount);

        productDao = new ProductDao(this);
        transactionDao = new TransactionDao(this);

        // Fetch products from database
        allProducts.clear();
        allProducts.addAll(productDao.getAllProducts());

        // Setup product list
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        ProductAdapter productAdapter = new ProductAdapter(allProducts, this::addToCart);
        rvProducts.setAdapter(productAdapter);

        // Setup cart list
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        CartAdapter cartAdapter = new CartAdapter(cartItems, this::removeFromCart);
        rvCart.setAdapter(cartAdapter);

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                productAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Payment type default
        rgPaymentType.check(R.id.rbCash);

        // Confirm button
        btnConfirm.setOnClickListener(v -> processTransaction());
    }

    private void addToCart(Product product) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                updateSummary();
                rvCart.getAdapter().notifyDataSetChanged();
                return;
            }
        }
        cartItems.add(new CartItem(product, 1));
        updateSummary();
        rvCart.getAdapter().notifyDataSetChanged();
    }

    private void removeFromCart(CartItem cartItem) {
        cartItems.remove(cartItem);
        updateSummary();
        rvCart.getAdapter().notifyDataSetChanged();
    }

    private void updateSummary() {
        total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }
        tax = total * 0.12; // Example: 12% VAT
        discount = parseDiscount(etDiscount.getText().toString().trim(), total);
        grandTotal = total + tax - discount;
        tvTotal.setText("Total: ₱" + String.format("%.2f", total));
        tvTax.setText("Tax: ₱" + String.format("%.2f", tax));
        tvDiscount.setText("Discount: ₱" + String.format("%.2f", discount));
        tvGrandTotal.setText("Grand Total: ₱" + String.format("%.2f", grandTotal));
    }

    private double parseDiscount(String input, double baseTotal) {
        if (input == null || input.isEmpty()) return 0;
        input = input.replace("₱", "").replace("%", "").trim();
        try {
            if (input.endsWith("%")) {
                double percent = Double.parseDouble(input.replace("%", ""));
                return baseTotal * (percent / 100.0);
            } else {
                return Double.parseDouble(input);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private void processTransaction() {
        int checkedId = rgPaymentType.getCheckedRadioButtonId();
        String paymentType = "Cash";
        if (checkedId == R.id.rbGCash) paymentType = "GCash";
        else if (checkedId == R.id.rbCredit) paymentType = "Credit";
        String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        // Save transaction to database
        long txId = transactionDao.saveTransaction(
            datetime, total, tax, discount, grandTotal, paymentType, cashierId, cartItems
        );
        // Update product stock
        for (CartItem item : cartItems) {
            int newStock = item.getProduct().getStock() - item.getQuantity();
            productDao.updateStock(item.getProduct().getId(), newStock);
        }
        Snackbar.make(btnConfirm, "Transaction processed! Payment: " + paymentType, Snackbar.LENGTH_LONG).show();
        generateAndShareReceipt(txId, datetime, paymentType);
        cartItems.clear();
        updateSummary();
        rvCart.getAdapter().notifyDataSetChanged();
        // Refresh product list
        allProducts.clear();
        allProducts.addAll(productDao.getAllProducts());
        rvProducts.getAdapter().notifyDataSetChanged();
    }

    private void generateAndShareReceipt(long txId, String datetime, String paymentType) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("BizCraft POS\n");
        receipt.append("Transaction ID: ").append(txId).append("\n");
        receipt.append("Date: ").append(datetime).append("\n");
        receipt.append("Payment: ").append(paymentType).append("\n");
        receipt.append("-----------------------------\n");
        for (CartItem item : cartItems) {
            receipt.append(item.getProduct().getName())
                .append(" x").append(item.getQuantity())
                .append("  ₱").append(String.format("%.2f", item.getSubtotal())).append("\n");
        }
        receipt.append("-----------------------------\n");
        receipt.append("Total: ₱").append(String.format("%.2f", total)).append("\n");
        receipt.append("Tax: ₱").append(String.format("%.2f", tax)).append("\n");
        receipt.append("Discount: ₱").append(String.format("%.2f", discount)).append("\n");
        receipt.append("Grand Total: ₱").append(String.format("%.2f", grandTotal)).append("\n");
        // Save to file and share
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "receipt_" + txId + ".txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(receipt.toString().getBytes());
            fos.close();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, receipt.toString());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Receipt"));
        } catch (IOException e) {
            Snackbar.make(btnConfirm, "Failed to generate receipt.", Snackbar.LENGTH_LONG).show();
        }
    }

    // --- Adapters ---
    private static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Product> products;
        private List<Product> filteredProducts;
        private final OnProductClickListener listener;
        interface OnProductClickListener { void onProductClick(Product product); }
        ProductAdapter(List<Product> products, OnProductClickListener listener) {
            this.products = products;
            this.filteredProducts = new ArrayList<>(products);
            this.listener = listener;
        }
        void filter(String query) {
            filteredProducts.clear();
            for (Product p : products) {
                if (p.getName().toLowerCase().contains(query.toLowerCase()) ||
                    p.getBarcode().contains(query)) {
                    filteredProducts.add(p);
                }
            }
            notifyDataSetChanged();
        }
        @Override public ProductViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
            View v = View.inflate(parent.getContext(), android.R.layout.simple_list_item_2, null);
            return new ProductViewHolder(v);
        }
        @Override public void onBindViewHolder(ProductViewHolder holder, int position) {
            Product product = filteredProducts.get(position);
            holder.tv1.setText(product.getName());
            holder.tv2.setText("₱" + String.format("%.2f", product.getPrice()));
            holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
        @Override public int getItemCount() { return filteredProducts.size(); }
        static class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;
            ProductViewHolder(View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    private static class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
        private List<CartItem> cartItems;
        private final OnCartItemRemoveListener listener;
        interface OnCartItemRemoveListener { void onRemove(CartItem item); }
        CartAdapter(List<CartItem> cartItems, OnCartItemRemoveListener listener) {
            this.cartItems = cartItems;
            this.listener = listener;
        }
        @Override public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = View.inflate(parent.getContext(), android.R.layout.simple_list_item_2, null);
            return new CartViewHolder(v);
        }
        @Override public void onBindViewHolder(CartViewHolder holder, int position) {
            CartItem item = cartItems.get(position);
            holder.tv1.setText(item.getProduct().getName() + " x" + item.getQuantity());
            holder.tv2.setText("₱" + String.format("%.2f", item.getSubtotal()));
            holder.itemView.setOnClickListener(v -> listener.onRemove(item));
        }
        @Override public int getItemCount() { return cartItems.size(); }
        static class CartViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;
            CartViewHolder(View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}