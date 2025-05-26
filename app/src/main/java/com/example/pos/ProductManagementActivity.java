package com.example.pos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.db.ProductDao;
import com.example.pos.db.StockLogDao;
import com.example.pos.model.Product;
import com.example.pos.model.StockLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductManagementActivity extends AppCompatActivity {
    private RecyclerView rvProducts;
    private Button btnAddProduct;
    private ProductDao productDao;
    private StockLogDao stockLogDao;
    private List<Product> products = new ArrayList<>();
    private ProductAdapter adapter;
    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri selectedImageUri = null;
    private int currentUserId = 1; // TODO: Set this dynamically

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        // Restrict access to admin only (simple check, improve as needed)
        String userType = getIntent().getStringExtra("user_type");
        if (userType == null || !userType.equals("admin")) {
            Toast.makeText(this, "Access denied. Admins only.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        rvProducts = findViewById(R.id.rvProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        productDao = new ProductDao(this);
        stockLogDao = new StockLogDao(this);

        products = productDao.getAllProducts();
        adapter = new ProductAdapter(products, this::editProduct, this::deleteProduct);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
        checkLowStockAlert();
    }

    private void showAddProductDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_product, null);
        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etBarcode = dialogView.findViewById(R.id.etProductBarcode);
        EditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        EditText etSupplier = dialogView.findViewById(R.id.etProductSupplier);
        EditText etCost = dialogView.findViewById(R.id.etProductCost);
        EditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        EditText etStock = dialogView.findViewById(R.id.etProductStock);
        ImageView ivImage = dialogView.findViewById(R.id.ivProductImage);
        Button btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        selectedImageUri = null;
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        new AlertDialog.Builder(this)
            .setTitle("Add Product")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String barcode = etBarcode.getText().toString().trim();
                String category = etCategory.getText().toString().trim();
                String supplier = etSupplier.getText().toString().trim();
                double cost = Double.parseDouble(etCost.getText().toString().trim());
                double price = Double.parseDouble(etPrice.getText().toString().trim());
                int stock = Integer.parseInt(etStock.getText().toString().trim());
                Product p = new Product(0, name, barcode, price);
                p.setCategory(category);
                p.setSupplier(supplier);
                p.setCost(cost);
                p.setStock(stock);
                p.setImageUri(selectedImageUri != null ? selectedImageUri.toString() : null);
                productDao.addProduct(p);
                logStockChange(p.getId(), stock, "add");
                refreshProducts();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void editProduct(Product product) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_product, null);
        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etBarcode = dialogView.findViewById(R.id.etProductBarcode);
        EditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        EditText etSupplier = dialogView.findViewById(R.id.etProductSupplier);
        EditText etCost = dialogView.findViewById(R.id.etProductCost);
        EditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        EditText etStock = dialogView.findViewById(R.id.etProductStock);
        ImageView ivImage = dialogView.findViewById(R.id.ivProductImage);
        Button btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        etName.setText(product.getName());
        etBarcode.setText(product.getBarcode());
        etCategory.setText(product.getCategory());
        etSupplier.setText(product.getSupplier());
        etCost.setText(String.valueOf(product.getCost()));
        etPrice.setText(String.valueOf(product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));
        if (product.getImageUri() != null) ivImage.setImageURI(Uri.parse(product.getImageUri()));
        selectedImageUri = product.getImageUri() != null ? Uri.parse(product.getImageUri()) : null;
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        new AlertDialog.Builder(this)
            .setTitle("Edit Product")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                product.setName(etName.getText().toString().trim());
                product.setBarcode(etBarcode.getText().toString().trim());
                product.setCategory(etCategory.getText().toString().trim());
                product.setSupplier(etSupplier.getText().toString().trim());
                product.setCost(Double.parseDouble(etCost.getText().toString().trim()));
                product.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
                int oldStock = product.getStock();
                int newStock = Integer.parseInt(etStock.getText().toString().trim());
                product.setStock(newStock);
                product.setImageUri(selectedImageUri != null ? selectedImageUri.toString() : null);
                productDao.updateProduct(product);
                if (oldStock != newStock) logStockChange(product.getId(), newStock - oldStock, newStock > oldStock ? "in" : "out");
                refreshProducts();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteProduct(Product product) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Delete", (dialog, which) -> {
                productDao.deleteProduct(product.getId());
                refreshProducts();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void refreshProducts() {
        products.clear();
        products.addAll(productDao.getAllProducts());
        adapter.notifyDataSetChanged();
    }

    private void logStockChange(int productId, int quantity, String type) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        StockLog log = new StockLog(0, productId, quantity, currentUserId, timestamp, "", type);
        stockLogDao.addStockLog(log);
    }

    private void checkLowStockAlert() {
        for (Product p : products) {
            if (p.getStock() <= 5) {
                Toast.makeText(this, "Low stock alert: " + p.getName(), Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    // --- Adapter ---
    private static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Product> products;
        private final OnProductEditListener editListener;
        private final OnProductDeleteListener deleteListener;
        interface OnProductEditListener { void onEdit(Product product); }
        interface OnProductDeleteListener { void onDelete(Product product); }
        ProductAdapter(List<Product> products, OnProductEditListener editListener, OnProductDeleteListener deleteListener) {
            this.products = products;
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }
        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ProductViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.tv1.setText(product.getName() + " (Stock: " + product.getStock() + ")");
            holder.tv2.setText("₱" + String.format("%.2f", product.getPrice()) + " | Barcode: " + product.getBarcode());
            // Color indicator
            int stock = product.getStock();
            if (stock > 10) holder.tv1.setTextColor(Color.parseColor("#388E3C")); // Green
            else if (stock > 5) holder.tv1.setTextColor(Color.parseColor("#FBC02D")); // Yellow
            else holder.tv1.setTextColor(Color.parseColor("#D32F2F")); // Red
            holder.itemView.setOnClickListener(v -> editListener.onEdit(product));
            holder.itemView.setOnLongClickListener(v -> { deleteListener.onDelete(product); return true; });
        }
        @Override
        public int getItemCount() { return products.size(); }
        static class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;
            ProductViewHolder(View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
        }
    }
} 