package com.example.test3.Subcategory;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.view.ActionMode;

import com.example.test3.R;
import com.example.test3.databinding.ActivityBottomNavBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

// BottomNavActivity.java
public class BottomNavActivity extends AppCompatActivity {
    private static final int PICK_IMAGES_REQUEST = 101;
    private ActivityBottomNavBinding binding;
    private String mainCategory;
    RecyclerView recyclerView;
    FloatingActionButton fab;
    private DatabaseHelper1 dbHelper;
    private ClothingAdapter adapter;
    private String currentSubCategory = "shirt"; // Default category
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView selectionCountTextView;

    private String categoryId;
    private String categoryName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBottomNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainCategory = getIntent().getStringExtra("CATEGORY_TYPE");
        if (mainCategory ==null){
            mainCategory = "Adults";
        }
        dbHelper = new DatabaseHelper1(this);

        // Retrieve intent extras
//        Intent intent = getIntent();
//        categoryId = intent.getStringExtra("CATEGORY_ID");
//        categoryName = intent.getStringExtra("CATEGORY_NAME");

//        if (categoryId == null || categoryName == null) {
//            Log.e("BottomNavActivity", " or name is null");
//            Toast.makeText(this, "Error: Category data is missing.", Toast.LENGTH_SHORT).show();
//            finish();  // Close activity if data is missing
//            return;
//        }
        setupRecyclerView();
        setupBottomNavigation();
        setupFab();

    }

    private void setupRecyclerView() {
//        Cursor cursor = dbHelper.getClothingByType(getIntent().getStringExtra("type"));
        RecyclerView recyclerView = findViewById(R.id.photos_recycler_view);
        adapter = new ClothingAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        loadClothingItems(currentSubCategory);
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_shirts) {
                item.setChecked(true);
                currentSubCategory = "shirt";
            } else if (id == R.id.nav_trousers) {
                item.setChecked(true);
                currentSubCategory = "trouser";
            } else if (id == R.id.nav_dresses) {
                item.setChecked(true);
                currentSubCategory = "dress";
            }
            loadClothingItems(currentSubCategory);
            return true;
        });
    }
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_photo);
        fab.setOnClickListener(v -> openImagePicker());
    }
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // Enable multiple selection
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            // Handle multiple images
            if (data.getClipData() != null) {
                // Multiple images selected
                ClipData clipData = data.getClipData();
                int totalImages = clipData.getItemCount();

                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Saving images...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMax(totalImages);
                progressDialog.show();
                new Thread(() -> {
                    for (int i = 0; i < totalImages; i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        saveImageToDatabase(imageUri);

                        int finalI = i;
                        runOnUiThread(() -> {
                            progressDialog.setProgress(finalI + 1);
                            if (finalI == totalImages - 1) {
                                progressDialog.dismiss();
                                loadClothingItems(currentSubCategory);
                            }
                        });
                    }

                }).start();
            } else if (data.getData() != null) {
                // Single image selected
                Uri imageUri = data.getData();
                saveImageToDatabase(imageUri);
                loadClothingItems(currentSubCategory);
            }
            // Reload the current category

        }
    }

    @SuppressLint("Range")
    private void loadClothingItems(String subCategory) {
        List<ClothingItem> items = new ArrayList<>();
        Cursor cursor = dbHelper.getClothingByType(subCategory, mainCategory);
        while (cursor.moveToNext()) {
            ClothingItem item = new ClothingItem();
            item.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper1.COLUMN_ID)));
            item.setType(cursor.getString(cursor.getColumnIndex(DatabaseHelper1.COLUMN_TYPE)));
            item.setImageUri(cursor.getString(cursor.getColumnIndex(DatabaseHelper1.COLUMN_IMAGE_URI)));
            item.setHasValidImage(!cursor.isNull(cursor.getColumnIndex(DatabaseHelper1.COLUMN_IMAGE_URI)));
            items.add(item);
        }
        cursor.close();
