package com.example.test3.Subcategory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper1 extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "photo_organizer.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    // Table name and columns
    public static final String TABLE_CLOTHES = "clothes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_CLOTHES_TABLE =
            "CREATE TABLE " + TABLE_CLOTHES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_URI + " TEXT NOT NULL, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    public DatabaseHelper1(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CLOTHES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLOTHES);
        onCreate(db);
    }

    // Existing methods remain the same...

    /**
     * Delete a single clothing item by ID
     * @param id The ID of the item to delete
     * @return true if deletion was successful
     */
    public boolean deleteClothing(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        // First get the image URI
        String imageUri = getImageUriById(id);

        if (imageUri != null) {
            // Delete the physical file
            success = deleteImageFile(imageUri);

            // Delete database entry regardless of file deletion success
            int rowsDeleted = db.delete(TABLE_CLOTHES,
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)});

            success = success && (rowsDeleted > 0);

            if (!success) {
                Log.e("DatabaseHelper1", "Failed to delete clothing item with ID: " + id);
            }
        }

        return success;
    }

    /**
     * Delete multiple clothing items
     * @param ids Array of item IDs to delete
     * @return Number of successfully deleted items
     */
    public int deleteMultipleClothing(List<Long> ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedCount = 0;

        db.beginTransaction();
        try {
            for (Long id : ids) {
                if (deleteClothing(id)) {
                    deletedCount++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper1", "Error during batch deletion", e);
        } finally {
            db.endTransaction();
        }

        return deletedCount;
    }
    public Cursor getClothingByType(String type,String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (type == null ) {
            throw new IllegalArgumentException("typeId cannot be null");
        }
        String[] columns = {COLUMN_ID, COLUMN_TYPE, COLUMN_IMAGE_URI, COLUMN_TIMESTAMP};
        return db.query(TABLE_CLOTHES,
                null,
                COLUMN_TYPE + "=? AND " + COLUMN_CATEGORY + "=?",
                new String[]{type, category},
                null, null,
                COLUMN_TIMESTAMP + " DESC");


    }
    public List<ClothingItem> getDefaultPlaceholderItems(String category, int count) {
        List<ClothingItem> placeholders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ClothingItem item = new ClothingItem();
            item.setType(category); // Set the appropriate category
            item.setImageUri("placeholder_" + i + ".jpg"); // Use a predefined placeholder image name
            item.setPlaceholder(true);
            placeholders.add(item);
        }
        return placeholders;
    }
    public long insertClothing(String type, String category, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_IMAGE_URI, imageUri);
        return db.insert(TABLE_CLOTHES, null, values);
    }

    /**
     * Delete all clothing items of a specific type and category
     */
    public int deleteClothingByTypeAndCategory(String type, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedCount = 0;

        // First get all image URIs
        Cursor cursor = getClothingByType(type, category);
        db.beginTransaction();
        try {
            while (cursor != null && cursor.moveToNext()) {
                String imageUri = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
                if (deleteImageFile(imageUri)) {
                    deletedCount++;
                }
            }

            // Delete all matching records from database
            db.delete(TABLE_CLOTHES,
                    COLUMN_TYPE + "=? AND " + COLUMN_CATEGORY + "=?",
                    new String[]{type, category});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper1", "Error deleting by type and category", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
        }

        return deletedCount;
    }

    /**
     * Get image URI by item ID
     */
    private String getImageUriById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String imageUri = null;

        Cursor cursor = db.query(TABLE_CLOTHES,
                new String[]{COLUMN_IMAGE_URI},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                imageUri = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper1", "Error getting image URI", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return imageUri;
    }

    /**
     * Delete the physical image file
     */
    private boolean deleteImageFile(String imageUri) {
        try {
            Uri uri = Uri.parse(imageUri);
            // Handle both file:// and content:// URIs
            if (uri.getScheme().equals("file")) {
                File imageFile = new File(uri.getPath());
                if (imageFile.exists()) {
                    return imageFile.delete();
                }
            } else if (uri.getScheme().equals("content")) {
                // For content URIs, delete file from app's private storage
                String fileName = uri.getLastPathSegment();
                File imageFile = new File(context.getFilesDir(), "images/" + fileName);
                if (imageFile.exists()) {
                    return imageFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper1", "Error deleting image file: " + imageUri, e);
        }
        return false;
    }


}