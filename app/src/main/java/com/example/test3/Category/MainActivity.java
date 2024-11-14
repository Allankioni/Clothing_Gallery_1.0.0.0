package com.example.test3.Category;

import android.Manifest;
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

import com.example.test3.Subcategory.BottomNavActivity;
import com.example.test3.Subcategory.DatabaseHelper1;
import com.example.test3.R;
import com.example.test3.VideoActivity;
import com.example.test3.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SelecteCategoryListener {
    private static final int STORAGE_PERMISSION_CODE = 101;
    private ActivityMainBinding binding;
    private CategoryAdapter categoryAdapter;
    private DatabaseHelper1 dbHelper;
    private SQLiteDatabase database;
    private Button btn;
    MediaPlayer sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        checkPermissions();
        playVideo();

        RecyclerView recyclerView = findViewById(R.id.categories_recycler_view);
        List<Category> categories = new ArrayList<Category>();
        categories.add(new Category("Adults","Adults", R.drawable.adult_default));
        categories.add(new Category("Children","Children", R.drawable.child_default));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CategoryAdapter(categories, getApplicationContext(), this));
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

        sp = MediaPlayer.create(this, R.raw.clicked);
        sp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                sp.start();
            }
        });
        sp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                sp.release();
            }
        });
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

        Intent intent = new Intent(MainActivity.this, BottomNavActivity.class);
        intent.putExtra("CATEGORY_TYPE", category.getName());
//        intent.putExtra("CATEGORY_ID", category.getId());
//        intent.putExtra("CATEGORY_NAME", category.getName());

        startActivity(intent);

        Toast.makeText(this,  category.getName(), Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "Category selected: " + category.getId());
    }
}