//        try {
//            // These will throw an exception if columns don't exist
//            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper1.COLUMN_ID);
//            int typeIndex = cursor.getColumnIndexOrThrow(DatabaseHelper1.COLUMN_TYPE);
//            int uriIndex = cursor.getColumnIndexOrThrow(DatabaseHelper1.COLUMN_IMAGE_URI);
//
//            while (cursor.moveToNext()) {
//                ClothingItem item = new ClothingItem();
//                item.setId(cursor.getInt(idIndex));
//                item.setType(cursor.getString(typeIndex));
//                item.setImageUri(cursor.getString(uriIndex));
//                items.add(item);
//            }
//        } catch (IllegalArgumentException e) {
//            // Handle missing columns
//            Log.e("Database", "Column not found in database", e);
//        } finally {
//            cursor.close();
//        }
        adapter.updateData(items);
    }

    private void saveImageToDatabase(Uri imageUri) {
        try {
            // Optional: Make a persistent copy of the image
            String persistentUri = makeImagePersistent(imageUri);
            // Save to database
            dbHelper.insertClothing(currentSubCategory, mainCategory, persistentUri);
        } catch (Exception e) {
            Log.e("BottomNavActivity", "Error saving image", e);
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    // Optional: Make a persistent copy of the image
    private String makeImagePersistent(Uri sourceUri) throws IOException {
        // Create a file in your app's private directory
        File destFile = new File(getFilesDir(), "images/" +
                System.currentTimeMillis() + ".jpg");
        destFile.getParentFile().mkdirs(); // Create directories if they don't exist

        // Copy the image
        try (InputStream in = getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }

        // Return the file URI
        return Uri.fromFile(destFile).toString();
    }


    // Deleting Items
//
//    public void startSelectionMode() {
//        if (actionMode == null) {
//            actionMode = startSupportActionMode(new ActionMode.Callback() {
//                @Override
//                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                    MenuInflater inflater = mode.getMenuInflater();
//                    inflater.inflate(R.menu.selection_menu, menu);
//                    return true;
//                }
//
//                @Override
//                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                    return false;
//                }
//
//                @Override
//                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                    if (item.getItemId() == R.id.action_delete) {
//                        deleteSelectedItems();
//                        return true;
//                    }
//                    return false;
//                }
//
//                @Override
//                public void onDestroyActionMode(ActionMode mode) {
//                    adapter.clearSelection();
//                    actionMode = null;
//                }
//            });
//        }
//    }
//
//    private void deleteSelectedItems() {
//        List<Long> selectedIds = adapter.getSelectedItemIds();
//
//        String message = selectedIds.size() == 1 ?
//                "Are you sure you want to delete this item?" :
//                "Are you sure you want to delete " + selectedIds.size() + " items?";
//
//        new AlertDialog.Builder(this)
//                .setTitle("Delete Selected Items")
//                .setMessage(message)
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    long[] idsArray = new long[selectedIds.size()];
//                    for (int i = 0; i < selectedIds.size(); i++) {
//                        idsArray[i] = selectedIds.get(i);
//                    }
//
//                    int deletedCount = dbHelper.deleteMultipleClothing(idsArray);
//
//                    // Refresh the data
//                    loadClothingItems(currentCategory);
//
//                    if (actionMode != null) {
//                        actionMode.finish();
//                    }
//
//                    Toast.makeText(this, deletedCount + " items deleted", Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    public void updateSelectionCount(int count) {
//        if (actionMode != null) {
//            actionMode.setTitle(count + " Selected");
//        }
//    }
    private ActionMode actionMode;

    public void startSelectionMode() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.selection_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.action_delete) {
                        deleteSelectedItems();
                        return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    adapter.clearSelection();
                    actionMode = null;
                }
            });
        }
    }

    private void deleteSelectedItems() {
        List<Long> selectedIds = adapter.getSelectedItemIds();
        if (selectedIds.isEmpty()) {
            return;
        }

        String message = selectedIds.size() == 1 ?
                "Are you sure you want to delete this item?" :
                "Are you sure you want to delete " + selectedIds.size() + " items?";

        new AlertDialog.Builder(this)
                .setTitle("Delete Selected Items")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Show progress dialog for large deletions
                    ProgressDialog progressDialog = null;
                    if (selectedIds.size() > 5) {
                        progressDialog = new ProgressDialog(this);
                        progressDialog.setMessage("Deleting items...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                    }

                    // Perform deletion in background
                    final ProgressDialog finalProgressDialog = progressDialog;
                    new Thread(() -> {
                        int deletedCount = dbHelper.deleteMultipleClothing(selectedIds);

                        runOnUiThread(() -> {
                            if (finalProgressDialog != null) {
                                finalProgressDialog.dismiss();
                            }

                            // Refresh the data
                            loadClothingItems(currentSubCategory);

                            if (actionMode != null) {
                                actionMode.finish();
                            }

                            Toast.makeText(this, deletedCount + " items deleted",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateSelectionCount(int count) {
        if (actionMode != null) {
            String title = count + " Selected";
            actionMode.setTitle(title);
        }
    }

}
