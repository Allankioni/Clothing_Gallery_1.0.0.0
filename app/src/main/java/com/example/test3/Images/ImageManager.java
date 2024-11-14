package com.example.test3.Images;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.test3.Subcategory.DatabaseHelper1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// ImageManager.java
public class ImageManager {
    private final Context context;
    private final DatabaseHelper1 dbHelper;
    private static final String IMAGES_DIR = "category_images";

    public ImageManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper1(context);
    }

    public String saveImage(Uri imageUri, String subcategoryId) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(context.getFilesDir(), IMAGES_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String filename = "IMG_" + System.currentTimeMillis() + "a122.jpg";
            File destination = new File(directory, filename);

            // Copy and compress the image
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            OutputStream output = new FileOutputStream(destination);

            Bitmap bitmap = BitmapFactory.decodeStream(input);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);

            // Clean up
            output.flush();
            output.close();
            input.close();

            // Save to database
            String imagePath = destination.getAbsolutePath();
            Image image = new Image(UUID.randomUUID().toString(), imagePath, subcategoryId);
            savePhotoToDatabase(image);

            return imagePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void savePhotoToDatabase(Image image) {
        DatabaseHelper1 dbHelper = new DatabaseHelper1(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", image.getId());
        values.put("url", image.getUrl());
        values.put("subcategory_id", image.getSubcategoryId());
        values.put("timestamp", image.getTimestamp());

        db.insert("photos", null, values);
    }

    public List<Image> getPhotosForSubcategory(String subcategoryId) {
        List<Image> images = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {"id", "url", "subcategory_id", "timestamp"};
        String selection = "subcategory_id = ?";
        String[] selectionArgs = {subcategoryId};

        Cursor cursor = db.query(
                "images",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                "timestamp DESC"
        );

        while (cursor.moveToNext()) {
            Image image = new Image(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("url")),
                    cursor.getString(cursor.getColumnIndexOrThrow("subcategory_id"))
            );
            image.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
            images.add(image);
        }

        cursor.close();
        return images;
    }

    public void deletePhoto(String photoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Get the photo URL before deletion
        String[] projection = {"url"};
        String selection = "id = ?";
        String[] selectionArgs = {photoId};

        Cursor cursor = db.query(
                "photos",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            String photoUrl = cursor.getString(cursor.getColumnIndexOrThrow("url"));

            // Delete the physical file
            File photoFile = new File(photoUrl);
            if (photoFile.exists()) {
                photoFile.delete();
            }

            // Delete from database
            db.delete("photos", "id = ?", new String[]{photoId});
        }

        cursor.close();
    }

    public Bitmap loadImage(String photoUrl) {
        return BitmapFactory.decodeFile(photoUrl);
    }
}
