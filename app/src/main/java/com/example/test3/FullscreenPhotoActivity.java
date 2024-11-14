package com.example.test3;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.test3.Images.ImageManager;
import com.example.test3.R;
import com.example.test3.databinding.ActivityFullscreenPhotoBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FullscreenPhotoActivity extends AppCompatActivity {
    private ActivityFullscreenPhotoBinding binding;
    private String photoUrl;
    private Bitmap currentBitmap;
    private boolean isImageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        // Setup action bar for sharing
        setSupportActionBar(binding.toolbar); // You'll need to add a toolbar to your layout
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoUrl = getIntent().getStringExtra("PHOTO_URL");
        setupFullscreenUI();
        loadImage();
        setupPhotoView();
    }

    private void setupFullscreenUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

//    private void loadImage() {
//        ImageView photoView = binding.fullscreenPhotoView;
//
//        // Load image using Glide
//        Glide.with(this)
//                .load(photoUrl)
//                .placeholder(R.drawable.fullscreen_default)
//                .into(photoView);
//
//        // Load bitmap for sharing
//        if (photoUrl != null) {
//            ImageManager imageManager = new ImageManager(this);
//            currentBitmap = imageManager.loadImage(photoUrl);
//            binding.fullscreenPhotoView.setImageBitmap(currentBitmap);
//        }
//    }

    private void loadImage() {
        ImageView photoView = binding.fullscreenPhotoView;

        // Use Glide with callback
        Glide.with(this)
                .asBitmap()  // Request as bitmap
                .load(photoUrl)
                .placeholder(R.drawable.download)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target, boolean isFirstResource) {
                        isImageLoaded = false;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model,
                                                   Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        currentBitmap = resource;
                        isImageLoaded = true;
                        return false;
                    }
                })
                .into(photoView);

        // Backup loading method using ImageManager
        if (photoUrl != null) {
            ImageManager imageManager = new ImageManager(this);
            // Run on background thread
            new Thread(() -> {
                Bitmap bitmap = imageManager.loadImage(photoUrl);
                if (bitmap != null && !isImageLoaded) {
                    runOnUiThread(() -> {
                        currentBitmap = bitmap;
                        binding.fullscreenPhotoView.setImageBitmap(bitmap);
                        isImageLoaded = true;
                    });
                }
            }).start();
        }
    }

    private void setupPhotoView() {
        binding.fullscreenPhotoView.setMaximumScale(5.0f);
        binding.fullscreenPhotoView.setMediumScale(4.0f);
        binding.fullscreenPhotoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        binding.fullscreenPhotoView.setAdjustViewBounds(true);

        supportPostponeEnterTransition();
        binding.fullscreenPhotoView.setOnMatrixChangeListener(rect -> {
            supportStartPostponedEnterTransition();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fullscreen_menu, menu); // You'll need to create this menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareImage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

private void shareImage() {
    // First try to get bitmap from ImageView if currentBitmap is null
    if (currentBitmap == null) {
        try {
            ImageView photoView = binding.fullscreenPhotoView;
            photoView.setDrawingCacheEnabled(true);
            currentBitmap = Bitmap.createBitmap(photoView.getDrawingCache());
            photoView.setDrawingCacheEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // If still null, try to reload the image
    if (currentBitmap == null) {
        Toast.makeText(this, "Preparing image for sharing...", Toast.LENGTH_SHORT).show();

        // Show loading indicator
        showLoadingDialog();

        // Attempt to reload the image
        Glide.with(this)
                .asBitmap()
                .load(photoUrl)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target, boolean isFirstResource) {
                        hideLoadingDialog();
                        Toast.makeText(FullscreenPhotoActivity.this,
                                "Failed to prepare image for sharing", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model,
                                                   Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        hideLoadingDialog();
                        currentBitmap = resource;
                        // Now proceed with sharing
                        proceedWithSharing();
                        return true;
                    }
                })
                .submit();
        return;
    }

    proceedWithSharing();
}
    private void proceedWithSharing() {
        try {
            File cachePath = new File(getCacheDir(), "shared_images");
            cachePath.mkdirs();

            File imageFile = new File(cachePath, "shared_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream stream = new FileOutputStream(imageFile);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();

            Uri imageUri = FileProvider.getUriForFile(this,
                    "com.example.test3.fileprovider",
                    imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Image"));

        } catch (IOException e) {
            Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Loading dialog methods
    private Dialog loadingDialog;

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(this);
            loadingDialog.setContentView(R.layout.dialog_loading);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }
}