package com.example.test3.Homepage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.Groups.GroupManagementActivity;
import com.example.test3.Subcategory.BottomNavActivity;
import com.example.test3.Subcategory.DatabaseHelper1;
import com.example.test3.R;
import com.example.test3.VideoActivity;
import com.example.test3.databinding.ActivityMainBinding;

import java.util.ArrayList;
/* Import for handling potential null references */
import java.util.List;

public class MainActivity extends AppCompatActivity implements SelecteCategoryListener {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private ActivityMainBinding binding;
    private CategoryAdapter categoryAdapter;
    private DatabaseHelper1 dbHelper;
    private SQLiteDatabase database;
    private Button btn;
    private MediaPlayer sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ActivityLifecycle", "onCreate: " + getClass().getSimpleName());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        checkPermissions();
        playVideo();

        // Immersive mode setup (consider user preference for hiding navigation bar)
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        RecyclerView recyclerView = findViewById(R.id.categories_recycler_view);
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Gents", "Gents", R.drawable.adult_default));
        categories.add(new Category("Ladies", "Ladies", R.drawable.adult_default));
        categories.add(new Category("Children", "Children", R.drawable.child_default));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categories, getApplicationContext(), this);
        recyclerView.setAdapter(categoryAdapter);
    }

    private void playVideo() {
        btn = findViewById(R.id.button_welcome);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
            startActivity(intent);
            playSound(v);
        });
    }

    public void playSound(View view) {
        if (sp == null) { // Check for null reference before creating MediaPlayer
            sp = MediaPlayer.create(this, R.raw.clicked);
        }
        sp.setOnPreparedListener(mp -> mp.start());
        sp.setOnCompletionListener(mp -> mp.release());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("MainActivity", "Permission granted");
            } else {
                // Permission denied
                Log.d("MainActivity", "Permission denied");
            }
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            // For Android 12 and below
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    public void onCategorySelected(Category category) {
        Intent intent = new Intent(MainActivity.this, GroupManagementActivity.class);
        intent.putExtra("CATEGORY_TYPE", category.getName());
        startActivity(intent);


        Toast.makeText(this, category.getName(), Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "Category selected: " + category.getId());
    }
}