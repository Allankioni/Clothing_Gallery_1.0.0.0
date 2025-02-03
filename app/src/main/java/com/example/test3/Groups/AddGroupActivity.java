package com.example.test3.Groups;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.R;
import com.example.test3.Subcategory.DatabaseHelper1;

import java.io.IOException;

public class AddGroupActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private ImageView groupImageView;
    private Button saveButton;
    private DatabaseHelper1 dbHelper;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        dbHelper = new DatabaseHelper1(this);

        groupNameEditText = findViewById(R.id.group_name_edit_text);
        groupImageView = findViewById(R.id.group_image_view);
        saveButton = findViewById(R.id.save_button);

        groupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGroup();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                groupImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle image loading errors (e.g., set selectedImageUri to null)
                selectedImageUri = null;
            }
        }
    }

    private void saveGroup() {
        String groupName = groupNameEditText.getText().toString();

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupImageUri = "";
        if (selectedImageUri != null) {
            groupImageUri = selectedImageUri.toString();
        } else {
            // Set a default image URI (if applicable)
            groupImageUri = "android.resource://" + getPackageName() + "/" + R.drawable.place_holder;
        }

        String dummyCategory = "AnyCategory"; // Or any default category you prefer
        Log.d("AddGroupActivity", "groupImageUri: " + groupImageUri);

        long groupId = dbHelper.addGroup(groupName, groupImageUri, dummyCategory);

        if (groupId > 0) {
            // Group saved successfully
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error saving group", Toast.LENGTH_SHORT).show();
        }
    }

}