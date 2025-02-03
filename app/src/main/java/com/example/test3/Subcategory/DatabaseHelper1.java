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
    private static final int DATABASE_VERSION = 4;

    // Table name and columns
    private static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_GROUP_NAME = "group_name";
    public  static final String COLUMN_GROUP_IMAGE_URI = "group_image_uri";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String TABLE_GROUPS = "groups";
    private static final String COLUMN_GROUP_ID = "group_id";

    private static final String CREATE_GROUPS_TABLE =
            "CREATE TABLE " + TABLE_GROUPS + " (" +
                    COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
                    COLUMN_GROUP_IMAGE_URI + " TEXT" +
                    ")";

    private static final String CREATE_ITEMS_TABLE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_URI + " TEXT NOT NULL, " +
                    COLUMN_GROUP_NAME + " TEXT, " +
                    COLUMN_GROUP_IMAGE_URI + " TEXT, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    public final Context context;

    public DatabaseHelper1(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ITEMS_TABLE);
        db.execSQL(CREATE_GROUPS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema upgrades if necessary (e.g., if you need to remove the subcategory column)
    }

    public Cursor getItemsByGroupAndCategory(String category, String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CATEGORY + "=? AND " + COLUMN_GROUP_NAME + "=?";
        String[] selectionArgs = {category, groupName};
        return db.query(TABLE_ITEMS, null, selection, selectionArgs, null, null, null);
    }

    public Cursor getAllItemsByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CATEGORY + "=?";
        String[] selectionArgs = {category};
        return db.query(TABLE_ITEMS, null, selection, selectionArgs, null, null, null);
    }

    public long insertItem(String category, String imageUri, String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_IMAGE_URI, imageUri);
        values.put(COLUMN_GROUP_NAME, groupName);
        long newRowId = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return newRowId;
    }

    public long addGroup(String groupName, String groupImageUri, String dummyCategory) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, dummyCategory);
        values.put(COLUMN_GROUP_NAME, groupName);
        values.put(COLUMN_GROUP_IMAGE_URI, groupImageUri);
        Log.d("DatabaseHelper1", "Inserting group_image_uri: " + groupImageUri);

        long groupId = db.insert(TABLE_GROUPS, null, values);
        db.close();
        return groupId;
    }
    public boolean deleteGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_ITEMS, COLUMN_GROUP_NAME + "=?", new String[]{groupName});
        return rowsDeleted > 0;
    }
    public List<String> getAllGroupNames() {
        List<String> groupNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT " + COLUMN_GROUP_NAME + " FROM " + TABLE_ITEMS;
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String groupName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME));
            if (groupName != null) {
                groupNames.add(groupName);
            }
        }

        cursor.close();
        return groupNames;
    }
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
            int rowsDeleted = db.delete(TABLE_ITEMS,
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
        String[] columns = {COLUMN_ID, COLUMN_IMAGE_URI, COLUMN_TIMESTAMP};
        return db.query(TABLE_ITEMS,
                null,
                 "=? AND " + COLUMN_CATEGORY + "=?",
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
            db.delete(TABLE_ITEMS,
                     "=? AND " + COLUMN_CATEGORY + "=?",
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

        Cursor cursor = db.query(TABLE_ITEMS,
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