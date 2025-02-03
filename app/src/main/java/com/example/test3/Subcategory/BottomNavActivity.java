package com.example.test3.Subcategory;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.view.ActionMode;

import com.example.test3.R;
import com.example.test3.databinding.ActivityBottomNavBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.Menu;
import android.view.MenuItem;

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
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private DatabaseHelper1 dbHelper;
    private ClothingAdapter adapter;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ActionMode actionMode;
    private MenuItem selectAllMenuItem;
    private String selectedGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBottomNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainCategory = getIntent().getStringExtra("CATEGORY_TYPE");
        if (mainCategory == null) {
            mainCategory = "Adults";
        }
        dbHelper = new DatabaseHelper1(this);

        selectedGroupName = getIntent().getStringExtra("SELECTED_GROUP_NAME");

        setupRecyclerView();
        setupFab();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.photos_recycler_view);
        adapter = new ClothingAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        loadClothingItems(mainCategory, selectedGroupName);
    }

    private void setupFab() {
        fab = findViewById(R.id.fab_add_photo);
        fab.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
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
                        saveImageToDatabase(imageUri, mainCategory, selectedGroupName);

                        int finalI = i;
                        runOnUiThread(() -> {
                            progressDialog.setProgress(finalI + 1);
                            if (finalI == totalImages - 1) {
                                progressDialog.dismiss();
                                loadClothingItems(mainCategory, selectedGroupName);
                            }
                        });
                    }
                }).start();
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                saveImageToDatabase(imageUri, mainCategory, selectedGroupName);
                loadClothingItems(mainCategory, selectedGroupName);
            }
        }
    }

    @SuppressLint("Range")
    private void loadClothingItems(String mainCategory, String selectedGroupName) {
        List<ClothingItem> items = new ArrayList<>();

        if (selectedGroupName != null) {
            Cursor cursor = dbHelper.getItemsByGroupAndCategory(mainCategory, selectedGroupName);

            while (cursor.moveToNext()) {
                ClothingItem item = new ClothingItem();
                item.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper1.COLUMN_ID)));
                item.setImageUri(cursor.getString(cursor.getColumnIndex(DatabaseHelper1.COLUMN_IMAGE_URI)));
                item.setHasValidImage(!cursor.isNull(cursor.getColumnIndex(DatabaseHelper1.COLUMN_IMAGE_URI)));
                // ... (
                items.add(item);
            }

            cursor.close();
        } else {
            Cursor cursor = dbHelper.getAllItemsByCategory(mainCategory);

            while (cursor.moveToNext()) {
                ClothingItem item = new ClothingItem();
                item.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper1.COLUMN_ID)));
                item.setImageUri(cursor.getString(cursor.getColumnIndex(DatabaseHelper1.COLUMN_IMAGE_URI)));
                item.setHasValidImage(!cursor.isNull(cursor.getColumnIndex(DatabaseHelper1.COLUMN_IMAGE_URI)));
                // ... (add other relevant data to ClothingItem) ...
                items.add(item);
            }

            cursor.close();
        }

        adapter.updateData(items);
    }

    private void saveImageToDatabase(Uri imageUri, String mainCategory, String selectedGroupName) {
        try {
            String persistentUri = makeImagePersistent(imageUri);
            dbHelper.insertItem(mainCategory, persistentUri, selectedGroupName);
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



    public void startSelectionMode() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getMenuInflater().inflate(R.menu.selection_menu, menu);
                    selectAllMenuItem = menu.findItem(R.id.action_select_all);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.action_select_all) {
                        if (adapter.areAllItemsSelected()) {
                            adapter.deselectAll();
                            selectAllMenuItem.setIcon(R.drawable.ic_select_all);
                        } else {
                            adapter.selectAll();
                            selectAllMenuItem.setIcon(R.drawable.baseline_deselect_24);
                        }
                    } else if (item.getItemId() == R.id.action_delete) {
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

        // Inflate the custom layout for the confirmation dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_deletion, null);

        // Get references to UI elements in the custom layout
        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView message = dialogView.findViewById(R.id.dialogMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Customize title and message based on the selected items
        title.setText("Delete Selected Items");
        message.setText(selectedIds.size() == 1 ?
                "Are you sure you want to delete this item?" :
                "Are you sure you want to delete " + selectedIds.size() + " items?");

        // Build the custom AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Set Cancel button action
        btnCancel.setOnClickListener(view -> dialog.dismiss());

        // Set Confirm button action
        btnConfirm.setOnClickListener(view -> {
            dialog.dismiss();

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
                    loadClothingItems(mainCategory, selectedGroupName);

                    if (actionMode != null) {
                        actionMode.finish();
                    }

                    Toast.makeText(this, deletedCount + " items deleted",
                            Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        // Show the custom dialog
        dialog.show();
    }


    public void updateSelectionCount(int count) {
        if (actionMode != null) {
            String title = count + " Selected";
            actionMode.setTitle(title);

            //update select all icon based on selection state
            if (selectAllMenuItem != null) {
                if (adapter.areAllItemsSelected()) {
                    selectAllMenuItem.setIcon(R.drawable.baseline_deselect_24);
                } else {
                    selectAllMenuItem.setIcon(R.drawable.ic_select_all);
                }
            }
        }
    }

}
