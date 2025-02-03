package com.example.test3.Groups;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.Subcategory.BottomNavActivity;
import com.example.test3.Subcategory.DatabaseHelper1;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GroupManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private DatabaseHelper1 dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        Log.d("ActivityLifecycle", "onCreate: " + getClass().getSimpleName());
        dbHelper = new DatabaseHelper1(this);

        recyclerView = findViewById(R.id.groups_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GroupAdapter(this);
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Group selectedGroup = adapter.getGroups().get(position);
                launchBottomNavActivity(Group.getId());
            }
        });



        FloatingActionButton fabAddGroup = findViewById(R.id.fab_add_group);
        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch AddGroupActivity
                Intent intent = new Intent(GroupManagementActivity.this, AddGroupActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        loadGroups();
        ////////////////////////////////////////////////////////

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Group group = adapter.getGroups().get(position);
                dbHelper.deleteGroup(String.valueOf(group.getId()));
                adapter.getGroups().remove(position);
                adapter.notifyItemRemoved(position);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);


        ////////////////////////////////////////////////////////
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Refresh the group list after adding a new group
            loadGroups();
        }
    }
    private void launchBottomNavActivity(int selectedGroupId) {
        Intent intent = new Intent(GroupManagementActivity.this, BottomNavActivity.class);
        intent.putExtra("SELECTED_GROUP_ID", String.valueOf(selectedGroupId));
        startActivity(intent);
    }

    private void loadGroups() {
        List<String> groups = dbHelper.getAllGroupNames();
        adapter.setGroups(groups);
    }
}